package com.mandarin.bcu.util.entity.attack;

import com.mandarin.bcu.util.basis.BasisLU;
import com.mandarin.bcu.util.entity.EUnit;
import com.mandarin.bcu.util.entity.EntCont;
import com.mandarin.bcu.util.entity.Entity;
import com.mandarin.bcu.util.unit.EForm;
import com.mandarin.bcu.util.unit.Unit;
import com.mandarin.bcu.util.unit.UnitStore;

public class AtkModelUnit extends AtkModelEntity {

	private final BasisLU bas;

	protected AtkModelUnit(Entity ent, double d0) {
		super(ent, d0);
		bas = ent.basis.b;
	}

	@Override
	public AttackAb getAttack(int ind) {
		int[][] proc = new int[PROC_TOT][PROC_WIDTH];
		if (abis[ind] == 1) {
			setProc(ind, proc);
			proc[P_KB][0] = proc[P_KB][0] * (100 + bas.getInc(C_KB)) / 100;
			extraAtk(ind);
		}
		int atk = atks[ind];
		if (e.status[P_WEAK][1] != 0)
			atk = atk * e.status[P_WEAK][1] / 100;
		if (e.status[P_STRONG][0] != 0)
			atk += atk * (e.status[P_STRONG][0] + bas.getInc(C_STRONG)) / 100;
		double[] ints = inRange(ind);
		return new AttackSimple(this, atk, e.type, getAbi(), proc, ints[0], ints[1], e.data.getAtkModel(ind));
	}

	@Override
	public void summon(int[] proc, Entity ent, Object acs) {

		Unit u = UnitStore.get(proc[1], true);
		int conf = proc[4];
		int time = proc[5];
		// conf 4
		if (u != null && (b.entityCount(-1) < b.max_num || (conf & 4) > 0)) {
			double up = ent.pos + getDire() * proc[2];
			EForm ef = new EForm(u.forms[0], u.max);
			EUnit eu = ef.getEntity(b);
			// conf 16
			if ((conf & 16) > 0)
				eu.health = e.health;
			int l0 = 0, l1 = 9;
			// conf 32
			if ((conf & 32) == 0)
				l0 = l1 = e.layer;
			eu.layer = (int) (b.r.nextDouble() * (l1 - l0)) + l0;
			eu.added(-1, (int) up);
			b.tempe.add(new EntCont(eu, time));
			eu.setSummon(conf & 3);
		}

	}

	@Override
	protected int getProc(int ind, int type, int ety) {
		int ans = super.getProc(ind, type, ety);
		if (type == P_STOP && ety == 1)
			ans = ans * (100 + bas.getInc(C_STOP)) / 100;
		if (type == P_SLOW && ety == 1)
			ans = ans * (100 + bas.getInc(C_SLOW)) / 100;
		if (type == P_WEAK && ety == 1)
			ans = ans * (100 + bas.getInc(C_WEAK)) / 100;
		if (type == P_CRIT && ety == 0 && ans > 0)
			ans += bas.getInc(C_CRIT);
		// TODO treasure for unit Warp
		return ans;
	}

}
