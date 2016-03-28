package fr.inria.autojmh.generators.microbenchmark;

import fr.inria.autojmh.generators.BenchmarkTest;
import fr.inria.autojmh.generators.microbenchmark.MicrobenchmarkGenerator;
import fr.inria.autojmh.snippets.BenchSnippet;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * Created by marodrig on 28/09/2015.
 */
public class MicrobenchmarkGeneratorTest extends BenchmarkTest {

    BenchSnippet snippet;

    @Before
    public void setup() throws Exception {
        snippet = buildSignalLoop();
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
