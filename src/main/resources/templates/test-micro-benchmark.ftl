<#--
Description:
  This is the template to generate the unit test for a given benchmarks.
  
  You may modify this template to your needs and make AutoJMH use it by configuring the value of the
  test-benchmark-template variable setting the path to your new template.
Author:
  Marcelino Rodriguez-Cancio
-->


<#macro assertions>
<#list input_vars as input_var>
  <#if input_var.initialized == true >
        ${input_var.variableTypeName} ${input_var.variableName} = ${class_name}_s.read${input_var.loadMethodName}();
      <#if input_var.isArray == true >
        assertArraysEquals(${input_var.variableName}, benchmark.${input_var.variableName});
      <#else>
        assertEquals(${input_var.variableName}, benchmark.${input_var.variableName});
      </#if>
  </#if>
</#list>
</#macro>

package ${package_name};

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class ${class_name}Test {

    static final String DATA_ROOT_FOLDER = "${data_root_folder_path}";
    static final String DATA_FILE = "${data_file_path}";

    private void assertArraysEquals(double[] a, double[] b) { assertArrayEquals(a, b, 0.00001d); }
    private void assertArraysEquals(float[] a, float[] b) { assertArrayEquals(a, b, 0.00001f); }

    <#list types as type>
    private void assertArraysEquals(${type}[] a, ${type}[] b) { assertArrayEquals(a, b); }
    </#list>

    private void assertArraysEquals(boolean[] a, boolean[] b) {
        assertEquals(a.length, b.length);
        for ( int i = 0; i < a.length; i++ )
            assertEquals(a[i], b[i]);
    }

    @Rule
    public Timeout globalTimeout= new Timeout(2000);

    @Test
    public void testOriginal() throws Exception {
        Loader ${class_name}_s = new Loader();
        ${class_name}_s.openStream(DATA_ROOT_FOLDER, DATA_FILE);

        ${class_name}_Benchmark benchmark =
            new ${class_name}_Benchmark();
        benchmark.setup();
        benchmark.doBenchmark();

<@assertions/>

        ${class_name}_s.closeStream();

    }

}
