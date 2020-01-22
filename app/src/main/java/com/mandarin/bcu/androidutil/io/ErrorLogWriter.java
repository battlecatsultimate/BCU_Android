package com.mandarin.bcu.androidutil.io;

import android.os.Build;
import android.os.Environment;

import com.mandarin.bcu.androidutil.StaticStore;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ErrorLogWriter implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler errors;
    private String path;
    private boolean upload;

    public ErrorLogWriter(String path, boolean upload) {
        this.path = path;
        this.errors = Thread.getDefaultUncaughtExceptionHandler();
        this.upload = upload;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (path != null) {
            writeToFile(e);
        }

        errors.uncaughtException(t, e);
    }

    private void writeToFile(Throwable e) {
        try {
            File f = new File(path);

            if (!f.exists()) {
                f.mkdirs();
            }

            File fe = new File(Environment.getDataDirectory()+"/data/com.mandarin.bcu/upload");

            if(!fe.exists()) fe.mkdirs();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
            Date date = new Date();
            String name = dateFormat.format(date);

            final Writer stringbuff = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringbuff);
            e.printStackTrace(printWriter);
            String current = stringbuff.toString();
            printWriter.close();

            if(upload) {
                String dname = name+"_"+Build.MODEL+".txt";

                File df = new File(Environment.getDataDirectory() + "/data/com.mandarin.bcu/upload/", dname);

                FileWriter dfileWriter = new FileWriter(df);
                dfileWriter.append("VERSION : ").append(StaticStore.VER).append("\r\n");
                dfileWriter.append("MODEL : ").append(Build.MANUFACTURER).append(" ").append(String.valueOf(Build.MODEL)).append("\r\n");
                dfileWriter.append("IS EMULATOR : ").append(String.valueOf(Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK"))).append("\r\n");
                dfileWriter.append("ANDROID_VER : ").append("API ").append(String.valueOf(Build.VERSION.SDK_INT)).append(" (").append(Build.VERSION.RELEASE).append(")").append("\r\n").append("\r\n");
                dfileWriter.append(current);
                dfileWriter.flush();
                dfileWriter.close();
            }

            name += ".txt";

            File file = new File(path, name);

            if (!file.exists())
                f.createNewFile();

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.append(current);
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void WriteLog(Exception error, boolean upload) {
        try {
            String path = Environment.getExternalStorageDirectory().getPath() + "/BCU/logs/";
            File f = new File(path);

            if (!f.exists()) {
                f.mkdirs();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
            Date date = new Date();
            String name = dateFormat.format(date) + ".txt";

            File file = new File(path, name);

            final Writer stringbuff = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringbuff);

            error.printStackTrace(printWriter);

            if (!file.exists())
                file.createNewFile();
            else {
                file = new File(path, GetExistingFileName(path, name));
                file.createNewFile();
            }

            if(upload) {
                File df = new File(Environment.getDataDirectory() + "/data/com.mandarin.bcu/upload/", name);

                if(!df.exists()) df.createNewFile();

                FileWriter dfileWriter = new FileWriter(df);
                dfileWriter.append("VERSION : ").append(StaticStore.VER).append("\r\n");
                dfileWriter.append("MODEL : ").append(Build.MANUFACTURER).append(" ").append(String.valueOf(Build.MODEL)).append("\r\n");
                dfileWriter.append("IS EMULATOR : ").append(String.valueOf(Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK"))).append("\r\n");
                dfileWriter.append("ANDROID_VER : ").append("API ").append(String.valueOf(Build.VERSION.SDK_INT)).append(" (").append(Build.VERSION.RELEASE).append(")").append("\r\n").append("\r\n");
                dfileWriter.append(stringbuff.toString());
                dfileWriter.flush();
                dfileWriter.close();
            }

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.append(stringbuff.toString());
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

        String nam = name + "-" + exist;

        while (!decided) {
            File f = new File(path, nam);

            if (!f.exists())
                return nam;
            else {
                exist++;
                nam = name + "-" + exist;
            }

            decided = true;
        }

        return nam;
    }

    public static boolean Upload(File file) throws IOException {
        if (!file.exists()) return false;

        String crlf = "\r\n";
        String hyphens = "--";
        String bound = "*****";

        FileInputStream fileInputStream = new FileInputStream(file);
        URL u = new URL("https://battle-cats-ultimate.000webhostapp.com/api/java/alogio.php");

        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setRequestMethod("POST");
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("ENCTYPE", "multipart/form-data");
        con.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****");
        con.setRequestProperty("uploaded_file", file.getName());

        DataOutputStream dos = new DataOutputStream(con.getOutputStream());

        byte[] buffer;
        int available, read, size;
        int max = 1024*1024;

        dos.writeBytes(hyphens+bound+crlf);
        dos.writeBytes("Content-Disposition: form-data; name=\""+"catFile"+"\";filename=\""+file.getName()+"\""+crlf);
        dos.writeBytes(crlf);

        available = fileInputStream.available();
        size = Math.min(available, max);
        buffer = new byte[size];

        read = fileInputStream.read(buffer, 0, size);

        while (read > 0) {
            dos.write(buffer, 0, size);
            available = fileInputStream.available();
            size = Math.min(available, size);
            read = fileInputStream.read(buffer, 0, size);
        }

        dos.writeBytes(crlf);
        dos.writeBytes(hyphens+bound+crlf);

        int response = con.getResponseCode();

        dos.flush();
        dos.close();
        fileInputStream.close();
        con.disconnect();

        return response == 200;
    }
}
