package com.mandarin.bcu.util.entity;

import com.mandarin.bcu.util.anim.EAnimU;
import com.mandarin.bcu.util.basis.StageBasis;
import com.mandarin.bcu.util.entity.attack.AtkModelUnit;
import com.mandarin.bcu.util.entity.attack.AttackAb;
import com.mandarin.bcu.util.entity.data.MaskEnemy;

public class EEnemy extends Entity {

	public final int mark;
	public final double mult;

	public EEnemy(StageBasis b, MaskEnemy de, EAnimU ea, double d, int d0, int d1, int m) {
		super(b, de, ea, d, m == -1 ? -1 : d);
		mult = d;
		mark = m;
		isBase = mark == -1;
		layer = d0 + (int) (b.r.nextDouble() * (d1 - d0 + 1));
		type = de.getType();
	}

	@Override
	public int getAtk() {
		int atk = aam.getAtk();
		if (status[P_WEAK][1] != 0)
			atk = atk * status[P_WEAK][1] / 100;
		if (status[P_STRONG][0] != 0)
			atk += atk * status[P_STRONG][0] / 100;
		return atk;
	}

	@Override
	public void kill() {
		super.kill();
		if (!basis.st.trail) {
			double mul = basis.b.t().getDropMulti();
			if (tempearn)
				mul *= 2;
			basis.mon += mul * ((MaskEnemy) data).getDrop();
		}
	}

	@Override
	protected int getDamage(AttackAb atk, int ans) {
		if (atk.model instanceof AtkModelUnit) {
			int overlap = type & atk.type;
			if (overlap != 0 && (atk.abi & AB_GOOD) != 0)
				ans *= basis.b.t().getGOODATK(overlap);
			if (overlap != 0 && (atk.abi & AB_MASSIVE) != 0)
				ans *= basis.b.t().getMASSIVEATK(overlap);
			if (overlap != 0 && (atk.abi & AB_MASSIVES) != 0)
				ans *= basis.b.t().getMASSIVESATK(overlap);
		}
		if (isBase && (atk.abi & AB_BASE) > 0)
			ans *= 4;
		if ((type & TB_WITCH) > 0 && (atk.abi & AB_WKILL) > 0)
			ans *= basis.b.t().getWKAtk();
		if ((type & TB_EVA) > 0 && (atk.abi & AB_EKILL) > 0)
			ans *= basis.b.t().getEKAtk();
		if (atk.canon == 5)
			if ((touchable() & TCH_UG) > 0)
				ans = (int) (maxH * basis.b.t().getCanonMulti(-5) / 1000);
			else
				ans = (int) (maxH * basis.b.t().getCanonMulti(5) / 1000);
		if ((data.getType() & TB_METAL) != 0)
			if (atk.getProc(P_CRIT)[0] > 0)
				ans *= 0.01 * atk.getProc(P_CRIT)[0];
			else if (atk.getProc(P_CRIT)[0] < 0)
				ans = (int) Math.ceil(health * atk.getProc(P_CRIT)[0] / -100.0);
			else
				ans = ans > 0 ? 1 : 0;
		else if (atk.getProc(P_CRIT)[0] > 0)
			ans *= 0.01 * atk.getProc(P_CRIT)[0];
		else if (atk.getProc(P_CRIT)[0] < 0)
			ans = (int) Math.ceil(maxH * 0.0001);
		return ans;
	}

	@Override
	protected double getFruit() {
		return basis.b.t().getFruit(type);
	}

	@Override
	protected double getLim() {
		double ans = 0;
		if (mark == 1)
			ans = pos - 800 - data.getWidth();
		else
			ans = pos - data.getWidth();
		return ans < 0 ? 0 : ans;
	}

	@Override
	protected boolean receive(int t) {
		return (type & t) > 0;
	}

}
