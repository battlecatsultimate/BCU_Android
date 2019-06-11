package com.mandarin.bcu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.androidutil.asynchs.DownloadApk;
import com.mandarin.bcu.decode.ZipLib;

import java.io.File;
import java.nio.file.Files;

public class ApkDownload extends AppCompatActivity {
    private final String PATH = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU/apk/";
    private final String FILESTART = "BCU_Android_";
    private final String ZIP = ".zip";
    private final String APK = ".apk";
    private final String URL = "http://battlecatsultimate.cf/api/resources/android/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk_download);

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 786);


        if (ContextCompat.checkSelfPermission(ApkDownload.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent result = getIntent();
            if (result.getStringExtra("ver") != null) {
                String ver = result.getStringExtra("ver");
                String realpath = PATH + FILESTART + ver + APK;
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
        } else {
            Toast.makeText(this, "Permission Required!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
