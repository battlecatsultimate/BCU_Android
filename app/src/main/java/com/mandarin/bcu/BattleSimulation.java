package com.mandarin.bcu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.mandarin.bcu.androidutil.battle.asynchs.BAdder;
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics;

import common.system.P;

public class BattleSimulation extends AppCompatActivity {

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
                setTheme(R.style.AppTheme_designNight);
            } else {
                setTheme(R.style.AppTheme_designDay);
            }
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_battle_simulation);

        Intent intent = getIntent();

        if(intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();

            int mapcode = bundle.getInt("mapcode");
            int stid = bundle.getInt("stid");
            int posit = bundle.getInt("stage");
            int star = bundle.getInt("star");
            int item = bundle.getInt("item");

            new BAdder(this,mapcode,stid,posit,star,item).execute();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public void onBackPressed() {
        P.stack.clear();
        CVGraphics.clear();
        super.onBackPressed();
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
}
