package com.mandarin.bcu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.battle.asynchs.BPAdder;
import com.mandarin.bcu.androidutil.lineup.LineUpView;

import common.battle.BasisSet;

public class BattlePrepare extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
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

        setContentView(R.layout.activity_battle_prepare);

        SharedPreferences preferences = getSharedPreferences(StaticStore.CONFIG, MODE_PRIVATE);

        LineUpView line = new LineUpView(this);
        line.setId(R.id.lineupView);
        LinearLayout layout = findViewById(R.id.preparelineup);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        float w;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            w = size.x / 2.0f;
        else
            w = size.x;

        float h = w / 5.0f * 3;

        line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) h));

        layout.addView(line);

        Intent intent = getIntent();

        Bundle result = intent.getExtras();

        if (result != null) {
            int mapcode = result.getInt("mapcode");
            int stid = result.getInt("stid");
            int posit = result.getInt("stage");

            if (result.containsKey("selection")) {
                new BPAdder(this, mapcode, stid, posit, result.getInt("selection")).execute();
            } else {
                new BPAdder(this, mapcode, stid, posit).execute();
            }
        }

    }

    @Override
    protected void onActivityResult(int code, int code1, @Nullable Intent data) {
        super.onActivityResult(code, code1, data);

        LineUpView line = findViewById(R.id.lineupView);

        line.UpdateLineUp();

        TextView setname = findViewById(R.id.lineupname);

        setname.setText(getSetLUName());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase, shared.getInt("Language", 0)));
    }

    private String getSetLUName() {
        return BasisSet.current.name + " - " + BasisSet.current.sele.name;
    }
}
