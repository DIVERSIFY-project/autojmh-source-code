package fr.inria.testproject.realcases;

import java.io.Serializable;
import java.util.List;

/**
 * Private extracted methods cannot be marked as 'Override'
 * <p>
 * A class to represent specific cases we found to be trouble some while extracting microbenchmarks
 */
public class RemoveOverrideCase extends AdenseArrayMatrixCase {

    @Override
    public int getArrayOffset() {
        return 0;
    }

    @Override
    protected void doSomething(String s) {
        System.out.print(s + "Yeah!!"  + getArrayOffset());
    }

    public void printStrings(List<String> stringList) {
        for ( String s : stringList )
            doSomething(s);
    }

}


