package com.mandarin.bcu.androidutil;

import android.content.Context;
import android.os.Environment;

import com.mandarin.bcu.decode.ZipLib;

import java.io.File;
import java.util.Queue;

import common.CommonStatic;
import common.system.MultiLangCont;
import common.system.files.AssetData;
import common.util.stage.CharaGroup;
import common.util.stage.Limit;
import common.util.stage.MapColc;
import common.util.stage.Stage;
import common.util.stage.StageMap;
import common.util.unit.Combo;

public class MapDefiner {
    private final String FILE = "StageName.txt";
    private final String [] LAN = {"/en/","/zh/","/kr/","/jp/"};

    public void define(Context context) {
        try {
            if(StaticStore.map == null) {
                try {
                    MapColc.read();
                    Combo.readFile();
                    Limit.read();
                    CharaGroup.read();
                } catch (Exception e) {
                    ZipLib.init();
                    ZipLib.read();
                    MapColc.read();
                    Combo.readFile();
                    Limit.read();
                    CharaGroup.read();
                }

                StaticStore.map = MapColc.MAPS;
            }

            if(StaticStore.stagelang == 1) {
                for(String l : LAN) {
                    String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/lang"+l+FILE;

                    File f = new File(path);

                    if(f.exists()) {
                        Queue<String> qs = AssetData.getAsset(f).readLine();

                        if(qs != null) {
                            for(String s : qs) {
                                String[] strs = s.trim().split("\t");

                                if(strs.length == 1)
                                    continue;
                                String id = strs[0].trim();
                                String name = strs[strs.length-1].trim();

                                if(id.length() == 0 || name.length() == 0)
                                    continue;

                                String[] ids = id.split("-");

                                int id0 = CommonStatic.parseIntN(ids[0].trim());

                                MapColc mc = StaticStore.map.get(id0);

                                if(mc == null)
                                    continue;

                                if(ids.length == 1) {
                                    MultiLangCont.MCNAME.put(l.substring(1,l.length()-1),mc,name);
                                    continue;
                                }

                                int id1 = CommonStatic.parseIntN(ids[1].trim());

                                if(id1 >= mc.maps.length || id1 < 0)
                                    continue;

                                StageMap stm = mc.maps[id1];

                                if(stm == null)
                                    continue;

                                if(ids.length == 2) {
                                    MultiLangCont.SMNAME.put(l.substring(1,l.length()-1),stm,name);
                                    continue;
                                }

                                int id2 = CommonStatic.parseIntN(ids[2].trim());

                                if(id2 >= stm.list.size() || id2 < 0)
                                    continue;

                                Stage st = stm.list.get(id2);

                                MultiLangCont.STNAME.put(l.substring(1,l.length()-1),st,name);
                            }
                        }
                    }
                }

                StaticStore.stagelang = 0;
            }

            if(StaticStore.mapnames == null) {
                StaticStore.mapnames = new String[StaticStore.map.size()][];

                for(int i = 0; i < StaticStore.mapnames.length; i++) {
                    MapColc mc = StaticStore.map.get(StaticStore.MAPCODE[i]);

                    if(mc == null) continue;

                    StaticStore.mapnames[i] = new String[mc.maps.length];

                    for(int k = 0; k < mc.maps.length; k++) {
                        StaticStore.mapnames[i][k] = MultiLangCont.SMNAME.getCont(mc.maps[k]);
                    }
                }
            }

            if(StaticStore.enemies == null)
                new EDefiner().define(context);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
