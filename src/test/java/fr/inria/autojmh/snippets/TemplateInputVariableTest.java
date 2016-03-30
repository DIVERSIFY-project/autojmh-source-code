package fr.inria.autojmh.snippets;

import fr.inria.autojmh.generators.microbenchmark.parts.substitutes.CtVariableAccessDecorator;
import org.junit.Test;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

import static fr.inria.autojmh.ElementProvider.loadFirstSnippets;
import static junit.framework.TestCase.assertEquals;

/**
 * Created by marodrig on 26/03/2016.
 */
public class TemplateInputVariableTest {

    @Test
    public void testGetCompilableName() throws Exception {

        BenchSnippet snippet = loadFirstSnippets(this, "anIntMethod", CtAssignment.class);

        //We don't use the get Accesses method because is to heavy in logic
        List<CtVariableAccess> vars = snippet.getASTElement().getElements(
                new TypeFilter<CtVariableAccess>(CtVariableAccess.class));

        CtVariableAccessDecorator decorator = new CtVariableAccessDecorator(vars.get(0));

        TemplateInputVariable var = new TemplateInputVariable();
        var.setVariableAccess(decorator);
        assertEquals("THIZ_field1", var.getTemplateCodeCompilableName());
    }

}