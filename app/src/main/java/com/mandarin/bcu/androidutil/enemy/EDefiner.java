package com.mandarin.bcu.androidutil.enemy;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder;
import com.mandarin.bcu.androidutil.io.DefineItf;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.decode.ZipLib;
import com.mandarin.bcu.util.Interpret;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Queue;

import common.CommonStatic;
import common.battle.BasisSet;
import common.system.MultiLangCont;
import common.system.fake.ImageBuilder;
import common.system.files.AssetData;
import common.util.pack.Pack;
import common.util.unit.Combo;
import common.util.unit.Enemy;

public class EDefiner {

    private String[] lan = {"/en/","/zh/","/kr/","/jp/"};
    private String[] files = {"EnemyName.txt","EnemyExplanation.txt"};

    private int [] colorid = StaticStore.colorid;
    private int [] starid = StaticStore.starid;
    private String [] starstring = new String[5];
    private String [] colorstring = new String[colorid.length];
    private int [] procid = StaticStore.procid;
    private String [] proc = new String[procid.length];
    private int [] abiid = StaticStore.abiid;
    private String [] abi = new String[abiid.length];
    private int [] textid = StaticStore.textid;
    private String [] textstring = new String[textid.length];

    public void define(Context context) {
        try {
            if(StaticStore.enemies == null) {
                try {
                    StaticStore.getEnemynumber();
                    Enemy.readData();
                } catch (NullPointerException e) {
                    StaticStore.clear();

                    SharedPreferences shared = context.getSharedPreferences("configuration",Context.MODE_PRIVATE);

                    StaticStore.getLang(shared.getInt("Language",0));
                    ZipLib.init();
                    ZipLib.read();
                    ImageBuilder.builder = new BMBuilder();
                    StaticStore.getEnemynumber();
                    new DefineItf().init();
                    Enemy.readData();
                    StaticStore.root = 1;
                }

                StaticStore.enemies = Pack.def.es.getList();

                if (StaticStore.img15 == null) {
                    StaticStore.readImg();
                }

                if (StaticStore.t == null) {
                    Combo.readFile();
                    StaticStore.t = BasisSet.current.t();
                }

                if (StaticStore.icons == null) {
                    int[] number = StaticStore.anumber;
                    StaticStore.icons = new Bitmap[number.length];
                    for (int i = 0; i < number.length; i++)
                        StaticStore.icons[i] = (Bitmap) StaticStore.img15[number[i]].bimg();

                    String iconpath = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU/files/org/page/icons/";
                    String[] files = StaticStore.afiles;

                    for(int i = 0;i<files.length;i++) {
                        if(files[i].equals(""))
                            continue;

                        StaticStore.icons[i] = BitmapFactory.decodeFile(iconpath+files[i]);
                    }
                }

                if (StaticStore.picons == null) {
                    int[] number = StaticStore.pnumber;

                    StaticStore.picons = new Bitmap[number.length];

                    for (int i = 0; i < number.length; i++)
                        StaticStore.picons[i] = (Bitmap) StaticStore.img15[number[i]].bimg();

                    String iconpath = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU/files/org/page/icons/";
                    String[] files = StaticStore.pfiles;

                    for(int i = 0;i<files.length;i++) {
                        if(files[i].equals(""))
                            continue;

                        StaticStore.picons[i] = BitmapFactory.decodeFile(iconpath+files[i]);
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

            if(StaticStore.enemeylang == 1) {
                MultiLangCont.ENAME.clear();
                MultiLangCont.EEXP.clear();

                for (String l : lan) {
                    for (String n : files) {
                        String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/lang" + l + n;

                        File f = new File(path);

                        if (f.exists()) {
                            Queue<String> qs = AssetData.getAsset(f).readLine();

                            switch (n) {
                                case "EnemyName.txt":
                                    for (String str : qs) {
                                        String[] strs = str.trim().split("\t");
                                        Enemy em = Pack.def.es.get(CommonStatic.parseIntN(strs[0]));
                                        if (em == null)
                                            continue;

                                        if (strs.length == 1)
                                            MultiLangCont.ENAME.put(l.substring(1, l.length() - 1), em, null);
                                        else
                                            MultiLangCont.ENAME.put(l.substring(1, l.length() - 1), em, strs[1].trim().startsWith("【")?strs[1].trim().substring(1,strs[1].trim().length()-1):strs[1].trim());
                                    }

                                    break;
                                case "EnemyExplanation.txt":
                                    for (String str : qs) {
                                        String[] strs = str.trim().split("\t");
                                        Enemy em = Pack.def.es.get(CommonStatic.parseIntN(strs[0]));

                                        if (em == null)
                                            continue;

                                        if (strs.length == 1)
                                            MultiLangCont.EEXP.put(l.substring(1, l.length() - 1), em, null);
                                        else {
                                            String[] lines = strs[1].trim().split("<br>");
                                            MultiLangCont.EEXP.put(l.substring(1, l.length() - 1), em, lines);
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                }

                StaticStore.enemeylang = 0;
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
        SharedPreferences shared = context.getSharedPreferences("configuration",Context.MODE_PRIVATE);

        StaticStore.getLang(shared.getInt("Language",0));

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
