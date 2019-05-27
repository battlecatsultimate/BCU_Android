package com.mandarin.bcu.util.pack;

import com.mandarin.bcu.util.anim.AnimI;
import com.mandarin.bcu.util.anim.EAnimD;
import com.mandarin.bcu.util.anim.MaAnim;
import com.mandarin.bcu.util.anim.MaModel;
import com.mandarin.bcu.util.system.fake.FakeImage;

public class WaveAnim extends AnimI {

	private final Background bg;
	private final MaModel mamodel;
	private final MaAnim maanim;

	private FakeImage[] parts;

	public WaveAnim(Background BG, MaModel model, MaAnim anim) {
		bg = BG;
		mamodel = model;
		maanim = anim;
	}

	@Override
	public void check() {
		if (parts == null)
			load();
	}

	@Override
	public EAnimD getEAnim(int t) {
		return new EAnimD(this, mamodel, maanim);
	}

	@Override
	public void load() {
		bg.check();
		parts = bg.parts;
	}

	@Override
	public String[] names() {
		return new String[] { "wave" };
	}

	@Override
	public FakeImage parts(int i) {
		check();
		return parts[i];
	}

}
