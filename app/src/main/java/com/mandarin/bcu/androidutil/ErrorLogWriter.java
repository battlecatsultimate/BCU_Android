package com.mandarin.bcu.androidutil;

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
}
