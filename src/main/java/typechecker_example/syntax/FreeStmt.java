package typechecker_example.syntax;

public class FreeStmt implements Stmt {
    public final Exp value;

    public FreeStmt(final Exp value) {
        this.value = value;
    }

    public int hashCode() { return value.hashCode(); }
    public boolean equals(final Object other) {
        return (other instanceof FreeStmt &&
                ((FreeStmt)other).value.equals(value));
    }
    public String toString() {
        return "free(" + value.toString() + ")";
    }
}
