package fr.inria.autojmh.generators.microbenchmark.parts;

import fr.inria.autojmh.snippets.BenchSnippet;
import org.junit.Test;
import spoon.reflect.code.CtIf;

import java.util.List;

import static fr.inria.autojmh.ElementProvider.loadSnippets;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by marodrig on 28/03/2016.
 */
public class ExtractedMethodsTest {

    @Test
    public void testGenerate() throws Exception {
        List<BenchSnippet> list = loadSnippets(this, "callInvocationsSomePublic", CtIf.class);
        ExtractedMethods replacement = new ExtractedMethods();
        String code = replacement.generate(list.get(0));
        System.out.println(code);
        assertTrue(code.contains("private int callPrivate(DataContextPlayGround THIZ) {"));
        assertTrue(code.contains("return callPrivate(THIZ);"));
        assertTrue(code.contains("private int callProtected(DataContextPlayGround THIZ) {"));
        assertTrue(code.contains("return callProtected(THIZ);"));
        assertFalse(code.contains("callPublic(DataContextPlayGround THIZ)"));
    }
}