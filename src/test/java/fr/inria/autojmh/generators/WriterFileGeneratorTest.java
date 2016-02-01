package fr.inria.autojmh.generators;

import fr.inria.autojmh.ResourcesPaths;
import fr.inria.autojmh.projectbuilders.ProjectFiles;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static fr.inria.autojmh.generators.LoaderGenerator.METHODS_NAME;
import static fr.inria.autojmh.generators.LoaderGenerator.PRIMITIVES;
import static fr.inria.autojmh.generators.WriterGenerator.PRIMITIVE_CLASS_NAME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class WriterFileGeneratorTest extends BenchmarkTest {

    @Before
    public void setup() throws URISyntaxException {
        String testPrj = ResourcesPaths.getTestPath(this, "loadertestprj");
        File f = new File(testPrj + "/src/main/java/fr/inria/autojmh/instrument/log/MicrobenchmarkLogger.java");
        if (f.exists()) f.delete();
    }

    public static void assertSuccesfullCompilation(String prjPath, String srcPath)
            throws Exception {
        //Assert that the generated file compiles
        MavenBuilder builder = new MavenBuilder(prjPath, srcPath);
        builder.setPhase(new String[] { "compile" });
        builder.setVerbose(true);
        builder.setTimeOut(1000);
        builder.runBuilder();
        int status = builder.getStatus();
        assertEquals(0, status);
    }

    @Test
    public void testGenerate() throws Exception {

        AJMHConfiguration configuration = buildGenerationConf();

        //Build the generation
        WriterGenerator generator = new WriterGenerator();
        generator.configure(configuration);
        generator.setWriteToFile(false);
        generator.generate();

        System.out.println(generator.getOutput());

        assertTrue(generator.getOutput().contains("logSerializableCollection"));

        //Assert that the generated file contains what it has to contain
        for (int i = 0; i < PRIMITIVES.length; i++) {
            assertTrue(generator.getOutput().contains(
                    String.format("public void logArray1%s", PRIMITIVES[i])));

            if (PRIMITIVE_CLASS_NAME[i] != "Char")
                assertTrue(generator.getOutput().contains(
                        String.format("public void log%sCollection(Collection<%s> data, String name, boolean after)",
                                PRIMITIVE_CLASS_NAME[i], PRIMITIVE_CLASS_NAME[i])));

            assertTrue(generator.getOutput().contains(
                    String.format("stream.write%s(data)", METHODS_NAME[i])));

            assertTrue(generator.getOutput().contains(
                    String.format(" public void log%s(%s data, String name, boolean after",
                            PRIMITIVES[i], PRIMITIVES[i])));
        }
    }

    /**
     * Test that the generation was done to the right folder
     */
    @Test
    public void testGenerateToFile() throws URISyntaxException {

        String testPrj = ResourcesPaths.getTestPath(this, "loadertestprj");

        AJMHConfiguration configuration = buildGenerationConf();
        configuration.setWorkingDir(testPrj);

        //Build the generation
        WriterGenerator generator = new WriterGenerator();
        generator.configure(configuration);
        generator.setWriteToFile(true);
        generator.generate();

        //Assert that the generation actually wrote something to the right place
        String loaderOut = ResourcesPaths.getTestPath(this,
                "loadertestprj/src/main/java/fr/inria/autojmh/instrument/log/MicrobenchmarkLogger.java");
        assertTrue(new File(loaderOut).exists());
    }

    public static void generateWriter(AJMHConfiguration configuration) {

        WriterGenerator generator = new WriterGenerator();
        generator.configure(configuration);
        generator.setWriteToFile(true);
        generator.generate();

    }

    /**
     * Asserts that the compilation was successful
     * @throws Exception
     */
    @Test
    public void testGenerateAndCompile() throws Exception {
        String testPrj = ResourcesPaths.getTestPath(this, "loadertestprj");
        AJMHConfiguration configuration = buildGenerationConf();
        configuration.setWorkingDir(testPrj);
        generateWriter(configuration);

        MavenBuilder mb = new MavenBuilder(testPrj, testPrj + "/src/main/java");
        mb.setPhase(new String[]{"compile"});
        mb.setTimeOut(1000);
        mb.setVerbose(true);
        mb.runBuilder();
        assertEquals(0, (int) mb.getStatus());
    }
}