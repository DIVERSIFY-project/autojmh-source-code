package fr.inria.autojmh.generators;

//import fr.inria.autojmh.generators.transformations.StaticMethodCalls;
import fr.inria.autojmh.generators.transformations.DecoratorsReplacement;
import fr.inria.autojmh.instrument.DataContextFileChooser;
import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.snippets.TemplateInputVariable;
import fr.inria.autojmh.tool.AJMHConfiguration;
import fr.inria.controlflow.AllBranchesReturn;
import spoon.reflect.code.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtCodeSnippetExpressionImpl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * Generate a bechmark class out of a loop
     *
     * @throws java.io.FileNotFoundException
     */
    public void generate(BenchSnippet snippet) {

        if (!getChooser().existsDataFile(dataContextPath, snippet.getMicrobenchmarkClassName()))
            return;


        //Obtain the list of imports from the variables
        Set<String> imports = new HashSet<>();
        for (TemplateInputVariable v : snippet.getTemplateAccessesWrappers()) {
            String importName = v.getPackageQualifiedName();
            if (!importName.isEmpty() && !imports.contains(importName) && !importName.startsWith("java.lang"))
                imports.add(importName);
        }

        HashMap<String, Object> input = new HashMap<String, Object>();
        input.put("package_name", packageName);
        input.put("imports", imports);
        input.put("class_comments", "Benchmark auto generated using AutoJMH");
        input.put("class_name", snippet.getMicrobenchmarkClassName());
        input.put("input_vars", snippet.getTemplateAccessesWrappers());
        input.put("bench_method_type", snippet.getBenchMethodReturnType());
        input.put("return_statement", getDefaultReturn(snippet));

        //Set the path to the files with the data context
        getChooser().setDataContextPath(dataContextPath);
        try {
            input.put("data_file_path", getChooser().chooseBefore(snippet.getMicrobenchmarkClassName()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        input.put("data_root_folder_path", new File(dataContextPath).getAbsolutePath().replace("\\", "/"));
        //input.put("data_file_path", snippet.getMicrobenchmarkClassName());

        DecoratorsReplacement decorators = new DecoratorsReplacement();
        decorators.transform(snippet);

        //Input the static methods
        input.put("static_methods", decorators.getGeneratedCode());
        //Code of the snippet
        input.put("snippet_code", decorators.getModifiedSnippetCode());

        //String degradedType = false ? GRACEFULLY_BENCHMARK : ORIGINAL_BENCHMARK;
        //input.put("degraded_type", degradedType);

        generateOutput(input, "micro-benchmark.ftl",
                writeToFile, outputPath + "/" + snippet.getMicrobenchmarkClassName() + "_Benchmark.java");
        generatedCount++;
    }

    private String getDefaultReturn(BenchSnippet snippet) {
        //Check if the return is actually needed
        AllBranchesReturn branchesReturn = new AllBranchesReturn();
        if (branchesReturn.execute(snippet.getASTElement())) return null;

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

