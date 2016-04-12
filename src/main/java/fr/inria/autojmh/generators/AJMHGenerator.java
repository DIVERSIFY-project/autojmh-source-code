package fr.inria.autojmh.generators;

import fr.inria.autojmh.generators.microbenchmark.MicrobenchmarkGenerator;
import fr.inria.autojmh.instrument.DataContextInstrumenter;
import fr.inria.autojmh.instrument.TaggletCollector;
import fr.inria.autojmh.projectbuilders.Maven.MavenProjectBuilder;
import fr.inria.autojmh.projectbuilders.ProjectFiles;
import fr.inria.autojmh.selection.SelectionFileWalker;
import fr.inria.autojmh.selection.SnippetSelector;
import fr.inria.autojmh.selection.TaggedStatementDetector;
import fr.inria.autojmh.selection.Tagglet;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.autojmh.tool.InstrumentationCleaner;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static fr.inria.autojmh.snippets.Preconditions.*;

/**
 * Class that drives the whole generation instrument.
 * <p/>
 * Created by marodrig on 27/10/2015.
 */
public class AJMHGenerator implements BenchmakGenerator {

    Logger log = Logger.getLogger(AJMHGenerator.class);

    /**
     * Configuration of the tool
     */
    private AJMHConfiguration conf;

    /**
     * Tagglets provided by the user.
     */
    private Collection<Tagglet> userTagglets;

    /**
     * snippets provided by the user
     */
    private Collection<BenchSnippet> snippets;


    private SnippetSelector customDetector;

    public AJMHGenerator() {

    }

    public AJMHGenerator(AJMHConfiguration configuration) {
        configure(configuration);
    }

    /**
     * Run the given generator
     *
     * @param generator
     */
    public void runGenerators(BaseGenerator generator, Collection<BenchSnippet> snippets) {
        generator.setWriteToFile(true);
        generator.setSnippets(snippets);
        generator.configure(conf);
        generator.generate();
    }

    @Override
    public void setSnippets(Collection<BenchSnippet> snippets) {
        this.snippets = snippets;
    }

    @Override
    public Collection<BenchSnippet> getSnippets() {
        return this.snippets;
    }

    private boolean checkInputPath() {
        if (!(new File(conf.getInputProjectPath()).exists())) {
            log.fatal("The input path " + conf.getInputProjectPath() + " can't be accessed.");
            return false;
        }
        if (!(new File(conf.getInputProjectPath() + conf.getInputProjectSrcPath()).exists())) {
            log.fatal("The input source path " + conf.getInputProjectSrcPath() + " can't be accessed.");
            return false;
        }
        if (!(new File(conf.getInputProjectPath() + conf.getInputProjectTestPath()).exists())) {
            log.fatal("The input test path " + conf.getInputProjectTestPath() + " can't be accessed.");
            return false;
        }
        return true;
    }

    /**
     * Generates the benchmark suite
     */
    public void generate() {
        try {
            if (!checkInputPath()) return;

            //Clean working and result's dir
            log.info("Cleaning working dirs");
            cleanWorkingDir();
            cleanResultsDir();

            //Build the output project
            log.info("Building output POM file");
            MavenProjectBuilder builder = new MavenProjectBuilder(conf);
            builder.build();

            //Collect all tagglets from the input source
            log.info("Collecting tagglets");
            Map<String, List<Tagglet>> tagglets = null;
            if (customDetector == null) tagglets = collectTagglets();

            //Instrument all tagged points to record the data context
            log.info("Instrumenting data context");
            List<BenchSnippet> snippets = recordDataContext(tagglets);

            log.info("Removing non compliant snippets");
            List<BenchSnippet> complyingSnippets = new ArrayList<>();
            for (BenchSnippet snippet : snippets) {
                if (snippet.meetsPreconditions())
                    complyingSnippets.add(snippet);
                else if ( conf.getPrintRejected() ) {
                    log.warn("REJECTING: " + snippet.getASTElement().toString());
                }
            }

            int totalSnippets = snippets.size();
            int accepted = complyingSnippets.size();
            if (accepted == 0) {
                log.info("No snippets accepted. Exiting");
                return;
            }

            snippets = complyingSnippets;

            //Clean the instrumentation from the snippets
            cleanInstrumentation(snippets);

            //Finally generate the benchmarks
            runGenerators(new MainClassGenerator(), snippets);
            log.info("Main file generated");

            runGenerators(new LoaderGenerator(), snippets);
            log.info("Loader file generated");

            runGenerators(new TestForMicrobenchmarkGenerator(), snippets);
            log.info("Unit tests file generated");

            //After the generation of the parts, the snippet changes its AST
            //That's why they are the last thing to be generated
            MicrobenchmarkGenerator g = new MicrobenchmarkGenerator();
            runGenerators(g, snippets);
            log.info("Microbenchmarks generated");

            printStats(totalSnippets, accepted, g);

        } catch (Exception e) {
            log.fatal("Process failed");
            throw new RuntimeException(e);
        }
    }

