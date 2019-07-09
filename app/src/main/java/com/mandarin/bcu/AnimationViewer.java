package com.mandarin.bcu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.mandarin.bcu.androidutil.FilterEntity;
import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.UnitListAdapter;
import com.mandarin.bcu.androidutil.asynchs.Adder;
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder;

import common.system.MultiLangCont;
import common.system.fake.ImageBuilder;
import common.util.unit.Form;

import java.util.ArrayList;

public class AnimationViewer extends AppCompatActivity {

    protected ImageButton search;
    private ListView list;
    private int unitnumber;
    static final int REQUEST_CODE = 1;

    private ArrayList<String> rarity = new ArrayList<>();
    private ArrayList<String> attack = new ArrayList<>();
    private ArrayList<String> target = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> ability = new ArrayList<>();
    private boolean atksimu = true;
    private boolean atkorand = true;
    private boolean tgorand = true;
    private boolean aborand = true;
    private boolean empty = true;
    private boolean talents = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences shared = getSharedPreferences("configuration",MODE_PRIVATE);

        if(shared.getInt("Orientation",0) == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else if(shared.getInt("Orientation",0) == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        else if(shared.getInt("Orientation",0) == 0)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

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
            tgorand = savedInstanceState.getBoolean("tgorand");
            atksimu = savedInstanceState.getBoolean("atksimu");
            aborand = savedInstanceState.getBoolean("aborand");
            atkorand = savedInstanceState.getBoolean("atkorand");
            talents = savedInstanceState.getBoolean("talents");
            target = savedInstanceState.getStringArrayList("target");
            attack = savedInstanceState.getStringArrayList("attack");
            rarity = savedInstanceState.getStringArrayList("rare");
            ability = (ArrayList<ArrayList<Integer>>)savedInstanceState.getSerializable("ability");
        }

        if(shared.getInt("Orientation",0) == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else if(shared.getInt("Orientation",0) == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        setContentView(R.layout.activity_animation_viewer);

        ImageBuilder.builder = new BMBuilder();

        unitnumber = StaticStore.unitnumber;

        ImageButton back = findViewById(R.id.animbck);
        search = findViewById(R.id.animsch);

        list = findViewById(R.id.unitinflist);

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

        new Adder(unitnumber,this,empty,tgorand,atksimu,aborand,atkorand,talents,target,attack,rarity,ability).execute();
    }

    protected String showName(int location) {
        ArrayList<String> names = new ArrayList<>();

        for(Form f : StaticStore.units.get(location).forms) {
            String name = MultiLangCont.FNAME.getCont(f);

            if(name == null)
                name = "";

            names.add(name);
        }

        StringBuilder result = new StringBuilder(withID(location, names.get(0)));

        for(int i = 1; i < names.size();i++) {
            result.append(" - ").append(names.get(i));
        }

        return result.toString();
    }

    protected String withID(int id, String name) {
        String result;

        if(name.equals("")) {
            result = number(id);
        } else {
            result = number(id)+" - "+name;
        }

        return result;
    }

    protected void gotoFilter() {
        Intent intent = new Intent(AnimationViewer.this,SearchFilter.class);

        intent.putExtra("empty",empty);
        intent.putExtra("tgorand",tgorand);
        intent.putExtra("atksimu",atksimu);
        intent.putExtra("aborand",aborand);
        intent.putExtra("atkorand",atkorand);
        intent.putExtra("talents",talents);
        intent.putExtra("target",target);
        intent.putExtra("attack",attack);
        intent.putExtra("rare",rarity);
        intent.putExtra("ability",ability);
        setResult(Activity.RESULT_OK,intent);
        startActivityForResult(intent,REQUEST_CODE);
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

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            assert data != null;
            Bundle extra = data.getExtras();

            assert extra != null;
            empty = extra.getBoolean("empty");
            rarity = extra.getStringArrayList("rare");
            attack = extra.getStringArrayList("attack");
            target = extra.getStringArrayList("target");
            ability = (ArrayList<ArrayList<Integer>>) extra.getSerializable("ability");
            atksimu = extra.getBoolean("atksimu");
            atkorand = extra.getBoolean("atkorand");
            tgorand = extra.getBoolean("tgorand");
            aborand = extra.getBoolean("aborand");
            talents = extra.getBoolean("talents");

            FilterEntity filterEntity = new FilterEntity(rarity,attack,target,ability,atksimu,atkorand,tgorand,aborand,empty,unitnumber,talents);
            ArrayList<Integer> newNumber = filterEntity.setFilter();
            ArrayList<String> newName = new ArrayList<>();

            for(int i : newNumber) {
                newName.add(StaticStore.names[i]);
            }

            UnitListAdapter unitListAdapter = new UnitListAdapter(this,newName.toArray(new String[0]),StaticStore.bitmaps,newNumber);
            list.setAdapter(unitListAdapter);
            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(AnimationViewer.this,showName(newNumber.get(position)),Toast.LENGTH_SHORT).show();
                    list.setClickable(false);
                    return true;
                }
            });
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(SystemClock.elapsedRealtime() - StaticStore.unitinflistClick < StaticStore.INTERVAL)
                        return;

                    Intent result = new Intent(AnimationViewer.this,UnitInfo.class);
                    result.putExtra("ID",newNumber.get(position));
                    startActivity(result);

                    StaticStore.unitinflistClick = SystemClock.elapsedRealtime();
                }
            });

            search.setOnClickListener(new SingleClick() {
                @Override
                public void onSingleClick(View v) {
                    Intent intent = new Intent(AnimationViewer.this,SearchFilter.class);

                    intent.putExtra("empty",empty);
                    intent.putExtra("tgorand",tgorand);
                    intent.putExtra("atksimu",atksimu);
                    intent.putExtra("aborand",aborand);
                    intent.putExtra("atkorand",atkorand);
                    intent.putExtra("talents",talents);
                    intent.putExtra("target",target);
                    intent.putExtra("attack",attack);
                    intent.putExtra("rare",rarity);
                    intent.putExtra("ability",ability);
                    setResult(Activity.RESULT_OK,intent);
                    startActivityForResult(intent,REQUEST_CODE);
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
        bundle.putBoolean("tgorand",tgorand);
        bundle.putBoolean("atksimu",atksimu);
        bundle.putBoolean("aborand",aborand);
        bundle.putBoolean("atkorand",atkorand);
        bundle.putBoolean("talents",talents);
        bundle.putStringArrayList("target",target);
        bundle.putStringArrayList("attack",attack);
        bundle.putStringArrayList("rare",rarity);
        bundle.putSerializable("ability",ability);

        super.onSaveInstanceState(bundle);
    }
}
