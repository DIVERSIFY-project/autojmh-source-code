package fr.inria.autojmh.selection;

import fr.inria.autojmh.snippets.BenchSnippet;
import spoon.reflect.code.CtStatement;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.support.reflect.declaration.CtClassImpl;

import java.util.*;
import java.util.logging.Logger;

/**
 * Detect all statements tagged with an AutoJMH tagglet.
 * <p/>
 * <p/>
 * Created by marodrig on 28/10/2015.
 */
public class TaggedStatementDetector<E extends CtStatement> extends SnippetSelector<E> {

    private static Logger logger = Logger.getLogger(TaggedStatementDetector.class.getName());

    /**
     * Tagglets separated by the class where they where found
     */
    private Map<String, List<Tagglet>> tagglets;

    /**
     * Closest statement to each tagglet
     */
    private Map<Tagglet, CtStatement> matches;

    /**
     * Indicate if shoudl stop on errors
     */
    private boolean failFirst;

    @Override
    public void process(CtStatement statement) {
        try {

            if ( statement.getParent(CtClassImpl.class) == null ) return;

            String className = statement.getPosition().getCompilationUnit().
                    getMainType().getQualifiedName();

            if (getTagglets().containsKey(className)) {
                for (Tagglet t : tagglets.get(className)) {
                    //Only Bench this are supported right now
                    if (t.getKind() == Tagglet.TaggletKind.BENCH_UNTIL) continue;

                    if (!getMatches().containsKey(t)) getMatches().put(t, statement);
                    else {
                        CtStatement s = getMatches().get(t);
                        int distNew = t.distanceToStatement(statement);
                        int distOld = t.distanceToStatement(s);
                        //Search for the nearest statement appearing after the tagglet
                        if (distOld == distNew && distOld >= 0) {
                            getMatches().put(t, shallower(s, statement));
                        } else if (distOld < 0 || (distNew > 0 && distOld > distNew)) getMatches().put(t, statement);
                    }
                }
            }
        } catch (Exception e) {
            SourcePosition p = statement.getPosition();
            logger.warning("Error while processing statement at "
                    + p.getFile().getAbsolutePath() + ".  Line:" + p.getLine() + ", Col:" + p.getColumn()
                    + " Got: " + e.getMessage());
            if (failFirst) throw new RuntimeException(e);
        }

    }

    /**
     * Returns the shallower with respect to the class
     *
     * @param a
     * @param b
     * @return
     */
    private CtStatement shallower(CtStatement a, CtStatement b) {
        return depth(a, CtClass.class) < depth(b, CtClass.class) ? a : b;
    }

    /**
     * Obtains from two statements the depth  with respect to a parent element in the
     * AST like a class or a method declaration
     *
     * @return
     */
    public int depth(CtStatement a, Class<?> klass) {
        CtElement e = a;
        int aDepth = 0;
        do {
            e = e.getParent();
            aDepth++;
        } while (e != null && !(e.getClass().equals(klass)));
        return aDepth;
    }

    public Map<Tagglet, CtStatement> getMatches() {
        if (matches == null) matches = new HashMap<>();
        return matches;
    }

    public Map<String, List<Tagglet>> getTagglets() {
        return tagglets;
    }

    public void setTagglets(Map<String, List<Tagglet>> tagglets) {
        this.tagglets = tagglets;
    }

    public void setTagglets(List<Tagglet> tagglets) {
        this.tagglets = new HashMap<>();
        for (Tagglet t : tagglets) {
            if (!this.tagglets.containsKey(t.getClassName()))
                this.tagglets.put(t.getClassName(), new ArrayList<Tagglet>());
            this.tagglets.get(t.getClassName()).add(t);
        }
    }

    @Override
    public void processingDone() {
        snippets = new ArrayList<>();
        //Instrument the data context
        for (Map.Entry<Tagglet, CtStatement> e : getMatches().entrySet()) {
            BenchSnippet s = new BenchSnippet();
            s.setASTElement(e.getValue());
            //The Bench snippet will auto resolve its context
            //if the size of the Input access is zero, it cannot be used to avoid DCE, therefore
            //a warning is issued
            if (s.isNeedsInitialization() && s.getTemplateAccessesWrappers().size() == 0)
                logger.warning("Cannot resolve context for " + s.getPosition());
            else {
                snippets.add(s);
                BenchSnippetDetectionData data = new BenchSnippetDetectionData(s);
                notify(SNIPPET_DETECTED, s.getASTElement(), data);
            }
        }
    }

    public boolean isFailFirst() {
        return failFirst;
    }

    public void setFailFirst(boolean failFirst) {
        this.failFirst = failFirst;
    }
}
