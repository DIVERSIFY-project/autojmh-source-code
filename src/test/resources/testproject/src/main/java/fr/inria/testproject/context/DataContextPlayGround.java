package fr.inria.testproject.context;

/**
 * Created by marcel on 23/02/14.
 * <p/>
 * A class to test some coverage. In some method an "explosive" line is introduced
 * which will not be tested.
 */
public class DataContextPlayGround {

    //A dummy Add procedure to test some logic branches
    public int arrayOfObjects(Object... values) {
        int result = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) result++;
        }
        return values.length;
    }

    //A dummy Add procedure to test some logic branches
    public int arrayOfSerializables() {
        int result = 0;
        SerializableObject[] seris = new SerializableObject[10];
        for (int i = 0; i < seris.length; i++) {
            if (seris[i] != null) result++;
        }
        return seris.length;
    }

    //A dummy Add procedure to test some logic branches
    public int arrayOfNonSerializables() {
        Thread[] seris = new Thread[10];
        int result = 0;
        for (int i = 0; i < seris.length; i++) {
            if (seris[i] != null) result++;
        }
        return seris.length;
    }

}
