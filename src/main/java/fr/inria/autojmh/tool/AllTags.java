package fr.inria.autojmh.tool;

import fr.inria.autojmh.generators.AJMHGenerator;
import fr.inria.autojmh.selection.SnippetSelector;
import spoon.reflect.code.CtLoop;

import java.io.File;

/**
 * Created by marodrig on 05/11/2015.
 */
public class AllTags {

    public static void main(String[] args) throws Exception {
        org.apache.log4j.PropertyConfigurator.configure(
                new File(AllTags.class.getClassLoader().getResource("log4j.properties").toURI().getPath()).getAbsolutePath());
        AJMHConfiguration conf = new AJMHConfiguration();
        conf.setInputProjectPath("C:\\MarcelStuff\\PROJECTS\\PHD\\benchsource");
        conf.setWorkingDir("C:\\MarcelStuff\\PROJECTS\\benchsource_work");
        conf.setGenerationOutputPath("C:\\MarcelStuff\\PROJECTS\\benchsource-benchmark");
        AJMHGenerator gen = new AJMHGenerator();
        gen.configure(conf);
        gen.generate();
    }

}
