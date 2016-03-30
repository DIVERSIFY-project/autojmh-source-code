package fr.inria.autojmh.tool;

import fr.inria.autojmh.snippets.SourceCodeSnippet;
import org.junit.Test;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtCodeSnippetStatementImpl;

import java.util.List;

import static fr.inria.autojmh.ElementProvider.loadSnippets;
import static fr.inria.autojmh.tool.InstrumentationCleaner.INSTRUMENTATION_PACKAGE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test to the instrumentation cleaner
 *
 * Created by marodrig on 28/03/2016.
 */
public class InstrumentationCleanerTest {

    private CtCodeSnippetStatementImpl codeSnippet(String val) {
        CtCodeSnippetStatementImpl sn = new CtCodeSnippetStatementImpl();
        sn.setValue(val);
        return sn;
    }

    /**
     * Test the clean up of the instrumentation
     * @throws Exception
     */
    @Test
    public void testCleanUp() throws Exception {
        List<SourceCodeSnippet> list = loadSnippets(this, "instrumentedStatements", CtLoop.class);
        //Now insert some instrumentation before and after:
        CtStatement ast = list.get(0).getASTElement();
        ast.insertAfter(codeSnippet(INSTRUMENTATION_PACKAGE + ".a()"));
        ast.insertAfter(codeSnippet(INSTRUMENTATION_PACKAGE + ".b()"));
        ast.insertBefore(codeSnippet(INSTRUMENTATION_PACKAGE + ".c()"));
        ast.insertBefore(codeSnippet(INSTRUMENTATION_PACKAGE + ".d()"));

        //Insert some instrumentation in the return
        CtReturn rt = ast.getElements(new TypeFilter<CtReturn>(CtReturn.class)).get(0);
        rt.insertBefore(codeSnippet(INSTRUMENTATION_PACKAGE + ".c()"));
        rt.insertBefore(codeSnippet(INSTRUMENTATION_PACKAGE + ".d()"));

        //Check that the injection was successful
        boolean found= false;
        for (CtElement st : ast.getParent().getElements(new TypeFilter<>(CtElement.class)))
            found |= st instanceof CtCodeSnippetStatementImpl;
        assertTrue(found);

        //Now Clean up
        InstrumentationCleaner cleaner = new InstrumentationCleaner();
        cleaner.cleanUp(list);

        //Assert
        for (CtElement st : ast.getParent().getElements(new TypeFilter<>(CtElement.class))) {
            assertFalse(st instanceof CtCodeSnippetStatementImpl);
        }
    }
}