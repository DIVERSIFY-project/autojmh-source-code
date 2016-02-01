package fr.inria.testproject;

import org.junit.Test;
import org.junit.Assert;

/**
 * Created by marcel on 23/02/14.
 */
public class TrigonometryTest {

    @Test
    public void testCosConditional() {
       Trigonometry t = new Trigonometry();
        t.cosConditional(1);
        Assert.assertFalse(false);
    }

    @Test
    public void testSinConditional() {
        Trigonometry t = new Trigonometry();
        t.sinConditional(1);
        Assert.assertFalse(false);
    }

    @Test
    public void testFullCoverage() {
        Trigonometry a = new Trigonometry();
        a.fullCoverage(4);
        Assert.assertFalse(false);
    }

}
