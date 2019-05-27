package com.mandarin.bcu.util.entity.data;

import com.mandarin.bcu.util.basis.Basis;
import com.mandarin.bcu.util.unit.Enemy;

public class DataEnemy extends DefaultData implements MaskEnemy {

	private final Enemy enemy;

	private int earn, star;

	public DataEnemy(Enemy e) {
		enemy = e;
		proc = new int[PROC_TOT][PROC_WIDTH];
	}

	public void fillData(String[] strs) {
		int[] ints = new int[strs.length];
		for (int i = 0; i < strs.length; i++)
			ints[i] = Integer.parseInt(strs[i]);
		hp = ints[0];
		hb = ints[1];
		speed = ints[2];
		atk = ints[3];
		tba = ints[4];
		range = ints[5];
		earn = ints[6];
		if (ints[7] != 0)
			;// Printer.p("DataEnemy", 32, enemy.id + "-new 0: " + ints[7]);
		width = ints[8];
		if (ints[9] != 0)
			;// Printer.p("DataEnemy", 32, enemy.id + "-new 2: " + ints[9]);
		int t = 0;
		if (ints[10] == 1)
			t |= TB_RED;
		isrange = ints[11] == 1;
		pre = ints[12];
		if (ints[13] == 1)
			t |= TB_FLOAT;
		if (ints[14] == 1)
			t |= TB_BLACK;
		if (ints[15] == 1)
			t |= TB_METAL;
		if (ints[16] == 1)
			t |= TB_WHITE;
		if (ints[17] == 1)
			t |= TB_ANGEL;
		if (ints[18] == 1)
			t |= TB_ALIEN;
		if (ints[19] == 1)
			t |= TB_ZOMBIE;
		proc[P_KB][0] = ints[20];
		proc[P_STOP][0] = ints[21];
		proc[P_STOP][1] = ints[22];
		proc[P_SLOW][0] = ints[23];
		proc[P_SLOW][1] = ints[24];
		proc[P_CRIT][0] = ints[25];
		int a = 0;
		if (ints[26] == 1)
			a |= AB_BASE;
		proc[P_WAVE][0] = ints[27];
		proc[P_WAVE][1] = ints[28];
		proc[P_WEAK][0] = ints[29];
		proc[P_WEAK][1] = ints[30];
		proc[P_WEAK][2] = ints[31];
		proc[P_STRONG][0] = ints[32];
		proc[P_STRONG][1] = ints[33];
		proc[P_LETHAL][0] = ints[34];

		lds = ints[35];
		ldr = ints[36];
		if (ints[37] == 1)
			proc[P_IMUWAVE][0] = 100;
		if (ints[38] == 1)
			a |= AB_WAVES;
		if (ints[39] == 1)
			proc[P_IMUKB][0] = 100;
		if (ints[40] == 1)
			proc[P_IMUSTOP][0] = 100;
		if (ints[41] == 1)
			proc[P_IMUSLOW][0] = 100;
		if (ints[42] == 1)
			proc[P_IMUWEAK][0] = 100;
		proc[P_BURROW][0] = ints[43];
		proc[P_BURROW][1] = ints[44] / 4;
		proc[P_REVIVE][0] = ints[45];
		proc[P_REVIVE][1] = ints[46];
		proc[P_REVIVE][2] = ints[47];
		if (ints[48] == 1)
			t |= TB_WITCH;
		if (ints[49] == 1)
			t |= TB_INFH;
		death = ints[54];
		atk1 = ints[55];
		atk2 = ints[56];
		pre1 = ints[57];
		pre2 = ints[58];
		abi0 = ints[59];
		abi1 = ints[60];
		abi2 = ints[61];
		shield = ints[64];
		proc[P_WARP][0] = ints[65];
		proc[P_WARP][1] = ints[66];
		proc[P_WARP][2] = ints[67] / 4;
		star = ints[69];
		if (ints[71] == 1)
			t |= TB_EVA;
		if (ints[72] == 1)
			t |= TB_RELIC;
		proc[P_CURSE][0] = ints[73];
		proc[P_CURSE][1] = ints[74];
		abi = a;
		type = t;
	}

	@Override
	public double getDrop() {
		return earn;
	}

	@Override
	public Enemy getPack() {
		return enemy;
	}

	@Override
	public int getStar() {
		return star;
	}

	@Override
	public double multi(Basis b) {
		if (star > 0)
			return b.t().getStarMulti(star);
		if ((type & TB_ALIEN) > 0)
			return b.t().getAlienMulti();
		return 1;
	}

}
