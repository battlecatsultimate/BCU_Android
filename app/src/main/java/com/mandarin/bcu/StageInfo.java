package com.mandarin.bcu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.stage.asynchs.StageAdder;

public class StageInfo extends AppCompatActivity {
    private int mapcode,stid,posit;

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

        setContentView(R.layout.activity_stage_info);

        FloatingActionButton bck = findViewById(R.id.stginfobck);
        bck.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                StaticStore.infoOpened = null;
                StaticStore.stageSpinner = -1;
                finish();
            }
        });

        Intent result = getIntent();

        if(result.getExtras() != null) {
            Bundle extra = result.getExtras();

            mapcode = extra.getInt("mapcode");
            stid = extra.getInt("stid");
            posit = extra.getInt("posit");
        }

        new StageAdder(this,mapcode,stid,posit).execute();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences("configuration",Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase,shared.getInt("Language",0)));
    }

    @Override
    public void onBackPressed() {
        FloatingActionButton bck = findViewById(R.id.stginfobck);

        bck.performClick();
    }
}
