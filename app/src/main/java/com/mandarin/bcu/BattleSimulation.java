package com.mandarin.bcu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.battle.BattleView;
import com.mandarin.bcu.androidutil.battle.asynchs.BAdder;
import com.mandarin.bcu.androidutil.fakeandroid.AndroidKeys;
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics;
import com.mandarin.bcu.util.page.BattleBox;

import common.battle.BasisSet;
import common.battle.SBCtrl;
import common.system.P;
import common.util.stage.MapColc;
import common.util.stage.Stage;
import common.util.stage.StageMap;

public class BattleSimulation extends AppCompatActivity {

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

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_battle_simulation);

        Intent intent = getIntent();

        if(intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();

            int mapcode = bundle.getInt("mapcode");
            int stid = bundle.getInt("stid");
            int posit = bundle.getInt("stage");

            LinearLayout layout = findViewById(R.id.battlelayout);

            MapColc mc = StaticStore.map.get(mapcode);

            if(mc == null)
                return;

            StageMap stm = mc.maps[stid];

            if(stm == null)
                return;

            Stage stg = stm.list.get(posit);

            if(stg == null)
                return;

            SBCtrl ctrl = new SBCtrl(new AndroidKeys(),stg,0,BasisSet.current.sele,new int [] {0},0L);

            BattleBox.OuterBox outerBox = new BattleBox.OuterBox() {
                @Override
                public int getSpeed() {
                    return 0;
                }

                @Override
                public void callBack(Object o) {

                }
            };

            boolean axis = shared.getBoolean("Axis",true);

            BattleView view = new BattleView(this,outerBox,ctrl,1,axis);
            view.initialized = false;
            view.setLayerType(View.LAYER_TYPE_HARDWARE,null);
            view.setId(R.id.battleView);
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            layout.addView(view);

            new BAdder(this).execute();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public void onBackPressed() {
        P.stack.clear();
        CVGraphics.clear();
        super.onBackPressed();
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
}
