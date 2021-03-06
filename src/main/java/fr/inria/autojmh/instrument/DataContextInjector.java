package fr.inria.autojmh.instrument;

import fr.inria.autojmh.instrument.log.Log;
import fr.inria.autojmh.selection.BenchSnippetDetectionData;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.snippets.TemplateInputVariable;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.autojmh.tool.Configurable;
import fr.inria.diversify.syringe.events.DetectionEvent;
import fr.inria.diversify.syringe.injectors.CtParametrizedSnippetStatement;
import fr.inria.diversify.syringe.injectors.GenericInjector;
import org.apache.log4j.Logger;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtCodeSnippetStatementImpl;
import spoon.support.reflect.code.CtStatementListImpl;

import java.util.HashMap;
import java.util.List;

/**
 * Instrument the selected statements in order to record the data context
 * <p/>
 * A syringe detector detect interesting parts in the code and then call the injectors to inject code
 * <p/>
 * Created by marodrig on 15/09/2015.
 */
public class DataContextInjector extends GenericInjector implements Configurable {

    Logger log = Logger.getLogger(DataContextInjector.class);

    private AJMHConfiguration conf;

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
        //String inj = buildInjection(m);
        CtParametrizedSnippetStatement st = new CtParametrizedSnippetStatement();
        st.setParameters(m);
        st.setValue(getInjectionTemplate());
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
    public void listen(DetectionEvent data) {
        //Builds the code that is going to be injected in order to record the data context
        if (data instanceof BenchSnippetDetectionData) {
            CtElement element = data.getDetected();
            //Build the before injectors
            BenchSnippet input = ((BenchSnippetDetectionData) data).getSnippet();

            //Reject snippet if it does not meet preconditions.
            if (!input.meetsPreconditions() || !input.isNeedsInitialization()) return;

            //Obtain return statements to instrument them
            List<CtReturn> returns = element.getElements(new TypeFilter<>(CtReturn.class));

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

                boolean isAReturn = element instanceof CtReturn;
                if (!isAReturn) {
                    ((CtStatement) element).insertAfter(st);
                    //Record After
                    statements = new CtStatementListImpl();
                    CtStatementList afterRetSt = new CtStatementListImpl();
                    setInjectionTemplate(Log.class.getCanonicalName() + ".getLog().log%type%(%var%, \"%signature%\", true)");
                    for (int i = 0; i < input.getTemplateAccessesWrappers().size(); i++) {
                        TemplateInputVariable wrap = input.getTemplateAccessesWrappers().get(i);
                        if (wrap.isInitialized()) {
                            statements.addStatement(buildCode(wrap, input));
                            if (returns != null && returns.size() > 0)
                                afterRetSt.addStatement(buildCode(wrap, input));
                        }
                    }

                    try {
                        ((CtStatement) element).insertAfter(statements);
                        if (returns != null && returns.size() > 0)
                            for (CtReturn r : returns) r.insertBefore(afterRetSt);
                    } catch (IllegalArgumentException e) {
                        log.warn("Unexpected exception while inserting statements after.");
                    }
                } else {
                    ((CtStatement) element).insertBefore(st);
                }


            } catch (ClassCastException ex1) {
                log.warn("Cannot inject code at " + input.getCode());
            } catch (UnsupportedOperationException ex) {
                log.warn("Cannot inject code at " + input.getCode());
            }
        }
    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        conf = configuration;
    }


}
