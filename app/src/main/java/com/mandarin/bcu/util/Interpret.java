package com.mandarin.bcu.util;

import android.content.Context;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import common.battle.BasisLU;
import common.battle.BasisSet;
import common.battle.Treasure;
import common.battle.data.MaskAtk;
import common.battle.data.MaskEnemy;
import common.battle.data.MaskUnit;
import common.util.Data;
import common.util.stage.MapColc;
import common.util.unit.Combo;
import common.util.unit.Enemy;
import common.util.unit.Unit;

public class Interpret extends Data {

    public static final String EN = "en";
    public static final String ZH = "zh";
    public static final String JA = "ja";
    public static final String KO = "ko";
    public static final String RU = "ru";
    public static final String FR = "fr";

    /**
     * enemy types
     */
    public static String[] ERARE;

    /**
     * unit rarities
     */
    public static String[] RARITY;

    /**
     * enemy traits
     */
    public static String[] TRAIT;

    /**
     * star names
     */
    public static String[] STAR;

    /**
     * ability name
     */
    public static String[] ABIS;

    /**
     * enemy ability name
     */
    public static String[] EABI;

    public static final int RESNUM = 9;

    public static String[] SABIS;
    public static String[] PROC;
    public static String[] SPROC;
    public static String[] TREA;
    public static String[] TEXT;
    public static String[] ATKCONF;
    public static String[] COMF;
    public static String[] COMN;
    public static String[] TCTX;
    public static String[] PCTX;

    /**
     * treasure orderer
     */
    public static final int[] TIND = {0, 1, 18, 19, 20, 21, 22, 23, 2, 3, 4, 5, 24, 25, 26, 27, 28, 6, 7, 8, 9, 10, 11,
            12, 13, 14, 15, 16, 17, 29, 30, 31, 32, 33, 34, 35};

    /**
     * treasure grouper
     */
    public static final int[][] TCOLP = {{0, 6}, {8, 6}, {14, 3}, {17, 4}, {21, 3}, {29, 7}};

    /**
     * treasure max
     */
    private static final int[] TMAX = {30, 30, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 600, 1500, 100,
            100, 100, 30, 30, 30, 30, 30, 10, 300, 300, 600, 600, 600, 20, 20, 20, 20, 20, 20, 20};

    /**
     * proc data formatter
     */
    private static final int[][] CMP = {{0, -1}, {0, -1, 1}, {0, -1, 1}, {0, -1}, {0, 2, -1},
            {0, -1, 3, 1}, {0, -1}, {0, -1, 1, 4}, {0, -1, 1}, {5, -1, 6}, {0, -1}, {-1, 7, 4},
            {-1, 7, 9, 10}, {-1, 14}, {-1, 13}, {-1, 13}, {-1, 15}, {-1, 13}, {-1, 16}, {-1, 13}, {0, -1},
            {0, -1, 1}, {0, -1, 1}, {0, -1, 4}, {0, -1, 21, 1, 22, 23, 24}, {0, -1, 1}, {0, -1}, {0, -1}, {-1}, {0, -1, 17},
            {0, -1, 1}, {0, -1, 18}, {0, -1, 1, 19, 20}, {-1}, {-1}, {0, -1, 1, 50}, {-1}};

    /**
     * proc data locator
     */
    private static final int[][] LOC = {{0, -1}, {0, -1, 1}, {0, -1, 1}, {0, -1}, {0, 1, -1},
            {0, -1, 2, 1}, {0, -1}, {0, -1, 1, 2}, {0, -1, 1}, {0, -1, 1}, {0, -1}, {-1, 0, 1},
            {-1, 0, 1, 2}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {0, -1},
            {0, -1, 1}, {0, -1, 1}, {0, -1, 2}, {0, -1, 5, 3, 1, 2, 4}, {0, -1, 1}, {0, -1, 1, 4, 2, 3}, {0, -1}, {-1}, {0, -1, 1},
            {0, -1, 1}, {0, -1, 1}, {0, -1, 3, 1, 2}, {-1}, {-1}, {0, -1, 1, 2}, {0, -1, 1, 3, 2}};

    private static final int[][] VENOM_CMP = {{0, -1, 1, 25, 26, 30}, {0, -1, 1, 25, 27, 30}, {0, -1, 1, 25, 28, 30}, {0, -1, 1, 25, 29 ,30}};

    private static final int[][] SUMMON_CMP_U = {{0, -1, 31, 32, 9, 25}, {0, -1, 31, 9, 25, 44}};
    private static final int[][] SUMMON_LOC_U = {{0, -1, 1, 2, 5, 4}, {0, -1, 1, 5, 4, 2}};

    private static final int[][] SUMMON_CMP_E = {{0, -1, 31, 32, 45, 9, 25}, {0, -1, 31, 45, 9, 25, 44}};
    private static final int[][] SUMMON_LOC_E = {{0, -1, 1, 2, 3, 5, 4}, {0, -1, 1, 3, 5, 4, 2}};

    private static final int[][] SPEED_CMP = {{0, -1, 1, 25, 47}, {0, -1, 1, 25, 48}, {0, -1, 1, 25, 49}};

    /**
     * proc data formatter for KR,JP
     */
    private static final int[][] CMP2 = {{0, -1}, {0, 1, -1}, {0, 1, -1}, {0, -1}, {0, 2, -1},
            {0, 3, 1, -1}, {0, -1}, {0, 1, 4, -1}, {0, 1, -1}, {5, 6, -1}, {0, -1}, {4, 7, -1},
            {9, 10, 7, -1}, {-1, 14}, {-1, 13}, {-1, 13}, {-1, 15}, {-1, 13}, {-1, 16}, {-1, 13}, {0, -1},
            {0, 1, -1}, {0, 1, -1}, {0, 4, -1}, {0, 21, 1, -1, 24, 23, 22}, {0, 1, -1}, {0, -1}, {0, -1}, {-1}, {0, -1, 17},
            {0, 1, -1}, {0, -1, 18}, {0, 1, -1, 19, 20}, {-1}, {-1}, {0, 1, -1, 50}, {-1}};

