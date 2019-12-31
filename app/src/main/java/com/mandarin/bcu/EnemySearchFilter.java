package com.mandarin.bcu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
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
import static common.util.Data.P_BURROW;
import static common.util.Data.P_CRIT;
import static common.util.Data.P_CURSE;
import static common.util.Data.P_IMUKB;
import static common.util.Data.P_IMUSLOW;
import static common.util.Data.P_IMUSTOP;
import static common.util.Data.P_IMUWAVE;
import static common.util.Data.P_IMUWEAK;
import static common.util.Data.P_KB;
import static common.util.Data.P_LETHAL;
import static common.util.Data.P_POIATK;
import static common.util.Data.P_REVIVE;
import static common.util.Data.P_SATK;
import static common.util.Data.P_SLOW;
import static common.util.Data.P_STOP;
import static common.util.Data.P_STRONG;
import static common.util.Data.P_WARP;
import static common.util.Data.P_WAVE;
import static common.util.Data.P_WEAK;

public class EnemySearchFilter extends AppCompatActivity {
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
    private CheckBox star;
    private CheckBox[] traits = new CheckBox[12];
    private CheckBox[] attacks = new CheckBox[3];
    private CheckBox[] abilities = new CheckBox[20];
    private ScrollView sc;
    private NestedScrollView nsc;
    private int[] trid = {R.id.eschchrd,R.id.eschchfl,R.id.eschchbla,R.id.eschchme,R.id.eschchan,R.id.eschchal,R.id.eschchzo,R.id.eschchre,R.id.eschchwh,R.id.eschwit,R.id.escheva,R.id.eschnone};
    private String [] colors = {"1","2","3","4","5","6","7","8","0","10","9",""};
    private int[] atkid = {R.id.eschchld,R.id.eschchom,R.id.eschchmu};
    private String [] atks = {"2","4","3"};
    private int [] abid = {R.id.eschchabwe,R.id.eschchabfr,R.id.eschchabsl,R.id.eschchabkb,R.id.eschchabwp,R.id.eschchabstr,R.id.eschchabsu,R.id.eschchabcd,R.id.eschchabcr,R.id.eschchabwv,R.id.eschchabimwe,
            R.id.eschchabimfr,R.id.eschchabimsl,R.id.eschchabimkb,R.id.eschchabimwv,R.id.eschchabcu,R.id.eschchabbu,R.id.eschchabrev,R.id.eschchabsb,R.id.eschchabpo};
    private int [] abtool = {R.string.sch_abi_we,R.string.sch_abi_fr,R.string.sch_abi_sl,R.string.sch_abi_kb,R.string.sch_abi_wa,R.string.sch_abi_str,R.string.sch_abi_su,R.string.sch_abi_bd,R.string.sch_abi_cr,
            R.string.sch_abi_wv,R.string.sch_abi_iw,R.string.sch_abi_if,R.string.sch_abi_is,R.string.sch_abi_ik,R.string.sch_abi_iwv,R.string.abi_cu,R.string.abi_bu,R.string.abi_rev,R.string.sch_abi_sb,R.string.sch_abi_poi};
    private int [] trtool = {R.string.sch_red,R.string.sch_fl,R.string.sch_bla,R.string.sch_me,R.string.sch_an,R.string.sch_al,R.string.sch_zo,R.string.sch_re,R.string.sch_wh};
    private  int [][] abils = {{1,P_WEAK},{1,P_STOP},{1,P_SLOW},{1,P_KB},{1,P_WARP},{1,P_STRONG},{1,P_LETHAL},{0,AB_BASE},{1,P_CRIT},{1,P_WAVE},{1,P_IMUWEAK},{1,P_IMUSTOP},{1,P_IMUSLOW},{1,P_IMUKB},{1,P_IMUWAVE},{1,P_CURSE},{1,P_BURROW},{1,P_REVIVE},{1,P_SATK},{1,P_POIATK}};

    private int [] atkdraw = {212,112};
    private int [] trdraw = {219,220,221,222,223,224,225,226,227,-1,-1,-1};
    private int [] abdraw = {195,197,198,207,266,196,199,200,201,208,213,214,215,216,210,-1,-1,-1,229,-1};
    private String [] abfiles = {"","","","","","","","","","","","","","","","Curse.png","Burrow.png","Revive.png","","BCPoison.png"};

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

        setContentView(R.layout.activity_enemy_search_filter);

        if(StaticStore.img15 == null) {
            StaticStore.readImg();
        }


