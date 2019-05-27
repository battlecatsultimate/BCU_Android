package com.mandarin.bcu.util.entity.data;

import com.mandarin.bcu.util.Data;

public abstract class DataEntity extends Data implements MaskEntity {

	public int hp, hb, speed, range;
	public int abi, type, width;
	public int loop = -1, death, shield;

	@Override
	public int getAbi() {
		return abi;
	}

	@Override
	public int getAtkLoop() {
		return loop;
	}

	@Override
	public int getDeathAnim() {
		return death;
	}

	@Override
	public int getHb() {
		return hb;
	}

	@Override
	public int getHp() {
		return hp;
	}

	@Override
	public int getRange() {
		return range;
	}

	@Override
	public int getShield() {
		return shield;
	}

	@Override
	public int getSpeed() {
		return speed;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public int getWidth() {
		return width;
	}

}
