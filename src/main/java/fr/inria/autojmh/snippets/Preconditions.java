package fr.inria.autojmh.snippets;

import fr.inria.autojmh.snippets.modelattrib.MethodAttributes;
import fr.inria.autojmh.snippets.modelattrib.TypeAttributes;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.autojmh.tool.Configurable;
import org.apache.log4j.Logger;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtNewClassImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * A class that holds the code to enforce the preconditions needed for a snippet in order to be selected for an
 * automatic microbenchmark
 * <p>
 * Created by marodrig on 22/03/2016.
 */
public class Preconditions implements Configurable {

    private static final String LEVELS_TOO_DEEP = "Levels too deep";
    private static final String TYPE_IS_NOT_PUBLIC = "Type is not public";
    private static final String TYPE_IS_NOT_STORABLE = "Type is not storable (primitive, class primitive, serializable)";
    private static final String UNEXPECTED_ERR = "Unexpected error";
    private static final String VARS_UNSUPPORTED = "Variables unsupported";
    private static final String DYN_INV_UNSUPPORTED = "Dynamic invocations unsupported";
    private static final String PRIVATE_CONSTRUCTOR = "Private constructor";
    //private static final String TARGET_TYPE_UNSUPPORTED = "Target type unsupported";
    private static final String PRIVATE_DYNAMIC_METHOD = "Private dynamic method";
    private static final String UNEXPECTED_ERR_DYN_METHOD = "Unexpected error while inspecting dynamic method";
    private static final String UNEXPECTED_ERR_CONSTRUCTOR = "Unexpected error while inspecting constructor";
    private static final String DYN_METHOD_TARGET_TYPE_UNSUPPORTED = "Dynamic method's target type unssuported";
    private static final String COLLECTION_OF_UNSUPPORTED_TYPE = "Collection of unsupported type";
    private static final String ERR_GET_BODY = "Error getting body of invocation";
    private static final String CANNOT_DETERMINE_VISIBILITY = "Cannot determine method's visibility";

    private static Logger log = Logger.getLogger(Preconditions.class);
    //Quantify the causes of rejection
    private HashMap<String, Integer> rejectionCause;
    //Global configurations
    private AJMHConfiguration conf;


    public Preconditions() {
        rejectionCause = new HashMap<>();
        rejectionCause.put(VARS_UNSUPPORTED, 0);
        rejectionCause.put(DYN_INV_UNSUPPORTED, 0);
    }

    /**
     * A Hash map containing the main rejection causes for the snippets.
     * <p>
     * Will allow us to better drive our efforts to develop strategies to smooth these preconditions
     */
    public HashMap<String, Integer> getRejectionCause() {
        if (rejectionCause == null) rejectionCause = new HashMap<>();
        return rejectionCause;
    }

    /**
     * A method that increments the cause of rejection and return always false as syntactic sugar, since the following
     * pattern is very common:
     * ---- incCause(cause)
     * ---- return false;
     *
     * @param cause Cause of rejectuion
     * @return
     */
    private boolean incCause(String cause) {
        if (!rejectionCause.containsKey(cause)) {
            rejectionCause.put(cause, 1);
            return false;
        }
        int k = rejectionCause.get(cause) + 1;
        rejectionCause.put(cause, k);
        return false;
    }

    /**
     * Indicate whether we can extract the segment into a microbenchmark or not.
     * An snippet must meet certain preconditions.
     */
    public boolean checkSnippet(BenchSnippet snippet) {
        int depth = conf == null ? 5 : conf.getMethodExtractionDepth();
        if (snippet.getASTElement() == null) return incCause(UNEXPECTED_ERR);
        if (!allVariablesAreSupported(snippet)) return incCause(VARS_UNSUPPORTED);
        if (containsUnsupportedDynamicInvocations(snippet.getASTElement(), depth))
            return incCause(DYN_INV_UNSUPPORTED);
        return true;
    }

