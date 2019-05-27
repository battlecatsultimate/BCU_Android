package com.mandarin.bcu.util.entity.data;

public abstract class DefaultData extends DataEntity implements MaskAtk {

	public int[][] proc;
	protected int lds, ldr;
	protected int atk, atk1, atk2, pre, pre1, pre2, abi0 = 1, abi1, abi2, tba;

	public boolean isrange;

	@Override
	public int allAtk() {
		return (int) ((atk + atk1 + atk2) * (1 + proc[P_CRIT][0] * 0.01));
	}

	@Override
	public int[] getAllProc(int ind) {
		return proc[ind];
	}

	@Override
	public int getAtkCount() {
		return atk1 == 0 ? 1 : atk2 == 0 ? 2 : 3;
	}

	@Override
	public MaskAtk getAtkModel(int ind) {
		if (ind >= getAtkCount())
			return null;
		return this;
	}

	@Override
	public int getItv() {
		int len = getAnimLen();
		int post = len - getLongPre();
		return getLongPre() + Math.max(tba * 2 - 1, post);
	}

	@Override
	public int getLongPoint() {
		return lds + ldr;
	}

	@Override
	public int getPost() {
		return getAnimLen() - getLongPre();
	}

	@Override
	public int[] getProc(int ind) {
		return proc[ind];
	}

	@Override
	public MaskAtk getRepAtk() {
		return this;
	}

	@Override
	public int getShortPoint() {
		return lds;
	}

	@Override
	public int getTBA() {
		return getItv() - getAnimLen();
	}

	@Override
	public boolean isLD() {
		return lds > 0;
	}

	@Override
	public boolean isOmni() {
		return lds > 0 && -ldr > lds;
	}

	@Override
	public boolean isRange() {
		return isrange;
	}

	@Override
	public int[][] rawAtkData() {
		int[][] data = new int[getAtkCount()][3];
		data[0][0] = atk;
		data[0][1] = pre;
		data[0][2] = abi0;
		if (atk1 == 0)
			return data;
		data[1][0] = atk1;
		data[1][1] = pre1 - pre;
		data[1][2] = abi1;
		if (atk2 == 0)
			return data;
		data[2][0] = atk2;
		data[2][1] = pre2 - pre1;
		data[2][2] = abi2;
		return data;
	}

	@Override
	public int touchBase() {
		return lds > 0 ? lds : range;
	}

	protected int getLongPre() {
		if (pre2 > 0)
			return pre2;
		if (pre1 > 0)
			return pre1;
		return pre;
	}

}
