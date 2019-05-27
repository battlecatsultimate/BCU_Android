package com.mandarin.bcu.util.anim;

import com.mandarin.bcu.util.system.fake.FakeImage;

public abstract class AnimD extends AnimI {

	public ImgCut imgcut;
	public MaModel mamodel;
	public MaAnim[] anims;
	public FakeImage[] parts;

	public boolean mismatch;

	protected final String str;
	protected boolean loaded = false;

	public AnimD(String st) {
		str = st;
		anim = this;
	}

	@Override
	public void check() {
		if (!loaded)
			load();
	}

	@Override
	public EAnimD getEAnim(int t) {
		check();
		if (mamodel == null || t >= anims.length || anims[t] == null)
			return null;
		return new EAnimD(this, mamodel, anims[t]);
	}

	public abstract FakeImage getNum();

	public int len(int t) {
		check();
		return anims[t].max + 1;
	}

	@Override
	public abstract void load();

	@Override
	public abstract String[] names();

	@Override
	public FakeImage parts(int i) {
		if (i < 0 || i >= parts.length)
			return null;
		return parts[i];
	}

	public void reorderModel(int[] inds) {
		for (int[] ints : mamodel.parts)
			if (ints != null && ints[0] >= 0)
				ints[0] = inds[ints[0]];
		for (MaAnim ma : anims)
			for (Part part : ma.parts)
				part.ints[0] = inds[part.ints[0]];
	}

	public void revert() {
		mamodel.revert();
		for (MaAnim ma : anims)
			if (ma != null)
				ma.revert();
	}

	public void validate() {
		check();
		mamodel.check(this);
		for (MaAnim ma : anims) {
			for (Part p : ma.parts) {
				p.check(this);
				p.validate();
			}
			ma.validate();
		}
	}

}
