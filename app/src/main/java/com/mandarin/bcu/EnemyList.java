package com.mandarin.bcu;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mandarin.bcu.androidutil.FilterEntity;
import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.enemy.adapters.EnemyListAdapter;
import com.mandarin.bcu.androidutil.enemy.asynchs.EAdder;
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder;

import java.util.ArrayList;
import java.util.Objects;

import common.system.fake.ImageBuilder;

public class EnemyList extends AppCompatActivity {
    private ListView list;
    private ArrayList<Integer> numbers = new ArrayList<>();

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
                setTheme(R.style.AppTheme_night);
            } else {
                setTheme(R.style.AppTheme_day);
            }
        }

        if(shared.getInt("Orientation",0) == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else if(shared.getInt("Orientation",0) == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        else if(shared.getInt("Orientation",0) == 0)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        setContentView(R.layout.activity_enemy_list);

        ImageBuilder.builder = new BMBuilder();

        FloatingActionButton back = findViewById(R.id.enlistbck);
        list = findViewById(R.id.enlist);
        FloatingActionButton search = findViewById(R.id.enlistsch);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        StaticStore.getEnemynumber();

        new EAdder(this,StaticStore.emnumber).execute();
    }

    protected void gotoFilter() {
        Intent intent = new Intent(EnemyList.this,EnemySearchFilter.class);
        startActivityForResult(intent,1);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(resultCode == Activity.RESULT_OK) {
            TextInputEditText schname = findViewById(R.id.enemlistschname);

            FilterEntity filterEntity;
            
            if(!Objects.requireNonNull(schname.getText()).toString().isEmpty())
                filterEntity = new FilterEntity(StaticStore.emnumber,schname.getText().toString());
            else
                filterEntity = new FilterEntity(StaticStore.emnumber);
            
            numbers = filterEntity.EsetFilter();
            ArrayList<String> newName = new ArrayList<>();

            for(int i : numbers)
                newName.add(StaticStore.enames[i]);

            EnemyListAdapter enemyListAdapter = new EnemyListAdapter(this,newName.toArray(new String[0]),numbers);
            list.setAdapter(enemyListAdapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(SystemClock.elapsedRealtime() - StaticStore.enemyinflistClick < StaticStore.INTERVAL)
                        return;

                    Intent result = new Intent(EnemyList.this,EnemyInfo.class);
                    result.putExtra("ID",numbers.get(position));
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
                    FilterEntity filterEntity = new FilterEntity(StaticStore.emnumber,s.toString());
                    numbers = filterEntity.EsetFilter();

                    ArrayList<String> names = new ArrayList<>();

                    for (int i : numbers) {
                        names.add(StaticStore.enames[i]);
                    }

                    EnemyListAdapter adap = new EnemyListAdapter(EnemyList.this, names.toArray(new String[0]), numbers);
                    list.setAdapter(adap);

                    if(s.toString().isEmpty()) {
                        schname.setCompoundDrawablesWithIntrinsicBounds(null,null,getDrawable(R.drawable.search),null);
                    } else {
                        schname.setCompoundDrawablesWithIntrinsicBounds(null,null,getDrawable(R.drawable.ic_close_black_24dp),null);
                    }
                }
            });
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences("configuration",Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase,shared.getInt("Language",0)));
    }

    @Override
    public void onBackPressed() {
        StaticStore.filterReset();
        super.onBackPressed();
    }
}
