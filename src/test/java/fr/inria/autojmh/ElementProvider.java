package fr.inria.autojmh;

import fr.inria.autojmh.selection.SnippetSelector;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.diversify.syringe.SpoonMetaFactory;
import spoon.processing.ProcessingManager;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
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
        return loadSnippets(obj, method, "DataContextPlayGround", CtLoop.class);
    }

    public static List<BenchSnippet> loadSnippets(Object obj, final String method, Class<?> klass) throws Exception {
        return loadSnippets(obj, method, "DataContextPlayGround", klass);
    }

    /**
     * Selects from a class located in the resources of the test,
     * the snippets in the method passed as parameter which are in an specific spoon metamodel construction
     *
     * @param method Method where the snippet is located
     * @param className Class where the snippet is located
     * @param klass Structure from the spoon metamodel where the snippets are located
     * @return A list of snippets
     * @throws Exception
     */
    public static List<BenchSnippet> loadSnippets(Object obj, final String method, final String className,
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
                    if (m != null && name.equals(className) && m.getSimpleName().equals(method))
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

    public static BenchSnippet loadFirstSnippets(Object that, String method, Class<?> klass) throws Exception {
        return loadSnippets(that, method, "DataContextPlayGround", klass).get(0);
    }

}
