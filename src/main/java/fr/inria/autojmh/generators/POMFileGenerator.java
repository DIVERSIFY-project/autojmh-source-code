package fr.inria.autojmh.generators;

import fr.inria.autojmh.projectbuilders.Maven.MavenDependency;
import fr.inria.autojmh.tool.AJMHConfiguration;

import java.util.HashMap;
import java.util.List;

/**
 * A generator to generate the POM file for the benchmark project
 *
 * Created by marodrig on 29/10/2015.
 */
public class POMFileGenerator extends BaseGenerator {

    /**
     * List of Maven dependencies of the benchmark project
     */
    private List<MavenDependency> dependencies;

    /**
     * Group id of the artifact
     */
    private String groupId;


    @Override
    public void generate() {
        HashMap<String, Object> input = new HashMap<>();
        input.put("groupId", groupId);
        input.put("dependencies", dependencies);
        generateOutput(input, "generated-project-pom.ftl", writeToFile, outputPath + "/pom.xml");
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<MavenDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<MavenDependency> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        super.configure(configuration);
        groupId = configuration.getPackageName();
    }
}
