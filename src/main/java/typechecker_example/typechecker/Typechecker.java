package typechecker_example.typechecker;

import java.util.Map;
import typechecker_example.syntax.*;

class Typechecker {
    // begin instance variables
    private final Map<FunctionName, FunctionDefinition> functions;
    private final Map<StructureName, StructureDeclaration> structures;
    
    public Typechecker(final Map<FunctionName, FunctionDefinition> functions,
                       final Map<StructureName, StructureDeclaration> structures) {
        this.functions = functions;
        this.structures = structures;
    }
    
    public void typecheck(final Program prog) throws TypeErrorException {
    }

    // inLoop: indicates if this statement is in a loop
    public Map<Variable,Type> typecheckStatement(final Map<Variable, Type> env, final Stmt s, final boolean inLoop) throws TypeErrorException {
        if (s instanceof WhileStmt) {
            final WhileStmt asWhile = (WhileStmt)s;
            final Type guardType = typeofExp(env, asWhile.guard);
            ensureTypesSame(new BoolType(), guardType);
            typecheckStatement(env, s.body, true);
            return env;
        } else if (s instanceof BreakStmt) {
            if (!inLoop) {
                throw new TypeErrorException("Break outside of loop");
            }
            return env;
        } else if (s instanceof VariableDeclarationInitializationStmt) {
            final VariableDeclarationInitializationStmt asDec =
                (VariableDeclarationInitializationStmt)s;
            ensureTypesSame(asDec.varDec.type, typeofExp(env, asDec.exp));
            
            final Map<Variable, Type> newEnv = Map(env);
            newEnv.put(asDec.varDec.variable, asDec.varDec.type);

            return newEnv;
    public void ensureTypesSame(final Type expected, final Type actual) throws TypeErrorException {
        if (!expected.equals(actual)) {
            throw new TypeErrorException("expected: " + expected.toString() +
                                         " got: " + actual.toString());
        }
    } // ensureTypesSame
    
    // env = environment = type environment
    public Type typeofExp(final Map<Variable, Type> env, final Exp e) throws TypeErrorException {
        if (e instanceof IntExp) {
            return new IntType();
        } else if (e instanceof CharExp) {
            return new CharType();
        } else if (e instanceof BoolExp) {
            return new BoolType();
        } else if (e instanceof VariableExp) {
            final VariableExp asVar = (VariableExp)e;
            final Variable var = asVar.variable;
            final Type retType = env.get(var);
            if (retType == null) {
                throw new TypeErrorException("variable not defined: " + var.toString());
            } else {
                return retType;
            }
        } else if (e instanceof MallocExp) {
            final MallocExp asMalloc = (MallocExp)e;
            final Exp subexpression = asMalloc.amount;
            final Type subexpressionType = typeofExp(env, subexpression);
            ensureTypesSame(new IntType(), subexpressionType);
            return new PointerType(new VoidType());
        } else if (e instanceof FreeExp) {
            final FreeExp asFree = (FreeExp)e;
            final Exp subexpression = asFree.value;
            final Type subexpressionType = typeofExp(env, subexpression);
            ensureTypesSame(new PointerType(new VoidType()), subexpressionType);
            return new VoidType();
        } else if (e instanceof BinopExp) {
            final BinopExp asBinop = (BinopExp)e;
            final Type leftType = typeofExp(env, asBinop.left);
            final Type rightType = typeofExp(env, asBinop.right);

            if (op instanceof PlusOp ||
                op instanceof MinusOp ||
                op instanceof MultOp ||
                op instanceof DivOp) {
                ensureTypesSame(new IntType(), leftType);
                ensureTypesSame(new IntType(), rightType);
                return new IntType();
            } else if (op instanceof EqualsOp) {
                ensureTypesSame(leftType, rightType);
                return new BoolType();
            } // TODO: need less than
        } else if (e instanceof FunctionCallExp) {
            final FunctionCallExp asFunc = (FunctionCallExp)e;
            final FunctionDefinition def = functions.get(asFunc.name);
            if (def == null) {
                throw new TypeErrorException("Function not defined: " + asFunc.name);
            } else {
                if (def.parameters.length != asFunc.parameters.length) {
                    throw new TypeErrorException("Function call has wrong arity: " + asFunc.name);
                } else {
                    for (int index = 0; index < def.parameters; index++) {
                        final Exp param = asFunc.parameters[index];
                        final VariableDeclaration varDec = def.parameters[index];
                        ensureTypesSame(varDec.type, typeofExp(env, param));
                    }
                    return def.returnType;
                }
            }
        } else if (e instanceof CastExp) {
            final CastExp asCast = (CastExp)e;
            if (typeofExp(asCast.exp) instanceof VoidType) {
                throw new TypeError("Cannot cast void: " + asCase.exp);
            } else {
                return e.type;
            }
        } else if (e instanceof FieldAccessExp) {
            final FieldAccessExp asAccess = (FieldAccessExp)e;
            final Type expType = typeofExp(asAccess.exp);

            if (expType instanceof StructureType) {
                final StructureName structName = ((StructureType)expType).name;
                final StructureDeclaration dec = structures.get(structName);
                if (dec == null) {
                    throw new TypeError("No such structure with name: " + dec.name);
                } else {
                    final FieldName fieldBeingAccessed = asAccess.field;
                    for (final VariableDeclaration field : dec.fields) {
                        if (fieldBeingAccessed.name.equals(field.variable.name)) {
                            return field.type;
                        }
                    }
                    throw new TypeError("No such field defined: " + fieldBeingAccessed.name);
                }
            } else {
                throw new TypeError("Expected structure; got: " + expType.toString());
            }
        }
    } // typeofExp
} // Typechecker

