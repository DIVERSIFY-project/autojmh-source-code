package fr.inria.autojmh.analysis;

import fr.inria.controlflow.BranchKind;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.ControlFlowNode;
import org.apache.log4j.Logger;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Class providing an analysis of when a particular variable must be reset
 * <p/>
 * Created by marodrig on 21/01/2016.
 */
public class ResetAnalysis {

    Logger log = Logger.getLogger(ResetAnalysis.class);

    /**
     * Variables that must be reset
     */
    List<CtVariableAccess> mustBeReset;

    private List<CtVariableAccess> getVarAccess(CtElement e) {
        return e.getElements(new TypeFilter<CtVariableAccess>(CtVariableAccess.class));
    }

    private void addVariables(HashSet<CtVariable> vars, List<CtVariableAccess> acccess) {
        for (CtVariableAccess v : acccess)
            try {
                vars.add(v.getVariable().getDeclaration());
            } catch (NullPointerException ex) {
                log.warn("Unable to find variable while performing the reset analysis");
            }
    }

    /**
     * Runs the analysis.
     *
     * @param graph
     */
    public void run(ControlFlowGraph graph) {
        mustBeReset = new ArrayList<>();

        //Variables found in a control branch
        HashSet<CtVariable> control = new HashSet<>();
        //Variables being assigned
        HashSet<CtVariable> assigned = new HashSet<>();

        for (ControlFlowNode n : graph.vertexSet()) {
            if (n.getKind() == BranchKind.BRANCH) {
                CtElement e = n.getStatement();
                List<CtVariableAccess> vars = getVarAccess(e);
                addVariables(control, vars);
            } else if (n.getKind() == BranchKind.STATEMENT) {
                if (n.getStatement() instanceof CtAssignment) {
                    CtAssignment assignment = (CtAssignment) n.getStatement();
                    addVariables(assigned, getVarAccess(assignment.getAssigned()));
                } else if (n.getStatement() instanceof CtUnaryOperator) {
                    addVariables(assigned, getVarAccess(n.getStatement()));
                }
            }
        }

        for ( CtVariable v : control ) {
            if ( !assigned.contains(v) ) control.remove(v);
        }

    }

}
