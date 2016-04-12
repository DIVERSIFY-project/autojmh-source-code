package fr.inria.autojmh.instrument;

import fr.inria.autojmh.selection.BenchSnippetDetectionData;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.diversify.syringe.SpoonMetaFactory;
import org.junit.Test;
import spoon.processing.AbstractProcessor;
import spoon.processing.ProcessingManager;
import spoon.reflect.code.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.QueueProcessingManager;
import spoon.support.reflect.code.CtCodeSnippetStatementImpl;

import java.util.List;

import static fr.inria.autojmh.ElementProvider.loadFirstSnippets;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class DataContextInjectorTest {

    private void testCollections(String method, String expected, String... nonExpected) throws Exception {
        testCollections(CtLoop.class, method, expected, nonExpected);
    }
    private void testCollections(Class<?> klass, String method, String expected, String... nonExpected) throws Exception {
        BenchSnippet snippet = loadFirstSnippets(this, method, klass);
        BenchSnippetDetectionData dd = new BenchSnippetDetectionData(snippet);
        //Finally inject
        DataContextInjector injector = new DataContextInjector();
        injector.inject(snippet.getASTElement(), dd);

        boolean found = false;
        List<CtCodeSnippetStatement> sts = snippet.getASTElement().getParent().getElements(
                new TypeFilter<CtCodeSnippetStatement>(CtCodeSnippetStatement.class));
        for (CtCodeSnippetStatement st : sts) {
            found |= st.getValue().contains(expected);
            for (String s : nonExpected)
                assertFalse(st.getValue().contains(s));
        }
        assertTrue(found);
    }

    /**
     * A method to test that the injection over a collection of serializables works fine
     */
    @Test
    public void testInjectLog_SerializablesCollection() throws Exception {
        testCollections("collectionOfSerializables", //Method where to inject
                "logSerializableCollection", //Expected injection
                "logDoubleCollection", "logdoubleCollection"); //Unwanted injections
    }

    /**
     * A method to test that the injection over a primitive class collection works fine
     */
    @Test
    public void testInjectLog_PrimitiveClassesCollection() throws Exception {
        testCollections("collectionOfClassPrimitives", //Method where to inject
                "logDoubleCollection", //Expected injection
                "logSerializableCollection", "logdoubleCollection"); //Unwanted injections
    }


    /**
     * Test the special case in which no injection is needed
     */
    @Test
    public void testNoInjection() throws Exception {
        BenchSnippet snippet = loadFirstSnippets(this, "assignCte", CtLocalVariable.class);
        BenchSnippetDetectionData dd = new BenchSnippetDetectionData(snippet);
        //Finally inject
        DataContextInjector injector = new DataContextInjector();
        injector.inject(snippet.getASTElement(), dd);

        List<CtCodeSnippetStatement> injected = snippet.getASTElement().getElements(
                new TypeFilter<CtCodeSnippetStatement>(CtCodeSnippetStatement.class));
        assertEquals(0, injected.size());
    }

    /**
     * Test the special case in which a return is not inside a block
     */
    @Test
    public void testInject_OnSingleReturn() throws Exception {
        testCollections(CtIf.class, "singleReturn", //Method to inject
                "logint" //Expected injection
        ); //Unwanted injections
    }

    /**
     * Test the injection.
     * <p>
     * After the injection all statements following and preceding the snippet's statement must be CtSnippetStatements
     *
     * @throws Exception
     */
    @Test
    public void testInject() throws Exception {

        //Obtain a CtElement to play with
        final CtStatement[] s = new CtStatement[1];
        s[0] = null;
        Factory factory = new SpoonMetaFactory().buildNewFactory(
                this.getClass().getResource("/input_sources").toURI().getPath(), 5);
        ProcessingManager pm = new QueueProcessingManager(factory);
        pm.addProcessor(new AbstractProcessor<CtIf>() {
            @Override
            public void process(CtIf element) {
                if (s[0] != null) return;
                s[0] = element;
            }
        });
        pm.process();

        //Build a detection data for this injector
        BenchSnippetDetectionData dd = new BenchSnippetDetectionData(new BenchSnippet());
        dd.getSnippet().setASTElement(s[0]);

        //Finally inject
        DataContextInjector injector = new DataContextInjector();
        injector.inject(s[0], dd);

        //Assert that two statements where inserted before and after the if
        CtBlock parent = (CtBlock) s[0].getParent();
        int indexIf = parent.getStatements().indexOf(s[0]);
        assertTrue(parent.getStatements().get(indexIf - 1) instanceof CtCodeSnippetStatementImpl);
        assertTrue(parent.getStatements().get(indexIf + 3) instanceof CtCodeSnippetStatementImpl);
        assertTrue(((CtCodeSnippetStatementImpl) parent.getStatement(indexIf + 3)).getValue().contains(".close()"));
    }


}