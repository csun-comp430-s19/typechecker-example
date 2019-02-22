package typechecker_example.syntax;

public class ExpStmt implements Stmt {
    public final Exp exp;

    public ExpStmt(final Exp exp) {
        this.exp = exp;
    }

    public int hashCode() { return exp.hashCode(); }
    public boolean equals(final Object other) {
        return (other instanceof ExpStmt &&
                ((ExpStmt)other).exp.equals(exp));
    }
    public String toString() { return exp.toString(); }
}
