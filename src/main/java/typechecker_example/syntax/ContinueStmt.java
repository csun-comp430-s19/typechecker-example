package typechecker_example.syntax;

public class ContinueStmt implements Stmt {
    public int hashCode() { return 1; }
    public boolean equals(final Object other) {
        return other instanceof ContinueStmt;
    }
    public String toString() { return "continue"; }
}
