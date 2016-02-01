package fr.inria.autojmh.tool;

/**
 * An object that can be configurable
 *
 * Created by marodrig on 29/10/2015.
 */
public interface Configurable {

    /**
     * Obtain the data this object needs from the configuration
     *
     * @param configuration
     */
    public void configure(AJMHConfiguration configuration);
}
