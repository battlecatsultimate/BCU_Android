package com.mandarin.bcu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private String path;
    final private String [] filename = {"000001.zip","000002.zip","000003.zip","080504.zip"};
    final private String [] lib = {"000001","000002","000003","080504"};
    final private long [] sizes = {68264194,69064400,69569256,2956};
    final private int FILE_NUMBER = 4;
    private ArrayList<String> fileneed = new ArrayList<>();
    private ArrayList<String> filenum = new ArrayList<>();
    private boolean lang = false;

    private Button animbtn;
    private Button stagebtn;
    private TextView checkstate;
    private ProgressBar mainprog;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        path = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU";

        animbtn = findViewById(R.id.anvibtn);
        stagebtn = findViewById(R.id.stgbtn);
        checkstate = findViewById(R.id.mainstup);
        mainprog = findViewById(R.id.mainprogup);

        animbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this,R.drawable.ic_kasa_jizo), null, null, null);
        stagebtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this,R.drawable.ic_castle),null,null,null);

        animbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationview();
            }
        });
        stagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stageinfoview();
            }
        });

        checkUpdates updates = new checkUpdates();
        updates.execute();
    }

    protected void animationview() {
        Intent intent = new Intent(this, AnimationViewer.class);
        startActivity(intent);
    }

    protected void stageinfoview()
    {
        Intent intent = new Intent(this,StageInfo.class);
        startActivity(intent);
    }

    protected void checkFiles() {
        AlertDialog.Builder donloader = new AlertDialog.Builder(this);
        final Intent intent = new Intent(this,DownloadScreen.class);
        donloader.setTitle(R.string.main_file_x);
        donloader.setMessage(R.string.main_file_cont);
        donloader.setPositiveButton(R.string.main_file_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(lang) {
                    fileneed.add("lang");
                    filenum.add(String.valueOf(FILE_NUMBER+1));
                }
                intent.putExtra("fileneed",fileneed);
                intent.putExtra("filenum",filenum);
                startActivity(intent);
                finish();
            }
        });

        donloader.setNegativeButton(R.string.main_file_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        donloader.setCancelable(false);

        langcheck();

        String infopath = path + "/files/info/";
        String filename = "info.txt";

        File g = new File(infopath);

        if(!g.exists()) {
            for(int i=0;i<lib.length;i++) {
                fileneed.add(lib[i]);
                filenum.add(String.valueOf(i));
            }

            AlertDialog downloader = donloader.create();
            downloader.show();
        } else {
            File f = new File(infopath,filename);

            if(f.exists()) {
                try {
                    String line;

                    FileInputStream fis = new FileInputStream(f);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder str = new StringBuilder();

                    while((line = br.readLine()) != null) {
                        str.append(line).append(System.getProperty("line.separator"));
                    }

                    String [] lit = str.toString().split("\n");

                    String [] libnum = lit[2].split("=");

                    ArrayList<String> ver = new ArrayList<>(Arrays.asList(libnum[1].split(",")));
                    System.out.println(ver.toString());

                    if(ver.size() != FILE_NUMBER) {
                        for(int i=0;i<lib.length;i++) {
                            if(!ver.contains(lib[i])) {
                                fileneed.add(lib[i]);
                                filenum.add(String.valueOf(i));
                            }
                        }
                        AlertDialog downloader = donloader.create();
                        downloader.show();
                    } else {
                        int checker = 0;

                        for(int i=0;i<lib.length;i++) {
                            if(lib[i].equals(ver.get(i)))
                                checker++;
                            else {
                                fileneed.add(lib[i]);
                                filenum.add(String.valueOf(i));
                            }
                        }

                        if(checker!=FILE_NUMBER) {
                            AlertDialog downloader = donloader.create();
                            downloader.show();
                        } else {
                            checker = 0;

                            for(int i=0;i<lib.length;i++) {
                                f = new File(infopath,lib[i]+".verinfo");

                                if(f.exists())
                                    checker++;
                                else
                                    if(!fileneed.contains(lib[i])) {
                                        fileneed.add(lib[i]);
                                        filenum.add(String.valueOf(i));
                                    }
                            }

                            if(checker != FILE_NUMBER) {
                                AlertDialog downloader = donloader.create();
                                downloader.show();
                            } else if(lang) {
                                AlertDialog downloader = donloader.create();
                                downloader.show();
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                for(int i=0;i<lib.length;i++) {
                    fileneed.add(lib[i]);
                    filenum.add(String.valueOf(i));
                }

                AlertDialog downloader = donloader.create();
                downloader.show();
            }
        }
    }

    protected void langcheck() {
        String [] lan = {"/en/","/jp/","/kr/","/zh/"};
        String [] langfile = {"EnemyName.txt","StageName.txt","UnitName.txt"};
        String source = path+"/lang";

        File f = new File(source);

        if(!f.exists()) {
            lang = true;
        } else {
            for (String s : lan) {
                for (String s1 : langfile) {
                    f = new File(source + s, s1);

                    if (!f.exists()) {
                        lang = true;
                        break;
                    }
                }

                if (lang) {
                    break;
                }
            }
        }
    }

    private class checkUpdates extends AsyncTask<Void,Integer,Void> {
        private String [] lan = {"/en/","/jp/","/kr/","/zh/"};
        private String [] langfile = {"EnemyName.txt","StageName.txt","UnitName.txt"};
        private String source = path+"/lang";
        private int size;
        private File output = null;
        private boolean add;
        final private String url = "http://battlecatsultimate.cf/api/resources/lang";
        private URL link;
        private HttpURLConnection c;

        @Override
        protected void onPreExecute() {
            animbtn.setVisibility(View.GONE);
            stagebtn.setVisibility(View.GONE);
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

                        InputStream urlis = c.getInputStream();

                        byte[] buf = new byte[1024];
                        int len1;
                        size = 0;
                        while ((len1 = urlis.read(buf)) != -1) {
                            size += len1;
                        }


                        output = new File(source + s1, s);

                        if (output.exists()) {
                            if (output.length() != size) {
                                System.out.println(size);
                                System.out.println(output.length());
                                add = true;
                                break;
                            }
                        }

                        c.disconnect();

                    } catch (IOException e) {
                        e.printStackTrace();
                        output = null;
                    }
                }

                if (add) {
                    break;
                }
            }

            publishProgress(1);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(add) {
                lang = true;
            }

            if (values[0] == 1) {
                checkstate.setText(R.string.main_check_file);
                checkFiles();
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            if(fileneed.isEmpty() && filenum.isEmpty()) {
                mainprog.setVisibility(View.GONE);
                checkstate.setVisibility(View.GONE);
                stagebtn.setVisibility(View.VISIBLE);
                animbtn.setVisibility(View.VISIBLE);
            }
        }
    }
}
