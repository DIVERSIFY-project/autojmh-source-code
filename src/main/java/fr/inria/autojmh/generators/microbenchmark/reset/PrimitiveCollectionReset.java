package fr.inria.autojmh.generators.microbenchmark.reset;

import java.util.ArrayList;

/**
 * Generates the reset code for a collection of primitives
 * <p/>
 * Created by marodrig on 02/02/2016.
 */
public class PrimitiveCollectionReset implements ResetGenerator<ArrayList<?>> {

    @Override
    public String resetCode(String varName, ArrayList<?> object) {
        StringBuilder sb = new StringBuilder();
        sb.append(varName).append(".clear();\n");
        if (object.size() > 0) {
            for (Object o : object) {
                if (object.get(0) instanceof String)
                    sb.append(varName).append(".add(\"").append(o).append("\");\n");
                else sb.append(varName).append(".add(").append(o).append(");\n");
            }
        }
        return sb.toString();
    }

    @Override
    public String resetFromAnotherVar(String varName, String otherVar) {
        StringBuilder sb = new StringBuilder();
        sb.append(varName).append(".clear();\n");
        sb.append("for (int i = 0; i < ").append(otherVar).append(".size(); i++) ").
                append(varName).append(".add(").append(otherVar).append(".get(i));\n");
        return sb.toString();
    }
}
