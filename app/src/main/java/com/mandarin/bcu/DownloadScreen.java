package com.mandarin.bcu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;


public class DownloadScreen extends AppCompatActivity{
    private String path;
    private ArrayList<String> fileneed;
    private ArrayList<String> filenum;

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

        new com.mandarin.bcu.asynchs.Downloader(prog,state,retry,path,fileneed,filenum,downloading,extracting,DownloadScreen.this).execute();

        Listeners();

    }

    protected void Listeners() {
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retry.setVisibility(View.GONE);
                new com.mandarin.bcu.asynchs.Downloader(prog,state,retry,path,fileneed,filenum,downloading,extracting,DownloadScreen.this).execute();
            }
        });
    }
}
