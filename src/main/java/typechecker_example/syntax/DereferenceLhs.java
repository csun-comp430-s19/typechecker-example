package typechecker_example.syntax;

public class DereferenceLhs implements Lhs {
    public final Lhs lhs;

    public DereferenceLhs(final Lhs lhs) {
        this.lhs = lhs;
    }

    public int hashCode() {
        return 1 + lhs.hashCode();
    }

    public boolean equals(final Object other) {
        return (other instanceof DereferenceLhs &&
                ((DereferenceLhs)other).lhs.equals(lhs));
    }

    public String toString() {
        return "*" + lhs.toString();
    }
}
