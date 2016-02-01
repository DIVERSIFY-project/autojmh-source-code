package fr.inria.autojmh.projectbuilders.Maven;

import fr.inria.autojmh.generators.POMFileGenerator;
import fr.inria.autojmh.projectbuilders.ProjectBuilder;
import fr.inria.autojmh.projectbuilders.ProjectFiles;
import fr.inria.autojmh.tool.AJMHConfiguration;
import org.apache.log4j.Logger;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Builds a Maven project to execute all tests
 * <p/>
 * Created by marodrig on 29/10/2015.
 */
public class MavenProjectBuilder implements ProjectBuilder {

    Logger log = Logger.getLogger(MavenProjectBuilder.class);

    /**
     * Name of the package that is going to be generated
     */
    private String packageName;

    /**
     * Output folder where the package is going to be generated
     */
    private String output;

    /**
     * Path to the output's source
     */
    private String outputSrc;

    /**
     * Path to the output's test
     */
    private String outputTest;

    /**
     * Configuration fot the project builder
     */
    private AJMHConfiguration configuration;

    public POMFileGenerator getPomFileGenerator() {
        return pomFileGenerator;
    }

    public void setPomFileGenerator(POMFileGenerator pomFileGenerator) {
        this.pomFileGenerator = pomFileGenerator;
    }

    private POMFileGenerator pomFileGenerator;

    /**
     * Path to the source pom file
     */
    private String srcPomFile;


    public MavenProjectBuilder(AJMHConfiguration configuration) {
        pomFileGenerator = new POMFileGenerator();
        configure(configuration);
    }

    /**
     * Clears the output path
     *
     * @throws IOException
     */
    public void clearOutputPath() throws IOException {
        File f = new File(output);
        ProjectFiles.removeRecursively(f);
        f.delete();
    }

    public MavenProject loadProject(File pomFile) throws IOException, XmlPullParserException {
        MavenProject ret = null;
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();

        //Removed null and file exists protections that mask errors
        FileReader reader = null;
        reader = new FileReader(pomFile);
        Model model = mavenReader.read(reader);
        model.setPomFile(pomFile);
        ret = new MavenProject(model);
        reader.close();

        return ret;
    }

    @Override
    public void build() throws IOException {

        //Builds the directories
        ProjectFiles.makeIfNotExists(output);
        outputSrc =  ProjectFiles.makeIfNotExists(
                output + "/src/main/java/" + packageName.replace(".", "/")).getAbsolutePath();
        outputTest = ProjectFiles.makeIfNotExists(
                output + "/src/test/java/" + packageName.replace(".", "/")).getAbsolutePath();

        //Put dependencies
        try {
            File mvnFile = new File(srcPomFile);
            if ( !mvnFile.exists() ) log.warn("The source file does not exist");
            else {
                MavenProject mvnPrj = loadProject(mvnFile);
                ArrayList<MavenDependency> deps = new ArrayList<>();
                deps.add(new MavenDependency(mvnPrj.getGroupId(), mvnPrj.getArtifactId(), mvnPrj.getVersion()));
                for (Dependency d : mvnPrj.getDependencies()) {
                    if ( !d.getGroupId().equals("junit") ) //Junit 4.11 is included by default
                     deps.add(new MavenDependency(d.getGroupId(), d.getArtifactId(), d.getVersion()));
                }
                pomFileGenerator.setDependencies(deps);
            }
        } catch (XmlPullParserException e) {
            //Generates the POM file
            pomFileGenerator.setDependencies(new ArrayList<MavenDependency>());
        }
        pomFileGenerator.generate();
    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        this.packageName = configuration.getPackageName();
        this.output = configuration.getGenerationOutputPath();
        this.pomFileGenerator.configure(configuration);
        this.srcPomFile = configuration.getInputProjectPath() + "/pom.xml";
    }

    /**
     * Reconfigures the AJMHConfiguration object so it hold new values
     * @param configuration
     */
    public void reconfigure(AJMHConfiguration configuration) {
        configuration.setGeneratedOutputSrcPath(outputSrc);
        configuration.setGeneratedOutputTestPath(outputTest);
    }
}
