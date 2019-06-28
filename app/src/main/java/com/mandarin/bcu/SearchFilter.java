package com.mandarin.bcu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
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
import static common.util.Data.P_IMUCURSE;
import static common.util.Data.P_IMUKB;
import static common.util.Data.P_IMUSLOW;
import static common.util.Data.P_IMUSTOP;
import static common.util.Data.P_IMUWARP;
import static common.util.Data.P_IMUWAVE;
import static common.util.Data.P_IMUWEAK;
import static common.util.Data.P_KB;
import static common.util.Data.P_LETHAL;
import static common.util.Data.P_SLOW;
import static common.util.Data.P_STOP;
import static common.util.Data.P_STRONG;
import static common.util.Data.P_WARP;
import static common.util.Data.P_WAVE;
import static common.util.Data.P_WEAK;

public class SearchFilter extends AppCompatActivity {

    private ImageButton back;
    private ImageButton reset;
    private RadioButton tgor;
    private RadioButton atkmu;
    private RadioButton atkor;
    private RadioButton abor;
    private RadioGroup tggroup;
    private RadioGroup atkgroup;
    private RadioGroup atkgroupor;
    private RadioGroup abgroup;
    private CheckBox[] rarities = new CheckBox[6];
    private CheckBox[] targets = new CheckBox[9];
    private CheckBox[] attacks = new CheckBox[3];
    private CheckBox[] abilities = new CheckBox[30];
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
            R.id.schchabwik,R.id.schchabevk};
    private int[] abtool = {R.string.sch_abi_we,R.string.sch_abi_fr,R.string.sch_abi_sl,R.string.sch_abi_ao,R.string.sch_abi_st,R.string.sch_abi_re,R.string.sch_abi_it,R.string.sch_abi_md,R.string.sch_abi_id,R.string.sch_abi_kb,
            R.string.sch_abi_wa,R.string.sch_abi_str,R.string.sch_abi_su,R.string.sch_abi_bd,R.string.sch_abi_cr,R.string.sch_abi_zk,R.string.sch_abi_bb,R.string.sch_abi_em,R.string.sch_abi_me,R.string.sch_abi_wv,
            R.string.sch_abi_iw,R.string.sch_abi_if,R.string.sch_abi_is,R.string.sch_abi_ik,R.string.sch_abi_iwv,R.string.sch_abi_iwa,R.string.sch_abi_ic,R.string.sch_abi_ws,R.string.sch_abi_wk,R.string.sch_abi_eva};
    private int[] tgtool = {R.string.sch_red,R.string.sch_fl,R.string.sch_bla,R.string.sch_me,R.string.sch_an,R.string.sch_al,R.string.sch_zo,R.string.sch_re,R.string.sch_wh};
    private int [][] abils = {{1,P_WEAK},{1,P_STOP},{1,P_SLOW},{0,AB_ONLY},{0,AB_GOOD},{0,AB_RESIST},{0,AB_RESISTS},{0,AB_MASSIVE},{0,AB_MASSIVES},{1,P_KB},{1,P_WARP},{1,P_STRONG},{1,P_LETHAL},{0,AB_BASE},{1,P_CRIT},{0,AB_ZKILL},{1,P_BREAK},
            {0,AB_EARN},{0,AB_METALIC},{1,P_WAVE},{1,P_IMUWEAK},{1,P_IMUSTOP},{1,P_IMUSLOW},{1,P_IMUKB},{1,P_IMUWAVE},{1,P_IMUWARP},{1,P_IMUCURSE},{0,AB_WAVES},{0,AB_WKILL},{0,AB_EKILL}};
    private ArrayList<String> tg = new ArrayList<>();
    private ArrayList<String> rare = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> ability = new ArrayList<>();
    private ArrayList<String> attack = new ArrayList<>();
    private boolean tgorand = true;
    private boolean atksimu = true;
    private boolean aborand = true;
    private boolean atkorand = true;
    private boolean talents = false;

    private int [] atkdraw = {212,112};
    private int [] tgdraw = {219,220,221,222,223,224,225,226,227};
    private int [] abdraw = {195,197,198,202,203,204,122,206,114,207,266,196,199,200,201,260,264,205,209,208,213,214,215,216,210,262,116,218,258,110};

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

        setContentView(R.layout.activity_search_filter);

        back = findViewById(R.id.schbck);
        reset = findViewById(R.id.schreset);
        tgor = findViewById(R.id.schrdtgor);
        atkmu = findViewById(R.id.schrdatkmu);
        atkmu.setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(211,40f),null);
        atkmu.setCompoundDrawablePadding(StaticStore.dptopx(16f,this));
        RadioButton atksi = findViewById(R.id.schrdatksi);
        atksi.setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(217,40f),null);
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
            targets[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(tgdraw[i],40f),null);
            targets[i].setCompoundDrawablePadding(StaticStore.dptopx(16f,this));
        }
        for(int i=0;i<rareid.length;i++)
            rarities[i] = findViewById(rareid[i]);
        for(int i=0;i<atkid.length;i++) {
            attacks[i] = findViewById(atkid[i]);

            if(i <atkid.length-1) {
                attacks[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(atkdraw[i],40f),null);
                attacks[i].setCompoundDrawablePadding(StaticStore.dptopx(16f,this));
            }
        }
        for(int i=0;i<abid.length;i++) {
            abilities[i] = findViewById(abid[i]);
            abilities[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(abdraw[i],40f),null);
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
                tg = new ArrayList<>();
                rare = new ArrayList<>();
                ability = new ArrayList<>();
                attack = new ArrayList<>();
                tgorand = true;
                atksimu = true;
                aborand = true;
                atkorand = true;
                talents = false;

                atkgroup.clearCheck();
                tgor.setChecked(true);
                abor.setChecked(true);
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
                tgorand = checkedId == tgor.getId();
            }
        });

        atkgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                atksimu = checkedId == atkmu.getId();
            }
        });

        abgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                aborand = checkedId == abor.getId();
            }
        });

        for(int i=0;i<targets.length;i++) {
            final int finalI = i;
            targets[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                        tg.add(colors[finalI]);
                    else
                        tg.remove(colors[finalI]);
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
                        rare.add(rarity[finall]);
                    else
                        rare.remove(rarity[finall]);
                }
            });
        }

        for(int i=0;i<attacks.length;i++) {
            final int finall = i;
            attacks[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                        attack.add(atks[finall]);
                    else
                        attack.remove(atks[finall]);
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
                        ability.add(abilval);
                    }
                    else
                        ability.remove(abilval);
                }
            });

            atkgroupor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    atkorand = checkedId == atkor.getId();
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
                if(isChecked)
                    talents = true;
                else
                    talents = false;
            }
        });
    }

    protected void returner() {
        Intent result = new Intent();
        if(atkgroup.getCheckedRadioButtonId() == -1) {
            result.putExtra("empty",true);
        } else {
            result.putExtra("empty",false);
        }
        result.putExtra("tgorand",tgorand);
        result.putExtra("atksimu",atksimu);
        result.putExtra("aborand",aborand);
        result.putExtra("atkorand",atkorand);
        result.putExtra("target",tg);
        result.putExtra("attack",attack);
        result.putExtra("rare",rare);
        result.putExtra("ability",ability);
        result.putExtra("talents",talents);
        setResult(RESULT_OK,result);
        finish();
    }

    @SuppressWarnings("unchecked")
    protected void Checker() {
        Intent data = getIntent();
        Bundle extra = data.getExtras();

        if(extra != null) {

            boolean empty = extra.getBoolean("empty");

            if (!empty)
                atkgroup.check(R.id.schrdatkmu);

            atksimu = extra.getBoolean("atksimu");

            if (!atksimu)
                if (!empty)
                    atkgroup.check(R.id.schrdatksi);

            atkorand = extra.getBoolean("atkorand");

            if (!atkorand)
                atkgroupor.check(R.id.schrdatkand);

            tgorand = extra.getBoolean("tgorand");

            if (!tgorand)
                tggroup.check(R.id.schrdtgand);

            aborand = extra.getBoolean("aborand");

            if (!aborand)
                abgroup.check(R.id.schrdaband);

            rare = extra.getStringArrayList("rare");

            for(int i=0;i<rarity.length;i++)
                if (rare != null && rare.contains(rarity[i]))
                    rarities[i].setChecked(true);

            attack = extra.getStringArrayList("attack");

            for(int i=0;i<atks.length;i++)
                if (attack != null && attack.contains(atks[i]))
                    attacks[i].setChecked(true);

            tg = extra.getStringArrayList("target");

            for(int i=0;i<colors.length;i++)
                if (tg != null && tg.contains(colors[i]))
                    targets[i].setChecked(true);

            ability = (ArrayList<ArrayList<Integer>>) extra.getSerializable("ability");

            for(int i =0;i<abils.length;i++) {
                ArrayList<Integer> checker = new ArrayList<>();
                for(int j : abils[i])
                    checker.add(j);

                if(ability.contains(checker))
                    abilities[i].setChecked(true);
            }

            talents = extra.getBoolean("talents");

            if(talents)
                chnp.setChecked(true);
        }
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
