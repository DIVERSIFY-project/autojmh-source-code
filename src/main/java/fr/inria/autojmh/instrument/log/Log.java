package fr.inria.autojmh.instrument.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * User: Simon
 * Date: 7/23/13
 * Time: 10:07 AM
 */
public class Log {

    static Properties props;

    static {
        props = new Properties();
        File storeLog;
        try {
            File fProperties = new File("log/perforation.properties");
            if ( fProperties.exists() ) {
                props.load(new BufferedReader(new FileReader(fProperties)));
                String logFileName = props.getProperty("log.dir", "log");
                int i = 0;
                File f = new File(logFileName);
                while (f.exists()) {
                    f = new File(logFileName + "-" + String.valueOf(i));
                    i++;
                }

                props.put("log.dir", "log/" + f.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<Thread, MicrobenchmarkLogger> logs = null;

    private static Object init = init();

    protected static Object init() {
        ShutdownHookLog shutdownHook = new ShutdownHookLog();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        return null;
    }

    public static MicrobenchmarkLogger getLog() {
        return getLog(Thread.currentThread());
    }

    protected static MicrobenchmarkLogger getLog(Thread thread) {
        if (logs == null) logs = new HashMap<Thread, MicrobenchmarkLogger>();
        if (logs.containsKey(thread)) return logs.get(thread);
        else {
            MicrobenchmarkLogger l = new MicrobenchmarkLogger(props);
            logs.put(thread, l);
            return l;
        }
    }

    public static void close() {
        getLog().close();
        /*
        for (MicrobenchmarkLogger l : logs.values()) {
            l.close();
        }*/
    }
}