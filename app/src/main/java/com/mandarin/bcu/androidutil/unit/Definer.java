package com.mandarin.bcu.androidutil.unit;

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
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder;
import com.mandarin.bcu.androidutil.io.DefineItf;
import com.mandarin.bcu.decode.ZipLib;
import com.mandarin.bcu.util.Interpret;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;

import common.CommonStatic;
import common.battle.BasisSet;
import common.battle.data.PCoin;
import common.system.MultiLangCont;
import common.system.P;
import common.system.fake.ImageBuilder;
import common.system.files.AssetData;
import common.util.pack.Pack;
import common.util.unit.Combo;
import common.util.unit.Unit;

public class Definer {
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
    private String [] lan = {"/en/","/zh/","/kr/","/jp/"};
    private String [] files = {"UnitName.txt","UnitExplanation.txt","CatFruitExplanation.txt","ComboName.txt"};


    public void define(Context context) {
        try {
            if(StaticStore.units==null) {
                try {
                    StaticStore.getUnitnumber();
                    ImageBuilder.builder = new BMBuilder();
                    new DefineItf().init();
                    Unit.readData();
                    PCoin.read();
                    Combo.readFile();
                } catch(Exception e) {
                    StaticStore.clear();

                    SharedPreferences shared2 = context.getSharedPreferences("configuration",Context.MODE_PRIVATE);

                    StaticStore.getLang(shared2.getInt("Language",0));
                    ZipLib.init();
                    ZipLib.read();
                    ImageBuilder.builder = new BMBuilder();
                    StaticStore.getUnitnumber();
                    new DefineItf().init();
                    Unit.readData();
                    PCoin.read();
                    Combo.readFile();
                    StaticStore.root = 1;
                }

                StaticStore.units = Pack.def.us.ulist.getList();

                if(StaticStore.img15 == null) {
                    StaticStore.readImg();
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

            if(StaticStore.unitlang == 1) {
                MultiLangCont.FNAME.clear();
                MultiLangCont.FEXP.clear();
                MultiLangCont.CFEXP.clear();
                MultiLangCont.COMNAME.clear();

                for(String l : lan) {
                    for(String n : files) {
                        String Path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/lang"+l+n;

                        File f = new File(Path);

                        if(f.exists()) {
                            Queue<String> qs = AssetData.getAsset(f).readLine();

                            switch (n) {
                                case "UnitName.txt":
                                    int size = qs.size();
                                    for (int j = 0; j < size; j++) {
                                        String[] strs = Objects.requireNonNull(qs.poll()).trim().split("\t");
                                        Unit u = Pack.def.us.ulist.get(CommonStatic.parseIntN(strs[0]));
                                        if (u == null)
                                            continue;

                                        for (int i = 0; i < Math.min(u.forms.length, strs.length - 1); i++)
                                            MultiLangCont.FNAME.put(l.substring(1, l.length() - 1), u.forms[i], strs[i + 1].trim());
                                    }
                                    break;
                                case "UnitExplanation.txt":
                                    size = qs.size();
                                    for (int j = 0; j < size; j++) {
                                        String[] strs = Objects.requireNonNull(qs.poll()).trim().split("\t");
                                        Unit u = Pack.def.us.ulist.get(CommonStatic.parseIntN(strs[0]));
                                        if (u == null)
                                            continue;

                                        for (int i = 0; i < Math.min(u.forms.length, strs.length - 1); i++) {
                                            String[] lines = strs[i + 1].trim().split("<br>");
                                            MultiLangCont.FEXP.put(l.substring(1, l.length() - 1), u.forms[i], lines);
                                        }
                                    }
                                    break;
                                case "CatFruitExplanation.txt":
                                    for (String str : qs) {
                                        String[] strs = str.trim().split("\t");
                                        Unit u = Pack.def.us.ulist.get(CommonStatic.parseIntN(strs[0]));
                                        if (u == null)
                                            continue;

                                        if(strs.length == 1) {
                                            continue;
                                        }

                                        String[] lines = strs[1].split("<br>");
                                        MultiLangCont.CFEXP.put(l.substring(1, l.length() - 1), u.info, lines);
                                    }
                                    break;
                                case "ComboName.txt":
                                    for(String str : qs) {
                                        String [] strs = str.trim().split("\t");

                                        if(strs.length <= 1) {
                                            continue;
                                        }

                                        int id = Integer.parseInt(strs[0].trim());

                                        String name = strs[1].trim();

                                        MultiLangCont.COMNAME.put(l.substring(1,l.length()-1),id,name);
                                    }
                            }
                        }
                    }
                }

                StaticStore.unitlang = 0;
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
                int[] number = StaticStore.anumber;
                StaticStore.icons = new Bitmap[number.length];
                for (int i = 0; i < number.length; i++)
                    StaticStore.icons[i] = (Bitmap)StaticStore.img15[number[i]].bimg();

                String iconpath = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU/files/org/page/icons/";
                String[] files = StaticStore.afiles;

                for(int i = 0;i<files.length;i++) {
                    if (files[i].equals(""))
                        continue;

                    StaticStore.icons[i] = BitmapFactory.decodeFile(iconpath + files[i]);
                }
            }

            if(StaticStore.picons == null) {
                int[] number = StaticStore.pnumber;

                StaticStore.picons = new Bitmap[number.length];

                for (int i = 0; i < number.length; i++)
                    StaticStore.picons[i] = (Bitmap)StaticStore.img15[number[i]].bimg();

                String iconpath = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU/files/org/page/icons/";
                String[] files = StaticStore.pfiles;

                for(int i = 0;i<files.length;i++) {
                    if(files[i].equals(""))
                        continue;

                    StaticStore.picons[i] = BitmapFactory.decodeFile(iconpath+files[i]);
                }
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

    public void redefine(Context context,String lang) {
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

    protected String number(int num) {
        if (0 <= num && num < 10) {
            return "00" + num;
        } else if (10 <= num && num <= 99) {
            return "0" + num;
        } else {
            return String.valueOf(num);
        }
    }
}