    private Map<String, List<Tagglet>> collectTagglets() {
        TaggletCollector collector = new TaggletCollector();
        collector.configure(conf);
        collector.setTagglets(userTagglets);
        return collector.collect();
    }

    private void printStats(int totalSnippets, int accepted, MicrobenchmarkGenerator g) {
        Map<String, Integer> causes = conf.getPreconditions().getRejectionCause();
        log.info("----------- STATS: ---------------");
        logCause(true, "Total", totalSnippets, totalSnippets);
        logCause(true, "Accepted", accepted, totalSnippets);


        logCause(true, VARS_UNSUPPORTED, causes, totalSnippets);
        logCause(false, COLLECTION_OF_UNSUPPORTED_TYPE, causes, totalSnippets);
        logCause(false, TYPE_IS_NOT_PUBLIC, causes, totalSnippets);
        logCause(false, TYPE_IS_NOT_STORABLE, causeCount(VARS_UNSUPPORTED, causes) -
                (causeCount(COLLECTION_OF_UNSUPPORTED_TYPE, causes) + causeCount(TYPE_IS_NOT_PUBLIC, causes)),
                totalSnippets);
        logCause(true, DYN_INV_UNSUPPORTED, causes, totalSnippets);
        logCause(false, DYN_METHOD_TARGET_TYPE_UNSUPPORTED, causes, totalSnippets);
        logCause(false, LEVELS_TOO_DEEP, causes, totalSnippets);
        logCause(false, PRIVATE_CONSTRUCTOR, causes, totalSnippets);
        logCause(false, ERR_GET_BODY + " - Protected", causes, totalSnippets);
        logCause(true, "Generated", g.getGeneratedCount(), totalSnippets);
        log.info("----------------------------------");

        for (Map.Entry<String, Integer> k : conf.getPreconditions().getRejectionCause().entrySet())
            log.info(" + " + k.getKey() + ": " + k.getValue());
    }

    private int causeCount(String cause, Map<String, Integer> causes) {
        if (causes.containsKey(cause)) return causes.get(cause);
        else return 0;
    }

    private void logCause(boolean b, String cause, Map<String, Integer> causes, int total) {
        if (!causes.containsKey(cause)) logCause(b, cause, 0, total);
        else logCause(b, cause, causes.get(cause), total);
    }

    private void logCause(boolean b, String cause, int k, int total) {
        String s = cause + ": " + k;
        if (total != k) s = s + " : " + Math.round((double)k / (double)total*100);
        if (!b) s = " -- " + s;
        log.info(s);
    }

    /**
     * Cleans the instrumentation out of the AST of the project
     */
    private void cleanInstrumentation(List<BenchSnippet> snippets) {
        new InstrumentationCleaner().cleanUp(snippets);
    }

    /**
     * Identify the AST tagged statements
     *
     * @param tagglets A map of tagglets
     * @return
     */
    private List<BenchSnippet> recordDataContext(Map<String, List<Tagglet>> tagglets) throws Exception {
        log.info("Identifying annotated statements - BEGIN");

        if (customDetector == null) {
            TaggedStatementDetector toAST = new TaggedStatementDetector();
            toAST.setTagglets(tagglets);
            customDetector = toAST;
        }

        try {
            DataContextInstrumenter instrumenter = new DataContextInstrumenter();
            instrumenter.setDetector(customDetector);
            instrumenter.configure(conf);
            instrumenter.execute();
        } catch (Exception e) {
            log.fatal("Could not record context");
            throw e;
        }
        log.info("Identifying annotated statements - DONE");
        return customDetector.getSnippets();
    }

    private void cleanResultsDir() throws IOException {
        File f = new File(conf.getGenerationOutputPath());
        if (f.exists()) {
            ProjectFiles.removeRecursively(f);
        }
        ProjectFiles.makeIfNotExists(f.getAbsolutePath());
    }

    private void cleanWorkingDir() throws IOException {
        File f = new File(conf.getWorkingDir());
        if (f.exists()) {
            ProjectFiles.removeRecursively(f);
        }
        ProjectFiles.makeIfNotExists(f.getAbsolutePath());
    }



    @Override
    public void configure(AJMHConfiguration configuration) {
        conf = configuration;
    }


    public void setSelector(SnippetSelector customDetector) {
        this.customDetector = customDetector;
        this.customDetector.configure(conf);
    }

    public SnippetSelector getSelector() {
        return customDetector;
    }
}
