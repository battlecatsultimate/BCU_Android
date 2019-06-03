package com.mandarin.bcu.asynchs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.MainActivity;
import com.mandarin.bcu.R;
import com.mandarin.bcu.io.Reader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private String [] langfile = {"EnemyName.txt","StageName.txt","UnitName.txt"};
    private String source;
    private String downloading;
    private String extracting;
    private final String path;
    private ArrayList<String> fileneed;

    private final ProgressBar prog;
    private final TextView state;
    private final Button retry;
    private final Context context;

    private ArrayList<String> purifyneed = new ArrayList<>();

    private File output = null;

    public Downloader(ProgressBar prog, TextView state, Button retry, String path, ArrayList<String> fileneed, ArrayList<String> filenum,
                      String downloading, String extracting, Context context) {
        this.prog = prog;
        this.state = state;
        this.retry = retry;
        this.path = path;
        this.fileneed = fileneed;
        this.downloading = downloading;
        this.extracting = extracting;
        this.context = context;
        source = path+"lang";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        prog.setIndeterminate(true);
        state.setText(R.string.down_check_file);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String url;
        URL link;
        HttpURLConnection connection;
        String urls = "http://battlecatsultimate.cf/api/resources/assets/";
        for(int i = 0; i<fileneed.size(); i++) {
            try {
                url = urls +fileneed.get(i)+".zip";
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
                url = urls +purifyneed.get(i)+".zip";
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
                for (String s1 : lan) {
                    for (String s : langfile) {
                        String lurl = "http://battlecatsultimate.cf/api/resources/lang/";
                        String langurl = lurl + s1 + s;
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

                        byte [] buffer = new byte[1024];
                        int len1;
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
                new Unzipper(prog,state,path,fileneed, extracting,context).execute();
            } else {
                state.setText(R.string.down_state_no);
                retry.setVisibility(View.VISIBLE);
            }
        } else {
            state.setText(R.string.down_state_ok);
            new Unzipper(prog,state,path,fileneed, extracting,context).execute();
        }
    }

    private void purify(ArrayList<Long> size) {
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
            Toast.makeText(context,"Error : Size is smaller than fileneed size",Toast.LENGTH_SHORT).show();
            ((Activity)context).finish();
        }
    }
}

class Unzipper extends AsyncTask<Void,Integer,Void> {
    private String destin;
    private ArrayList<String> fileneed;
    private final String path;
    private final String extracting;

    private final ProgressBar prog;
    private final TextView state;
    private final Context context;

    Unzipper(ProgressBar prog, TextView state, String path, ArrayList<String> fileneed,
             String extracting, Context context) {
        this.prog = prog;
        this.state = state;
        this.path = path;
        this.fileneed = fileneed;
        this.extracting = extracting;
        this.context = context;
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

                    if(ze.isDirectory()) {
                        File fmd = new File(destin+filenam);
                        fmd.mkdirs();
                        continue;
                    }

                    FileOutputStream fout = new FileOutputStream(destin+filenam);

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        prog.setIndeterminate(true);
        state.setText(R.string.down_zip_ex);
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        state.setText(extracting+fileneed.get(values[0]));
    }

    @Override
    protected void onPostExecute(Void result) {
        String pathes = path;
        infowirter();
        for (String s : fileneed) {
            File f = new File(pathes, s+".zip");

            if (f.exists()) {
                f.delete();
            }
        }
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
        ((Activity)context).finish();
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
