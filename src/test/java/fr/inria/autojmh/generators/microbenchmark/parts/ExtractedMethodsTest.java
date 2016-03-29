package fr.inria.autojmh.generators.microbenchmark.parts;

import fr.inria.autojmh.snippets.BenchSnippet;
import org.junit.Test;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtReturn;

import java.util.List;

import static fr.inria.autojmh.ElementProvider.loadSnippets;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by marodrig on 28/03/2016.
 */
public class ExtractedMethodsTest {

    /**
     * Generates code using the ExtractedMethod part
     * @param replace Whether invocations should be replaced or not
     * @param method Method to extract methods
     * @param klass CtElement class to pick inside the method
     * @return The generated code
     * @throws Exception if something goes wrong
     */
    private String buildCode(boolean replace, String method, Class<?> klass) throws Exception {
        List<BenchSnippet> list = loadSnippets(this, method, klass);
        if ( replace ) {
            SnippetCode snippetCode = new SnippetCode();
            snippetCode.generate(list.get(0));
        }
        ExtractedMethods replacement = new ExtractedMethods();
        String code = replacement.generate(list.get(0));
        return code;
    }

    /**
     * Test that only one method's body is extracted, even if the method is called twice
     */
    @Test
    public void testGenerate_Dynamic_Two_Invocations_To_The_Same_Method() throws Exception {
        String code = buildCode(false, "callInvocations", CtIf.class);
        //Count the times callProtected appears.
        //Is equal two because callProtected is recursive, hence appearing twice
        assertEquals(2, code.split("callProtected").length - 1);
    }

    /**
     * Test the correct extraction of a combination of dynamic an static methods
     */
    @Test
    public void testGenerate_Dynamic_SomePublic() throws Exception {
        String code = buildCode(false, "callInvocationsSomePublic", CtIf.class);
        assertTrue(code.contains("private int callPrivate(fr.inria.testproject.context.DataContextPlayGround THIZ,boolean k) {"));
        assertTrue(code.contains("protected int callProtected(fr.inria.testproject.context.DataContextPlayGround THIZ) {"));
        assertFalse(code.contains("callPublic(DataContextPlayGround THIZ)"));
    }

    /**
     * Test the correct extraction of an static method
     */
    @Test
    public void testGenerate_StaticMethod() throws Exception {
        String code = buildCode(false, "callStatic", CtReturn.class);
        assertTrue(code.contains("private static int privateStaticMethod(int x)"));
        assertTrue(code.contains("privateStaticMethod((x + (x * 90)"));
    }

    /**
     * Test the correct extraction of an static method when invocations where replaced previously
     */
    @Test
    public void testGenerate_InvocationsReplaced_StaticMethod() throws Exception {
        String code = buildCode(true, "callStatic", CtReturn.class);
        assertTrue(code.contains("private static int privateStaticMethod(int x)"));
        //This is not a dynamic method, must stay the same
        assertFalse(code.contains("private static void privateStaticMethod(fr.inria.testproject.context.DataContextPlayGround"));
        assertTrue(code.contains("privateStaticMethod((x + (x * 90)"));
    }

    /**
     * Test the correct extraction of a combination of dynamic an static methods
     * when invocations where replaced previously
     */
    @Test
    public void testGenerate_InvocationsReplaced() throws Exception {
        String code = buildCode(true, "callInvocationsSomePublic", CtIf.class);
        assertTrue(code.contains("private int callPrivate(fr.inria.testproject.context.DataContextPlayGround THIZ,boolean"));
        assertTrue(code.contains("return callPrivate(THIZ, k);"));
        assertTrue(code.contains("protected int callProtected(fr.inria.testproject.context.DataContextPlayGround THIZ) {"));
        assertTrue(code.contains("return callProtected(THIZ);"));
        assertFalse(code.contains("callPublic(DataContextPlayGround THIZ)"));
    }
}