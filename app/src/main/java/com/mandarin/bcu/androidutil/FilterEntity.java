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
    private ArrayList<String> rarity;
    private ArrayList<String> attack;
    private ArrayList<String> target;
    private ArrayList<ArrayList<Integer>> ability;
    private boolean atksimu;
    private boolean atkorand;
    private boolean tgorand;
    private boolean aborand;
    private boolean empty;
    private boolean talents;
    private int unitnumber;
    private int enemnumber;


    public FilterEntity(ArrayList<String> rarity, ArrayList<String> attack, ArrayList<String> target,
                        ArrayList<ArrayList<Integer>> ability, boolean atksimu, boolean atkorand, boolean tgorand,
                        boolean aborand, boolean empty, int unitnumber, boolean talents) {
        this.rarity = rarity;
        this.attack = attack;
        this.target = target;
        this.ability = ability;
        this.atksimu = atksimu;
        this.atkorand = atkorand;
        this.tgorand = tgorand;
        this.aborand = aborand;
        this.unitnumber = unitnumber;
        this.empty = empty;
        this.talents = talents;
    }

    public FilterEntity( ArrayList<String> attack, ArrayList<String> target, ArrayList<ArrayList<Integer>> ability,
                        boolean atksimu, boolean atkorand,boolean tgorand, boolean aborand,boolean empty, int enemnumber) {
        this.attack = attack;
        this.target = target;
        this.ability = ability;
        this.aborand = aborand;
        this.tgorand =tgorand;
        this.atksimu = atksimu;
        this.atkorand = atkorand;
        this.empty = empty;
        this.enemnumber = enemnumber;
    }

    public ArrayList<Integer> setFilter() {
        ArrayList<Boolean> b0 = new ArrayList<>();
        ArrayList<Boolean> b1 = new ArrayList<>();
        ArrayList<Boolean> b2 = new ArrayList<>();
        ArrayList<Boolean> b3 = new ArrayList<>();
        ArrayList<Boolean> b4 = new ArrayList<>();

        if(rarity.isEmpty()) {
            for (int i = 0; i < unitnumber; i++)
                b0.add(true);
        }

        if(empty) {
            for (int i = 0; i < unitnumber; i++)
                b1.add(true);
        }

        if(attack.isEmpty()) {
            for (int i = 0; i < unitnumber; i++)
                b2.add(true);
        }

        if(target.isEmpty()) {
            for (int i = 0; i < unitnumber; i++)
                b3.add(true);
        }

        if(ability.isEmpty()) {
            for (int i = 0; i < unitnumber; i++)
                b4.add(true);
        }

        for(Unit u : Pack.def.us.ulist.getList()) {
            b0.add(rarity.contains(String.valueOf(u.rarity)));
            ArrayList<Boolean> b10 = new ArrayList<>();
            ArrayList<Boolean> b20 = new ArrayList<>();
            ArrayList<Boolean> b30 = new ArrayList<>();
            ArrayList<Boolean> b40 = new ArrayList<>();
            for(Form f : u.forms) {
                MaskUnit du = talents? f.maxu() : f.du;

                int t = du.getType();
                int a = du.getAbi();

                if(!empty)
                    if(atksimu)
                        b10.add(Interpret.isType(du,1));
                    else
                        b10.add(Interpret.isType(du,0));

                boolean b21 = !atkorand;

                for(int k = 0;k<attack.size();k++) {
                    if(atkorand)
                        b21 |= Interpret.isType(du,Integer.parseInt(attack.get(k)));
                    else
                        b21 &= Interpret.isType(du,Integer.parseInt(attack.get(k)));
                }

                boolean b31 = !tgorand;

                for(int k = 0; k < target.size();k++) {
                    if(tgorand)
                        b31 |= ((t>>Integer.parseInt(target.get(k)))&1) == 1;
                    else
                        b31 &= ((t>>Integer.parseInt(target.get(k)))&1) == 1;
                }

                boolean b41 = !aborand;

                for(int k = 0;k < ability.size();k++) {
                    ArrayList<Integer> vect = ability.get(k);

                    if(vect.get(0) == 0) {
                        boolean bind = (a&vect.get(1)) != 0;
                        if(aborand)
                            b41 |= bind;
                        else
                            b41 &= bind;
                    } else if(vect.get(0) == 1) {
                        if(aborand)
                            b41 |= du.getProc(vect.get(1))[0] > 0;
                        else
                            b41 &= du.getProc(vect.get(1))[0] > 0;
                    }
                }

                b20.add(b21);
                b30.add(b31);
                b40.add(b41);
            }

            if(!empty)
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

        for(int i =0;i<unitnumber;i++)
            if(b0.get(i) && b1.get(i) && b2.get(i) && b3.get(i) && b4.get(i))
                result.add(i);


        return result;
    }

    public ArrayList<Integer> EsetFilter() {
        ArrayList<Boolean> b0 = new ArrayList<>();
        ArrayList<Boolean> b1 = new ArrayList<>();
        ArrayList<Boolean> b2 = new ArrayList<>();
        ArrayList<Boolean> b3 = new ArrayList<>();

        if(empty) {
            for(int i = 0; i < enemnumber; i++)
                b0.add(true);
        }

        if(attack.isEmpty())
            for(int i = 0;i < enemnumber;i++)
                b1.add(true);

        if(target.isEmpty())
            for(int i = 0;i < enemnumber;i++)
                b2.add(true);

        if(ability.isEmpty())
            for(int i = 0; i< enemnumber;i++)
                b3.add(true);

        for(Enemy e : Pack.def.es.getList()) {
            boolean b10,b20,b30;

            MaskEnemy de = e.de;

            int t = de.getType();
            int a = de.getAbi();

            if(!empty)
                if(atksimu)
                    b0.add(Interpret.isType(de,1));
                else
                    b0.add(Interpret.isType(de,0));

            b10 = !atkorand;

            for(int k = 0; k < attack.size();k++){
                if(atkorand)
                    b10 |= Interpret.isType(de,Integer.parseInt(attack.get(k)));
                else
                    b10 &= Interpret.isType(de,Integer.parseInt(attack.get(k)));
            }

            b20 = !tgorand;

            for(int k = 0; k<target.size();k++) {
                if(tgorand)
                    if(target.get(k).equals(""))
                        b20 = t == 0;
                    else
                        b20 |= ((t>>Integer.parseInt(target.get(k)))&1) == 1;
                else
                    if(target.get(k).equals(""))
                        b20 = t == 0;
                    else
                        b20 &= ((t>>Integer.parseInt(target.get(k)))&1) == 1;
            }

            b30 = !aborand;

            for(int k = 0; k <ability.size();k++) {
                ArrayList<Integer> vect = ability.get(k);

                if(vect.get(0) == 0) {
                    boolean bind = (a&vect.get(1)) != 0;
                    if(aborand)
                        b30 |= bind;
                    else
                        b30 &= bind;
                } else if(vect.get(0) == 1) {
                    if(aborand)
                        b30 |= de.getProc(vect.get(1))[0] > 0;
                    else
                        b30 &= de.getProc(vect.get(1))[0] > 0;
                }
            }

            b1.add(b10);
            b2.add(b20);
            b3.add(b30);
        }

        ArrayList<Integer> result = new ArrayList<>();

        for(int i =0;i<enemnumber;i++)
            if(b0.get(i) && b1.get(i) && b2.get(i) && b3.get(i))
                result.add(i);

        return result;
    }
}
