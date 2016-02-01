package fr.inria.autojmh.generators;

import fr.inria.autojmh.instrument.DataContextFileChooser;
import fr.inria.autojmh.tool.AJMHConfiguration;

import java.io.File;
import java.util.HashMap;

/**
 * Created by marodrig on 29/09/2015.
 */
public class MainClassGenerator extends BaseGenerator {

    private String dataContextPath;

    private DataContextFileChooser chooser;


    /**
     * Generates the main method to execute all benchmarks
     */
    public void generate() {
        HashMap<String, Object> input = new HashMap<String, Object>();

        input.put("package_name", packageName);
        input.put("snippets", getSnippets());
        input.put("data_path", new File(dataContextPath).getAbsolutePath());
        //input.put("db_path", outputDBPath.replace("\\", "/"));
        input.put("generator", this);
        generateOutput(input, "main-micro-benchmark.ftl", writeToFile, outputPath + "/Main.java");
    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        super.configure(configuration);
        this.dataContextPath = configuration.getDataContextPath();
        outputPath = outputPath + "/src/main/java/" + packageName.replace(".", "/");
    }

    /**
     * Indicates whether exist an input data file for the class name in the given data path
     *
     * @param dataPath  Data path to search for the data file
     * @param className Name of the micro-benchmark class
     * @return True if a file exists, false otherwise
     */
    public boolean existsDataFile(String dataPath, final String className) {
        return getChooser().existsDataFile(dataPath, className);
    }

    public DataContextFileChooser getChooser() {
        if (chooser == null)
            chooser = new DataContextFileChooser(dataContextPath);
        return chooser;
    }

    public void setChooser(DataContextFileChooser chooser) {
        this.chooser = chooser;
    }
}
