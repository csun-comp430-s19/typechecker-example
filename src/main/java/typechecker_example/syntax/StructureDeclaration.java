package typechecker_example.syntax;

import java.util.Arrays;

public class StructureDeclaration {
    public final StructureName name;
    public final VariableDeclaration[] fields;

    public StructureDeclaration(final StructureName name,
                                final VariableDeclaration[] fields) {
        this.name = name;
        this.fields = fields;
    }

    public int hashCode() {
        return name.hashCode() + Arrays.deepHashCode(fields);
    }

    public boolean equals(final Object other) {
        if (other instanceof StructureDeclaration) {
            final StructureDeclaration otherDec =
                (StructureDeclaration)other;
            return (otherDec.name.equals(name) &&
                    Arrays.deepEquals(otherDec.fields, fields));
        } else {
            return false;
        }
    }

    public String toString() {
        return (name.toString() + " { " +
                Join.join("; ", fields) + " }");
    }
}

