package com.mandarin.bcu.util.entity;

import com.mandarin.bcu.util.BattleObj;
import com.mandarin.bcu.util.Data;
import com.mandarin.bcu.util.ImgCore;
import com.mandarin.bcu.util.anim.AnimD;
import com.mandarin.bcu.util.anim.EAnimD;
import com.mandarin.bcu.util.anim.EAnimU;
import com.mandarin.bcu.util.entity.data.AtkDataModel;
import com.mandarin.bcu.util.pack.EffAnim;
import com.mandarin.bcu.util.pack.Soul;
import com.mandarin.bcu.util.pack.SoulStore;
import com.mandarin.bcu.util.system.P;
import com.mandarin.bcu.util.system.fake.FakeGraphics;
import com.mandarin.bcu.util.system.fake.FakeTransform;

public class AnimManager extends BattleObj {

	private final Entity e;
	private final int[][] status;

	/**
	 * dead FSM time <br>
	 * -1 means not dead<br>
	 * positive value means time remain for death anim to play
	 */
	public int dead = -1;

	/** KB anim, null means not being KBed, can have various value during battle */
	protected EAnimD back;

	/** entity anim */
	private final EAnimU anim;

	/** corpse anim */
	protected EAnimD corpse;

	/** soul anim, null means not dead yet */
	private EAnimD soul;

	/** responsive effect FSM time */
	private int efft;

	/** responsive effect FSM type */
	private int eftp;

	/**
	 * on-entity effect icons<br>
	 * index defined by Data.A_()
	 */
	private final EAnimD[] effs = new EAnimD[A_TOT];

	protected AnimManager(Entity ent, EAnimU ea) {
		e = ent;
		anim = ea;
		status = e.status;
	}

	/** draw this entity */
	public void draw(FakeGraphics gra, P p, double siz) {
		if (dead > 0) {
			soul.draw(gra, p, siz);
			return;
		}
		FakeTransform at = gra.getTransform();
		if (corpse != null)
			corpse.draw(gra, p, siz);
		if (corpse == null || status[P_REVIVE][1] < Data.REVIVE_SHOW_TIME) {
			if (corpse != null) {
				gra.setTransform(at);
				anim.changeAnim(0);
			}
		} else
			return;

		anim.paraTo(back);
		if (e.kbTime == 0 || e.kb.kbType != INT_WARP)
			anim.draw(gra, p, siz);
		anim.paraTo(null);
		gra.setTransform(at);
		if (ImgCore.ref)
			e.drawAxis(gra, p, siz);
	}

	/** draw the effect icons */
	public void drawEff(FakeGraphics g, P p, double siz) {
		if (dead != -1)
			return;
		FakeTransform at = g.getTransform();
		int EWID = 48;
		double x = p.x;
		if (effs[eftp] != null) {
			effs[eftp].draw(g, p, siz);
		}
		for (EAnimD eae : effs) {
			if (eae == null)
				continue;
			g.setTransform(at);
			eae.draw(g, new P(x, p.y), siz);
			x -= EWID * e.dire * siz;
		}
	}

	/** get a effect icon */
	public void getEff(int t) {
		int dire = e.dire;
		if (t == INV) {
			effs[eftp] = null;
			eftp = A_EFF_INV;
			effs[eftp] = EffAnim.effas[eftp].getEAnim(0);
			efft = EffAnim.effas[eftp].len(0);
		}
		if (t == P_WAVE) {
			int id = dire == -1 ? A_WAVE_INVALID : A_E_WAVE_INVALID;
			effs[id] = EffAnim.effas[id].getEAnim(0);
			status[P_WAVE][0] = EffAnim.effas[id].len(0);
		}
		if (t == STPWAVE) {
			effs[eftp] = null;
			eftp = dire == -1 ? A_WAVE_STOP : A_E_WAVE_STOP;
			effs[eftp] = EffAnim.effas[eftp].getEAnim(0);
			efft = EffAnim.effas[eftp].len(0);
		}
		if (t == INVWARP) {
			effs[eftp] = null;
			eftp = dire == -1 ? A_FARATTACK : A_E_FARATTACK;
			effs[eftp] = EffAnim.effas[eftp].getEAnim(0);
			efft = EffAnim.effas[eftp].len(0);
		}
		if (t == P_STOP) {
			int id = dire == -1 ? A_STOP : A_E_STOP;
			effs[id] = EffAnim.effas[id].getEAnim(0);
		}
		if (t == P_SLOW) {
			int id = dire == -1 ? A_SLOW : A_E_SLOW;
			effs[id] = EffAnim.effas[id].getEAnim(0);
		}
		if (t == P_WEAK) {
			int id = dire == -1 ? A_DOWN : A_E_DOWN;
			effs[id] = EffAnim.effas[id].getEAnim(0);
		}
		if (t == P_CURSE) {
			int id = A_CURSE;
			effs[id] = EffAnim.effas[id].getEAnim(0);
		}
		if (t == P_POISON) {
			int mask = status[P_POISON][0];
			for (int i = 0; i < A_POIS.length; i++)
				if ((mask & (1 << i)) > 0) {
					int id = A_POIS[i];
					effs[id] = EffAnim.effas[id].getEAnim(0);
				}

		}
		if (t == P_SEAL) {
			int id = A_SEAL;
			effs[id] = EffAnim.effas[id].getEAnim(0);
		}
		if (t == P_STRONG) {
			int id = dire == -1 ? A_UP : A_E_UP;
			effs[id] = EffAnim.effas[id].getEAnim(0);
		}
		if (t == P_LETHAL) {
			int id = dire == -1 ? A_SHIELD : A_E_SHIELD;
			AnimD ea = EffAnim.effas[id];
			status[P_LETHAL][1] = ea.len(0);
			effs[id] = ea.getEAnim(0);
		}
		if (t == P_WARP) {
			AnimD ea = EffAnim.effas[A_W];
			int pa = status[P_WARP][2];
			e.basis.lea.add(new WaprCont(e.pos, pa, e.layer, anim));
			status[P_WARP][pa] = ea.len(pa);

		}

		if (t == BREAK_ABI) {
			int id = dire == -1 ? A_U_E_B : A_E_B;
			effs[id] = EffAnim.effas[id].getEAnim(0);
			status[P_BREAK][0] = effs[id].len();
		}
		if (t == BREAK_ATK) {
			int id = dire == -1 ? A_U_E_B : A_E_B;
			effs[id] = EffAnim.effas[id].getEAnim(1);
			status[P_BREAK][0] = effs[id].len();
		}
		if (t == BREAK_NON) {
			int id = dire == -1 ? A_U_B : A_B;
			effs[id] = EffAnim.effas[id].getEAnim(4);
			status[P_BREAK][0] = effs[id].len();
		}
	}

