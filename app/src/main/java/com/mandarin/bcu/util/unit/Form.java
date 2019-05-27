package com.mandarin.bcu.util.unit;

import com.mandarin.bcu.util.Animable;
import com.mandarin.bcu.util.anim.AnimC;
import com.mandarin.bcu.util.anim.AnimU;
import com.mandarin.bcu.util.anim.EAnimU;
import com.mandarin.bcu.util.anim.ImgCut;
import com.mandarin.bcu.util.entity.data.CustomUnit;
import com.mandarin.bcu.util.entity.data.DataUnit;
import com.mandarin.bcu.util.entity.data.MaskUnit;
import com.mandarin.bcu.util.entity.data.PCoin;
import com.mandarin.bcu.util.system.BasedCopable;
import com.mandarin.bcu.util.system.VImg;

public class Form extends Animable<AnimU> implements BasedCopable<Form, Unit> {

	public static ImgCut unicut, udicut;

	public static String lvString(int[] lvs) {
		String str = "Lv." + lvs[0] + ", {";
		for (int i = 1; i < 5; i++)
			str += lvs[i] + ",";
		str += lvs[5] + "}";
		return str;
	}

	public final MaskUnit du;
	public final Unit unit;
	public final int uid;
	public int fid;
	public final VImg udi;// TODO unused

	public String name = "";

	public Form(Unit u, int f, String str, AnimC ac, CustomUnit cu) {
		unit = u;
		uid = u.id;
		fid = f;
		name = str;
		anim = ac;
		du = cu;
		cu.pack = this;
		udi = null;
	}

	protected Form(Unit u, int f, String str, String data) {
		unit = u;
		uid = u.id;
		fid = f;
		String nam = trio(unit.id) + "_" + SUFX[fid];
		anim = new AnimU(str, nam, "edi" + nam + ".png");
		anim.uni = new VImg(str + "uni" + nam + "00.png");
		udi = new VImg(str + "udi" + nam + ".png");
		anim.uni.setCut(unicut);
		udi.setCut(udicut);
		String[] strs = data.split("//")[0].trim().split(",");
		du = new DataUnit(this, unit, strs);
	}

	@Override
	public Form copy(Unit b) {
		CustomUnit cu = new CustomUnit();
		cu.importData(du);
		return new Form(b, fid, name, (AnimC) anim, cu);
	}

	public int getDefaultPrice(int sta) {
		PCoin pc = getPCoin();
		int price = pc == null ? du.getPrice() : pc.full.getPrice();
		return (int) (price * (1 + sta * 0.5));
	}

	@Override
	public EAnimU getEAnim(int t) {
		return anim.getEAnim(t);
	}

	public PCoin getPCoin() {
		if (du instanceof DataUnit)
			return ((DataUnit) du).pcoin;
		return null;
	}

	public String[] lvText(int[] lvs) {
		PCoin pc = getPCoin();
		if (pc == null)
			return new String[] { "Lv." + lvs[0], "" };
		else {
			String lab = "";// PCTX[pc.info[0][0]];
			String str = "Lv." + lvs[0] + ", {";
			for (int i = 1; i < 5; i++) {
				str += lvs[i] + ",";
				lab += ", ";// PCTX[pc.info[i][0]];
			}
			str += lvs[5] + "}";
			return new String[] { str, lab };
		}
	}

	public MaskUnit maxu() {
		PCoin pc = getPCoin();
		if (pc != null)
			return pc.full;
		return du;
	}

	public int[] regulateLv(int[] mod, int[] lv) {
		if (mod != null)
			for (int i = 0; i < Math.min(mod.length, 6); i++)
				lv[i] = mod[i];
		int[] maxs = new int[6];
		maxs[0] = unit.max + unit.maxp;
		PCoin pc = null;
		if (unit.forms.length >= 3)
			pc = unit.forms[2].getPCoin();
		if (pc != null)
			for (int i = 0; i < 5; i++)
				maxs[i + 1] = Math.max(1, pc.info[i][1]);
		for (int i = 0; i < 6; i++) {
			if (lv[i] < 0)
				lv[i] = 0;
			if (lv[i] > maxs[i])
				lv[i] = maxs[i];
		}
		if (lv[0] == 0)
			lv[0] = 1;
		return lv;
	}

	@Override
	public String toString() {
		String base = trio(uid) + "-" + fid + " ";
		String desp = "";
		if (desp != null && desp.length() > 0)
			return base + desp;
		if (name.length() > 0)
			return base + name;
		return base;
	}

}
