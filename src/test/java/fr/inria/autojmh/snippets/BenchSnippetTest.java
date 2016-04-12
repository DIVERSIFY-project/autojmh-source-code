package fr.inria.autojmh.snippets;

import fr.inria.autojmh.ElementProvider;
import fr.inria.autojmh.selection.TaggedStatementDetector;
import org.junit.Test;
import spoon.reflect.code.*;

import java.util.List;

import static fr.inria.autojmh.ElementProvider.loadFirstSnippets;
import static fr.inria.autojmh.selection.TaggletStatementDetectorTest.*;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class BenchSnippetTest {

    @Test
    public void testMeetsPreconditions() throws Exception {
        List<BenchSnippet> snippets = loadSnippets("arrayOfSerializables");
        assertTrue(snippets.get(0).meetsPreconditions());

        snippets = loadSnippets("arrayOfNonSerializables");
        junit.framework.Assert.assertFalse(snippets.get(0).meetsPreconditions());
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
        assertEquals(snippets.get(0).getCode(), 3, snippets.get(0).getAccesses().size());
    }

    @Test
    public void testGetTemplateAccessesWrappers() throws Exception {
        //Get some BenchSnippets
        TaggedStatementDetector p = process(
                this.getClass().getResource("/input_sources/java").toURI().getPath(), getTaggletsList(CLASS_NAME));
        List<BenchSnippet> benchs = p.getSnippets();

        //We don't know the order in which they came out, some times is 0 some others is 1
        List<TemplateInputVariable> wraps = benchs.get(0).getTemplateAccessesWrappers();
        if (!wraps.get(0).getVariableName().equals("a")) wraps = benchs.get(1).getTemplateAccessesWrappers();

        assertEquals(1, wraps.size());
        assertEquals("a", wraps.get(0).getVariableName());
        assertTrue(wraps.get(0).isInitialized());
        assertFalse(wraps.get(0).getIsArray());
    }

    /**
     * Test the case in which no injection is needed
     */
    @Test
    public void testNoInjectionNeeded() throws Exception {
        BenchSnippet snippet = loadFirstSnippets(this, "assignCte", CtLocalVariable.class);
        assertEquals(0, snippet.getInitialized().size());
        assertFalse(snippet.isNeedsInitialization());
    }

    /**
     * Test the proper extraction of an array of objects.
     */
    @Test
    public void testArrayOfObjects() throws Exception {
        List<BenchSnippet> snippets = loadSnippets("arrayOfObjects");
        assertEquals(3, snippets.get(0).getInitialized().size());
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
        assertEquals(snippets.get(0).toString(), 3, snippets.get(0).getInitialized().size());
    }

    @Test
    public void testGetInitializedArrayOfSerializables() throws Exception {
        List<BenchSnippet> snippets = loadSnippets("arrayOfSerializables");
        assertEquals(2, snippets.get(0).getInitialized().size());
    }

    /**
     * Test the proper extraction of fields of serializables
     */
    @Test
    public void testGetInitialized_FieldOfSerializable() throws Exception {
        BenchSnippet snippet = loadFirstSnippets(this, "callSerializable", CtIf.class);
        snippet.setPrinterToAJMH();
        assertEquals(2, snippet.getInitialized().size());
        assertEquals("seri_values", snippet.getTemplateAccessesWrappers().get(0).getTemplateCodeCompilableName());
        assertEquals("seri", snippet.getTemplateAccessesWrappers().get(1).getTemplateCodeCompilableName());
    }


    /**
     * Test the proper extraction of fields of serializables
     */
    @Test
    public void testPrivateStaticMethod() throws Exception {
        BenchSnippet snippet = loadFirstSnippets(this, "privateStaticMethod", CtIf.class);
        snippet.setPrinterToAJMH();
        assertEquals(1, snippet.getInitialized().size());
        //assertEquals(2, snippet.getAccesses().size());  //<--DEFINE BEHAVIOR
        int constants = 0;
        for (TemplateInputVariable t : snippet.getTemplateAccessesWrappers()) {
            if ( t.getIsPrivateConstant() || t.getIsPublicConstant() ) constants++;
        }
        assertEquals(2, constants);
    }

    /**
     * Test calling a method with private fields in it
     */
    @Test
    public void test_CallPrivateMethodWithPrivateFields() throws Exception {
        BenchSnippet snippet = loadFirstSnippets(this, "callPrivateMethodWithPrivateFields", CtReturn.class);
        snippet.setPrinterToAJMH();
        assertEquals(snippet.toString(), "k", snippet.getTemplateAccessesWrappers().get(0).getTemplateCodeCompilableName());
        assertEquals(snippet.toString(), "THIZ_field1", snippet.getTemplateAccessesWrappers().get(1).getTemplateCodeCompilableName());
        assertEquals(snippet.toString(), 2, snippet.getInitialized().size());

    }


    /**
     * Test that public fields of allowed objects are substituted for their allowed parents
     */
    @Test
    public void testsnippetIsABlock() throws Exception {
        BenchSnippet snippet = loadFirstSnippets(this,
                "snippetIsABlock", CtBlock.class);
        assertEquals(snippet.getASTElement().toString(), 3, snippet.getInitialized().size());
    }

    /**
     * Test that public fields of allowed objects are substituted for their allowed parents
     */
    @Test
    public void testReplaceFieldByAllowedParent() throws Exception {
        BenchSnippet snippet = loadFirstSnippets(this, "realcases.AdenseArrayMatrixCase",
                "mikeraAdenseArrayMatrix46", CtIf.class);
        boolean found = false;
        for (CtVariableAccess var : snippet.getAccesses()) {
            assertNotEquals(var.getVariable().getSimpleName(), "data");
            found |= var.getVariable().getSimpleName() == "THIZ";
        }
        assertFalse(found);

        for ( TemplateInputVariable var : snippet.getTemplateAccessesWrappers() ) {
            found |= var.getTemplateCodeCompilableName().equals("THIZ") &&
                    var.getInstrumentedCodeCompilableName().equals("this");
        }
    }

    /**
     * Test that public fields of allowed objects are substituted for their allowed parents.
     * Checks also that in static context THIZ is never mentioned
     */
    @Test
    public void testTHIZInStaticContext() throws Exception {
        BenchSnippet snippet = loadFirstSnippets(this, "realcases.Index54", "create", CtLoop.class);
        boolean foundTHIZ = false;
        boolean foundInd = false;
        for (CtVariableAccess var : snippet.getAccesses()) {
            foundTHIZ |= var.getVariable().getSimpleName().equals("THIZ");
            foundInd |= var.getVariable().getSimpleName().equals("ind");
        }
        //Assert that the "THIZ" variable was not in static context
        assertFalse(foundTHIZ);
        //Assert that the field was substituted
        assertTrue(foundInd);
    }

    @Test
    public void testmikera_matrixx_alg_Definite_isPositiveSemiDefinite() throws Exception {
        BenchSnippet snippet = loadFirstSnippets(this, "realcases.Index54", "isPositiveSemiDefinite", CtLoop.class);
        boolean foundv = false;
        for (CtVariableAccess var : snippet.getInitialized()) {
            foundv |= var.getVariable().getSimpleName().equals("v");
        }
        assertFalse(foundv);
    }

    /*
    public void testTHIZis_this() {

    }*/

}