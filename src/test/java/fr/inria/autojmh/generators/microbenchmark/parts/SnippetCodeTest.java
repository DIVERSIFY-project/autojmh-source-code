package fr.inria.autojmh.generators.microbenchmark.parts;

import fr.inria.autojmh.generators.microbenchmark.parts.substitutes.CtInvocationDecorator;
import fr.inria.autojmh.generators.microbenchmark.parts.substitutes.CtVariableAccessDecorator;
import fr.inria.autojmh.snippets.BenchSnippet;
import org.junit.Ignore;
import org.junit.Test;
import spoon.reflect.code.*;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

import static fr.inria.autojmh.ElementProvider.loadSnippets;
import static org.junit.Assert.*;

/**
 * Test the code generator in charge of the snippet
 * <p>
 * Created by marodrig on 25/03/2016.
 */
public class SnippetCodeTest {

    private static class GenResult {
        String code;
        BenchSnippet snippet;
        GenResult(String code, BenchSnippet snippet) {
            this.code = code;
            this.snippet = snippet;
        }
    }

    private GenResult generateCode(String method, Class<?> klazz) throws Exception {
        List<BenchSnippet> list = loadSnippets(this, method, klazz);
        SnippetCode replacement = new SnippetCode();
        return new GenResult(replacement.generate(list.get(0)), list.get(0));
    }

    //------------------------- METHOD TRANSFORMATION -----------------------------------------------

    @Test
    public void testTransform_Invocations() throws Exception {
        GenResult r = generateCode("callInvocations", CtIf.class);
        //Assert that all invocations have been replaced
        List<CtInvocation> invs = r.snippet.getASTElement().getElements(
                new TypeFilter<CtInvocation>(CtInvocation.class));
        assertEquals(3, invs.size());
        for (CtInvocation inv : invs) assertTrue(inv instanceof CtInvocationDecorator);
    }

    @Test
    public void testTransform_Invocations_Public() throws Exception {
        GenResult r = generateCode("callInvocationsSomePublic", CtIf.class);
        assertTrue(r.code.contains("callPrivate(THIZ, bb)"));
        assertFalse(r.code.contains("callPrivate(bb)"));
        assertTrue(r.code.contains("callProtected(THIZ)"));
        assertFalse(r.code.contains("callProtected()"));
        assertFalse(r.code.contains("callPublic(THIZ)"));
    }

    @Test
    public void testTransform_Invocations_AbstractPublic() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "callPublicAbstractMethod", CtReturn.class);
        SnippetCode replacement = new SnippetCode();
        String code = replacement.generate(list.get(0));
        List<CtInvocation> invs = list.get(0).getASTElement().getElements(
                new TypeFilter<CtInvocation>(CtInvocation.class));
        assertEquals(1, invs.size());
        assertFalse(invs.get(0) instanceof CtInvocationDecorator);

        //Assert the code generation
        assertTrue(code.contains("ac.abstractMethod()"));
        assertFalse(code.contains("abstractMethod(ac)"));
    }


    @Test
    public void testTransform_StaticPublic() throws Exception {
        GenResult r = generateCode("callSerializable", CtIf.class);
        assertTrue(r.code.contains("Math.abs"));
        assertFalse(r.code.contains("Math_abs"));
    }

    /**
     * Test that a public method's target is equal to THIZ
     */
    @Test
    public void testTransform_Public_with_THIZ_Target() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "callInvocationsSomePublic", CtIf.class);
        SnippetCode replacement = new SnippetCode();
        String code = replacement.generate(list.get(0));
        List<CtInvocation> invs = list.get(0).getASTElement().getElements(
                new TypeFilter<CtInvocation>(CtInvocation.class));
        assertEquals(3, invs.size());
        //Assert all invocations are transformed
        assertTrue(invs.get(0) instanceof CtInvocationDecorator);
        assertTrue(invs.get(1) instanceof CtInvocationDecorator);
        assertTrue(invs.get(2) instanceof CtInvocationDecorator);

        System.out.print(code);
        //Assert the code generation
        assertTrue(code.contains("callPrivate(THIZ, bb)"));
        assertTrue(code.contains("THIZ.callPublic(bb)"));
        assertFalse(code.contains(" callPublic(bb)"));
    }

    /**
     * A protected abstract method should be rejected by the preconditions. So we throw!
     */
    @Test(expected = RuntimeException.class)
    public void testTransform_AbstractProtected_Method() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "callProtectedAbstractMethod", CtIf.class);
        SnippetCode replacement = new SnippetCode();
        replacement.generate(list.get(0));
    }

    @Test
    public void testTransform_Variables() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "arrayOfSerializables", CtLoop.class);
        SnippetCode replacement = new SnippetCode();
        String code = replacement.generate(list.get(0));
        List<CtVariableAccess> invs = list.get(0).getAccesses();
        assertEquals(4, invs.size());
        for (CtVariableAccess inv : invs)
            assertTrue(inv instanceof CtVariableAccessDecorator);
    }

    //------------------------- VARIABLE NAME TRANSFORMATION -----------------------------------------------

    @Test
    public void testTransform_LocalVariables() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "containOnlyPrimitiveClasses", CtExpression.class);
        SnippetCode replacement = new SnippetCode();
        String code = replacement.generate(list.get(0));
        assertEquals("b = b * a", code);
    }

    @Test
    public void testTransform_PublicConstant() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "privateStaticMethod", CtExpression.class);
        SnippetCode replacement = new SnippetCode();
        String code = replacement.generate(list.get(0));
        assertTrue(code.contains(" CONSTANT2"));
    }

    @Test
    public void testTransform_PrivateConstant() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "privateStaticMethod", CtIf.class);
        SnippetCode replacement = new SnippetCode();
        String code = replacement.generate(list.get(0));
        assertTrue(code.contains("fr_inria_testproject_context_DataContextPlayGround_CONSTANT"));
        assertFalse(code.contains("fr.inria.testproject.context.DataContextPlayGround.CONSTANT"));
    }

    @Test
    public void testTransform_Private_THIZ_Field() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "anIntMethod", CtAssignment.class);
        SnippetCode replacement = new SnippetCode();
        String code = replacement.generate(list.get(0));
        assertTrue(code.contains("THIZ_field1"));
        assertFalse(code.contains(" field1"));
    }

    @Test
    public void testTransform_Private_TARGET_Field() throws Exception {
        GenResult r = generateCode("anIntMethod", CtWhile.class);
        assertTrue(r.code.contains("ground_field1"));
        assertFalse(r.code.contains("ground.field1"));
    }

    @Test
    public void testTransform_Public_NON_Serializable() throws Exception {
        GenResult r = generateCode("callNonSerializable", CtReturn.class);
        assertTrue(r.code.contains("seri_pubNonSerializable"));
        assertFalse(r.code.contains("seri.pubNonSerializable"));
    }

    @Test
    public void testTransform_Public_Serializable_THIZ() throws Exception {
        List<BenchSnippet> snippets = loadSnippets(this, "doSomethingWithPriValue", "SerializableObject", CtIf.class);
        SnippetCode replacement = new SnippetCode();
        String code = replacement.generate(snippets.get(0));
        assertTrue(code.contains("THIZ_priValue"));
        assertFalse(code.contains(" priValue"));
    }

    @Ignore("The correct behavior here is still undefined")
    @Test
    public void testTransform_Public_Serializable_TARGET() throws Exception {
        GenResult r = generateCode("callSerializable", CtReturn.class);
        assertTrue(r.code.contains("seri.pubField"));
        assertFalse(r.code.contains("seri_pubField"));
    }
}