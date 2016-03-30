package fr.inria.autojmh.generators;

import fr.inria.autojmh.projectbuilders.ProjectFiles;
import fr.inria.autojmh.snippets.SourceCodeSnippet;
import fr.inria.autojmh.tool.AJMHConfiguration;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by marodrig on 29/09/2015.
 */
public abstract class BaseGenerator implements BenchmakGenerator {

    protected static final String GRACEFULLY_BENCHMARK = "GRACEFULLY";
    protected static final String ORIGINAL_BENCHMARK = "ORIGINAL";
    public static final String PAD_8 = "        ";
    public static final String PAD_4 = "    ";

    private static Logger log = Logger.getLogger(BaseGenerator.class);

    /**
     * Indicates whether the generation must be exported to file or not. True by default
     */
    protected boolean writeToFile = true;

    /**
     * Generation output
     */
    protected String output;

    /**
     * Path to generate the output to
     */
    protected String outputPath;

    /**
     * Configuraion of the generation instrument.
     */
    private AJMHConfiguration configuration;

    /**
     * Configuration of the FreeMarker engine
     */
    protected Configuration templateConf = null;

    /**
     * Snippets for which the benchmarks will be generated
     */
    private Collection<SourceCodeSnippet> snippets;

    /**
     * Template path
     */
    protected String templatePath;

    /**
     * Name of the resulting package that will share all classes in the micro-benchmark project
     */
    protected String packageName;

    /**
     * Generation output
     *
     * @return
     */
    public String getOutput() {
        return output != null ? output : "";
    }

    public boolean isWriteToFile() {
        return writeToFile;
    }

    public void setWriteToFile(boolean writeToFile) {
        this.writeToFile = writeToFile;
    }

    /**
     * Initializes the benchmark generator
     *
     * @throws java.io.IOException
     */
    public void initialize() throws IOException {

        //Initialize the configuration only once
        if (templateConf != null) return;

        // Create your Configuration instance, and specify if up to what FreeMarker
        // version (here 2.3.22) do you want to apply the fixes that are not 100%
        // backward-compatible. See the Configuration JavaDoc for details.
        templateConf = new Configuration(Configuration.VERSION_2_3_22);

        // Specify the source where the template files come from. Here I set a
        // plain directory for it, but non-file-system sources are possible too:
        templateConf.setDirectoryForTemplateLoading(new File(templatePath));

        // Set the preferred charset template files are stored in. UTF-8 is
        // a good choice in most applications:
        templateConf.setDefaultEncoding("UTF-8");

        // Sets how errors will appear.
        // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
        templateConf.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        //templateConf.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    /**
     * Generates the output
     *
     * @param input        Input data for the template
     * @param templateName Name of the template
     * @param writeToFile  Whether we should write to file the output or not
     * @param outputPath   Output file path in the case writeToFile is true
     */
    protected void generateOutput(HashMap<String, Object> input, String templateName, boolean writeToFile, String outputPath) {
        try {
            ProjectFiles.makeIfNotExists(new File(outputPath).getParent());

            PrintWriter out = null;
            try {
                Template template = getTemplateConf().getTemplate(templateName);
                StringWriter writer = new StringWriter();
                template.process(input, writer);
                output = writer.getBuffer().toString();

                if (writeToFile) {
                    out = new PrintWriter(outputPath);
                    out.println(output);
                }



            } finally {
                if (out != null) out.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void configure(AJMHConfiguration conf) {
        packageName = conf.getPackageName();
        outputPath = conf.getGenerationOutputPath();
        templatePath = conf.getTemplatePath();
    }

    @Override
    public void setSnippets(Collection<SourceCodeSnippet> snippets) {
        this.snippets = snippets;
    }

    @Override
    public Collection<SourceCodeSnippet> getSnippets() {
        return snippets;
    }

    protected Configuration getTemplateConf() {
        if (templateConf == null)
            try {
                initialize();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        return templateConf;
    }
}
