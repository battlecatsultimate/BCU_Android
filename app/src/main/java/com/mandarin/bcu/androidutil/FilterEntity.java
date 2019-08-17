package com.mandarin.bcu.androidutil;

import com.mandarin.bcu.util.Interpret;

import common.battle.data.MaskEnemy;
import common.battle.data.MaskUnit;
import common.util.pack.Pack;
import common.util.unit.Enemy;
import common.util.unit.Form;
import common.util.unit.Unit;

import java.util.ArrayList;

public class FilterEntity {
    private int entitynumber;


    public FilterEntity(int entitynumber) {
        this.entitynumber = entitynumber;
    }

    public ArrayList<Integer> setFilter() {
        ArrayList<Boolean> b0 = new ArrayList<>();
        ArrayList<Boolean> b1 = new ArrayList<>();
        ArrayList<Boolean> b2 = new ArrayList<>();
        ArrayList<Boolean> b3 = new ArrayList<>();
        ArrayList<Boolean> b4 = new ArrayList<>();

        if(StaticStore.rare.isEmpty()) {
            for (int i = 0; i < entitynumber; i++)
                b0.add(true);
        }

        if(StaticStore.empty) {
            for (int i = 0; i < entitynumber; i++)
                b1.add(true);
        }

        if(StaticStore.attack.isEmpty()) {
            for (int i = 0; i < entitynumber; i++)
                b2.add(true);
        }

        if(StaticStore.tg.isEmpty()) {
            for (int i = 0; i < entitynumber; i++)
                b3.add(true);
        }

        if(StaticStore.ability.isEmpty()) {
            for (int i = 0; i < entitynumber; i++)
                b4.add(true);
        }

        for(Unit u : Pack.def.us.ulist.getList()) {
            b0.add(StaticStore.rare.contains(String.valueOf(u.rarity)));
            ArrayList<Boolean> b10 = new ArrayList<>();
            ArrayList<Boolean> b20 = new ArrayList<>();
            ArrayList<Boolean> b30 = new ArrayList<>();
            ArrayList<Boolean> b40 = new ArrayList<>();
            for(Form f : u.forms) {
                MaskUnit du = StaticStore.talents? f.maxu() : f.du;

                int t = du.getType();
                int a = du.getAbi();

                if(!StaticStore.empty)
                    if(StaticStore.atksimu)
                        b10.add(Interpret.isType(du,1));
                    else
                        b10.add(Interpret.isType(du,0));

                boolean b21 = !StaticStore.atkorand;

                for(int k = 0;k<StaticStore.attack.size();k++) {
                    if(StaticStore.atkorand)
                        b21 |= Interpret.isType(du,Integer.parseInt(StaticStore.attack.get(k)));
                    else
                        b21 &= Interpret.isType(du,Integer.parseInt(StaticStore.attack.get(k)));
                }

                boolean b31 = !StaticStore.tgorand;

                for(int k = 0; k < StaticStore.tg.size();k++) {
                    if(StaticStore.tgorand)
                        b31 |= ((t>>Integer.parseInt(StaticStore.tg.get(k)))&1) == 1;
                    else
                        b31 &= ((t>>Integer.parseInt(StaticStore.tg.get(k)))&1) == 1;
                }

                boolean b41 = !StaticStore.aborand;

                for(int k = 0;k < StaticStore.ability.size();k++) {
                    ArrayList<Integer> vect = StaticStore.ability.get(k);

                    if(vect.get(0) == 0) {
                        boolean bind = (a&vect.get(1)) != 0;
                        if(StaticStore.aborand)
                            b41 |= bind;
                        else
                            b41 &= bind;
                    } else if(vect.get(0) == 1) {
                        if(StaticStore.aborand)
                            b41 |= du.getProc(vect.get(1))[0] > 0;
                        else
                            b41 &= du.getProc(vect.get(1))[0] > 0;
                    }
                }

                b20.add(b21);
                b30.add(b31);
                b40.add(b41);
            }

            if(!StaticStore.empty)
                if(b10.contains(true))
                    b1.add(true);
                else
                    b1.add(false);

            if(b20.contains(true))
                b2.add(true);
            else
                b2.add(false);

            if(b30.contains(true))
                b3.add(true);
            else
                b3.add(false);

            if(b40.contains(true))
                b4.add(true);
            else
                b4.add(false);
        }

        ArrayList<Integer> result = new ArrayList<>();

        for(int i =0;i<entitynumber;i++)
            if(b0.get(i) && b1.get(i) && b2.get(i) && b3.get(i) && b4.get(i))
                result.add(i);


        return result;
    }

