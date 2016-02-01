package fr.inria.autojmh.generators;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by marodrig on 28/09/2015.
 */
public class TestForMicrobenchmarkGeneratorTest extends BenchmarkTest {

    @Before
    public void setup() throws Exception {
        snippet = buildSignalLoop();
    }

    @Test
    public void testBenchmarkOriginall() throws Exception {
        TestForMicrobenchmarkGenerator generator = new TestForMicrobenchmarkGenerator();
        generator.setWriteToFile(false);
        generator.configure(buildGenerationConf());
        //Runtime context
        generator.generate(snippet);

        //System.out.println(generator.getOutput());
        boolean contains = generator.getOutput().contains(
                "new testpack_Arithmetic_18_Benchmark()");
        assertEquals(true, contains);
    }
}
