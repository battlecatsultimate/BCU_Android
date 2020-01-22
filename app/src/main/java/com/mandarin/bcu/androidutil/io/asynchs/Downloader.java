package com.mandarin.bcu.androidutil.io.asynchs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Downloader extends AsyncTask<Void, Integer, Void> {

    private int size;
    private Map<String, Long> sizes = new HashMap<>();
    private ArrayList<Boolean> remover = new ArrayList<>();

    private String[] lan = {"/en/", "/jp/", "/kr/", "/zh/"};
    private String difffile = "Difficulty.txt";
    private String source;
    private String downloading;
    private String extracting;
    private final String path;
    private final String mpath;
    private ArrayList<String> fileneed;
    private ArrayList<String> musics;

    private final WeakReference<Activity> weakActivity;

    private ArrayList<String> purifyneed = new ArrayList<>();

    private File output = null;

    public Downloader(String path, ArrayList<String> fileneed, ArrayList<String> music, String downloading, String extracting, Activity context) {
        this.path = path;
        this.fileneed = fileneed;
        this.downloading = downloading;
        this.extracting = extracting;
        this.weakActivity = new WeakReference<>(context);
        this.musics = music;
        source = path + "lang";
        mpath = path + "music/";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Activity activity = weakActivity.get();

        if (activity == null) return;

        ProgressBar prog = activity.findViewById(R.id.downprog);
        TextView state = activity.findViewById(R.id.downstate);
        prog.setIndeterminate(true);
        state.setText(R.string.down_check_file);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String url;
        URL link;
        HttpURLConnection connection;
        String urls = "https://github.com/battlecatsultimate/bcu-resources/blob/master/resources/assets/";
        String RAW = "?raw=true";
        for (int i = 0; i < fileneed.size(); i++) {
            try {
                File f = new File(path, fileneed.get(i) + ".zip");

                if (!f.exists()) continue;

                url = urls + fileneed.get(i) + ".zip" + RAW;
                link = new URL(url);

                connection = (HttpURLConnection) link.openConnection();
                size = connection.getContentLength();

                sizes.put(fileneed.get(i), (long) size);

                connection.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        purify(sizes);

        int number;
        if(purifyneed.contains("Language") && purifyneed.contains("Music")) {
            number = purifyneed.size() - 2;
        } else if (purifyneed.contains("Language") || purifyneed.contains("Music")) {
            number = purifyneed.size() - 1;
        } else {
            number = purifyneed.size();
        }
        long total;
        for (int i = 0; i < number; i++) {
            try {
                url = urls + purifyneed.get(i) + ".zip" + RAW;
                link = new URL(url);
                connection = (HttpURLConnection) link.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                size = connection.getContentLength();

                connection.getResponseCode();


                output = new File(path, purifyneed.get(i) + ".zip");
                File pathes = new File(path);

                if (!pathes.exists()) {
                    pathes.mkdirs();
                }

                if (!output.exists()) {
                    output.createNewFile();
                }

                FileOutputStream fos = new FileOutputStream(output);
                InputStream is = connection.getInputStream();

                byte[] buffer = new byte[1024];
                int len1;
                total = 0;

                while ((len1 = is.read(buffer)) != -1) {
                    total += Math.abs(len1);
                    int progress = (int) (total * 100 / size);
                    publishProgress(progress, i);
                    fos.write(buffer, 0, len1);
                }

                connection.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                output = null;
            } catch (IOException e) {
                e.printStackTrace();
                output = null;
            }

            if (output != null) {
                remover.add(false);
            }
        }

        System.out.println(musics);

        if(purifyneed.contains("Music")) {
            if(!musics.isEmpty()) {
                try {
                    for(int i = 0; i < musics.size(); i++) {
                        String murl = "https://github.com/battlecatsultimate/bcu-resources/raw/master/resources/music/";
                        String mfile = musics.get(i);

                        link = new URL(murl+mfile);

                        connection = (HttpURLConnection) link.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        connection.getResponseCode();

                        size = connection.getContentLength();

                        output = new File(mpath,mfile);

                        File mf = output.getParentFile();

                        if(!mf.exists()) {
                            mf.mkdirs();
                        }

                        if(!output.exists()) {
                            output.createNewFile();
                        }

                        FileOutputStream fos = new FileOutputStream(output);
                        InputStream is = connection.getInputStream();

                        byte [] buffer = new byte[1024];
                        int len1;
                        total = 0;

                        while((len1 = is.read(buffer)) != -1) {
                            total += len1;

                            int progress = 0;

                            if(size != 0)
                                progress = (int) (total*100/size);

                            publishProgress(progress,50,i);

                            fos.write(buffer,0,len1);
                        }

                        connection.disconnect();

                        fos.close();
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            purifyneed.remove("Music");
            fileneed.remove("Music");
        }

        if (purifyneed.contains("Language")) {
            try {
                String lurl = "https://raw.githubusercontent.com/battlecatsultimate/bcu-resources/master/resources/lang";
                String durl = lurl + "/" + difffile;
                link = new URL(durl);
                connection = (HttpURLConnection) link.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                connection.getResponseCode();

                output = new File(source + "/", difffile);

                File dpath = output.getParentFile();

                if (!dpath.exists()) {
                    dpath.mkdirs();
                }

                if (!output.exists()) {
                    output.createNewFile();
                }

                FileOutputStream dfos = new FileOutputStream(output);
                InputStream dis = connection.getInputStream();

                byte[] buffer = new byte[1024];
                int len1;
                total = 0;

                while ((len1 = dis.read(buffer)) != -1) {
                    total += len1;
                    int progress = 0;
                    if (size != 0)
                        progress = (int) (total * 100 / size);
                    publishProgress(progress, 100);
                    dfos.write(buffer, 0, len1);
                }

                connection.disconnect();
                dis.close();
                dfos.close();

                for (String s1 : lan) {
                    for (String s : StaticStore.langfile) {
                        String langurl = lurl + s1 + s + RAW;
                        link = new URL(langurl);
                        connection = (HttpURLConnection) link.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        connection.getResponseCode();

                        output = new File(source + s1, s);

                        File pathes = new File(source + s1);

                        if (!pathes.exists()) {
                            pathes.mkdirs();
                        }

                        if (!output.exists()) {
                            output.createNewFile();
                        }

                        FileOutputStream fos = new FileOutputStream(output);
                        InputStream is = connection.getInputStream();

                        buffer = new byte[1024];
                        total = 0;

                        while ((len1 = is.read(buffer)) > 0) {
                            total += len1;
                            int progress = 0;
                            if (size != 0)
                                progress = (int) (total * 100 / size);
                            publishProgress(progress, 100);
                            fos.write(buffer, 0, len1);
                        }

                        connection.disconnect();
                        fos.close();
                        is.close();
                    }

                    purifyneed.remove("Language");
                    fileneed.remove("Language");

                    StaticStore.unitlang = 1;
                    StaticStore.enemeylang = 1;
                    StaticStore.stagelang = 1;
                    StaticStore.maplang = 1;
                }
            } catch (IOException e) {
                output = null;
                e.printStackTrace();
            } finally {
                if (!(output == null)) {
                    fileneed.remove("Language");
                    purifyneed.remove("Language");
                }
            }
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Activity activity = weakActivity.get();

        if (activity == null) return;

        ProgressBar prog = activity.findViewById(R.id.downprog);
        TextView state = activity.findViewById(R.id.downstate);

        if (prog.isIndeterminate()) {
            prog.setIndeterminate(false);
        }
        prog.setProgress(values[0]);
        if (values[1] != 100 && values[1] != 50) {
            state.setText(downloading + purifyneed.get(values[1]));
        } else if(values[1] == 50) {
            String mdownload = activity.getString(R.string.down_state_music)+musics.get(values[2]);

            state.setText(mdownload);
        } else {
            state.setText(downloading + "Language Files");
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakActivity.get();

        if (activity == null) return;

        ProgressBar prog = activity.findViewById(R.id.downprog);
        TextView state = activity.findViewById(R.id.downstate);
        Button retry = activity.findViewById(R.id.retry);

        ArrayList<String> results = new ArrayList<>();

        if (remover.size() < purifyneed.size()) {
            for (int i = 0; i < purifyneed.size() - remover.size(); i++) {
                remover.add(true);
            }
        }

        for (int i = 0; i < remover.size(); i++) {
            if (remover.get(i)) {
                results.add(fileneed.get(i));
            }
        }

        purifyneed = results;

        if (prog.isIndeterminate()) {
            prog.setIndeterminate(false);
            prog.setProgress(100);
        }

        SharedPreferences preferences = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);

        if (!purifyneed.isEmpty() || output == null) {
            if (purifyneed.isEmpty()) {
                state.setText(R.string.down_state_ok);
                new Unzipper(path, fileneed, extracting, activity, preferences.getBoolean("upload", false) || preferences.getBoolean("ask_upload", true)).execute();
            } else {
                state.setText(R.string.down_state_no);
                retry.setVisibility(View.VISIBLE);
            }
        } else {
            state.setText(R.string.down_state_ok);
            new Unzipper(path, fileneed, extracting, activity, preferences.getBoolean("upload", false) || preferences.getBoolean("ask_upload", true)).execute();
        }
    }

    private void purify(Map<String, Long> size) {
        Activity activity = weakActivity.get();

        if (size == null || size.size() == 0) {
            purifyneed.addAll(fileneed);
            return;
        }

        if (activity == null) return;

        purifyneed = new ArrayList<>();
        ArrayList<Integer> result = new ArrayList<>();

        for (int i = 0; i < fileneed.size(); i++) {
            File f = new File(path, fileneed.get(i) + ".zip");

            if (f.exists()) {
                if (f.length() != size.get(fileneed.get(i))) {
                    result.add(i);
                }
            } else {
                result.add(i);
            }
        }

        if (!result.isEmpty()) {
            for (int i = 0; i < result.size(); i++) {
                purifyneed.add(fileneed.get(result.get(i)));
            }
        }
    }
}

