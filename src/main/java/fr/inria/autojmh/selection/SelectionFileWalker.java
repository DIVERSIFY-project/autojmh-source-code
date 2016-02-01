package fr.inria.autojmh.selection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A file walker that goes through all Java files obtaining the selected snippets
 * <p/>
 * Drives the SourceSnippetFinder class
 * <p/>
 * Created by marodrig on 28/10/2015.
 */
public class SelectionFileWalker extends SimpleFileVisitor<Path> {

    /**
     * Tagglet finder
     */
    private TaggletFinder finder;

    /**
     * Root path of the analysis
     */
    private Path rootPath;

    /**
     * Tagglets separated by files
     */
    private Map<String, List<Tagglet>> tagglets;

    /**
     * Walk a directory searching for tagglets
     *
     * @param absolutePath Absolute path for the tagglet.
     * @throws IOException
     */
    public void walkDir(String absolutePath) throws IOException {
        this.rootPath = Paths.get(new File(absolutePath).getAbsolutePath());
        Files.walkFileTree(this.rootPath, this);
    }

    /**
     * Collects all tagglets from a file
     *
     * @param file  File to collect from
     * @param attrs
     * @return
     * @throws IOException
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
        if ( !file.toString().toLowerCase().endsWith(".java") ) return FileVisitResult.CONTINUE;

        try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {
            String className = getClassName(file);
            List<Tagglet> t = getFinder().collect(br, className, 1);
            if (t != null && t.size() > 0) getTagglets().put(className, t);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FileVisitResult.CONTINUE;
    }

    private String getClassName(Path file) {
        String path = rootPath.relativize(file).toString().replace("\\", ".");
        return path.substring(0, path.lastIndexOf(".java"));
    }

    public TaggletFinder getFinder() {
        if (finder == null) finder = new TaggletFinder();
        return finder;
    }

    public void setFinder(TaggletFinder finder) {
        this.finder = finder;
    }


    /**
     * Tagglets separated by file.
     *
     * @return A map with the tagglets separated by file
     */
    public Map<String, List<Tagglet>> getTagglets() {
        if (tagglets == null) tagglets = new HashMap<>();
        return tagglets;
    }
}
