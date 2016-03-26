package fr.inria.autojmh.snippets.modelattrib;

import fr.inria.autojmh.generators.transformations.substitutes.CtInvocationDecorator;
import org.apache.log4j.Logger;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Stack;

/**
 * Class to reflect some important high level attributes of method, invocations and constructors
 * which are specific to our case. The name of the class is somehow loose of course.
 * <p>
 * Created by marodrig on 22/03/2016.
 */
public class MethodAttributes {

    private static Logger log = Logger.getLogger(MethodAttributes.class);

    private final CtElement element;

    /**
     * If this element represents an invocation/method/constructor invocation, this variable represents the
     * block of code in the methods body
     *
     *
     */
    private CtBlock block;

    private ModifierKind visibility = null;

    public MethodAttributes(CtElement e) {
        this.element = e;
    }

    /**
     * Obtains the visibility of
     * @param e
     * @return
     */
    public static ModifierKind visibility(CtElement e) {
        ModifierKind m = ModifierKind.PUBLIC;

        CtExecutableReference ex;
        if ( e instanceof CtInvocation) ex = ((CtInvocation)e).getExecutable();
        else if ( e instanceof CtNewClass )ex = ((CtNewClass)e).getExecutable();
        else {
            try {
                CtMethod method = e.getParent(CtMethod.class);
                return method.getVisibility();
            } catch (NullPointerException exec) {
                throw new RuntimeException("Element has no parent method", exec);
            }
        }

        //Try to get visibility
        if (ex.getDeclaration() != null) m = ex.getDeclaration().getVisibility();
        else if (ex.getActualMethod() != null) {
            int i = ex.getActualMethod().getModifiers();
            if (Modifier.isProtected(i)) m = ModifierKind.PROTECTED;
            if (Modifier.isPrivate(i)) m = ModifierKind.PRIVATE;
        } else {
            CtTypeReference invType = e.getParent(CtClass.class).getReference();
            CtTypeReference execType = ex.getDeclaringType();

            //This should not be happening:
            if (e == execType) throw new RuntimeException("Unknown type");
                //This is more like it:
            else {
                try {
                    //Search super classes of inv to see if we can get to the execType
                    Stack<Object> superStack = new Stack<>();
                    HashSet<CtTypeReference> superSet = new HashSet<>();
                    superStack.push(invType);
                    superSet.add(invType);
                    CtTypeReference ref = null;
                    do {
                        ref = (CtTypeReference) superStack.pop();
                        for (Object r : invType.getSuperInterfaces())
                            if (!superSet.contains(r)) {
                                superSet.add((CtTypeReference)r);
                                superStack.push(r);
                            }
                    } while (!superStack.empty() && e != ref);
                    if (e == ref) m = ModifierKind.PROTECTED;
                } catch (Exception exec) {
                    log.warn("I really tried! But I was unable to find methods visibility");
                    throw new RuntimeException("Unable to find method visibility");
                }
            }
        }
        return m;
    }

    public ModifierKind getVisibility() {
        if ( visibility == null ) visibility = visibility(element);
        return visibility;
    }
}



