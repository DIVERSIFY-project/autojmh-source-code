package fr.inria.autojmh.selection;

import fr.inria.autojmh.snippets.SourceCodeSnippet;
import fr.inria.diversify.syringe.SpoonMetaFactory;
import org.junit.Test;
import spoon.processing.ProcessingManager;
import spoon.reflect.factory.Factory;
import spoon.support.QueueProcessingManager;

import java.util.ArrayList;
import java.util.List;

import static fr.inria.autojmh.selection.Tagglet.TaggletKind.BENCH_THIS;
import static fr.inria.autojmh.selection.Tagglet.TaggletKind.BENCH_UNTIL;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class TaggletStatementDetectorTest {

    public static final String CLASS_NAME = "testpack.Trigonometry";

    public static ArrayList<Tagglet> getTaggletsList(String className) {
        //Build the tagglets
        ArrayList<Tagglet> taggletList = new ArrayList<>();
        taggletList.add(new Tagglet(BENCH_THIS, 17, 12, className));
        taggletList.add(new Tagglet(BENCH_THIS, 29, 9, className));
        taggletList.add(new Tagglet(BENCH_UNTIL, 35, 8, className));
        return taggletList;
    }

    public static TaggedStatementDetector process(String sourcePath,
                                                  List<Tagglet> tagglets) throws Exception {
        //Build the processor
        TaggedStatementDetector p = new TaggedStatementDetector();
        p.setTagglets(tagglets);

        //Process the two files
        Factory factory = new SpoonMetaFactory().buildNewFactory(sourcePath, 5);
        ProcessingManager pm = new QueueProcessingManager(factory);
        pm.addProcessor(p);
        pm.process();

        return p;
    }

    @Test
    public void testProduceBenchSnippet() throws Exception {
        ArrayList<Tagglet> tagglets = getTaggletsList(CLASS_NAME);

        TaggedStatementDetector p = process(this.getClass().getResource("/input_sources").toURI().getPath(), tagglets);
        List<SourceCodeSnippet> benchs = p.getSnippets();

        assertEquals(2, benchs.size());
        assertTrue(p.getTagglets().containsKey(benchs.get(0).getClassName()));
        assertTrue(p.getTagglets().containsKey(benchs.get(1).getClassName()));
    }

    @Test
    public void testTaggletsInSourceMatchesStatement() throws Exception {
        //Build some tagglets pragmatically
        ArrayList<Tagglet> tagglets = getTaggletsList(CLASS_NAME);
        TaggedStatementDetector p = process(this.getClass().getResource("/input_sources").toURI().getPath(), tagglets);
        //Asserts
        assertEquals(2, p.getMatches().size());
        assertTrue(p.getMatches().get(tagglets.get(0)).toString().contains("if (senA > 0.5) {"));
        assertTrue(p.getMatches().get(tagglets.get(1)).toString().contains("Math.cos(a)"));
    }
}