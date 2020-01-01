package com.mandarin.bcu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.getStrings;
import com.mandarin.bcu.androidutil.unit.asynchs.UInfoLoader;

public class UnitInfo extends AppCompatActivity {
    private FloatingActionButton treasure;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

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

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(shared.getBoolean("Lay_Land",false)) {
                setContentView(R.layout.activity_unit_info);
            } else {
                setContentView(R.layout.activity_unit_infor);
            }
        } else {
            if(shared.getBoolean("Lay_Port",true)) {
                setContentView(R.layout.activity_unit_info);
            } else {
                setContentView(R.layout.activity_unit_infor);
            }
        }
        if(StaticStore.unitinfreset) {
            StaticStore.unittabposition = 0;
            StaticStore.unitinfreset = false;
        }

        ScrollView scrollView = findViewById(R.id.unitinfscroll);
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scrollView.setFocusable(false);
        scrollView.setFocusableInTouchMode(true);
        scrollView.setVisibility(View.GONE);

        ConstraintLayout treasuretab = findViewById(R.id.treasurelayout);
        treasuretab.setVisibility(View.GONE);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(shared.getBoolean("Lay_Land",false)) {
                ViewPager unittable = findViewById(R.id.unitinftable);
                unittable.setFocusable(false);
                unittable.requestFocusFromTouch();
            } else {
                RecyclerView recyclerView = findViewById(R.id.unitinfrec);
                recyclerView.requestFocusFromTouch();
            }
        } else {
            if(shared.getBoolean("Lay_Port",false)) {
                ViewPager unittable = findViewById(R.id.unitinftable);
                unittable.setFocusable(false);
                unittable.requestFocusFromTouch();
            } else {
                RecyclerView recyclerView = findViewById(R.id.unitinfrec);
                recyclerView.requestFocusFromTouch();
            }
        }

        TextView unittitle = findViewById(R.id.unitinfrarname);

        FloatingActionButton back = findViewById(R.id.unitinfback);
        treasure = findViewById(R.id.treabutton);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticStore.unitinfreset = true;
                StaticStore.UisOpen = false;
                finish();
            }
        });

        Intent result = getIntent();
        Bundle extra = result.getExtras();

        if(extra != null) {
            int id = extra.getInt("ID");
            getStrings s = new getStrings(this);
            unittitle.setText(s.getTitle(StaticStore.units.get(id).forms[0]));

            Button anim = findViewById(R.id.animanim);

            anim.setOnClickListener(new SingleClick() {
                @Override
                public void onSingleClick(View v) {
                    Intent intent = new Intent(UnitInfo.this,ImageViewer.class);

                    StaticStore.formposition = StaticStore.unittabposition;

                    intent.putExtra("Img",2);
                    intent.putExtra("ID",id);
                    intent.putExtra("Form",StaticStore.formposition);

                    startActivity(intent);
                }
            });

            new UInfoLoader(id,this,getSupportFragmentManager()).execute();
        }
    }

    @Override
    public void onBackPressed() {
        if(StaticStore.UisOpen) {
            treasure.performClick();
        } else {
            super.onBackPressed();
            StaticStore.unitinfreset = true;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences("configuration",Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase,shared.getInt("Language",0)));
    }

}
