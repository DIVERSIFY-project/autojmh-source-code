package fr.inria.autojmh.projectbuilders;

import fr.inria.autojmh.tool.Configurable;

import java.io.IOException;

/**
 * Created by marodrig on 29/10/2015.
 */
public interface ProjectBuilder extends Configurable {

    /**
     * Builds the project
     */
    void build() throws IOException;

}
