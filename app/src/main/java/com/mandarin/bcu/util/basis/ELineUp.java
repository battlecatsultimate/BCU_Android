package com.mandarin.bcu.util.basis;

import com.mandarin.bcu.util.BattleObj;
import com.mandarin.bcu.util.stage.Limit;

public class ELineUp extends BattleObj {

	private final StageBasis b;
	public final int[][] price, cool, maxC;

	protected ELineUp(LineUp lu, StageBasis sb) {
		b = sb;
		price = new int[2][5];
		cool = new int[2][5];
		maxC = new int[2][5];
		Limit lim = b.est.lim;
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 5; j++) {
				if (lu.fs[i][j] == null) {
					price[i][j] = -1;
					continue;
				}
				price[i][j] = lu.efs[i][j].getPrice(b.st.map.price);
				maxC[i][j] = sb.b.t().getFinRes(lu.fs[i][j].du.getRespawn());
				if (lim == null)
					continue;
				int overl = lim.rare >> lu.fs[i][j].unit.rarity;
				if (lim.rare != 0 && (overl & 1) == 0)
					price[i][j] = -1;
				if (lim.line == 1 && i == 1)
					price[i][j] = -1;
				if (lim.min > 0 && price[i][j] < lim.min)
					price[i][j] = -1;
				if (lim.max > 0 && price[i][j] > lim.max)
					price[i][j] = -1;
				if (lim.group != null && !lim.group.allow(lu.fs[i][j].unit))
					price[i][j] = -1;

			}
	}

	/** reset cool down time of an unit */
	protected void get(int i, int j) {
		cool[i][j] = maxC[i][j];
	}

	/** count down the cool down time */
	protected void update() {
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 5; j++)
				if (cool[i][j] > 0)
					cool[i][j]--;
	}

}
