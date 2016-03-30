package fr.inria.autojmh.generators.microbenchmark;

import fr.inria.autojmh.ElementProvider;
import fr.inria.autojmh.generators.BenchmarkTest;
import fr.inria.autojmh.instrument.DataContextFileChooser;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.tool.AJMHConfiguration;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.code.CtWhile;

import java.io.IOException;
import java.net.URISyntaxException;

import static fr.inria.autojmh.ResourcesPaths.getMainPath;
import static fr.inria.autojmh.ResourcesPaths.getTestPath;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Created by marodrig on 28/09/2015.
 */
public class MicrobenchmarkGeneratorTest extends BenchmarkTest {

    public static class StubFileChooser extends DataContextFileChooser {
        @Override
        public boolean existsDataFile(String dataPath, String className) {
            return true;
        }

        @Override
        public String chooseAfter(String className) throws IOException {
            return "file_path";
        }

        @Override
        public String chooseBefore(String className) throws IOException {
            return "file_path_after";
        }
    }

    BenchSnippet snippet;

    @Before
    public void setup() throws Exception {
        snippet = buildSignalLoop();
    }

    private String buildOutput(BenchSnippet snippet) throws URISyntaxException {
        AJMHConfiguration configuration = new AJMHConfiguration();
        configuration.setWorkingDir(getTestPath(this, "testproject"));
        configuration.setPackageName("fr.inria.testproject.context");
        configuration.setGenerationOutputPath("/output_sources");
        configuration.setTemplatePath(getMainPath("templates"));
        configuration.setGenerationOutputPath("/output");

        MicrobenchmarkGenerator generator = new MicrobenchmarkGenerator();
        generator.setChooser(new StubFileChooser());
        generator.setWriteToFile(false);
        generator.configure(buildGenerationConf());
        generator.generate(snippet);

        return generator.getOutput();
    }

    /**
     * Test the variable extraction of the whole microbenchamark
     */
    @Test
    public void test_Variables_AnIntMethod() throws Exception {
        String output = buildOutput(ElementProvider.loadFirstSnippets(this, "anIntMethod", CtWhile.class));

        //Var declaration
        assertTrue(output.contains("public int ground_field1"));

        //Var initialization
        assertTrue(output.contains("ground_field1 = "));
        assertFalse(output.contains("ground.field1 ="));
        assertFalse(output.contains("(ground.field1) ="));

        //Snippet usage
        assertTrue(output.contains("while (ground_field1 < 100)"));
    }

    @Test
    public void testBenchmarkOriginall() throws Exception {
        MicrobenchmarkGenerator generator = new MicrobenchmarkGenerator();
        generator.setWriteToFile(false);
        generator.configure(buildGenerationConf());
        generator.generate(snippet);

        System.out.println(generator.getOutput());
        assertTrue(generator.getOutput().contains("package fr.mypackage"));
        assertTrue(generator.getOutput().contains("class testpack_Arithmetic"));
        assertTrue(generator.getOutput().contains("static final String DATA_FILE = \"testpack-Arithmetic-18"));
        assertTrue(generator.getOutput().contains("int a ;"));
        assertTrue(generator.getOutput().contains("b = testpack_Arithmetic_18_s.readint();"));
        assertTrue(generator.getOutput().contains("testpack_Arithmetic_18_s.openStream("));
        assertTrue(generator.getOutput().contains("testpack_Arithmetic_18_s.closeStream()"));
    }
}
