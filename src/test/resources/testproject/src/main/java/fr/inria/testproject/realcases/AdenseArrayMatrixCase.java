package fr.inria.testproject.realcases;

import java.io.Serializable;

/**
 * Created by marcel on 23/02/14.
 * <p>
 * A class to represent specific cases we found to be trouble some while extracting microbenchmarks
 */
public abstract class AdenseArrayMatrixCase implements Serializable {

    public double[] data;

    public abstract int getArrayOffset();

    public final int rowCount() {
        return 0;
    }

    public final int columnCount() {
        return 10;
    }

    public boolean mikeraAdenseArrayMatrix46() {
        int rc = rowCount();
        int cc = columnCount();
        int offset = getArrayOffset();
        for (int i = 1; i < rc; i++) {
            if (!StaticMethods.isZero(data, offset + i * cc, Math.min(cc, i))) return false;
        }

        return true;
    }


    protected void doSomething(String s) {
        System.out.print(s + "Yeah");
    }

}


