package typechecker_example.typechecker;

import typechecker_example.syntax.*;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;

public class Typechecker {
    // maps the name of the structure to its fields, and each of those
    // fields to its type
    private final Map<StructureName, LinkedHashMap<FieldName, Type>> structDecs;

    // maps each function name to its parameter types and return type
    private final Map<FunctionName, Pair<Type[], Type>> functionDefs;

    private Typechecker(final Program program) throws TypeErrorException {
        structDecs = makeStructMapping(program.structDecs);
        functionDefs = makeFunctionMapping(program.functionDefs);
        for (final FunctionDefinition def : program.functionDefs) {
            typecheckFunctionDef(def);
        }
    }

    // intended for testing
    public static Type expTypeForTesting(final Exp exp) throws TypeErrorException {
        final Typechecker checker =
            new Typechecker(new Program(new StructureDeclaration[0],
                                        new FunctionDefinition[0]));
        return checker.expTypeNoScopeForTesting(exp);
    }
    
    public static void typecheckProgram(final Program program) throws TypeErrorException {
        new Typechecker(program);
    }
    
    // not permitted to have multiple functions with the same name
    private static Map<FunctionName, Pair<Type[], Type>>
        makeFunctionMapping(final FunctionDefinition[] functions) throws TypeErrorException {

        final Map<FunctionName, Pair<Type[], Type>> result =
            new HashMap<FunctionName, Pair<Type[], Type>>();

        for (final FunctionDefinition def : functions) {
            final Type[] parameters = parameterTypes(def.parameters);
            final Pair<Type[], Type> value =
                new Pair<Type[], Type>(parameters, def.returnType);
            result.put(def.name, value);
        }

        if (result.size() != functions.length) {
            throw new TypeErrorException("Duplicate function name");
        }

        return result;
    }

    // throws exception if any are void
    private static Type[] parameterTypes(final VariableDeclaration[] vars) throws TypeErrorException {
        final Type[] result = new Type[vars.length];

        for (int index = 0; index < vars.length; index++) {
            final Type current = vars[index].type;
            ensureNonVoidType(current);
            result[index] = current;
        }

        return result;
    }

    private static void ensureNonVoidType(final Type type) throws TypeErrorException {
        if (type instanceof VoidType) {
            throw new TypeErrorException("Void type illegal here");
        }
    }
    
    // not permitted to have multiple structure declarations with the same name
    private static Map<StructureName, LinkedHashMap<FieldName, Type>>
        makeStructMapping(final StructureDeclaration[] structDecs) throws TypeErrorException {

        final Map<StructureName, LinkedHashMap<FieldName, Type>> result =
            new HashMap<StructureName, LinkedHashMap<FieldName, Type>>();

        for (final StructureDeclaration dec : structDecs) {
            final LinkedHashMap<FieldName, Type> fieldMapping =
                makeFieldMapping(dec.fields);
            result.put(dec.name, fieldMapping);
        }

        if (result.size() != structDecs.length) {
            throw new TypeErrorException("Duplicate structure name");
        }

        return result;
    }

    // not permitted to have repeated field names in the same structure
    // fields cannot have void types
    private static LinkedHashMap<FieldName, Type>
        makeFieldMapping(final VariableDeclaration[] fields) throws TypeErrorException {
        
        final LinkedHashMap<FieldName, Type> result =
            new LinkedHashMap<FieldName, Type>();

        for (final VariableDeclaration dec : fields) {
            ensureNonVoidType(dec.type);
            result.put(new FieldName(dec.variable.name), dec.type);
        }

        if (result.size() != fields.length) {
            throw new TypeErrorException("Duplicate field name");
        }

        return result;
    }
    
    private void typecheckFunctionDef(final FunctionDefinition fdef) throws TypeErrorException {
        final InScope initialScope = new InScope(fdef.returnType,
                                                 initialVariableMapping(fdef.parameters),
                                                 false);
        final Pair<InScope, Boolean> stmtResult = initialScope.typecheckStmt(fdef.body);

        if (!stmtResult.second.booleanValue() &&
            !(fdef.returnType instanceof VoidType)) {
            throw new TypeErrorException("Missing return in " + fdef.name.toString());
        }
    }

