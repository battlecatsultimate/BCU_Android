package com.mandarin.bcu.util.entity.data;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.util.Data;
import com.mandarin.bcu.util.system.BasedCopable;

public class AtkDataModel extends Data implements MaskAtk, BasedCopable<AtkDataModel, CustomEntity> {

	public final CustomEntity ce;
	public String str = "";
	public int atk, pre = 1, ld0, ld1, targ = TCH_N, count = -1;
	public boolean rev, range = true;
	public int[][] proc = new int[PROC_TOT][PROC_WIDTH];

	public AtkDataModel(CustomEntity ent) {
		ce = ent;
		str = ce.getAvailable(str);
	}

	protected AtkDataModel(CustomEntity ene, AtkDataModel adm) {
		ce = ene;
		str = ce.getAvailable(adm.str);
		atk = adm.atk;
		pre = adm.pre;
		ld0 = adm.ld0;
		ld1 = adm.ld1;
		range = adm.range;
		rev = adm.rev;
		count = adm.count;
		targ = adm.targ;
		for (int i = 0; i < PROC_TOT; i++)
			proc[i] = adm.proc[i].clone();
	}

	protected AtkDataModel(CustomEntity ent, InStream is) {
		ce = ent;
		zread("0.3.7", is);
	}

	@Deprecated
	protected AtkDataModel(CustomEntity ent, InStream is, String ver) {
		ce = ent;
		zread(ver, is);
	}

	@Override
	public AtkDataModel clone() {
		return new AtkDataModel(ce, this);
	}

	@Override
	public AtkDataModel copy(CustomEntity nce) {
		return new AtkDataModel(nce, this);
	}

	@Override
	public int getDire() {
		return rev ? -1 : 1;
	}

	@Override
	public int getLongPoint() {
		return isLD() ? ld1 : ce.range;
	}

	@Override
	public int[] getProc(int ind) {
		if (ce.rep != this && ce.common)
			return ce.rep.getProc(ind);
		return proc[ind];
	}

	@Override
	public int getShortPoint() {
		return isLD() ? ld0 : -ce.width;
	}

	@Override
	public int getTarget() {
		return targ;
	}

	@Override
	public boolean isRange() {
		return range;
	}

	@Override
	public String toString() {
		return str;
	}

	protected int[] getAtkData() {
		return new int[] { atk, pre, 1 };
	}

	protected boolean isLD() {
		return ld0 != 0 || ld1 != 0;
	}

	protected boolean isOmni() {
		return ld0 * ld1 < 0;
	}

	protected void write(OutStream os) {
		os.writeString("0.4.2");
		os.writeString(str);
		os.writeInt(atk);
		os.writeInt(pre);
		os.writeInt(ld0);
		os.writeInt(ld1);
		os.writeInt(targ);
		os.writeInt(count);
		os.writeInt((rev ? 1 : 0) + (range ? 2 : 0));
		os.writeIntBB(proc);
	}

	protected void zread$000301(InStream is) {
		str = is.nextString();
		atk = is.nextInt();
		pre = is.nextInt();
		ld0 = is.nextShort();
		ld1 = is.nextShort();
		int[][] temp = is.nextIntsBB();
		proc = new int[PROC_TOT][PROC_WIDTH];
		for (int i = 0; i < temp.length; i++)
			for (int j = 0; j < temp[i].length; j++)
				proc[i][j] = temp[i][j];
	}

	protected void zread$000400(InStream is) {
		str = is.nextString();
		atk = is.nextInt();
		pre = is.nextInt();
		ld0 = is.nextInt();
		ld1 = is.nextInt();
		targ = is.nextInt();
		rev = is.nextInt() == 1;
		int[][] temp = is.nextIntsBB();
		proc = new int[PROC_TOT][PROC_WIDTH];
		for (int i = 0; i < temp.length; i++)
			for (int j = 0; j < temp[i].length; j++)
				proc[i][j] = temp[i][j];
	}

	private void zread(String ver, InStream is) {
		int val = getVer(ver);
		if (val >= 307)
			val = getVer(is.nextString());
		if (val >= 402)
			zread$000402(is);
		else if (val >= 401)
			zread$000401(is);
		else if (val >= 400)
			zread$000400(is);
		else if (val >= 301)
			zread$000301(is);
	}

	private void zread$000401(InStream is) {
		str = is.nextString();
		atk = is.nextInt();
		pre = is.nextInt();
		ld0 = is.nextInt();
		ld1 = is.nextInt();
		targ = is.nextInt();
		int bm = is.nextInt();
		rev = (bm & 1) > 0;
		range = (bm & 2) > 0;
		int[][] temp = is.nextIntsBB();
		proc = new int[PROC_TOT][PROC_WIDTH];
		for (int i = 0; i < temp.length; i++)
			for (int j = 0; j < temp[i].length; j++)
				proc[i][j] = temp[i][j];
	}

	private void zread$000402(InStream is) {
		str = is.nextString();
		atk = is.nextInt();
		pre = is.nextInt();
		ld0 = is.nextInt();
		ld1 = is.nextInt();
		targ = is.nextInt();
		count = is.nextInt();
		int bm = is.nextInt();
		rev = (bm & 1) > 0;
		range = (bm & 2) > 0;
		int[][] temp = is.nextIntsBB();
		proc = new int[PROC_TOT][PROC_WIDTH];
		for (int i = 0; i < temp.length; i++)
			for (int j = 0; j < temp[i].length; j++)
				proc[i][j] = temp[i][j];
	}

}
