package fr.inria.autojmh.instrument.log;

import java.io.*;
import java.util.*;

/**
 * Created by marodrig on 22/12/2014.
 */
public class MicrobenchmarkLogger extends LightLog {

    static byte byte_type = 0;
    static byte short_type = 1;
    static byte int_type = 2;
    static byte long_type = 3;
    static byte float_type = 4;
    static byte double_type = 5;
    static byte char_type = 6;
    static byte String_type = 7;
    static byte boolean_type = 8;

    static byte byte_type_array = 10;
    static byte short_type_array = 11;
    static byte int_type_array = 12;
    static byte long_type_array = 13;
    static byte float_type_array = 14;
    static byte double_type_array = 15;
    static byte char_type_array = 16;
    static byte String_type_array = 17;
    static byte boolean_type_array = 18;

    HashSet<String> varRegistered = new HashSet<String>();

    public OutputStreamProvider getStreamProvider() {
        if (streamProvider == null) streamProvider = new OutputStreamProvider();
        return streamProvider;
    }

    public void setStreamProvider(OutputStreamProvider streamProvider) {
        this.streamProvider = streamProvider;
    }

    OutputStreamProvider streamProvider;

    public MicrobenchmarkLogger(Properties props) {
        super(props);
    }

    private DataOutputStream getStream(String name) {
        return getStreamProvider().getStream(name);
    }

    @Override
    public void flush() {
        super.flush();
        getStreamProvider().flushAndClear();
    }

    public void logSerializable(Serializable data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            ObjectOutputStream out = new ObjectOutputStream(stream);
            out.writeObject(data);
            out.flush();
        } catch (Exception e) {
            System.out.print(e.getCause());
        }
    }

    public void logArray1Serializable(Serializable[] data, String name, boolean after) {
        if (data == null) data = new Serializable[0];
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                try {
                    ObjectOutputStream out = new ObjectOutputStream(stream);
                    out.writeObject(data);
                    out.flush();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void logSerializableCollection(Collection data, String name, boolean after) {
        if (data == null) data = new ArrayList<Serializable>();
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.size());
            for (Object element : data ) {
                try {
                    ObjectOutputStream out = new ObjectOutputStream(stream);
                    out.writeObject(element);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void logbyte(byte data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeByte(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void logArray1byte(byte[] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new byte[0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeByte(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray2byte(byte[][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new byte[0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++)
                    stream.writeByte(data[i][j]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray3byte(byte[][][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new byte[0][0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++) {
                    stream.writeInt(data[i][j].length);
                    for (int k = 0; k < data[i][j].length; k++)
                        stream.writeByte(data[i][j][k]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }


    public void logByteCollection(Collection<Byte> data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new ArrayList<Byte>();
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.size());
            for (Byte element : data ) stream.writeByte(element);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void logshort(short data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeShort(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void logArray1short(short[] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new short[0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeShort(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray2short(short[][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new short[0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++)
                    stream.writeShort(data[i][j]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray3short(short[][][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new short[0][0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++) {
                    stream.writeInt(data[i][j].length);
                    for (int k = 0; k < data[i][j].length; k++)
                        stream.writeShort(data[i][j][k]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }


    public void logShortCollection(Collection<Short> data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new ArrayList<Short>();
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.size());
            for (Short element : data ) stream.writeShort(element);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void logint(int data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void logArray1int(int[] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new int[0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeInt(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray2int(int[][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new int[0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++)
                    stream.writeInt(data[i][j]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray3int(int[][][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new int[0][0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++) {
                    stream.writeInt(data[i][j].length);
                    for (int k = 0; k < data[i][j].length; k++)
                        stream.writeInt(data[i][j][k]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }


    public void logIntegerCollection(Collection<Integer> data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new ArrayList<Integer>();
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.size());
            for (Integer element : data ) stream.writeInt(element);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void loglong(long data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeLong(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void logArray1long(long[] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new long[0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeLong(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray2long(long[][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new long[0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++)
                    stream.writeLong(data[i][j]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray3long(long[][][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new long[0][0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++) {
                    stream.writeInt(data[i][j].length);
                    for (int k = 0; k < data[i][j].length; k++)
                        stream.writeLong(data[i][j][k]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }


    public void logLongCollection(Collection<Long> data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new ArrayList<Long>();
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.size());
            for (Long element : data ) stream.writeLong(element);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void logfloat(float data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeFloat(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void logArray1float(float[] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new float[0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeFloat(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray2float(float[][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new float[0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++)
                    stream.writeFloat(data[i][j]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray3float(float[][][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new float[0][0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++) {
                    stream.writeInt(data[i][j].length);
                    for (int k = 0; k < data[i][j].length; k++)
                        stream.writeFloat(data[i][j][k]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }


    public void logFloatCollection(Collection<Float> data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new ArrayList<Float>();
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.size());
            for (Float element : data ) stream.writeFloat(element);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void logdouble(double data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeDouble(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void logArray1double(double[] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new double[0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeDouble(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray2double(double[][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new double[0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++)
                    stream.writeDouble(data[i][j]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray3double(double[][][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new double[0][0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++) {
                    stream.writeInt(data[i][j].length);
                    for (int k = 0; k < data[i][j].length; k++)
                        stream.writeDouble(data[i][j][k]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }


    public void logDoubleCollection(Collection<Double> data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new ArrayList<Double>();
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.size());
            for (Double element : data ) stream.writeDouble(element);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void logchar(char data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeChar(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void logArray1char(char[] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new char[0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeChar(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray2char(char[][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new char[0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++)
                    stream.writeChar(data[i][j]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray3char(char[][][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new char[0][0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++) {
                    stream.writeInt(data[i][j].length);
                    for (int k = 0; k < data[i][j].length; k++)
                        stream.writeChar(data[i][j][k]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }


    public void logString(String data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeUTF(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void logArray1String(String[] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new String[0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeUTF(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray2String(String[][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new String[0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++)
                    stream.writeUTF(data[i][j]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray3String(String[][][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new String[0][0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++) {
                    stream.writeInt(data[i][j].length);
                    for (int k = 0; k < data[i][j].length; k++)
                        stream.writeUTF(data[i][j][k]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }


    public void logStringCollection(Collection<String> data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new ArrayList<String>();
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.size());
            for (String element : data ) stream.writeUTF(element);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void logboolean(boolean data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeBoolean(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void logArray1boolean(boolean[] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new boolean[0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.writeBoolean(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray2boolean(boolean[][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new boolean[0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++)
                    stream.writeBoolean(data[i][j]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray3boolean(boolean[][][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new boolean[0][0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++) {
                    stream.writeInt(data[i][j].length);
                    for (int k = 0; k < data[i][j].length; k++)
                        stream.writeBoolean(data[i][j][k]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }


    public void logBooleanCollection(Collection<Boolean> data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new ArrayList<Boolean>();
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.size());
            for (Boolean element : data ) stream.writeBoolean(element);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}

