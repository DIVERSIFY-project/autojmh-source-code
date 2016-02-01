<#--
Description:
  This is the template to generate the Loader class to collect the data context.

  You may modify this template to your needs and make AutoJMH use it by configuring the value of the
  loader-template variable setting the path to your new template.
Author:
  Marcelino Rodriguez-Cancio
-->

package ${package_name};

import java.io.*;
import java.util.*;

public class Loader {
    /**
     * Given a id, provides a stream
     */
    public static class InputStreamProvider {
        /**
         * Returns a data stream
         * @param path
         * @param dataFile
         * @return
         */
        public DataInputStream getStream(String path, final String dataFile) {
            try {
                File f = new File(path + "/" + dataFile);
                return new DataInputStream(new BufferedInputStream(
                        new FileInputStream(f), 1024));
            } catch (IOException e) {
                //Don't handle much, if something goes wrong is not this loop's fault
                throw new RuntimeException(e);
            }
        }
    }


<#list types as type>
  <#if type.primitiveClassName != "Char">
    <#list collectionTypes as collType>

    public ${collType.name} < ${type.primitiveClassName} > read${type.primitiveClassName}${collType.name}() {
        ${collType.name} < ${type.primitiveClassName} > result = new ${collType.concreteName} < ${type.primitiveClassName} >();
        ${type.name}[] nn = readArray1${type.name}();
        for ( ${type.name} v : nn ) result.add(v);
        return result;
    }

    </#list>
  </#if>

    public ${type.name}[] readArray1${type.name}() {
        try {
            int length = openStream.readInt();
            ${type.name}[] result = new ${type.name}[length];
            for (int k = 0; k < length; k++) result[k] = openStream.read${type.method}();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ${type.name}[][][] readArray3${type.name}() {
        try {
            int length = openStream.readInt();
            ${type.name}[][][] result = new ${type.name}[length][][];
            for (int k = 0; k < length; k++) {
                int kSize = openStream.readInt();
                result[k] = new ${type.name}[kSize][];
                for (int i = 0; i < kSize; i++) {
                    int iSize = openStream.readInt();
                    result[k][i] = new ${type.name}[iSize];
                    for (int j = 0; j < iSize; j++)
                        result[k][i][j] = openStream.read${type.method}();
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ${type.name}[][] readArray2${type.name}() {
        try {
            int length = openStream.readInt();
            ${type.name}[][] result = new ${type.name}[length][];
            for (int k = 0; k < length; k++) {
                int kSize = openStream.readInt();
                result[k] = new ${type.name}[kSize];
                for (int i = 0; i < kSize; i++)
                    result[k][i] = openStream.read${type.method}();
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ${type.name} read${type.name}() {
        try {
            return openStream.read${type.method}();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

</#list>

    public <T> T readSerializable() {
        try {
            ObjectInputStream ois = new ObjectInputStream(openStream);
            return (T)ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

<#list collectionTypes as collType>

    public <T> ${collType.name} < T > readSerializable${collType.name}() {

        try {
            ${collType.name} < T > result = new ${collType.concreteName} < T >();
            T[] nn = readArray1Serializable();
            for ( T v : nn ) result.add(v);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
</#list>

    public <T> T[] readArray1Serializable() {
        try {
            int length = openStream.readInt();
            Object[] result = new Object[length];
            for (int k = 0; k < length; k++) {
                ObjectInputStream ois = new ObjectInputStream(openStream);
                result[k] = ois.readObject();
            }
            return (T[])result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stream from which all data will be read
     */
    private DataInputStream openStream;

    /**
     * Input stream provider
     */
    private InputStreamProvider provider;

    public void setStreamProvider(InputStreamProvider provider) {
        this.provider = provider;
    }

    public InputStreamProvider getStreamProvider() {
        if ( provider == null ) provider = new InputStreamProvider();
        return provider;
    }

    /**
     * Returns a data stream
     * @param path Path to where all the files are now
     * @param dataFile Name of the single file representing this case
     * @return A stream to that file
     */
    public void openStream(String path, final String dataFile) {
        openStream = getStreamProvider().getStream(path, dataFile);
    }

    public void closeStream() {
        try {
            openStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void assertDoubleArrayEquals(double[] data, double[] data1) {

    }
}
