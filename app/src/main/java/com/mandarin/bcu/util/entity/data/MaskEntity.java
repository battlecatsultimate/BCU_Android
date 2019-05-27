package com.mandarin.bcu.util.entity.data;

import com.mandarin.bcu.util.Animable;
import com.mandarin.bcu.util.Data;
import com.mandarin.bcu.util.BattleStatic;
import com.mandarin.bcu.util.anim.AnimU;

public interface MaskEntity extends BattleStatic {

	public int allAtk();

	public int getAbi();

	public int[] getAllProc(int ind);

	/** get the attack animation length */
	public default int getAnimLen() {
		return getPack().anim.getAtkLen();
	}

	public int getAtkCount();

	public int getAtkLoop();

	public MaskAtk getAtkModel(int ind);

	public int getDeathAnim();

	public int getHb();

	public int getHp();

	/** get the attack period */
	public int getItv();

	/** get the Enemy/Form this data represents */
	public Animable<AnimU> getPack();

	public int getPost();

	public int[] getProc(int ind);

	public int getRange();

	public MaskAtk getRepAtk();

	public default AtkDataModel getResurrection() {
		return null;
	}

	public default AtkDataModel getRevenge() {
		return null;
	}

	public int getShield();

	public int getSpeed();

	/** get waiting time */
	public int getTBA();

	public default int getTouch() {
		return Data.TCH_N;
	}

	public int getType();

	public int getWidth();

	public boolean isLD();

	public boolean isOmni();

	public boolean isRange();

	public int[][] rawAtkData();

	public int touchBase();

}
