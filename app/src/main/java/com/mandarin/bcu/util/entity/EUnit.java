package com.mandarin.bcu.util.entity;

import com.mandarin.bcu.util.anim.EAnimU;
import com.mandarin.bcu.util.basis.StageBasis;
import com.mandarin.bcu.util.entity.attack.AtkModelEnemy;
import com.mandarin.bcu.util.entity.attack.AttackAb;
import com.mandarin.bcu.util.entity.data.MaskUnit;

public class EUnit extends Entity {

	public EUnit(StageBasis b, MaskUnit de, EAnimU ea, double d0) {
		super(b, de, ea, d0 * b.b.t().getAtkMulti(), d0 * b.b.t().getDefMulti());
		layer = de.getFront() + (int) (b.r.nextDouble() * (de.getBack() - de.getFront() + 1));
		type = de.getType();
	}

	@Override
	public int getAtk() {
		int atk = aam.getAtk();
		if (status[P_WEAK][1] != 0)
			atk = atk * status[P_WEAK][1] / 100;
		if (status[P_STRONG][0] != 0)
			atk += atk * (status[P_STRONG][0] + basis.b.getInc(C_STRONG)) / 100;
		return atk;
	}

	@Override
	public void update() {
		super.update();
		type = status[P_CURSE][0] == 0 ? data.getType() : 0;
	}

	@Override
	protected int getDamage(AttackAb atk, int ans) {
		if (atk.model instanceof AtkModelEnemy) {
			int overlap = type & atk.type;
			if (overlap != 0 && (getAbi() & AB_GOOD) != 0)
				ans *= basis.b.t().getGOODDEF(overlap);
			if (overlap != 0 && (getAbi() & AB_RESIST) != 0)
				ans *= basis.b.t().getRESISTDEF(overlap);
			if (overlap != 0 && (getAbi() & AB_RESISTS) != 0)
				ans *= basis.b.t().getRESISTSDEF(overlap);
		}
		if ((atk.type & TB_WITCH) > 0 && (getAbi() & AB_WKILL) > 0)
			ans *= basis.b.t().getWKDef();
		if ((atk.type & TB_EVA) > 0 && (getAbi() & AB_EKILL) > 0)
			ans *= basis.b.t().getEKDef();
		if (isBase && (atk.abi & AB_BASE) > 0)
			ans *= 4;
		if ((getAbi() & AB_METALIC) != 0)
			if (atk.getProc(P_CRIT)[0] > 0)
				ans *= 0.01 * atk.getProc(P_CRIT)[0];
			else
				ans = 1;
		else if (atk.getProc(P_CRIT)[0] > 0)
			ans *= 0.01 * atk.getProc(P_CRIT)[0];
		return ans;
	}

	@Override
	protected double getLim() {
		return basis.st.len - pos;
	}

	@Override
	protected boolean updateMove(double maxl, double extmov) {
		if (status[P_SLOW][0] == 0)
			extmov += data.getSpeed() * basis.b.getInc(C_SPE) / 200.0;
		return super.updateMove(maxl, extmov);
	}

}
