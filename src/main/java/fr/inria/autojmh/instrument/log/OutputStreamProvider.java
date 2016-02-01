package fr.inria.autojmh.instrument.log;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

/**
 * Given a id, provides a stream
 *
 * Created by marodrig on 30/12/2015.
 */
public class OutputStreamProvider {

    HashMap<String, DataOutputStream> data = new HashMap<String, DataOutputStream>();

    public void flushAndClear() {
        try {
            for (DataOutputStream s : data.values()) {
                s.flush();
                s.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        data.clear();
    }

    public DataOutputStream getStream(String name) {
        name = name.substring(0, name.lastIndexOf("-")) + "--" +
                Thread.currentThread().getName() + "-" +
                this.getClass().getClassLoader().toString();
        if (data.containsKey(name)) return data.get(name);
        try {
            DataOutputStream stream = new DataOutputStream(new FileOutputStream("log/" + name));
            data.put(name, stream);
            return stream;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
