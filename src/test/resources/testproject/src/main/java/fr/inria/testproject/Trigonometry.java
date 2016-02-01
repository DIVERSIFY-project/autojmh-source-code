package fr.inria.testproject;

/**
 * Created by marcel on 23/02/14.
 *
 * A class to test some coverage. In some method an "explosive" line is introduced
 * which will not be tested.
 *
 */
public class Trigonometry {

    //A dummy Add procedure to test some logic branches
    public double sinConditional(double a) {
        /** @bench-this */
        double senA = Math.sin(a);

        if ( senA > 0.5 ) {
            return senA * 2;
        }
        else {
            return senA;
        }
    }

    //Yet another dummy procedure to test some logic branches
    public double cosConditional(double a) {

        double cosA = Math.cos(a);
        /** @bench-this */
        for ( int i = 0; i < a; i++ ) cosA += a * Math.cos(a);
        /** @bench-until-here */

        if ( cosA > 0.5 ) {
            return cosA * 2;
        }

        return cosA;
    }

    //Some lines to test full coverage
    public double fullCoverage(double a) {
        double sc = (Math.sin(a) + Math.cos(a));
        return sc * 2;
    }

}
