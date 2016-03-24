package fr.inria.autojmh.generators.transformations;

import fr.inria.autojmh.snippets.BenchSnippet;

/**
 * Created by marodrig on 23/03/2016.
 */
public interface Transformation {
    /**
     * Performs the transformation of the snippet's code
     * @param snippet Snippet to be transformed
     */
    void transform(BenchSnippet snippet);

    /**
     * Generate the extra code that goes inside the class
     * @return A string with the generated code
     */
    String getGeneratedCode();

    /**
     * Gets the modified code of the snippet
     * @return A string with the generated code
     */
    String getModifiedSnippetCode();
}
