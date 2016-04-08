package fr.inria.testproject.context;

/**
 * Created by marcel on 23/02/14.
 *
 * A class to test some coverage. In some method an "explosive" line is introduced
 * which will not be tested.
 *
*/
public class SerializableObject implements SerializableInterface {

    int values;

    private int priValue = 0;

    public void doSomethingWithPriValue() {
        if ( priValue == 0 ) priValue = -1;
        else priValue = 0;
    }

    public SerializableObject pubField;

    public int getValues() {
        return values;
    }

    public void setValues(int values) {
        this.values = values;
    }

    public void doSomething() {
        values = 0;
    }

    //Call invocations in serializable THIZ
    public void callInvocationsSomePublic(boolean bb) {
        if (bb) {
            callPrivate(bb);
            callProtected();
        } else callPublic(bb);
    }

    private int callPrivate(boolean k) {
        return callPrivate(k);
    }

    private int callPrivate() {
        return callPrivate();
    }

    public int callPublic(boolean k) {
        return callPublic(k);
    }

    protected int callProtected() {
        return callProtected();
    }

}
