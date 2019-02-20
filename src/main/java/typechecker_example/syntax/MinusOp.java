package typechecker_example.syntax;

public class MinusOp implements Op {
    public int hashCode() { return 1; }
    public boolean equals(final Object other) {
        return other instanceof MinusOp;
    }
    public String toString() { return "-"; }
}

