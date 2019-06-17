package com.mandarin.bcu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends AppCompatActivity {
    SharedPreferences shared;
    ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shared = getSharedPreferences("configuration",MODE_PRIVATE);
        SharedPreferences.Editor ed = shared.edit();
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

        setContentView(R.layout.activity_config_screen);

        back = findViewById(R.id.configback);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfigScreen.this,MainActivity.class);
                intent.putExtra("Config",true);
                startActivity(intent);
                finish();
            }
        });

        RadioButton day = findViewById(R.id.themeday);
        RadioButton night = findViewById(R.id.themenight);
        RadioButton frames = findViewById(R.id.configframe);
        RadioButton seconds = findViewById(R.id.configsecond);

        if(shared.contains("initial")) {
            if(!shared.getBoolean("theme",false))
                night.setChecked(true);
            else
                day.setChecked(true);
        }

        if (shared.getBoolean("frame", true)) {
            frames.setChecked(true);
        } else {
            seconds.setChecked(true);
        }

        RadioGroup theme = findViewById(R.id.configrgtheme);
        theme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == day.getId()) {
                    SharedPreferences.Editor ed = shared.edit();
                    ed.putBoolean("theme",true);
                    ed.apply();
                    restart();
                } else {
                    SharedPreferences.Editor ed = shared.edit();
                    ed.putBoolean("theme",false);
                    ed.apply();
                    restart();
                }
            }
        });

        RadioGroup frse = findViewById(R.id.configfrse);
        frse.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == frames.getId()) {
                    SharedPreferences.Editor ed = shared.edit();
                    ed.putBoolean("frame",true);
                    ed.apply();
                } else {
                    SharedPreferences.Editor ed = shared.edit();
                    ed.putBoolean("frame",false);
                    ed.apply();
                }
            }
        });

        List<Integer> levels = new ArrayList<>();
        for(int j =1;j < 51;j++)
            levels.add(j);

        Spinner deflev = findViewById(R.id.configdeflev);
        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<>(this,R.layout.spinneradapter,levels);
        deflev.setAdapter(arrayAdapter);
        deflev.setSelection(getIndex(deflev,shared.getInt("default_level",50)));

        deflev.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor ed = shared.edit();
                ed.putInt("default_level",(int)deflev.getSelectedItem());
                ed.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Switch apktest = findViewById(R.id.apktest);

        if(!shared.getBoolean("apktest",false)) {
            apktest.setChecked(false);
        } else {
            apktest.setChecked(true);
        }

        apktest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    SharedPreferences.Editor ed = shared.edit();
                    ed.putBoolean("apktest",true);
                    ed.apply();
                } else {
                    SharedPreferences.Editor ed = shared.edit();
                    ed.putBoolean("apktest",false);
                    ed.apply();
                }
            }
        });
    }

    private int getIndex(Spinner spinner, int lev) {
        int index = 0;

        for(int i = 0; i< spinner.getCount();i++)
            if (lev == (int)spinner.getItemAtPosition(i))
                index = i;

        return index;
    }

    protected void restart() {
        Intent intent = new Intent(ConfigScreen.this,ConfigScreen.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        back.performClick();
    }
}
