<#--
Description:
  This is the template to generate the MAIN class that will launch all generated benchmarks.

  You may modify this template to your needs and make AutoJMH use it by configuring the value of the
  main-micro-benchmark-template variable setting the path to your new template.
Author:
  Marcelino Rodriguez-Cancio
-->

package ${package_name};

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()

        <#list snippets as snippet>
            <#if generator.existsDataFile(data_path, snippet.microbenchmarkClassName) >
                .include(${snippet.microbenchmarkClassName}_Benchmark.class.getSimpleName())
            </#if>
        </#list>
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .shouldFailOnError(true)
                .build();

        Collection<RunResult> results = new Runner(opt).run();
    }
}