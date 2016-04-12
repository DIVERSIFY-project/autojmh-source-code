package fr.inria.autojmh.instrument;

import fr.inria.autojmh.selection.SelectionFileWalker;
import fr.inria.autojmh.selection.Tagglet;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.autojmh.tool.Configurable;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Collects the tagglets
 * <p>
 * Created by marodrig on 09/04/2016.
 */
public class TaggletCollector implements Configurable {

    private static Logger log = Logger.getLogger(TaggletCollector.class.getCanonicalName());

    private Collection<Tagglet> userTagglets;

    private AJMHConfiguration conf;

    public void setTagglets(Collection<Tagglet> tagglets) {
        this.userTagglets = tagglets;
    }

    public Collection<Tagglet> getTagglets() {
        return userTagglets;
    }

    /**
     * Collect all tagglets from the input source
     *
     * @return A Map of taglets ordered by their corresponding class
     */
    public Map<String, List<Tagglet>> collect() {

        log.info("Collecting tagglets");

        if (userTagglets == null) {
            //1. Obtain the tagglets from the code
            SelectionFileWalker walker = new SelectionFileWalker();
            try {
                //Collect in the source dir
                walker.walkDir(conf.getInputProjectPath() + conf.getInputProjectSrcPath());
                //And collect in the test dir as well
                walker.walkDir(conf.getInputProjectPath() + conf.getInputProjectTestPath());
            } catch (IOException e) {
                log.fatal("Could not collect tagglets: " + e.toString());
                return null;
            }
            log.info("Done");
            return walker.getTagglets();
        } else {
            HashMap<String, List<Tagglet>> result = new HashMap<>();
            for (Tagglet t : userTagglets) {
                if (!result.containsKey(t.getClassName()))
                    result.put(t.getClassName(), new ArrayList<Tagglet>());
                result.get(t.getClassName()).add(t);
            }
            return result;
        }
    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        conf = configuration;
    }
}
