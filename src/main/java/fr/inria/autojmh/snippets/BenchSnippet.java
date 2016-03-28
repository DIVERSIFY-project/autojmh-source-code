package fr.inria.autojmh.snippets;

import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.autojmh.tool.Configurable;
import fr.inria.controlflow.ControlFlowBuilder;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.NotFoundException;
import fr.inria.dataflow.InitializedVariables;
import org.apache.log4j.Logger;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Class that represents a selected snippet to benchmark.
 * <p>
 * Created by marodrig on 27/10/2015.
 */
public class BenchSnippet implements Configurable {

    /**
     * Snippet we want to extract data context
     */
    private static Logger log = Logger.getLogger(BenchSnippet.class);


    /**
     * Indicates whether this snippet contains a dynamic calls to an implicit "this" which is serializable
     * <p>
     * For example:
     * <p>
     * if ( slice(i) == null ) return
     * <p>
     * is the same that
     * <p>
     * if ( this.slice(i) == null ) return
     * <p>
     * When extracted, this code must look like:
     * <p>
     * if ( THIZ.slice(i) == null ) return
     * <p>
     * To achieve that we must (i) see whether "this" is serializable,
     */
    private Boolean mustSerializeThiz = null;

    /**
     * Code of the snippet being benchmarked.
     * <p>
     * Note: All properties will sync to the AST element. Values provides will be used only as default when
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
    HashSet<CtVariableAccess> initialized = null;

    /**
     * Variables included inside this snippet.
     */
    List<CtVariableAccess> accesses;

    /**
     * AST element extracted directly from the code.
     * <p>
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
     * <p>
     * All properties will sync to the AST element. Values provides will be used only as default when
     * no AST element has been set
     */
    private String className = "";

    /**
     * Number of the line containing the snippet.
     * <p>
     * Note: All properties will sync to the AST element. Values provides will be used only as default when
     * no AST element has been set
     */
    private int lineNumber;

    private Preconditions preconditions;

    //public Preconditions getPreconditions() { return preconditions; }

    public void setPreconditions(Preconditions preconditions) {
        this.preconditions = preconditions;
    }

    //Indicates if the snippet meets the preconditions
    private Boolean meetsPreconditions = null;

    /**
     * Gets the name of the micro benchmark class for this snippet
     */
    public String getMicrobenchmarkClassName() {
        return getPosition().replace(".", "_").replace(":", "_");
    }


    /**
     * Type of the benchmark method.
     */
    private String benchMethodReturnType = null;

    /**
     * Gets the source code position of for the element this snippet wraps
     *
     * @return A Spoon like position string
     */
    public String getPosition() {
        if (astElement != null) position = getClassName() + ":" + getLineNumber();
        return position;
    }


