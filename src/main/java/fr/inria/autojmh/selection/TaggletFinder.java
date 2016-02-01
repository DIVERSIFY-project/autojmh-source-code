package fr.inria.autojmh.selection;

import fr.inria.autojmh.selection.Tagglet.TaggletKind;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that obtains the selected snippets from text
 * <p/>
 * Created by marodrig on 28/10/2015.
 */
public class TaggletFinder {

    /**
     * Collected tagglets
     List<Tagglet> tagglets = new ArrayList<Tagglet>();
     */

    private static final String BENCH_THIS = "@bench-this";
    private static final String BENCH_UNTIL = "@bench-until-here";

    private BoyerMoore benchPatrn = new BoyerMoore("@bench-");

    /**
     * Collect the tagglets of a set of lines of code
     *
     * @param lines
     * @param startingLine Index of the first line being METHODS_NAME in the source file. Zero if the reading begin from the
     *                     top
     * @className Name of the class that the source code belongs to
     * @return
     */
    public List<Tagglet> collect(List<String> lines, String className, int startingLine) {
        int i = startingLine;
        ArrayList<Tagglet> tagglets = new ArrayList<>();
        for (String l : lines) {
            collect(l, i, className, tagglets);
            i++;
        }
        return tagglets;
    }


    /**
     * Collects all tagglets of a single line
     *
     * @param line
     */
    protected void collect(String line, int lineNumber, String className, List<Tagglet> tagglets) {
        int i = 0;
        while (i < line.length()) {
            i += benchPatrn.search(line.substring(i));
            if (line.substring(i).startsWith(BENCH_THIS)) {
                tagglets.add(new Tagglet(TaggletKind.BENCH_THIS, lineNumber, i, className));
                i += BENCH_THIS.length();
            } else if (line.substring(i).startsWith(BENCH_UNTIL)) {
                tagglets.add(new Tagglet(TaggletKind.BENCH_UNTIL, lineNumber, i, className));
                i += BENCH_UNTIL.length();
            }
        }
    }

    /**
     * Collects directly from a BufferedReader
     *
     *
     *
     * @param reader Buffered reader to METHODS_NAME from
     * @param className Name of the class we are reading from
     * @param startingLine Index of the first line being METHODS_NAME in the source file. Zero if the reading begin from the
     *                    top. THIS INDEX IS 1 BASED
     */
    public List<Tagglet> collect(BufferedReader reader, String className, int startingLine) throws IOException {
        int k = startingLine;
        ArrayList<Tagglet> tagglets = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            collect(line, k, className, tagglets);
            k++;
        }
        return tagglets;
    }

    /**
     * Obtain the snippets from the file
     * @return A collection of BenchSnippets

    public List<Tagglet> getTagglets() {
        return tagglets;
    }*/
}