    // error if duplicate variable names are used
    private static Map<Variable, Type> initialVariableMapping(final VariableDeclaration[] parameters) throws TypeErrorException {
        final Map<Variable, Type> result = new HashMap<Variable, Type>();

        for (final VariableDeclaration dec : parameters) {
            result.put(dec.variable, dec.type);
        }

        if (result.size() != parameters.length) {
            throw new TypeErrorException("Duplicate variable name in function parameters");
        }

        return result;
    }
    
    private void checkMakeStructure(final StructureName name,
                                   final Type[] parameterTypes) throws TypeErrorException {
        final LinkedHashMap<FieldName, Type> expected = structDecs.get(name);

        if (expected != null) {
            if (expected.size() == parameterTypes.length) {
                int parameterIndex = 0;
                for (final Type expectedType : expected.values()) {
                    final Type receivedType = parameterTypes[parameterIndex++];
                    ensureTypesSame(expectedType, receivedType);
                }
            } else {
                throw new TypeErrorException("Expected " + expected.size() +
                                             " params; got " + parameterTypes.length + " params");
            }
        } else {
            throw new TypeErrorException("No such structure defined: " + name.toString());
        }
    }

    // returns the return type
    private Type checkFunctionCall(final FunctionName name,
                                   final Type[] parameterTypes) throws TypeErrorException {
        final Pair<Type[], Type> expected = functionDefs.get(name);

        if (expected != null) {
            if (parameterTypes.length == expected.first.length) {
                for (int index = 0; index < parameterTypes.length; index++) {
                    ensureTypesSame(expected.first[index], parameterTypes[index]);
                }
                return expected.second;
            } else {
                throw new TypeErrorException("Function " + name.toString() +
                                             " has arity " + expected.first.length +
                                             "; was called with " +
                                             expected.first.length + " parameters");
            }
        } else {
            throw new TypeErrorException("No such function defined: " + name.toString());
        }
    } // checkFunctionCall
    
    private static void ensureTypesSame(final Type expected, final Type received) throws TypeErrorException {
        if (!expected.equals(received)) {
            throw new TypeErrorException(expected, received);
        }
    }

    private static Type binopType(final Type left, final Op op, final Type right) throws TypeErrorException {
        final IntType intType = new IntType();
        if (op instanceof PlusOp) {
            // TWO kinds are permitted:
            // int + int: returns int
            // pointer + int: returns same pointer type
            //
            // in both cases, the right side is an int
            ensureTypesSame(intType, right);
            if (left instanceof IntType) {
                // int + int returns int
                return intType;
            } else if (left instanceof PointerType) {
                return left;
            } else {
                throw new TypeErrorException("invalid lhs for +: " + left.toString());
            }
        } else if (op instanceof MinusOp ||
                   op instanceof MultOp ||
                   op instanceof DivOp) {
            // int (-|*|/) int = int
            ensureTypesSame(intType, left);
            ensureTypesSame(intType, right);
            return intType;
        } else if (op instanceof EqualsOp) {
            // type == type = boolean
            // both need to be of the same type
            ensureTypesSame(left, right);
            return new BoolType();
        } else if (op instanceof LessThanOp) {
            // int < int = boolean
            ensureTypesSame(intType, left);
            ensureTypesSame(intType, right);
            return new BoolType();
        } else {
            // should be no other operators
            assert(false);
            throw new TypeErrorException("Unknown operator: " + op.toString());
        }
    } // binopType

    // intended for testing
    private Type expTypeNoScopeForTesting(final Exp exp) throws TypeErrorException {
        return new InScope(new VoidType(),
                           new HashMap<Variable, Type>(),
                           false).typeofExp(exp);
    }
    
    private class InScope {
        // return type of the function we are currently in
        private final Type returnType;
        // maps variables in scope to their corresponding type
        private final Map<Variable, Type> inScope;
        // records if we are in a while loop or not
        private final boolean inWhile;

        public InScope(final Type returnType,
                       final Map<Variable, Type> inScope,
                       final boolean inWhile) {
            this.returnType = returnType;
            this.inScope = inScope;
            this.inWhile = inWhile;
        }

        private InScope addVariable(final Variable variable,
                                   final Type variableType) {
            final Map<Variable, Type> copy =
                new HashMap<Variable, Type>(inScope);
            copy.put(variable, variableType);
            return new InScope(returnType, copy, inWhile);
        }

        private InScope setInWhile() {
            return new InScope(returnType, inScope, true);
        }
        
