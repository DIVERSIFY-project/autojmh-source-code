package fr.inria.autojmh.instrument;

import fr.inria.autojmh.generators.AJMHGenerator;
import fr.inria.autojmh.generators.BenchmarkTest;
import fr.inria.autojmh.selection.SnippetSelector;
import fr.inria.autojmh.selection.TaggedStatementDetector;
import fr.inria.autojmh.selection.Tagglet;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.snippets.TemplateInputVariable;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.diversify.syringe.SpoonMetaFactory;
import fr.inria.diversify.syringe.detectors.Detector;
import org.junit.Test;
import spoon.processing.ProcessingManager;
import spoon.reflect.code.CtLoop;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.support.QueueProcessingManager;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static fr.inria.autojmh.selection.TaggletStatementDetectorTest.CLASS_NAME;
import static fr.inria.autojmh.selection.TaggletStatementDetectorTest.getTaggletsList;
import static fr.inria.autojmh.selection.TaggletStatementDetectorTest.process;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class DataContextResolverTest extends BenchmarkTest {


    /**
     * Selects from the DataContextPlayGround class located in the resources of the test,
     * the variables of the snippets in the method passed as parameter
     *
     * @param method Method passed as parameter
     * @return
     * @throws Exception
     */
    private List<BenchSnippet> loopSnippets(final String method) throws Exception {
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

    @Test
    public void testExtractContext() throws Exception {
        //Get some BenchSnippets
        TaggedStatementDetector p = process(
                this.getClass().getResource("/input_sources/java").toURI().getPath(), getTaggletsList(CLASS_NAME));
        List<BenchSnippet> benchs = p.getSnippets();

        //Resolve inputs to some of them
        DataContextResolver resolver = new DataContextResolver();
        resolver.resolve(benchs.get(0));

        List<TemplateInputVariable> wraps = benchs.get(0).getTemplateAccessesWrappers();
        assertEquals(1, wraps.size());
        assertEquals("a", wraps.get(0).getVariableName());
        assertTrue(wraps.get(0).isInitialized());
        assertFalse(wraps.get(0).getIsArray());
    }

    /**
     * Test the proper extraction of an array of objects.
     */
    @Test
    public void testArrayOfObjects() throws Exception {
        List<BenchSnippet> snippets = loopSnippets("arrayOfObjects");
        DataContextResolver r = new DataContextResolver();
        r.resolve(snippets.get(0));
        assertEquals(0, snippets.get(0).getInitialized().size());
    }

    /**
     * Test the proper extraction of an array of serializable.
     */
    @Test
    public void testArrayOfSerializable() throws Exception {
        List<BenchSnippet> snippets = loopSnippets("arrayOfSerializables");
        DataContextResolver r = new DataContextResolver();
        r.resolve(snippets.get(0));
        assertEquals(2, snippets.get(0).getInitialized().size());
    }

    @Test
    public void testArrayOfNonSerializable() throws Exception {
        List<BenchSnippet> snippets = loopSnippets("arrayOfNonSerializables");
        DataContextResolver r = new DataContextResolver();
        r.resolve(snippets.get(0));
        assertEquals(0, snippets.get(0).getInitialized().size());
    }
}