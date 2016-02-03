package fr.inria.autojmh.generators;

import fr.inria.autojmh.instrument.DataContextFileChooser;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.snippets.TemplateInputVariable;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.controlflow.AllBranchesReturn;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtCodeSnippetExpressionImpl;
import spoon.support.reflect.declaration.CtMethodImpl;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static fr.inria.autojmh.snippets.TemplateInputVariable.getCompilableName;

/**
 * Created by marodrig on 28/09/2015.
 */
public class MicrobenchmarkGenerator extends BaseGenerator {

    private String dataContextPath;

    private DataContextFileChooser chooser;

    private int generatedCount = 0;

    public int getGeneratedCount() {
        return generatedCount;
    }

    public static class MicroSnippetExpression<T> extends CtCodeSnippetExpressionImpl<T> {

        @Override
        public String toString() {
            return getValue();
        }
    }

    /**
     * Pretty print the snippet, basically substitute static private method for our public of the methods
     * and static fields for our public copy of the static field
     *
     * @param snippet
     * @param extractedMethods
     * @return
     */
    private String snippetPrettyPrint(BenchSnippet snippet, Set<CtMethod> extractedMethods) {

        //An old logic copy and pasted. It works well...

        String snippetStr = snippet.getASTElement().toString();
        snippetStr = snippetStr.replace("\r\n", "\r\n" + PAD_8);
        StringBuilder sb = new StringBuilder();
        /*
        if (degraded) {
            // TO DO: Have into consideration the case where there is only one line (no final })
            sb.append(PAD_8).append(snippetStr.substring(0, snippetStr.length() - 1)).append("\n");//eliminate last "}"
            sb.append(PAD_8).append(snippet.getDegradedSnippet()).append("}\n");
        } else*/
        sb.append(PAD_8).append(snippetStr).append("\n");

        snippetStr = sb.toString();

        for (CtVariableAccess a : snippet.getAccesses()) {
            if (a instanceof CtFieldAccess) {
                CtFieldAccess f = (CtFieldAccess) a;
                if (snippet.getInitialized().contains(f)) {
                    snippetStr = snippetStr.replace(f.toString(), getCompilableName(f, '_'));
                }
            }
        }

        //Replace all invocations by the ones copied and pasted
        for (CtInvocation inv : snippet.getASTElement().getElements(new TypeFilter<CtInvocation>(CtInvocation.class))) {
            CtMethodImpl m = getInvocationMethod(inv);
            if (extractedMethods.contains(m)) {
                String invStr = inv.toString();
                invStr = invStr.substring(0, invStr.lastIndexOf("(") - 1);
                snippetStr = snippetStr.replace(invStr, invStr.replace(".", "_"));
            }
        }

        return snippetStr;
    }

    private CtMethodImpl getInvocationMethod(CtInvocation inv) {
        if (inv.getExecutable().getDeclaration() != null &&
                inv.getExecutable().getDeclaration() instanceof CtMethodImpl)
            return (CtMethodImpl) inv.getExecutable().getDeclaration();
        return null;
    }

