package typechecker_example.syntax;

public class DivOp implements Op {
    public int hashCode() { return 3; }
    public boolean equals(final Object other) {
        return other instanceof DivOp;
    }
    public String toString() { return "/"; }
}