    /**
     * proc data locator for KR,JP
     */
    private static final int[][] LOC2 = {{0, -1}, {0, 1, -1}, {0, 1, -1}, {0, -1}, {0, 1, -1},
            {0, 2, 1, -1}, {0, -1}, {0, 1, 2, -1}, {0, 1, -1}, {0, 1, -1}, {0, -1}, {1, 0, -1},
            {1, 2, 0, -1}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {-1, 0}, {0, -1},
            {0, 1, -1}, {0, 1, -1}, {0, 2, -1}, {0, 5, 3, -1, 4, 2, 1}, {0, 1, -1}, {0, 1, -1, 4, 3, 2}, {0, -1}, {-1}, {0, -1, 1},
            {0, 1, -1}, {0, -1, 1}, {0, 3, -1, 1, 2}, {-1}, {-1}, {0, 1, -1, 2}, {0, 1, -1, 3, 2}};

    private static final int[][] VENOM_CMP2 = {{0, 1, -1, 25, 30, 26}, {0, 1, -1, 25, 30, 27}, {0, 1, -1, 25, 30, 28}, {0, 1, -1, 25, 30, 29}};

    private static final int[][] SUMMON_CMP2_U = {{0, 9, 32, 31, -1, 25}, {0, 9, 31, -1, 25, 44}};
    private static final int[][] SUMMON_LOC2_U = {{0, 5, 2, 1, -1, 4}, {0, 5, 1, -1, 4, 2}};

    private static final int[][] SUMMON_CMP2_E = {{0, 9, 32, 45, 31, -1 ,25}, {0, 9, 45, 31, -1, 25, 44}};
    private static final int[][] SUMMON_LOC2_E = {{0, 5, 2, 3, 1, -1, 4}, {0, 5, 3, 1, -1, 4, 2}};

    private static final int[][] SPEED_CMP2 = {{0, 1, -1, 25, 47}, {0, 1, -1, 25, 48}, {0, 1, -1, 25, 49}};

    /**
     * combo string component
     */
    private static final String[][] CDP = {{"", "+", "-"}, {"_", "_%", "_f", "Lv._"}};

    /**
     * combo string formatter
     */
    private static final int[][] CDC = {{1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 3}, {1, 0}, {1, 1}, {2, 2},
            {}, {1, 1}, {1, 1}, {2, 2}, {1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1},
            {1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}};

    private static final List<Integer> immune = Arrays.asList(13, 14, 15, 16, 17, 18, 19, 33, 34);

    public static final int[] EABIIND = {5, 7, 8, 9, 10, 11, 12, 15, 16, 18, 113, 114, 115, 116, 117, 118, 119};
    public static final int[] ABIIND = {113, 114, 115, 116, 117, 118, 119};
    public static final int IMUSFT = 13, EFILTER = 8;

    public static String[] comboInfo(int[] inc) {
        List<String> ls = new ArrayList<>();
        for (int i = 0; i < C_TOT; i++) {
            if (inc[i] == 0)
                continue;
            ls.add(combo(i, inc[i]));
        }
        return ls.toArray(new String[0]);
    }

    public static int getComp(int ind, Treasure t) {
        int ans = -2;
        for (int i = 0; i < TCOLP[ind][1]; i++) {
            int temp = getValue(TIND[i + TCOLP[ind][0]], t);
            if (ans == -2)
                ans = temp;
            else if (ans != temp)
                return -1;
        }
        return ans;
    }

