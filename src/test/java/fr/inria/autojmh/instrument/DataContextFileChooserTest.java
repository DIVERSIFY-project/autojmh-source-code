package fr.inria.autojmh.instrument;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class DataContextFileChooserTest {

    static final String CLASS_NAME = "fr_inria_testproject_Trigonometry_15";

    @Test
    public void testChooseAfter() throws Exception {
        DataContextFileChooser chooser = new DataContextFileChooser();
        chooser.setDataContextPath(this.getClass().getResource("/log").toURI().getPath());
        String after = chooser.chooseAfter(CLASS_NAME);
        assertTrue(after.contains("after-" + CLASS_NAME.replace("_", "-") + "--"));
        String before = chooser.chooseBefore(CLASS_NAME);
        assertEquals(after, "after-" + before);
    }

    @Test
    public void testChooseBefore() throws Exception {
        DataContextFileChooser chooser = new DataContextFileChooser();
        chooser.setDataContextPath(this.getClass().getResource("/log").toURI().getPath());
        String s = chooser.chooseBefore(CLASS_NAME);
        assertTrue(s.contains(CLASS_NAME.replace("_", "-") + "--"));
    }

    @Test(expected = IOException.class)
    public void testChooseBeforeRaise() throws Exception {
        DataContextFileChooser chooser = new DataContextFileChooser();
        chooser.setDataContextPath(this.getClass().getResource("/log").toURI().getPath());
        chooser.chooseBefore("fr_inria_testproject_Arithmetic_18");
    }
}