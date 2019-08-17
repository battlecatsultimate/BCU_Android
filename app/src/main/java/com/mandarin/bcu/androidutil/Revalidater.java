package com.mandarin.bcu.androidutil;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

import common.system.MultiLangCont;
import common.util.pack.Pack;
import common.util.stage.MapColc;

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

        if(StaticStore.enames != null) {
            StaticStore.enames = new String[StaticStore.emnumber];

            for(int i = 0;i < StaticStore.emnumber;i++) {
                StaticStore.enames[i] = withID(i,MultiLangCont.ENAME.getCont(Pack.def.es.get(i)));
            }
        }

        if(StaticStore.mapnames != null) {
            for(int i = 0;i < MapColc.MAPS.size(); i++) {
                MapColc mc = MapColc.MAPS.get(StaticStore.MAPCODE[i]);

                if(mc == null) continue;

                for(int k = 0; k < mc.maps.length; k ++) {
                    StaticStore.mapnames[i][k] = MultiLangCont.SMNAME.getCont(mc.maps[k]);
                }
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
}
