package fr.inria.autojmh.generators.microbenchmark.parts;

import fr.inria.autojmh.snippets.BenchSnippet;
import org.junit.Test;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtReturn;

import java.util.List;

import static fr.inria.autojmh.ElementProvider.loadSnippets;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by marodrig on 28/03/2016.
 */
public class ExtractedMethodsTest {

    /**
     * Generates code using the ExtractedMethod part
     *
     * @param method Method to extract methods
     * @param klass  CtElement class to pick inside the method
     * @return The generated code
     * @throws Exception if something goes wrong
     */
    private String buildCode(String method, Class<?> klass) throws Exception {
        List<BenchSnippet> list = loadSnippets(this, method, klass);
        ExtractedMethods replacement = new ExtractedMethods();
        String code = replacement.generate(list.get(0));
        return code;
    }

    private String buildCode(String className, String method, Class<?> klass) throws Exception {
        List<BenchSnippet> list = loadSnippets(this, method, className, klass);
        ExtractedMethods replacement = new ExtractedMethods();
        String code = replacement.generate(list.get(0));
        return code;
    }

    /**
     * Test that only one method's body is extracted, even if the method is called twice
     */
    @Test
    public void testGenerate_Dynamic_Two_Invocations_To_The_Same_Method() throws Exception {
        String code = buildCode("callInvocations", CtIf.class);
        //Count the times callProtected appears.
        //Is equal two because callProtected is recursive, hence appearing twice
        assertEquals(2, code.split("callProtected").length - 1);
    }

    /**
     * Test the correct extraction of a combination of dynamic an static methods
     */
    @Test
    public void testGenerate_Dynamic_SomePublic_THIZ_Not_Allowed() throws Exception {
        String code = buildCode("callInvocationsSomePublic", CtIf.class);
        assertTrue(code, code.contains("private int callPrivate(boolean k) {"));
        assertTrue(code, code.contains("protected int callProtected() {"));
        assertTrue(code, code.contains("return callProtected()"));
        assertTrue(code, code.contains("return callPrivate(k)"));
        assertFalse(code, code.contains("callPublic(DataContextPlayGround THIZ)"));
    }

    /**
     * Test the correct extraction of an static method when invocations where replaced previously
     */
    @Test
    public void testGenerate_InvocationsReplaced_StaticMethod() throws Exception {
        String code = buildCode("callStatic", CtReturn.class);
        assertTrue(code.contains("private static int fr_inria_testproject_context_DataContextPlayGround_privateStaticMethod(int x)"));
        //This is not a dynamic method, must stay the same
        assertFalse(code.contains("privateStaticMethod(fr.inria.testproject.context.DataContextPlayGround"));
        assertTrue(code.contains("fr_inria_testproject_context_DataContextPlayGround_privateStaticMethod((x + (x * 90)"));
    }


    /**
     * Test the correct extraction of an static method
     */
    @Test
    public void testGenerate_StaticMethod() throws Exception {
        String code = buildCode("callStatic", CtReturn.class);
        assertTrue(code.contains("fr_inria_testproject_context_DataContextPlayGround_privateStaticMethod(int x)"));
        assertTrue(code.contains("privateStaticMethod((x + (x * 90)"));
    }


    /**
     * Test the correct extraction of a combination of dynamic an static methods
     * when invocations where replaced previously
     */
    @Test
    public void testGenerate_InvocationsReplaced() throws Exception {
        String code = buildCode("SerializableObject", "callInvocationsSomePublic", CtIf.class);
        assertTrue(code, code.contains("private int callPrivate(fr.inria.testproject.context.SerializableObject THIZ,boolean"));
        assertTrue(code, code.contains("return callPrivate(THIZ, k);"));
        assertTrue(code, code.contains("protected int callProtected(fr.inria.testproject.context.SerializableObject THIZ) {"));
        assertTrue(code, code.contains("return callProtected(THIZ);"));
        assertFalse(code, code.contains("callPublic(DataContextPlayGround THIZ)"));
    }
}