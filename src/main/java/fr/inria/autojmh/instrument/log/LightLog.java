package fr.inria.autojmh.instrument.log;

import java.io.*;
import java.util.Properties;
//DO NOT REMOVE THIS STATIC IMPORT!
import static java.lang.System.lineSeparator;

/**
 * Verbose log for humans to understand
 * <p/>
 * Created by marodrig on 25/06/2014.
 */
public class LightLog {

    ///File writer for each thread. Each one saved in a different file
    private PrintWriter fileWriters;

    private StringBuilder stringBuilders;

    protected String logFileName = "log";

    protected String logThreadName = "";

    protected File dir;

    protected Properties properties;

    public LightLog(Properties properties) {
        this(Thread.currentThread(), properties);
    }

    public LightLog() {
        this(Thread.currentThread(), new Properties());
    }

    public LightLog(Thread thread) {
        this(thread, new Properties());
    }

    /**
     * Constructor of the verbose log
     */
    public LightLog(Thread thread, Properties properties) {
        logFileName = properties.getProperty("log.dir", "log");
        dir = new File(logFileName);
        dir.mkdir();
        logFileName = getThreadLogFilePath(thread);
        stringBuilders = new StringBuilder();
        logThreadName = getThreadFileName(thread);
    }

    /**
     * Entry with Id
     *
     * @param type   Type of the entry
     * @param id     ID of the entry
     * @param params Params to the entry

    public void entryWithId(String type, int id, Object... params) {
        entryCustomMillis(type, System.currentTimeMillis(), id, params);
    }*/

    /**
     * Logs an entry without id
     *
     * @param type   Type of the entry
     * @param params params of the entry
    public void entryWhithoutId(String type, Object... params) {
        stringBuilders.append(type).append(";").append(System.currentTimeMillis()).append(";");
        if (params != null && params.length > 0) {
            log(params);
        } else stringBuilders.append(System.lineSeparator());
    }*/

    /**
     * Log an entry with custom millis
     *
     * @param type   Type of the entry
     * @param millis Millis of the entry
     * @param id     Id of the entry
     * @param params params of the entry

    public void entryCustomMillis(String type, long millis, int id, Object... params) {
        stringBuilders.append(type).append(";").append(millis).append(";").append(id).append(";");
        if (params != null && params.length > 0) {
            log(params);
        } else stringBuilders.append(System.lineSeparator());
    }*/


    /**
     * Create a list of semicolon separated data
     *
     * @param params

    private void log(Object... params) {
        StringBuilder p = stringBuilders;
        for (int i = 0; i < params.length; i++) {
            p.append(params[i].toString());
            if (i < params.length - 1) p.append(";");
        }
        p.append(System.lineSeparator());
    }*/

    /**
     * Flush all data to file
     */
    public void flush() {
        if (stringBuilders.length() > 0) {
            //Write to file only if needed
            try {
                if (fileWriters == null)
                    fileWriters = new PrintWriter(new BufferedWriter(new FileWriter(logFileName), 1024 * 8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            fileWriters.append(stringBuilders);
            fileWriters.flush();
        }
        resetStringBuilder();
    }

    /**
     * Close file
     */
    public void close() {
        try {
            flush();
            if (fileWriters != null) fileWriters.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the loggin path for the current thread
     *
     * @param thread Thread to log
     * @return The path with the log file
     */
    public String getThreadLogFilePath(Thread thread) {
        return dir.getAbsolutePath() + "/" + getThreadFileName(thread);
    }


    /**
     * Returns the file name of the file where this thread's log is being stored
     *
     * @param thread
     * @return Relative filename of the file where this thread's log is being stored
     */
    protected String getThreadFileName(Thread thread) {
        return "log" + thread.getName() + "_" + this.getClass().getClassLoader().toString().split("@")[1] + System.currentTimeMillis();
    }

    protected synchronized void resetStringBuilder() {
        stringBuilders = new StringBuilder();
    }

}
