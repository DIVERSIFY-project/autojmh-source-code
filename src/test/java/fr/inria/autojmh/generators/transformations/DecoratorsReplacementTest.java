package fr.inria.autojmh.generators.transformations;

import fr.inria.autojmh.generators.transformations.substitutes.CtInvocationDecorator;
import fr.inria.autojmh.generators.transformations.substitutes.CtVariableAccessDecorator;
import fr.inria.autojmh.snippets.BenchSnippet;
import org.junit.Test;
import spoon.reflect.code.*;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

import static fr.inria.autojmh.ElementProvider.loadSnippets;
import static org.junit.Assert.*;

/**
 * Created by marodrig on 25/03/2016.
 */
public class DecoratorsReplacementTest {

    @Test
    public void testTransform_Invocations() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "callInvocations", CtIf.class);
        DecoratorsReplacement replacement = new DecoratorsReplacement();
        replacement.transform(list.get(0));
        List<CtInvocation> invs = list.get(0).getASTElement().getElements(
                new TypeFilter<CtInvocation>(CtInvocation.class));
        assertEquals(3, invs.size());
        for ( CtInvocation inv : invs )
            assertTrue(inv instanceof CtInvocationDecorator);
    }

    @Test
    public void testTransform_Variables() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "callInvocationsSomePublic", CtLoop.class);
        DecoratorsReplacement replacement = new DecoratorsReplacement();
        replacement.transform(list.get(0));
        List<CtVariableAccess> invs = list.get(0).getAccesses();
        assertEquals(4, invs.size());
        for ( CtVariableAccess inv : invs )
            assertTrue(inv instanceof CtVariableAccessDecorator);
    }
}