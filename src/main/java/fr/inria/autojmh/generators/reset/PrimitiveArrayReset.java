package fr.inria.autojmh.generators.reset;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;

/**
 * Generates the reset code for a collection of primitives
 * <p/>
 * Created by marodrig on 02/02/2016.
 */
public class PrimitiveArrayReset implements ResetGenerator<ArrayDescriptor> {

    @Override
    public String resetCode(String varName, ArrayDescriptor object) {
        throw new NotImplementedException();
    }

    @Override
    public String resetFromAnotherVar(String varName, String otherVar) {
        throw new NotImplementedException();
    }
}
