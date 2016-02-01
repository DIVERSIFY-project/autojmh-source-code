package fr.inria.autojmh.generators;

/**
 * Created by marodrig on 03/01/2016.
 */
public class CollectionType {

    /**
     * Name of the type
     */
    String name;

    /**
     * Name of the collection type that can be actually instantiated
     */
    String concreteName;

    public CollectionType(String name, String concreteName) {
        this.name = name;
        this.concreteName = concreteName;
    }

    public String getConcreteName() {
        return concreteName;
    }

    public String getName() {
        return name;
    }
}
