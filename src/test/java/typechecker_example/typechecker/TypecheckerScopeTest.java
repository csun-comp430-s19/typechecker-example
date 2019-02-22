package typechecker_example.typechecker;

import typechecker_example.syntax.*;

import org.junit.Test;

public class TypecheckerScopeTest {
    public static final StructureDeclaration[] EMPTY_STRUCTURES =
        new StructureDeclaration[0];
    public static final FunctionDefinition[] EMPTY_FUNCTIONS =
        new FunctionDefinition[0];
    public static final VariableDeclaration[] EMPTY_VARDECS =
        new VariableDeclaration[0];
    
    public static Stmt stmts(final Stmt... input) {
        assert(input.length > 0);
        Stmt result = input[input.length - 1];
        for (int index = input.length - 2; index >= 0; index--) {
            result = new SequenceStmt(input[index], result);
        }
        return result;
    }

    public static VariableDeclarationInitializationStmt def(final Type type, final String name, final Exp exp) {
        return new VariableDeclarationInitializationStmt(new VariableDeclaration(type, new Variable(name)), exp);
    }

    // void foo() {
    //   body
    // }
    public static FunctionDefinition voidFunction(final Stmt body) {
        return new FunctionDefinition(new VoidType(),
                                      new FunctionName("foo"),
                                      EMPTY_VARDECS,
                                      body);
    }
    
    @Test
    public void testVariableDefinitionAndUse() throws TypeErrorException {
        // void foo() {
        //   int x = 0;
        //   int y = x;
        // }
        final Stmt body = stmts(def(new IntType(), "x", new IntExp(0)),
                                def(new IntType(), "y", new VariableExp(new Variable("x"))));
        final FunctionDefinition fdef = voidFunction(body);
        final Program prog = new Program(EMPTY_STRUCTURES,
                                         new FunctionDefinition[]{fdef});
        Typechecker.typecheckProgram(prog);
    }

    @Test(expected = TypeErrorException.class)
    public void testAccessUndeclaredVariable() throws TypeErrorException {
        // void foo() {
        //   int x = x;
        // }
        final Stmt body = stmts(def(new IntType(), "x", new VariableExp(new Variable("x"))));
        final FunctionDefinition fdef = voidFunction(body);
        final Program prog = new Program(EMPTY_STRUCTURES,
                                         new FunctionDefinition[]{fdef});
        Typechecker.typecheckProgram(prog);
    }

    @Test
    public void testPointerToVariable() throws TypeErrorException {
        // void foo() {
        //   int x = 0;
        //   int* y = &x;
        // }
        final Stmt body = stmts(def(new IntType(), "x", new IntExp(0)),
                                def(new PointerType(new IntType()),
                                    "y",
                                    new AddressOfExp(new VariableLhs(new Variable("x")))));
        final FunctionDefinition fdef = voidFunction(body);
        final Program prog = new Program(EMPTY_STRUCTURES,
                                         new FunctionDefinition[]{fdef});
        Typechecker.typecheckProgram(prog);
    }

    @Test
    public void testAddPointer() throws TypeErrorException {
        // void foo() {
        //   int x = 0;
        //   int* y = &x;
        //   int* z = y + 3;
        // }
        final Stmt body = stmts(def(new IntType(), "x", new IntExp(0)),
                                def(new PointerType(new IntType()),
                                    "y",
                                    new AddressOfExp(new VariableLhs(new Variable("x")))),
                                def(new PointerType(new IntType()),
                                    "z",
                                    new BinopExp(new VariableExp(new Variable("y")),
                                                 new PlusOp(),
                                                 new IntExp(3))));
        final FunctionDefinition fdef = voidFunction(body);
        final Program prog = new Program(EMPTY_STRUCTURES,
                                         new FunctionDefinition[]{fdef});
        Typechecker.typecheckProgram(prog);
    }

