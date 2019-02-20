package typechecker_example.syntax;

public class CastExp implements Exp {
    public final Type type;
    public final Exp exp;

    public CastExp(final Type type, final Exp exp) {
        this.type = type;
        this.exp = exp;
    }

    public int hashCode() {
        return type.hashCode() + exp.hashCode();
    }

    public boolean equals(final Object other) {
        if (other instanceof CastExp) {
            final CastExp otherExp = (CastExp)other;
            return (otherExp.type.equals(type) &&
                    otherExp.exp.equals(exp));
        } else {
            return false;
        }
    }

    public String toString() {
        return "(" + type.toString() + ")" + exp.toString();
    }
}
