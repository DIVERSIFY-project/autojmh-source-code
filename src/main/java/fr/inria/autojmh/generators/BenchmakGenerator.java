package fr.inria.autojmh.generators;

import fr.inria.autojmh.snippets.SourceCodeSnippet;
import fr.inria.autojmh.tool.Configurable;

import java.util.Collection;

/**
 * Created by marodrig on 27/10/2015.
 */
public interface BenchmakGenerator extends Configurable {
    /**
     * Collections of snippets to generate the output of this generator
     *
     * @param snippets
     */
    void setSnippets(Collection<SourceCodeSnippet> snippets);

    /**
     * Collections of snippets
     */
    Collection<SourceCodeSnippet> getSnippets();

    /**
     * Generate output
     */
    public void generate();
}
