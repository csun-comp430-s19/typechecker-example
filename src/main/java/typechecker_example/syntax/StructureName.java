package typechecker_example.syntax;

public class StructureName extends Name {
    public StructureName(final String name) {
        super(name);
    }

    public boolean sameClass(final Name other) {
        return other instanceof StructureName;
    }
}
