package typechecker_example.syntax;

public class DereferenceExp implements Exp {
    public final Exp exp;

    public DereferenceExp(final Exp exp) {
        this.exp = exp;
    }

    public int hashCode() { return exp.hashCode(); }
    public boolean equals(final Object other) {
        return (other instanceof DereferenceExp &&
                ((DereferenceExp)other).exp.equals(exp));
    }
    public String toString() {
        return "*" + exp.toString();
    }
}
