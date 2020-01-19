package com.mandarin.bcu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;

import java.io.File;

public class BackgroundList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences shared = getSharedPreferences(StaticStore.CONFIG, MODE_PRIVATE);
        SharedPreferences.Editor ed = shared.edit();
        if (!shared.contains("initial")) {
            ed.putBoolean("initial", true);
            ed.putBoolean("theme", true);
            ed.putBoolean("frame", true);
            ed.putBoolean("apktest", false);
            ed.putInt("default_level", 50);
            ed.putInt("Language", 0);
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

        setContentView(R.layout.activity_background_list);

        ListView listView = findViewById(R.id.bglist);

        if (StaticStore.bgnumber == 0) {
            String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/img/bg/";

            File f = new File(path);

            StaticStore.bgnumber = f.list().length - 1;
        }

        String[] names = new String[StaticStore.bgnumber];

        for (int i = 0; i < names.length; i++) {
            names[i] = getString(R.string.bg_names).replace("_", number(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinneradapter, names);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (SystemClock.elapsedRealtime() - StaticStore.bglistClick < StaticStore.INTERVAL)
                    return;

                StaticStore.bglistClick = SystemClock.elapsedRealtime();

                Intent intent = new Intent(BackgroundList.this, ImageViewer.class);
                intent.putExtra("Path", Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.mandarin.BCU/files/org/img/bg/bg" + number(position) + ".png");
                intent.putExtra("Img", 0);
                intent.putExtra("BGNum", position);

                startActivity(intent);
            }
        });

        FloatingActionButton bck = findViewById(R.id.bgbck);

        bck.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase, shared.getInt("Language", 0)));
    }

    private String number(int n) {
        if (0 <= n && n < 10) {
            return "00" + n;
        } else if (10 <= n && n <= 99) {
            return "0" + n;
        } else {
            return String.valueOf(n);
        }
    }
}