    @Test
    public void testNormalStructureCreation() throws TypeErrorException {
        // Foo {
        //   int x;
        //   char y;
        // };
        // void foo() {
        //   Foo f = Foo(7, 'a');
        // }
        final StructureName sname = new StructureName("Foo");
        final StructureDeclaration sdef =
            new StructureDeclaration(sname,
                                     new VariableDeclaration[]{
                                         new VariableDeclaration(new IntType(), new Variable("x")),
                                         new VariableDeclaration(new CharType(), new Variable("y"))
                                     });
        final Stmt body = stmts(def(new StructureType(sname),
                                    "f",
                                    new MakeStructureExp(sname,
                                                         new Exp[]{
                                                             new IntExp(7),
                                                             new CharExp('a')
                                                         })));
        final FunctionDefinition fdef = voidFunction(body);
        final Program prog = new Program(new StructureDeclaration[]{sdef},
                                         new FunctionDefinition[]{fdef});
        Typechecker.typecheckProgram(prog);
    }

    @Test(expected = TypeErrorException.class)
    public void testStructureDuplicateFields() throws TypeErrorException {
        // Foo {
        //   int x;
        //   char x;
        // };
        final StructureName sname = new StructureName("Foo");
        final StructureDeclaration sdef =
            new StructureDeclaration(sname,
                                     new VariableDeclaration[]{
                                         new VariableDeclaration(new IntType(), new Variable("x")),
                                         new VariableDeclaration(new CharType(), new Variable("x"))
                                     });
        final Program prog = new Program(new StructureDeclaration[]{sdef},
                                         EMPTY_FUNCTIONS);
        Typechecker.typecheckProgram(prog);
    }

    @Test(expected = TypeErrorException.class)
    public void testStructureCreationTooManyParams() throws TypeErrorException {
        // Foo {
        //   int x;
        //   char y;
        // };
        // void foo() {
        //   Foo f = Foo(7, 'a', 'b');
        // }
        final StructureName sname = new StructureName("Foo");
        final StructureDeclaration sdef =
            new StructureDeclaration(sname,
                                     new VariableDeclaration[]{
                                         new VariableDeclaration(new IntType(), new Variable("x")),
                                         new VariableDeclaration(new CharType(), new Variable("y"))
                                     });
        final Stmt body = stmts(def(new StructureType(sname),
                                    "f",
                                    new MakeStructureExp(sname,
                                                         new Exp[]{
                                                             new IntExp(7),
                                                             new CharExp('a'),
                                                             new CharExp('b')
                                                         })));
        final FunctionDefinition fdef = voidFunction(body);
        final Program prog = new Program(new StructureDeclaration[]{sdef},
                                         new FunctionDefinition[]{fdef});
        Typechecker.typecheckProgram(prog);
    }

    @Test(expected = TypeErrorException.class)
    public void testStructureCreationWrongParamTypes() throws TypeErrorException {
        // Foo {
        //   int x;
        //   char y;
        // };
        // void foo() {
        //   Foo f = Foo('a', 7);
        // }
        final StructureName sname = new StructureName("Foo");
        final StructureDeclaration sdef =
            new StructureDeclaration(sname,
                                     new VariableDeclaration[]{
                                         new VariableDeclaration(new IntType(), new Variable("x")),
                                         new VariableDeclaration(new CharType(), new Variable("y"))
                                     });
        final Stmt body = stmts(def(new StructureType(sname),
                                    "f",
                                    new MakeStructureExp(sname,
                                                         new Exp[]{
                                                             new CharExp('a'),
                                                             new IntExp(7)
                                                         })));
        final FunctionDefinition fdef = voidFunction(body);
        final Program prog = new Program(new StructureDeclaration[]{sdef},
                                         new FunctionDefinition[]{fdef});
        Typechecker.typecheckProgram(prog);
    }

    @Test
    public void testNormalStructureAccess() throws TypeErrorException {
        // Foo {
        //   int x;
        //   char y;
        // };
        // void foo() {
        //   Foo f = Foo(7, 'a');
        //   int g = f.x;
        // }
        final StructureName sname = new StructureName("Foo");
        final StructureDeclaration sdef =
            new StructureDeclaration(sname,
                                     new VariableDeclaration[]{
                                         new VariableDeclaration(new IntType(), new Variable("x")),
                                         new VariableDeclaration(new CharType(), new Variable("y"))
                                     });
        final Stmt body = stmts(def(new StructureType(sname),
                                    "f",
                                    new MakeStructureExp(sname,
                                                         new Exp[]{
                                                             new IntExp(7),
                                                             new CharExp('a')
                                                         })),
                                def(new IntType(),
                                    "g",
                                    new FieldAccessExp(new VariableExp(new Variable("f")),
                                                       new FieldName("x"))));
        final FunctionDefinition fdef = voidFunction(body);
        final Program prog = new Program(new StructureDeclaration[]{sdef},
                                         new FunctionDefinition[]{fdef});
        Typechecker.typecheckProgram(prog);
    }

