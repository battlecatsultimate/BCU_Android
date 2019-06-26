package com.mandarin.bcu.androidutil;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.mandarin.bcu.R;
import com.mandarin.bcu.util.Interpret;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Queue;

import common.CommonStatic;
import common.battle.BasisSet;
import common.battle.data.PCoin;
import common.system.MultiLangCont;
import common.system.files.AssetData;
import common.util.pack.Pack;
import common.util.unit.Combo;
import common.util.unit.Unit;

public class Definer {
    private int [] colorid = {R.string.sch_wh,R.string.sch_red,R.string.sch_fl,R.string.sch_bla,R.string.sch_me,R.string.sch_an,R.string.sch_al,R.string.sch_zo,R.string.sch_re};
    private int [] starid = {R.string.unit_info_starred,R.string.unit_info_god1,R.string.unit_info_god2,R.string.unit_info_god3};
    private String [] starstring = new String[5];
    private String [] colorstring = new String[colorid.length];
    private int [] procid = {R.string.sch_abi_kb,R.string.sch_abi_fr,R.string.sch_abi_sl,R.string.sch_abi_cr,R.string.sch_abi_wv,R.string.sch_abi_we,R.string.sch_abi_bb,R.string.sch_abi_wa,R.string.sch_abi_cr,
            R.string.sch_abi_str,R.string.sch_abi_su,R.string.abi_bo,R.string.abi_rev,R.string.sch_abi_ik,R.string.sch_abi_if,R.string.sch_abi_is,R.string.sch_abi_iwv,R.string.sch_abi_iw,R.string.sch_abi_iwa,
            R.string.sch_abi_ic,R.string.abi_snk,R.string.abi_stt,R.string.abi_seal,R.string.abi_sum,R.string.abi_mvatk,R.string.abi_thch,R.string.abi_poi,R.string.abi_boswv
            ,R.string.talen_kb,R.string.talen_fr,R.string.talen_sl,R.string.talen_wv,R.string.talen_we,R.string.talen_warp,
            R.string.talen_cu};
    private String [] proc = new String[procid.length];
    private int [] abiid = {R.string.sch_abi_st,R.string.sch_abi_re,R.string.sch_abi_md,R.string.sch_abi_ao,R.string.sch_abi_em,R.string.sch_abi_bd,R.string.sch_abi_me,R.string.abi_imvatk,R.string.sch_abi_ws,
            R.string.abi_isnk,R.string.abi_istt,R.string.abi_gh,R.string.abi_ipoi,R.string.sch_abi_zk,R.string.sch_abi_wk,R.string.abi_sui,R.string.abi_ithch,R.string.sch_abi_eva,
            R.string.abi_iseal,R.string.abi_iboswv,R.string.sch_abi_it,R.string.sch_abi_id};
    private String [] abi = new String[abiid.length];
    private int [] textid = {R.string.unit_info_text0,R.string.unit_info_text1,R.string.unit_info_text2,R.string.unit_info_text3,R.string.unit_info_text4,R.string.unit_info_text5,R.string.unit_info_text6,R.string.unit_info_text7,
            R.string.def_unit_info_text8,R.string.unit_info_text9,R.string.unit_info_text10,R.string.def_unit_info_text11,R.string.def_unit_info_text12,R.string.unit_info_text13,
            R.string.unit_info_text14,R.string.unit_info_text15,R.string.unit_info_text16};
    private String [] textstring = new String[textid.length];
    private String [] lan = {"/en/","/zh/","/kr/","/jp/"};
    private String [] files = {"UnitName.txt","UnitExplanation.txt","CatFruitExplanation.txt"};


