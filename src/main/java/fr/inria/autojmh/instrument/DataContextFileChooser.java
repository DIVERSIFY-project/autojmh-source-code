package fr.inria.autojmh.instrument;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;

/**
 * A class that consistently select the same thread and the same class loader for all recorded snippets
 * <p/>
 * During a normal unit test execution, many times the execution flow passes over a certain piece of code
 * being benchmarked. This causes the generation of many datafiles.
 * <p/>
 * This class provides a consistent default way of selecting the same thread and the same pass for all pieces
 * <p/>
 * Created by marodrig on 03/11/2015.
 */
public class DataContextFileChooser {

    /**
     * Path where the files recording the execution are loaded
     */
    private String dataContextPath;

    /**
     * Set of thread and class loader
     */
    private HashMap<String, String> choosenBefore;
    private HashMap<String, String> choosenAfter;

    /**
     * Builds the chooser
     */
    public DataContextFileChooser() {
        choosenBefore = new HashMap<>();
        choosenAfter = new HashMap<>();
    }

    public DataContextFileChooser(String dataContextPath) {
        this();
        this.dataContextPath = dataContextPath;
    }

    /**
     * Selects one file recorded after the execution of the
     *
     * @param className
     */
    public String chooseAfter(String className) throws IOException {
        if (choosenAfter.containsKey(className))
            return choosenAfter.get(className);
        else if (choosenBefore.containsKey(className)) {
            String s = "after-" + choosenBefore.get(className);
            choosenBefore.put(className, s);
            return s;
        } else {
            String s = chooseFileFor(className, true);
            choosenAfter.put(className, s);
            return s;
        }
    }

    /**
     * Selects one file recorded after the execution of the
     *
     * @param className
     */
    public String chooseBefore(String className) throws IOException {
        if (choosenBefore.containsKey(className))
            return choosenBefore.get(className);
        else if (choosenAfter.containsKey(className)) {
            String s = choosenAfter.get(className);
            s = s.substring(6, s.length());
            choosenBefore.put(className, s);
            return s;
        } else {
            String s = chooseFileFor(className, false);
            choosenAfter.put(className, s);
            return s;
        }
    }

    private String chooseFileFor(String className, boolean after) throws IOException {

        String c = (after ? "after-" + className : className).replace("_", "-") + "--";

        for (File file : new File(dataContextPath).listFiles() ) {
            if ( file.getName().startsWith(c) ) {
                return file.getName();
            }
        }
        throw new IOException("Could not find file for the class given");
    }



    /**
     * Sets the path where the files recording the execution are loaded
     */
    public void setDataContextPath(String dataContextPath) {
        this.dataContextPath = dataContextPath;
    }

    /**
     * Path where the files recording the execution are loaded
     */
    public String getDataContextPath() {
        return dataContextPath;
    }

    /**
     * Indicates whether exist an input data file for the class name in the given data path
     *
     * @param dataPath  Data path to search for the data file
     * @param className Name of the micro-benchmark class
     * @return True if a file exists, false otherwise
     */
    public boolean existsDataFile(String dataPath, final String className) {
        try {
            final String fileName = className.replace("_", "-") + "--";
            File[] files = new File(dataPath).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(fileName) || name.startsWith("after-" + fileName);
                }
            });
            return files != null && files.length > 0;
        } catch (Exception e) {
            //log.error("Unexpected exception at existDataFile. ", e);
            throw new RuntimeException(e);
        }
    }
}
