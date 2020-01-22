package com.mandarin.bcu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mandarin.bcu.androidutil.FilterEntity;
import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder;
import com.mandarin.bcu.androidutil.unit.adapters.UnitListAdapter;
import com.mandarin.bcu.androidutil.unit.asynchs.Adder;

import java.util.ArrayList;
import java.util.Objects;

import common.system.MultiLangCont;
import common.system.fake.ImageBuilder;
import common.util.unit.Form;

public class AnimationViewer extends AppCompatActivity {

    protected FloatingActionButton search;
    private ListView list;
    static final int REQUEST_CODE = 1;
    private ArrayList<Integer> numbers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences shared = getSharedPreferences(StaticStore.CONFIG, MODE_PRIVATE);

        if (shared.getInt("Orientation", 0) == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else if (shared.getInt("Orientation", 0) == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        else if (shared.getInt("Orientation", 0) == 0)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

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

        setContentView(R.layout.activity_animation_viewer);

        ImageBuilder.builder = new BMBuilder();

        FloatingActionButton back = findViewById(R.id.animbck);
        search = findViewById(R.id.animsch);

        list = findViewById(R.id.unitinflist);

        back.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                StaticStore.filterReset();
                finish();
            }
        });

        search.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                gotoFilter();
            }
        });

        new Adder(this).execute();
    }

    protected String showName(int location) {
        ArrayList<String> names = new ArrayList<>();

        for (Form f : StaticStore.units.get(location).forms) {
            String name = MultiLangCont.FNAME.getCont(f);

            if (name == null)
                name = "";

            names.add(name);
        }

        StringBuilder result = new StringBuilder(withID(location, names.get(0)));

        for (int i = 1; i < names.size(); i++) {
            result.append(" - ").append(names.get(i));
        }

        return result.toString();
    }

    protected String withID(int id, String name) {
        String result;

        if (name.equals("")) {
            result = number(id);
        } else {
            result = number(id) + " - " + name;
        }

        return result;
    }

    protected void gotoFilter() {
        Intent intent = new Intent(AnimationViewer.this, SearchFilter.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    protected String number(int num) {
        if (0 <= num && num < 10) {
            return "00" + num;
        } else if (10 <= num && num <= 99) {
            return "0" + num;
        } else {
            return String.valueOf(num);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            TextInputEditText schname = findViewById(R.id.animschname);

            FilterEntity filterEntity;

            if (Objects.requireNonNull(schname.getText()).toString().isEmpty())
                filterEntity = new FilterEntity(StaticStore.unitnumber);
            else
                filterEntity = new FilterEntity(StaticStore.unitnumber, schname.getText().toString());

            numbers = filterEntity.setFilter();
            ArrayList<String> newName = new ArrayList<>();

            for (int i : numbers) {
                newName.add(StaticStore.names[i]);
            }

            UnitListAdapter unitListAdapter = new UnitListAdapter(this, newName.toArray(new String[0]), numbers);
            list.setAdapter(unitListAdapter);
            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(AnimationViewer.this, showName(numbers.get(position)), Toast.LENGTH_SHORT).show();
                    list.setClickable(false);
                    return true;
                }
            });

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (SystemClock.elapsedRealtime() - StaticStore.unitinflistClick < StaticStore.INTERVAL)
                        return;

                    Intent result = new Intent(AnimationViewer.this, UnitInfo.class);
                    result.putExtra("ID", numbers.get(position));
                    startActivity(result);

                    StaticStore.unitinflistClick = SystemClock.elapsedRealtime();
                }
            });

            schname.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    FilterEntity filterEntity = new FilterEntity(StaticStore.unitnumber, s.toString());
                    numbers = filterEntity.setFilter();

                    ArrayList<String> names = new ArrayList<>();

                    for (int i : numbers) {
                        names.add(StaticStore.names[i]);
                    }

                    UnitListAdapter adap = new UnitListAdapter(AnimationViewer.this, names.toArray(new String[0]), numbers);
                    list.setAdapter(adap);
                }
            });
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase, shared.getInt("Language", 0)));
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        StaticStore.filterReset();
    }
}
