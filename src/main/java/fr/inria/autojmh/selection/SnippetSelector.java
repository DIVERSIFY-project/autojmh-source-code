package fr.inria.autojmh.selection;

import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.diversify.syringe.detectors.AbstractDetector;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class to all snippet selectors
 *
 * Created by marodrig on 05/11/2015.
 */
public abstract class SnippetSelector<E extends CtStatement> extends AbstractDetector<E> {

    public static final String SNIPPET_DETECTED = "SNIPPET_DETECTED";

    /**
     * Resulting snippets
     */
    protected List<BenchSnippet> snippets;

    public List<BenchSnippet> getSnippets() {
        if ( snippets == null ) snippets = new ArrayList<>();
        return snippets;
    }

    @Override
    public int getElementsDetectedCount() {
        return getSnippets().size();
    }

    public void select(E e) {
        BenchSnippet s = new BenchSnippet();
        s.setASTElement(e);
        if (snippets == null) snippets = new ArrayList<>();
        snippets.add(s);
    }

    @Override
    public void processingDone() {
        for (BenchSnippet s : snippets) {
            BenchSnippetDetectionData data = new BenchSnippetDetectionData();
            data.setSnippet(s);
            notify(SNIPPET_DETECTED, s.getASTElement(), data);
        }
    }
}
