package com.mandarin.bcu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.mandarin.bcu.androidutil.FilterEntity;
import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.EnemyListAdapter;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.asynchs.EAdder;
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder;

import java.util.ArrayList;

import common.system.fake.ImageBuilder;
import common.util.unit.Enemy;

public class EnemyList extends AppCompatActivity {
    private ArrayList<String> attack = new ArrayList<>();
    private ArrayList<String> trait = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> ability = new ArrayList<>();
    private boolean atksimu = true;
    private boolean atkorand = true;
    private boolean trorand = true;
    private boolean aborand = true;
    private boolean empty = true;

    private ListView list;
    private ImageButton search;

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

        if(savedInstanceState != null) {
            empty = savedInstanceState.getBoolean("empty");
            trorand = savedInstanceState.getBoolean("trorand");
            atksimu = savedInstanceState.getBoolean("atksimu");
            atkorand = savedInstanceState.getBoolean("atkorand");
            aborand = savedInstanceState.getBoolean("aborand");
            trait = savedInstanceState.getStringArrayList("trait");
            attack = savedInstanceState.getStringArrayList("attack");
            ability = (ArrayList<ArrayList<Integer>>)savedInstanceState.getSerializable("ability");
        }

        if(shared.getInt("Orientation",0) == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else if(shared.getInt("Orientation",0) == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        else if(shared.getInt("Orientation",0) == 0)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        setContentView(R.layout.activity_enemy_list);

        ImageBuilder.builder = new BMBuilder();

        ImageButton back = findViewById(R.id.enlistbck);
        list = findViewById(R.id.enlist);
        search = findViewById(R.id.enlistsch);

        back.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
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

        new EAdder(this,attack,trait,ability,atksimu,atkorand,trorand,aborand,empty,StaticStore.emnumber).execute();
    }

    protected void gotoFilter() {
        Intent intent = new Intent(EnemyList.this,EnemySearchFilter.class);

        intent.putExtra("empty",empty);
        intent.putExtra("trorand",trorand);
        intent.putExtra("atksimu",atksimu);
        intent.putExtra("atkorand",atkorand);
        intent.putExtra("aborand",aborand);
        intent.putExtra("trait",trait);
        intent.putExtra("attack",attack);
        intent.putExtra("ability",ability);
        setResult(Activity.RESULT_OK,intent);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(resultCode == Activity.RESULT_OK) {
            assert data != null;
            Bundle extra = data.getExtras();
            assert extra != null;

            empty = extra.getBoolean("empty");
            attack = extra.getStringArrayList("attack");
            trait = extra.getStringArrayList("trait");
            ability = (ArrayList<ArrayList<Integer>>) extra.getSerializable("ability");
            atksimu = extra.getBoolean("atksimu");
            atkorand = extra.getBoolean("atkorand");
            trorand = extra.getBoolean("trorand");
            aborand = extra.getBoolean("aborand");

            FilterEntity filterEntity = new FilterEntity(attack,trait,ability,atksimu,atkorand,trorand,aborand,empty,StaticStore.emnumber);
            ArrayList<Integer> newNumber = filterEntity.EsetFilter();
            ArrayList<String> newName = new ArrayList<>();

            for(int i : newNumber)
                newName.add(StaticStore.enames[i]);

            EnemyListAdapter enemyListAdapter = new EnemyListAdapter(this,newName.toArray(new String[0]),StaticStore.ebitmaps,newNumber);
            list.setAdapter(enemyListAdapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(SystemClock.elapsedRealtime() - StaticStore.enemyinflistClick < StaticStore.INTERVAL)
                        return;

                    Intent result = new Intent(EnemyList.this,EnemyInfo.class);
                    result.putExtra("ID",newNumber.get(position));
                    startActivity(result);

                    StaticStore.unitinflistClick = SystemClock.elapsedRealtime();
                }
            });

            search.setOnClickListener(new SingleClick() {
                @Override
                public void onSingleClick(View v) {
                    Intent intent = new Intent(EnemyList.this,EnemySearchFilter.class);

                    intent.putExtra("empty",empty);
                    intent.putExtra("trorand",trorand);
                    intent.putExtra("atksimu",atksimu);
                    intent.putExtra("atkorand",atkorand);
                    intent.putExtra("aborand",aborand);
                    intent.putExtra("trait",trait);
                    intent.putExtra("attack",attack);
                    intent.putExtra("ability",ability);
                    setResult(Activity.RESULT_OK,intent);
                    startActivityForResult(intent,1);
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
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean("empty",empty);
        bundle.putBoolean("trorand",trorand);
        bundle.putBoolean("atksimu",atksimu);
        bundle.putBoolean("atkorand",atkorand);
        bundle.putBoolean("aborand",aborand);
        bundle.putStringArrayList("trait",trait);
        bundle.putStringArrayList("attack",attack);
        bundle.putSerializable("ability",ability);

        super.onSaveInstanceState(bundle);
    }
}
