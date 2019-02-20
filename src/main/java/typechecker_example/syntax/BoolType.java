package typechecker_example.syntax;

public class BoolType implements Type {
    public int hashCode() { return 3; }
    public boolean equals(final Object other) {
        return other instanceof BoolType;
    }
    public String toString() { return "bool"; }
}
