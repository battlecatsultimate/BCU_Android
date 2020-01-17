package com.mandarin.bcu.androidutil.io;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ErrorLogWriter implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler errors;
    private String path;

    public ErrorLogWriter(String path) {
        this.path = path;
        this.errors = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        final Writer stringbuff = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringbuff);
        e.printStackTrace(printWriter);
        String stacktrace = stringbuff.toString();
        printWriter.close();

        if(path != null) {
            writeToFile(stacktrace);
        }

        errors.uncaughtException(t,e);
    }

    private void writeToFile(String current) {
        try {
            File f = new File(path);

            if(!f.exists()) {
                f.mkdirs();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
            Date date = new Date();
            String name = dateFormat.format(date)+".txt";

            File file = new File(path,name);

            if(!file.exists())
                f.createNewFile();

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.append(current);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void WriteLog(Exception error) {
        try {
            String path = Environment.getExternalStorageDirectory().getPath()+"/BCU/logs/";
            File f = new File(path);

            if(!f.exists()) {
                f.mkdirs();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
            Date date = new Date();
            String name = dateFormat.format(date)+".txt";

            File file = new File(path,name);

            final Writer stringbuff = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringbuff);

            error.printStackTrace(printWriter);

            if(!file.exists())
                file.createNewFile();
            else {
                file = new File(path,GetExistingFileName(path,name));
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.append(printWriter.toString());
            fileWriter.flush();
            fileWriter.close();
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String GetExistingFileName(String path, String name) {
        boolean decided = false;

        int exist = 1;

        String nam = name+"-"+exist;

        while(!decided) {
            File f = new File(path,nam);

            if(!f.exists())
                return nam;
            else {
                exist++;
                nam = name + "-" + exist;
            }

            decided = true;
        }

        return nam;
    }
}
