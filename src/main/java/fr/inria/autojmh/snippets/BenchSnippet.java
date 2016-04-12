package fr.inria.autojmh.snippets;

import fr.inria.autojmh.generators.printer.AJMHPrettyPrinter;
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
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import static fr.inria.autojmh.snippets.modelattrib.MethodAttributes.visibility;
import static fr.inria.autojmh.snippets.modelattrib.VariableAccessAttributes.*;
import static spoon.reflect.declaration.ModifierKind.PUBLIC;

/**
 * Class that represents a snippet extracted from the source code.
 * <p>
 * Created by marodrig on 27/10/2015.
 */
public class BenchSnippet implements Configurable {

    /**
     * Snippet we want to extract data context
     */
    private static Logger log = Logger.getLogger(BenchSnippet.class);

    /**
     * In some very special cases, the microbenchmarks needs no initialization.
     * <p>
     * We set a flag because in those cases, there is no need from data from unit test to exits.
     */
    private boolean needsInitialization = true;


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
     * To achieve that we must (i) see whether "this" is serializable, (ii) this is not serialized already
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

    /**
     * The printer this snippet will use to output itself
     */
    private DefaultJavaPrettyPrinter printer;

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

    public boolean isNeedsInitialization() {
        if (initialized == null) resolveDataContext(astElement);
        return needsInitialization;
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
            if (printer instanceof DefaultJavaPrettyPrinter) {
                var.setPrinter(new DefaultJavaPrettyPrinter(astElement.getFactory().getEnvironment()));
            } else var.setPrinter(new AJMHPrettyPrinter(this));
            result.add(var);
        }
        boolean canSerializeThiz = getPreconditions().checkTypeRef(
                getASTElement().getParent(CtClass.class).getReference());
        if (canSerializeThiz && getMustSerializeThiz()) {
//            astElement.getFactory().Code().createVariableAccess()
            TemplateInputVariable thiz = new TemplateInputVariable();
            thiz.initializeAsThiz(this);
            if (printer instanceof DefaultJavaPrettyPrinter) {
                thiz.setPrinter(new DefaultJavaPrettyPrinter(astElement.getFactory().getEnvironment()));
            } else thiz.setPrinter(new AJMHPrettyPrinter(this));
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
                if (printer == null) return astElement.toString();
                printer.reset();
                printer.scan(astElement);
                return printer.toString();
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
            if (!resolveDataContext(astElement)) accesses = new ArrayList<>();
        return accesses;
    }

    public void setAccesses(List<CtVariableAccess> accesses) {
        this.accesses = accesses;
        this.templateAccessesWrappers = null;
    }

