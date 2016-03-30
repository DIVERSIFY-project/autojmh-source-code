package fr.inria.autojmh.snippets.modelattrib;

import fr.inria.autojmh.snippets.SourceCodeSnippet;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

import static fr.inria.autojmh.ElementProvider.loadSnippets;
import static org.junit.Assert.*;

/**
 * Created by marodrig on 24/03/2016.
 */
public class MethodAttributesTest {

    public ModifierKind findVisibility(String method) throws Exception {
        List<SourceCodeSnippet> list = loadSnippets(this, method, CtReturn.class);
        List<CtInvocation> invs = list.get(0).getASTElement().getElements(
                new TypeFilter<CtInvocation>(CtInvocation.class));
        return MethodAttributes.visibility(invs.get(0));

    }

    @Test
    public void testVisibilityPublic() throws Exception {
        assertEquals(ModifierKind.PUBLIC, findVisibility("callPublic"));
    }

    @Test
    public void testVisibilityProtected() throws Exception {
        assertEquals(ModifierKind.PROTECTED, findVisibility("callProtected"));
    }

    @Test
    public void testVisibilityPrivate() throws Exception {
        assertEquals(ModifierKind.PRIVATE, findVisibility("callPrivate"));
    }

}