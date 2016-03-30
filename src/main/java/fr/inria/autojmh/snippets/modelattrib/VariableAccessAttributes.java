package fr.inria.autojmh.snippets.modelattrib;

import fr.inria.autojmh.snippets.Preconditions;
import fr.inria.controlflow.ControlFlowBuilder;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.NotFoundException;
import fr.inria.dataflow.InitializedVariables;
import org.apache.log4j.Logger;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

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
     * Indicate whether 'a' is a variable declared inside the statement being benchmarked
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
     * Indicate if a variable access is initialized before the statement.
     *
     */
    public static boolean isInitialized(CtVariableAccess a, CtStatement statement, InitializedVariables vars) {

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

    public static boolean isImplicitThiz(CtInvocation inv) {
        return isImplicitThiz(inv, new Preconditions());
    }

    public static boolean isImplicitThiz(CtInvocation inv, Preconditions preconditions) {
        return !inv.getExecutable().isStatic() &&
                (inv.getTarget() == null || inv.getTarget().toString().equals("this")) &&
                preconditions.checkTypeRef(inv.getExecutable().getDeclaringType());
    }

}
