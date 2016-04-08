package testpack;

import java.util.ArrayList;

/**
 * Created by marcel on 23/02/14.
 * A class to test some coverage. In some method an "explosive" line is introduced
 * which will not be tested.
 *
 */
public class Trigonometry {

    //A dummy Add procedure to test some logic branches
    public double sinConditional(double a) {
        double senA = Math.sin(a);
        ArrayList<Double> d = new ArrayList<Double>();
        /** @bench-this */
        if ( senA > 0.5 ) {
            d.add(senA * 2);
            return d.get(0);
        }
        else {
            return senA;
        }
    }

    //Yet another dummy procedure to test some logic branches
    public double cosConditional(double a) {
        /** @bench-this */
        double cosA = Math.cos(a);

        if ( cosA > 0.5 ) {
            return cosA * 2;
        }
        /** @bench-until-here */

        for ( int i = 0; i < a; i++ ) cosA += a * Math.cos(a);
        return cosA;
    }

    //Some lines to test full coverage
    public double fullCoverage(double a) {
        double sc = (Math.sin(a) + Math.cos(a));
        return sc * 2;
    }

}
