package fr.inria.autojmh.tool;

import java.io.*;
import java.util.Properties;

/**
 * Class to hold all configuration variables to AutoJMH
 * <p/>
 * Created by marodrig on 27/10/2015.
 */
public class AJMHConfiguration {

    /**
     * Path to the input project to benchmark
     */
    private String inputProjectPath;

    /**
     * Path where the input project sources
     */
    private String inputProjectSrcPath;

    /**
     * Path where the input project test
     */
    private String inputProjectTestPath;

    /**
     * Working directory to store all intermediate state, runtime context, etc.
     */
    private String workingDir;

    /**
     * Name of the package to which all benchmarks will belong to
     */
    private String packageName = "fr.inria.autojmh.benchmarks";

    /**
     * Path where the generation is going to be stored
     */
    private String generationOutputPath;

    /**
     * Path to the custom templates to use. Default templates used if not specified
     */
    private String templatePath;

    /**
     * Path to the generated sources of the benchmark(output) project
     */
    private String generatedSrcPath;

    /**
     * Path to the generated test of the benchmark(output) project
     */
    private String generatedTestPath;


    private static final String RUNTIME_CONTEXT = "log";

    /**
     * Get the path where the context data is stored
     *
     * @return
     */
    public String getDataContextPath() {
        return getWorkingDir() + "/" + RUNTIME_CONTEXT;
    }

    /**
     * Obtains a configuration object from the tool arguments
     *
     * @param args Arguments passed to AutoJMH tool
     * @return A configuration object
     * @throws IllegalArgumentException when the arguments are invalid.
     */
    public static AJMHConfiguration fromArgs(String[] args)
            throws IllegalArgumentException, IOException {
        Properties p = new Properties();
        p.load(new FileInputStream(args[0]));

        AJMHConfiguration c = new AJMHConfiguration();
        c.setInputProjectPath(p.getProperty("projectDir"));
        c.setGenerationOutputPath(p.getProperty("outputDir"));
        c.setWorkingDir(p.getProperty("workingDir"));
        return c;
    }

    public AJMHConfiguration() {
        setInputProjectSrcPath("/src/main/java");
        setInputProjectTestPath("/src/test/java");
        setGeneratedOutputSrcPath("src/main/java");
        setGeneratedOutputTestPath("src/test/java");
        try {
            setTemplatePath(
                    new File(this.getClass().getResource("/templates").toURI().getPath()).getAbsolutePath());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setGeneratedOutputSrcPath(String srcPath) {

        this.generatedSrcPath = srcPath;
    }

    public String getGeneratedSrcPath() {
        return generatedSrcPath;
    }

    public void setGeneratedOutputTestPath(String testPath) {
        this.generatedTestPath = testPath;
    }

    public String getGeneratedTestPath() {
        return generatedTestPath;
    }

    public String getInputProjectTestPath() {
        return inputProjectTestPath;
    }

    public void setInputProjectTestPath(String inputProjectTestPath) {
        this.inputProjectTestPath = inputProjectTestPath;
    }

    public String getInputProjectSrcPath() {
        return inputProjectSrcPath;
    }

    public void setInputProjectSrcPath(String inputProjectSrcPath) {
        this.inputProjectSrcPath = inputProjectSrcPath;
    }

    public String getInputProjectPath() {
        return inputProjectPath;
    }

    public void setInputProjectPath(String inputProjectPath) {
        this.inputProjectPath = inputProjectPath;
    }

    public String getGenerationOutputPath() {
        return generationOutputPath;
    }

    public void setGenerationOutputPath(String generationOutputPath) {
        this.generationOutputPath = generationOutputPath;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

}
