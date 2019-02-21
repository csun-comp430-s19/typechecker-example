package typechecker_example.typechecker;

import typechecker_example.syntax.Type;

public class TypeErrorException extends Exception {
    public TypeErrorException(final String message) {
        super(message);
    }

    public TypeErrorException(final Type expected, final Type received) {
        this("Expected " + expected.toString() +
             "; received: " + received.toString());
    }
}
