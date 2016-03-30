package fr.inria.autojmh.generators.microbenchmark.parts;

import fr.inria.autojmh.generators.microbenchmark.parts.substitutes.CtInvocationDecorator;
import fr.inria.autojmh.generators.microbenchmark.parts.substitutes.CtVariableAccessDecorator;
import fr.inria.autojmh.generators.printer.AJMHPrettyPrinter;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.autojmh.tool.Configurable;
import org.apache.log4j.Logger;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

import static fr.inria.autojmh.snippets.modelattrib.MethodAttributes.invocationTargetIsThis;
import static fr.inria.autojmh.snippets.modelattrib.MethodAttributes.visibility;

/**
 * Extract private static method out of an statement and copy its body to the parts
 */
public class SnippetCode extends AbstractMicrobenchmarkPart implements Configurable {

    public static Logger log = Logger.getLogger(SnippetCode.class);

    private int deep = 5;

    /**
     * Replaces all invocations in the snippet with an invocation stub with a pretty print
     *
     * @param snippet Snippet to be transformed
     */
    @Override
    public String generate(BenchSnippet snippet) {
        transformed = snippet.getASTElement();
        replace(transformed, deep, snippet);
        AJMHPrettyPrinter printer = new AJMHPrettyPrinter(snippet.getASTElement().getFactory().getEnvironment());
        printer.scan(snippet.getASTElement());
        return printer.toString();
    }

    /**
     * Replaces recursively all invocations in the statement in the snippet with an invocation stub with a pretty print
     * It goes inside the method bodies when it can and recursively replaces invocations
     */
    private void replace(CtElement st, int deep, BenchSnippet snippet) {
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
            ModifierKind m = visibility(inv);
            if (!(inv instanceof CtInvocationDecorator) &&
                    //We must replace protected/private methods and public methods calling 'this'
                    (invocationTargetIsThis(inv) || m != ModifierKind.PUBLIC)) {
                CtInvocationDecorator invDeco = new CtInvocationDecorator(inv);
                invDeco.setParent(inv.getParent());
                inv.replace(invDeco);
                //Find body of the element
                if (m != ModifierKind.PUBLIC) {
                    CtBlock b = inv.getExecutable().getDeclaration().getBody();
                    if (b == null) {
                        log.error("Can't replace method's body for:" + snippet.getMicrobenchmarkClassName());
                        throw new RuntimeException();
                    } else replace(b, deep, snippet);
                }
            }
        }
    }


    @Override
    public void configure(AJMHConfiguration configuration) {
        deep = configuration.getMethodExtractionDepth();
    }
}
