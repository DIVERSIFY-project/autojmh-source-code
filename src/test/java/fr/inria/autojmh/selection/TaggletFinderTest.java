package fr.inria.autojmh.selection;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class TaggletFinderTest {

    private List<String> buildLines() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("double senA = Math.sin(a); \n");
        lines.add("/** @bench-this */ \n");
        lines.add("if ( senA > 0.5 ) { \n");
        lines.add("    return senA * 2; \n");
        lines.add("}\n");
        lines.add("else {\n");
        lines.add("    return senA;\n");
        lines.add("}\n");
        lines.add(" /** @bench-this */ double cosA = Math.cos(a); \n");
        lines.add("if ( cosA > 0.5 ) { \n");
        lines.add("    return cosA * 2; \n");
        lines.add("} \n");
        lines.add("/**    @bench-until-here */ \n");
        return lines;
    }

    private void assertTagglets(List<Tagglet> tagglets) {
        assertEquals(3, tagglets.size());
        //Assert kind
        assertEquals(Tagglet.TaggletKind.BENCH_THIS, tagglets.get(0).getKind());
        assertEquals(Tagglet.TaggletKind.BENCH_THIS, tagglets.get(1).getKind());
        assertEquals(Tagglet.TaggletKind.BENCH_UNTIL, tagglets.get(2).getKind());
        //Assert line number
        assertEquals(1, tagglets.get(0).getLineNumber());
        assertEquals(8, tagglets.get(1).getLineNumber());
        assertEquals(12, tagglets.get(2).getLineNumber());
        //Assert line column number
        assertEquals(4, tagglets.get(0).getColumnNumber());
        assertEquals(5, tagglets.get(1).getColumnNumber());
        assertEquals(7, tagglets.get(2).getColumnNumber());
        //Assert class
        assertEquals("fr.inria.testing.Test", tagglets.get(0).getClassName());
    }

    @Test
    public void testCollectFromLines() throws Exception {
        TaggletFinder finder = new TaggletFinder();
        List<Tagglet> tagglets = finder.collect(buildLines(), "fr.inria.testing.Test", 0);
        assertTagglets(tagglets);
    }

    @Test
    public void testCollectFromBuffer() throws Exception {
        //ByteArrayInputStream
        TaggletFinder finder = new TaggletFinder();

        StringBuilder b = new StringBuilder();
        for (String l : buildLines()) b.append(l);

        List<Tagglet> tagglets = finder.collect(
                new BufferedReader(new StringReader(b.toString())), "fr.inria.testing.Test", 0);

        assertTagglets(tagglets);
    }
}
