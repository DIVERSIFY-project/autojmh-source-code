package fr.inria.testproject.realcases;

import java.io.Serializable;
import java.util.List;

/**
 * Test that methods in not allowed types are not passed the initial THIZ
 * <p>
 * A class to represent specific cases we found to be trouble some while extracting microbenchmarks
 */
public class ThisNotAllowed {

    public int getArrayOffset() {
        return 0;
    }

    protected void doSomething(String s) {
        System.out.print(s + "Yeah!!"  + getArrayOffset());
    }

    public void printStrings(List<String> stringList) {
        for ( String s : stringList )
            doSomething(s);
    }

}


