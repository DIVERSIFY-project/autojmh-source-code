package fr.inria.autojmh.generators;

/**
* Created by marodrig on 20/11/2015.
*/
public class StreamType {

    public String getName() {
        return name;
    }

    public String getMethod() {
        return method;
    }

    /**
     * Name of the Type
     */
    String name;
    /**
     * Name of the method used to store this type
     */
    String method;

    /**
     * Name of the equivalent class for the primitive type.
     */
    String primitiveClassName;

    public String getPrimitiveClassName() {
        return primitiveClassName;
    }

    public StreamType(String name, String method) {
        this.name = name;
        this.method = method;
    }

    public StreamType(String name, String method, String primitiveClassName) {
        this.name = name;
        this.method = method;
        this.primitiveClassName = primitiveClassName;
    }
}
