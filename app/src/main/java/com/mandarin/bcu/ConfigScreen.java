package com.mandarin.bcu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;

import java.util.ArrayList;
import java.util.List;

import common.CommonStatic;

public class ConfigScreen extends AppCompatActivity {
    SharedPreferences shared;
    FloatingActionButton back;
    private int[] LangId = {R.string.lang_auto, R.string.def_lang_en, R.string.def_lang_zh, R.string.def_lang_ko, R.string.def_lang_ja};
    private String[] locales = StaticStore.lang;
    private boolean started = false;
    private boolean changed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shared = getSharedPreferences(StaticStore.CONFIG, MODE_PRIVATE);
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

        setContentView(R.layout.activity_config_screen);

        back = findViewById(R.id.configback);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfigScreen.this, MainActivity.class);
                intent.putExtra("Config", true);
                startActivity(intent);
                finish();
            }
        });

        RadioButton day = findViewById(R.id.themeday);
        RadioButton night = findViewById(R.id.themenight);
        RadioButton frames = findViewById(R.id.configframe);
        RadioButton seconds = findViewById(R.id.configsecond);

        if (shared.contains("initial")) {
            if (!shared.getBoolean("theme", false))
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
                if (checkedId == day.getId()) {
                    SharedPreferences.Editor ed = shared.edit();
                    ed.putBoolean("theme", true);
                    ed.apply();
                    restart();
                } else {
                    SharedPreferences.Editor ed = shared.edit();
                    ed.putBoolean("theme", false);
                    ed.apply();
                    restart();
                }
            }
        });

        RadioGroup frse = findViewById(R.id.configfrse);
        frse.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == frames.getId()) {
                    SharedPreferences.Editor ed = shared.edit();
                    ed.putBoolean("frame", true);
                    ed.apply();
                } else {
                    SharedPreferences.Editor ed = shared.edit();
                    ed.putBoolean("frame", false);
                    ed.apply();
                }
            }
        });

        List<Integer> levels = new ArrayList<>();
        for (int j = 1; j < 51; j++)
            levels.add(j);

        Spinner deflev = findViewById(R.id.configdeflevsp);
        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<>(this, R.layout.spinneradapter, levels);
        deflev.setAdapter(arrayAdapter);
        deflev.setSelection(getIndex(deflev, shared.getInt("default_level", 50)));

        deflev.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor ed = shared.edit();
                ed.putInt("default_level", (int) deflev.getSelectedItem());
                ed.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        System.out.println(CommonStatic.Lang.lang);

        Switch apktest = findViewById(R.id.apktest);

        if (!shared.getBoolean("apktest", false)) {
            apktest.setChecked(false);
        } else {
            apktest.setChecked(true);
        }

        apktest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedPreferences.Editor ed = shared.edit();
                    ed.putBoolean("apktest", true);
                    ed.apply();
                } else {
                    SharedPreferences.Editor ed = shared.edit();
                    ed.putBoolean("apktest", false);
                    ed.apply();
                }
            }
        });

        Spinner language = findViewById(R.id.configlangsp);

        List<String> lang = new ArrayList<>();
        for (int i1 : LangId) {
            lang.add(getString(i1));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinneradapter, lang);
        language.setAdapter(adapter);
        language.setSelection(shared.getInt("Language", 0));

        language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (started) {
                    changed = true;
                    SharedPreferences.Editor ed = shared.edit();
                    ed.putInt("Language", position);
                    ed.apply();

                    String lang = locales[position];

                    if (lang.equals(""))
                        lang = Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();

                    if (StaticStore.units != null || StaticStore.enemies != null)
                        new Revalidater(ConfigScreen.this).Validate(lang, ConfigScreen.this);
                    else {
                        StaticStore.getLang(position);
                    }

                    restart();
                }

                started = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        RadioGroup orientation = findViewById(R.id.configorirg);
        RadioButton[] oris = {findViewById(R.id.configoriauto), findViewById(R.id.configoriland), findViewById(R.id.configoriport)};

        orientation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (started)
                    for (int i = 0; i < 3; i++)
                        if (i != shared.getInt("Orientation", 0) && checkedId == oris[i].getId()) {
                            SharedPreferences.Editor ed = shared.edit();
                            ed.putInt("Orientation", i);
                            ed.apply();
                            restart();
                        }
            }
        });

        oris[shared.getInt("Orientation", 0)].setChecked(true);

        RadioGroup unitinfland = findViewById(R.id.configinfland);
        RadioButton unitinflandlist = findViewById(R.id.configlaylandlist);
        RadioButton unitinflandslide = findViewById(R.id.configlaylandslide);

        if (shared.getBoolean("Lay_Land", true))
            unitinflandslide.setChecked(true);
        else
            unitinflandlist.setChecked(true);

        unitinfland.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SharedPreferences.Editor ed = shared.edit();
                ed.putBoolean("Lay_Land", checkedId == unitinflandslide.getId());
                ed.apply();
            }
        });

        RadioGroup unitinfport = findViewById(R.id.configinfport);
        RadioButton unitinfportlist = findViewById(R.id.configlayportlist);
        RadioButton unitinfportslide = findViewById(R.id.configlayportslide);

        if (shared.getBoolean("Lay_Port", true))
            unitinfportslide.setChecked(true);
        else
            unitinfportlist.setChecked(true);

        unitinfport.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SharedPreferences.Editor ed = shared.edit();
                ed.putBoolean("Lay_Port", checkedId == unitinfportslide.getId());
                ed.apply();
            }
        });

        Switch skiptext = findViewById(R.id.configskiptext);

        if (shared.getBoolean("Skip_Text", false))
            skiptext.setChecked(true);
        else
            skiptext.setChecked(false);

        skiptext.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor ed = shared.edit();
                ed.putBoolean("Skip_Text", isChecked);
                ed.apply();
            }
        });

        Button Checkupdate = findViewById(R.id.configcheckup);

        Checkupdate.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(ConfigScreen.this, CheckUpdateScreen.class);
                intent.putExtra("Config", true);
                startActivity(intent);
                finish();
            }
        });

        Switch axis = findViewById(R.id.configaxis);

        if (shared.getBoolean("Axis", true))
            axis.setChecked(true);
        else
            axis.setChecked(false);

        axis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor ed = shared.edit();
                ed.putBoolean("Axis", isChecked);
                ed.apply();
            }
        });

        Switch fps = findViewById(R.id.configfps);

        if (shared.getBoolean("FPS", true))
            fps.setChecked(true);
        else
            fps.setChecked(false);

        fps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor ed = shared.edit();
                ed.putBoolean("FPS", isChecked);
                ed.apply();
            }
        });
    }

    private int getIndex(Spinner spinner, int lev) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++)
            if (lev == (int) spinner.getItemAtPosition(i))
                index = i;

        return index;
    }

    protected void restart() {
        Intent intent = new Intent(ConfigScreen.this, ConfigScreen.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        back.performClick();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase, shared.getInt("Language", 0)));
    }
}
