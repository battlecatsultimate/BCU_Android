package com.mandarin.bcu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class DownloadScreen extends AppCompatActivity{
    private String path;
    final private String urls = "http://battlecatsultimate.cf/api/resources/assets/";
    private ArrayList<String> fileneed;
    private ArrayList<String> filenum;
    private ArrayList<String> purifyneed = new ArrayList<>();

    private Button retry;
    private ProgressBar prog;
    private TextView state;

    private String downloading;
    private String extracting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_screen);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        path = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU/";
        downloading = getString(R.string.down_state_doing);
        extracting = getString(R.string.down_zip_ex);

        Intent result = getIntent();
        fileneed = result.getStringArrayListExtra("fileneed");
        filenum = result.getStringArrayListExtra("filenum");

        retry = findViewById(R.id.retry);
        retry.setVisibility(View.GONE);
        prog = findViewById(R.id.downprog);
        prog.setMax(100);
        state = findViewById(R.id.downstate);

        new Downloader().execute();

        Listeners();

    }

    protected void Listeners() {
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retry.setVisibility(View.GONE);
                new Downloader().execute();
            }
        });
    }

    protected void purify(ArrayList<Long> size) {
        purifyneed = new ArrayList<>();
        ArrayList<Integer> result = new ArrayList<>();


        if(fileneed.size() <= size.size()) {
            for(int i = 0;i<fileneed.size();i++) {
                File f= new File(path,fileneed.get(i)+".zip");

                if(f.exists()) {
                    if(f.length() != size.get(Integer.parseInt(filenum.get(i)))) {
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
            Toast.makeText(DownloadScreen.this,"Error : Size is smaller than fileneed size",Toast.LENGTH_SHORT).show();
            DownloadScreen.this.finish();
        }
    }

    class Downloader extends AsyncTask<Void,Integer,Void> {
        private long total;
        private int size;
        private int number;
        private ArrayList<Long> sizes = new ArrayList<>();
        private String url;
        private URL link;
        private HttpURLConnection connection;
        private ArrayList<Boolean> remover = new ArrayList<>();

        private String [] lan = {"/en/","/jp/","/kr/","/zh/"};
        private String [] langfile = {"EnemyName.txt","StageName.txt","UnitName.txt"};
        private String source = path+"/lang";
        private String lurl = "http://battlecatsultimate.cf/api/resources/lang/";

        File output = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prog.setIndeterminate(true);
            state.setText(R.string.down_check_file);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for(int i = 0; i<fileneed.size();i++) {
                try {
                    url = urls+fileneed.get(i)+".zip";
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

            if(purifyneed.contains("Language")) {
                number = purifyneed.size()-1;
            } else {
                number = purifyneed.size();
            }
            for(int i = 0; i < number;i++) {
                try {
                    url = urls+purifyneed.get(i)+".zip";
                    link = new URL(url);
                    connection = (HttpURLConnection) link.openConnection();

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

                    while((len1 = is.read(buffer)) > 0) {
                        total += Math.abs(len1);
                        int progress = (int)(total*100/size);
                        publishProgress(progress,i);
                        fos.write(buffer,0,len1);
                    }

                    connection.disconnect();
                    fos.close();
                    is.close();
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
                            String langurl = lurl + s1 + s;
                            link = new URL(langurl);
                            connection = (HttpURLConnection) link.openConnection();

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
                                int progress = (int) (total*100/size);
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
            if(prog.isIndeterminate()) {
                prog.setIndeterminate(false);
                prog.setProgress(100);
            }

            if(!purifyneed.isEmpty() || output == null) {
                if(purifyneed.isEmpty()) {
                    state.setText(R.string.down_state_ok);
                    new Unzipper().execute();
                } else {
                    state.setText(R.string.down_state_no);
                    retry.setVisibility(View.VISIBLE);
                }
            } else {
                state.setText(R.string.down_state_ok);
                new Unzipper().execute();
            }
        }
    }

    protected class Unzipper extends AsyncTask<Void,Integer,Void> {
        InputStream is;
        ZipInputStream zis;
        private String source;
        private String destin = path+"/files/";

        @Override
        protected Void doInBackground(Void... voids) {
            int j;
            if(fileneed.contains("Language"))
                j = fileneed.size()-1;
            else
                j = fileneed.size();

            for(int i=0;i<j;i++) {
                try {
                    source = path+"/"+fileneed.get(i)+".zip";
                    is = new FileInputStream(source);
                    zis = new ZipInputStream(new BufferedInputStream(is));
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
            String pathes = path+"/";
            infowirter();
            for (String s : fileneed) {
                File f = new File(pathes, s+".zip");

                if (f.exists()) {
                    f.delete();
                }
            }
            Intent intent = new Intent(DownloadScreen.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        private void infowirter() {
            String pathes = path+"/files/info/";
            String filename = "info_android.ini";

            File f = new File(pathes,filename);
            try {
                FileOutputStream fos = new FileOutputStream(f,false);

                String libs = infolibbuild();

                fos.write(("Installed lib=4\nver=0.5.0\nlib="+libs).getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String infolibbuild() {
            String pathes = path+"/files/info/";
            String filename = "info_android.ini";
            ArrayList<String> Original = new ArrayList<>();
            String result = "";
            String abort = "";

            for(int i = 0; i < fileneed.size();i++) {
                if(i != fileneed.size() - 1) {
                    abort += fileneed.get(i)+",";
                } else {
                    abort += fileneed.get(i);
                }
            }

            File f = new File(pathes,filename);

            if(f.exists()) {
                try {
                    String line;

                    FileInputStream fis = new FileInputStream(f);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);

                    ArrayList<String> lines = new ArrayList<>();

                    while((line = br.readLine()) != null) {
                        lines.add(line);
                    }

                    if(lines.size() == 3) {
                        String [] libline = lines.get(2).split("=");
                        if(libline.length == 2) {
                            String [] libs = libline[1].split(",");

                            for(int i = 0; i < libs.length; i++) {
                                Original.add(libs[i]);
                            }

                            Set<String> set = new LinkedHashSet<>(Original);
                            set.addAll(fileneed);
                            ArrayList<String> combined = new ArrayList<>(set);

                            for(int i = 0; i < combined.size();i++) {
                                if(i != combined.size() - 1) {
                                    result += combined.get(i)+",";
                                } else {
                                    result += combined.get(i);
                                }
                            }
                            return result;
                        } else {
                            return abort;
                        }
                    } else {
                        return abort;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return abort;
                } catch (IOException e) {
                    e.printStackTrace();
                    return abort;
                }
            } else {
                return abort;
            }
        }
    }
}
