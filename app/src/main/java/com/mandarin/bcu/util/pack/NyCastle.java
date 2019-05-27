package com.mandarin.bcu.util.pack;

import com.mandarin.bcu.util.anim.AnimI;
import com.mandarin.bcu.util.anim.EAnimD;
import com.mandarin.bcu.util.anim.ImgCut;
import com.mandarin.bcu.util.anim.MaAnim;
import com.mandarin.bcu.util.anim.MaModel;
import com.mandarin.bcu.util.system.VImg;
import com.mandarin.bcu.util.system.fake.FakeImage;

public class NyCastle extends AnimI {

	public static final int TOT = 7;

	public static final VImg[][] main = new VImg[3][TOT];
	public static final NyCastle[] atks = new NyCastle[TOT];

	public static void read() {
		String pre = "./org/castle/00";
		String mid = "/nyankoCastle_00";
		int[] type = new int[] { 0, 2, 3 };
		for (int t = 0; t < 3; t++)
			for (int i = 0; i < TOT; i++) {
				String str = pre + type[t] + mid + type[t] + "_0" + i;
				main[t][i] = new VImg(str + ".png");
			}
		for (int i = 0; i < TOT; i++) {
			String str = pre + 1 + mid + 1 + "_0";
			atks[i] = new NyCastle(str, i);
		}
	}

	private final int id;
	private final VImg sprite;
	private final ImgCut ic;
	private final MaModel model, atkm, extm;
	private final MaAnim manim, atka, exta;
	private FakeImage[] parts;

	private NyCastle(String str, int t) {
		anim = this;
		id = t;
		sprite = new VImg(str + t + "_00.png");
		ic = ImgCut.newIns(str + t + "_00.imgcut");
		model = MaModel.newIns(str + t + "_01.mamodel");
		manim = MaAnim.newIns(str + t + "_01.maanim");
		if (t != 1 && t != 2) {
			atkm = MaModel.newIns(str + t + "_00.mamodel");
			atka = MaAnim.newIns(str + t + "_00.maanim");
		} else {
			atkm = null;
			atka = null;
		}
		if (t == 6) {
			extm = MaModel.newIns(str + t + "_02.mamodel");
			exta = MaAnim.newIns(str + t + "_02.maanim");
		} else {
			extm = null;
			exta = null;
		}
	}

	@Override
	public void check() {
		if (parts == null)
			load();
	}

	@Override
	public EAnimD getEAnim(int t) {
		check();
		if (t == 0)
			return new EAnimD(this, model, manim);
		if (t == 1)
			return new EAnimD(this, atkm, atka);
		if (t == 2)
			return new EAnimD(this, extm, exta);
		return null;
	}

	@Override
	public void load() {
		parts = ic.cut(sprite.getImg());
	}

	@Override
	public String[] names() {
		if (atkm == null)
			return new String[] { "castle" };
		if (extm == null)
			return new String[] { "castle", "atk" };
		return new String[] { "castle", "atk", "ext" };
	}

	@Override
	public FakeImage parts(int i) {
		return parts[i];
	}

	@Override
	public String toString() {
		return "castle " + id;
	}

}
