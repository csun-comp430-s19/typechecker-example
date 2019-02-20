package typechecker_example;

public class Variable {
    public final String name;

    public Variable(final String name) {
        this.name = name;
    }

    public int hashCode() { return name.hashCode(); }
    public boolean equals(final Object other) {
        return (other instanceof Variable &&
                ((Variable)other).name.equals(name));
    }
    public String toString() { return name; }
}
