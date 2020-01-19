package com.mandarin.bcu.androidutil.medal;

import android.os.Environment;

import com.mandarin.bcu.androidutil.StaticStore;

import java.io.File;
import java.util.Queue;

import common.system.files.AssetData;

public class MDefiner {
    private final String FILEN = "MedalName.txt";
    private final String FILED = "MedalExplanation.txt";
    private final String[] LAN = {"/en/", "/zh/", "/kr/", "/jp/"};

    public void define() {
        if (StaticStore.medalnumber == 0) {
            String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/page/medal/";

            File f = new File(path);

            if (f.exists()) {
                StaticStore.medalnumber = f.list().length;
            }
        }

        if (StaticStore.medallang == 1) {
            StaticStore.MEDNAME.clear();
            StaticStore.MEDEXP.clear();

            for (String l : LAN) {
                String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/lang" + l + FILEN;

                File f = new File(path);

                if (f.exists()) {
                    Queue<String> qs = AssetData.getAsset(f).readLine();

                    if (qs != null) {
                        for (String str : qs) {
                            String[] strs = str.trim().split("\t");

                            if (strs.length == 1) {
                                continue;
                            }

                            int id = Integer.parseInt(strs[0].trim());

                            String name = strs[1].trim();

                            StaticStore.MEDNAME.put(l.substring(1, l.length() - 1), id, name);
                        }
                    }
                }

                path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/lang" + l + FILED;

                f = new File(path);

                if (f.exists()) {
                    Queue<String> qs = AssetData.getAsset(f).readLine();

                    if (qs != null) {
                        for (String str : qs) {
                            String[] strs = str.trim().split("\t");

                            if (strs.length == 1) {
                                continue;
                            }

                            int id = Integer.parseInt(strs[0].trim());

                            String name = strs[1].trim();

                            StaticStore.MEDEXP.put(l.substring(1, l.length() - 1), id, name);
                        }
                    }
                }
            }

            StaticStore.medallang = 0;
        }
    }
}
