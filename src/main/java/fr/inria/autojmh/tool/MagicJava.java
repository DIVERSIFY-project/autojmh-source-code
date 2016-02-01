package fr.inria.autojmh.tool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by marodrig on 30/11/2015.
 */
public class MagicJava {

    private int A;
    private int B;

    public MagicJava(int A, int B) {
        this.A = A;
        this.B = B;
    }

    public int incr2(int m) {
        return m + m + m + m + m + m + m +
               m + m + m + m + m + m + m;
    }
    public int magic() {
        long b = System.nanoTime();
        int i = 0;
        int ki = 1;
        for  (int k = 0; k < 400000000; k++)
            i += incr2(ki);
        System.out.print(" 2:" + (double)(System.nanoTime() - b));
        System.out.println();
        return i;
    }

    public double magic3() {
        long a = System.nanoTime();
        double d = 0.0;
        for (int i = 0; i < 10000; i++) {
            d += Math.log(42);
        }
        System.out.println("Log takes: " + (System.nanoTime() - a) / 10000);
        return d;
    }

    public double magic5(int k) {
        long a = System.nanoTime();
        double d = 0.0;
        d += 5.0;
        System.out.println("Log takes: " + (System.nanoTime() - a));
        return d;
    }

    public double magic2(int k) {
        long a = System.nanoTime();
        double d = 0.0;
        for (int i = 0; i < 1000000000; i++) {
            d += Math.log(42);
        }
        System.out.println("Log takes: " + (double) (System.nanoTime() - a) / 1000000000.0);
        return d;
    }

    public static void main(String[] args) throws Exception {
        int k = new MagicJava(50, 500).magic();
        k += new MagicJava(50, 500).magic();
        k += new MagicJava(50, 500).magic();
        k += new MagicJava(50, 500).magic();
        k += new MagicJava(50, 500).magic();
        k += new MagicJava(50, 500).magic();
        k += new MagicJava(50, 500).magic();
        k += new MagicJava(50, 500).magic();
        k += new MagicJava(50, 500).magic();
        k += new MagicJava(50, 500).magic();
        k += new MagicJava(50, 500).magic();

        System.out.println(k);

        /*
        new MagicJava(50, 500).magic3();
        new MagicJava(50, 500).magic5(1);
/*
        for ( int i = 0; i < 5; i++ ) {
            System.out.print(", " + i * 26);
        }
*/
    }
}
