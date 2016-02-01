package fr.inria.autojmh.generators;

import fr.inria.autojmh.tool.AJMHConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A generator to generate the POM file for the benchmark project
 * <p/>
 * Created by marodrig on 29/10/2015.
 */
public class LoaderGenerator extends BaseGenerator {

    public static final String[] PRIMITIVES = {"byte", "short", "int", "long", "float", "double", "char", "String", "boolean"};

    public static final String[] COLLECTIONS = {
            "ArrayList",
            "LinkedList",
            "List",
            "HashSet",
            "Set",
            "SortedSet",
            "Collection"};

    public static final String[] CONCRETE_COLLECTIONS = {
            "ArrayList",
            "LinkedList",
            "ArrayList",
            "HashSet",
            "HashSet",
            "TreeSet",
            "ArrayList"};


    public static final String[] METHODS_NAME = {
            "Byte",
            "Short",
            "Int",
            "Long",
            "Float",
            "Double",
            "Char",
            "UTF",
            "Boolean"};

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

        ArrayList<CollectionType> collTypes = new ArrayList<>();
        for (int i = 0; i < COLLECTIONS.length; i++)
            collTypes.add(new CollectionType(COLLECTIONS[i], CONCRETE_COLLECTIONS[i]));

        HashMap<String, Object> input = new HashMap<>();
        input.put("types", types);
        input.put("collectionTypes", collTypes);
        input.put("package_name", packageName);
        String outFile = outputPath.endsWith("/") ? outputPath + "Loader.java" : outputPath + "/Loader.java";
        generateOutput(input, "loader.ftl", writeToFile, outFile);
       // generateOutput(input, "writer.ftl", writeToFile, workpath + "/MicrobenchmarkLogger.java");
    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        super.configure(configuration);
        outputPath = outputPath.endsWith("/") ? outputPath : outputPath + "/";
        outputPath = outputPath + configuration.getGeneratedSrcPath() + "/" +  packageName.replace(".", "/");
        //workpath= configuration.getWorkingDir() + "/src/main/java/fr/inria/autojmh/instrument/log/";
    }
}