    @Test(expected = TypeErrorException.class)
    public void testStructureAccessNonexistentField() throws TypeErrorException {
        // Foo {
        //   int x;
        //   char y;
        // };
        // void foo() {
        //   Foo f = Foo(7, 'a');
        //   int g = f.z;
        // }
        final StructureName sname = new StructureName("Foo");
        final StructureDeclaration sdef =
            new StructureDeclaration(sname,
                                     new VariableDeclaration[]{
                                         new VariableDeclaration(new IntType(), new Variable("x")),
                                         new VariableDeclaration(new CharType(), new Variable("y"))
                                     });
        final Stmt body = stmts(def(new StructureType(sname),
                                    "f",
                                    new MakeStructureExp(sname,
                                                         new Exp[]{
                                                             new IntExp(7),
                                                             new CharExp('a')
                                                         })),
                                def(new IntType(),
                                    "g",
                                    new FieldAccessExp(new VariableExp(new Variable("f")),
                                                       new FieldName("z"))));
        final FunctionDefinition fdef = voidFunction(body);
        final Program prog = new Program(new StructureDeclaration[]{sdef},
                                         new FunctionDefinition[]{fdef});
        Typechecker.typecheckProgram(prog);
    }

    @Test
    public void testNormalStructurePointerToField() throws TypeErrorException {
        // Foo {
        //   int x;
        //   char y;
        // };
        // void foo() {
        //   Foo f = Foo(7, 'a');
        //   int* g = &f.x;
        // }
        final StructureName sname = new StructureName("Foo");
        final StructureDeclaration sdef =
            new StructureDeclaration(sname,
                                     new VariableDeclaration[]{
                                         new VariableDeclaration(new IntType(), new Variable("x")),
                                         new VariableDeclaration(new CharType(), new Variable("y"))
                                     });
        final Stmt body = stmts(def(new StructureType(sname),
                                    "f",
                                    new MakeStructureExp(sname,
                                                         new Exp[]{
                                                             new IntExp(7),
                                                             new CharExp('a')
                                                         })),
                                def(new PointerType(new IntType()),
                                    "g",
                                    new AddressOfExp(new FieldAccessLhs(new VariableLhs(new Variable("f")),
                                                                        new FieldName("x")))));
        final FunctionDefinition fdef = voidFunction(body);
        final Program prog = new Program(new StructureDeclaration[]{sdef},
                                         new FunctionDefinition[]{fdef});
        Typechecker.typecheckProgram(prog);
    }

    @Test
    public void testNormalFunctionCall() throws TypeErrorException {
        // int blah(int x, char y) {
        //   return 7;
        // }
        // void foo() {
        //   blah(7, 'a');
        // }

        final FunctionDefinition blah =
            new FunctionDefinition(new IntType(),
                                   new FunctionName("blah"),
                                   new VariableDeclaration[]{
                                       new VariableDeclaration(new IntType(), new Variable("x")),
                                       new VariableDeclaration(new CharType(), new Variable("y"))
                                   },
                                   new ReturnExpStmt(new IntExp(7)));
        final FunctionDefinition foo =
            voidFunction(new ExpStmt(new FunctionCallExp(new FunctionName("blah"),
                                                         new Exp[]{
                                                             new IntExp(7),
                                                             new CharExp('a')
                                                         })));
        final Program prog = new Program(EMPTY_STRUCTURES,
                                         new FunctionDefinition[]{blah, foo});
        Typechecker.typecheckProgram(prog);
    }

