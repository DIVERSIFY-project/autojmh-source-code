package fr.inria.autojmh.generators.transformations;

import fr.inria.autojmh.snippets.BenchSnippet;

/**
 * Extract private static method out of an statement and copy its body to the microbenchmark
 *
 */
public class AvoidDCE extends AbstractTransformation {

    @Override
    public void transform(BenchSnippet snippet) {

    }

    @Override
    public String getGeneratedCode() {
        return null;
    }

    @Override
    public String getModifiedSnippetCode() {
        return null;
    }
}
