package fr.inria.autojmh.snippets.modelattrib;

import fr.inria.autojmh.snippets.Preconditions;
import fr.inria.controlflow.ControlFlowBuilder;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.NotFoundException;
import fr.inria.dataflow.InitializedVariables;
import org.apache.log4j.Logger;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.Set;

import static spoon.reflect.declaration.ModifierKind.FINAL;
import static spoon.reflect.declaration.ModifierKind.STATIC;

/**
 * Class to determine some important high level variable accesses
 * <p>
 * Created by marodrig on 22/03/2016.
 */
public class VariableAccessAttributes {

    private static Logger log = Logger.getLogger(VariableAccessAttributes.class);

    /**
     * Indicate if the variable access is a field of type primitive(int, float, byte, etc) array
     */
    public static boolean isFieldOfPrimitiveArray(CtVariableAccess a) {
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
     * Indicates if variable 'a' is public a field of a class that can be stored.
     * <p>
     * In that case we store the object and not the field
     *
     * @param a Variable to determine whether it must be serialized or not
     * @return True if it must be serialized, false otherwise.
     */
    public static boolean canBeReplacedByTarget(CtVariableAccess a) {
        //boolean result = !isLocalVariable(a, localVars);
        if (a instanceof CtTargetedAccess) {
            CtTargetedAccess access = (CtTargetedAccess) a;

            CtTypeReference ref = null;
            //This code is to get the type of the target.
            //Spoon have so many undefined behaviors as for version 3.0,
            //forcing to have this somehow complicated code to handle specific cases
            if (access.getTarget() != null) {
                ref = access.getTarget().getType();
                if (ref == null && access.getTarget() instanceof CtFieldAccess)
                    ref = ((CtFieldAccess) access.getTarget()).getVariable().getType();
                if (ref == null)
                    throw new RuntimeException("Unable to get type of " + ((CtTargetedAccess) a).getTarget().toString());
            }
            //if access is null then is this
            else ref = access.getParent(CtClass.class).getReference();

            try {
                //If is a public field variable of an accepted type, it should not be stored
                return new Preconditions().checkTypeRef(ref) &&
                        (access.getVariable().getDeclaration() == null ||
                                access.getVariable().getDeclaration().getVisibility() == ModifierKind.PUBLIC);
            } catch (NullPointerException ex) {
                return !(new Preconditions().checkTypeRef(ref));
            }
        }
        return false;
    }

    /**
     * Indicate whether 'a' is a variable declared inside the statement being benchmarked
     * Local variables are with respect to the block containing the micro benchmark. NOT WITH RESPECT TO THE METHOD
     */
    public static boolean isLocalVariable(CtVariableAccess a, List<CtLocalVariable> localVars) {
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

    public static boolean isInitialized(CtVariableAccess a, CtStatement statement) throws NotFoundException {
        ControlFlowBuilder v = new ControlFlowBuilder();
        CtMethod m = statement.getParent(CtMethod.class);
        if (m == null) return false;
        m.accept(v);
        ControlFlowGraph g = v.getResult();
        g.simplify();
        InitializedVariables vars = new InitializedVariables();
        vars.run(ControlFlowBuilder.firstNode(g, statement));
        return isInitialized(a, statement, vars);
    }

    /**
     * Indicates if the target of a field is allowed for storage
     *
     * @param targetedAccess
     * @return
     */
    public static boolean isTargetAllowed(CtTargetedExpression targetedAccess) {
        if (targetedAccess.getTarget() != null) {
            CtTypeReference ref = targetedAccess.getTarget().getType();
            if (ref == null && targetedAccess.getTarget() instanceof CtFieldAccess)
                ref = ((CtFieldAccess) targetedAccess.getTarget()).getVariable().getType();
            return new Preconditions().checkTypeRef(ref);
        } else return new Preconditions().checkTypeRef(targetedAccess.getParent(CtClass.class).getReference());
    }

    /**
     * Indicates that the variable is an static final
     * @param a
     * @return
     */
    public static boolean isAConstant(CtVariableAccess a) {
        try {
            //An static final variable is indeed initialized,
            Set<ModifierKind> ms = a.getVariable().getDeclaration().getModifiers();
            if (ms.contains(STATIC) && ms.contains(FINAL)) return true;
        } catch (NullPointerException ex) {
            return false;
        }
        return false;
    }

    /**
     * Indicate if a variable access is initialized before the statement.
     */
    public static boolean isInitialized(CtVariableAccess a, CtStatement statement, InitializedVariables vars) {

        //Discard all variables being declared inside the loop expression
        //TODO: review this, sometimes the declaration is null
        try {


            List<CtVariable> vs = statement.getElements(new TypeFilter<CtVariable>(CtVariable.class));
            if (a.getVariable().getDeclaration() != null) {
                //Special cases
                if (vs.contains(a.getVariable().getDeclaration())) return false;
            }

            if (a.getVariable() instanceof CtParameterReference) return true;
            if (a instanceof CtFieldAccess) {
                CtFieldAccess ta = (CtFieldAccess) a;
                //Is a field of 'this'
                if (ta.getTarget() == null) return true;
                if (ta.getTarget() != null && ta.getTarget() instanceof CtVariableAccess) {
                    return isInitialized((CtVariableAccess) ta.getTarget(), statement, vars);
                }
            }
            /*
            List<CtVariable> vs = statement.getElements(new TypeFilter<CtVariable>(CtVariable.class));
            if (  )
            if (a.getVariable().getDeclaration() != null) {
                //Special cases
                if (vs.contains(a.getVariable().getDeclaration())) return false;
                if (a.getVariable().getDeclaration().getDefaultExpression() != null
                        || !(a.getVariable() instanceof CtLocalVariableReference)) return true;
            }*/
        } catch (IllegalStateException | NullPointerException ex) {
            log.warn("Unable to evaluate the initialization special case for " + a);
        }

        return vars.getInitialized().contains(a.getVariable());
    }

    public static boolean isImplicitThiz(CtInvocation inv) {
        return isImplicitThiz(inv, new Preconditions());
    }

    public static boolean isImplicitThiz(CtInvocation inv, Preconditions preconditions) {
        return !inv.getExecutable().isStatic() &&
                (inv.getTarget() == null || inv.getTarget().toString().equals("this")) &&
                preconditions.checkTypeRef(inv.getExecutable().getDeclaringType());
    }

}
