package fr.inria.autojmh.instrument;


import fr.inria.autojmh.instrument.log.*;
import fr.inria.autojmh.snippets.Preconditions;
import fr.inria.autojmh.selection.TaggedStatementDetector;
import fr.inria.autojmh.selection.Tagglet;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.autojmh.tool.Configurable;
import fr.inria.diversify.syringe.Configuration;
import fr.inria.diversify.syringe.SyringeInstrumenter;
import fr.inria.diversify.syringe.SyringeInstrumenterImpl;
import fr.inria.diversify.syringe.detectors.Detector;
import spoon.reflect.code.CtStatement;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Syringe program to instrument a series of tagged statements
 * <p/>
 * Created by marodrig on 15/09/2015.
 */
public class DataContextInstrumenter implements Configurable {

    AJMHConfiguration ajmhConf;

    public static final String EXECUTION_OK = "Execution OK";

    private static Logger log = Logger.getLogger(DataContextInstrumenter.class.getCanonicalName());

    /**
     * Path to to the project root
     */
    String prjPath;

    /**
     * Path to the sources
     */
    String srcPath;

    /**
     * Output path of the instrumented project
     */
    String outputPrj;

    private Detector<CtStatement> detector;

    private String executionResult;

    private Preconditions preconditions;

    public void execute() throws Exception {
        Configuration confSrc = new Configuration(srcPath);

        //Detect tagged statement
        if (detector == null) detector = new TaggedStatementDetector();
        confSrc.addDetector(detector);

        //Inject them with data context recording injectors
        confSrc.addInjector(TaggedStatementDetector.SNIPPET_DETECTED, new DataContextInjector());

        //This are the loggers to log
        confSrc.addLogger(MicrobenchmarkLogger.class);
        confSrc.addLogger(OutputStreamProvider.class);
        confSrc.addLogger(Log.class);
        confSrc.addLogger(ShutdownHookLog.class);
        confSrc.addLogger(LightLog.class);

        //Instrument
        SyringeInstrumenter l = new SyringeInstrumenterImpl(prjPath, srcPath, outputPrj);
        l.instrument(confSrc);
        l.setOnlyCopyLogger(true);
        l.writeIdFile("microbenchmarkProperties.id");

        if (detector.getElementsDetectedCount() == 0) log.warning("No snippet for benchmarking detected");

        executionResult = EXECUTION_OK;
        try {
            l.runTests(true);
        } catch (Exception e) {
            executionResult = e.getMessage();
        }
    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        srcPath = configuration.getInputProjectSrcPath();
        prjPath = configuration.getInputProjectPath();
        outputPrj = configuration.getWorkingDir();
        ajmhConf = configuration;
    }

    public void instrument(Map<Tagglet, CtStatement> matches) {

    }

    public void setDetector(Detector<CtStatement> detector) {
        this.detector = detector;
    }

    public Detector<CtStatement> getDetector() {
        return detector;
    }

    public String getExecutionResult() {
        return executionResult;
    }

}
