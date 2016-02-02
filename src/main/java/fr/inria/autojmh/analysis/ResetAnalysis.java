package fr.inria.autojmh.analysis;

import fr.inria.controlflow.BranchKind;
import fr.inria.controlflow.ControlFlowBuilder;
import fr.inria.controlflow.ControlFlowGraph;
import fr.inria.controlflow.ControlFlowNode;
import org.apache.log4j.Logger;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class providing an analysis of when a particular variable must be reset
 * <p/>
 * Created by marodrig on 21/01/2016.
 */
public class ResetAnalysis {

    Logger log = Logger.getLogger(ResetAnalysis.class);

    /**
     * Indicates that a dynamic call made by the variable forces to reset the variable
     */
    private boolean dynamicCallsForcesReset;

    /**
     * Variables that must be reset
     */
    Set<CtVariable> mustBeReset;

    private List<CtVariableAccess> getVarAccess(CtElement e) {
        return e.getElements(new TypeFilter<CtVariableAccess>(CtVariableAccess.class));
    }

    private List<CtInvocation> getInvocations(CtElement e) {
        return e.getElements(new TypeFilter<CtInvocation>(CtInvocation.class));
    }

    private void addVariables(HashSet<CtVariable> vars, List<CtVariableAccess> acccess) {
        for (CtVariableAccess v : acccess)
            try {
                vars.add(v.getVariable().getDeclaration());
            } catch (NullPointerException ex) {
                log.warn("Unable to find variable while performing the reset analysis");
            }
    }


    public void run(CtStatement statement) {
        ControlFlowBuilder builder = new ControlFlowBuilder();
        ControlFlowGraph graph = builder.build(statement);
        run(graph);

        if (dynamicCallsForcesReset) {
            List<CtInvocation> invs = getInvocations(statement);
            for (CtInvocation in : invs)
                if (in.getTarget() != null)
                    for ( CtVariableAccess access : getVarAccess(in.getTarget()))
                        mustBeReset.add(access.getVariable().getDeclaration());
        }
    }

    /**
     * Runs the analysis.
     *
     * @param graph
     */
    public void run(ControlFlowGraph graph) {
        mustBeReset = new HashSet<>();

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

        for (CtVariable v : control) {
            if (assigned.contains(v)) mustBeReset.add(v);
        }
    }

    /**
     * Indicates whether a dynamic call made by the variable forces to reset the variable
     */
    public boolean isDynamicCallsForcesReset() {
        return dynamicCallsForcesReset;
    }

    /**
     * Indicates whether a dynamic call made by the variable forces to reset the variable
     */
    public void setDynamicCallsForcesReset(boolean dynamicCallsForcesReset) {
        this.dynamicCallsForcesReset = dynamicCallsForcesReset;
    }

    public Set<CtVariable> getMustBeReset() {
        return mustBeReset;
    }

}
