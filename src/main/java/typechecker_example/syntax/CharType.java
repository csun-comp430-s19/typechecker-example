package typechecker_example.syntax;

public class CharType implements Type {
    public int hashCode() { return 1; }
    public boolean equals(final Object other) {
        return other instanceof CharType;
    }
    public String toString() { return "char"; }
}
