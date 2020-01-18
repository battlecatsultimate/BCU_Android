package com.mandarin.bcu.androidutil.battle;

import com.mandarin.bcu.androidutil.StaticStore;

import common.util.Res;
import common.util.pack.Background;
import common.util.pack.EffAnim;
import common.util.pack.NyCastle;
import common.util.pack.Soul;

public class BDefinder {
    public void define() {
        if(!StaticStore.effread) {
            EffAnim.read();

            StaticStore.effread = true;
        }

        if(StaticStore.bgread == 0) {
            Background.read();

            StaticStore.bgread = 1;
        }

        if(!StaticStore.soulread) {
            Soul.read();

            StaticStore.soulread = true;
        }

        if(!StaticStore.nycread) {
            NyCastle.read();

            StaticStore.nycread = true;
        }

        if(!StaticStore.resread) {
            Res.readData();

            StaticStore.resread = true;
        }
    }
}
