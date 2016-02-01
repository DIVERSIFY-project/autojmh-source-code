package fr.inria.autojmh.generators;

import fr.inria.autojmh.instrument.DataContextInstrumenter;
import fr.inria.autojmh.instrument.DataContextResolver;
import fr.inria.autojmh.projectbuilders.Maven.MavenProjectBuilder;
import fr.inria.autojmh.projectbuilders.ProjectFiles;
import fr.inria.autojmh.selection.SnippetSelector;
import fr.inria.autojmh.selection.SelectionFileWalker;
import fr.inria.autojmh.selection.TaggedStatementDetector;
import fr.inria.autojmh.selection.Tagglet;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.tool.AJMHConfiguration;
import org.apache.log4j.Logger;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static fr.inria.autojmh.instrument.DataContextResolver.checkPreconditions;

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

    /**
     * Generates the benchmark suite
     */
    public void generate() {
        try {
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
                if (checkPreconditions(snippet)) complyingSnippets.add(snippet);
            }
            log.info("Total snippets: " + snippets.size());
            log.info("Snippet accepted: " + complyingSnippets.size());
            if ( complyingSnippets.size() == 0 ) {
                log.info("No snippets accepted. Exiting");
                return;
            }

            snippets = complyingSnippets;

            //Clean the instrumentation from the snippets
            cleanInstrumentation(snippets);

            //Finally generate the benchmarks

            runGenerators(new MainClassGenerator(), snippets);
            log.info("Main file generated");
            MicrobenchmarkGenerator g = new MicrobenchmarkGenerator();
            runGenerators(g, snippets);

            log.info("Microbenchmarks generated: " + g.getGeneratedCount());

            log.info("Microbenchmarks generated");
            runGenerators(new LoaderGenerator(), snippets);
            log.info("Loader file generated");
            runGenerators(new TestForMicrobenchmarkGenerator(), snippets);
            log.info("Unit tests file generated");

        } catch (Exception e) {
            log.fatal("Process failed");
            throw new RuntimeException(e);
        }


    }


    private void cleanInstrumentation(List<BenchSnippet> snippets) {
        for (BenchSnippet s : snippets) {
            CtStatement st = s.getASTElement();
            if (st.getParent() instanceof CtBlock) {
                CtBlock block = (CtBlock) st.getParent();
                int k = 0;
                while (k < block.getStatements().size()){
                    CtStatement blockSt = block.getStatement(k);
                    if (blockSt instanceof CtCodeSnippetStatement) {
                        CtCodeSnippetStatement snippet = (CtCodeSnippetStatement) blockSt;
                        if (snippet.getValue().startsWith("fr.inria.autojmh"))
                            block.removeStatement(blockSt);
                    } else k++;
                }


                /*
                int index = block.getStatements().indexOf(st) - 1;
                try {
                    while (index >= 0 && block.getStatements().get(index) instanceof CtCodeSnippetStatement) {
                        block.removeStatement(block.getStatement(index));
                        index--;
                    }
                } catch (IndexOutOfBoundsException ex) {
                    log.warn("Unexpected exception");
                }
                try {
                    index += 2;
                    while (index < block.getStatements().size() &&
                            block.getStatements().get(index) instanceof CtCodeSnippetStatement) {
                        block.removeStatement(block.getStatement(index));
                    }
                } catch (IndexOutOfBoundsException ex) {
                    log.warn("Unexpected exception");
                }*/
            }
        }
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

    /**
     * Collect all tagglets from the input source
     *
     * @return A Map of taglets ordered by their corresponding class
     */
    private Map<String, List<Tagglet>> collectTagglets() {

        log.info("Collecting tagglets");

        if (userTagglets == null) {
            //1. Obtain the tagglets from the code
            SelectionFileWalker walker = new SelectionFileWalker();
            try {
                //Collect in the source dir
                walker.walkDir(conf.getInputProjectPath() + conf.getInputProjectSrcPath());
                //And collect in the test dir as well
                walker.walkDir(conf.getInputProjectPath() + conf.getInputProjectTestPath());
            } catch (IOException e) {
                log.fatal("Could not collect tagglets: " + e.toString());
                return null;
            }
            log.info("Done");
            return walker.getTagglets();
        } else {
            HashMap<String, List<Tagglet>> result = new HashMap<>();
            for (Tagglet t : userTagglets) {
                if (!result.containsKey(t.getClassName()))
                    result.put(t.getClassName(), new ArrayList<Tagglet>());
                result.get(t.getClassName()).add(t);
            }
            return result;
        }
    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        conf = configuration;
    }

    /**
     * Builds a report of the generated instrument based on the configuration.
     */
    public void report() {

    }

    public void setTagglets(Collection<Tagglet> tagglets) {
        this.userTagglets = tagglets;
    }

    public Collection<Tagglet> getTagglets() {
        return null;
    }


    public SnippetSelector getCustomDetector() {
        return customDetector;
    }

    public void setSelector(SnippetSelector customDetector) {
        this.customDetector = customDetector;
    }
}
