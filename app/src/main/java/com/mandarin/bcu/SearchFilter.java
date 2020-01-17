package com.mandarin.bcu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;

import java.util.ArrayList;

import static common.util.Data.AB_BASE;
import static common.util.Data.AB_EARN;
import static common.util.Data.AB_EKILL;
import static common.util.Data.AB_GOOD;
import static common.util.Data.AB_MASSIVE;
import static common.util.Data.AB_MASSIVES;
import static common.util.Data.AB_METALIC;
import static common.util.Data.AB_ONLY;
import static common.util.Data.AB_RESIST;
import static common.util.Data.AB_RESISTS;
import static common.util.Data.AB_WAVES;
import static common.util.Data.AB_WKILL;
import static common.util.Data.AB_ZKILL;
import static common.util.Data.P_BREAK;
import static common.util.Data.P_CRIT;
import static common.util.Data.P_IMUATK;
import static common.util.Data.P_IMUCURSE;
import static common.util.Data.P_IMUKB;
import static common.util.Data.P_IMUSLOW;
import static common.util.Data.P_IMUSTOP;
import static common.util.Data.P_IMUWARP;
import static common.util.Data.P_IMUWAVE;
import static common.util.Data.P_IMUWEAK;
import static common.util.Data.P_KB;
import static common.util.Data.P_LETHAL;
import static common.util.Data.P_SATK;
import static common.util.Data.P_SLOW;
import static common.util.Data.P_STOP;
import static common.util.Data.P_STRONG;
import static common.util.Data.P_WARP;
import static common.util.Data.P_WAVE;
import static common.util.Data.P_WEAK;

public class SearchFilter extends AppCompatActivity {

    private FloatingActionButton back;
    private FloatingActionButton reset;
    private RadioButton tgor;
    private RadioButton atkmu;
    private RadioButton atkor;
    private RadioButton abor;
    private RadioGroup tggroup;
    private RadioGroup atkgroup;
    private RadioGroup atkgroupor;
    private RadioGroup abgroup;
    private CheckBox chnp;
    private ScrollView sc;
    private NestedScrollView nsc;
    private int[] tgid = {R.id.schchrd,R.id.schchfl,R.id.schchbla,R.id.schchme,R.id.schchan,R.id.schchal,R.id.schchzo,R.id.schchre,R.id.schchwh};
    private String [] colors = {"1","2","3","4","5","6","7","8","0"};
    private int[] rareid = {R.id.schchba,R.id.schchex,R.id.schchr,R.id.schchsr,R.id.schchur,R.id.schchlr};
    private String [] rarity = {"0","1","2","3","4","5"};
    private int[] atkid = {R.id.schchld,R.id.schchom,R.id.schchmu};
    private String [] atks = {"2","4","3"};
    private int[] abid = {R.id.schchabwe,R.id.schchabfr,R.id.schchabsl,R.id.schchabta,R.id.schchabst,R.id.schchabre,R.id.schchabir,R.id.schchabmd,R.id.schchabid,R.id.schchabkb,R.id.schchabwp,R.id.schchabstr,R.id.schchabsu,R.id.schchabcd,
            R.id.schchabcr,R.id.schchabzk,R.id.schchabbb,R.id.schchabem,R.id.schchabme,R.id.schchabwv,R.id.schchabimwe,R.id.schchabimfr,R.id.schchabimsl,R.id.schchabimkb,R.id.schchabimwv,R.id.schchabimwp,R.id.schchabimcu,R.id.schchabws,
            R.id.schchabwik,R.id.schchabevk,R.id.schchabsb,R.id.schchabiv};
    private int[] abtool = {R.string.sch_abi_we,R.string.sch_abi_fr,R.string.sch_abi_sl,R.string.sch_abi_ao,R.string.sch_abi_st,R.string.sch_abi_re,R.string.sch_abi_it,R.string.sch_abi_md,R.string.sch_abi_id,R.string.sch_abi_kb,
            R.string.sch_abi_wa,R.string.sch_abi_str,R.string.sch_abi_su,R.string.sch_abi_bd,R.string.sch_abi_cr,R.string.sch_abi_zk,R.string.sch_abi_bb,R.string.sch_abi_em,R.string.sch_abi_me,R.string.sch_abi_wv,
            R.string.sch_abi_iw,R.string.sch_abi_if,R.string.sch_abi_is,R.string.sch_abi_ik,R.string.sch_abi_iwv,R.string.sch_abi_iwa,R.string.sch_abi_ic,R.string.sch_abi_ws,R.string.sch_abi_wk,R.string.sch_abi_eva,R.string.sch_abi_sb,R.string.sch_abi_iv};
    private int[] tgtool = {R.string.sch_red,R.string.sch_fl,R.string.sch_bla,R.string.sch_me,R.string.sch_an,R.string.sch_al,R.string.sch_zo,R.string.sch_re,R.string.sch_wh};
    private int [][] abils = {{1,P_WEAK},{1,P_STOP},{1,P_SLOW},{0,AB_ONLY},{0,AB_GOOD},{0,AB_RESIST},{0,AB_RESISTS},{0,AB_MASSIVE},{0,AB_MASSIVES},{1,P_KB},{1,P_WARP},{1,P_STRONG},{1,P_LETHAL},{0,AB_BASE},{1,P_CRIT},{0,AB_ZKILL},{1,P_BREAK},
            {0,AB_EARN},{0,AB_METALIC},{1,P_WAVE},{1,P_IMUWEAK},{1,P_IMUSTOP},{1,P_IMUSLOW},{1,P_IMUKB},{1,P_IMUWAVE},{1,P_IMUWARP},{1,P_IMUCURSE},{0,AB_WAVES},{0,AB_WKILL},{0,AB_EKILL},{1,P_SATK},{1,P_IMUATK}};