    public static String getTrait(int type, int star) {
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < TRAIT.length; i++)
            if (((type >> i) & 1) > 0) {
                if (i == 6 && star == 1)
                    ans.append(TRAIT[i]).append(" ").append("(").append(STAR[star]).append(")").append(", ");
                else
                    ans.append(TRAIT[i]).append(", ");
            }
        return ans.toString();
    }

    public static List<String> getProc(Context ac, MaskUnit du, int cmp, int frse) {
        List<Integer> res = new ArrayList<>();
        String lang = Locale.getDefault().getLanguage();

        for(int i = immune.size()-1; i >= 0; i--) {
            res.add(PROC.length-i);
        }

        List<String> l = new ArrayList<>();
        List<Integer> id = new ArrayList<>();

        MaskAtk mr = du.getRepAtk();

        if (cmp == 0) {
            //EN Handling

            for (int i = 0; i < PROC.length-RESNUM; i++) {
                if (mr.getProc(i)[0] == 0)
                    continue;

                int [] c;

                if(i == P_POISON) {
                    c = VENOM_CMP[getVenomType(mr.getProc(i)[4])];
                } else if (i == P_SUMMON) {
                    c = SUMMON_CMP_U[getSummonType(mr.getProc(i)[4])];
                } else if (i == P_SPEED) {
                    c = SPEED_CMP[getSpeedType(mr.getProc(i)[3])];
                } else {
                    c = CMP[i];
                }

                int [] loc;

                if(i == P_SUMMON) {
                    loc = SUMMON_LOC_U[getSummonType(mr.getProc(i)[4])];
                } else{
                    loc = LOC[i];
                }

                StringBuilder ans = getProcNameEN(ac, i, mr, c, loc, res, frse,true, lang);

                if(!l.contains(ans.toString())) {
                    if(id.contains(i)) {
                        if(StaticStore.isEnglish()) {
                            ans.append(" [").append(getNumberAttack(numberWithExtension(1, lang), lang)).append("]");
                        } else {
                            ans.append(" [").append(TEXT[46].replace("_", Integer.toString(1))).append("]");
                        }
                    }

                    l.add(ans.toString());
                    id.add(i);
                }
            }
        } else {
            // KR/JP Handling

            for (int i = 0; i < PROC.length-RESNUM; i++) {
                if (mr.getProc(i)[0] == 0)
                    continue;

                int[] c2;

                if(i == P_POISON) {
                    c2 = VENOM_CMP2[getVenomType(mr.getProc(i)[4])];
                } else if(i == P_SUMMON) {
                    c2 = SUMMON_CMP2_U[getSummonType(mr.getProc(i)[4])];
                } else if (i == P_SPEED) {
                    c2 = SPEED_CMP2[getSpeedType(mr.getProc(i)[3])];
                } else {
                    c2 = CMP2[i];
                }

                int[] loc2;

                if(i == P_SUMMON) {
                    loc2 = SUMMON_LOC2_U[getSummonType(mr.getProc(i)[4])];
                } else {
                    loc2 = LOC2[i];
                }

                StringBuilder ans = getProcNameKR(ac, i, mr, c2, loc2, res, frse, true);

                if(!l.contains(ans.toString())) {
                    if(id.contains(i)) {
                        ans.append(" [").append(TEXT[46].replace("_", Integer.toString(1))).append("]");
                    }

                    l.add(ans.toString());
                    id.add(i);
                }
            }
        }

        for(int k = 0; k < du.getAtkCount(); k++) {
            MaskAtk ma = du.getAtkModel(k);

            if (cmp == 0) {
                //EN Handling

                for (int i = 0; i < PROC.length-RESNUM; i++) {
                    if (ma.getProc(i)[0] == 0)
                        continue;

                    int [] c;

                    if(i == P_POISON) {
                        c = VENOM_CMP[getVenomType(ma.getProc(i)[4])];
                    } else if (i == P_SUMMON) {
                        c = SUMMON_CMP_U[getSummonType(ma.getProc(i)[4])];
                    } else if (i == P_SPEED) {
                        c = SPEED_CMP[getSpeedType(ma.getProc(i)[3])];
                    } else {
                        c = CMP[i];
                    }

                    int [] loc;

                    if(i == P_SUMMON) {
                        loc = SUMMON_LOC_U[getSummonType(ma.getProc(i)[4])];
                    } else{
                        loc = LOC[i];
                    }

                    StringBuilder ans = getProcNameEN(ac, i, ma, c, loc, res, frse, true, lang);

                    if(!l.contains(ans.toString())) {
                        if(id.contains(i)) {
                            if(StaticStore.isEnglish()) {
                                ans.append(" [").append(getNumberAttack(numberWithExtension(k + 1, lang),lang)).append("]");
                            } else {
                                ans.append(" [").append(TEXT[46].replace("_", Integer.toString(k+1))).append("]");
                            }
                        }

                        l.add(ans.toString());
                        id.add(i);
                    }
                }
            } else {
                // KR/JP Handling

                for (int i = 0; i < PROC.length-RESNUM; i++) {
                    if (ma.getProc(i)[0] == 0)
                        continue;

                    int[] c2;

                    if(i == P_POISON) {
                        c2 = VENOM_CMP2[getVenomType(ma.getProc(i)[4])];
                    } else if(i == P_SUMMON) {
                        c2 = SUMMON_CMP2_U[getSummonType(ma.getProc(i)[4])];
                    } else if (i == P_SPEED) {
                        c2 = SPEED_CMP2[getSpeedType(ma.getProc(i)[3])];
                    } else {
                        c2 = CMP2[i];
                    }

                    int[] loc2;

                    if(i == P_SUMMON) {
                        loc2 = SUMMON_LOC2_U[getSummonType(ma.getProc(i)[4])];
                    } else {
                        loc2 = LOC2[i];
                    }

                    StringBuilder ans = getProcNameKR(ac, i, ma, c2, loc2, res, frse, true);

                    if(!l.contains(ans.toString())) {
                        if(id.contains(i)) {
                            ans.append(" [").append(TEXT[46].replace("_", Integer.toString(k+1))).append("]");
                        }

                        l.add(ans.toString());
                        id.add(i);
                    }
                }
            }
        }

        return l;
    }

    public static List<String> getProc(Context activity, MaskEnemy du, int cmp, int frse) {
        List<Integer> res = new ArrayList<>();
        String lang = Locale.getDefault().getLanguage();

        for(int i = immune.size()-1; i >= 0; i--) {
            res.add(PROC.length-i);
        }

        List<String> l = new ArrayList<>();
        List<Integer> id = new ArrayList<>();

        MaskAtk mr = du.getRepAtk();

        if (cmp == 0) {
            // EN Handling

            for (int i = 0; i < PROC.length-RESNUM; i++) {
                if (mr.getProc(i)[0] == 0)
                    continue;

                int [] c;

                if(i == P_POISON) {
                    c = VENOM_CMP[getVenomType(mr.getProc(i)[4])];
                } else if(i == P_SUMMON) {
                    c = SUMMON_CMP_E[getSummonType(mr.getProc(i)[4])];
                } else if (i == P_SPEED) {
                    c = SPEED_CMP[getSpeedType(mr.getProc(i)[3])];
                } else {
                    c = CMP[i];
                }

                int [] loc;

                if(i == P_SUMMON) {
                    loc = SUMMON_LOC_E[getSummonType(mr.getProc(i)[4])];
                } else {
                    loc = LOC[i];
                }

                StringBuilder ans = getProcNameEN(activity, i, mr, c, loc, res, frse, false, lang);

                if(!l.contains(ans.toString())) {
                    if(id.contains(i)) {
                        if(StaticStore.isEnglish()) {
                            ans.append(" [").append(getNumberAttack(numberWithExtension(1, lang),lang)).append("]");
                        } else {
                            ans.append(" [").append(TEXT[46].replace("_", Integer.toString(1))).append("]");
                        }
                    }

                    l.add(ans.toString());
                    id.add(i);
                }
            }
        } else {
            // KR/JP Handling

            for (int i = 0; i < PROC.length - RESNUM; i++) {
                int [] c2;

                if(i == P_POISON) {
                    c2 = VENOM_CMP2[getVenomType(mr.getProc(i)[4])];
                } else if(i == P_SUMMON) {
                    c2 = SUMMON_CMP2_E[getSummonType(mr.getProc(i)[4])];
                } else if(i == P_SPEED) {
                    c2 = SPEED_CMP2[getSpeedType(mr.getProc(i)[3])];
                } else {
                    c2 = CMP2[i];
                }

                int [] loc2;

                if(i == P_SUMMON) {
                    loc2 = SUMMON_LOC2_E[getSummonType(mr.getProc(i)[4])];
                } else {
                    loc2 = LOC2[i];
                }

                if (mr.getProc(i)[0] == 0)
                    continue;

                StringBuilder ans = getProcNameKR(activity, i, mr, c2, loc2, res, frse, false);

                if(!l.contains(ans.toString())) {
                    if(id.contains(i)) {
                        ans.append(" [").append(TEXT[46].replace("_", Integer.toString(1))).append("]");
                    }

                    l.add(ans.toString());
                    id.add(i);
                }
            }
        }

        for(int k = 0; k < du.getAtkCount(); k++) {
            MaskAtk ma = du.getAtkModel(k);

            if (cmp == 0) {
                // EN Handling

                for (int i = 0; i < PROC.length-RESNUM; i++) {
                    if (ma.getProc(i)[0] == 0)
                        continue;

                    int [] c;

                    if(i == P_POISON) {
                        c = VENOM_CMP[getVenomType(ma.getProc(i)[4])];
                    } else if(i == P_SUMMON) {
                        c = SUMMON_CMP_E[getSummonType(ma.getProc(i)[4])];
                    } else if(i == P_SPEED) {
                        c = SPEED_CMP[getSpeedType(ma.getProc(i)[3])];
                    } else {
                        c = CMP[i];
                    }

                    int [] loc;

                    if(i == P_SUMMON) {
                        loc = SUMMON_LOC_E[getSummonType(ma.getProc(i)[4])];
                    } else {
                        loc = LOC[i];
                    }

                    StringBuilder ans = getProcNameEN(activity, i, ma, c, loc, res, frse, false, lang);

                    if(!l.contains(ans.toString())) {
                        if(id.contains(i)) {
                            if(StaticStore.isEnglish()) {
                                ans.append(" [").append(getNumberAttack(numberWithExtension(k + 1, lang),lang)).append("]");
                            } else {
                                ans.append(" [").append(TEXT[46].replace("_", Integer.toString(k+1))).append("]");
                            }
                        }

                        l.add(ans.toString());
                        id.add(i);
                    }
                }
            } else {
                // KR/JP Handling

                for (int i = 0; i < PROC.length - RESNUM; i++) {
                    int [] c2;

                    if(i == P_POISON) {
                        c2 = VENOM_CMP2[getVenomType(ma.getProc(i)[4])];
                    } else if(i == P_SUMMON) {
                        c2 = SUMMON_CMP2_E[getSummonType(ma.getProc(i)[4])];
                    } else if(i == P_SPEED) {
                        c2 = SPEED_CMP2[getSpeedType(ma.getProc(i)[3])];
                    } else {
                        c2 = CMP2[i];
                    }

                    int [] loc2;

                    if(i == P_SUMMON) {
                        loc2 = SUMMON_LOC2_E[getSummonType(ma.getProc(i)[4])];
                    } else {
                        loc2 = LOC2[i];
                    }

                    if (ma.getProc(i)[0] == 0)
                        continue;

                    StringBuilder ans = getProcNameKR(activity, i, ma, c2, loc2, res, frse, false);

                    if(!l.contains(ans.toString())) {
                        if(id.contains(i)) {
                            ans.append(" [").append(TEXT[46].replace("_", Integer.toString(k+1))).append("]");
                        }

                        l.add(ans.toString());
                        id.add(i);
                    }
                }
            }
        }

        return l;
    }

    private static StringBuilder getProcNameEN(Context ac, int i, MaskAtk ma, int[] c, int[] loc, List<Integer> res, int frse, boolean unit, String lang) {
        StringBuilder ans = new StringBuilder();

        ans.append(i).append("\\");

        for (int j = 0; j < c.length; j++) {
            if (c[j] == -1)
                if (Interpret.immune.contains(i) && StaticStore.isEnglish()) {
                    int pro = ma.getProc(i)[0];
                    if (pro != 100)
                        ans.append(TEXT[12]).append(PROC[i].substring(4));
                    else
                        ans.append(TEXT[11]).append(PROC[i].substring(4));
                } else {
                    int pro = ma.getProc(i)[0];

                    if (Interpret.immune.contains(i) && pro != 100)
                        ans.append(PROC[res.get(Interpret.immune.indexOf(i))]);
                    else
                        ans.append(PROC[i]);
                }
            else {
                if (frse == 0) {
                    int pro = ma.getProc(i)[loc[j]];

                    String rep = pro == -1 ? ac.getString(R.string.infinity) : "" + pro;

                    if (Interpret.immune.contains(i) && pro != 100)
                        ans.append(TEXT[c[j]].replace("_", rep));
                    else if (!Interpret.immune.contains(i)) {
                        switch (i) {
                            case P_BURROW:
                            case P_REVIVE:
                                if (StaticStore.isEnglish()) {
                                    if (c[j] == 7)
                                        ans.append(getOnceTwice(pro, lang, ac));
                                    else
                                        ans.append(TEXT[c[j]].replace("_", rep));
                                } else
                                    ans.append(TEXT[c[j]].replace("_", rep));

                                break;
                            case P_SUMMON:
                                if(c[j] == 25) {
                                    ans.append(TEXT[c[j]].replace("_", rep)).append(getSummonTypeText(ma.getProc(i)[4], unit));
                                } else if(c[j] == 31) {
                                    if(unit) {
                                        ans.append(TEXT[c[j]].replace("_", getUnit(Integer.parseInt(rep))));
                                    } else {
                                        ans.append(TEXT[c[j]].replace("_", getEnemy(Integer.parseInt(rep))));
                                    }
                                } else {
                                    ans.append(TEXT[c[j]].replace("_", rep));
                                }
                                break;
                            default:
                                ans.append(TEXT[c[j]].replace("_", rep));
                                break;
                        }
                    }
                } else {
                    if (TEXT[c[j]].contains("_f")) {
                        int pro = ma.getProc(i)[loc[j]];

                        String rep = pro == -1 ? ac.getString(R.string.infinity) : new DecimalFormat("#.##").format((double) pro / 30);

                        ans.append(TEXT[c[j]].replace("_f", "_s").replace("_", rep));
                    } else {
                        int pro = ma.getProc(i)[loc[j]];

                        String rep = pro == -1 ? ac.getString(R.string.infinity) : "" + pro;

                        if (Interpret.immune.contains(i) && pro != 100) {
                            ans.append(TEXT[c[j]].replace("_", rep));
                        } else if (!Interpret.immune.contains(i)) {
                            switch (i) {
                                case P_BURROW:
                                case P_REVIVE:
                                    if (StaticStore.isEnglish()) {
                                        if (c[j] == 7)
                                            ans.append(getOnceTwice(pro, lang, ac));
                                        else
                                            ans.append(TEXT[c[j]].replace("_", rep));
                                    } else
                                        ans.append(TEXT[c[j]].replace("_", rep));

                                    break;
                                case P_SUMMON:
                                    if(c[j] == 25) {
                                        ans.append(TEXT[c[j]].replace("_", rep)).append(getSummonTypeText(ma.getProc(i)[4], unit));
                                    } else if(c[j] == 31) {
                                        if(unit) {
                                            ans.append(TEXT[c[j]].replace("_", getUnit(Integer.parseInt(rep))));
                                        } else {
                                            ans.append(TEXT[c[j]].replace("_", getEnemy(Integer.parseInt(rep))));
                                        }
                                    } else {
                                        ans.append(TEXT[c[j]].replace("_", rep));
                                    }
                                    break;
                                default:
                                    ans.append(TEXT[c[j]].replace("_", rep));
                                    break;
                            }
                        }
                    }
                }
            }
        }

        return ans;
    }

    private static StringBuilder getProcNameKR(Context ac, int i, MaskAtk ma, int[] c, int[] l, List<Integer> res, int frse, boolean unit) {
        StringBuilder ans = new StringBuilder();

        ans.append(i).append("\\");

        for (int j = 0; j < c.length; j++) {
            if (c[j] == -1) {
                if (Interpret.immune.contains(i)) {
                    int pro = ma.getProc(i)[0];

                    if (pro != 100)
                        ans.append(PROC[res.get(Interpret.immune.indexOf(i))]);
                    else
                        ans.append(PROC[i]);
                } else
                    ans.append(PROC[i]);
            } else {
                if (frse == 0) {
                    int pro = ma.getProc(i)[l[j]];

                    String rep = pro == -1 ? ac.getString(R.string.infinity) : "" + pro;

                    if (Interpret.immune.contains(i) && pro != 100)
                        ans.append(TEXT[c[j]].replace("_", rep));
                    else if (!Interpret.immune.contains(i))
                        if(i == P_SUMMON && c[j] == 25) {
                            ans.append(TEXT[c[j]].replace("_", rep)).append(getSummonTypeText(ma.getProc(i)[4], unit));
                        } else if(i == P_SUMMON && c[j] == 31) {
                            if(unit) {
                                ans.append(TEXT[c[j]].replace("_", getUnit(Integer.parseInt(rep))));
                            } else {
                                ans.append(TEXT[c[j]].replace("_", getEnemy(Integer.parseInt(rep))));
                            }
                        } else {
                            ans.append(TEXT[c[j]].replace("_", rep));
                        }
                } else {
                    if (TEXT[c[j]].contains("_f")) {
                        int pro = ma.getProc(i)[l[j]];

                        String rep = pro == -1 ? ac.getString(R.string.infinity) : new DecimalFormat("#.##").format((double) pro / 30);

                        ans.append(TEXT[c[j]].replace("_f", "_s").replace("_", rep));
                    } else {
                        int pro = ma.getProc(i)[l[j]];

                        String rep = pro == -1 ? ac.getString(R.string.infinity) : "" + pro;

                        if (Interpret.immune.contains(i) && pro != 100)
                            ans.append(TEXT[c[j]].replace("_", rep));
                        else if (!Interpret.immune.contains(i))
                            if(i == P_SUMMON && c[j] == 25) {
                                ans.append(TEXT[c[j]].replace("_", rep)).append(getSummonTypeText(ma.getProc(i)[4], unit));
                            } else if(i == P_SUMMON && c[j] == 31) {
                                if(unit) {
                                    ans.append(TEXT[c[j]].replace("_", getUnit(Integer.parseInt(rep))));
                                } else {
                                    ans.append(TEXT[c[j]].replace("_", getEnemy(Integer.parseInt(rep))));
                                }
                            } else {
                                ans.append(TEXT[c[j]].replace("_", rep));
                            }
                    }
                }
            }
        }

        return ans;
    }

    public static List<Integer> getAbiid(MaskUnit me) {
        List<Integer> l = new ArrayList<>();

        for (int i = 0; i < ABIS.length; i++)
            if (((me.getAbi() >> i) & 1) > 0)
                l.add(i);

        return l;
    }

    public static List<Integer> getAbiid(MaskEnemy me) {
        List<Integer> l = new ArrayList<>();

        for (int i = 0; i < ABIS.length; i++)
            if (((me.getAbi() >> i) & 1) > 0)
                l.add(i);

        return l;
    }


    public static List<String> getAbi(MaskUnit me, String[][] frag, String[] addition, int lang) {
        List<String> l = new ArrayList<>();

        for (int i = 0; i < ABIS.length; i++) {
            StringBuilder imu = new StringBuilder(frag[lang][0]);

            if (((me.getAbi() >> i) & 1) > 0) {
                if (ABIS[i].startsWith("Imu.")) {
                    imu.append(ABIS[i].substring(4));
                } else {
                    if (i == 0)
                        l.add(ABIS[i] + addition[0]);
                    else if (i == 1)
                        l.add(ABIS[i] + addition[1]);
                    else if (i == 2)
                        l.add(ABIS[i] + addition[2]);
                    else if (i == 4)
                        l.add(ABIS[i] + addition[3]);
                    else if (i == 5)
                        l.add(ABIS[i] + addition[4]);
                    else if (i == 14)
                        l.add(ABIS[i] + addition[5]);
                    else if (i == 17)
                        l.add(ABIS[i] + addition[6]);
                    else if (i == 20)
                        l.add(ABIS[i] + addition[7]);
                    else if (i == 21)
                        l.add(ABIS[i] + addition[8]);
                    else
                        l.add(ABIS[i]);
                }
            }

            if (!imu.toString().isEmpty() && !imu.toString().equals(frag[lang][0]))
                l.add(imu.toString());
        }

        return l;
    }

    public static List<String> getAbi(MaskEnemy me, String[][] frag, String[] addition, int lang) {
        List<String> l = new ArrayList<>();

        for (int i = 0; i < ABIS.length; i++) {
            StringBuilder imu = new StringBuilder(frag[lang][0]);

            if (((me.getAbi() >> i) & 1) > 0)
                if (ABIS[i].startsWith("Imu."))
                    imu.append(ABIS[i].substring(4));
                else {
                    if (i == 0)
                        l.add(ABIS[i] + addition[0]);
                    else if (i == 1)
                        l.add(ABIS[i] + addition[1]);
                    else if (i == 2)
                        l.add(ABIS[i] + addition[2]);
                    else if (i == 4)
                        l.add(ABIS[i] + addition[3]);
                    else if (i == 5)
                        l.add(ABIS[i] + addition[4]);
                    else if (i == 14)
                        l.add(ABIS[i] + addition[5]);
                    else if (i == 17)
                        l.add(ABIS[i] + addition[6]);
                    else if (i == 20)
                        l.add(ABIS[i] + addition[7]);
                    else if (i == 21)
                        l.add(ABIS[i] + addition[8]);
                    else
                        l.add(ABIS[i]);
                }

            if (!imu.toString().isEmpty() && !imu.toString().equals(frag[lang][0]))
                l.add(imu.toString());
        }

        return l;
    }

    public static int getValue(int ind, Treasure t) {
        if (ind == 0)
            return t.tech[LV_RES];
        else if (ind == 1)
            return t.tech[LV_ACC];
        else if (ind == 2)
            return t.trea[T_ATK];
        else if (ind == 3)
            return t.trea[T_DEF];
        else if (ind == 4)
            return t.trea[T_RES];
        else if (ind == 5)
            return t.trea[T_ACC];
        else if (ind == 6)
            return t.fruit[T_RED];
        else if (ind == 7)
            return t.fruit[T_FLOAT];
        else if (ind == 8)
            return t.fruit[T_BLACK];
        else if (ind == 9)
            return t.fruit[T_ANGEL];
        else if (ind == 10)
            return t.fruit[T_METAL];
        else if (ind == 11)
            return t.fruit[T_ZOMBIE];
        else if (ind == 12)
            return t.fruit[T_ALIEN];
        else if (ind == 13)
            return t.alien;
        else if (ind == 14)
            return t.star;
        else if (ind == 15)
            return t.gods[0];
        else if (ind == 16)
            return t.gods[1];
        else if (ind == 17)
            return t.gods[2];
        else if (ind == 18)
            return t.tech[LV_BASE];
        else if (ind == 19)
            return t.tech[LV_WORK];
        else if (ind == 20)
            return t.tech[LV_WALT];
        else if (ind == 21)
            return t.tech[LV_RECH];
        else if (ind == 22)
            return t.tech[LV_CATK];
        else if (ind == 23)
            return t.tech[LV_CRG];
        else if (ind == 24)
            return t.trea[T_WORK];
        else if (ind == 25)
            return t.trea[T_WALT];
        else if (ind == 26)
            return t.trea[T_RECH];
        else if (ind == 27)
            return t.trea[T_CATK];
        else if (ind == 28)
            return t.trea[T_BASE];
        else if (ind == 29)
            return t.bslv[BASE_H];
        else if (ind == 30)
            return t.bslv[BASE_SLOW];
        else if (ind == 31)
            return t.bslv[BASE_WALL];
        else if (ind == 32)
            return t.bslv[BASE_STOP];
        else if (ind == 33)
            return t.bslv[BASE_WATER];
        else if (ind == 34)
            return t.bslv[BASE_GROUND];
        else if (ind == 35)
            return t.bslv[BASE_BARRIER];
        else if (ind == 36)
            return t.bslv[BASE_CURSE];
        return -1;
    }

    public static boolean isER(Enemy e, int t) {
        if (t == 0)
            return e.inDic;
        if (t == 1)
            return e.de.getStar() == 1;
        List<MapColc> lis = e.findMap();
        boolean colab = false;
        if (lis.contains(MapColc.getMap("C")))
            if (lis.size() == 1)
                colab = true;
            else if (lis.size() == 2)
                colab = lis.contains(MapColc.getMap("R")) || lis.contains(MapColc.getMap("CH"));

        if (t == 2)
            return !colab;
        if (t == 3)
            return !colab && !e.inDic;
        if (t == 4)
            return colab;
        if (t == 5)
            return e.pac != Pack.def;
        return false;
    }

    public static boolean isType(MaskUnit de, int type) {
        int[][] raw = de.rawAtkData();
        if (type == 0)
            return !de.isRange();
        else if (type == 1)
            return de.isRange();
        else if (type == 2)
            return de.isLD();
        else if (type == 3)
            return raw.length > 1;
        else if (type == 4)
            return de.isOmni();
        else if (type == 5)
            return de.getTBA() + raw[0][1] < de.getItv() / 2;
        return false;
    }

    public static boolean isType(MaskEnemy de, int type) {
        int[][] raw = de.rawAtkData();
        if (type == 0)
            return !de.isRange();
        else if (type == 1)
            return de.isRange();
        else if (type == 2)
            return de.isLD();
        else if (type == 3)
            return raw.length > 1;
        else if (type == 4)
            return de.isOmni();
        else if (type == 5)
            return de.getTBA() + raw[0][1] < de.getItv() / 2;
        return false;
    }

    public static void setComp(int ind, int v, BasisSet b) {
        for (int i = 0; i < TCOLP[ind][1]; i++)
            setValue(TIND[i + TCOLP[ind][0]], v, b);
    }

    public static void setValue(int ind, int v, BasisSet b) {
        setVal(ind, v, b.t());
        for (BasisLU bl : b.lb)
            setVal(ind, v, bl.t());
    }

    private static String combo(int t, int val) {
        int[] con = CDC[t];
        return COMN[t] + " " + CDP[0][con[0]] + CDP[1][con[1]].replaceAll("_", "" + val);

    }

    private static void setVal(int ind, int v, Treasure t) {

        if (v < 0)
            v = 0;
        if (v > TMAX[ind])
            v = TMAX[ind];

        if (ind == 0)
            t.tech[LV_RES] = v;
        else if (ind == 1)
            t.tech[LV_ACC] = v;
        else if (ind == 2)
            t.trea[T_ATK] = v;
        else if (ind == 3)
            t.trea[T_DEF] = v;
        else if (ind == 4)
            t.trea[T_RES] = v;
        else if (ind == 5)
            t.trea[T_ACC] = v;
        else if (ind == 6)
            t.fruit[T_RED] = v;
        else if (ind == 7)
            t.fruit[T_FLOAT] = v;
        else if (ind == 8)
            t.fruit[T_BLACK] = v;
        else if (ind == 9)
            t.fruit[T_ANGEL] = v;
        else if (ind == 10)
            t.fruit[T_METAL] = v;
        else if (ind == 11)
            t.fruit[T_ZOMBIE] = v;
        else if (ind == 12)
            t.fruit[T_ALIEN] = v;
        else if (ind == 13)
            t.alien = v;
        else if (ind == 14)
            t.star = v;
        else if (ind == 15)
            t.gods[0] = v;
        else if (ind == 16)
            t.gods[1] = v;
        else if (ind == 17)
            t.gods[2] = v;
        else if (ind == 18)
            t.tech[LV_BASE] = v;
        else if (ind == 19)
            t.tech[LV_WORK] = v;
        else if (ind == 20)
            t.tech[LV_WALT] = v;
        else if (ind == 21)
            t.tech[LV_RECH] = v;
        else if (ind == 22)
            t.tech[LV_CATK] = v;
        else if (ind == 23)
            t.tech[LV_CRG] = v;
        else if (ind == 24)
            t.trea[T_WORK] = v;
        else if (ind == 25)
            t.trea[T_WALT] = v;
        else if (ind == 26)
            t.trea[T_RECH] = v;
        else if (ind == 27)
            t.trea[T_CATK] = v;
        else if (ind == 28)
            t.trea[T_BASE] = v;
        else if (ind == 29)
            t.bslv[BASE_H] = v;
        else if (ind == 30)
            t.bslv[BASE_SLOW] = v;
        else if (ind == 31)
            t.bslv[BASE_WALL] = v;
        else if (ind == 32)
            t.bslv[BASE_STOP] = v;
        else if (ind == 33)
            t.bslv[BASE_WATER] = v;
        else if (ind == 34)
            t.bslv[BASE_GROUND] = v;
        else if (ind == 35)
            t.bslv[BASE_BARRIER] = v;
        else
            t.bslv[BASE_CURSE] = v;
    }

    private static int getVenomType(int type) {
        if(type == 0) {
            return 0;
        } else if(type == 1) {
            return 1;
        } else if(type == 2) {
            return 2;
        } else if(type == 3) {
            return 3;
        } else {
            return 3;
        }
    }

    public static String numberWithExtension(int n, String lang) {
        int f = n % 10;

        switch (lang) {
            case EN:
                switch (f) {
                    case 1:
                        return n +"st";
                    case 2:
                        return n + "nd";
                    case 3:
                        return n + "rd";
                    default:
                        return n + "th";
                }
            case RU:
                if (f == 3) {
                    return n + "ья";
                }
                return n + "ая";
            case FR:
                if (f == 1) {
                    return n + "ière";
                }

                return n + "ième";
            default:
                return "" + n;
        }
    }

    private static String getNumberAttack(String pre, String lang) {
        switch (lang) {
            case EN:
                return pre + " Attack";
            case RU:
                return pre + " Аттака";
            case FR:
                return pre + " Attaque";
            default:
                return pre;
        }
    }

    private static String getSummonTypeText(int type, boolean unit) {
        if(type == 0) {
            return TEXT[33];
        } else if(type == 1) {
            return TEXT[34];
        } else if(type == 2) {
            return TEXT[35];
        } else if(type == 3) {
            return TEXT[36];
        } else if (type >= 4 && type <= 7){
            return TEXT[37];
        } else if(type >= 8 && type <= 15) {
            if(unit) {
                return TEXT[39];
            } else {
                return TEXT[38];
            }
        } else if(type >= 16 && type <= 31) {
            return TEXT[40];
        } else if(type >= 32 && type <= 63) {
            return TEXT[41];
        } else if(type >= 64 && type <= 127) {
            return TEXT[42];
        } else {
            return TEXT[43];
        }
    }

    private static int getSummonType(int type) {
        if(type <= 64) {
            return 0;
        } else {
            return 1;
        }
    }

    private static int getSpeedType(int type) {
        if(type == 0) {
            return 0;
        } else if(type == 1) {
            return 1;
        } else {
            return 2;
        }
    }

    private static String getUnit(int id) {
        if(id < 1000) {
            if(id >= StaticStore.unitnumber) {
                return Data.hex(0)+" - "+Data.trio(id);
            }

            String name = MultiLangCont.FNAME.getCont(Pack.def.us.ulist.getList().get(id).forms[0]);

            if(name != null) {
                return name;
            } else {
                return Data.hex(0)+" - "+Data.trio(id);
            }
        } else {
            int uid = StaticStore.getID(id);

            int pid = Integer.parseInt(Integer.toString(id).replace(Data.trio(uid),""));

            Pack p = Pack.map.get(pid);

            if(p == null) {
                return Data.hex(pid)+" - "+Data.trio(id);
            } else {
                if(uid >= p.us.ulist.size()) {
                    return Data.hex(pid)+" - "+Data.trio(id);
                }

                Unit u = p.us.ulist.get(uid);

                if(u == null) {
                    return Data.hex(pid)+" - "+Data.trio(id);
                } else {
                    String name = MultiLangCont.FNAME.getCont(u.forms[0]);

                    if(name == null) {
                        name = u.forms[0].name;
                    }

                    if(name == null) {
                        return Data.hex(pid)+" - "+Data.trio(id);
                    } else {
                        return name;
                    }
                }
            }
        }
    }

    private static String getEnemy(int id) {
        if (id < 1000) {
            if (id >= StaticStore.emnumber) {
                return Data.hex(0) + " - " + Data.trio(id);
            }

            String name = MultiLangCont.ENAME.getCont(Pack.def.es.getList().get(id));

            if (name != null) {
                return name;
            } else {
                return Data.hex(0) + " - " + Data.trio(id);
            }
        } else {
            int eid = StaticStore.getID(id);

            int pid = StaticStore.getPID(id);

            Pack p = Pack.map.get(pid);

            if (p == null) {
                return Data.hex(pid) + " - " + Data.trio(eid);
            } else {
                if (eid >= p.us.ulist.size()) {
                    return Data.hex(pid) + " - " + Data.trio(eid);
                }

                Enemy e = p.es.get(eid);

                if (e == null) {
                    return Data.hex(pid) + " - " + Data.trio(eid);
                } else {
                    String name = MultiLangCont.ENAME.getCont(e);

                    if (name == null) {
                        name = e.name;
                    }

                    if (name == null) {
                        return Data.hex(pid) + " - " + Data.trio(eid);
                    } else {
                        return name;
                    }
                }
            }
        }
    }

    private static String getOnceTwice(int n, String lang, Context c) {
        switch (lang) {
            case EN:
                switch (n) {
                    case 1:
                        return " Once";
                    case 2:
                        return " Twice";
                    case 3:
                        return " " + n + " Times";
                    case -1:
                        return " " + c.getString(R.string.infinity)+ " Times";
                    default:
                        return " " + n;
                }
            case FR:
                if(n == -1) {
                    return " Temps Infini";
                } else {
                    return " " + n + " Fois";
                }
            case RU:
                if(n % 10 == 1) {
                    return " "+n + " раза";
                } else if(n % 10 == 2 || n % 10 == 3 || n % 10 == 4) {
                    if(n/10 == 1) {
                        return " " + n + " раз";
                    } else {
                        return " "+ n + " раза";
                    }
                } else if(n == -1) {
                    return " " + c.getString(R.string.infinity) + " раз";
                } else {
                    return " "+ n + " раза";
                }
            default:
                return TEXT[7].replace("_", "" + n);
        }
    }
}