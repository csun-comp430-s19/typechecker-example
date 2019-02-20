package typechecker_example.syntax;

public class FreeExp implements Exp {
    public final Exp value;

    public FreeExp(final Exp value) {
        this.value = value;
    }

    public int hashCode() { return value.hashCode(); }
    public boolean equals(final Object other) {
        return (other instanceof FreeExp &&
                ((FreeExp)other).value.equals(value));
    }
    public String toString() {
        return "free(" + value.toString() + ")";
    }
}
