package fr.inria.autojmh.generators.microbenchmark.parts;

import fr.inria.autojmh.snippets.SourceCodeSnippet;

/**
 * Class generating code for one specific part of a microbenchmark for a given segment
 *
 * Created by marodrig on 23/03/2016.
 */
public interface MicrobenchmarkPart {
    /**
     * Performs the transformation of the snippet's code
     * @param snippet Snippet to be transformed
     */
    String generate(SourceCodeSnippet snippet);

}
