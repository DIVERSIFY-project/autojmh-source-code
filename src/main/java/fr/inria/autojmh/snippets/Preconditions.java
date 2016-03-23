package fr.inria.autojmh.snippets;

import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.autojmh.tool.Configurable;
import org.apache.log4j.Logger;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtNewClassImpl;

import java.util.*;

/**
 *
 * A class that holds the code to enforce the preconditions needed for a snippet in order to be selected for an
 * automatic microbenchmark
 *
 * Created by marodrig on 22/03/2016.
 */
public class Preconditions implements Configurable {

    private static Logger log = Logger.getLogger(Preconditions.class);


    //Quantify the causes of rejection
    private HashMap<String,  Integer> rejectionCause;

    //Global configurations
    private AJMHConfiguration conf;

    /**
     * A Hash map containing the main rejection causes for the snippets.
     *
     * Will allow us to better drive our efforts to develop strategies to smooth these preconditions
     */
    public HashMap<String, Integer> getRejectionCause() {
        return rejectionCause;
    }

    public void setRejectionCause(HashMap<String, Integer> rejectionCause) {
        this.rejectionCause = rejectionCause;
    }



    public Preconditions() {

    }



    /**
     * Indicate whether we can extract the segment into a microbenchmark or not.
     * An snippet must meet certain preconditions
     *
     * <p/>
     *
     * @param snippet
     * @return
     */
    public boolean checkSnippet(BenchSnippet snippet) {
        int depth = conf == null ? 5 : conf.getMethodExtractionDepth();

        return snippet.getASTElement() != null &&
                allVariablesAreSupported(snippet) &&
                !containsUnsupportedDynamicInvocations(snippet.getASTElement(), depth, conf);
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
                    ref.getDeclaration().getVisibility() == ModifierKind.PRIVATE) return false;
        }

        TypeAttributes refAttr = new TypeAttributes(ref);
        if (ref instanceof CtArrayTypeReference) {
            ref = ((CtArrayTypeReference) ref).getComponentType();
            return checkTypeRef(ref);
        } else if (refAttr.isCollection()) {
            for (CtTypeReference args : ref.getActualTypeArguments())
                if (!checkTypeRef(args)) return false;
            return true;
        } else return ref.isPrimitive() || refAttr.isSerializable() || refAttr.isClassPrimitive();
    }

    /**
     * Indicate if the element contains dynamic invocations to the nth level.
     * <p/>
     * Dynamic invocations of collections are supported
     *
     * @param element Invocation to inspect
     * @param levels  Levels to explore
     * @param configuration
     * @return True if the method contains non static invocations
     */
    private boolean containsUnsupportedDynamicInvocations(CtElement element, int levels,
                                                                 AJMHConfiguration configuration) {
        if (levels <= 0) return true;
        levels--;

        //Finds whether the snippet contains unsupported constructors
        List<CtNewClassImpl> newClasses = element.getElements(
                new TypeFilter<CtNewClassImpl>(CtNewClassImpl.class));
        for (CtNewClassImpl n : newClasses) {
            try {
                ModifierKind m = n.getExecutable().getDeclaration().getVisibility();
                if ( m == ModifierKind.PRIVATE || m == ModifierKind.PROTECTED ) return true;
            } catch (NullPointerException ex) {
                log.warn("Unable to find if element " + element.toString() + " contains unsupported methods");
                return true;
            }
        }

        List<CtInvocation> invocations = element.getElements(
                new TypeFilter<CtInvocation>(CtInvocation.class));

        for (CtInvocation inv : invocations) {
            //Static methods are supported whether they are private or not
            if (inv.getExecutable().isStatic()) continue;

            //Finds whether is a public dynamic method
            boolean isPublicDynamic = false;
            try {
                ModifierKind m = inv.getExecutable().getDeclaration().getVisibility();
                isPublicDynamic = m != ModifierKind.PRIVATE && m != ModifierKind.PROTECTED;
            } catch (NullPointerException ex) {
                log.warn("Unable to find if element " + element.toString() + " contains unsupported methods");
                return true;
            }
            //private dynamic methods are not currently supported
            if (isPublicDynamic) {
                try {
                    //Check the special case in which the target is "this"
                    if (inv.getTarget() == null) {
                        if (!checkTypeRef(element.getPosition().getCompilationUnit().getMainType().getReference())) {
                            return true;
                            //If called by another invocation, it will depend on whether the other is supported or not
                        } else if (inv.getTarget() instanceof CtInvocation) continue;
                    } else return checkTypeRef(inv.getTarget().getType());
                    if (containsUnsupportedDynamicInvocations(inv, levels - 1, configuration)) return true;
                } catch (NullPointerException ex) {
                    log.warn("Unable to find if element " + element.toString() + " contains unsupported methods");
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

    @Override
    public void configure(AJMHConfiguration configuration) {
        conf = configuration;
    }
}