    public void define(Context context) {
        try {
            if(StaticStore.units==null) {
                Unit.readData();
                PCoin.read();

                StaticStore.units = Pack.def.us.ulist.getList();

                StaticStore.readImg();

                for(String l : lan) {
                    for(String n : files) {
                        String Path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/lang"+l+n;

                        File f = new File(Path);

                        if(f.exists()) {
                            Queue<String> qs = AssetData.getAsset(f).readLine();

                            if(n.equals("UnitName.txt")) {
                                for(String str : qs) {
                                    String[] strs = str.trim().split("\t");
                                    Unit u = Pack.def.us.ulist.get(CommonStatic.parseIntN(strs[0]));
                                    if(u == null)
                                        continue;

                                    for(int i = 0;i<Math.min(u.forms.length,strs.length-1);i++)
                                        MultiLangCont.FNAME.put(l.substring(1, l.length() - 1), u.forms[i], strs[i + 1].trim());
                                }
                            } else if (n.equals("UnitExplanation.txt")) {
                                for(String str : qs) {
                                    String[] strs = str.trim().split("\t");
                                    Unit u = Pack.def.us.ulist.get(CommonStatic.parseIntN(strs[0]));
                                    if (u == null)
                                        continue;

                                    for (int i = 0; i < Math.min(u.forms.length, strs.length - 1); i++) {
                                        String[] lines = strs[i + 1].trim().split("<br>");
                                        MultiLangCont.FEXP.put(l.substring(1,l.length()-1), u.forms[i], lines);
                                    }
                                }
                            } else if (n.equals("CatFruitExplanation.txt")) {
                                for(String str : qs) {
                                    String[] strs = str.trim().split("\t");
                                    Unit u = Pack.def.us.ulist.get(CommonStatic.parseIntN(strs[0]));
                                    if(u==null)
                                        continue;

                                    String[] lines = strs[1].split("<br>");
                                    MultiLangCont.CFEXP.put(l.substring(1,l.length()-1),u.info,lines);
                                }
                            }
                        }
                    }
                }

                for(int i = 0;i<colorid.length;i++) {
                    colorstring[i] = context.getString(colorid[i]);
                }

                starstring[0] = "";

                for(int i = 0;i<starid.length;i++)
                    starstring[i+1] = context.getString(starid[i]);

                for(int i =0;i<procid.length;i++)
                    proc[i] = context.getString(procid[i]);

                for(int i=0;i<abiid.length;i++)
                    abi[i] = context.getString(abiid[i]);

                for(int i=0;i<textid.length;i++)
                    textstring[i] = context.getString(textid[i]);

                Interpret.TRAIT = colorstring;
                Interpret.STAR = starstring;
                Interpret.PROC = proc;
                Interpret.ABIS = abi;
                Interpret.TEXT = textstring;
            }

            if(StaticStore.t == null) {
                Combo.readFile();
                StaticStore.t = BasisSet.current.t();
            }

            if(StaticStore.fruit == null) {
                String Path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/page/catfruit/";
                File f = new File(Path);
                StaticStore.fruit = new Bitmap[f.listFiles().length];

                String[] names = {"gatyaitemD_30_f.png","gatyaitemD_31_f.png","gatyaitemD_32_f.png","gatyaitemD_33_f.png","gatyaitemD_34_f.png","gatyaitemD_35_f.png","gatyaitemD_36_f.png"
                ,"gatyaitemD_37_f.png","gatyaitemD_38_f.png","gatyaitemD_39_f.png","gatyaitemD_40_f.png","gatyaitemD_41_f.png","gatyaitemD_42_f.png","xp.png"};

                for(int i = 0; i<names.length;i++) {
                    StaticStore.fruit[i] = BitmapFactory.decodeFile(Path+names[i]);
                }
            }

            if(StaticStore.icons == null) {
                int[] number = {203,204,206,202,205,200,209,227,218,227,227,227,227,260,258,227,227,110,227,227,122,114};
                StaticStore.icons = new Bitmap[number.length];
                for (int i = 0; i < number.length; i++)
                    StaticStore.icons[i] = (Bitmap)StaticStore.img15[number[i]].bimg();
            }

            if(StaticStore.picons == null) {
                int[] number = {207,197,198,201,208,195,264,266,227,196,199,227,227,216,214,215,210,213,262,116,227,227,227,227,227,227,227,227,49,45,47,51,43,53,109};

                StaticStore.picons = new Bitmap[number.length];

                for (int i = 0; i < number.length; i++)
                    StaticStore.picons[i] = (Bitmap)StaticStore.img15[number[i]].bimg();
            }

            if(StaticStore.addition == null) {
                int[] addid = {R.string.unit_info_strong, R.string.unit_info_resis, R.string.unit_info_masdam, R.string.unit_info_exmon, R.string.unit_info_atkbs, R.string.unit_info_wkill, R.string.unit_info_evakill, R.string.unit_info_insres, R.string.unit_info_insmas};
                StaticStore.addition = new String[addid.length];
                for (int i = 0; i < addid.length; i++)
                    StaticStore.addition[i] = context.getString(addid[i]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void redefine(Context context,String lang) {

        CommonStatic.Lang.lang = Arrays.asList(StaticStore.lang).indexOf(lang)-1;

        if(CommonStatic.Lang.lang >= 4 || CommonStatic.Lang.lang == -2)
            CommonStatic.Lang.lang = 0;

        System.out.println(CommonStatic.Lang.lang);

        for(int i = 0;i<colorid.length;i++) {
            colorstring[i] = getString(context,colorid[i],lang);
        }

        starstring[0] = "";

        for(int i = 0;i<starid.length;i++)
            starstring[i+1] = getString(context,starid[i],lang);

        for(int i =0;i<procid.length;i++)
            proc[i] = getString(context,procid[i],lang);

        for(int i=0;i<abiid.length;i++)
            abi[i] = getString(context,abiid[i],lang);

        for(int i=0;i<textid.length;i++)
            textstring[i] = getString(context,textid[i],lang);

        int[] addid = {R.string.unit_info_strong, R.string.unit_info_resis, R.string.unit_info_masdam, R.string.unit_info_exmon, R.string.unit_info_atkbs, R.string.unit_info_wkill, R.string.unit_info_evakill, R.string.unit_info_insres, R.string.unit_info_insmas};
        StaticStore.addition = new String[addid.length];
        for (int i = 0; i < addid.length; i++)
            StaticStore.addition[i] = getString(context,addid[i],lang);

        Interpret.TRAIT = colorstring;
        Interpret.STAR = starstring;
        Interpret.PROC = proc;
        Interpret.ABIS = abi;
        Interpret.TEXT = textstring;
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private String getString(Context context,int id,String lang) {

        Locale locale = new Locale(lang);


        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }
}