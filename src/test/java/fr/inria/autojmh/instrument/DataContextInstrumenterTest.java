package fr.inria.autojmh.instrument;

import fr.inria.autojmh.projectbuilders.ProjectFiles;
import fr.inria.autojmh.selection.TaggedStatementDetector;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.autojmh.selection.Tagglet;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static fr.inria.autojmh.selection.TaggletStatementDetectorTest.getTaggletsList;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

public class DataContextInstrumenterTest {

    private static final String CLASS_NAME = "fr.inria.testproject.Trigonometry";

    @Before
    public void setup() throws URISyntaxException, IOException {
        URL gen = this.getClass().getResource("/work/instrumenter");
        if (gen != null) ProjectFiles.removeRecursively(new File(gen.toURI().getPath()));
    }

    @After
    public void tearDown() throws URISyntaxException, IOException {
        //setup();
    }

    public static AJMHConfiguration getConfiguration(String rootPath) {
        //Start the configuration
        AJMHConfiguration conf = new AJMHConfiguration();
        conf.setInputProjectPath(rootPath + "testproject");
        conf.setWorkingDir(rootPath + "work/instrumenter");
        String output = new File(rootPath + "gen_output").getAbsolutePath();
        conf.setGenerationOutputPath(output);
        return conf;
    }

    @Test
    public void testExecuteIntegration() throws Exception {
        //Start the tagglets and the detector
        ArrayList<Tagglet> t = getTaggletsList(CLASS_NAME);
        t.get(0).setLineNumber(14);
        TaggedStatementDetector detector = new TaggedStatementDetector();
        HashMap<String, List<Tagglet>> tagglets = new HashMap<>();
        tagglets.put(CLASS_NAME, t);
        detector.setTagglets(tagglets);

        String rootPath = this.getClass().getResource("/").toURI().getPath();

        AJMHConfiguration conf = getConfiguration(rootPath);
        DataContextInstrumenter instrumenter = new DataContextInstrumenter();
        instrumenter.configure(conf);
        instrumenter.setDetector(detector);
        instrumenter.execute();

        //Very weak... improve quality of test
        assertEquals( DataContextInstrumenter.EXECUTION_OK, instrumenter.getExecutionResult());
        assertTrue(new File(rootPath + "work/instrumenter/src/main/java/fr/inria/testproject/Trigonometry.java").exists());
    }
}