    /**
     * Type of the benchmarks method for this snippet. This is 'void' by default.
     * <p>
     * The benchmark method type must be the same that the one
     */
    public String getBenchMethodReturnType() {
        if (benchMethodReturnType == null) {
            if (astElement != null) {
                List<CtReturn> rets =
                        astElement.getElements(new TypeFilter<CtReturn>(CtReturn.class));
                if (rets != null && rets.size() > 0 && rets.get(0).getReturnedExpression() != null) {
                    CtTypeReference t = rets.get(0).getReturnedExpression().getType();
                    if (t.isAnonymous() || t.getQualifiedName().equals("<nulltype>")) benchMethodReturnType = "Object";
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
     * Some extra information is needed per variable inside the snippet to generate the parts. This
     * information is wrapped in an extra
     * <p>
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
        boolean canSerializeThiz = getPreconditions().checkTypeRef(
                getASTElement().getParent(CtClass.class).getReference());
        if (canSerializeThiz && getMustSerializeThiz()) {
//            astElement.getFactory().Code().createVariableAccess()
            TemplateInputVariable thiz = new TemplateInputVariable();
            thiz.initializeAsThiz(this);
            result.add(thiz);
        }
        templateAccessesWrappers = result;
        return result;
    }

    /*
    public void setPosition(String position) {
        this.position = position;
    }*/

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

    /**
     * Get the variable accesses inside the snippet.
     */
    public List<CtVariableAccess> getAccesses() {
        if (accesses == null || accesses.size() == 0)
            if (!resolveDataContext()) accesses = new ArrayList<>();
        return accesses;
    }

    public void setAccesses(List<CtVariableAccess> accesses) {
        this.accesses = accesses;
        this.templateAccessesWrappers = null;
    }

    public HashSet<CtVariableAccess> getInitialized() {
        if (initialized == null) resolveDataContext();
        return initialized;
    }

    public CtStatement getASTElement() {
        return astElement;
    }

    public void setASTElement(CtStatement astElement) {
        this.mustSerializeThiz = null;
        this.astElement = astElement;
    }

    public String getClassName() {
        if (astElement != null)
            className = astElement.getPosition().getCompilationUnit().getMainType().getQualifiedName();
        return className;
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

    public boolean isImplicitThiz(CtInvocation inv) {
        return !inv.getExecutable().isStatic() &&
                (inv.getTarget() == null || inv.getTarget().toString().equals("this")) &&
                preconditions.checkTypeRef(inv.getExecutable().getDeclaringType());
    }

    public boolean meetsPreconditions() {
        if (meetsPreconditions == null) meetsPreconditions = getPreconditions().checkSnippet(this);
        return meetsPreconditions;
    }


    /**
     * Resolves all the variables used by the snippet. The ones which are initialized and the ones which are not
     *
     * @return True if the context was extracted successfully
     */
    private boolean resolveDataContext() {
        //Check some preconditions needed for the processor to run:
        CtStatement statement = getASTElement();

        //All variable access made inside the statement
        List<CtVariableAccess> access = statement.getElements(
                new TypeFilter<CtVariableAccess>(CtVariableAccess.class));
        access = cleanRepeatedAccesses(access);
        setAccesses(access);

        //All local variables inside the body
        List<CtLocalVariable> localVars =
                statement.getElements(new TypeFilter<CtLocalVariable>(CtLocalVariable.class));

        //Find initialized variables of allowed types
        ControlFlowBuilder v = new ControlFlowBuilder();
        CtMethod m = statement.getParent(CtMethod.class);
        if (m == null) return false;
        m.accept(v);
        ControlFlowGraph g = v.getResult();
        g.simplify();
        try {
            InitializedVariables vars = new InitializedVariables();
            vars.run(ControlFlowBuilder.firstNode(g, statement));
            initialized = new HashSet<>();
            for (CtVariableAccess a : access) {
                if (isInitialized(a, statement, vars))
                    initialized.add(a);
            }
        } catch (NotFoundException e) {
            return false;
        } catch (StackOverflowError e) {
            System.out.print(g.toGraphVisText());
            throw e;
        }

        //Build the injection of each variable
        //Add to the injection only initialized and Non-local variable
        for (CtVariableAccess a : access) {
            if (this.initialized.contains(a) && isLocalVariable(a, localVars)) {
                this.initialized.remove(a);
            }
        }
        return true;
    }


    /**
     * Indicate if a variable access is initialized before the statement
     */
    private boolean isInitialized(CtVariableAccess a, CtStatement statement, InitializedVariables vars) {

        //Discard all variables being declared inside the loop expression
        //TODO: review this, sometimes the declaration is null
        try {
            List<CtVariable> vs = statement.getElements(new TypeFilter<CtVariable>(CtVariable.class));
            if (a.getVariable().getDeclaration() != null) {
                //Special cases
                if (vs.contains(a.getVariable().getDeclaration())) return false;
                if ((a.getVariable().getDeclaration() != null &&
                        a.getVariable().getDeclaration().getDefaultExpression() != null)
                        || !(a.getVariable() instanceof CtLocalVariableReference)) return true;
            }
        } catch (IllegalStateException ex) {
            log.warn("Unable to evaluate the initialization special case for " + a);
        }


        return vars.getInitialized().contains(a.getVariable());
    }


    /**
     * One var can be pointed by multiples accesses. This method leaves only one access per variable in the list
     */
    private List<CtVariableAccess> cleanRepeatedAccesses(List<CtVariableAccess> allAccess) {
        //Have only one access per variable
        HashSet<String> varSignatures = new HashSet<>();
        List<CtVariableAccess> access = new ArrayList<>();
        for (CtVariableAccess a : allAccess) {
            if (isFieldOfPrimitiveArray(a)) continue;
            String signature = a.getVariable().getSimpleName() + a.getVariable().getClass().getSimpleName();
            if (!varSignatures.contains(signature)) {
                varSignatures.add(signature);
                access.add(a);
            }
        }
        varSignatures.clear();
        return access;
    }

    /**
     * Indicate if the variable access is a field of type primitive(int, float, byte, etc) array
     */
    private boolean isFieldOfPrimitiveArray(CtVariableAccess a) {
        boolean result = false;
        if (a instanceof CtFieldAccess) {
            CtFieldAccess field = (CtFieldAccess) a;
            result = field.getTarget() instanceof CtVariableAccess;
            result = result && ((CtVariableAccess) field.getTarget()).getVariable().getType() instanceof CtArrayTypeReference;
            if (result) {
                CtArrayTypeReference arrayRef =
                        (CtArrayTypeReference) ((CtVariableAccess) field.getTarget()).getVariable().getType();
                result = arrayRef.getComponentType().isPrimitive();
            }
        }
        return result;
    }


    /**
     * Indicate whether 'a' is a variable declared inside the statement being benchmarked
     */
    private boolean isLocalVariable(CtVariableAccess a, List<CtLocalVariable> localVars) {
        for (CtLocalVariable lv : localVars) {
            if (lv.getReference().equals(a.getVariable())) {
                return true;
            } else if (a instanceof CtFieldAccess) {
                //Fields of local variables
                CtFieldAccess fd = (CtFieldAccess) a;
                if (fd.getTarget() != null && fd.getTarget() instanceof CtVariableAccess &&
                        lv.getReference().equals(((CtVariableAccess) fd.getTarget()).getVariable())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        preconditions = configuration.getPreconditions();
    }


    public Preconditions getPreconditions() {
        if (preconditions == null) preconditions = new Preconditions();
        return preconditions;
    }
}
