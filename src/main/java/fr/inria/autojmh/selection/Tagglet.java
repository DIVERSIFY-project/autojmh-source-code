package fr.inria.autojmh.selection;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;

/**
 * A class that represents a tagglet obtained from file
 * <p/>
 * Created by marodrig on 28/10/2015.
 */
public class Tagglet {

    /**
     * Builds the Tagglet
     *
     * @param kind         Kind of the tablet
     * @param lineNumber   Line where the tablet was found
     * @param columnNumber Column where the tablet was found
     * @param className    Name of the class where the taglet was found
     */
    public Tagglet(TaggletKind kind, int lineNumber, int columnNumber, String className) {
        this.kind = kind;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.className = className;
    }

    public enum TaggletKind {BENCH_THIS, BENCH_UNTIL}

    /**
     * Kind of the tagglet
     */
    private TaggletKind kind;

    /**
     * Number of the line where the tagglet was found
     */
    private int lineNumber = -1;

    /**
     * Column number where it was found
     */
    private int columnNumber = -1;

    /**
     * Name of the class where it was found
     */
    public String className = "";

    /**
     * Calculate the distance to an statement
     *
     * @param statement Statement to calculate distance to
     * @return The number of lines separating the tagglet and the statement or Integer.MAX_VALUE if they are not in the
     * same file.
     */
    public int distanceToStatement(CtElement statement) {
        String className = statement.getPosition().getCompilationUnit().
                getMainType().getQualifiedName();
        if (className.equals(className))
            return statement.getPosition().getLine() - lineNumber;
        return Integer.MAX_VALUE;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public TaggletKind getKind() {
        return kind;
    }

    public void setKind(TaggletKind kind) {
        this.kind = kind;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return getKind() + "-" + getClassName() + ":" + getLineNumber() + "," + getColumnNumber();
    }
}
