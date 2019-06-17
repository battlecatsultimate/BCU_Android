package com.mandarin.bcu;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mandarin.bcu.androidutil.asynchs.DownloadApk;

public class ApkDownload extends AppCompatActivity {
    private final String PATH = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU/apk/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences shared = getSharedPreferences("configuration",MODE_PRIVATE);
        SharedPreferences.Editor ed;
        if(!shared.contains("initial")) {
            ed = shared.edit();
            ed.putBoolean("initial",true);
            ed.putBoolean("theme",true);
            ed.apply();
        } else {
            if(!shared.getBoolean("theme",false)) {
                setTheme(R.style.AppTheme_night);
            } else {
                setTheme(R.style.AppTheme_day);
            }
        }

        setContentView(R.layout.activity_apk_download);

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 786);


        if (ContextCompat.checkSelfPermission(ApkDownload.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent result = getIntent();
            if (result.getStringExtra("ver") != null) {
                String ver = result.getStringExtra("ver");
                String FILESTART = "BCU_Android_";
                String APK = ".apk";
                String realpath = PATH + FILESTART + ver + APK;
                String ZIP = ".zip";
                String URL = "http://battlecatsultimate.cf/api/resources/android/";
                String realurl = URL + FILESTART + ver + ZIP;

                Button retry = findViewById(R.id.apkretry);
                retry.setVisibility(View.GONE);
                ProgressBar prog = findViewById(R.id.apkprog);
                prog.setIndeterminate(true);
                prog.setMax(100);
                TextView state = findViewById(R.id.apkstate);
                state.setText(R.string.down_state_rea);

                new DownloadApk(ApkDownload.this, ver, realurl, PATH, realpath).execute();

                retry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new DownloadApk(ApkDownload.this, ver, realurl, PATH, realpath).execute();
                        retry.setVisibility(View.GONE);
                    }
                });
            }
        }
    }
}
