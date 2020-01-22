package com.mandarin.bcu.androidutil.io.asynchs;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mandarin.bcu.CheckUpdateScreen;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.io.ErrorLogWriter;
import com.mandarin.bcu.io.Reader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class Unzipper extends AsyncTask<Void, Integer, Void> {
    private String destin;
    private ArrayList<String> fileneed;
    private final String path;
    private final String extracting;
    private boolean contin = true;
    private boolean upload;

    private WeakReference<Activity> weakReference;

    Unzipper(String path, ArrayList<String> fileneed, String extracting, Activity context, boolean upload) {
        this.path = path;
        this.fileneed = fileneed;
        this.extracting = extracting;
        this.weakReference = new WeakReference<>(context);
        destin = path + "files/";
        this.upload = upload;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int j;
        if (fileneed.contains("Language"))
            j = fileneed.size() - 1;
        else
            j = fileneed.size();

        for (int i = 0; i < j; i++) {
            try {
                String source = path + fileneed.get(i) + ".zip";
                InputStream is = new FileInputStream(source);
                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
                ZipEntry ze;
                byte[] buffer = new byte[1024];
                int count;

                while ((ze = zis.getNextEntry()) != null) {
                    String filenam = ze.getName();

                    File f = new File(destin + filenam);

                    if (ze.isDirectory()) {
                        if (!f.exists())
                            f.mkdirs();
                        continue;
                    }

                    File dir = new File(f.getParent());
                    if (!dir.exists())
                        dir.mkdirs();

                    if (!f.exists())
                        f.createNewFile();

                    FileOutputStream fout = new FileOutputStream(f);

                    while ((count = zis.read(buffer)) != -1) {
                        publishProgress(i);
                        fout.write(buffer, 0, count);
                    }

                    fout.close();
                    zis.closeEntry();
                }

                zis.close();

            } catch (IOException e) {
                ErrorLogWriter.WriteLog(e,upload);
                contin = false;
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        if (activity == null) return;

        ProgressBar prog = activity.findViewById(R.id.downprog);
        TextView state = activity.findViewById(R.id.downstate);
        prog.setIndeterminate(true);
        state.setText(R.string.down_zip_ex);
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        Activity activity = weakReference.get();

        if (activity == null) return;

        TextView state = activity.findViewById(R.id.downstate);
        state.setText(extracting + fileneed.get(values[0]));
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        if (activity == null) return;

        if (contin) {
            infowirter();
            for (String s : fileneed) {
                File f = new File(path, s + ".zip");

                if (f.exists()) {
                    f.delete();
                }
            }
            Intent intent = new Intent(activity, CheckUpdateScreen.class);
            StaticStore.clear();
            activity.startActivity(intent);
            activity.finish();
        } else {
            Button retry = activity.findViewById(R.id.retry);
            TextView checkstate = activity.findViewById(R.id.downstate);
            ProgressBar prog = activity.findViewById(R.id.downprog);

            checkstate.setText(R.string.unzip_fail);
            prog.setIndeterminate(false);
            retry.setVisibility(View.VISIBLE);
        }
    }

    private void infowirter() {
        String pathes = path + "files/info/";
        String filename = "info_android.ini";

        File f = new File(pathes, filename);
        try {
            String libs = infolibbuild();

            FileOutputStream fos = new FileOutputStream(f, false);

            fos.write((libs).getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String infolibbuild() {
        String pathes = path + "files/info/";
        String filename = "info_android.ini";
        Set<String> Original;
        StringBuilder result = new StringBuilder();
        StringBuilder abort = new StringBuilder();

        for (int i = 0; i < fileneed.size(); i++) {
            if (i != fileneed.size() - 1) {
                abort.append(fileneed.get(i)).append(",");
            } else {
                abort.append(fileneed.get(i));
            }
        }

        abort.insert(0, "file_version = 00040510\nnumber_of_libs = " + fileneed.size() + "\nlib=");

        File f = new File(pathes, filename);

        if (f.exists()) {
            try {

                Original = Reader.getInfo(path);

                if (Original != null)
                    System.out.println(Original);

                if (Original == null) {
                    Original = new TreeSet<>();
                }

                Original.addAll(fileneed);

                ArrayList<String> combined = new ArrayList<>(Original);

                for (int i = 0; i < combined.size(); i++) {
                    if (i != combined.size() - 1) {
                        result.append(combined.get(i)).append(",");
                    } else {
                        result.append(combined.get(i));
                    }
                }

                result.insert(0, "file_version = 00040510\nnumber_of_libs = " + combined.size() + "\nlib=");

                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return abort.toString();
            }
        } else {
            return abort.toString();
        }
    }
}
