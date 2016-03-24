package fr.inria.autojmh.snippets;

import fr.inria.autojmh.selection.SnippetSelector;
import fr.inria.autojmh.selection.TaggedStatementDetector;
import fr.inria.diversify.syringe.SpoonMetaFactory;
import org.junit.Test;
import spoon.processing.ProcessingManager;
import spoon.reflect.code.CtLoop;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.support.QueueProcessingManager;

import java.util.List;

import static fr.inria.autojmh.selection.TaggletStatementDetectorTest.*;
import static junit.framework.Assert.*;
import static org.junit.Assert.fail;

public class BenchSnippetTest {

    @Test
    public void testGetMicrobenchmarkClassName() throws Exception {
        List<BenchSnippet> snippets = loopSnippets("arrayOfSerializables");
        assertTrue(snippets.get(0).getMicrobenchmarkClassName().startsWith("fr_inria_testproject_context_DataContextPlayGround"));

    }

    @Test
    public void testGetPosition() throws Exception {
        List<BenchSnippet> snippets = loopSnippets("arrayOfSerializables");
        assertTrue(snippets.get(0).getPosition().contains(":"));
        assertTrue(snippets.get(0).getPosition().startsWith("fr.inria.testproject.context"));
    }

/*
    @Test
    public void testGetBenchMethodReturnType() throws Exception {
        fail();
    }

    @Test
    public void testGetTemplateAccessesWrappers() throws Exception {
        fail();
    }

    @Test
    public void testGetAccesses() throws Exception {
        fail();
    }

    @Test
    public void testGetInitialized() throws Exception {
        fail();
    }

    @Test
    public void testGetLineNumber() throws Exception {
        fail();
    }
*/

    @Test
    public void testExtractContext() throws Exception {
        //Get some BenchSnippets
        TaggedStatementDetector p = process(
                this.getClass().getResource("/input_sources/java").toURI().getPath(), getTaggletsList(CLASS_NAME));
        List<BenchSnippet> benchs = p.getSnippets();

        //Resolve inputs to some of them
        //DataContextResolver resolver = new DataContextResolver();
        //resolver.resolve(benchs.get(0));

        //We don't know the order in which they came out, some times is 0 some others is 1
        List<TemplateInputVariable> wraps = benchs.get(0).getTemplateAccessesWrappers();
        if ( !wraps.get(0).getVariableName().equals("a") ) wraps = benchs.get(1).getTemplateAccessesWrappers();

        assertEquals(1, wraps.size());
        assertEquals("a", wraps.get(0).getVariableName());
        assertTrue(wraps.get(0).isInitialized());
        assertFalse(wraps.get(0).getIsArray());

    }

    /**
     * Selects from the DataContextPlayGround class located in the resources of the test,
     * the variables of the snippets in the method passed as parameter
     *
     * @param method Method passed as parameter
     * @return
     * @throws Exception
     */
    private List<BenchSnippet> loopSnippets(final String method) throws Exception {
        //Process the two files
        Factory factory = new SpoonMetaFactory().buildNewFactory(
                this.getClass().getResource(
                        "/testproject/src/main/java/fr/inria/testproject/context").toURI().getPath(), 5);
        ProcessingManager pm = new QueueProcessingManager(factory);
        SnippetSelector<CtLoop> selector = new SnippetSelector<CtLoop>() {
            @Override
            public void process(CtLoop element) {
                String name = element.getPosition().getCompilationUnit().getMainType().getSimpleName();
                CtMethod m = element.getParent(CtMethod.class);
                if (m != null && name.equals("DataContextPlayGround") && m.getSimpleName().equals(method)) {
                    select(element);
                }
            }
        };
        pm.addProcessor(selector);
        pm.process();
        return selector.getSnippets();
    }

    /**
     * Test the proper extraction of an array of objects.
     */
    @Test
    public void testArrayOfObjects() throws Exception {
        List<BenchSnippet> snippets = loopSnippets("arrayOfObjects");
        //DataContextResolver r = new DataContextResolver();
        //r.resolve(snippets.get(0));
        assertEquals(1, snippets.get(0).getInitialized().size());
    }

    /**
     * Test the proper extraction of an array of serializable.
     */
    @Test
    public void testArrayOfSerializable() throws Exception {
        List<BenchSnippet> snippets = loopSnippets("arrayOfSerializables");
        //DataContextResolver r = new DataContextResolver();
        //r.resolve(snippets.get(0));
        assertEquals(2, snippets.get(0).getInitialized().size());
    }

    @Test
    public void testArrayOfNonSerializable() throws Exception {
        List<BenchSnippet> snippets = loopSnippets("arrayOfNonSerializables");
        //DataContextResolver r = new DataContextResolver();
        //r.resolve(snippets.get(0));
        assertEquals(1, snippets.get(0).getInitialized().size());
    }
}