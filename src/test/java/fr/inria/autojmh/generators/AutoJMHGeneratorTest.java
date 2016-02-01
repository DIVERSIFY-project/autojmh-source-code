package fr.inria.autojmh.generators;

import fr.inria.autojmh.ResourcesPaths;
import fr.inria.autojmh.instrument.DataContextInstrumenterTest;
import fr.inria.autojmh.projectbuilders.ProjectFiles;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import org.apache.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

public class AutoJMHGeneratorTest {

    @Before
    public void setup() throws URISyntaxException, IOException {
        URL gen = this.getClass().getResource("/work/instrumenter");
        if (gen != null) ProjectFiles.removeRecursively(new File(gen.toURI().getPath()));

        gen = this.getClass().getResource("gen_output");
        if (gen != null) ProjectFiles.removeRecursively(new File(gen.toURI().getPath()));
    }

    @After
    public void tearDown() throws URISyntaxException, IOException {
        //setup();
    }

    @Test
    public void testGenerateIntegration() throws Exception {

//        String outPath = ResourcesPaths.getTestPath(this, "gen_output");

        //Start the configuration
        String rootPath = this.getClass().getResource("/").toURI().getPath();
        AJMHConfiguration conf = DataContextInstrumenterTest.getConfiguration(rootPath);
        AJMHGenerator gen = new AJMHGenerator();
        gen.configure(conf);
        gen.generate();
        assertTrue(new File(rootPath +
                "work/instrumenter/src/main/java/fr/inria/testproject/Trigonometry.java").exists());

        //Assert that the generated project actually compiles
/*
        MavenBuilder mb = new MavenBuilder(outPath, outPath + "/src/main/java");
        mb.setTimeOut(1000);
        mb.runBuilder();
        assertEquals(0, (int)mb.getStatus());
        */
    }
}