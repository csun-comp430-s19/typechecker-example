package typechecker_example.syntax;

import java.util.Arrays;

public class MakeStructureExp implements Exp {
    public final StructureName name;
    public final Exp[] parameters;

    public MakeStructureExp(final StructureName name,
                            final Exp[] parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public int hashCode() {
        return name.hashCode() + Arrays.deepHashCode(parameters);
    }

    public boolean equals(final Object other) {
        if (other instanceof MakeStructureExp) {
            final MakeStructureExp otherExp = (MakeStructureExp)other;
            return (otherExp.name.equals(name) &&
                    Arrays.deepEquals(parameters,
                                      otherExp.parameters));
        } else {
            return false;
        }
    }
    
    public String toString() {
        return (name.toString() + "(" +
                Join.join(", ", parameters) + ")");
    }
}
