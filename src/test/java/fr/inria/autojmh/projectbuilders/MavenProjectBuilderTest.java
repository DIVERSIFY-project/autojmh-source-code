package fr.inria.autojmh.projectbuilders;

import fr.inria.autojmh.projectbuilders.Maven.MavenProjectBuilder;
import fr.inria.autojmh.tool.AJMHConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MavenProjectBuilderTest {

    @Before
    public void setup() throws URISyntaxException, IOException {
        URL gen = this.getClass().getResource("/gen_output");
        if (gen != null) ProjectFiles.removeRecursively(new File(gen.toURI().getPath()));
    }

    @After
    public void tearDown() throws URISyntaxException, IOException {
        setup();
    }

    @Test
    public void testBuild() throws Exception {
        String output = new File(this.getClass().getResource("/").toURI().getPath() + "gen_output").getAbsolutePath();

        AJMHConfiguration configuration = new AJMHConfiguration();
        configuration.setGenerationOutputPath(output);
        configuration.setPackageName("fr.inria.testbench");
        configuration.setInputProjectPath(this.getClass().getResource("/").toURI().getPath() + "testproject");
        configuration.setTemplatePath(
                Thread.currentThread().getContextClassLoader().getResource("templates").toURI().getPath());

        MavenProjectBuilder builder = new MavenProjectBuilder(configuration);
        builder.build();

        String out = builder.getPomFileGenerator().getOutput();
        System.out.println(out);

        assertTrue(out.contains("<groupId>fr.inria.juncoprovider</groupId>"));
        assertTrue(out.contains("<artifactId>junco-provider</artifactId>"));
        assertTrue(out.contains("<version>0.1</version>"));
        assertTrue(out.contains("<groupId>fr.inria.covermath</groupId>"));
        assertTrue(out.contains("<artifactId>test-project</artifactId>"));
        assertTrue(out.contains("<version>1.0-SNAPSHOT</version>"));

        assertTrue(new File(output + "/src").exists());
        assertTrue(new File(output + "/src/test").exists());
        assertTrue(new File(output + "/pom.xml").exists());
    }
}