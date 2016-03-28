package fr.inria.autojmh.snippets;

import org.junit.Test;
import spoon.reflect.code.CtLoop;

import static fr.inria.autojmh.ElementProvider.loadFirstSnippets;
import static org.junit.Assert.*;

/**
 * Created by marodrig on 26/03/2016.
 */
public class TemplateInputVariableTest {

    @Test
    public void testGetIsCollection() throws Exception {
        BenchSnippet snippet = loadFirstSnippets(this, "collectionOfSerializables", CtLoop.class);

        /*
        for (  )
        snippet.getTemplateAccessesWrappers()*/

    }

    @Test
    public void testGetIsSerializable() throws Exception {

    }
}