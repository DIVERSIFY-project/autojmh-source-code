package fr.inria.autojmh.generators.transformations.substitutes;

import fr.inria.autojmh.snippets.BenchSnippet;
import org.junit.Test;
import spoon.reflect.code.CtReturn;

import java.util.List;

import static fr.inria.autojmh.ElementProvider.loadSnippets;
import static org.junit.Assert.*;

/**
 * Created by marodrig on 24/03/2016.
 */
public class CtVariableAccessDecoratorTest {

    @Test
    public void testToString() throws Exception {
        //
        List<BenchSnippet> list = loadSnippets(this, "callTheCallDontPass", CtReturn.class);
    }
}