    public ArrayList<Integer> EsetFilter() {
        ArrayList<Boolean> b0 = new ArrayList<>();
        ArrayList<Boolean> b1 = new ArrayList<>();
        ArrayList<Boolean> b2 = new ArrayList<>();
        ArrayList<Boolean> b3 = new ArrayList<>();

        if(StaticStore.empty) {
            for(int i = 0; i < entitynumber; i++)
                b0.add(true);
        }

        if(StaticStore.attack.isEmpty())
            for(int i = 0;i < entitynumber;i++)
                b1.add(true);

        if(StaticStore.tg.isEmpty() && !StaticStore.starred)
            for(int i = 0;i < entitynumber;i++)
                b2.add(true);

        if(StaticStore.ability.isEmpty())
            for(int i = 0; i< entitynumber;i++)
                b3.add(true);

        for(Enemy e : Pack.def.es.getList()) {
            boolean b10,b20,b30;

            MaskEnemy de = e.de;

            int t = de.getType();
            int a = de.getAbi();

            if(!StaticStore.empty)
                if(StaticStore.atksimu)
                    b0.add(Interpret.isType(de,1));
                else
                    b0.add(Interpret.isType(de,0));

            b10 = !StaticStore.atkorand;

            for(int k = 0; k < StaticStore.attack.size();k++){
                if(StaticStore.atkorand)
                    b10 |= Interpret.isType(de,Integer.parseInt(StaticStore.attack.get(k)));
                else
                    b10 &= Interpret.isType(de,Integer.parseInt(StaticStore.attack.get(k)));
            }

            if(StaticStore.tg.isEmpty())
                b20 = true;
            else {
                b20 = !StaticStore.tgorand;

                for (int k = 0; k < StaticStore.tg.size(); k++) {
                    if (StaticStore.tgorand)
                        if (StaticStore.tg.get(k).equals(""))
                            b20 = t == 0;
                        else
                            b20 |= ((t >> Integer.parseInt(StaticStore.tg.get(k))) & 1) == 1;
                    else if (StaticStore.tg.get(k).equals(""))
                        b20 = t == 0;
                    else
                        b20 &= ((t >> Integer.parseInt(StaticStore.tg.get(k))) & 1) == 1;
                }
            }

            boolean b21 = de.getStar() == 1;

            b30 = !StaticStore.aborand;

            for(int k = 0; k <StaticStore.ability.size();k++) {
                ArrayList<Integer> vect = StaticStore.ability.get(k);

                if(vect.get(0) == 0) {
                    boolean bind = (a&vect.get(1)) != 0;
                    if(StaticStore.aborand)
                        b30 |= bind;
                    else
                        b30 &= bind;
                } else if(vect.get(0) == 1) {
                    if(StaticStore.aborand)
                        b30 |= de.getProc(vect.get(1))[0] != 0;
                    else
                        b30 &= de.getProc(vect.get(1))[0] != 0;
                }
            }

            b1.add(b10);
            if(StaticStore.starred)
                b2.add(b20 && b21);
            else
                b2.add(b20);
            b3.add(b30);
        }

        ArrayList<Integer> result = new ArrayList<>();

        for(int i =0;i<entitynumber;i++)
            if(b0.get(i) && b1.get(i) && b2.get(i) && b3.get(i))
                result.add(i);

        return result;
    }
}
