package fr.inria.autojmh.generators;

import fr.inria.autojmh.ResourcesPaths;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static fr.inria.autojmh.generators.LoaderGenerator.PRIMITIVES;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class LoaderFileGeneratorTest extends BenchmarkTest {

    private void generateReader(String path) throws URISyntaxException {
        AJMHConfiguration configuration = buildGenerationConf();
        configuration.setGenerationOutputPath(path);
        configuration.setPackageName("fr.inria.loadertestprj");

        LoaderGenerator generator = new LoaderGenerator();
        generator.configure(configuration);
        generator.setWriteToFile(true);
        generator.generate();
    }

    private void runBuilderAndAssertSuccess(String path, String[] phases) throws InterruptedException, IOException {
        MavenBuilder mb = new MavenBuilder(path, path + "/src/main/java");
        mb.setPhase(phases);//);
        mb.setTimeOut(1000);
        mb.setVerbose(true);
        mb.runBuilder();
        assertEquals(0, (int) mb.getStatus());
    }

    @Before
    public void setup() throws URISyntaxException {
        String testPrj = ResourcesPaths.getTestPath(this, "loadertestprj");
        File f = new File(testPrj + "/src/main/java/fr/inria/loadertestprj/Loader.java");
        if (f.exists()) f.delete();
    }


    @Test
    public void testGenerate() throws Exception {
        AJMHConfiguration configuration = buildGenerationConf();
        LoaderGenerator generator = new LoaderGenerator();
        generator.configure(configuration);
        generator.setWriteToFile(false);
        generator.generate();

        System.out.println(generator.getOutput());

        for (int i = 0; i < PRIMITIVES.length; i++) {
            assertTrue(generator.getOutput().contains(
                    String.format("public %s[] readArray1%s", PRIMITIVES[i], PRIMITIVES[i])));
            assertTrue(generator.getOutput().contains(
                    String.format("%s[] result = new %s[length];", PRIMITIVES[i], PRIMITIVES[i])));
            assertTrue(generator.getOutput().contains(
                    String.format("public %s read%s", PRIMITIVES[i], PRIMITIVES[i])));
        }
    }

    @Test
    public void testGenerateAndCompile() throws Exception {
        String testPrj = ResourcesPaths.getTestPath(this, "loadertestprj");
        generateReader(testPrj);
        String loaderOut = ResourcesPaths.getTestPath(this,
                "loadertestprj/src/main/java/fr/inria/loadertestprj/Loader.java");
        assertTrue(new File(loaderOut).exists());
        runBuilderAndAssertSuccess(testPrj, new String[]{"compile"});
    }

    @Test
    public void testRunUnitTestOverGenerated() throws Exception {

        //Generate the writer
        String testPrj = ResourcesPaths.getTestPath(this, "loadertestprj");
        AJMHConfiguration configuration = buildGenerationConf();
        configuration.setWorkingDir(testPrj);
        WriterFileGeneratorTest.generateWriter(configuration);

        //Generate the reader
        generateReader(testPrj);

        runBuilderAndAssertSuccess(testPrj, new String[]{"clean", "test"});
    }
}