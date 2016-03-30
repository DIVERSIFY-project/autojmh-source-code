package fr.inria.autojmh.tool;

import fr.inria.autojmh.snippets.BenchSnippet;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

/**
 * Cleans up instrumentation statements
 *
 * Created by marodrig on 27/03/2016.
 */
public class InstrumentationCleaner {

    public static final String INSTRUMENTATION_PACKAGE = "fr.inria.autojmh";

    /**
     * Cleans up the whole list of snippets
     */
    public void cleanUp(List<BenchSnippet> snippets) {
        for (BenchSnippet s : snippets) {
            cleanStatement(s.getASTElement());
            for (CtReturn r : s.getASTElement().getElements(
                    new TypeFilter<CtReturn>(CtReturn.class))) {
                cleanStatement(r);
            }
            //Remove some that may remain due to malformations of the AST
            CtElement parent = s.getASTElement().getParent();
            for ( CtCodeSnippetStatement st : parent.getElements(
                    new TypeFilter<CtCodeSnippetStatement>(CtCodeSnippetStatement.class))) {
                st.setValue("//REMOVE");
            }
        }
    }

    /**
     * Cleans up one statement
     */
    private void cleanStatement(CtStatement st) {
        if (st.getParent() instanceof CtBlock) {
            CtBlock block = (CtBlock) st.getParent();
            int k = 0;
            while (k < block.getStatements().size()){
                CtStatement blockSt = block.getStatement(k);
                if (blockSt instanceof CtCodeSnippetStatement) {
                    CtCodeSnippetStatement snippet = (CtCodeSnippetStatement) blockSt;
                    if (snippet.getValue().equals("//REMOVE") ||
                            snippet.getValue().startsWith(INSTRUMENTATION_PACKAGE))
                        block.removeStatement(blockSt);
                    else k++;
                } else k++;
            }
        }
    }

}
