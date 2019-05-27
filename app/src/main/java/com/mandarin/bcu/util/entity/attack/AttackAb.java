package com.mandarin.bcu.util.entity.attack;

import java.util.ArrayList;
import java.util.List;

import com.mandarin.bcu.util.BattleObj;
import com.mandarin.bcu.util.entity.AbEntity;

public abstract class AttackAb extends BattleObj {

	public final int atk, type, abi;
	public final AtkModelAb model;
	public final AttackAb origin;

	public int touch = TCH_N, dire, canon = -2, waveType = 0;

	protected final int[][] proc;
	protected final List<AbEntity> capt = new ArrayList<>();
	protected double sta, end;

	protected AttackAb(AtkModelAb ent, int ATK, int t, int eab, int[][] pro, double p0, double p1) {
		dire = ent.getDire();
		origin = this;
		model = ent;
		type = t;
		atk = ATK;
		proc = pro;
		abi = eab;
		sta = p0;
		end = p1;
	}

	protected AttackAb(AttackAb a, double mid, double wid) {
		dire = a.dire;
		origin = a.origin;
		model = a.model;
		atk = a.atk;
		abi = a.abi;
		type = a.type;
		proc = a.proc;
		touch = a.touch;
		canon = a.canon;
		sta = mid - wid / 2;
		end = mid + wid / 2;
	}

	/** capture the entities */
	public abstract void capture();

	/** apply this attack to the entities captured */
	public abstract void excuse();

	public int[] getProc(int type) {
		return proc[type];
	}

}
