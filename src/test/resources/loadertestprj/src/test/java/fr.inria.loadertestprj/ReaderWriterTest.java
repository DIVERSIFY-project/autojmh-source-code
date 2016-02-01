package fr.inria.loadertestprj;

import fr.inria.autojmh.instrument.log.MicrobenchmarkLogger;
import fr.inria.autojmh.instrument.log.OutputStreamProvider;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;

import java.io.*;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;

/**
 * Created by marodrig on 30/12/2015.
 */
public class ReaderWriterTest {

    public static class MockInStreamProvider extends Loader.InputStreamProvider {

        DataInputStream in;

        public MockInStreamProvider(MicrobenchmarkLogger logger) {
            ByteArrayInputStream bis = new ByteArrayInputStream(
                    ((MockOutStreamProvider) logger.getStreamProvider()).bos.toByteArray());
            in = new DataInputStream(bis);
        }

        @Override
        public DataInputStream getStream(String path, String dataFile) {
            return in;
        }
    }

    public static class MockOutStreamProvider extends OutputStreamProvider {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        DataOutputStream stream = new DataOutputStream(bos);

        @Override
        public DataOutputStream getStream(String name) {
            return stream;
        }
    }

    private MicrobenchmarkLogger buildLogger() {
        MicrobenchmarkLogger logger = new MicrobenchmarkLogger(new Properties());
        logger.setStreamProvider(new MockOutStreamProvider());
        return logger;
    }

    @Test
    public void testIOByte() throws IOException {
        MicrobenchmarkLogger logger = buildLogger();
        byte b = 34;
        logger.logbyte(b, "name", false);

        Loader loader = new Loader();
        loader.setStreamProvider(new MockInStreamProvider(logger));
        loader.openStream("", "");
        byte b1 = loader.readbyte();
        assertEquals(b, b1);
    }

    @Test
    public void testIOByteArray1() throws IOException {
        MicrobenchmarkLogger logger = buildLogger();
        byte[] a = new byte[]{34, 9, 0};
        logger.logArray1byte(a, "name", false);

        Loader loader = new Loader();
        loader.setStreamProvider(new MockInStreamProvider(logger));
        loader.openStream("", "");
        byte[] b = loader.readArray1byte();

        assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) assertEquals(a[i], b[i]);
    }

    @Test
    public void testIOByteArray2() throws IOException {
        MicrobenchmarkLogger logger = buildLogger();
        byte[] a1 = new byte[]{31, 91, 10};
        byte[] a2 = new byte[]{33, 92, 20};
        byte[][] a = new byte[][]{a1, a2};
        logger.logArray2byte(a, "name", false);

        Loader loader = new Loader();
        loader.setStreamProvider(new MockInStreamProvider(logger));
        loader.openStream("", "");
        byte[][] b = loader.readArray2byte();

        assertEquals(a.length, b.length);
        assertEquals(a[0].length, b[0].length);
        assertEquals(a[1].length, b[1].length);
        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < a[i].length; j++) assertEquals(a[i][j], b[i][j]);
    }

    @Test
    public void testIOByteArray3() throws IOException {
        MicrobenchmarkLogger logger = buildLogger();
        byte[] a1 = new byte[]{31, 91, 10};
        byte[] a2 = new byte[]{32, 92, 20};
        byte[] a3 = new byte[]{33, 93, 30};
        byte[] a4 = new byte[]{34, 94, 40};
        byte[][] a11 = new byte[][]{a1, a2};
        byte[][] a21 = new byte[][]{a1, a2};
        byte[][][] a = new byte[][][]{a11, a21};
        logger.logArray3byte(a, "name", false);

        Loader loader = new Loader();
        loader.setStreamProvider(new MockInStreamProvider(logger));
        loader.openStream("", "");
        byte[][][] b = loader.readArray3byte();

        assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i].length, b[i].length);
            for (int j = 0; j < a[i].length; j++) {
                assertEquals(a[i][j].length, b[i][j].length);
                for (int k = 0; k < a[i][j].length; k++)
                    assertEquals(a[i][j][k], b[i][j][k]);
            }
        }
    }

    @Test
    public void testIOFloatArray2() throws IOException {
        MicrobenchmarkLogger logger = buildLogger();
        float[] a1 = new float[]{31, 91, 10};
        float[] a2 = new float[]{33, 92, 20};
        float[][] a = new float[][]{a1, a2};
        logger.logArray2float(a, "name", false);

        Loader loader = new Loader();
        loader.setStreamProvider(new MockInStreamProvider(logger));
        loader.openStream("", "");
        float[][] b = loader.readArray2float();

        assertEquals(a.length, b.length);
        assertEquals(a[0].length, b[0].length);
        assertEquals(a[1].length, b[1].length);
        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < a[i].length; j++) assertEquals(a[i][j], b[i][j]);
    }

    @Test
    public void testIOFloatArray3() throws IOException {
        MicrobenchmarkLogger logger = buildLogger();
        float[] a1 = new float[]{31.3f, 91.5f, 10.12f};
        float[] a2 = new float[]{32.3f, 92.5f, 20.12f};
        float[] a3 = new float[]{33.3f, 93.5f, 30.12f};
        float[] a4 = new float[]{34.3f, 94.5f, 40.12f};
        float[][] a11 = new float[][]{a1, a2};
        float[][] a21 = new float[][]{a1, a2};
        float[][][] a = new float[][][]{a11, a21};
        logger.logArray3float(a, "name", false);

        Loader loader = new Loader();
        loader.setStreamProvider(new MockInStreamProvider(logger));
        loader.openStream("", "");
        float[][][] b = loader.readArray3float();

        assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i].length, b[i].length);
            for (int j = 0; j < a[i].length; j++) {
                assertEquals(a[i][j].length, b[i][j].length);
                for (int k = 0; k < a[i][j].length; k++)
                    assertEquals(a[i][j][k], b[i][j][k]);
            }
        }
    }

    @Test
    public void testIOFloatArray1() throws IOException {
        MicrobenchmarkLogger logger = buildLogger();
        float[] a = new float[]{34.0f, 9.0f, 0.98f};
        logger.logArray1float(a, "name", false);

        Loader loader = new Loader();
        loader.setStreamProvider(new MockInStreamProvider(logger));
        loader.openStream("", "");
        float[] b = loader.readArray1float();

        assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) assertEquals(a[i], b[i]);
    }

    @Test
    public void testIOFloat() throws IOException {
        MicrobenchmarkLogger logger = buildLogger();
        float b = 34.90f;
        logger.logfloat(b, "name", false);

        Loader loader = new Loader();
        loader.setStreamProvider(new MockInStreamProvider(logger));
        loader.openStream("", "");
        float b1 = loader.readfloat();
        assertEquals(b, b1);
    }
}
