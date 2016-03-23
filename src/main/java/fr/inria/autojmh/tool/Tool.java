package fr.inria.autojmh.tool;

import fr.inria.autojmh.generators.AJMHGenerator;

/**
 * Entry point for the AutoJMH tool
 *
 * Created by marodrig on 27/10/2015.
 */
public class Tool {

    public static void main(String[] args) throws Exception {
        AJMHConfiguration configuration = AJMHConfiguration.fromArgs(args);
        AJMHGenerator generator = new AJMHGenerator(configuration);
        generator.generate();
    }

}
