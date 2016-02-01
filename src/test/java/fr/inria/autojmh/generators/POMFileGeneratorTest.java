package fr.inria.autojmh.generators;

import fr.inria.autojmh.projectbuilders.Maven.MavenDependency;
import fr.inria.autojmh.tool.AJMHConfiguration;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class POMFileGeneratorTest extends BenchmarkTest {

    @Test
    public void testGenerate() throws Exception {

        AJMHConfiguration configuration = buildGenerationConf();

        ArrayList<MavenDependency> dependencies = new ArrayList<>();
        dependencies.add(new MavenDependency("org.depe.ncy", "arti1", "v1"));
        dependencies.add(new MavenDependency("com.compa.ny", "thing2", "20.0.0"));

        POMFileGenerator generator = new POMFileGenerator();
        generator.configure(configuration);
        generator.setWriteToFile(false);
        generator.setDependencies(dependencies);

        generator.generate();

        System.out.println(generator.getOutput());

        assertTrue(generator.getOutput().contains("<groupId>fr.mypackage</groupId>"));
        assertTrue(generator.getOutput().contains("<groupId>org.depe.ncy</groupId>"));
        assertTrue(generator.getOutput().contains("<groupId>com.compa.ny</groupId>"));
        assertTrue(generator.getOutput().contains("<artifactId>arti1</artifactId>"));
        assertTrue(generator.getOutput().contains("<version>20.0.0</version>"));
    }
}