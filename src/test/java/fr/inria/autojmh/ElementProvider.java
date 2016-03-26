package fr.inria.autojmh;

import fr.inria.autojmh.selection.SnippetSelector;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.diversify.syringe.SpoonMetaFactory;
import spoon.processing.ProcessingManager;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.support.QueueProcessingManager;

import java.util.List;

/**
 * A class to provide Spoon CtElements to the tests
 *
 * Created by marodrig on 24/03/2016.
 */
public class ElementProvider {

    public static List<BenchSnippet> loadSnippets(Object obj, final String method) throws Exception {
        return loadSnippets(obj, method, CtLoop.class);
    }

    /**
     * Selects from the DataContextPlayGround class located in the resources of the test,
     * the variables of the snippets in the method passed as parameter
     *
     * @param method Method passed as parameter
     * @return
     * @throws Exception
     */
    public static List<BenchSnippet> loadSnippets(Object obj, final String method,
                                                  final Class<?> klass) throws Exception {
        //Process the two files
        final Factory factory = new SpoonMetaFactory().buildNewFactory(
                obj.getClass().getResource(
                        "/testproject/src/main/java/fr/inria/testproject/context").toURI().getPath(), 5);
        ProcessingManager pm = new QueueProcessingManager(factory);
        SnippetSelector<CtStatement> selector = new SnippetSelector<CtStatement>() {
            @Override
            public boolean isToBeProcessed(CtStatement candidate) {
                try {
                    return klass.isAssignableFrom(candidate.getClass());
                } catch (NullPointerException ex) {
                    return false;
                }
            }
            @Override
            public void process(CtStatement element) {
                try {
                    String name = element.getPosition().getCompilationUnit().getMainType().getSimpleName();
                    CtMethod m = element.getParent(CtMethod.class);
                    if (m != null && name.equals("DataContextPlayGround") && m.getSimpleName().equals(method))
                        select(element);
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            }
        };
        pm.addProcessor(selector);
        pm.process();
        return selector.getSnippets();
    }

}