    @Test(expected = TypeErrorException.class)
    public void testFunctionCallNotEnoughParams() throws TypeErrorException {
        // int blah(int x, char y) {
        //   return 7;
        // }
        // void foo() {
        //   blah(7);
        // }
        final FunctionDefinition blah =
            new FunctionDefinition(new IntType(),
                                   new FunctionName("blah"),
                                   new VariableDeclaration[]{
                                       new VariableDeclaration(new IntType(), new Variable("x")),
                                       new VariableDeclaration(new CharType(), new Variable("y"))
                                   },
                                   new ReturnExpStmt(new IntExp(7)));
        final FunctionDefinition foo =
            voidFunction(new ExpStmt(new FunctionCallExp(new FunctionName("blah"),
                                                         new Exp[]{
                                                             new IntExp(7)
                                                         })));
        final Program prog = new Program(EMPTY_STRUCTURES,
                                         new FunctionDefinition[]{blah, foo});
        Typechecker.typecheckProgram(prog);
    }
        
    @Test(expected = TypeErrorException.class)
    public void testFunctionCallTooManyParams() throws TypeErrorException {
        // int blah(int x, char y) {
        //   return 7;
        // }
        // void foo() {
        //   blah(7, 'a', true);
        // }

        final FunctionDefinition blah =
            new FunctionDefinition(new IntType(),
                                   new FunctionName("blah"),
                                   new VariableDeclaration[]{
                                       new VariableDeclaration(new IntType(), new Variable("x")),
                                       new VariableDeclaration(new CharType(), new Variable("y"))
                                   },
                                   new ReturnExpStmt(new IntExp(7)));
        final FunctionDefinition foo =
            voidFunction(new ExpStmt(new FunctionCallExp(new FunctionName("blah"),
                                                         new Exp[]{
                                                             new IntExp(7),
                                                             new CharExp('a'),
                                                             new BoolExp(true)
                                                         })));
        final Program prog = new Program(EMPTY_STRUCTURES,
                                         new FunctionDefinition[]{blah, foo});
        Typechecker.typecheckProgram(prog);
    }

    @Test(expected = TypeErrorException.class)
    public void testFunctionCallWrongTypes() throws TypeErrorException {
        // int blah(int x, char y) {
        //   return 7;
        // }
        // void foo() {
        //   blah('a', y);
        // }

        final FunctionDefinition blah =
            new FunctionDefinition(new IntType(),
                                   new FunctionName("blah"),
                                   new VariableDeclaration[]{
                                       new VariableDeclaration(new IntType(), new Variable("x")),
                                       new VariableDeclaration(new CharType(), new Variable("y"))
                                   },
                                   new ReturnExpStmt(new IntExp(7)));
        final FunctionDefinition foo =
            voidFunction(new ExpStmt(new FunctionCallExp(new FunctionName("blah"),
                                                         new Exp[]{
                                                             new CharExp('a'),
                                                             new IntExp(7)
                                                         })));
        final Program prog = new Program(EMPTY_STRUCTURES,
                                         new FunctionDefinition[]{blah, foo});
        Typechecker.typecheckProgram(prog);
    }

    @Test(expected = TypeErrorException.class)
    public void testFunctionCallNonexistent() throws TypeErrorException {
        // void foo() {
        //   blah(7, 'a');
        // }

        final FunctionDefinition foo =
            voidFunction(new ExpStmt(new FunctionCallExp(new FunctionName("blah"),
                                                         new Exp[]{
                                                             new IntExp(7),
                                                             new CharExp('a')
                                                         })));
        final Program prog = new Program(EMPTY_STRUCTURES,
                                         new FunctionDefinition[]{foo});
        Typechecker.typecheckProgram(prog);
    }

    @Test
    public void testIfNormal() throws TypeErrorException {
        // void foo() {
        //   if (true) {
        //     return;
        //   } else {
        //     return;
        //   }
        // }

        final FunctionDefinition foo =
            voidFunction(new IfStmt(new BoolExp(true),
                                    new ReturnVoidStmt(),
                                    new ReturnVoidStmt()));
        final Program prog = new Program(EMPTY_STRUCTURES,
                                         new FunctionDefinition[]{foo});
        Typechecker.typecheckProgram(prog);
    }   
}
