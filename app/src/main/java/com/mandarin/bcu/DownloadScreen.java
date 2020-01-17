package com.mandarin.bcu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.io.asynchs.Downloader;

import java.util.ArrayList;


public class DownloadScreen extends AppCompatActivity{
    private String path;
    private ArrayList<String> fileneed;

    private Button retry;

    private String downloading;
    private String extracting;

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

        if(shared.getInt("Orientation",0) == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else if(shared.getInt("Orientation",0) == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        else if(shared.getInt("Orientation",0) == 0)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

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

        retry = findViewById(R.id.retry);
        retry.setVisibility(View.GONE);
        ProgressBar prog = findViewById(R.id.downprog);
        prog.setMax(100);

        new Downloader(path,fileneed,downloading,extracting,DownloadScreen.this).execute();

        Listeners();

    }

    protected void Listeners() {
        retry.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                retry.setVisibility(View.GONE);
                new Downloader(path,fileneed,downloading,extracting,DownloadScreen.this).execute();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        TextView state = findViewById(R.id.downstate);
        ProgressBar prog = findViewById(R.id.downprog);
        bundle.putString("state",state.getText().toString());
        bundle.putInt("prog",prog.getProgress());

        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences("configuration",Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase,shared.getInt("Language",0)));
    }
}
