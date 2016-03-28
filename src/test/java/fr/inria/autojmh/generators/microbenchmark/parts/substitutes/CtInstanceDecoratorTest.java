package fr.inria.autojmh.generators.microbenchmark.parts.substitutes;

import fr.inria.autojmh.generators.microbenchmark.parts.SnippetCode;
import fr.inria.autojmh.generators.printer.AJMHPrettyPrinter;
import fr.inria.autojmh.snippets.BenchSnippet;
import org.junit.Test;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

import static fr.inria.autojmh.ElementProvider.loadSnippets;
import static org.junit.Assert.*;

/**
 * Created by marodrig on 24/03/2016.
 */
public class CtInstanceDecoratorTest {

    @Test
    public void testToString() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "callInvocations", CtIf.class);
        SnippetCode replacement = new SnippetCode();
        replacement.generate(list.get(0));
        List<CtInvocation> invs = list.get(0).getASTElement().getElements(
                new TypeFilter<CtInvocation>(CtInvocation.class));
        assertEquals(3, invs.size());
        for ( CtInvocation inv : invs ) {
            assertTrue(inv.toString().contains(inv.getExecutable().getSimpleName() + "(THIZ"));
            assertFalse(inv.toString().contains(", )"));
            //Private methods should be printed method(THIZ)
            assertFalse(inv.toString().contains("callPrivate()"));
            assertFalse(inv.toString().contains("callProtected()"));
        }
    }
}