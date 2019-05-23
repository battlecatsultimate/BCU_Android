package com.mandarin.bcu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import pub.devrel.easypermissions.EasyPermissions;


public class DownloadScreen extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    final private String url = "http://battlecatsultimate.cf/api/resources/assets/";
    final private String [] filename = {"000001.zip","000002.zip","000003.zip","080504.zip"};
    final private String [] lib = {"000001","000002","000003","080504"};
    final private int FILE_NUMBER = 4;
    public ArrayList<Integer> allset = new ArrayList<>();
    private URL link;
    private HttpURLConnection c;
    private String folder;
    private String checkerstr;
    private String extra;
    private ArrayList<String> fileneed;
    private ArrayList<String> filenum;
    private ProgressBar[] downpro = new ProgressBar[4];
    private ProgressBar zippro;
    private ProgressBar langpro;
    private TextView[] state = new TextView[4];
    private TextView zipstate;
    private TextView langstate;
    private Button[] buttons = new Button[4];
    private Button all;
    private Button done;
    private Button lang;
    private int [] btid = {R.id.downbt1,R.id.downbt2,R.id.downbt3,R.id.downbt01};
    private int [] proid = {R.id.prog1,R.id.prog2,R.id.prog3,R.id.prog01};
    private int [] stid = {R.id.downst1,R.id.downst2,R.id.downst3,R.id.downst01};
    private long [] sizes = {68264194,69064400,69569256,2956};
    private ArrayList<String> num = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_screen);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }
        }

        Intent result = getIntent();
        fileneed = result.getStringArrayListExtra("fileneed");
        filenum = result.getStringArrayListExtra("filenum");
        for(int i=0;i<FILE_NUMBER+1;i++) {
            num.add(String.valueOf(i));
        }
        num = numfilter(num,filenum);

        folder = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU";
        checkerstr = getResources().getString(R.string.down_finish);
        extra = getResources().getString(R.string.down_zip_ex);
        all = findViewById(R.id.downbtall);
        done = findViewById(R.id.downbtdone);
        zipstate = findViewById(R.id.downzipst);
        zippro = findViewById(R.id.progzip);
        zippro.setMax(100);
        zippro.setProgress(0);
        lang = findViewById(R.id.downbtlang);
        langstate = findViewById(R.id.downstlang);
        langpro = findViewById(R.id.proglang);
        langpro.setMax(100);
        langpro.setProgress(0);

        for(int i=0;i<stid.length;i++) {
            downpro[i] = findViewById(proid[i]);
            downpro[i].setMax(100);

            state[i] = findViewById(stid[i]);

            buttons[i] = findViewById(btid[i]);
        }

        for(int i=0;i<filename.length;i++) {
            if(checker(i))
                buttons[i].setClickable(false);
        }

        if(langcheck())
            lang.setClickable(false);

        allcheck();

        listeners();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, DownloadScreen.this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d("Error : ","Permission has been denied");
        finish();
    }

    protected ArrayList<String> numfilter(ArrayList<String> target, ArrayList<String> compare) {
        ArrayList<String> result = new ArrayList<>();
        for(int i=0;i<target.size();i++) {
            if(!compare.contains(target.get(i)))
                result.add(target.get(i));
        }

        return result;
    }

    protected void listeners() {
        for(int i=0;i<buttons.length;i++) {
            final int finall = i;
            if(buttons[i].isClickable()) {
                buttons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downloader down = new downloader(url,downpro[finall],filename[finall],finall);

                        down.doing();

                        allset.add(1);

                        if(allset.size() == filename.length+1) {
                            all.setClickable(false);
                            all.setTextColor(0xFF777777);
                            all.setBackgroundColor(0xFFEDEDED);
                        }
                    }
                });
            }
        }

        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0;i<filename.length;i++) {
                    if(buttons[i].isClickable()) {
                        buttons[i].performClick();
                    }
                }

                if(lang.isClickable()) {
                    lang.performClick();
                }
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checker = 0;

                for(int i=0;i<filename.length;i++) {
                    if(buttons[i].getText().toString().equals(checkerstr))
                        checker++;
                }

                if(lang.getText().toString().equals(checkerstr))
                    checker++;

                if(checker == FILE_NUMBER+1) {
                    done.setClickable(false);
                    done.setBackgroundColor(0xFFEDEDED);
                    done.setTextColor(0xFF777777);
                    unzipper unzip = new unzipper(zippro);
                    unzip.execute();
                } else {
                    Toast.makeText(v.getContext(), R.string.down_notdone, Toast.LENGTH_SHORT).show();
                }
            }
        });

        lang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lang.isClickable()) {
                    new langDownloader().execute();

                    allset.add(1);

                    lang.setClickable(false);
                    lang.setBackgroundColor(0xFFEDEDED);
                    lang.setTextColor(0xFF777777);

                    if (allset.size() == filename.length + 1) {
                        all.setClickable(false);
                        all.setTextColor(0xFF777777);
                        all.setBackgroundColor(0xFFEDEDED);
                    }
                }
            }
        });
    }

    protected class unzipper extends AsyncTask<Void,Integer,Void> {
        private ProgressBar prog;
        InputStream is;
        ZipInputStream zis;
        private String source;
        private String destin = folder+"/files/";

        unzipper(ProgressBar prog) {
            this.prog = prog;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int j;
            if(fileneed.contains("lang"))
                j = fileneed.size()-1;
            else
                j = fileneed.size();

            for(int i=0;i<j;i++) {
                try {
                    source = folder+"/"+fileneed.get(i)+".zip";
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
            zipstate.setText(R.string.down_zip_ex);
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onProgressUpdate(Integer... values) {
            zipstate.setText(extra+fileneed.get(values[0]));
        }

        @Override
        protected void onPostExecute(Void result) {
            String path = folder+"/";
            infowirter();
            for (String s : filename) {
                File f = new File(path, s);

                if (f.exists()) {
                    f.delete();
                }
            }
            Intent intent = new Intent(DownloadScreen.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        void infowirter() {
            String path = folder+"/files/info/";
            String filename = "info.txt";

            File f = new File(path,filename);
            try {
                FileOutputStream fos = new FileOutputStream(f,false);

                fos.write(("Installed lib=4\nver=0.3.5\nlib=000001,000002,000003,080504").getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected boolean checker(int number) {
        if(num.contains(String.valueOf(number))) {
            File f = new File(folder+"/",filename[number]);

            if(f.exists()) {
                fileneed.add(lib[number]);
                filenum.add(String.valueOf(number));

                if(f.length() == sizes[Integer.parseInt(num.get(number))]) {
                    finisher(number);
                    allset.add(0);
                    return true;
                } else {
                    return false;
                }
            } else {
                installed(number);
                allset.add(0);
                return true;
            }
        }

        File f = new File(folder+"/",filename[number]);

        if(f.exists()) {
            if(f.length() == sizes[Integer.parseInt(filenum.get(number))]) {
                finisher(number);
                allset.add(0);
                return true;
            }
            return false;
        }

        return false;
    }

    protected boolean langcheck() {
        if(fileneed.contains("lang")) {
            allset.add(0);
            return false;
        } else {
            langinstall();
            return true;
        }
    }

    protected void installed(int num) {
        buttons[num].setBackgroundColor(0xFFEDEDED);
        buttons[num].setTextColor(0xFF777777);
        buttons[num].setText(R.string.down_finish);
        downpro[num].setProgress(100);
        state[num].setText(R.string.down_state_install);
    }

    protected void finisher(int num) {
        buttons[num].setBackgroundColor(0xFFEDEDED);
        buttons[num].setTextColor(0xFF777777);
        buttons[num].setText(R.string.down_finish);
        downpro[num].setProgress(100);
        state[num].setText(R.string.down_state_ok);
    }

    protected void langinstall() {
        lang.setBackgroundColor(0xFFEDEDED);
        lang.setTextColor(0xFF777777);
        lang.setText(R.string.down_state_ok);
        langpro.setProgress(100);
        langstate.setText(R.string.down_state_install);
    }

    protected class downloader {

        private String url;
        private String filename;
        ProgressBar prog;
        private long total;
        private int size;
        private int number;

        downloader(String url, ProgressBar prog, String filenam, int num) {
            this.url = url+filenam;
            this.prog = prog;
            this.filename = filenam;
            this.number = num;
        }

        private void doing() {
            new DownloadingTask().execute();
        }

        private class DownloadingTask extends AsyncTask<Context,Integer,String> {

            File output = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                prog.setIndeterminate(true);
                downpro[number].setProgress(0);
                state[number].setText(R.string.down_wait);
                buttons[number].setClickable(false);
                checkbutton();
            }

            @Override
            protected String doInBackground(Context... contexts) {
                try {
                    link = new URL(url);
                    c = (HttpURLConnection) link.openConnection();
                    c.setRequestMethod("GET");
                    c.connect();
                    size = c.getContentLength();

                    c.getResponseCode();

                    output = new File(folder,filename);

                    File path = new File(folder);

                    if(!path.exists()) {
                        path.mkdirs();
                    }

                    if(!output.exists()) {
                        output.createNewFile();
                    }

                    FileOutputStream fos = new FileOutputStream(output);
                    InputStream is = c.getInputStream();

                    byte [] buffer = new byte[2048];
                    int len1;
                    total = 0;
                    while((len1 = is.read(buffer)) != -1) {
                        total += Math.abs(len1);
                        int progress = (int)(total*100/size);
                        publishProgress(progress);
                        fos.write(buffer,0,len1);
                    }

                    c.disconnect();
                    fos.close();
                    is.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    output = null;
                }

                return "Complete";
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    if(output != null) {
                        state[number].setText(R.string.down_state_ok);
                        buttons[number].setText(R.string.down_finish);
                    } else {
                        state[number].setText(R.string.down_state_no);
                        buttons[number].setText(R.string.down_redown);
                        buttons[number].setClickable(true);
                        if(prog.isIndeterminate())
                        {
                            prog.setIndeterminate(false);
                            prog.setProgress(0);
                        }
                        checkbutton();

                        if(!all.isClickable()) {
                            all.setClickable(true);
                            all.setTextColor(0xFF1C1C1C);
                            all.setBackgroundColor(0xFFD7D7D7);
                            allset.remove(0);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                super.onPostExecute(result);
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                prog.setIndeterminate(false);
                state[number].setText(R.string.down_state_doing);
                prog.setProgress(values[0]);
            }

            void checkbutton() {
                if(buttons[number].isClickable()) {
                    buttons[number].setBackgroundColor(0xFFD7D7D7);
                    buttons[number].setTextColor(0xFF1C1C1C);
                }
                else {
                    buttons[number].setBackgroundColor(0xFFEDEDED);
                    buttons[number].setTextColor(0xFF777777);
                }
            }
        }
    }

    class langDownloader extends AsyncTask<Void,Integer,Void> {

        private long size;
        private File output = null;
        private String [] lan = {"/en/","/jp/","/kr/","/zh/"};
        private String [] langfile = {"EnemyName.txt","StageName.txt","UnitName.txt"};
        private String source = folder+"/lang";
        private String url = "http://battlecatsultimate.cf/api/resources/lang/";
        private long total;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            langstate.setText(R.string.down_wait);
            langpro.setIndeterminate(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (String s1 : lan) {
                for (String s : langfile) {
                    try {
                        String langurl = url + s1 + s;
                        link = new URL(langurl);
                        c = (HttpURLConnection) link.openConnection();
                        c.setRequestMethod("GET");
                        c.connect();
                        size = c.getContentLength();

                        c.getResponseCode();

                        output = new File(source + s1, s);

                        File path = new File(source + s1);

                        if (!path.exists()) {
                            path.mkdirs();
                        }

                        if (!output.exists()) {
                            output.createNewFile();
                        }

                        FileOutputStream fos = new FileOutputStream(output);
                        InputStream is = c.getInputStream();

                        byte[] buffer = new byte[1024];
                        int len1;
                        total = 0;
                        while ((len1 = is.read(buffer)) != -1) {
                            total += Math.abs(len1);
                            int progress = (int) (total * 100 / size);
                            publishProgress(progress);
                            fos.write(buffer, 0, len1);
                        }

                        c.disconnect();
                        fos.close();
                        is.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                        output = null;
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            langpro.setProgress(values[0]);
            langstate.setText(R.string.down_state_doing);
            langpro.setIndeterminate(false);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            try {
                if(output != null) {
                    langstate.setText(R.string.down_state_ok);
                    lang.setText(R.string.down_finish);
                    checkbutton();
                    langpro.setProgress(100);
                } else {
                    langstate.setText(R.string.down_state_no);
                    lang.setText(R.string.down_redown);
                    lang.setClickable(true);
                    if(langpro.isIndeterminate())
                    {
                        langpro.setIndeterminate(false);
                        langpro.setProgress(0);
                    }
                    checkbutton();

                    if(!all.isClickable()) {
                        all.setClickable(true);
                        all.setTextColor(0xFF1C1C1C);
                        all.setBackgroundColor(0xFFD7D7D7);
                        allset.remove(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            super.onPostExecute(result);
        }

        void checkbutton() {
            if(lang.isClickable()) {
                lang.setBackgroundColor(0xFFD7D7D7);
                lang.setTextColor(0xFF1C1C1C);
            } else {
                lang.setBackgroundColor(0xFFEDEDED);
                lang.setTextColor(0xFF777777);
            }
        }
    }

    protected void allcheck() {
        if(allset.size() == filename.length+1) {
            all.setClickable(false);
            all.setTextColor(0xFF777777);
            all.setBackgroundColor(0xFFEDEDED);
        }
    }
}