    /**
     * Extract private static method out of an statement and copy its body to the microbenchmark
     *
     * @param sb        Output string builder that will contain the body of the method
     * @param statement Statement containing the method invocations.
     */
    private void extractStaticMethod(StringBuilder sb,
                                     CtStatement statement, Set<CtMethod> extractedMethods) {
        //Append all static methods
        List<CtInvocation> methods = statement.getElements(
                new TypeFilter<CtInvocation>(CtInvocation.class));

        if (extractedMethods == null) extractedMethods = new HashSet<>();

        for (CtInvocation inv : methods) {
            if (BenchSnippet.isImplicitThiz(inv)) {
                MicroSnippetExpression thiz = new MicroSnippetExpression();
                thiz.setValue("THIZ");
                inv.setTarget(thiz);
            }
            //Find private static and private methods
            //There is no need to extract public methods since we can directly use them
            CtMethodImpl m = getInvocationMethod(inv);
            if (m == null || extractedMethods.contains(m)) continue;
                //Extract non-public static methods so we can use them
                //public methods need no extraction. They can be used directly
            else if (inv.getExecutable().isStatic() &&
                    (m.getVisibility() == ModifierKind.PRIVATE ||
                            m.getVisibility() == ModifierKind.PROTECTED)) {
                extractedMethods.add(m);

                //Prety print the static declaration of the method with a different name
                CtExecutable ref = inv.getExecutable().getDeclaration();
                sb.append(PAD_4).append("private static ").
                        append(ref.getType().getQualifiedName()).
                        append(" ").
                        append(inv.getExecutable().getDeclaringType().getQualifiedName().replace(".", "_").replace("$", "_")).
                        append("_").
                        append(ref.getSimpleName()).
                        append("(");

                //Print parameters of the method
                CtParameter p = (CtParameter) ref.getParameters().get(0);
                //sb.append(p.getType().getQualifiedName()).append(" ").append(p.getSimpleName());
                sb.append(p.toString());
                for (int i = 1; i < ref.getParameters().size(); i++) {
                    p = (CtParameter) ref.getParameters().get(i);
                    //sb.append(", ").append(p.getType().getQualifiedName()).append(" ").append(p.getSimpleName());
                    sb.append(", ").append(p.toString());
                }
                sb.append(")");
                //Print the body
                String decStr = ref.getBody().toString();
                decStr = decStr.replace("\r\n", "\r\n" + PAD_4);
                sb.append(PAD_4).append(decStr);
                sb.append("\n\n");
                List<CtInvocation> deepMethods =
                        inv.getExecutable().getDeclaration().getBody().getElements(
                                new TypeFilter<CtInvocation>(CtInvocation.class));
                //Recursively add other static methods
                for (CtStatement otherStatic : deepMethods) {
                    extractStaticMethod(sb, otherStatic, extractedMethods);
                }
            }
        }
    }

