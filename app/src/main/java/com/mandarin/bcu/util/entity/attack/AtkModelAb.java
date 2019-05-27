package com.mandarin.bcu.util.entity.attack;

import com.mandarin.bcu.util.BattleObj;
import com.mandarin.bcu.util.basis.StageBasis;
import com.mandarin.bcu.util.entity.Entity;

public abstract class AtkModelAb extends BattleObj {

	public final StageBasis b;

	public AtkModelAb(StageBasis bas) {
		b = bas;
	}

	/** get the ability bitmask of this attack */
	public abstract int getAbi();

	/** get the direction of the entity */
	public abstract int getDire();

	/** get the position of the entity */
	public abstract double getPos();

	/** invoke when damage calculation is finished */
	public void invokeLater(AttackAb atk, Entity e) {
	}

	protected int getLayer() {
		return 10;
	}

}
