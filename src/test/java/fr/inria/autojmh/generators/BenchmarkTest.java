package fr.inria.autojmh.generators;

import fr.inria.autojmh.ResourcesPaths;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.diversify.syringe.SpoonMetaFactory;
import spoon.processing.AbstractProcessor;
import spoon.processing.ProcessingManager;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.QueueProcessingManager;

import java.net.URISyntaxException;
import java.util.List;

import static fr.inria.autojmh.ResourcesPaths.getTestPath;

/**
 * Created by marodrig on 29/09/2015.
 */
public class BenchmarkTest {

    BenchSnippet snippet;

    protected AJMHConfiguration buildGenerationConf() throws URISyntaxException {
        AJMHConfiguration configuration = new AJMHConfiguration();
        configuration.setWorkingDir(getTestPath(this, "work"));
        configuration.setPackageName("fr.mypackage");
        configuration.setGenerationOutputPath("/output_sources");
        configuration.setTemplatePath(ResourcesPaths.getMainPath("templates"));
        configuration.setGenerationOutputPath("./output");
        return configuration;
    }

    protected BenchSnippet buildSignalLoop() throws Exception {

        snippet = new BenchSnippet();

        //Initialize the CtElements

        SpoonMetaFactory.process(
                this.getClass().getResource("/input_sources").toURI().getPath(),
                new AbstractProcessor<CtLoop>() {
                    @Override
                    public void process(CtLoop element) {
                        if (snippet.getASTElement() != null) return;
                        snippet.setASTElement(element);
                        List<CtVariableAccess> access = snippet.getASTElement().getElements(
                                new TypeFilter<CtVariableAccess>(CtVariableAccess.class));
                        snippet.getAccesses().addAll(access);
                        access.remove(0);//Make not all variables initialized
                        snippet.getInitialized().addAll(access);
                    }
                });
        return snippet;
    }


}
