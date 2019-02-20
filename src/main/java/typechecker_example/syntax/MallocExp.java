package typechecker_example.syntax;

public class MallocExp implements Exp {
    public final Exp amount;

    public MallocExp(final Exp amount) {
        this.amount = amount;
    }

    public int hashCode() { return amount.hashCode(); }
    public boolean equals(final Object other) {
        return (other instanceof MallocExp &&
                ((MallocExp)other).amount.equals(amount));
    }
    public String toString() {
        return "malloc(" + amount.toString() + ")";
    }
}
