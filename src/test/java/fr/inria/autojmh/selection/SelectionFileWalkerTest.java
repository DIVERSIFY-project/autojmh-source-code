package fr.inria.autojmh.selection;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class SelectionFileWalkerTest {

    @Test
    public void testVisitFile() throws Exception {
        SelectionFileWalker walker = new SelectionFileWalker();
        walker.walkDir(this.getClass().getResource("/input_sources/java").toURI().getPath());
        Map<String, List<Tagglet>> tagglets = walker.getTagglets();
        assertEquals(1, tagglets.size());
        assertEquals(3, tagglets.get("testpack.Trigonometry").size());
    }
}