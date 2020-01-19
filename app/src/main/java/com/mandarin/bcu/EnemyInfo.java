package com.mandarin.bcu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.enemy.asynchs.EInfoLoader;

import common.system.MultiLangCont;

public class EnemyInfo extends AppCompatActivity {
    FloatingActionButton treasure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences shared = getSharedPreferences(StaticStore.CONFIG, MODE_PRIVATE);
        SharedPreferences.Editor ed;
        if (!shared.contains("initial")) {
            ed = shared.edit();
            ed.putBoolean("initial", true);
            ed.putBoolean("theme", true);
            ed.apply();
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_night);
            } else {
                setTheme(R.style.AppTheme_day);
            }
        }

        if (shared.getInt("Orientation", 0) == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else if (shared.getInt("Orientation", 0) == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        else if (shared.getInt("Orientation", 0) == 0)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        setContentView(R.layout.activity_enemy_info);

        treasure = findViewById(R.id.enemtreasure);

        ScrollView scrollView = findViewById(R.id.eneminfscroll);
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scrollView.setFocusable(true);
        scrollView.setFocusableInTouchMode(true);
        scrollView.setVisibility(View.GONE);

        TextView title = findViewById(R.id.eneminftitle);

        Intent result = getIntent();
        Bundle extra = result.getExtras();

        if (extra != null) {
            int id = extra.getInt("ID");
            int multi = extra.getInt("Multiply");
            title.setText(MultiLangCont.ENAME.getCont(StaticStore.enemies.get(id)));

            Button eanim = findViewById(R.id.eanimanim);

            eanim.setOnClickListener(new SingleClick() {
                @Override
                public void onSingleClick(View v) {
                    Intent intent = new Intent(EnemyInfo.this, ImageViewer.class);
                    intent.putExtra("Img", 3);
                    intent.putExtra("ID", id);

                    startActivity(intent);
                }
            });

            if (multi != 0)
                new EInfoLoader(this, id, multi).execute();
            else
                new EInfoLoader(this, id).execute();
        }
    }

    @Override
    public void onBackPressed() {
        if (StaticStore.EisOpen)
            treasure.performClick();
        else
            super.onBackPressed();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase, shared.getInt("Language", 0)));
    }
}
