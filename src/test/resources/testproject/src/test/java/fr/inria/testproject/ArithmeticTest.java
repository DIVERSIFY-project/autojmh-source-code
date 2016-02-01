package fr.inria.testproject;


import fr.inria.testproject.Arithmetic;
import org.junit.Test;
import org.junit.Assert;

/**
 * Created by marcel on 23/02/14.
 */
public class ArithmeticTest {

    @Test
    public void testAddConditional() {
       Arithmetic a = new Arithmetic();
        a.addConditional(2, 4);
        Assert.assertFalse(false);
    }

    @Test
    public void testSubConditional() {
        Arithmetic a = new Arithmetic();
        a.subConditional(4, 2);
        Assert.assertFalse(false);
    }

    @Test
    public void testFullCoverage() {
        Arithmetic a = new Arithmetic();
        a.fullCoverage(4);
        Assert.assertFalse(false);
    }
}
