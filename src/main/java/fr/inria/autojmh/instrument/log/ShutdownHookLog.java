package fr.inria.autojmh.instrument.log;


/**
 * User: Simon
 * Date: 7/23/13
 * Time: 10:10 AM
 */
public class ShutdownHookLog extends Thread {
    public void run() {
        Log.close();
    }
}

/*
class Example {

    private static final double PI = 3.14159265;

    public float sumExample(float[] data) {

        double pi = java.lang.Math.PI;

        float result = 0;
        /** @bench-this */
/*
        for (float v : data) {
            if (result > PI) return result;
            if (v > 0) result +=Math.sin(v);
            else result += Math.cos(v);
        }
        return result;
    }
}*/