        private Type typeofAccess(final Type maybeStructureType,
                                 final FieldName field) throws TypeErrorException {
            if (maybeStructureType instanceof StructureType) {
                final StructureName name = ((StructureType)maybeStructureType).name;
                final LinkedHashMap<FieldName, Type> expected = structDecs.get(name);
                if (expected != null) {
                    final Type fieldType = expected.get(field);
                    if (fieldType != null) {
                        return fieldType;
                    } else {
                        throw new TypeErrorException("Structure " + name.toString() +
                                                     " does not have field " + field.toString());
                    }
                } else {
                    throw new TypeErrorException("No structure with name: " + name.toString());
                }
            } else {
                throw new TypeErrorException("Expected structure type; received: " +
                                             maybeStructureType.toString());
            }
        }

        private Type typeofDereference(final Type maybePointerType) throws TypeErrorException {
            if (maybePointerType instanceof PointerType) {
                // dereferencing a pointer yields whatever its underlying type is
                return ((PointerType)maybePointerType).pointsTo;
            } else {
                throw new TypeErrorException("Expected pointer type; received: " +
                                             maybePointerType.toString());
            }
        }
        
        private Type typeofLhs(final Lhs lhs) throws TypeErrorException {
            if (lhs instanceof VariableLhs) {
                return lookupVariable(((VariableLhs)lhs).variable);
            } else if (lhs instanceof FieldAccessLhs) {
                final FieldAccessLhs asAccess = (FieldAccessLhs)lhs;
                return typeofAccess(typeofLhs(asAccess.lhs),
                                    asAccess.field);
            } else if (lhs instanceof DereferenceLhs) {
                final Type nestedType =
                    typeofLhs(((DereferenceLhs)lhs).lhs);
                return typeofDereference(nestedType);
            } else {
                assert(false);
                throw new TypeErrorException("Unknown lhs: " +lhs.toString());
            }
        }
                    
        private Type[] typeofExps(final Exp[] exps) throws TypeErrorException {
            final Type[] types = new Type[exps.length];
            for (int index = 0; index < exps.length; index++) {
                types[index] = typeofExp(exps[index]);
            }
            return types;
        }

        // Look up the type of the variable.
        // If it's not present in the map, then it's not in scope.
        private Type lookupVariable(final Variable var) throws TypeErrorException {
            final Type varType = inScope.get(var);
            if (varType == null) {
                throw new TypeErrorException("Variable not in scope: " + var);
            }
            return varType;
        }
        
        public Type typeofExp(final Exp exp) throws TypeErrorException {
            if (exp instanceof IntExp) {
                return new IntType();
            } else if (exp instanceof CharExp) {
                return new CharType();
            } else if (exp instanceof BoolExp) {
                return new BoolType();
            } else if (exp instanceof VariableExp) {
                return lookupVariable(((VariableExp)exp).variable);
            } else if (exp instanceof MallocExp) {
                // Malloc takes an integer and returns void*
                final MallocExp asMalloc = (MallocExp)exp;
                ensureTypesSame(new IntType(),
                                typeofExp(asMalloc.amount));
                return new PointerType(new VoidType());
            } else if (exp instanceof SizeofExp) {
                // takes a type and returns an int
                // there is no sort of checking that can be done on the type
                return new IntType();
            } else if (exp instanceof BinopExp) {
                // the return type and expected parameter types all depend
                // on the operator.  In all cases, we need to get the types
                // of the operands, and then check if this matches with the
                // operator
                final BinopExp asBinop = (BinopExp)exp;
                final Type leftType = typeofExp(asBinop.left);
                final Type rightType = typeofExp(asBinop.right);
                return binopType(leftType, asBinop.op, rightType);
            } else if (exp instanceof MakeStructureExp) {
                final MakeStructureExp asStruct = (MakeStructureExp)exp;
                checkMakeStructure(asStruct.name,
                                   typeofExps(asStruct.parameters));
                return new StructureType(asStruct.name);
            } else if (exp instanceof FunctionCallExp) {
                final FunctionCallExp asCall = (FunctionCallExp)exp;
                return checkFunctionCall(asCall.name,
                                         typeofExps(asCall.parameters));
            } else if (exp instanceof CastExp) {
                // Explicit cast.  Trust the user.  Ideally, we'd check
                // this at runtime.  We still need to look at the expression
                // to make sure that this is itself well-typed.
                final CastExp asCast = (CastExp)exp;
                typeofExp(asCast.exp);
                return asCast.type;
            } else if (exp instanceof AddressOfExp) {
                final Type nested = typeofLhs(((AddressOfExp)exp).lhs);
                // point to this now
                return new PointerType(nested);
            } else if (exp instanceof DereferenceExp) {
                final Type nested = typeofExp(((DereferenceExp)exp).exp);
                return typeofDereference(nested);
            } else if (exp instanceof FieldAccessExp) {
                final FieldAccessExp asAccess = (FieldAccessExp)exp;
                return typeofAccess(typeofExp(asAccess.exp),
                                    asAccess.field);
            } else {
                assert(false);
                throw new TypeErrorException("Unrecognized expression: " + exp.toString());
            }
        } // typeofExp

