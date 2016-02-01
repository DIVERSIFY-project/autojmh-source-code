package fr.inria.autojmh.instrument;

import fr.inria.autojmh.selection.BenchSnippetDetectionData;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.diversify.syringe.SpoonMetaFactory;
import org.junit.Test;
import spoon.processing.AbstractProcessor;
import spoon.processing.ProcessingManager;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.factory.Factory;
import spoon.support.QueueProcessingManager;
import spoon.support.reflect.code.CtCodeSnippetStatementImpl;

import static junit.framework.Assert.assertTrue;

public class DataContextInjectorTest {

    @Test
    public void testInject() throws Exception {

        //Obtain a CtElement to play with
        final CtStatement[] s = new CtStatement[1];
        s[0] = null;
        Factory factory = new SpoonMetaFactory().buildNewFactory(
                this.getClass().getResource("/input_sources").toURI().getPath(), 5);
        ProcessingManager pm = new QueueProcessingManager(factory);
        pm.addProcessor(new AbstractProcessor<CtIf>() {
            @Override
            public void process(CtIf element) {
                if (s[0]!= null) return;
                s[0] = element;
            }
        });
        pm.process();

        //Build a detection data for this injector
        BenchSnippetDetectionData dd = new BenchSnippetDetectionData();
        dd.setSnippet(new BenchSnippet());
        dd.getSnippet().setASTElement(s[0]);

        //Finally inject
        DataContextInjector injector = new DataContextInjector();
        injector.inject(s[0], dd);

        //Assert that two statements where inserted before and after the if
        CtBlock parent = (CtBlock) s[0].getParent();
        int indexIf = parent.getStatements().indexOf(s[0]);
        assertTrue(parent.getStatements().get(indexIf - 1) instanceof CtCodeSnippetStatementImpl);
        assertTrue(parent.getStatements().get(indexIf + 3) instanceof CtCodeSnippetStatementImpl);
        assertTrue(((CtCodeSnippetStatementImpl)parent.getStatement(indexIf+3)).getValue().contains(".close()"));
    }


}