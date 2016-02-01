package fr.inria.autojmh.snippets;

import fr.inria.autojmh.instrument.DataContextResolver;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static fr.inria.autojmh.instrument.DataContextResolver.isSupported;

/**
 * Class that represents a selected snippet to benchmark.
 * <p/>
 * Created by marodrig on 27/10/2015.
 */
public class BenchSnippet {

    /**
     * Indicates whether this snippet contains a dynamic calls to an implicit "this" which is serializable
     * <p/>
     * For example:
     * <p/>
     * if ( slice(i) == null ) return
     * <p/>
     * is the same that
     * <p/>
     * if ( this.slice(i) == null ) return
     * <p/>
     * When extracted, this code must look like:
     * <p/>
     * if ( THIZ.slice(i) == null ) return
     * <p/>
     * To achieve that we must (i) see whether "this" is serializable,
     */
    private Boolean mustSerializeThiz = null;

    /**
     * Code of the snippet being benchmarked.
     *
     * @Note: All properties will sync to the AST element. Values provides will be used only as default when
     * no AST element has been set
     */
    private String code;

    /**
     * Position of the code being benchmarked, in the form <class_qualified_name>:<line_number>,
     * for example: mypackage.MyClassName:89.
     */
    private String position;

    /**
     * Indicate if the variable included in the snippet that has any value before the execution of the snippet;
     */
    HashSet<CtVariableAccess> initialized = new HashSet<CtVariableAccess>();

    /**
     * Variables included inside this snippet.
     */
    List<CtVariableAccess> accesses;

    /**
     * AST element extracted directly from the code.
     * <p/>
     * All other properties will sync to the AST element. Values provides will be used only as default when
     * no AST element has been set
     */
    private CtStatement astElement;

    /**
     * Wrappers for the accesses that will go in the templates
     */
    private List<TemplateInputVariable> templateAccessesWrappers;

    /**
     * Name of the class containing the snippet
     * <p/>
     * All properties will sync to the AST element. Values provides will be used only as default when
     * no AST element has been set
     */
    private String className = "";

    /**
     * Number of the line containing the snippet.
     *
     * @Note: All properties will sync to the AST element. Values provides will be used only as default when
     * no AST element has been set
     */
    private int lineNumber;
    private Boolean meetPreconditions;

    /**
     * Gets the name of the micro benchmark class for this snippet
     *
     * @return
     */
    public String getMicrobenchmarkClassName() {
        return getPosition().replace(".", "_").replace(":", "_");
    }


    /**
     * Type of the benchmark method.
     */
    private String benchMethodReturnType = null;

    public BenchSnippet() {

    }

    public BenchSnippet(CtStatement e) {
        setASTElement(e);
    }

    public String getPosition() {
        if (astElement != null)
            return getClassName() + ":" + getLineNumber();
        return position;
    }


    /**
     * Type of the benchmarks method. This is 'void' by default
     *
     * @return
     */
    public String getBenchMethodReturnType() {
        if (benchMethodReturnType == null) {
            if (astElement != null) {
                List<CtReturn> rets =
                        astElement.getElements(new TypeFilter<CtReturn>(CtReturn.class));
                if (rets != null && rets.size() > 0 && rets.get(0).getReturnedExpression() != null) {
                    CtTypeReference t = rets.get(0).getReturnedExpression().getType();
                    if (t.isAnonymous() || t.getQualifiedName().equals("<nulltype>"))
                        benchMethodReturnType = "Object";
                    else benchMethodReturnType = t.getQualifiedName();
                }
            }
        }

        return benchMethodReturnType == null ? "void" : benchMethodReturnType;
    }

    public void setBenchMethodReturnType(String benchMethodReturnType) {
        this.benchMethodReturnType = benchMethodReturnType;
    }


    /**
     * Build the set of template wrappers for the input variables of the loop
     *
     * @return
     */
    public List<TemplateInputVariable> getTemplateAccessesWrappers() {
        //TODO: Unit test for this
        if (templateAccessesWrappers != null) return templateAccessesWrappers;

        ArrayList<TemplateInputVariable> result = new ArrayList<>();
        for (CtVariableAccess access : getAccesses()) {
            TemplateInputVariable var = new TemplateInputVariable();
            var.initialize(this, access);
            result.add(var);
        }

        boolean canSerializeThiz =
                DataContextResolver.isSupported(getASTElement().getParent(CtClass.class).getReference());
        if (canSerializeThiz && getMustSerializeThiz()) {
//            astElement.getFactory().Code().createVariableAccess()
            TemplateInputVariable thiz = new TemplateInputVariable();
            thiz.initializeAsThiz(this);
            result.add(thiz);
        }
        templateAccessesWrappers = result;
        return result;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCode() {
        if (astElement != null) {
            try {
                return astElement.toString();
            } catch (NullPointerException ex) {
                throw new NullPointerException(
                        "Unable to get the code. Code field was empty and astElement.toString() throw null");
            }
        }
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<CtVariableAccess> getAccesses() {
        if (accesses == null || accesses.size() == 0)
            //This will update the accesses
            if (!(new DataContextResolver().resolve(this)))
                accesses = new ArrayList<>();
        return accesses;
    }

    public void setAccesses(List<CtVariableAccess> accesses) {
        this.accesses = accesses;
        this.templateAccessesWrappers = null;
    }

    public HashSet<CtVariableAccess> getInitialized() {
        if (initialized == null) initialized = new HashSet<>();
        return initialized;
    }

    public void setInitialized(HashSet<CtVariableAccess> initialized) {
        this.initialized = initialized;
    }

    public CtStatement getASTElement() {
        return astElement;
    }

    public void setASTElement(CtStatement astElement) {
        this.mustSerializeThiz = null;
        this.astElement = astElement;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        if (astElement != null)
            className = astElement.getPosition().getCompilationUnit().getMainType().getQualifiedName();
        return className;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        if (astElement != null)
            lineNumber = astElement.getPosition().getLine();
        return lineNumber;
    }

    public Boolean getMustSerializeThiz() {
        if (mustSerializeThiz == null) {
            List<CtInvocation> invocations = astElement.getElements(
                    new TypeFilter<CtInvocation>(CtInvocation.class));
            for (CtInvocation inv : invocations) {
                if (isImplicitThiz(inv)) {
                    mustSerializeThiz = true;
                    return true;
                }
            }
            mustSerializeThiz = false;
        }
        return mustSerializeThiz;
    }

    public static boolean isImplicitThiz(CtInvocation inv) {
        return !inv.getExecutable().isStatic() &&
                (inv.getTarget() == null || inv.getTarget().toString().equals("this")) &&
                isSupported(inv.getExecutable().getDeclaringType());
    }

    public Boolean getMeetPreconditions() {
        return meetPreconditions;
    }

    public void setMeetPreconditions(Boolean meetPreconditions) {
        this.meetPreconditions = meetPreconditions;
    }
}
