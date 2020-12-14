package com.callrecorder.payamgostar;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Created by j.amini on 11/14/2017.
 */

public class Logger {

    private static final String LOG_FILE_NAME = "CallRecorderLog.txt";

    private static BufferedWriter getLogFile() throws IOException {
        File Root = Environment.getExternalStorageDirectory();
        if (Root.canWrite()) {
            File LogFile = new File(Root, LOG_FILE_NAME);
            boolean fileExist = LogFile.exists();
            FileWriter LogWriter = new FileWriter(LogFile, true);
            BufferedWriter out = new BufferedWriter(LogWriter);

            if (!fileExist) {
                Date date = new Date();
                out.write("Logged at" + String.valueOf(date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "\n"));
            }

            return out;
        }

        return null;
    }

    private static boolean logFileExist() {
        File Root = Environment.getExternalStorageDirectory();
        if (Root.canWrite()) {
            File logFile = new File(Root, LOG_FILE_NAME);
            return logFile.exists();
        }
        return false;
    }

    private static void writeToFile(String message) {
        try {
            BufferedWriter out = getLogFile();
            if (out != null) {
                Date date = new Date();
                out.write(String.valueOf(date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds()) + ":" + message + "\n");
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearLogs() {
        File Root = Environment.getExternalStorageDirectory();
        File logFile = new File(Root, LOG_FILE_NAME);
        if (logFile.exists())
            logFile.delete();
    }


    public static final void d(String tag, String message) {
        Log.d(tag, message);
        writeToFile("tag:" + tag + ": " + message);
    }

    public static final void e(String tag, String message) {
        Log.e(tag, message);
    }

    public static final void i(String tag, String message) {
        Log.i(tag, message);
        writeToFile("tag:" + tag + ": " + message);
    }

    public static final void printStackTrace(Throwable throwable) {
        if (!logFileExist())
            throwable.printStackTrace();
        String stackTrace = Log.getStackTraceString(throwable);
        writeToFile(stackTrace);
    }
}
