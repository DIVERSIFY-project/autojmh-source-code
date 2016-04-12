package fr.inria.autojmh.tool;

import fr.inria.autojmh.instrument.TaggletCollector;

/**
 * Explores a project to determine the number of snippets that can be automatically extracted
 *
 * Created by marodrig on 09/04/2016.
 */
public class PreconditionExplorer implements Configurable {

    private AJMHConfiguration conf;

    public void explore() {
        TaggletCollector collector = new TaggletCollector();


    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        this.conf = configuration;
    }
}
