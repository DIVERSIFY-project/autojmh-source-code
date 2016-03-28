package fr.inria.autojmh.generators.microbenchmark.reset;

/**
 * Created by marodrig on 02/02/2016.
 */
public interface ResetGenerator<T> {

    /**
     * Generate code to reset a variable from an object value
     * @param varName Name of the variable
     * @param object  Object to obtain the data from
     * @return
     */
    String resetCode(String varName, T object);

    /**
     * Generate code to reset a variable from another variable
     * @param varName Name of the variable being reset
     * @param otherVar Name of the variable we are going to take data to reset from
     * @return The reset string code
     */
    String resetFromAnotherVar(String varName, String otherVar);
}