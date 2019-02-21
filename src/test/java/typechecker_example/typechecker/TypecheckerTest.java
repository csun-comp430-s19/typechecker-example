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
                       exp == null);
        }
    }

    @Test
    public void testIntExp() throws TypeErrorException {
        assertExpType(new IntType(),
                      new IntExp(42));
    }
} // TypecheckerTest
