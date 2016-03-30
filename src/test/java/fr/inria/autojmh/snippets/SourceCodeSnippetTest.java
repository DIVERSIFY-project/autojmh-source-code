package fr.inria.autojmh.snippets;

import fr.inria.autojmh.ElementProvider;
import fr.inria.autojmh.selection.TaggedStatementDetector;
import org.junit.Test;
import spoon.reflect.code.CtIf;

import java.util.List;
import static fr.inria.autojmh.selection.TaggletStatementDetectorTest.*;
import static junit.framework.Assert.*;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SourceCodeSnippetTest {

    @Test
    public void testMeetsPreconditions() throws Exception {
        List<BenchSnippet> snippets = loadSnippets("arrayOfSerializables");
        assertTrue(snippets.get(0).meetsPreconditions());

        snippets = loadSnippets("arrayOfNonSerializables");
        assertFalse(snippets.get(0).meetsPreconditions());
    }

    @Test
    public void testGetMicrobenchmarkClassName() throws Exception {
        List<BenchSnippet> snippets = loadSnippets("arrayOfSerializables");
        assertTrue(snippets.get(0).getMicrobenchmarkClassName().startsWith("fr_inria_testproject_context_DataContextPlayGround"));

    }

    @Test
    public void testGetPosition() throws Exception {
        List<BenchSnippet> snippets = loadSnippets("arrayOfSerializables");
        assertTrue(snippets.get(0).getPosition().contains(":"));
        assertTrue(snippets.get(0).getPosition().startsWith("fr.inria.testproject.context"));
    }

    @Test
    public void testGetBenchMethodReturnType() throws Exception {
        List<BenchSnippet> snippets = loadSnippets("arrayOfSerializables");
        assertEquals("void", snippets.get(0).getBenchMethodReturnType());

        snippets = ElementProvider.loadSnippets(this, "anIntMethod", CtIf.class);
        assertEquals("int", snippets.get(0).getBenchMethodReturnType());
    }

    @Test
    public void testGetAccesses() throws Exception {
        List<BenchSnippet> snippets = loadSnippets("arrayOfSerializables");
        assertEquals(4, snippets.get(0).getAccesses().size());
    }

    @Test
    public void testGetTemplateAccessesWrappers() throws Exception {
        //Get some BenchSnippets
        TaggedStatementDetector p = process(
                this.getClass().getResource("/input_sources/java").toURI().getPath(), getTaggletsList(CLASS_NAME));
        List<BenchSnippet> benchs = p.getSnippets();

        //We don't know the order in which they came out, some times is 0 some others is 1
        List<TemplateInputVariable> wraps = benchs.get(0).getTemplateAccessesWrappers();
        if ( !wraps.get(0).getVariableName().equals("a") ) wraps = benchs.get(1).getTemplateAccessesWrappers();

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
        List<BenchSnippet> snippets = loadSnippets("arrayOfObjects");
        assertEquals(2, snippets.get(0).getInitialized().size());
    }

    private List<BenchSnippet> loadSnippets(String arrayOfObjects) throws Exception {
        return ElementProvider.loadSnippets(this, arrayOfObjects);
    }

    /**
     * Test the proper extraction of an array of serializable.
     */
    @Test
    public void testGetInitializedArrayOf_NON_Serializables() throws Exception {
        List<BenchSnippet> snippets = loadSnippets("arrayOfNonSerializables");
        assertEquals(2, snippets.get(0).getInitialized().size());
    }

    @Test
    public void testGetInitializedArrayOfSerializables() throws Exception {
        List<BenchSnippet> snippets = loadSnippets("arrayOfSerializables");
        assertEquals(2, snippets.get(0).getInitialized().size());
    }
}