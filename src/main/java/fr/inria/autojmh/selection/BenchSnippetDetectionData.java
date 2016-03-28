package fr.inria.autojmh.selection;

import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.diversify.syringe.detectors.DetectionData;

/**
 * A DetectionData specially made for the DataContrxtInjector
 *
 * Created by marodrig on 02/11/2015.
 */
public class BenchSnippetDetectionData extends DetectionData {

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
}
