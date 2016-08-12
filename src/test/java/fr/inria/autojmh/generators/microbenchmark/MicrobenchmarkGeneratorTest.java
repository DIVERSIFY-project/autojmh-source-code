package fr.inria.autojmh.generators.microbenchmark;

import fr.inria.autojmh.generators.BenchmarkTest;
import fr.inria.autojmh.instrument.DataContextFileChooser;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.tool.AJMHConfiguration;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.code.*;

import java.io.IOException;
import java.net.URISyntaxException;

import static fr.inria.autojmh.ElementProvider.loadFirstSnippets;
import static fr.inria.autojmh.ResourcesPaths.getMainPath;
import static fr.inria.autojmh.ResourcesPaths.getTestPath;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

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

    private String buildOutput(BenchSnippet snippet) throws URISyntaxException {
        /*
        AJMHConfiguration configuration = new AJMHConfiguration();
        configuration.setWorkingDir(getTestPath(this, "testproject"));
        configuration.setPackageName("fr.inria.testproject.context");
        configuration.setGenerationOutputPath("/output_sources");
        configuration.setTemplatePath(getMainPath("templates"));
        configuration.setGenerationOutputPath("./output");*/

        MicrobenchmarkGenerator generator = new MicrobenchmarkGenerator();
        generator.setChooser(new StubFileChooser());
        generator.setWriteToFile(false);
        generator.configure(buildGenerationConf());
        generator.generate(snippet);
        return generator.getOutput();
    }

    /**
     * BUG: Test one real case
     * @throws Exception
     */
    @Test
    public void test_CaseADenseArrayMatrixCase() throws Exception {
        String output = buildOutput(loadFirstSnippets(this, "realcases.AdenseArrayMatrixCase",
                "mikeraAdenseArrayMatrix46", CtIf.class));
        assertEquals(output, 1, countMatches(output, "AdenseArrayMatrixCase THIZ"));
        assertEquals(output, 1, countMatches(output, "THIZ = fr_inria_testproject_realcases_AdenseArrayMatrixCase_29_s.readSerializable()"));
        assertEquals(output, 1, countMatches(output, "if (!(fr.inria.testproject.realcases.StaticMethods.isZero(THIZ.data,(offset + (i * cc)),java.lang.Math.min(cc,i))))"));
    }

    /**
     * BUG: Test one real case
     * @throws Exception
     */
    @Test
    public void test_maxAbsElementIndex() throws Exception {
        String output = buildOutput(loadFirstSnippets(this, "realcases.Index54",
                "maxAbsElementIndex", CtFor.class));
        assertFalse(output, output.contains("data_length"));
        assertEquals(output, 1, countMatches(output, "data.length"));
    }

    /**
     * BUG: Test one real case
     * @throws Exception
     */
    @Test
    public void test_Override() throws Exception {
        String output = buildOutput(loadFirstSnippets(this, "realcases.RemoveOverrideCase",
                "printStrings", CtLoop.class));
        assertFalse(output, output.contains("@java.lang.Override"));
        assertEquals(output, 1, countMatches(output, "void doSomething(fr.inria.testproject.realcases.RemoveOverrideCase THIZ,java.lang.String s"));
        assertEquals(output, 1, countMatches(output, "doSomething(THIZ, s)"));

    }

    /**
     * Test that extracted dynamic methods in not allowed types are not appended the initial THIZ
     * @throws Exception
     */
    @Test
    public void test_ThisNotAllowed() throws Exception {
        String output = buildOutput(loadFirstSnippets(this, "realcases.ThisNotAllowed",
                "printStrings", CtLoop.class));
        assertFalse(output, output.contains("protected void doSomething(fr.inria.testproject.realcases.ThisNotAllowed THIZ"));
        assertEquals(output, 1, countMatches(output, "void doSomething(java.lang.String s)"));

    }

    /**
     * BUG: Test one real case
     * @throws Exception
     */
    @Test
    public void test_allInRange() throws Exception {
        String output = buildOutput(loadFirstSnippets(this, "realcases.Index54",
                "allInRange", CtFor.class));
        assertFalse(output, output.contains("data_length"));
        assertEquals(output, 1, countMatches(output, "THIZ.data.length"));
    }

    /**
     * BUG: THIZ is not serializable
     * @throws Exception
     */
    @Test
    public void test_Case_Index54Compose() throws Exception {
        String output = buildOutput(loadFirstSnippets(this, "realcases.Index54",
                "compose", CtFor.class));
        //System.out.print(output);
        assertEquals(output, 1, countMatches(output,"public Index54 THIZ"));
        assertEquals(output, 1, countMatches(output, "r.intData[i] = a.intData[THIZ.intData[i]]"));
    }

    @Test
    public void test_Case_PrivateFieldHasPublicField() throws Exception {
        String output = buildOutput(loadFirstSnippets(this, "realcases.Index54",
                "getDiagonal", CtFor.class));
        //System.out.print(output);
        assertFalse(output.contains("public double[] THIZ_QT.data"));
        assertFalse(output.contains("THIZ_QT.data = fr_inria_testproject_realcases_Index54_48_s.readArray1double()"));
        assertEquals(output, 1, countMatches(output, "public fr.inria.testproject.realcases.AdenseArrayMatrixCase THIZ_QT"));
    }

    @Test
    public void test_Case_thisIterator() throws Exception {
        String output = buildOutput(loadFirstSnippets(this, "realcases.Index54",
                "thisIterator", CtLoop.class));
        //System.out.print(output);
        assertEquals(output, 1, countMatches(output, "public Index54 THIZ"));
    }

    /**
     * Test the variable extraction of the whole microbenchamark
     */
    @Test
    public void test_assignConstant() throws Exception {
        String output = buildOutput(loadFirstSnippets(this, "assignCte", CtLocalVariable.class));
        assertEquals(output, 1, countMatches(output, "y = fr.inria.testproject.context.DataContextPlayGround.CONSTANT2"));
    }

    /**
     * Test the variable extraction of the whole microbenchamark
     */
    @Test
    public void test_callSerializable() throws Exception {
        String output = buildOutput(loadFirstSnippets(this, "callSerializable", CtIf.class));
        assertEquals(output, 1, countMatches(output, "if ((java.lang.Math.abs(seri_values)) != ((seri_values)))"));
        assertEquals(output, 1, countMatches(output, "return seri.pubField"));
    }

    /**
     * Test the variable extraction of the whole microbenchamark
     */
    @Test
    public void test_privateStaticMethod() throws Exception {
        String output = buildOutput(loadFirstSnippets(this, "privateStaticMethod", CtIf.class));
        //Long name because is private
        assertEquals(output, 1, countMatches(output, "public final static int fr_inria_testproject_context_DataContextPlayGround_PRIVCONSTANT1 = 1"));
        assertEquals(output, 1, countMatches(output, "fr.inria.testproject.context.DataContextPlayGround.CONSTANT"));
        //Short name
        assertFalse(output, output.contains("int CONSTANT2"));
    }



    /**
     * Test the variable extraction of the whole microbenchamark
     */
    @Test
    public void test_CallPrivateMethodWithPrivateFields() throws Exception {
        String output = buildOutput(loadFirstSnippets(this, "callPrivateMethodWithPrivateFields", CtReturn.class));
        assertFalse(output, output.contains("ground_field2"));
        assertEquals(output, 1, countMatches(output, "while (((THIZ_field1)) < ((ground.field2)))"));
        assertEquals(output, 1, countMatches(output, "((THIZ_field1))++;"));
    }
}
