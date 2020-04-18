package com.mandarin.bcu.decode;

import android.util.Log;

import com.mandarin.bcu.androidutil.StaticStore;

import java.io.File;
import java.io.IOException;

import common.system.files.AssetData;
import common.system.files.VFile;
import main.Opts;

public class ZipLib {

    public static final String[] LIBREQS = StaticStore.LIBREQ;
    public static final String[] OPTREQS = StaticStore.OPTREQS;

    public static String lib;
    public static LibInfo info;

    public static void check() {
        for (String req : LIBREQS)
            if (info == null || !info.merge.set.contains(req)) {
                Opts.loadErr("this version requires lib " + req);
            }
    }

    public static void init(String path) {
        File f = new File(path);

        if (!f.exists())
            return;
        info = new LibInfo(path);
    }

    public static void merge(File f) {
        try {

            LibInfo nlib = new LibInfo("");
            info.merge(nlib);

            if(!f.delete()) {
                Log.e("ZipLib", "Failed to delete file "+f.getAbsolutePath());
            }

        } catch (IOException e) {
            Opts.loadErr("failed to merge lib");
            e.printStackTrace();
        }
    }

    public static void read(String prev) {
        for (PathInfo pi : info.merge.paths.values()) {
            if (pi.type != 0)
                continue;
            VFile.root.build(pi.path, AssetData.getAsset(new File(prev + pi.path.substring(2))));
        }

        VFile.root.sort();
    }

}