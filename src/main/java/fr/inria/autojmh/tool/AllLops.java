package fr.inria.autojmh.tool;

import fr.inria.autojmh.generators.AJMHGenerator;
import fr.inria.autojmh.selection.SnippetSelector;
import spoon.reflect.code.CtLoop;

import java.io.File;

/**
 * Created by marodrig on 05/11/2015.
 */
public class AllLops {

    public static void main(String[] args) throws Exception {

        org.apache.log4j.PropertyConfigurator.configure(
                new File(AllLops.class.getClassLoader().getResource("log4j.properties").toURI().getPath()).getAbsolutePath());


        AJMHConfiguration conf = new AJMHConfiguration();

        conf.setInputProjectPath("C:\\MarcelStuff\\DATA\\DIVERSE\\input_programs\\commons-io-trunk\\commons-io-trunk");

        conf.setInputProjectPath("C:\\MarcelStuff\\PROJECTS\\vectorz-develop");
 /*       conf.setWorkingDir("C:\\MarcelStuff\\PROJECTS\\working");
        conf.setGenerationOutputPath("C:\\MarcelStuff\\PROJECTS\\vectorz-benchmark");
*/
/*
        conf.setInputProjectPath("C:\\MarcelStuff\\DATA\\DIVERSE\\input_programs\\LANG_3_3_2");
        conf.setWorkingDir("C:\\MarcelStuff\\DATA\\DIVERSE\\input_programs\\LANG_3_3_2-work");
        conf.setGenerationOutputPath("C:\\MarcelStuff\\DATA\\DIVERSE\\input_programs\\LANG_3_3_2-bench");/**/
/*

        conf.setWorkingDir("C:\\MarcelStuff\\DATA\\DIVERSE\\input_programs\\commons-io-trunk-work");
        conf.setGenerationOutputPath("C:\\MarcelStuff\\DATA\\DIVERSE\\input_programs\\commons-io-trunk-bench");
*/
        /*
        conf.setInputProjectPath("C:\\MarcelStuff\\DATA\\DIVERSE\\input_programs\\commons-codec-trunk");
        conf.setWorkingDir("C:\\MarcelStuff\\DATA\\DIVERSE\\input_programs\\commons-codec-work");
        conf.setGenerationOutputPath("C:\\MarcelStuff\\DATA\\DIVERSE\\input_programs\\commons-codec-bench");
        */
/*
        conf.setInputProjectPath("C:\\MarcelStuff\\PROJECTS\\DIVERSE\\jsyn-master");
        conf.setWorkingDir("C:\\MarcelStuff\\PROJECTS\\working");
        conf.setGenerationOutputPath("C:\\MarcelStuff\\PROJECTS\\jsyn-benchmark");



/*
        conf.setInputProjectPath("C:\\MarcelStuff\\PROJECTS\\PHD\\benchsource");
        conf.setWorkingDir("C:\\MarcelStuff\\PROJECTS\\benchsource_work");
        conf.setGenerationOutputPath("C:\\MarcelStuff\\PROJECTS\\benchsource-benchmark");
*/
        conf.setInputProjectPath("C:\\MarcelStuff\\DATA\\DIVERSE\\input_programs\\commons-cli-trunk");
        conf.setWorkingDir("C:\\MarcelStuff\\DATA\\DIVERSE\\input_programs\\commons-cli-trunk_work");
        conf.setGenerationOutputPath("C:\\MarcelStuff\\DATA\\DIVERSE\\input_programs\\commons-cli-trunk-benchmark");

        final int[] i = {0};
        AJMHGenerator gen = new AJMHGenerator();
        gen.configure(conf);
        try {
            gen.setSelector(new SnippetSelector<CtLoop>() {
                @Override
                public void process(CtLoop loop) {
                    if (loop.getBody() != null)
                        selectIfMeetPreconditions(loop);
                    i[0]++;
                }
            });
            gen.generate();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
            throw e;
        }
        System.out.println("Elements detected: " + i[0]);
    }

}
