package com.mandarin.bcu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.androidutil.DefineItf;
import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.asynchs.CheckApk;
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder;

import common.CommonStatic;
import common.system.fake.ImageBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    private final String [] LIB_REQUIRED = StaticStore.LIBREQ;
    private String path;
    private final String PATH = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU/apk/";
    private ArrayList<String> fileneed = new ArrayList<>();
    private ArrayList<String> filenum = new ArrayList<>();

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences shared = getSharedPreferences("configuration",MODE_PRIVATE);
        SharedPreferences.Editor ed = shared.edit();
        if(!shared.contains("initial")) {
            ed.putBoolean("initial",true);
            ed.putBoolean("theme",true);
            ed.putBoolean("frame",true);
            ed.putBoolean("apktest",false);
            ed.putInt("default_level",50);
            ed.putInt("Language",0);
            ed.apply();
        } else {
            if(!shared.getBoolean("theme",false)) {
                setTheme(R.style.AppTheme_night);
            } else {
                setTheme(R.style.AppTheme_day);
            }
        }

        if(!shared.contains("frame")) {
            ed.putBoolean("frame",true);
            ed.apply();
        }

        if(!shared.contains("apktest")) {
            ed.putBoolean("apktest",true);
            ed.apply();
        }

        if(!shared.contains("default_level")) {
            ed.putInt("default_level",50);
            ed.apply();
        }

        if(!shared.contains("apktest")) {
            ed.putBoolean("apktest",false);
            ed.apply();
        }

        if(!shared.contains("Language")) {
            ed.putInt("Language",0);
            ed.apply();
        }

        setContentView(R.layout.activity_main);

        deleter(new File(PATH));

        Intent result = getIntent();
        Button animbtn = findViewById(R.id.anvibtn);
        Button stagebtn = findViewById(R.id.stgbtn);
        TextView checkstate = findViewById(R.id.mainstup);
        ProgressBar mainprog = findViewById(R.id.mainprogup);
        ImageButton config = findViewById(R.id.mainconfig);

        animbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this,R.drawable.ic_kasa_jizo), null, null, null);
        stagebtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this,R.drawable.ic_castle),null,null,null);

        animbtn.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                animationview();
            }
        });

        stagebtn.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                stageinfoview();
            }
        });

        config.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                gotoconfig();
            }
        });

        if(result.getBooleanExtra("Config",false)) {
            mainprog.setVisibility(View.GONE);
            checkstate.setVisibility(View.GONE);
            stagebtn.setVisibility(View.VISIBLE);
            animbtn.setVisibility(View.VISIBLE);
            config.setVisibility(View.VISIBLE);
        } else {
            path = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU";

            ImageBuilder.builder = new BMBuilder();



            animbtn.setVisibility(View.GONE);
            stagebtn.setVisibility(View.GONE);
            config.setVisibility(View.GONE);

            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

            if(connectivityManager.getActiveNetworkInfo() != null) {
                boolean lang = false;
                CheckApk checkApk = new CheckApk(path,lang,fileneed,filenum,this,cando());
                checkApk.execute();
            } else {
                if(cando()) {
                    com.mandarin.bcu.decode.ZipLib.init();
                    com.mandarin.bcu.decode.ZipLib.read();

                    StaticStore.getUnitnumber();
                    StaticStore.root = 1;

                    new DefineItf().init();

                    String language = Locale.getDefault().getLanguage();
                    CommonStatic.Lang.lang = Arrays.asList(StaticStore.lang).indexOf(language)-1;

                    if(CommonStatic.Lang.lang >= 4 || CommonStatic.Lang.lang == -2)
                        CommonStatic.Lang.lang = 0;

                    mainprog.setVisibility(View.GONE);
                    checkstate.setVisibility(View.GONE);
                    stagebtn.setVisibility(View.VISIBLE);
                    animbtn.setVisibility(View.VISIBLE);
                    config.setVisibility(View.VISIBLE);
                } else {
                    mainprog.setVisibility(View.GONE);
                    checkstate.setText(R.string.main_internet_no);
                    Toast.makeText(this, "You need internet connection to run this application!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    protected boolean cando() {
        String infopath = path + "/files/info/";
        String filename = "info_android.ini";

        File f = new File(infopath,filename);

        if(f.exists()) {
            try {
                String line;

                FileInputStream fis = new FileInputStream(f);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);

                ArrayList<String> lines = new ArrayList<>();

                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }

                try {
                    Set<String> libs = new TreeSet<>(Arrays.asList(lines.get(2).split("=")[1].split(",")));

                    for(String s : LIB_REQUIRED)
                        if(!libs.contains(s))
                            return false;

                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
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

    protected void gotoconfig() {
        Intent intent = new Intent(this,ConfigScreen.class);
        startActivity(intent);
        finish();
    }

    private void deleter(File f) {
        if(f.isDirectory())
            for(File g : f.listFiles())
                deleter(g);
        else
            f.delete();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences("configuration",Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase,shared.getInt("Language",0)));
    }

}
