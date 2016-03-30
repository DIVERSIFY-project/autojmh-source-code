package fr.inria.autojmh.snippets.modelattrib;

import fr.inria.autojmh.snippets.SourceCodeSnippet;
import org.junit.Test;
import spoon.reflect.code.*;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;

import static fr.inria.autojmh.ElementProvider.loadSnippets;
import static org.junit.Assert.*;

/**
 * Created by marodrig on 24/03/2016.
 */
public class TypeAttributesTest {

    private interface RefCall {
        boolean call(CtTypeReference ref);
    }

    RefCall primitive = new RefCall() {
        public boolean call(CtTypeReference ref) {
            return new TypeAttributes(ref).isClassPrimitive();
        }
    };

    RefCall collection = new RefCall() {
        public boolean call(CtTypeReference ref) {
            return new TypeAttributes(ref).isCollection();
        }
    };

    RefCall serializable = new RefCall() {
        public boolean call(CtTypeReference ref) {
            return new TypeAttributes(ref).isSerializable();
        }
    };

    private void testProperty(String method, Class<?> klass, boolean expected, RefCall real) throws Exception {
        List<SourceCodeSnippet> list = loadSnippets(this, method, klass);
        CtTypeReference ref = list.get(0).getAccesses().get(0).getType();
        assertEquals(expected, real.call(ref));
    }

    @Test
    public void testIsClassPrimitive_False() throws Exception {
        testProperty("call", CtReturn.class, false, primitive);
    }

    @Test
    public void testIsClassPrimitive_True() throws Exception {
        List<SourceCodeSnippet> list = loadSnippets(this, "containOnlyPrimitiveClasses", CtExpression.class);
        for (CtVariableAccess a : list.get(0).getAccesses())
            assertTrue(new TypeAttributes(a.getType()).isClassPrimitive());
    }

    @Test
    public void testIsCollection_true() throws Exception {
        testProperty("containOnlyCollections", CtInvocation.class, true, collection);
    }

    @Test
    public void testIsCollection_Array_True() throws Exception {
        testProperty("containOnlyArrays", CtReturn.class, true, collection);
    }

    @Test
    public void testIsCollection_False() throws Exception {
        testProperty("containOnlySerializables", CtInvocation.class, false, collection);
    }

    @Test
    public void testIsSerializable_True() throws Exception {
        testProperty("containOnlySerializables", CtInvocation.class, true, serializable);
    }

    @Test
    public void testIsSerializable_False() throws Exception {
        testProperty("arrayOfNonSerializables", CtLoop.class, false, serializable);
    }




}