    private CheckBox[] rarities = new CheckBox[rareid.length];
    private CheckBox[] targets = new CheckBox[tgid.length];
    private CheckBox[] attacks = new CheckBox[atkid.length];
    private CheckBox[] abilities = new CheckBox[abid.length];

    private int [] atkdraw = {212,112};
    private int [] tgdraw = {219,220,221,222,223,224,225,226,227};
    private int [] abdraw = {195,197,198,202,203,204,122,206,114,207,266,196,199,200,201,260,264,205,209,208,213,214,215,216,210,262,116,218,258,110,229,231};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(StaticStore.img15 == null)
            StaticStore.readImg();

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

        setContentView(R.layout.activity_search_filter);

        back = findViewById(R.id.schbck);
        reset = findViewById(R.id.schreset);
        tgor = findViewById(R.id.schrdtgor);
        atkmu = findViewById(R.id.schrdatkmu);
        atkmu.setCompoundDrawablePadding(StaticStore.dptopx(16f,this));
        RadioButton atksi = findViewById(R.id.schrdatksi);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            atkmu.setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(211,40f),null);
            atksi.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(217, 40f), null);
        } else {
            atkmu.setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(211,32f),null);
            atksi.setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(217, 32f), null);
        }
        atksi.setCompoundDrawablePadding(StaticStore.dptopx(16f,this));
        atkor = findViewById(R.id.schrdatkor);
        abor = findViewById(R.id.schrdabor);
        tggroup = findViewById(R.id.schrgtg);
        atkgroup = findViewById(R.id.schrgatk);
        atkgroupor = findViewById(R.id.schrgatkor);
        abgroup = findViewById(R.id.schrgab);
        sc = findViewById(R.id.animsc);
        nsc = findViewById(R.id.animnscview);
        chnp = findViewById(R.id.schnp);
        for(int i = 0; i < tgid.length; i++) {
            targets[i] = findViewById(tgid[i]);
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                targets[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(tgdraw[i],40f),null);
            else
                targets[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(tgdraw[i],32f),null);
            targets[i].setCompoundDrawablePadding(StaticStore.dptopx(16f,this));
        }
        for(int i=0;i<rareid.length;i++)
            rarities[i] = findViewById(rareid[i]);
        for(int i=0;i<atkid.length;i++) {
            attacks[i] = findViewById(atkid[i]);

            if(i <atkid.length-1) {
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    attacks[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(atkdraw[i],40f),null);
                else
                    attacks[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(atkdraw[i],32f),null);
                attacks[i].setCompoundDrawablePadding(StaticStore.dptopx(8f,this));
            }
        }
        for(int i=0;i<abid.length;i++) {
            abilities[i] = findViewById(abid[i]);
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                abilities[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(abdraw[i],40f),null);
            else
                abilities[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(abdraw[i],32f),null);
            abilities[i].setCompoundDrawablePadding(StaticStore.dptopx(16f,this));
        }

        tgor.setChecked(true);
        atkor.setChecked(true);
        abor.setChecked(true);

        Checker();

        Listeners();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void Listeners() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returner();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticStore.filterReset();

                atkgroup.clearCheck();
                tgor.setChecked(true);
                atkor.setChecked(true);
                abor.setChecked(true);
                chnp.setChecked(false);

                for (CheckBox rarity1 : rarities) {
                    if (rarity1.isChecked())
                        rarity1.setChecked(false);
                }

                for (CheckBox attack1 : attacks) {
                    if (attack1.isChecked())
                        attack1.setChecked(false);
                }

                for (CheckBox target : targets) {
                    if (target.isChecked())
                        target.setChecked(false);
                }

                for (CheckBox ability1 : abilities)
                    if (ability1.isChecked())
                        ability1.setChecked(false);
            }
        });

        tggroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                StaticStore.tgorand = checkedId == tgor.getId();
            }
        });

        atkgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                StaticStore.atksimu = checkedId == atkmu.getId();
            }
        });

        atkgroupor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                StaticStore.atkorand = checkedId == atkor.getId();
            }
        });

        abgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                StaticStore.aborand = checkedId == abor.getId();
            }
        });

        for(int i=0;i<targets.length;i++) {
            final int finalI = i;
            targets[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                        StaticStore.tg.add(colors[finalI]);
                    else
                        StaticStore.tg.remove(colors[finalI]);
                }
            });

            targets[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(v.getContext(), tgtool[finalI], Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }

        for(int i=0;i<rarities.length;i++) {
            final int finall = i;
            rarities[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                        StaticStore.rare.add(rarity[finall]);
                    else
                        StaticStore.rare.remove(rarity[finall]);
                }
            });
        }

        for(int i=0;i<attacks.length;i++) {
            final int finall = i;
            attacks[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                        StaticStore.attack.add(atks[finall]);
                    else
                        StaticStore.attack.remove(atks[finall]);
                }
            });
        }

        for(int i=0;i<abilities.length;i++) {
            final int finall = i;
            abilities[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(v.getContext(),abtool[finall],Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            abilities[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ArrayList<Integer> abilval = new ArrayList<>();
                    for(int i : abils[finall])
                        abilval.add(i);

                    if(isChecked) {
                        StaticStore.ability.add(abilval);
                    }
                    else
                        StaticStore.ability.remove(abilval);
                }
            });
        }

        nsc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sc.requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        sc.requestDisallowInterceptTouchEvent(false);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        chnp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StaticStore.talents = isChecked;
            }
        });
    }

    protected void returner() {
        Intent result = new Intent();

        StaticStore.empty = atkgroup.getCheckedRadioButtonId() == -1;
        setResult(RESULT_OK,result);
        StaticStore.updateList = true;
        finish();
    }

    protected void Checker() {
        if (!StaticStore.empty)
            atkgroup.check(R.id.schrdatkmu);

        if (!StaticStore.atksimu)
            if (!StaticStore.empty)
                atkgroup.check(R.id.schrdatksi);

        if (!StaticStore.atkorand)
            atkgroupor.check(R.id.schrdatkand);

        if (!StaticStore.tgorand)
            tggroup.check(R.id.schrdtgand);

        if (!StaticStore.aborand)
            abgroup.check(R.id.schrdaband);

        for(int i=0;i<rarity.length;i++)
            if (StaticStore.rare != null && StaticStore.rare.contains(rarity[i]))
                rarities[i].setChecked(true);

        for(int i=0;i<atks.length;i++)
            if (StaticStore.attack != null && StaticStore.attack.contains(atks[i]))
                attacks[i].setChecked(true);

        for(int i=0;i<colors.length;i++)
            if (StaticStore.tg != null && StaticStore.tg.contains(colors[i]))
                targets[i].setChecked(true);

        for(int i =0;i<abils.length;i++) {
            ArrayList<Integer> checker = new ArrayList<>();
            for(int j : abils[i])
                checker.add(j);

            if(StaticStore.ability.contains(checker))
                abilities[i].setChecked(true);
        }

        if(StaticStore.talents)
            chnp.setChecked(true);
    }

    protected BitmapDrawable getResizeDraw(int id,float dp) {
        BitmapDrawable bd = new BitmapDrawable(getResources(),StaticStore.getResizeb((Bitmap)StaticStore.img15[id].bimg(),this,dp));
        bd.setFilterBitmap(true);
        bd.setAntiAlias(true);
        return bd;
    }

    @Override
    public void onBackPressed() {
        back.performClick();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences("configuration",Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase,shared.getInt("Language",0)));
    }
}
