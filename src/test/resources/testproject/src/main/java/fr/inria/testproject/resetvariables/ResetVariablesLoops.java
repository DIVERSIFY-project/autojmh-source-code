package fr.inria.testproject.resetvariables;

/**
 * A class to test the reset variable analysis
 *
 * Created by marodrig on 21/01/2016.
 */
public class ResetVariablesLoops {


    /**
     * Method in which the reset is not needed
     * @param offset
     * @param end
     * @return
     */
    public int noResetNeeded(int offset, int end) {
        for (int i = 0; i < end; i++) {
            offset++;
        }
        return offset;
    }

    /**
     * Method in which the reset is needed.
     * @param offset
     * @param end
     * @return
     */
    public boolean resetNeeded(int offset, int end) {
        for (int i = 0; i < end; i++) {
            if ( offset > end ) return true;
            offset++;
        }
        return false;
    }

}
