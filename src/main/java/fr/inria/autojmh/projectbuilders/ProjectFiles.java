package fr.inria.autojmh.projectbuilders;

import java.io.File;
import java.io.IOException;

/**
 * A class to handle common operation on project's files
 *
 * Created by marodrig on 29/10/2015.
 */
public class ProjectFiles {

    public static void removeRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) removeRecursively(f);
            }
            file.delete();
        } else file.delete();
    }

    public static File makeIfNotExists(String path) throws IOException {
        File f = new File(path);
        if (!f.exists())
            if (!f.mkdirs()) throw new IOException("Unable to build " + path);
        return f;
    }
}
