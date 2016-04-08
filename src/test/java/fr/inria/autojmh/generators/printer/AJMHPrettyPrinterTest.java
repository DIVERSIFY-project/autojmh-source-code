package fr.inria.autojmh.generators.printer;

import fr.inria.autojmh.ElementProvider;
import fr.inria.autojmh.snippets.BenchSnippet;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import spoon.reflect.code.*;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.junit.Assert.*;

/**
 * Created by marodrig on 31/03/2016.
 */
public class AJMHPrettyPrinterTest {

    private String generateCode(String method, Class<?> klass) throws Exception {
        return generateCode("DataContextPlayGround", method, klass);
    }

    private String generateCode(String klass, String method, Class<?> spoonMetamodelClass) throws Exception {
        BenchSnippet snippet = ElementProvider.loadFirstSnippets(this, klass, method, spoonMetamodelClass);
        AJMHPrettyPrinter printer = new AJMHPrettyPrinter(snippet);
        snippet.getASTElement().accept(printer);
        return printer.toString();
    }

    @Test
    public void testTargetField() throws Exception {
        String code = generateCode("anIntMethod", CtWhile.class);
        assertEquals(code, 2, countMatches(code, "ground_field1"));
        assertFalse(code.contains("ground.field1"));
    }

    /**
     * Test that a public method's target is equal to THIZ
     */
    @Test
    public void testTransform_Public_Private_with_THIZ_Allowed() throws Exception {
        String code = generateCode("SerializableObject", "callInvocationsSomePublic", CtIf.class);
        //Assert the code generation
        assertEquals(code, 1, countMatches(code, "callPrivate(THIZ, bb)"));
        assertEquals(code, 1, countMatches(code, "THIZ.callPublic(bb)"));
        assertFalse(code.contains(" callPublic(bb)"));
    }

    @Test
    public void testTransform_Invocations_Public_Private_THIZ_Not_Allowed() throws Exception {
        String code = generateCode("callInvocationsSomePublic", CtIf.class);
        assertEquals(code, 1, countMatches(code, "callPrivate(bb)"));
        assertFalse(code, code.contains("callPrivate(THIZ,"));
        assertEquals(code, 1, countMatches(code, "callProtected()"));
        assertEquals(code, 1, countMatches(code, "callProtected()"));
        assertFalse(code, code.contains("callProtected(THIZ)"));
        assertFalse(code, code.contains("callPublic(THIZ)"));
    }

    @Test
    public void testTransform_Invocations_AbstractPublic() throws Exception {
        String code = generateCode("callPublicAbstractMethod", CtBlock.class);
        assertEquals(code, 1, countMatches(code, "ac.abstractMethod()"));
        assertFalse(code.contains("abstractMethod(ac)"));

    }


    @Test
    public void testTransform_StaticPublic() throws Exception {
        String code = generateCode("callSerializable", CtIf.class);
        assertEquals(code, 1, countMatches(code, "Math.abs"));
        assertFalse(code.contains("Math_abs"));
    }

    /**
     * A protected abstract method should be rejected by the preconditions. So we throw!

    @Test(expected = RuntimeException.class)
    public void testTransform_AbstractProtected_Method() throws Exception {
        String code = generateCode("callProtectedAbstractMethod", CtIf.class);
        System.out.print(code);
    }*/

    @Test
    public void testTransform_LocalVariables() throws Exception {
        String code = generateCode("containOnlyPrimitiveClasses", CtExpression.class);
        assertEquals("b = b * a", code);
    }

    @Test
    public void testTransform_PublicConstant() throws Exception {
        String code = generateCode("privateStaticMethod", CtExpression.class);
        assertEquals(code, 1, countMatches(code, " CONSTANT2"));
    }

    @Test
    public void testTransform_PrivateConstant() throws Exception {
        String code = generateCode("privateStaticMethod", CtIf.class);
        assertEquals(code, 1, countMatches(code, "fr_inria_testproject_context_DataContextPlayGround_CONSTANT"));
        assertFalse(code.contains("fr.inria.testproject.context.DataContextPlayGround.CONSTANT"));
    }

    @Test
    public void testTransform_Private_THIZ_Field() throws Exception {
        String code = generateCode("anIntMethod", CtAssignment.class);


        assertEquals(code, 1, countMatches(code, "THIZ_field1"));
        assertFalse(code.contains(" field1"));
    }


    @Test
    public void testTransform_Private_TARGET_Field() throws Exception {
        String code = generateCode("anIntMethod", CtWhile.class);
        assertEquals(code, 2, countMatches(code, "ground_field1"));
        assertFalse(code.contains("ground.field1"));
    }

    @Test
    public void testTransform_Public_NON_Serializable() throws Exception {
        String code = generateCode("callNonSerializable", CtReturn.class);
        assertEquals(code, 1, countMatches(code, "nonSeri_pubNonSerializable"));
        assertFalse(code.contains("nonSeri.pubNonSerializable"));
    }

    @Test
    public void testTransform_Public_Serializable_THIZ() throws Exception {
        String code = generateCode("SerializableObject","doSomethingWithPriValue", CtIf.class);
        assertEquals(code, 3, countMatches(code, "THIZ_priValue"));
        assertFalse(code.contains(" priValue"));
    }

    @Test
    public void testTransform_Public_Serializable_TARGET() throws Exception {
        String code = generateCode("callSerializable", CtReturn.class);
        assertEquals(code, 1, countMatches(code, "seri.pubField"));
        assertFalse(code.contains("seri_pubField"));
    }

}