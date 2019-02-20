package typechecker_example.syntax;

public class FieldAccessLhs implements Lhs {
    public final Lhs lhs;
    public final FieldName field;

    public FieldAccessLhs(final Lhs lhs,
                          final FieldName field) {
        this.lhs = lhs;
        this.field = field;
    }

    public int hashCode() {
        return lhs.hashCode() + field.hashCode();
    }

    public boolean equals(final Object other) {
        if (other instanceof FieldAccessLhs) {
            final FieldAccessLhs otherLhs = (FieldAccessLhs)other;
            return (otherLhs.lhs.equals(lhs) &&
                    otherLhs.field.equals(field));
        } else {
            return false;
        }
    }

    public String toString() {
        return lhs.toString() + "." + field.toString();
    }
}
