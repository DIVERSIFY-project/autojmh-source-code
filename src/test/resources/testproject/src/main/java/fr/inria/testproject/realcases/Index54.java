package fr.inria.testproject.realcases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by marcel on 23/02/14.
 * <p>
 * A class to represent specific cases we found to be trouble some while extracting microbenchmarks
 */
public final class Index54 implements Serializable, Iterable<Double> {

    public final double[] data;

    public final int[] intData;

    public Index54(int n) {

        data = new double[n];
        intData = new int[n];
    }

    public Iterator<Double> iterator() {
        return null;
    }

    public int length() {
        return intData.length;
    }

    /**
     * Creates an Index using the values from the given ArrayList.
     * <p>
     * Values are cast to integers as needed, according to the semantics of (int)value
     *
     * @param v
     * @return
     */
    public static Index54 create(ArrayList<Integer> v) {
        int n = v.size();
        Index54 ind = new Index54(n);
        for (int i = 0; i < n; i++) {
            ind.data[i] = v.get(i);
        }
        return ind;
    }

    public static boolean isPositiveSemiDefinite(ArrayList<Index54> e) {
        ArrayList<Index54> eigenValues = e;
        for (Index54 v : eigenValues) {
            if (v.data[0] < 0) return false;
        }
        return true;
    }

    private AdenseArrayMatrixCase QT;

    //Testing a serializable private field of a non serialiable target and the private field has a public field
    public void getDiagonal(double[] diag, double[] off, int N) {
        for (int i = 0; i < N; i++) {
            diag[i] = QT.data[i * N + i];
            if (i + 1 < N) {
                off[i] = QT.data[i * N + i + 1];
            }
        }
    }

    private double[] privData;

    public int maxAbsElementIndex() {
        double[] data = this.privData;
        if (data.length == 0) return 0;
        double result = data[0];
        int di = 0;
        for (int i = 1; i < data.length; i++) {
            double d = Math.abs(data[i]);
            if (d > result) {
                result = d;
                di = i;
            }
        }
        return di;
    }

    public boolean allInRange(double start, double end) {
        for (int i = 0; i < data.length; i++) {
            double a = data[i];
            if ((a < start) || (a >= end)) return false;
        }
        return true;
    }

    public Index54 compose(Index54 a) {
        int len = this.length();
        Index54 r = new Index54(len);
        for (int i = 0; i < len; i++) {
            r.intData[i] = a.intData[intData[i]];
        }
        return r;
    }

    public ArrayList<Double> thisIterator(Double a) {
        ArrayList<Double> al = new ArrayList<Double>();
        for (Double s : this) {
            if ( s.equals(a) ) return al;
        }
        return null;
    }
}


