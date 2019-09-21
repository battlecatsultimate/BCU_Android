package com.mandarin.bcu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.stage.asynchs.StageLoader;

import common.system.MultiLangCont;
import common.util.stage.MapColc;
import common.util.stage.StageMap;

public class StageList extends AppCompatActivity {
    private int mapcode;
    private int stid;

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

        setContentView(R.layout.activity_stage_list);

        Intent result = getIntent();

        Bundle extra = result.getExtras();

        if(extra != null) {
            mapcode = extra.getInt("mapcode");
            stid = extra.getInt("stid");
        }

        TextView name = findViewById(R.id.stglistname);

        MapColc mc = MapColc.MAPS.get(mapcode);

        if(mc != null) {
            StageMap stm = mc.maps[stid];

            String stname = MultiLangCont.SMNAME.getCont(stm);

            if(stname == null) stname = "";

            name.setText(stname);
        }

        StageLoader stageLoader = new StageLoader(this,mapcode,stid);
        stageLoader.execute();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences("configuration",Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase,shared.getInt("Language",0)));
    }

    @Override
    public void onBackPressed() {
        ImageButton bck = findViewById(R.id.stglistbck);

        bck.performClick();
    }
}
