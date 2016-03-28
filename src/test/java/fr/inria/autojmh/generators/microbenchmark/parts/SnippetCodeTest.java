package fr.inria.autojmh.generators.microbenchmark.parts;

import fr.inria.autojmh.generators.microbenchmark.parts.substitutes.CtInvocationDecorator;
import fr.inria.autojmh.generators.microbenchmark.parts.substitutes.CtVariableAccessDecorator;
import fr.inria.autojmh.snippets.BenchSnippet;
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

    @Test
    public void testTransform_Invocations() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "callInvocations", CtIf.class);
        SnippetCode replacement = new SnippetCode();
        String code = replacement.generate(list.get(0));

        //Assert that all invocations have been replaced
        List<CtInvocation> invs = list.get(0).getASTElement().getElements(
                new TypeFilter<CtInvocation>(CtInvocation.class));
        assertEquals(3, invs.size());
        for (CtInvocation inv : invs) assertTrue(inv instanceof CtInvocationDecorator);
    }

    @Test
    public void testTransform_Invocations_Public() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "callInvocationsSomePublic", CtIf.class);
        SnippetCode replacement = new SnippetCode();
        String code = replacement.generate(list.get(0));
        //Assert the code generation
        assertTrue(code.contains("callPrivate(THIZ)"));
        assertFalse(code.contains("callPrivate()"));
        assertTrue(code.contains("callProtected(THIZ)"));
        assertFalse(code.contains("callProtected()"));
        assertFalse(code.contains("callPublic(THIZ)"));
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

    /**
     * A protected abstract method should be rejected by the preconditions. So we throw!
     */
    @Test(expected = RuntimeException.class)
    public void testTransform_Invocations_AbstractProtected() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "callProtectedAbstractMethod", CtIf.class);
        SnippetCode replacement = new SnippetCode();
        replacement.generate(list.get(0));
    }

    @Test
    public void testTransform_Variables() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "arrayOfSerializables", CtLoop.class);
        SnippetCode replacement = new SnippetCode();
        replacement.generate(list.get(0));
        List<CtVariableAccess> invs = list.get(0).getAccesses();
        assertEquals(4, invs.size());
        for (CtVariableAccess inv : invs)
            assertTrue(inv instanceof CtVariableAccessDecorator);
    }
}