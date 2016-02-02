package fr.inria.autojmh.generators.reset;

/**
 * Created by marodrig on 02/02/2016.
 */
public class PrimitiveReset implements ResetGenerator<Object> {
    @Override
    public String resetCode(String varName, Object object) {
        if ( object instanceof String) return varName + " = \"" + object + "\";\n";
        return varName + " = " + object + ";\n";
    }

    @Override
    public String resetFromAnotherVar(String varName, String otherVar) {
        return varName + " = " + otherVar + ";";
    }
}
