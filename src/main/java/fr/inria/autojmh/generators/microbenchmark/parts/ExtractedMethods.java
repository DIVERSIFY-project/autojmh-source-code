package fr.inria.autojmh.generators.microbenchmark.parts;

import fr.inria.autojmh.generators.printer.AJMHPrettyPrinter;
import fr.inria.autojmh.snippets.BenchSnippet;
import spoon.compiler.Environment;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.HashSet;
import java.util.Stack;

import static fr.inria.autojmh.snippets.modelattrib.MethodAttributes.visibility;
import static spoon.reflect.declaration.ModifierKind.PUBLIC;

/**
 * Class to generate the code for the extracted methods
 * <p>
 * Created by marodrig on 28/03/2016.
 */
public class ExtractedMethods extends AbstractMicrobenchmarkPart {

    @Override
    public String generate(BenchSnippet snippet) {
        StringBuilder sb = new StringBuilder();

        //Print invocations
        HashSet<CtInvocation> visited = new HashSet<>();
        HashSet<CtExecutable> extracted = new HashSet<>();
        Stack<CtInvocation> stack = new Stack<>();
        for (CtInvocation inv : snippet.getASTElement().getElements(new TypeFilter<>(CtInvocation.class)))
            if (visibility(inv) != PUBLIC)
                stack.push(inv);

        while (!stack.isEmpty()) {
            CtInvocation inv = stack.pop();
            visited.add(inv);
            //Try to get the declaration
            CtExecutable ex = inv.getExecutable().getDeclaration();
            if ( ex != null && !extracted.contains(ex) ) {
                extracted.add(ex);
                Environment env = snippet.getASTElement().getFactory().getEnvironment();
                AJMHPrettyPrinter printer = new AJMHPrettyPrinter(env);
                printer.scan(ex);
                sb.append(printer.toString()).append("\n");
            }
            for (CtInvocation child : inv.getElements(new TypeFilter<>(CtInvocation.class))) {
                if (!visited.contains(child) && visibility(inv) != PUBLIC)
                    stack.push(child);
            }
        }

        return sb.toString();
    }
}
