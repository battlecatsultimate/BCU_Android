package com.mandarin.bcu.util.unit;

public class UnitInfo {

    public int[][] evo;
    public int[] price = new int[10];
    public String[][] explanation;
    public int type;

    protected void fillBuy(String[] strs) {
        for (int i = 0; i < 10; i++)
            price[i] = Integer.parseInt(strs[2 + i]);
        type = Integer.parseInt(strs[12]);
        int et = Integer.parseInt(strs[23]);
        if (et >= 15000 && et < 17000) {
            evo = new int[6][2];
            evo[0][0] = Integer.parseInt(strs[27]);
            for (int i = 0; i < 5; i++) {
                evo[i][0] = Integer.parseInt(strs[28 + i * 2]);
                evo[i][1] = Integer.parseInt(strs[29 + i * 2]);
            }
        }
    }

}