    public HashSet<CtVariableAccess> getInitialized() {
        if (initialized == null) resolveDataContext(astElement);
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

    public boolean meetsPreconditions() {
        if (meetsPreconditions == null) meetsPreconditions = getPreconditions().checkSnippet(this);
        return meetsPreconditions;
    }


    /**
     * Resolves all the variables used by the snippet. The ones which are initialized and the ones which are not
     *
     * @return True if the context was extracted successfully
     */
    private boolean resolveDataContext(CtStatement statement) {

        needsInitialization = false;

        //Check some preconditions needed for the processor to run:
        //All variable access made inside the statement
        List<CtVariableAccess> access = statement.getElements(
                new TypeFilter<CtVariableAccess>(CtVariableAccess.class));

        if (statement.getElements(new TypeFilter<CtThisAccess>(CtThisAccess.class)).size() > 0) {
            mustSerializeThiz = new Preconditions().checkTypeRef(statement.getParent(CtClass.class).getReference());
        }

        initialized = new HashSet<>();

        //Get all THIZ field access from all invocations used in the element
        HashSet<CtInvocation> visited = new HashSet<>();
        Stack<CtInvocation> invStack = new Stack<>();
        invStack.addAll(statement.getElements(
                new TypeFilter<CtInvocation>(CtInvocation.class)));
        while (!invStack.empty()) {
            CtInvocation inv = invStack.pop();
            if (!visited.contains(inv)) {
                visited.add(inv);
                CtBlock b;
                try {
                    b = inv.getExecutable().getDeclaration().getBody();
                } catch (NullPointerException ex) {
                    b = null;
                }
                if (visibility(inv) != PUBLIC && b != null) {
                    for (CtFieldAccess ta : b.getElements(new TypeFilter<CtFieldAccess>(CtFieldAccess.class))) {
                        if (ta.getTarget() instanceof CtThisAccess || ta.getTarget() == null) {
                            access.add(ta);
                            //initialized.add(ta);
                        }
                    }
                    for (CtInvocation i : b.getElements(new TypeFilter<CtInvocation>(CtInvocation.class))) {
                        if (!visited.contains(i)) invStack.push(i);
                    }
                }
            }
        }

        access = cleanRepeatedAccesses(access);
        setAccesses(access);

        //All local variables inside the body
        List<CtLocalVariable> localVars = statement.getElements(
                new TypeFilter<CtLocalVariable>(CtLocalVariable.class));

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

            for (CtVariableAccess a : access) {
                //A variable must be initialized if is local and is not a serializable field
                //Also if is not a constant. Constant get declared in the body of the microbenchmark
                if (isInitialized(a, statement, vars) && !isAConstant(a))
                    initialized.add(a);
            }
        } catch (NotFoundException e) {
            return false;
        } catch (StackOverflowError e) {
            System.out.print(g.toGraphVisText());
            throw e;
        }

        //Remove fields of storable instances
       /* CtVariableAccess thizVarAccess = null;
        //Try first finding the real 'this'
        for (CtVariableAccess thisA : access)
            if (thisA.getVariable().getSimpleName().equals("this")) {
                thizVarAccess = thisA;
            } else if (thisA instanceof CtTargetedAccess &&
                    ((CtTargetedAccess) thisA).getTarget() instanceof CtThisAccess) {
                CtThisAccess thisAccess = (CtThisAccess) ((CtTargetedAccess) thisA).getTarget();
                CodeFactory c = astElement.getFactory().Code();
                CtLocalVariable thizVariable = c.createLocalVariable(
                        astElement.getParent(CtClass.class).getReference(), "THIZ", thisAccess);
                thizVarAccess = c.createVariableAccess(thizVariable.getReference(), false);
            }*/
        needsInitialization = initialized.size() > 0;
        if (access.size() <= 0) return true;

        int i = 0;
        int replaced = 0;
        do {
            if (i == 0) replaced = 0;
            CtVariableAccess a = access.get(i);
            if (canBeReplacedByTarget(a)) {
                CtVariableAccess targetOfA = null;
                CtTargetedAccess ta = (CtTargetedAccess) a;
                if (ta.getTarget() != null) {
                    if (ta.getTarget() instanceof CtVariableAccess) {
                        targetOfA = (CtVariableAccess) ta.getTarget();
                    } else if (ta.getTarget() instanceof CtThisAccess) {
                        //targetOfA = thizVarAccess;
                        mustSerializeThiz = true;
                    }
                } else {
                    mustSerializeThiz = true;
                    /*
                    if (thizVarAccess == null) {
                        //No real 'this' could be found, build one
                        CtCodeSnippetExpression ex = new CtCodeSnippetExpressionImpl();
                        ex.setValue("this");
                        CtLocalVariable thiz = a.getFactory().Code().createLocalVariable(
                                a.getParent(CtClass.class).getReference(), "THIZ", ex);
                        thizVarAccess = a.getFactory().Code().createVariableAccess(thiz.getReference(), false);
                    }
                    targetOfA = thizVarAccess;*/
                }
                if (targetOfA != null) {
                    if (!access.contains(targetOfA)) access.add(targetOfA);
                    if (initialized.contains(a) && !initialized.contains(targetOfA))
                        initialized.add(targetOfA);
                }
                access.remove(a);
                if (initialized.contains(a)) initialized.remove(a);
                replaced++;
            } else i++;
            if (i >= access.size()) i = 0;
        } while (replaced > 0 || i != 0);
        return true;
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
            String signature = "";
            if (a instanceof CtFieldAccess && ((CtFieldAccess) a).getTarget() != null)
                signature = ((CtFieldAccess) a).getTarget().toString();
            /*try {
                signature = a.toString() + a.getVariable().getClass().getSimpleName();
            } catch (NullPointerException ex) {*/
            signature += a.getVariable().getSimpleName() + a.getVariable().getClass().getSimpleName();
            //}
            //String signature = a.toString() + a.getVariable().getClass().getSimpleName();
            if (!varSignatures.contains(signature)) {
                varSignatures.add(signature);
                access.add(a);
            }
        }
        varSignatures.clear();
        return access;
    }


    @Override
    public void configure(AJMHConfiguration configuration) {
        preconditions = configuration.getPreconditions();
    }


    public Preconditions getPreconditions() {
        if (preconditions == null) preconditions = new Preconditions();
        return preconditions;
    }

    /**
     * Sets the printer to be the AJMH printer
     */
    public void setPrinterToAJMH() {
        this.printer = new AJMHPrettyPrinter(this);
        for (TemplateInputVariable v : getTemplateAccessesWrappers()) {
            v.setPrinter(new AJMHPrettyPrinter(this));
        }
    }

    /**
     * Sets the printer to be de default printer
     */
    public void setPrinterToDefault() {
        this.printer = new DefaultJavaPrettyPrinter(astElement.getFactory().getEnvironment());
        for (TemplateInputVariable v : getTemplateAccessesWrappers()) {
            v.setPrinter(new DefaultJavaPrettyPrinter(astElement.getFactory().getEnvironment()));
        }
    }

    public DefaultJavaPrettyPrinter getPrinter() {
        return printer;
    }
}