	/**
	 * process kb animation <br>
	 * called when kb is applied
	 */
	protected void kbAnim() {
		int t = e.kb.kbType;
		if (t != INT_SW && t != INT_WARP)
			setAnim(3);
		else {
			setAnim(0);
			anim.update(false);
		}
		if (t == INT_WARP) {
			e.kbTime = status[P_WARP][0];
			getEff(P_WARP);
			status[P_WARP][2] = 1;
		}
		if (t == INT_KB)
			e.kbTime = status[P_KB][0];
		if (t == INT_HB)
			back = EffAnim.effas[A_KB].getEAnim(0);
		if (t == INT_SW)
			back = EffAnim.effas[A_KB].getEAnim(1);
		if (t == INT_ASS)
			back = EffAnim.effas[A_KB].getEAnim(2);

		// Z-kill icon
		if (e.health <= 0 && e.tempZK && status[P_REVIVE][0] > 0) {
			EAnimD eae = EffAnim.effas[A_Z_STRONG].getEAnim(0);
			e.basis.lea.add(new EAnimCont(e.pos, e.layer, eae));
		}
	}

	/** set kill anim */
	protected void kill() {
		Soul s = SoulStore.getSoul(e.data.getDeathAnim());
		dead = s == null ? 0 : (soul = s.getEAnim(0)).len();
	}

	protected int setAnim(int t) {
		if (anim.type != t)
			anim.changeAnim(t);
		return anim.len();
	}

	protected void update() {
		checkEff();

		for (int i = 0; i < effs.length; i++)
			if (effs[i] != null)
				effs[i].update(false);

		if (status[P_STOP][0] == 0 && (e.kbTime == 0 || e.kb.kbType != INT_SW))
			anim.update(false);
		if (back != null)
			back.update(false);
		if (dead > 0) {
			soul.update(false);
			dead--;
		}
		if (e.data.getResurrection() != null && dead >= 0) {
			AtkDataModel adm = e.data.getResurrection();
			if (soul == null || adm.pre == soul.len() - dead)
				e.basis.getAttack(e.aam.getAttack(e.data.getAtkCount() + 1));
		}
	}

	/** update effect icons animation */
	private void checkEff() {
		int dire = e.dire;
		if (efft == 0)
			effs[eftp] = null;
		if (status[P_STOP][0] == 0) {
			int id = dire == -1 ? A_STOP : A_E_STOP;
			effs[id] = null;
		}
		if (status[P_SLOW][0] == 0) {
			int id = dire == -1 ? A_SLOW : A_E_SLOW;
			effs[id] = null;
		}
		if (status[P_WEAK][0] == 0) {
			int id = dire == -1 ? A_DOWN : A_E_DOWN;
			effs[id] = null;
		}
		if (status[P_CURSE][0] == 0) {
			int id = A_CURSE;
			effs[id] = null;
		}
		if (status[P_POISON][0] == 0) {
			int id = A_POI0;
			effs[id] = null;
		}
		if (status[P_SEAL][0] == 0) {
			int id = A_SEAL;
			effs[id] = null;
		}
		if (status[P_LETHAL][1] == 0) {
			int id = dire == -1 ? A_SHIELD : A_E_SHIELD;
			effs[id] = null;
		} else
			status[P_LETHAL][1]--;
		if (status[P_WAVE][0] == 0) {
			int id = dire == -1 ? A_WAVE_INVALID : A_E_WAVE_INVALID;
			effs[id] = null;
		} else
			status[P_WAVE][0]--;
		if (status[P_STRONG][0] == 0) {
			int id = dire == -1 ? A_UP : A_E_UP;
			effs[id] = null;
		}
		if (status[P_BREAK][0] == 0) {
			int id = dire == -1 ? A_U_B : A_B;
			effs[id] = null;
		} else
			status[P_BREAK][0]--;
		efft--;

	}

}
