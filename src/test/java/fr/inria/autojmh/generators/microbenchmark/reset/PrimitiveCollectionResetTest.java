package fr.inria.autojmh.generators.microbenchmark.reset;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by marodrig on 02/02/2016.
 */
public class PrimitiveCollectionResetTest {

    @Test
    public void testResetCodeNumber() throws Exception {
        ArrayList<Integer> a = new ArrayList<>();
        a.add(1);
        a.add(2);
        a.add(3);

        PrimitiveCollectionReset r = new PrimitiveCollectionReset();
        String c = r.resetCode("a", a);
        assertTrue(c.contains("a.clear();\n"));
        assertTrue(c.contains("a.add(1);\n"));
        assertTrue(c.contains("a.add(2);\n"));
        assertTrue(c.contains("a.add(3);\n"));
    }

    @Test
    public void testResetCodeString() throws Exception {
        ArrayList<String> a = new ArrayList<>();
        a.add("AA");
        a.add("BB");
        a.add("CC");

        PrimitiveCollectionReset r = new PrimitiveCollectionReset();
        String c = r.resetCode("a", a);
        assertTrue(c.contains("a.clear();\n"));
        assertTrue(c.contains("a.add(\"AA\");\n"));
        assertTrue(c.contains("a.add(\"BB\");\n"));
        assertTrue(c.contains("a.add(\"CC\");\n"));
    }


    @Test
    public void testResetFromAnotherVar() throws Exception {
        ArrayList<String> a = new ArrayList<>();
        ArrayList<String> b = new ArrayList<>();
        a.add("AA");
        a.add("BB");
        a.add("CC");
        PrimitiveCollectionReset r = new PrimitiveCollectionReset();
        String c = r.resetFromAnotherVar("a", "b");
        assertEquals(c, "a.clear();\nfor (int i = 0; i < b.size(); i++) a.add(b.get(i));\n");
    }
}