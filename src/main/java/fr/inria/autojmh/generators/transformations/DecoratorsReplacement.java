package fr.inria.autojmh.generators.transformations;

import fr.inria.autojmh.generators.transformations.substitutes.CtInvocationDecorator;
import fr.inria.autojmh.generators.transformations.substitutes.CtVariableAccessDecorator;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.autojmh.tool.Configurable;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import static fr.inria.autojmh.snippets.modelattrib.MethodAttributes.visibility;

/**
 * Extract private static method out of an statement and copy its body to the transformations
 */
public class DecoratorsReplacement extends AbstractTransformation implements Configurable {

    private int deep = 5;

    /**
     * Replaces all invocations in the snippet with an invocation stub with a pretty print
     *
     * @param snippet Snippet to be transformed
     */
    @Override
    public void transform(BenchSnippet snippet) {
        transformed = snippet.getASTElement();
        replace(transformed, deep, new HashSet<CtInvocation>());
    }

    /**
     * Replaces recursively all invocations in the statement in the snippet with an invocation stub with a pretty print
     * It goes inside the method bodies when it can and recursively replaces invocations
     */
    private void replace(CtElement st, int deep, HashSet<CtInvocation> visited) {
        if (deep <= 0) return;
        deep--;

        //Replace variable accesses
        List<CtVariableAccess> accesses = st.getElements(new TypeFilter<CtVariableAccess>(CtVariableAccess.class));
        for (CtVariableAccess a : accesses) {
            if (!(a instanceof CtVariableAccessDecorator)) {
                CtVariableAccessDecorator varDeco = new CtVariableAccessDecorator(a);
                a.replace(varDeco);
            }
        }

        //Replace invocations
        List<CtInvocation> invs = st.getElements(new TypeFilter<CtInvocation>(CtInvocation.class));
        for (CtInvocation inv : invs) {
            if (!(inv instanceof CtInvocationDecorator) && visibility(inv) != ModifierKind.PUBLIC) {
                CtInvocationDecorator invDeco = new CtInvocationDecorator(inv);
                inv.replace(invDeco);
                visited.add(inv);
                //Find body of the element
                replace(inv.getExecutable().getDeclaration().getBody(), deep, visited);
            }
        }
    }

    @Override
    public String getGeneratedCode() {
        StringBuilder sb = new StringBuilder();

        //Print invocations
        HashSet<CtInvocation> visited = new HashSet<>();
        Stack<CtInvocation> stack = new Stack<>();
        for (CtInvocation inv : transformed.getElements(new TypeFilter<CtInvocation>(CtInvocation.class)))
            if (inv instanceof CtInvocationDecorator)
                stack.push(inv);

        while (!stack.isEmpty()) {
            CtInvocation inv = stack.pop();
            visited.add(inv);
            CtInvocationDecorator invDeco = (CtInvocationDecorator) inv;
            sb.append(invDeco.toString()).append("\n");
            for (CtInvocation child : inv.getElements(new TypeFilter<CtInvocation>(CtInvocation.class))) {
                if (child instanceof CtInvocationDecorator && !visited.contains(child))
                    stack.push(child);
            }
        }
        return sb.toString();
    }

    @Override
    public String getModifiedSnippetCode() {
        return transformed.toString();
    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        deep = configuration.getMethodExtractionDepth();
    }
}
