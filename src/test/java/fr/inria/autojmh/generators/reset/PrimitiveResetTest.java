package fr.inria.autojmh.generators.reset;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by marodrig on 02/02/2016.
 */
public class PrimitiveResetTest {

    @Test
    public void testResetCode() throws Exception {
        PrimitiveReset reset = new PrimitiveReset();
        assertEquals("sum = 0;\n", reset.resetCode("sum", 0));
        assertEquals("name = \"Lady Gaga\";\n", reset.resetCode("name", "Lady Gaga"));
    }

    @Test
    public void testResetFromAnotherVar() throws Exception {

    }
}