    /**
     * Generate a bechmark class out of a loop
     *
     * @throws java.io.FileNotFoundException
     */
    public void generate(BenchSnippet snippet) {

        if (!getChooser().existsDataFile(dataContextPath, snippet.getMicrobenchmarkClassName()))
            return;

        //Get static methods
        StringBuilder staticMethodsPrint = new StringBuilder();

        Set<CtMethod> extractedMethods = new HashSet<>();
        extractStaticMethod(staticMethodsPrint, snippet.getASTElement(), extractedMethods);

        //Obtain the list of imports from the variables
        Set<String> imports = new HashSet<>();
        for (TemplateInputVariable v : snippet.getTemplateAccessesWrappers()) {
            String importName = v.getPackageQualifiedName();
            if (!importName.isEmpty() && !imports.contains(importName) && !importName.startsWith("java.lang"))
                imports.add(importName);
        }

        HashMap<String, Object> input = new HashMap<String, Object>();
        input.put("reset_code", snippet.hasResetCode());
        input.put("package_name", packageName);
        input.put("imports", imports);
        input.put("class_comments", "Benchmark auto generated using AutoJMH");
        input.put("class_name", snippet.getMicrobenchmarkClassName());
        input.put("input_vars", snippet.getTemplateAccessesWrappers());
        input.put("bench_method_type", snippet.getBenchMethodReturnType());

        List<CtLocalVariable> bhVars = blackHolesNeeded(snippet);
        if ( bhVars.size() > 0 && input.get("return_statement") == null && input.get("bench_method_type") == "void" ) {
            input.put("bench_method_type", bhVars.get(0).getType().getQualifiedName());
        }
        input.put("return_statement", getDefaultReturn(snippet, bhVars));
        input.put("black_holes_neded", bhVars.size() > 0);
        input.put("black_holes", bhVars);




        //Set the path to the files with the data context
        getChooser().setDataContextPath(dataContextPath);
        try {
            input.put("data_file_path", getChooser().chooseBefore(snippet.getMicrobenchmarkClassName()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        input.put("data_root_folder_path", new File(dataContextPath).getAbsolutePath().replace("\\", "/"));
        //input.put("data_file_path", snippet.getMicrobenchmarkClassName());

        input.put("static_methods", staticMethodsPrint.toString());

        //Code of the snippet
        input.put("snippet_code", snippetPrettyPrint(snippet, extractedMethods));

        //String degradedType = false ? GRACEFULLY_BENCHMARK : ORIGINAL_BENCHMARK;
        //input.put("degraded_type", degradedType);

        generateOutput(input, "micro-benchmark.ftl",
                writeToFile, outputPath + "/" + snippet.getMicrobenchmarkClassName() + "_Benchmark.java");
        generatedCount++;
    }

    /**
     * Indicates if black holes are needed and for which variables
     * <p/>
     * Current analysis have limitations. It only checks that there are assigned local variables not being used further
     * ahead and assumes that the original code did use them.
     * This is limited because it does not ensure to preserve optimizations in the original code.
     * <p/>
     * The correct way should be check they are used in both the original and preserve only if so.
     *
     * @param snippet
     * @return
     */
    private List<CtLocalVariable> blackHolesNeeded(BenchSnippet snippet) {
        return snippet.getASTElement().getElements(new TypeFilter<CtLocalVariable>(CtLocalVariable.class));
    }

    private String getDefaultReturn(BenchSnippet snippet, List<CtLocalVariable> blakHoles) {

        //Check if the return is actually needed
        AllBranchesReturn branchesReturn = new AllBranchesReturn();
        if (branchesReturn.execute(snippet.getASTElement())) {
            if ( blakHoles.size() > 0 ) {
                String s = blakHoles.get(0).getSimpleName();
                blakHoles.remove(0);
                return "return " + s + ";";
            }
            return null;
        }

        //Find the type to return something
        List<CtReturn> returns = snippet.getASTElement().getElements(new TypeFilter<CtReturn>(CtReturn.class));
        String name = null;
        for (CtReturn r : returns) {
            CtExpression exp = returns.get(0).getReturnedExpression();
            if (exp != null) {
                CtTypeReference ref = exp.getType();
                if (ref != null) {
                    name = ref.getQualifiedName();
                    break;
                }
            }
        }
        if (name == null) return null;

        //If we can find a variable that can serve as return
        for (TemplateInputVariable v : snippet.getTemplateAccessesWrappers()) {
            if (v.getVariableTypeName().equals(name)) return "return " + v.getTemplateCodeCompilableName() + ";";
        }

        //Otherwise, just return a constant
        switch (name) {
            case "java.lang.Byte":
                return "return 0; ";
            case "java.lang.Boolean":
                return "return false; ";
            case "java.lang.Character":
                return "return 'a'; ";
            case "java.lang.Double":
                return "return 0.0; ";
            case "java.lang.Float":
                return "return 0.0f; ";
            case "java.lang.Integer":
                return "return 0; ";
            case "java.lang.Long":
                return "return 0; ";
            case "java.lang.Number":
                return "return 0; ";
            case "java.lang.Short":
                return "return 0; ";
            case "java.lang.String":
                return "return 0; ";

            case "byte":
                return "return 0; ";
            case "boolean":
                return "return false; ";
            case "character":
                return "return 'a'; ";
            case "double":
                return "return 0.0; ";
            case "float":
                return "return 0.0f; ";
            case "integer":
                return "return 0; ";
            case "long":
                return "return 0; ";
            case "short":
                return "return 0; ";
            default:
                return "return null;";
        }
    }

    @Override
    public void configure(AJMHConfiguration configuration) {
        super.configure(configuration);
        this.dataContextPath = configuration.getDataContextPath();
        outputPath = outputPath + "/src/main/java/" + packageName.replace(".", "/");
    }

    @Override
    public void generate() {
        for (BenchSnippet snippet : getSnippets()) {
            generate(snippet);
        }
    }

    public DataContextFileChooser getChooser() {
        if (chooser == null)
            chooser = new DataContextFileChooser(dataContextPath);
        return chooser;
    }

    public void setChooser(DataContextFileChooser chooser) {
        this.chooser = chooser;
    }
}