    /**
     * Check that all variables are primitive types, their classes counterpart, serializables
     * or collections of the above.
     *
     * @param snippet Snippet to check
     * @return
     */
    private boolean allVariablesAreSupported(BenchSnippet snippet) {
        HashSet<CtVariableAccess> accesses = snippet.getInitialized();
        try {
            for (CtVariableAccess a : accesses)
                if (a.getVariable().getType() == null || !checkTypeRef(a.getVariable().getType()))
                    return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Indicates if the type is supported for extraction
     *
     * @param ref
     * @return
     */
    public boolean checkTypeRef(CtTypeReference ref) {
        if (ref == null) return false;

        if (ref.getDeclaration() != null && ref.getDeclaration().getVisibility() != null) {
            if (ref.getDeclaration().getVisibility() == ModifierKind.PROTECTED ||
                    ref.getDeclaration().getVisibility() == ModifierKind.PRIVATE)
                return incCause(TYPE_IS_NOT_PUBLIC);
        }

        TypeAttributes refAttr = new TypeAttributes(ref);
        if (ref instanceof CtArrayTypeReference) {
            ref = ((CtArrayTypeReference) ref).getComponentType();
            return checkTypeRef(ref);
        } else if (refAttr.isCollection()) {
            for (CtTypeReference args : ref.getActualTypeArguments())
                if (!checkTypeRef(args)) return incCause(COLLECTION_OF_UNSUPPORTED_TYPE);
            return true;
        } else {
            if (!(ref.isPrimitive() || refAttr.isSerializable() || refAttr.isClassPrimitive()))
                return incCause(TYPE_IS_NOT_STORABLE);
        }
        return true;
    }


    /**
     * Returns the visibility of an invocation
     */
    private ModifierKind visibilityOf(CtElement e) {
        return new MethodAttributes(e).getVisibility();
    }

    /**
     * Indicate if the element contains dynamic invocations to the nth level.
     * <p>
     * Dynamic invocations of collections are supported
     *
     * @param element Invocation to inspect
     * @param levels  Levels to explore
     * @return True if the method contains non static invocations
     */

    private boolean containsUnsupportedDynamicInvocations(CtElement element, int levels) {
        if (levels <= 0) return !incCause(LEVELS_TOO_DEEP); //ALWAYS EQUAL TRUE
        levels--;

        //Finds whether the snippet contains unsupported constructors
        List<CtNewClassImpl> newClasses = element.getElements(new TypeFilter<CtNewClassImpl>(CtNewClassImpl.class));
        for (CtNewClassImpl n : newClasses) {
            try {
                ModifierKind m =  visibilityOf(n);
                if (m == ModifierKind.PRIVATE || m == ModifierKind.PROTECTED)
                    return !incCause(PRIVATE_CONSTRUCTOR);//ALWAYS EQUAL TRUE
            } catch (Exception ex) {
                log.warn("Unable to find if element " + element.toString() + " contains unsupported methods");
                return !incCause(UNEXPECTED_ERR_CONSTRUCTOR);//ALWAYS EQUAL TRUE
            }
        }

        List<CtInvocation> invocations = element.getElements(
                new TypeFilter<CtInvocation>(CtInvocation.class));

        for (CtInvocation inv : invocations) {
            //Static methods are supported whether they are private or not
            if (inv.getExecutable().isStatic()) continue;

            //Finds whether is a public dynamic method
            boolean isPublicDynamic = false;
            ModifierKind m;
            try {
                m = visibilityOf(inv);
            } catch (RuntimeException ex) {
                log.warn("Unable to find if element " + element.toString() + " contains unsupported methods");
                return !incCause(CANNOT_DETERMINE_VISIBILITY);//ALWAYS EQUAL TRUE
            }
            isPublicDynamic = m != ModifierKind.PRIVATE && m != ModifierKind.PROTECTED;

            //private dynamic methods are not currently supported
            if (isPublicDynamic) {
                try {
                    //Check the special case in which the target is "this"
                    if (inv.getTarget() == null) {
                        if (!checkTypeRef(element.getPosition().getCompilationUnit().getMainType().getReference())) {
                            return !incCause(DYN_METHOD_TARGET_TYPE_UNSUPPORTED);//ALWAYS EQUAL TRUE
                            //If called by another invocation, it will depend on whether the other is supported or not
                        } else if (inv.getTarget() instanceof CtInvocation) continue;
                    } else {
                        //Checks that the target is accepted.
                        //TODO: check recursively when the target is another invocation.
                        return !checkTypeRef(inv.getTarget().getType()) && !incCause(DYN_METHOD_TARGET_TYPE_UNSUPPORTED);
                    }

                } catch (Exception ex) {
                    log.warn("Unable to find if element " + element.toString() + " contains unsupported methods");
                    return !incCause(UNEXPECTED_ERR_DYN_METHOD);
                }
            } else {
                //Private methods are only supported when they don't contain other private dynamic or
                //unsuported variables inside
                try {
                    element = inv.getExecutable().getDeclaration().getBody();
                    if (containsUnsupportedDynamicInvocations(element, levels - 1)) return true;
                } catch (Exception ex) {
                    //If we manage to even find their bodies... :P
                    log.warn("Unable to find body of invocation " + inv.toString() + " contains unsupported methods");
                    //Kind of weird we can't find the body of a private method... but maybe there is a bug somewhere
                    if (m == ModifierKind.PROTECTED) return !incCause(ERR_GET_BODY + " - Protected");//ALWAYS EQUAL TRUE
                    if (m == ModifierKind.PRIVATE) return !incCause(ERR_GET_BODY + " - Private");//ALWAYS EQUAL TRUE
                    return !incCause(ERR_GET_BODY + " - SHOULD NOT BE HERE!");//ALWAYS EQUAL TRUE
                }
            }
        }
        return false;
    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        conf = configuration;
    }
}
