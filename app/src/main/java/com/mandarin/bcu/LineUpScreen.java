package com.mandarin.bcu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.lineup.LineUpView;
import com.mandarin.bcu.androidutil.lineup.asynchs.LUAdder;

public class LineUpScreen extends AppCompatActivity {


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

        setContentView(R.layout.activity_line_up_screen);

        LineUpView line = new LineUpView(this);
        line.setId(R.id.lineupView);
        LinearLayout layout = findViewById(R.id.lineuplayout);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        float w = size.x;

        float h = w / 5.0f * 3;

        line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) h));

        layout.addView(line);

        new LUAdder(this, getSupportFragmentManager()).execute();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase, shared.getInt("Language", 0)));
    }

    @Override
    public void onBackPressed() {
        StaticStore.SaveLineUp();
        StaticStore.updateList = false;
        StaticStore.filterReset();
        StaticStore.set = null;
        StaticStore.lu = null;
        StaticStore.combos.clear();
        StaticStore.lineunitname = null;
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mustDie(this);
    }

    public void mustDie(Object object) {
        if(MainActivity.watcher != null) {
            MainActivity.watcher.watch(object);
        }
    }
}
