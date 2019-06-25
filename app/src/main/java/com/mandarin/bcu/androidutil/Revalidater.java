package com.mandarin.bcu.androidutil;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.Locale;
import java.util.Queue;

import common.system.MultiLangCont;
import common.system.files.AssetData;
import common.system.files.VFile;
import common.util.pack.Pack;

public class Revalidater extends ContextWrapper {
    private static String [] locales = StaticStore.lang;
    private final int unitnumber;

    public Revalidater(Context context) {
        super(context);
        this.unitnumber = StaticStore.unitnumber;
    }

    public static ContextWrapper LangChange(Context context,int position) {
        String lang = locales[position];

        if(lang.equals("")) {
            lang = Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
        }

        Configuration config = context.getResources().getConfiguration();

        if(!lang.equals("")) {
            Locale locale = new Locale(lang);

            Locale.setDefault(locale);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setSystemLocale(config,locale);
            } else {
                setSystemLocaleLegacy(config,locale);
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context = context.createConfigurationContext(config);
        } else {
            context.getResources().updateConfiguration(config,context.getResources().getDisplayMetrics());
        }

        return new Revalidater(context);
    }

    @SuppressWarnings("deprecation")
    public static void setSystemLocaleLegacy(Configuration config, Locale locale){
        config.locale = locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static void setSystemLocale(Configuration config, Locale locale){
        config.setLocale(locale);
    }

    public void Validate(String lang, Context context) {

        new Definer().redefine(context,lang);

        if(StaticStore.names != null) {
            StaticStore.names = new String[unitnumber];

            for(int i =0;i<unitnumber;i++) {
                StaticStore.names[i] = withID(i, MultiLangCont.FNAME.getCont(Pack.def.us.ulist.get(i).forms[0]));
            }
        }
    }

    private String withID(int id, String name) {
        String result;
        String names = name;

        if(name == null)
            names = "";

        if(names.equals("")) {
            result = number(id);
        } else {
            result = number(id)+" - "+names;
        }

        return result;
    }

    private String number(int num) {
        if (0 <= num && num < 10) {
            return "00" + num;
        } else if (10 <= num && num <= 99) {
            return "0" + num;
        } else {
            return String.valueOf(num);
        }
    }

    private String findName(int form, int num, String[] names) {
        String name = null;

        if(names.length>num) {
            String [] wait = names[num].split("\t");
            if(wait.length >= 3 && form+1 < wait.length) {
                if(!wait[form+1].equals("")) {
                    name = wait[form+1];
                }
            }
        }

        return name;
    }

    protected String[][] getName(String [] priority) {
        String[][] result = new String[4][];

        for(int i = 0; i <priority.length;i++) {
            String shortPath = "./lang"+priority[i]+"UnitName.txt";
            String longPath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU";

            VFile.root.build(shortPath, AssetData.getAsset(new File(longPath+shortPath.substring(1))));
            Queue<String> qs = VFile.getFile(shortPath).getData().readLine();
            result[i] = qs.toArray(new String[0]);
        }

        return result;
    }

    private String[][] getExplain(String[] priority) {
        String[][] result = new String[4][];

        for(int i=0;i<priority.length;i++) {
            String shortPath = "./lang"+priority[i]+"UnitExplanation.txt";
            String longPath = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU";

            VFile.root.build(shortPath,AssetData.getAsset(new File(longPath+shortPath.substring(1))));
            Queue<String> qs = VFile.getFile(shortPath).getData().readLine();
            result[i] = qs.toArray(new String[0]);
        }

        return result;
    }

    private String[] findExplain(int form, int num, String[] explains) {
        String[] explain = null;

        if(explains.length > num) {
            String[] lines = explains[num].split("\t");
            if(lines.length >= 3 && form+1 < lines.length) {
                explain = lines[form+1].split("<br>");
            }
        }

        return explain;
    }

    private String[] chooser(String lang) {
        String langs;

        if(lang.equals("")) {
            langs = Locale.getDefault().getLanguage();
        } else {
            langs = lang;
        }

        String [] priority;
        switch (langs) {
            case "en":
                priority = new String[]{"/en/", "/jp/", "/zh/", "/kr/"};
                break;
            case "ja":
                priority = new String[]{"/jp/", "/en/", "/zh/", "/kr/"};
                break;
            case "zh":
                priority = new String[]{"/zh/", "/jp/", "/en/", "/kr/"};
                break;
            case "ko":
                priority = new String[]{"/kr/", "/jp/", "/en/", "/zh/"};
                break;
            default:
                priority = new String[]{"/en/", "/jp/", "/zh/", "/kr/"};
                break;
        }

        return priority;
    }
}
