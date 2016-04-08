package fr.inria.testproject.realcases;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

/**
 * Created by marcel on 23/02/14.
 * <p>
 * A class to represent specific cases we found to be trouble some while extracting microbenchmarks
 */
public abstract class StaticMethods {

    public static boolean isZero(double[] data, int k, int j) {
        return data[j + k] == 0;
    }

}


