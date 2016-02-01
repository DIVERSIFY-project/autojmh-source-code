package fr.inria.autojmh.projectbuilders.Maven;

/**
 * A class representing a Maven dependency.
 *
 * We do not use the Maven model(http://Maven.apache.org/ref/3.0.2/Maven-model/apidocs/index.html)
 * because it seems an overkill in this case. We only need couple string fields
 *
 * Created by marodrig on 29/10/2015.
 */
public class MavenDependency {

    private String groupId;

    private String artifactId;

    private String version;

    public MavenDependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }



}
