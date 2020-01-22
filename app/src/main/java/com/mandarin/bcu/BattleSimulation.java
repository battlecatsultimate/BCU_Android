package com.mandarin.bcu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler;
import com.mandarin.bcu.androidutil.battle.asynchs.BAdder;

public class BattleSimulation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences shared = getSharedPreferences("configuration", MODE_PRIVATE);
        SharedPreferences.Editor ed;
        if (!shared.contains("initial")) {
            ed = shared.edit();
            ed.putBoolean("initial", true);
            ed.putBoolean("theme", true);
            ed.apply();
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_designNight);
            } else {
                setTheme(R.style.AppTheme_designDay);
            }
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_battle_simulation);

        SoundHandler.inBattle = true;

        Intent intent = getIntent();

        if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();

            int mapcode = bundle.getInt("mapcode");
            int stid = bundle.getInt("stid");
            int posit = bundle.getInt("stage");
            int star = bundle.getInt("star");
            int item = bundle.getInt("item");

            new BAdder(this, mapcode, stid, posit, star, item).execute();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public void onBackPressed() {
        Button exit = findViewById(R.id.battleexit);

        exit.performClick();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase, shared.getInt("Language", 0)));
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mustDie(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        if(SoundHandler.MUSIC != null) {
            if(SoundHandler.MUSIC.isInitialized()) {
                if(SoundHandler.MUSIC.isRunning() || SoundHandler.MUSIC.isPlaying()) {
                    SoundHandler.MUSIC.pause();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(SoundHandler.MUSIC != null) {
            if(SoundHandler.MUSIC.isInitialized()) {
                if((!SoundHandler.MUSIC.isRunning() || !SoundHandler.MUSIC.isPlaying()) && SoundHandler.musicPlay) {
                    SoundHandler.MUSIC.start();
                }
            }
        }
    }

    public void mustDie(Object object) {
        if(MainActivity.watcher != null) {
            MainActivity.watcher.watch(object);
        }
    }
}
