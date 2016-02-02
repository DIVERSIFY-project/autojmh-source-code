package fr.inria.autojmh.generators.reset;

import fr.inria.autojmh.instrument.DataContextResolver;
import spoon.reflect.code.CtVariableAccess;

/**
 * A class that returns a reset generator depending on the type of the variable requiresting it
 *
 * Created by marodrig on 02/02/2016.
 */
public class ResetFactory {

    PrimitiveReset primitives = new PrimitiveReset();
    PrimitiveCollectionReset primitiveCollection = new PrimitiveCollectionReset();

    public ResetGenerator fetchGenerator(CtVariableAccess a) {
        if (a.getType().isPrimitive()) {
            return primitives;
        } else if (DataContextResolver.isCollection(a.getType())) {
            return primitiveCollection;
        } else throw new UnsupportedOperationException();
    }



    public boolean canProvide(CtVariableAccess a) {
        return a.getType().isPrimitive() || DataContextResolver.isCollection(a.getType());
    }
}
