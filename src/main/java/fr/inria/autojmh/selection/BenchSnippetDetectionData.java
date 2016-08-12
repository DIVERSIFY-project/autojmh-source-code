package fr.inria.autojmh.selection;

import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.diversify.syringe.events.AbstractEvent;
import fr.inria.diversify.syringe.events.DetectionEvent;
import spoon.reflect.declaration.CtElement;


/**
 * A DetectionData specially made for the DataContrxtInjector
 *
 * Created by marodrig on 02/11/2015.
 */
public class BenchSnippetDetectionData extends AbstractEvent {

    private BenchSnippet snippet;

    public BenchSnippet getSnippet() {
        return snippet;
    }

    public BenchSnippetDetectionData(BenchSnippet snippet) {
        this.snippet = snippet;
    }

    public void setSnippet(BenchSnippet snippet) {
        this.snippet = snippet;
    }

    @Override
    public CtElement getDetected() {
        return snippet.getASTElement();
    }
}
