package fr.inria.autojmh.generators.transformations.substitutes;

import fr.inria.autojmh.generators.transformations.DecoratorsReplacement;
import fr.inria.autojmh.generators.transformations.printer.AJMHPrettyPrinter;
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
        DecoratorsReplacement replacement = new DecoratorsReplacement();
        replacement.transform(list.get(0));
        List<CtInvocation> invs = list.get(0).getASTElement().getElements(
                new TypeFilter<CtInvocation>(CtInvocation.class));
        assertEquals(3, invs.size());
        for ( CtInvocation inv : invs ) {
            System.out.print(inv.toString());
            assertTrue(inv.toString().contains(inv.getExecutable().getSimpleName() + "(THIZ"));
            assertFalse(inv.toString().contains(", )"));
        }

        AJMHPrettyPrinter printer = new AJMHPrettyPrinter(list.get(0).getASTElement().getFactory().getEnvironment());
        printer.scan(list.get(0).getASTElement());
        String ppPrint = printer.toString();
        assertTrue(ppPrint.contains("callPrivate(THIZ)"));
        assertTrue(ppPrint.contains("callProtected(THIZ)"));
    }
}