        back = findViewById(R.id.eschbck);
        reset = findViewById(R.id.schreset);
        tgor = findViewById(R.id.eschrdtgor);
        atkmu = findViewById(R.id.eschrdatkmu);
        star = findViewById(R.id.eschstar);
        atkmu.setCompoundDrawablePadding(StaticStore.dptopx(16f,this));
        RadioButton atksi = findViewById(R.id.eschrdatksi);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            atkmu.setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(211,40f),null);
            atksi.setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(217,40f),null);
        } else {
            atkmu.setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(211,32f),null);
            atksi.setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(217,32f),null);
        }

        atksi.setCompoundDrawablePadding(StaticStore.dptopx(16f,this));
        atkor = findViewById(R.id.eschrdatkor);
        abor = findViewById(R.id.eschrdabor);
        tggroup = findViewById(R.id.eschrgtg);
        atkgroup = findViewById(R.id.eschrgatk);
        atkgroupor = findViewById(R.id.eschrgatkor);
        abgroup = findViewById(R.id.eschrgab);
        sc = findViewById(R.id.animsc);
        nsc = findViewById(R.id.animnscview);
        for(int i = 0;i< trid.length;i++) {
            traits[i] = findViewById(trid[i]);
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && trdraw[i] != -1)
                traits[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(trdraw[i],40f),null);
            else if(trdraw[i] != -1)
                traits[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(trdraw[i],32f),null);

            if(trdraw[i] != -1)
                traits[i].setCompoundDrawablePadding(StaticStore.dptopx(16f,this));
        }

        for(int i = 0;i<atkid.length;i++) {
            attacks[i] = findViewById(atkid[i]);

            if(i < atkid.length -1) {
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    attacks[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(atkdraw[i],40f),null);
                else
                    attacks[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(atkdraw[i],32f),null);

                attacks[i].setCompoundDrawablePadding(StaticStore.dptopx(8f,this));
            }
        }

        for(int i = 0; i <abid.length;i++) {
            abilities[i] = findViewById(abid[i]);

            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                if(abdraw[i] != -1) {
                    if (abdraw[i] == -100) {
                        abilities[i].setText(abtool[i]);
                        continue;
                    }

                    abilities[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(abdraw[i],40f),null);
                 } else {
                    Bitmap b  = StaticStore.getResizeb(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU/files/org/page/icons/"+abfiles[i]),this,40f);

                    abilities[i].setCompoundDrawablesWithIntrinsicBounds(null,null,new BitmapDrawable(getResources(),b),null);
                }
            else
                if(abdraw[i] != -1) {
                    if (abdraw[i] == -100) {
                        abilities[i].setText(abtool[i]);
                        continue;
                    }

                    abilities[i].setCompoundDrawablesWithIntrinsicBounds(null, null, getResizeDraw(abdraw[i], 32f), null);
                } else {
                    Bitmap b  = StaticStore.getResizeb(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU/files/org/page/icons/"+abfiles[i]),this,32f);

                    abilities[i].setCompoundDrawablesWithIntrinsicBounds(null,null,new BitmapDrawable(getResources(),b),null);
                }

            abilities[i].setCompoundDrawablePadding(StaticStore.dptopx(16f,this));
        }

        tgor.setChecked(true);
        atkor.setChecked(true);
        abor.setChecked(true);

        Checker();

        Listeners();
    }

    protected BitmapDrawable getResizeDraw(int id, float dp) {
        BitmapDrawable bd = new BitmapDrawable(getResources(),StaticStore.getResizeb((Bitmap)StaticStore.img15[id].bimg(),this,dp));
        bd.setFilterBitmap(true);
        bd.setAntiAlias(true);
        return bd;
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
                StaticStore.tg = new ArrayList<>();
                StaticStore.ability = new ArrayList<>();
                StaticStore.attack = new ArrayList<>();
                StaticStore.tgorand = true;
                StaticStore.atksimu = true;
                StaticStore.aborand = true;
                StaticStore.atkorand = true;
                StaticStore.starred  = false;

                atkgroup.clearCheck();
                tgor.setChecked(true);
                atkor.setChecked(true);
                abor.setChecked(true);
                star.setChecked(false);

                for(CheckBox attack1 : attacks) {
                    if(attack1.isChecked())
                        attack1.setChecked(false);
                }

                for(CheckBox trait : traits) {
                    if(trait.isChecked())
                        trait.setChecked(false);
                }

                for(CheckBox ability : abilities)
                    if(ability.isChecked())
                        ability.setChecked(false);

            }
        });

        star.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StaticStore.starred = isChecked;
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

        for(int i = 0; i < traits.length;i++) {
            final int finall = i;
            traits[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                        StaticStore.tg.add(colors[finall]);
                    else
                        StaticStore.tg.remove(colors[finall]);
                }
            });

            if(i < 9)
                traits[i].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(v.getContext(), trtool[finall], Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
        }

        for(int i =0;i<attacks.length;i++) {
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

        for(int i = 0;i < abilities.length;i++) {
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

                    if(isChecked)
                        StaticStore.ability.add(abilval);
                    else
                        StaticStore.ability.remove(abilval);
                }
            });

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

            star.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    StaticStore.starred = isChecked;
                }
            });
        }
    }

    protected void returner() {
        Intent result = new Intent();

        StaticStore.empty = atkgroup.getCheckedRadioButtonId() == -1;

        setResult(RESULT_OK,result);
        finish();
    }

    protected void Checker() {

            star.setChecked(StaticStore.starred);

            if(!StaticStore.empty)
                atkgroup.check(R.id.schrdatkmu);

            if(!StaticStore.atksimu)
                if(!StaticStore.empty)
                    atkgroup.check(R.id.eschrdatksi);

            if(!StaticStore.atkorand)
                atkgroupor.check(R.id.eschrdatkand);

            if(!StaticStore.tgorand)
                tggroup.check(R.id.eschrdtgand);

            if(!StaticStore.aborand)
                abgroup.check(R.id.eschrdaband);

            for(int i = 0;i<atks.length;i++) {
                if(StaticStore.attack != null && StaticStore.attack.contains(atks[i]))
                    attacks[i].setChecked(true);
            }

            for(int i = 0;i<colors.length;i++) {
                if(StaticStore.tg != null && StaticStore.tg.contains(colors[i]))
                    traits[i].setChecked(true);
            }

            for(int i = 0; i< abils.length;i++) {
                ArrayList<Integer> checker = new ArrayList<>();
                for(int k : abils[i])
                    checker.add(k);

                if(StaticStore.ability != null && StaticStore.ability.contains(checker))
                    abilities[i].setChecked(true);
            }
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
