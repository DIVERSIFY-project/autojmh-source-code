package fr.inria.autojmh.generators.transformations;

import fr.inria.autojmh.snippets.BenchSnippet;
import spoon.reflect.code.CtStatement;

/**
 * Abstract class for all transformations performed over the snippet
 *
 * Every transformation will do at leas one of the following:
 * 1- Modify the snippet
 * 2- Generate extra code that goes inside the class
 *
 * Created by marodrig on 23/03/2016.
 */
public abstract class AbstractTransformation implements Transformation {

    //Transformed Snippet to pass to the transformation chain
    protected CtStatement transformed;

    protected String generatedCode;

    protected String modifiedSnippetCode;

}
