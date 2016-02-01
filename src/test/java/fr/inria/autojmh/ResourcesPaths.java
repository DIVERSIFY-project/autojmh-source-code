package fr.inria.autojmh;

import java.net.URISyntaxException;

/**
 * Created by marodrig on 30/12/2015.
 */
public class ResourcesPaths {


    /**
     * Method to find paths in the resource folder of the main sources of AutoJMH
     * @param path Path to find, relative to the resources folder of the main src
     * @return The full path
     */
    public static String getMainPath(String path) {
        try {
            return Thread.currentThread().getContextClassLoader().getResource(path).toURI().getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param obj
     * @param path
     * @return
     */
    public static String getTestPath(Object obj, String path) {
        try {
            return obj.getClass().getResource("/"+path).toURI().getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
