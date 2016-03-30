package fr.inria.autojmh.generators;

import fr.inria.autojmh.snippets.BenchSnippet;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by marodrig on 28/09/2015.
 */
public class MainClassGeneratorTest extends BenchmarkTest {

    @Before
    public void setup() throws Exception {
        snippet = buildSignalLoop();
    }

    @Test
    public void testBenchmarkOriginall() throws Exception {
        MainClassGenerator generator = new MainClassGenerator();
        generator.setWriteToFile(false);
        generator.configure(buildGenerationConf());
        ArrayList<BenchSnippet> loops = new ArrayList<BenchSnippet>();
        loops.add(snippet);
        generator.setSnippets(loops);
        //Runtime context
        generator.generate();
        System.out.println(generator.getOutput());
        assertTrue(generator.getOutput().contains(".include(testpack_Arithmetic_18_Benchmark"));
    }
}
