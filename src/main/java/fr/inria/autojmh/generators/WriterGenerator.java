package fr.inria.autojmh.generators;

import fr.inria.autojmh.tool.AJMHConfiguration;
import org.apache.log4j.helpers.Loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static fr.inria.autojmh.generators.LoaderGenerator.METHODS_NAME;
import static fr.inria.autojmh.generators.LoaderGenerator.PRIMITIVES;

/**
 * A generator to generate the POM file for the benchmark project
 * <p/>
 * Created by marodrig on 29/10/2015.
 */
public class WriterGenerator extends BaseGenerator {

    public static final String[] PRIMITIVE_CLASS_NAME = {
            "Byte",
            "Short",
            "Integer",
            "Long",
            "Float",
            "Double",
            "Char",
            "String",
            "Boolean"};

    @Override
    public void generate() {
        List<StreamType> types = new ArrayList<>();
        for (int i = 0; i < PRIMITIVES.length; i++)
            types.add(new StreamType(PRIMITIVES[i], METHODS_NAME[i], PRIMITIVE_CLASS_NAME[i]));
        HashMap<String, Object> input = new HashMap<>();
        input.put("types", types);
        input.put("package_name", packageName);
        generateOutput(input, "writer.ftl", writeToFile, outputPath);
    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        super.configure(configuration);
        outputPath = configuration.getWorkingDir() +
                "/src/main/java/fr/inria/autojmh/instrument/log/MicrobenchmarkLogger.java";
    }
}
