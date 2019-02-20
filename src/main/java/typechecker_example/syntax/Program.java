package typechecker_example.syntax;

import java.util.Arrays;

public class Program {
    public final StructureDeclaration[] structDecs;
    public final FunctionDefinition[] functionDefs;

    public Program(final StructureDeclaration[] structDecs,
                   final FunctionDefinition[] functionDefs) {
        this.structDecs = structDecs;
        this.functionDefs = functionDefs;
    }

    public int hashCode() {
        return (Arrays.deepHashCode(structDecs) +
                Arrays.deepHashCode(functionDefs));
    }

    public boolean equals(final Object other) {
        if (other instanceof Program) {
            final Program otherProgram = (Program)other;
            return (Arrays.deepEquals(otherProgram.structDecs, structDecs) &&
                    Arrays.deepEquals(otherProgram.functionDefs, functionDefs));
        } else {
            return false;
        }
    }

    public String toString() {
        return (Join.join("\n", structDecs) + "\n\n" +
                Join.join("\n", functionDefs) + "\n");
    }
}
