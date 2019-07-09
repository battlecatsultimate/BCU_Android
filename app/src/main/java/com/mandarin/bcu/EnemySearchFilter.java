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
import static common.util.Data.P_REVIVE;
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
    private CheckBox[] traits = new CheckBox[12];
    private CheckBox[] attacks = new CheckBox[3];
    private CheckBox[] abilities = new CheckBox[18];
    private ScrollView sc;
    private NestedScrollView nsc;
    private int[] trid = {R.id.eschchrd,R.id.eschchfl,R.id.eschchbla,R.id.eschchme,R.id.eschchan,R.id.eschchal,R.id.eschchzo,R.id.eschchre,R.id.eschchwh,R.id.eschwit,R.id.escheva,R.id.eschnone};
    private String [] colors = {"1","2","3","4","5","6","7","8","0","10","9",""};
    private int[] atkid = {R.id.eschchld,R.id.eschchom,R.id.eschchmu};
    private String [] atks = {"2","4","3"};
    private int [] abid = {R.id.eschchabwe,R.id.eschchabfr,R.id.eschchabsl,R.id.eschchabkb,R.id.eschchabwp,R.id.eschchabstr,R.id.eschchabsu,R.id.eschchabcd,R.id.eschchabcr,R.id.eschchabwv,R.id.eschchabimwe,
            R.id.eschchabimfr,R.id.eschchabimsl,R.id.eschchabimkb,R.id.eschchabimwv,R.id.eschchabcu,R.id.eschchabbu,R.id.eschchabrev};
    private int [] abtool = {R.string.sch_abi_we,R.string.sch_abi_fr,R.string.sch_abi_sl,R.string.sch_abi_kb,R.string.sch_abi_wa,R.string.sch_abi_str,R.string.sch_abi_su,R.string.sch_abi_bd,R.string.sch_abi_cr,
            R.string.sch_abi_wv,R.string.sch_abi_iw,R.string.sch_abi_if,R.string.sch_abi_is,R.string.sch_abi_ik,R.string.sch_abi_iwv,R.string.abi_cu,R.string.abi_bu,R.string.abi_rev};
    private int [] trtool = {R.string.sch_red,R.string.sch_fl,R.string.sch_bla,R.string.sch_me,R.string.sch_an,R.string.sch_al,R.string.sch_zo,R.string.sch_re,R.string.sch_wh};
    private  int [][] abils = {{1,P_WEAK},{1,P_STOP},{1,P_SLOW},{1,P_KB},{1,P_WARP},{1,P_STRONG},{1,P_LETHAL},{0,AB_BASE},{1,P_CRIT},{1,P_WAVE},{1,P_IMUWEAK},{1,P_IMUSTOP},{1,P_IMUSLOW},{1,P_IMUKB},{1,P_IMUWAVE},{1,P_CURSE},{1,P_BURROW},{1,P_REVIVE}};

    private ArrayList<String> tr = new ArrayList<>();
    private ArrayList<String> attack = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> ability = new ArrayList<>();
    private boolean trorand = true;
    private boolean atksimu = true;
    private boolean atkorand = true;
    private boolean aborand = true;

    private int [] atkdraw = {212,112};
    private int [] trdraw = {219,220,221,222,223,224,225,226,227,-1,-1,-1};
    private int [] abdraw = {195,197,198,207,266,196,199,200,201,208,213,214,215,216,210,-1,-1,-1};
    private String [] abfiles = {"","","","","","","","","","","","","","","","Curse.png","Burrow.png","Revive.png"};

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
                if(abdraw[i] != -1)
                    abilities[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(abdraw[i],40f),null);
                else {
                    Bitmap b  = StaticStore.getResizeb(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU/files/org/page/icons/"+abfiles[i]),this,40f);

                    abilities[i].setCompoundDrawablesWithIntrinsicBounds(null,null,new BitmapDrawable(getResources(),b),null);
                }
            else
                if(abdraw[i] != -1)
                    abilities[i].setCompoundDrawablesWithIntrinsicBounds(null,null,getResizeDraw(abdraw[i],32f),null);
                else {
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
                tr = new ArrayList<>();
                ability = new ArrayList<>();
                attack = new ArrayList<>();
                trorand = true;
                atksimu = true;
                aborand = true;
                atkorand = true;

                atkgroup.clearCheck();
                tgor.setChecked(true);
                atkor.setChecked(true);
                abor.setChecked(true);

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

        tggroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                trorand = checkedId == tgor.getId();
            }
        });

        atkgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                atksimu = checkedId == atkmu.getId();
            }
        });

        atkgroupor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                atkorand = checkedId == atkor.getId();
            }
        });

        abgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                aborand = checkedId == abor.getId();
            }
        });

        for(int i = 0; i < traits.length;i++) {
            final int finall = i;
            traits[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                        tr.add(colors[finall]);
                    else
                        tr.remove(colors[finall]);
                }
            });

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
                        attack.add(atks[finall]);
                    else
                        attack.remove(atks[finall]);
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
                        ability.add(abilval);
                    else
                        ability.remove(abilval);
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
        }
    }

    protected void returner() {
        Intent result = new Intent();
        if(atkgroup.getCheckedRadioButtonId() == -1)
            result.putExtra("empty",true);
        else
            result.putExtra("empty",false);

        result.putExtra("trorand",trorand);
        result.putExtra("atksimu",atksimu);
        result.putExtra("atkorand",atkorand);
        result.putExtra("aborand",aborand);
        result.putExtra("trait",tr);
        result.putExtra("attack",attack);
        result.putExtra("ability",ability);
        setResult(RESULT_OK,result);
        finish();
    }

    protected void Checker() {
        Intent data = getIntent();
        Bundle extra = data.getExtras();

        if(extra != null) {
            boolean empty = extra.getBoolean("empty");

            if(!empty)
                atkgroup.check(R.id.schrdatkmu);

            atksimu = extra.getBoolean("atksimu");

            if(!atksimu)
                if(!empty)
                    atkgroup.check(R.id.eschrdatksi);

            atkorand = extra.getBoolean("atkorand");

            if(!atkorand)
                atkgroupor.check(R.id.eschrdatkand);

            trorand = extra.getBoolean("trorand");

            if(!trorand)
                tggroup.check(R.id.eschrdtgand);

            aborand = extra.getBoolean("aborand");

            if(!aborand)
                abgroup.check(R.id.eschrdaband);

            attack = extra.getStringArrayList("attack");

            for(int i = 0;i<atks.length;i++) {
                if(attack != null && attack.contains(atks[i]))
                    attacks[i].setChecked(true);
            }

            tr = extra.getStringArrayList("trait");

            for(int i = 0;i<colors.length;i++) {
                if(tr != null && tr.contains(colors[i]))
                    traits[i].setChecked(true);
            }

            ability = (ArrayList<ArrayList<Integer>>) extra.getSerializable("ability");

            for(int i = 0; i< abils.length;i++) {
                ArrayList<Integer> checker = new ArrayList<>();
                for(int k : abils[i])
                    checker.add(k);

                if(ability != null && ability.contains(checker))
                    abilities[i].setChecked(true);
            }
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
