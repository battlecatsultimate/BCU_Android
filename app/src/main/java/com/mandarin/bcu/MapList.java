package com.mandarin.bcu;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.stage.asynchs.MapAdder;

public class MapList extends AppCompatActivity {

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

        setContentView(R.layout.activity_map_list);
        FloatingActionButton back = findViewById(R.id.stgbck);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Spinner stageset = findViewById(R.id.stgspin);
        String[] setstg = getResources().getStringArray(R.array.set_stg);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinneradapter, setstg) {
            @NonNull
            public View getView(int position, View converView, @NonNull ViewGroup parent) {
                View v = super.getView(position, converView, parent);

                ((TextView) v).setTextColor(ContextCompat.getColor(MapList.this, R.color.TextPrimary));
                int eight = StaticStore.dptopx(8f, MapList.this);
                v.setPadding(eight, eight, eight, eight);

                return v;
            }

            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);

                ((TextView) v).setTextColor(ContextCompat.getColor(MapList.this, R.color.TextPrimary));

                return v;
            }
        };

        stageset.setAdapter(adapter);

        MapAdder mapAdder = new MapAdder(this);

        mapAdder.execute();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase, shared.getInt("Language", 0)));
    }

    @Override
    public void onBackPressed() {
        FloatingActionButton bck = findViewById(R.id.stgbck);

        bck.performClick();
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
