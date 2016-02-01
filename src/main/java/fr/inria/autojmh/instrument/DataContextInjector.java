package fr.inria.autojmh.instrument;

import fr.inria.autojmh.instrument.log.Log;
import fr.inria.autojmh.selection.BenchSnippetDetectionData;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.snippets.TemplateInputVariable;
import fr.inria.diversify.syringe.detectors.DetectionData;
import fr.inria.diversify.syringe.injectors.AbstractInjector;
import org.apache.log4j.Logger;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.support.reflect.code.CtCodeSnippetStatementImpl;
import spoon.support.reflect.code.CtStatementListImpl;

import java.util.HashMap;

/**
 * Instrument the selected statements in order to record the data context
 * <p/>
 * A syringe detector detect interesting parts in the code and then call the injectors to inject code
 * <p/>
 * Created by marodrig on 15/09/2015.
 */
public class DataContextInjector extends AbstractInjector {

    Logger log = Logger.getLogger(DataContextInjector.class);

    public DataContextInjector() {
        setInjectionTemplate(Log.class.getCanonicalName() + ".getLog().log%type%(%var%, \"%name%\", false);\n");
    }

    /**
     * Builds the injection statement
     *
     * @return
     */
    protected CtCodeSnippetStatement buildCode(TemplateInputVariable wrap, BenchSnippet input) {
        HashMap<String, Object> m = new HashMap<>();

        m.put("var", wrap.getInstrumentedCodeCompilableName());
        m.put("signature", wrap.getLoggingSignature());
        m.put("type", wrap.getLogMethodName());

        String inj = buildInjection(m);
        CtCodeSnippetStatement st = new CtCodeSnippetStatementImpl();
        st.setValue(inj);
        return st;
    }


    /**
     * Obtains the name signature of a CtVariableAccess for logging purposes
     *
     * @param a
     * @param loop
     * @return
     */
    private String getSignatureOfVar(CtVariableAccess a, CtStatement loop) {
        SourcePosition pos = loop.getPosition();
        return pos.getCompilationUnit().getMainType().getQualifiedName().replace(".", "-") + "-" + pos.getLine() +
                "-" + a.getVariable().toString();
    }

    @Override
    public void inject(CtElement element, DetectionData data) {
        //Builds the code that is going to be injected in order to record the data context

        if (data instanceof BenchSnippetDetectionData) {
            //Build the before injectors
            BenchSnippet input = ((BenchSnippetDetectionData) data).getSnippet();

            int initializedCount = 0;
            for (TemplateInputVariable wrap : input.getTemplateAccessesWrappers())
                if (wrap.isInitialized()) initializedCount++;

            if (initializedCount <= 0) return;

            //Record before
            setInjectionTemplate(Log.class.getCanonicalName() + ".getLog().log%type%(%var%, \"%signature%\", false)");
            try {
                CtStatementList statements = new CtStatementListImpl();
                for (TemplateInputVariable wrap : input.getTemplateAccessesWrappers()) {
                    if (wrap.isInitialized())
                        statements.addStatement(buildCode(wrap, input));
                }
                try {
                    ((CtStatement) element).insertBefore(statements);
                } catch (IllegalArgumentException e) {
                    log.warn("Unexpected exception while inserting statements before.");
                }

                //Close the file, goes before the after because is done piling up
                CtCodeSnippetStatement st = new CtCodeSnippetStatementImpl();
                st.setValue(Log.class.getCanonicalName() + ".close()");
                ((CtStatement) element).insertAfter(st);

                //Record After
                statements = new CtStatementListImpl();
                setInjectionTemplate(Log.class.getCanonicalName() + ".getLog().log%type%(%var%, \"%signature%\", true)");
                for (int i = 0; i < input.getTemplateAccessesWrappers().size(); i++) {
                    TemplateInputVariable wrap = input.getTemplateAccessesWrappers().get(i);
                    if (wrap.isInitialized())
                        statements.addStatement(buildCode(wrap, input));
                }
                try {
                    ((CtStatement) element).insertAfter(statements);
                } catch (IllegalArgumentException e) {
                    log.warn("Unexpected exception while inserting statements after.");
                }


            } catch (ClassCastException ex1) {
                log.warn("Cannot inject code at " + input.getCode());
            } catch (UnsupportedOperationException ex) {
                log.warn("Cannot inject code at " + input.getCode());
            }
        }
    }
}
