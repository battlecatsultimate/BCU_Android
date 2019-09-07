package com.mandarin.bcu.androidutil.io.asynchs;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.CheckUpdateScreen;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.io.Reader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Downloader extends AsyncTask<Void,Integer,Void> {

    private int size;
    private ArrayList<Long> sizes = new ArrayList<>();
    private ArrayList<Boolean> remover = new ArrayList<>();

    private String [] lan = {"/en/","/jp/","/kr/","/zh/"};
    private String [] langfile = {"EnemyName.txt","StageName.txt","UnitName.txt","UnitExplanation.txt","EnemyExplanation.txt","CatFruitExplanation.txt","RewardName.txt"};
    private String difffile = "Difficulty.txt";
    private String source;
    private String downloading;
    private String extracting;
    private final String path;
    private ArrayList<String> fileneed;

    private final WeakReference<Activity> weakActivity;

    private ArrayList<String> purifyneed = new ArrayList<>();

    private File output = null;

    public Downloader(String path, ArrayList<String> fileneed, String downloading, String extracting, Activity context) {
        this.path = path;
        this.fileneed = fileneed;
        this.downloading = downloading;
        this.extracting = extracting;
        this.weakActivity = new WeakReference<>(context);
        source = path+"lang";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Activity activity = weakActivity.get();

        if(activity == null) return;

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
        for(int i = 0; i<fileneed.size(); i++) {
            try {
                url = urls +fileneed.get(i)+".zip" + RAW;
                link = new URL(url);
                connection = (HttpURLConnection) link.openConnection();

                size = connection.getContentLength();

                sizes.add((long)size);

                connection.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        purify(sizes);

        int number;
        if(purifyneed.contains("Language")) {
            number = purifyneed.size()-1;
        } else {
            number = purifyneed.size();
        }
        long total;
        for(int i = 0; i < number; i++) {
            try {
                url = urls +purifyneed.get(i)+".zip"+ RAW;
                link = new URL(url);
                connection = (HttpURLConnection) link.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                size = connection.getContentLength();

                connection.getResponseCode();


                output = new File(path,purifyneed.get(i)+".zip");
                File pathes = new File(path);

                if(!pathes.exists()) {
                    pathes.mkdirs();
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
                    total += Math.abs(len1);
                    int progress = (int)(total *100/size);
                    publishProgress(progress,i);
                    fos.write(buffer,0,len1);
                }

                connection.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                output = null;
            } catch (IOException e) {
                e.printStackTrace();
                output = null;
            }

            if(output != null) {
                remover.add(false);
            }
        }

        if(purifyneed.contains("Language")) {
            try {
                String lurl = "https://raw.githubusercontent.com/battlecatsultimate/bcu-resources/master/resources/lang";
                String durl = lurl+"/"+difffile;
                link = new URL(durl);
                connection = (HttpURLConnection)link.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                connection.getResponseCode();

                output = new File(source+"/",difffile);

                File dpath = output.getParentFile();

                if(!dpath.exists()) {
                    dpath.mkdirs();
                }

                if(!output.exists()) {
                    output.createNewFile();
                }

                FileOutputStream dfos = new FileOutputStream(output);
                InputStream dis = connection.getInputStream();

                byte[] buffer = new byte[1024];
                int len1;
                total = 0;

                while((len1 = dis.read(buffer)) != -1) {
                    total += len1;
                    int progress = (int) (total *100/size);
                    publishProgress(progress,100);
                    dfos.write(buffer,0,len1);
                }

                connection.disconnect();
                dis.close();
                dfos.close();

                for (String s1 : lan) {
                    for (String s : langfile) {
                        String langurl = lurl + s1 + s + RAW;
                        link = new URL(langurl);
                        connection = (HttpURLConnection) link.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        connection.getResponseCode();

                        output = new File(source + s1,s);

                        File pathes = new File(source + s1);

                        if(!pathes.exists()) {
                            pathes.mkdirs();
                        }

                        if(!output.exists()) {
                            output.createNewFile();
                        }

                        FileOutputStream fos = new FileOutputStream(output);
                        InputStream is = connection.getInputStream();

                        buffer = new byte[1024];
                        total = 0;

                        while((len1 = is.read(buffer)) > 0) {
                            total += len1;
                            int progress = (int) (total *100/size);
                            publishProgress(progress,100);
                            fos.write(buffer,0,len1);
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
            } catch (MalformedURLException e) {
                output = null;
                e.printStackTrace();
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

        if(activity == null) return;

        ProgressBar prog = activity.findViewById(R.id.downprog);
        TextView state = activity.findViewById(R.id.downstate);

        if(prog.isIndeterminate()) {
            prog.setIndeterminate(false);
        }
        prog.setProgress(values[0]);
        if(values[1] != 100) {
            state.setText(downloading + purifyneed.get(values[1]));
        } else {
            state.setText(downloading+"Language Files");
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakActivity.get();

        if(activity == null) return;

        ProgressBar prog = activity.findViewById(R.id.downprog);
        TextView state = activity.findViewById(R.id.downstate);
        Button retry = activity.findViewById(R.id.retry);

        ArrayList<String> results = new ArrayList<>();

        if(remover.size() < purifyneed.size()) {
            for(int i = 0; i < purifyneed.size()-remover.size();i++) {
                remover.add(true);
            }
        }

        for(int i = 0; i < remover.size();i++) {
            if(remover.get(i)) {
                results.add(fileneed.get(i));
            }
        }

        purifyneed = results;

        if(prog.isIndeterminate()) {
            prog.setIndeterminate(false);
            prog.setProgress(100);
        }

        if(!purifyneed.isEmpty() || output == null) {
            if(purifyneed.isEmpty()) {
                state.setText(R.string.down_state_ok);
                new Unzipper(path,fileneed, extracting,activity).execute();
            } else {
                state.setText(R.string.down_state_no);
                retry.setVisibility(View.VISIBLE);
            }
        } else {
            state.setText(R.string.down_state_ok);
            new Unzipper(path,fileneed, extracting,activity).execute();
        }
    }

    private void purify(ArrayList<Long> size) {
        Activity activity = weakActivity.get();

        if(activity == null) return;

        purifyneed = new ArrayList<>();
        ArrayList<Integer> result = new ArrayList<>();

        if(fileneed.size() <= size.size()) {
            for(int i = 0;i<fileneed.size();i++) {
                File f= new File(path,fileneed.get(i)+".zip");

                if(f.exists()) {
                    if(f.length() != size.get(i)) {
                        result.add(i);
                    }
                } else {
                    result.add(i);
                }
            }

            if(!result.isEmpty()) {
                for (int i = 0; i < result.size(); i++) {
                    purifyneed.add(fileneed.get(result.get(i)));
                }
            }
        } else {
            Toast.makeText(activity,"Error : Size is smaller than fileneed size",Toast.LENGTH_SHORT).show();
            activity.finish();
        }
    }
}

class Unzipper extends AsyncTask<Void,Integer,Void> {
    private String destin;
    private ArrayList<String> fileneed;
    private final String path;
    private final String extracting;
    private boolean contin = true;

    private WeakReference<Activity> weakReference;

    Unzipper(String path, ArrayList<String> fileneed, String extracting, Activity context) {
        this.path = path;
        this.fileneed = fileneed;
        this.extracting = extracting;
        this.weakReference = new WeakReference<>(context);
        destin = path+"files/";
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int j;
        if(fileneed.contains("Language"))
            j = fileneed.size()-1;
        else
            j = fileneed.size();

        for(int i=0;i<j;i++) {
            try {
                String source = path + fileneed.get(i) + ".zip";
                InputStream is = new FileInputStream(source);
                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
                ZipEntry ze;
                byte[] buffer = new byte[1024];
                int count;

                while((ze = zis.getNextEntry()) != null) {
                    String filenam = ze.getName();

                    File f= new File(destin+filenam);

                    if(ze.isDirectory()) {
                        if (!f.exists())
                            f.mkdirs();
                        continue;
                    }

                    File dir = new File(f.getParent());
                    if(!dir.exists())
                        dir.mkdirs();

                    if(!f.exists())
                        f.createNewFile();

                    FileOutputStream fout = new FileOutputStream(f);

                    while((count = zis.read(buffer)) != -1) {
                        publishProgress(i);
                        fout.write(buffer,0,count);
                    }

                    fout.close();
                    zis.closeEntry();
                }

                zis.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                contin = false;
            } catch (IOException e) {
                e.printStackTrace();
                contin = false;
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        if(activity == null) return;

        ProgressBar prog = activity.findViewById(R.id.downprog);
        TextView state = activity.findViewById(R.id.downstate);
        prog.setIndeterminate(true);
        state.setText(R.string.down_zip_ex);
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        TextView state = activity.findViewById(R.id.downstate);
        state.setText(extracting+fileneed.get(values[0]));
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        if (contin) {
            infowirter();
            for (String s : fileneed) {
                File f = new File(path, s + ".zip");

                if (f.exists()) {
                    f.delete();
                }
            }
            Intent intent = new Intent(activity, CheckUpdateScreen.class);
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
        String pathes = path+"files/info/";
        String filename = "info_android.ini";

        File f = new File(pathes,filename);
        try {
            String libs = infolibbuild();

            FileOutputStream fos = new FileOutputStream(f,false);

            fos.write((libs).getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String infolibbuild() {
        String pathes = path+"files/info/";
        String filename = "info_android.ini";
        Set<String> Original;
        StringBuilder result = new StringBuilder();
        StringBuilder abort = new StringBuilder();

        for(int i = 0; i < fileneed.size();i++) {
            if(i != fileneed.size() - 1) {
                abort.append(fileneed.get(i)).append(",");
            } else {
                abort.append(fileneed.get(i));
            }
        }

        abort.insert(0, "file_version = 00040510\nnumber_of_libs = " + fileneed.size() + "\nlib=");

        File f = new File(pathes,filename);

        if(f.exists()) {
            try {

                Original = Reader.getInfo(path);

                if(Original != null)
                    System.out.println(Original);

                if(Original == null) {
                    Original = new TreeSet<>();
                }

                Original.addAll(fileneed);

                ArrayList<String> combined = new ArrayList<>(Original);

                for(int i = 0; i < combined.size();i++) {
                    if(i != combined.size() - 1) {
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
