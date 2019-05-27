package com.mandarin.bcu.util.basis;

import com.mandarin.bcu.util.stage.EStage;

public abstract class BattleField {

	public StageBasis sb;

	protected BattleField(EStage stage, BasisLU bas, int[] ints, long seed) {
		sb = new StageBasis(stage, bas, ints, seed);
	}

	protected BattleField(StageBasis bas) {
		sb = bas;
	}

	public void update() {
		sb.time++;
		actions();
		sb.update();
	}

	protected boolean act_can() {
		return sb.act_can();
	}

	protected void act_lock(int i, int j) {
		sb.act_lock(i, j);
	}

	protected boolean act_mon() {
		return sb.act_mon();
	}

	protected boolean act_sniper() {
		return sb.act_sniper();
	}

	protected boolean act_spawn(int i, int j, boolean boo) {
		return sb.act_spawn(i, j, boo);
	}

	protected abstract void actions();

}
