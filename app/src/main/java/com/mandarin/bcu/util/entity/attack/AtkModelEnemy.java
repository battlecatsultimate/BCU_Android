package com.mandarin.bcu.util.entity.attack;

import com.mandarin.bcu.util.entity.EEnemy;
import com.mandarin.bcu.util.entity.EntCont;
import com.mandarin.bcu.util.entity.Entity;
import com.mandarin.bcu.util.unit.AbEnemy;
import com.mandarin.bcu.util.unit.EnemyStore;

public class AtkModelEnemy extends AtkModelEntity {

	protected AtkModelEnemy(EEnemy ent, double d0) {
		super(ent, d0);
	}

	@Override
	public AttackAb getAttack(int ind) {
		int[][] proc = new int[PROC_TOT][PROC_WIDTH];
		if (abis[ind] == 1) {
			extraAtk(ind);
			setProc(ind, proc);
		}
		int atk = atks[ind];
		if (e.status[P_WEAK][1] != 0)
			atk = atk * e.status[P_WEAK][1] / 100;
		if (e.status[P_STRONG][0] != 0)
			atk += atk * e.status[P_STRONG][0] / 100;
		double[] ints = inRange(ind);
		return new AttackSimple(this, atk, e.type, getAbi(), proc, ints[0], ints[1], e.data.getAtkModel(ind));
	}

	@Override
	public void summon(int[] proc, Entity ent, Object acs) {
		AbEnemy ene = EnemyStore.getAbEnemy(proc[1], false);
		int conf = proc[4];
		int time = proc[5];
		int allow = b.st.data.allow(b, ene);
		// conf 4
		if (ene != null && (allow >= 0 || (conf & 4) > 0)) {
			double ep = ent.pos + getDire() * proc[2];
			double mult = proc[3] * 0.01;
			// conf 8
			if ((conf & 8) == 0)
				mult *= ((EEnemy) e).mult;
			int l0 = 0, l1 = 9;
			// conf 32
			if ((conf & 32) == 0)
				l0 = l1 = e.layer;
			EEnemy ee = ene.getEntity(b, acs, mult, 0, l0, l1);
			ee.group = allow;
			if (ep < ee.data.getWidth())
				ep = ee.data.getWidth();
			if (ep > b.st.len - 800)
				ep = b.st.len - 800;
			ee.added(1, (int) ep);
			b.tempe.add(new EntCont(ee, time));
			// conf 16
			if ((conf & 16) != 0)
				ee.health = e.health;
			ee.setSummon(conf & 3);
		}

	}

	@Override
	protected int getProc(int ind, int type, int ety) {
		if (e.status[P_CURSE][0] > 0
				&& (type == P_KB || type == P_STOP || type == P_SLOW || type == P_WEAK || type == P_WARP
						|| type == P_CURSE || type == P_SNIPER || type == P_SEAL || type == P_POISON || type == P_BOSS))
			return 0;
		return super.getProc(ind, type, ety);
	}

}
