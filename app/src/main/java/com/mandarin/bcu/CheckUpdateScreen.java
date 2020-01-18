package com.mandarin.bcu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder;
import com.mandarin.bcu.androidutil.io.ErrorLogWriter;
import com.mandarin.bcu.androidutil.io.asynchs.CheckApk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import common.system.fake.ImageBuilder;

public class CheckUpdateScreen extends AppCompatActivity {
    private final String [] LIB_REQUIRED = StaticStore.LIBREQ;
    private final String PATH = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU/apk/";
    private String path;
    private boolean config = false;

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
            ed.putInt("Orientation",0);
            ed.putBoolean("Lay_Port",true);
            ed.putBoolean("Lay_Land",false);
            ed.apply();
        } else {
            if(!shared.getBoolean("theme",false)) {
                setTheme(R.style.AppTheme_night);
            } else {
                setTheme(R.style.AppTheme_day);
            }
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

        if(!shared.contains("frame")) {
            ed.putBoolean("frame",true);
            ed.apply();
        }

        if(!shared.contains("Orientation")) {
            ed.putInt("Orientation",0);
            ed.apply();
        }

        if(!shared.contains("Lay_Port")) {
            ed.putBoolean("Lay_Port",false);
            ed.apply();
        }

        if(!shared.contains("Lay_Land")) {
            ed.putBoolean("Lay_Land",false);
        }

        if(!shared.contains("Skip_Text")) {
            ed.putBoolean("Skip_Text",false);
        }

        if(shared.getInt("Orientation",0) == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else if(shared.getInt("Orientation",0) == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        else if(shared.getInt("Orientation",0) == 0)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        Thread.setDefaultUncaughtExceptionHandler(new ErrorLogWriter(StaticStore.LOGPATH));

        setContentView(R.layout.activity_check_update_screen);

        if(MainActivity.isRunning) finish();

        deleter(new File(PATH));

        Intent result = getIntent();

        if(result.getExtras() != null) {
            Bundle extra = result.getExtras();

            config = extra.getBoolean("Config");
        }

        TextView checkstate = findViewById(R.id.mainstup);
        ProgressBar mainprog = findViewById(R.id.mainprogup);
        Button retry = findViewById(R.id.checkupretry);

        retry.setVisibility(View.GONE);

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        path = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU";

        ImageBuilder.builder = new BMBuilder();

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connectivityManager.getActiveNetworkInfo() != null) {
                    retry.setVisibility(View.GONE);
                    mainprog.setVisibility(View.VISIBLE);
                    boolean lang = false;
                    CheckApk checkApk = new CheckApk(path,lang,CheckUpdateScreen.this,cando());
                    checkApk.execute();
                } else {
                    Toast.makeText(CheckUpdateScreen.this, R.string.needconnect, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(connectivityManager.getActiveNetworkInfo() != null) {
            if(!config) {
                boolean lang = false;
                CheckApk checkApk = new CheckApk(path, lang, this, cando());
                checkApk.execute();
            } else {
                boolean lang = false;
                CheckApk checkApk = new CheckApk(path,lang,this,cando(),config);
                checkApk.execute();
            }
        } else {
            if(cando()) {
                boolean lang = false;
                new CheckApk(path,lang,this,cando(),config).execute();

            } else {
                mainprog.setVisibility(View.GONE);
                retry.setVisibility(View.VISIBLE);
                checkstate.setText(R.string.main_internet_no);
                Toast.makeText(this, R.string.needconnect, Toast.LENGTH_SHORT).show();
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

    private void deleter(File f) {
        if(f.isDirectory())
            for(File g : f.listFiles())
                deleter(g);
        else
            f.delete();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences("configuration",Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase,shared.getInt("Language",0)));
    }
}
