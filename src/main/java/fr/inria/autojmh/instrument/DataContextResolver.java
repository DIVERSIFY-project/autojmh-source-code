package fr.inria.autojmh.instrument;

import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.controlflow.ControlFlowBuilder;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.NotFoundException;
import fr.inria.dataflow.InitializedVariables;
import org.apache.log4j.Logger;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtNewClassImpl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * This class extract all variables entering and leaving an statement (Data Context)
 * and stores it on the BenchSnippet
 * <p/>
 * It also decides which snippets can be benchmarked!
 * <p/>
 * <p/>
 * Created by marodrig on 30/10/2015.
 */
public class DataContextResolver {

    /**
     * Snippet we want to extract data context
     */
    //private BenchSnippet snippet;

    private static Logger log = Logger.getLogger(DataContextResolver.class);


    /**
     * Preprares the data needed for build a microbenchark of the statement.
     *
     * @return True if the context was extracted successfully
     */
    public boolean resolve(BenchSnippet inputs) {

        //Check some preconditions needed for the processor to run:
        //All invocations within the body are statics
        if (!checkPreconditions(inputs)) {
            log.warn("Snippet at " + inputs.getPosition() + " does not meet preconditions");
            return false;
        }


        CtStatement statement = inputs.getASTElement();

        //All variable access made inside the statement
        List<CtVariableAccess> access = statement.getElements(
                new TypeFilter<CtVariableAccess>(CtVariableAccess.class));
        access = cleanRepeatedAccesses(access);

        HashSet<CtVariableAccess> inits = inputs.getInitialized();
        for (CtVariableAccess a : access)
            if (isInitialized(a, statement)) inits.add(a);
        //Check that all initialized variables are actually supported for extraction
        if (!allVariablesAreSupported(inits)) {
            //If not meet, exit gracefully
            inputs.getInitialized().clear();
            inputs.setAccesses(new ArrayList<CtVariableAccess>());
            return false;
        }
        inputs.setAccesses(access);

        //All local variables inside the body
        List<CtLocalVariable> localVars =
                statement.getElements(new TypeFilter<CtLocalVariable>(CtLocalVariable.class));

        //Build the injection of each variable
        //Add to the injection only initialized and Non-local variable
        for (CtVariableAccess a : access) {
            if (inputs.getInitialized().contains(a) && isLocalVariable(a, localVars)) {
                inputs.getInitialized().remove(a);
            }
        }
        return true;
    }

    /**
     * Indicate if a variable access is initialized before the loop
     *
     * @param a
     * @param statement
     * @return
     */
    private boolean isInitialized(CtVariableAccess a, CtStatement statement) {

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

        //General case
        ControlFlowBuilder v = new ControlFlowBuilder();
        CtMethod m = a.getParent(CtMethod.class);
        if (m == null) return false;
        m.accept(v);
        ControlFlowGraph g = v.getResult();
        g.simplify();
        try {
            InitializedVariables vars = new InitializedVariables();
            vars.run(ControlFlowBuilder.firstNode(g, statement));
            return vars.getInitialized().contains(a.getVariable());
        } catch (NotFoundException e) {
            return false;
        } catch (StackOverflowError e) {
            System.out.print(g.toGraphVisText());
            throw e;
        }
    }

