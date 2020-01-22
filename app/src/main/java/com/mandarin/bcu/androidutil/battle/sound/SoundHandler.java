package com.mandarin.bcu.androidutil.battle.sound;

import android.os.Environment;

import com.mandarin.bcu.androidutil.battle.asynchs.SoundAsync;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;

import common.CommonStatic;
import common.util.pack.Pack;

public class SoundHandler {
    public static SoundPlayer MUSIC = new SoundPlayer();

    public static ArrayList<ArrayDeque<SoundPlayer>> SE = new ArrayList<>();

    public static boolean[] play;

    public static boolean inBattle = false;
    public static boolean battleEnd = false;
    public static boolean twoMusic = false;
    public static boolean haveToChange = false;
    public static boolean Changed = false;

    public static boolean musicPlay = true;
    public static int mu1 = 3;

    public static boolean sePlay = true;
    public static float se_vol = 1;
    public static float mu_vol = 1;

    public static int speed = 0;

    private static int MAX = 30;

    public static int available = MAX;

    public static void read() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.mandarin.bcu/music/";

        File mf = new File(path);

        if (!mf.exists()) return;

        for (int i = 0; i < mf.listFiles().length; i++) {
            SE.add(new ArrayDeque<>());
        }

        play = new boolean[mf.listFiles().length];

        for (File f : mf.listFiles()) {
            String name = f.getName();

            if (name.length() != 7) continue;

            if (!name.endsWith("ogg")) continue;

            int id = CommonStatic.parseIntN(name.substring(0, 3));

            if (id < 0) continue;

            Pack.def.ms.set(id, f);
        }
    }

    public static void setSE(int ind) {
        if (speed > 1) return;

        if (play[ind]) return;

        if (battleEnd) return;

        new SoundAsync(ind).execute();

        play[ind] = true;
    }

    public static SoundPlayer getMP(int ind) {

        if (!SE.get(ind).isEmpty()) {
            SoundPlayer mp = SE.get(ind).pollFirst();

            if (mp == null) {
                available--;

                return new SoundPlayer();
            }

            available--;

            return mp;
        }

        available--;

        return new SoundPlayer();
    }

    public static void returnBack(SoundPlayer mp, int ind) {
        available++;
        SE.get(ind).add(mp);
    }

    public static void ReleaseAll() {

        for (ArrayDeque<SoundPlayer> d : SE) {
            for (SoundPlayer mp : d) {
                mp.release();
                mp = null;
            }
        }

        available = MAX;
    }

    public static void ResetHandler() {
        ReleaseAll();

        for (int i = 0; i < play.length; i++)
            play[i] = false;

        inBattle = false;
        battleEnd = false;
        twoMusic = false;
        haveToChange = false;
        Changed = false;

        mu1 = 3;

        speed = 0;
    }
}