        // returns any new scope to use, along with whether or not return was observed on
        // all paths
        public Pair<InScope, Boolean> typecheckStmt(final Stmt stmt) throws TypeErrorException {
            if (stmt instanceof IfStmt) {
                final IfStmt asIf = (IfStmt)stmt;
                ensureTypesSame(new BoolType(), typeofExp(asIf.guard));

                // since the true and false branches form their own blocks, we
                // don't care about any variables they put in scope
                final Pair<InScope, Boolean> leftResult = typecheckStmt(asIf.ifTrue);
                final Pair<InScope, Boolean> rightResult = typecheckStmt(asIf.ifFalse);
                final boolean returnOnBoth =
                    leftResult.second.booleanValue() && rightResult.second.booleanValue();
                return new Pair<InScope, Boolean>(this, returnOnBoth);
            } else if (stmt instanceof WhileStmt) {
                final WhileStmt asWhile = (WhileStmt)stmt;
                ensureTypesSame(new BoolType(), typeofExp(asWhile.guard));

                // Don't care about variables in the while.
                // Because the body of the while loop will never execute if the condition is
                // initially false, even if all paths in the while loop have return, this doesn't
                // mean that we are guaranteed to hit return.
                setInWhile().typecheckStmt(asWhile.body);
                return new Pair<InScope, Boolean>(this, Boolean.valueOf(false));
            } else if (stmt instanceof BreakStmt ||
                       stmt instanceof ContinueStmt) {
                if (!inWhile) {
                    throw new TypeErrorException("Break or continue outside of loop");
                }
                return new Pair<InScope, Boolean>(this, Boolean.valueOf(false));
            } else if (stmt instanceof VariableDeclarationInitializationStmt) {
                final VariableDeclarationInitializationStmt dec =
                    (VariableDeclarationInitializationStmt)stmt;
                final Type expectedType = dec.varDec.type;
                ensureNonVoidType(expectedType);
                ensureTypesSame(expectedType,
                                typeofExp(dec.exp));
                final InScope resultInScope =
                    addVariable(dec.varDec.variable, expectedType);
                return new Pair<InScope, Boolean>(resultInScope, Boolean.valueOf(false));
            } else if (stmt instanceof ReturnVoidStmt) {
                ensureTypesSame(new VoidType(), returnType);
                return new Pair<InScope, Boolean>(this, Boolean.valueOf(true));
            } else if (stmt instanceof ReturnExpStmt) {
                final Type receivedType = typeofExp(((ReturnExpStmt)stmt).exp);
                ensureTypesSame(returnType, receivedType);
                return new Pair<InScope, Boolean>(this, Boolean.valueOf(true));
            } else if (stmt instanceof FreeStmt) {
                ensureTypesSame(new PointerType(new VoidType()),
                                typeofExp(((FreeStmt)stmt).value));
                return new Pair<InScope, Boolean>(this, Boolean.valueOf(false));
            } else if (stmt instanceof SequenceStmt) {
                final SequenceStmt asSeq = (SequenceStmt)stmt;
                final Pair<InScope, Boolean> fromLeft = typecheckStmt(asSeq.first);
                
                if (fromLeft.second.booleanValue()) {
                    throw new TypeErrorException("Dead code from early return");
                }

                return fromLeft.first.typecheckStmt(asSeq.second);
            } else if (stmt instanceof ExpStmt) {
                // Just need to check that it's well-typed.  Permitted to
                // return anything.
                typeofExp(((ExpStmt)stmt).exp);
                return new Pair<InScope, Boolean>(this, Boolean.valueOf(false));
            } else {
                assert(false);
                throw new TypeErrorException("Unrecognized statement: " + stmt.toString());
            }
        } // typecheckStmt
    } // InScope
}

