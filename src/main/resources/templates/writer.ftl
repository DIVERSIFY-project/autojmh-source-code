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
            throw new RuntimeException(e);
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

<#list types as type>
    public void log${type.name}(${type.name} data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.write${type.method}(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void logArray1${type.name}(${type.name}[] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new ${type.name}[0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) stream.write${type.method}(data[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray2${type.name}(${type.name}[][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new ${type.name}[0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++)
                    stream.write${type.method}(data[i][j]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

    public void logArray3${type.name}(${type.name}[][][] data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new ${type.name}[0][0][0];
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                stream.writeInt(data[i].length);
                for (int j = 0; j < data[i].length; j++) {
                    stream.writeInt(data[i][j].length);
                    for (int k = 0; k < data[i][j].length; k++)
                        stream.write${type.method}(data[i][j][k]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        varRegistered.add(name);
    }

  <#if type.primitiveClassName != "Char">

    public void log${type.primitiveClassName}Collection(Collection<${type.primitiveClassName}> data, String name, boolean after) {
        if (after) name = "after-" + name;
        if (varRegistered.contains(name)) return;
        if (data == null) data = new ArrayList<${type.primitiveClassName}>();
        varRegistered.add(name);
        try {
            DataOutputStream stream = getStream(name);
            stream.writeInt(data.size());
            for (${type.primitiveClassName} element : data ) stream.write${type.method}(element);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

  </#if>

</#list>

}