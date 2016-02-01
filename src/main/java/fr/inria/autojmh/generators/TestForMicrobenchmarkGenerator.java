package fr.inria.autojmh.generators;

import fr.inria.autojmh.instrument.DataContextFileChooser;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.tool.AJMHConfiguration;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by marodrig on 29/09/2015.
 */
public class TestForMicrobenchmarkGenerator extends BaseGenerator {

    public static final String[] PRIMITIVES = {"byte", "short", "int", "long", "char", "String", "Object"};

    private String dataContextPath;

    private DataContextFileChooser chooser;
    private Logger log = Logger.getLogger(TestForMicrobenchmarkGenerator.class);

    public void generate(BenchSnippet snippet) {

        if (!getChooser().existsDataFile(dataContextPath, snippet.getMicrobenchmarkClassName())) return;

        try {
            HashMap<String, Object> input = new HashMap<String, Object>();
            input.put("data_file_path", getChooser().chooseAfter(snippet.getMicrobenchmarkClassName()));
            input.put("types", PRIMITIVES);
            input.put("package_name", packageName);
            input.put("generator", this);
            input.put("class_name", snippet.getMicrobenchmarkClassName());
            input.put("loop", snippet);
            input.put("input_vars", snippet.getTemplateAccessesWrappers());


            input.put("data_root_folder_path", new File(dataContextPath).getAbsolutePath().replace("\\", "/"));
            //chooser = new DataContextFileChooser(dataContextPath);

            generateOutput(input, "test-micro-benchmark.ftl", writeToFile, outputPath + "/" +
                    snippet.getMicrobenchmarkClassName() + "Test.java");
        } catch (IOException ex) {
            log.warn("Unable to find the after file. THIS SHOULD NOT BE HAPPENING.");
            //throw new RuntimeException(ex);
        }
        //input.put("data_file_path", snippet.getMicrobenchmarkClassName());

    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        super.configure(configuration);
        this.dataContextPath = configuration.getDataContextPath();
        outputPath = outputPath + "/src/test/java/" + packageName.replace(".", "/");
    }

    @Override
    public void generate() {
        for (BenchSnippet snippet : getSnippets()) {
            generate(snippet);
        }
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
