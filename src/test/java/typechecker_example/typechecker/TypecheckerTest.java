package typechecker_example.typechecker;

import typechecker_example.syntax.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TypecheckerTest {
    // use null if there should be a type error
    public void assertExpType(final Type expected, final Exp exp) {
        try {
            final Type received = Typechecker.expTypeForTesting(exp);
            assertTrue("Expected type error; got: " + received.toString(),
                       expected != null);
            assertEquals(expected, received);
        } catch (final TypeErrorException e) {
            assertTrue("Unexpected type error: " + e.getMessage(),
                       expected == null);
        }
    }

    @Test
    public void testIntExp() {
        assertExpType(new IntType(),
                      new IntExp(42));
    }

    @Test
    public void testCharExp() {
        assertExpType(new CharType(),
                      new CharExp('a'));
    }

    @Test
    public void testBoolExp() {
        assertExpType(new BoolType(),
                      new BoolExp(true));
    }

    // TODO: variables need a scope

    @Test
    public void testMallocWithInt() {
        assertExpType(new PointerType(new VoidType()),
                      new MallocExp(new IntExp(42)));
    }

    @Test
    public void testMallocWithNonInt() {
        assertExpType(null,
                      new MallocExp(new BoolExp(false)));
    }

    @Test
    public void testSizeof() {
        assertExpType(new IntType(),
                      new SizeofExp(new BoolType()));
    }

    @Test
    public void testBinopPlusInts() {
        assertExpType(new IntType(),
                      new BinopExp(new IntExp(1),
                                   new PlusOp(),
                                   new IntExp(2)));
    }

    // TODO: need variables to get pointers

    @Test
    public void testBinopPlusNonIntOrPointer() {
        assertExpType(null,
                      new BinopExp(new CharExp('a'),
                                   new PlusOp(),
                                   new IntExp(1)));
    }

    @Test
    public void testMinusInts() {
        assertExpType(new IntType(),
                      new BinopExp(new IntExp(1),
                                   new MinusOp(),
                                   new IntExp(2)));
    }

    @Test
    public void testMinusNonInts() {
        assertExpType(null,
                      new BinopExp(new IntExp(1),
                                   new MinusOp(),
                                   new CharExp('a')));
    }

    @Test
    public void testMultInts() {
        assertExpType(new IntType(),
                      new BinopExp(new IntExp(1),
                                   new MultOp(),
                                   new IntExp(2)));
    }

    @Test
    public void testMultNonInts() {
        assertExpType(null,
                      new BinopExp(new IntExp(1),
                                   new MultOp(),
                                   new CharExp('a')));
    }

    @Test
    public void testDivInts() {
        assertExpType(new IntType(),
                      new BinopExp(new IntExp(1),
                                   new DivOp(),
                                   new IntExp(2)));
    }

    @Test
    public void testDivNonInts() {
        assertExpType(null,
                      new BinopExp(new IntExp(1),
                                   new DivOp(),
                                   new CharExp('a')));
    }

    @Test
    public void testEqualSameType() {
        assertExpType(new BoolType(),
                      new BinopExp(new CharExp('a'),
                                   new EqualsOp(),
                                   new CharExp('b')));
    }

    @Test
    public void testEqualDifferentTypes() {
        assertExpType(null,
                      new BinopExp(new CharExp('a'),
                                   new EqualsOp(),
                                   new IntExp(1)));
    }

    @Test
    public void testLessThanInts() {
        assertExpType(new BoolType(),
                      new BinopExp(new IntExp(1),
                                   new LessThanOp(),
                                   new IntExp(0)));
    }

    @Test
    public void testLessThanNonInts() {
        assertExpType(null,
                      new BinopExp(new CharExp('a'),
                                   new LessThanOp(),
                                   new IntExp(0)));
    }

    // TODO: structure creation needs scope
    // TODO: function calls need scope

    @Test
    public void testCastWellTypedSubexpression() {
        assertExpType(new IntType(),
                      new CastExp(new IntType(),
                                  new CharExp('a')));
    }

    @Test
    public void testCastIllTypedSubexpression() {
        assertExpType(null,
                      new CastExp(new CharType(),
                                  new BinopExp(new IntExp(1),
                                               new PlusOp(),
                                               new CharExp('a'))));
    }

    // TODO: address-of needs scope
    // TODO: field access needs scope
    // TODO: typecheck statements
    // TODO: functions
} // TypecheckerTest
