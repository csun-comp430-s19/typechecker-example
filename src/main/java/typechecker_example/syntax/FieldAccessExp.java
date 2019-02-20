package typechecker_example.syntax;

public class FieldAccessExp implements Exp {
    public final Exp exp;
    public final FieldName field;

    public FieldAccessExp(final Exp exp,
                          final FieldName field) {
        this.exp = exp;
        this.field = field;
    }

    public int hashCode() {
        return exp.hashCode() + field.hashCode();
    }

    public boolean equals(final Object other) {
        if (other instanceof FieldAccessExp) {
            final FieldAccessExp otherExp = (FieldAccessExp)other;
            return (otherExp.exp.equals(exp) &&
                    otherExp.field.equals(field));
        } else {
            return false;
        }
    }

    public String toString() {
        return exp.toString() + "." + field.toString();
    }
}
