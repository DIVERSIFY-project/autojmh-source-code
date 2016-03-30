package fr.inria.autojmh.selection;

import fr.inria.autojmh.snippets.SourceCodeSnippet;
import fr.inria.diversify.syringe.detectors.DetectionData;

/**
 * A DetectionData specially made for the DataContrxtInjector
 *
 * Created by marodrig on 02/11/2015.
 */
public class BenchSnippetDetectionData extends DetectionData {

    private SourceCodeSnippet snippet;

    public SourceCodeSnippet getSnippet() {
        return snippet;
    }

    public BenchSnippetDetectionData(SourceCodeSnippet snippet) {
        this.snippet = snippet;
    }

    public void setSnippet(SourceCodeSnippet snippet) {
        this.snippet = snippet;
    }
}
