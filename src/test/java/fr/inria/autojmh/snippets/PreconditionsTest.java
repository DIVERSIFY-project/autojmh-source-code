package fr.inria.autojmh.snippets;

import fr.inria.autojmh.ElementProvider;
import org.junit.Test;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtReturn;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.reference.CtTypeReferenceImpl;

import java.util.List;

import static fr.inria.autojmh.ElementProvider.loadSnippets;
import static org.junit.Assert.*;

/**
 * Created by marodrig on 24/03/2016.
 */
public class PreconditionsTest {

    @Test
    public void testCheckSnippet_Variables_Pass() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "anIntMethod", CtIf.class);
        Preconditions preconditions = new Preconditions();
        assertTrue(preconditions.checkSnippet(list.get(0)));
    }

    @Test
    public void testCheckSnippet_Variables_DONT_Pass() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "arrayOfNonSerializables", CtLoop.class);
        Preconditions preconditions = new Preconditions();
        assertFalse(preconditions.checkSnippet(list.get(0)));
    }

    @Test
    public void testCheckSnippet_Methods_Fail() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "callTheCallDontPass", CtIf.class);
        Preconditions preconditions = new Preconditions();
        assertFalse(preconditions.checkSnippet(list.get(0)));
    }

    @Test
    public void testCheckSnippet_ProtectedAbstract_Fail() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "callProtectedAbstractMethod", CtIf.class);
        Preconditions preconditions = new Preconditions();
        assertFalse(preconditions.checkSnippet(list.get(0)));
        assertFalse(list.get(0).meetsPreconditions());
    }

    @Test
    public void testCheckSnippet_Methods_Pass() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "callTheCallPass", CtIf.class);
        Preconditions preconditions = new Preconditions();
        assertTrue(preconditions.checkSnippet(list.get(0)));
    }

    @Test
    public void testCheckTypeRef_Fail() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "call", CtReturn.class);
        CtTypeReference ref = list.get(0).getAccesses().get(0).getType();
        Preconditions preconditions = new Preconditions();
        assertFalse(preconditions.checkTypeRef(ref));
    }

    @Test
    public void testCheckTypeRef_Pass() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "arrayOfSerializables", CtLoop.class);
        CtTypeReference ref = list.get(0).getAccesses().get(0).getType();
        Preconditions preconditions = new Preconditions();
        assertTrue(preconditions.checkTypeRef(ref));
    }

}