    /**
     * One var can be pointed by multiples accesses. This method leaves only one access per variable in the list
     *
     * @return
     */
    private List<CtVariableAccess> cleanRepeatedAccesses(List<CtVariableAccess> allAccess) {
        //Have only one access per variable
        HashSet<String> varSignatures = new HashSet<>();
        List<CtVariableAccess> access = new ArrayList<CtVariableAccess>();
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
     * Indicate whether 'a' is a variable declared inside the statement being benchmarked
     *
     * @param a
     * @param localVars
     * @return
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

    /**
     * Indicates whether all variables are PRIMITIVES or not
     *
     * @param access
     * @return
     */
    /*
    private List<CtVariableAccess> removeNonSupported(List<CtVariableAccess> access) {
        //Verify they are of type primitive or array
        List<CtVariableAccess> result = new ArrayList<>();
        for (CtVariableAccess a : access) {
            if (isSupported(a.getVariable().getType())) {
                result.add(a);
            }
        }
        return result;
    }*/

    /**
     * Indicates if the type is supported for extraction
     *
     * @param ref
     * @return
     */
    public static boolean isSupported(CtTypeReference ref) {
        if (ref == null) return false;

        if (ref.getDeclaration() != null && ref.getDeclaration().getVisibility() != null) {
            if (ref.getDeclaration().getVisibility() == ModifierKind.PROTECTED ||
                    ref.getDeclaration().getVisibility() == ModifierKind.PRIVATE) return false;
        }

        if (ref instanceof CtArrayTypeReference) {
            ref = ((CtArrayTypeReference) ref).getComponentType();
            return isSupported(ref);
        } else if (isCollection(ref)) {
            for (CtTypeReference args : ref.getActualTypeArguments()) {
                if (!isSupported(args)) return false;
            }
            return true;
        } else return ref.isPrimitive() || isSerializable(ref) || isClassPrimitive(ref);
    }

    private static boolean isClassPrimitive(CtTypeReference ref) {
        return ref.getQualifiedName().equals("java.lang.Byte") ||
                ref.getQualifiedName().equals("java.lang.Boolean") ||
                ref.getQualifiedName().equals("java.lang.Character") ||
                ref.getQualifiedName().equals("java.lang.Double") ||
                ref.getQualifiedName().equals("java.lang.Float") ||
                ref.getQualifiedName().equals("java.lang.Integer") ||
                ref.getQualifiedName().equals("java.lang.Long") ||
                ref.getQualifiedName().equals("java.lang.Number") ||
                ref.getQualifiedName().equals("java.lang.Short") ||
                ref.getQualifiedName().equals("java.lang.String");
    }

    /**
     * Indicates if the type is a collection
     *
     * @param ref
     * @return
     */
    public static boolean isCollection(CtTypeReference ref) {
        Set<CtTypeReference<?>> refs = ref.getSuperInterfaces();
        if (refs == null) return false;
        for (CtTypeReference r : refs) {
            if (r.getQualifiedName().equals("java.util.Collection") ||
                    isCollection(r)) {
                return true;
            }
        }
        return false;
    }

    //public CtTypeRefere

    /**
     * Indicates if the type is serializable
     *
     * @param componentType
     * @return
     */
    public static boolean isSerializable(CtTypeReference componentType) {
        HashSet<CtTypeReference> refs = new HashSet<>();
        if (componentType.getSuperInterfaces() != null) refs.addAll(componentType.getSuperInterfaces());
        CtTypeReference superRef = componentType.getSuperclass();
        if (superRef != null) refs.add(superRef);
        if (refs.size() == 0) return false;
        for (CtTypeReference ref : refs) {
            if (ref.getQualifiedName().equals("java.io.Serializable") ||
                    isSerializable(ref)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates whether all variables are PRIMITIVES or not
     *
     * @param access
     * @return
     */
    private boolean allVariablesAreSupported(Collection<CtVariableAccess> access) {
        //Verify they are of type primitive or array
        for (CtVariableAccess a : access) {
            if (a.getVariable().getType() == null || !isSupported(a.getVariable().getType())) return false;
        }
        return true;
    }

    /**
     * Indicate if the variable access is a field of type primitive(int, float, byte, etc) array
     *
     * @param a
     * @return
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
     * Check that all variables are primitive types, their classes counterpart, serializables
     * or collections of the above.
     *
     * @param snippet
     * @return
     */
    private static boolean containsUnsuportedVars(BenchSnippet snippet) {
        List<CtVariableAccess> accesses = snippet.getASTElement().getElements(
                new TypeFilter<CtVariableAccess>(CtVariableAccess.class));
        try {
            for (CtVariableAccess a : accesses)
                if (!DataContextResolver.isSupported(a.getVariable().getType()))
                    return true;
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    /**
     * Indicate whether we can extract the segment into a microbenchmark or not.
     * <p/>
     *
     * @param snippet
     * @return
     */
    public static boolean checkPreconditions(BenchSnippet snippet) {
        if (snippet.getMeetPreconditions() == null) {
            Boolean preconditions = snippet.getASTElement() != null &&
                    !containsUnsuportedVars(snippet) &&
                    !containsUnsupportedDynamicInvocations(snippet.getASTElement(), 3);
            snippet.setMeetPreconditions(preconditions);
            return preconditions;
        } else return snippet.getMeetPreconditions();
    }

    protected boolean testme;

    /**
     * Checks whether two elements are from the same package
     *
     * @param e1
     * @param t
     * @return
     */
    private static boolean samePackage(CtElement e1, CtTypeReference t) {
        String s1 = e1.getPosition().getCompilationUnit().getMainType().getPackage().getQualifiedName();
        String s2 = t.getQualifiedName();
        return s1.equals(s2);
    }

    /**
     * Indicate if the element contains dynamic invocations to the nth level.
     * <p/>
     * Dynamic invocations of collections are supported
     *
     * @param element Invocation to inspect
     * @param levels  Levels to explore
     * @return False if:
     * <p/>
     * 1. Is a non public dynamic method
     * 2. Belongs to a non supported variable
     */
    private static boolean containsUnsupportedDynamicInvocations(CtElement element, int levels) {
        if (levels <= 0) return true;
        levels--;

        List<CtNewClassImpl> newClasses = element.getElements(
                new TypeFilter<CtNewClassImpl>(CtNewClassImpl.class));
        for (CtNewClassImpl n : newClasses) {
            try {
                Method m = n.getExecutable().getActualMethod();
                if (m != null && n.getAnonymousClass() != null && //An anonymous class is extraible
                        !Modifier.isPublic(m.getModifiers())) return true;
                    //If we can't get the method and they belong to the same package then its accessible
                else if (samePackage(element, n.getExecutable().getDeclaringType())) return true;
            } catch (Exception ex) {
                //If we can't get the method and they belong to the same package then its accessible
                if (samePackage(element, n.getExecutable().getDeclaringType())) return true;
            }
        }

        List<CtInvocation> invocations = element.getElements(
                new TypeFilter<CtInvocation>(CtInvocation.class));

        for (CtInvocation inv : invocations) {
            //Static methods are supported whether they are private or not
            if (inv.getExecutable().isStatic()) continue;

            //Handle some special weird cases Spoon is giving me
            if (inv.toString().equals("super()") &&
                    inv.getExecutable().toString().equals("java.lang.Object.Object")) continue;


            boolean isPublicDynamic = false;
            try {
                isPublicDynamic = Modifier.isPublic(inv.getExecutable().getActualMethod().getModifiers());
            } catch (Exception ex) {
                if (inv.getExecutable().getDeclaringType().isInterface()) isPublicDynamic = true;
                else if (!samePackage(element, inv.getExecutable().getDeclaringType())) isPublicDynamic = true;
                else isPublicDynamic = false;
            }
            //private dynamic methods are not currently supported
            if (isPublicDynamic) {
                try {
                    //Check the special case in which the target is "this"
                    if (inv.getTarget() == null) {
                        if (!isSupported(
                                element.getPosition().getCompilationUnit().getMainType().getReference())) {
                            return true;
                            //If called by another invocation, it will depend on whether the other is supported or not
                        } else if (inv.getTarget() instanceof CtInvocation) continue;
                    } else if (!isSupported(inv.getTarget().getType())) return true;
                    //if (containsUnsupportedDynamicInvocations(inv, levels - 1)) return true;
                } catch (NullPointerException ex) {
                    log.warn("Unable to find if element \"" + element.toString() + "\" contains unsupported methods");
                    return true;
                }
            } else return true;

            //Private methods are only supported when they don't contain other private dynamic or
            //unsuported variables inside
            /*
            if (!isPublicDynamic) {
                return containsUnsupportedDynamicInvocations(inv, levels - 1);
            }*/
        }
        return false;
    }

    /*
    public BenchSnippet getSnippet() {
        return snippet;
    }

    public void setSnippet(BenchSnippet snippet) {
        this.snippet = snippet;
    }*/
}
