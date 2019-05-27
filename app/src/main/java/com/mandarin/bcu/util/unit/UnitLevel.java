package com.mandarin.bcu.util.unit;

import java.util.ArrayList;
import java.util.List;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.util.pack.Pack;

public class UnitLevel {

	public static UnitLevel def;

	public final int[] lvs = new int[3];

	public final List<Unit> units = new ArrayList<>();

	public int id;

	public UnitLevel(Pack p, int ind, UnitLevel ul) {
		id = p.id * 1000 + ind;
		for (int i = 0; i < 3; i++)
			lvs[i] = ul.lvs[i];
	}

	protected UnitLevel(int[] inp) {
		int val = -1;
		for (int i = 0; i < inp.length; i++) {
			if (val != inp[i]) {
				val = inp[i];
				if (val == 10)
					lvs[0] = i;
				if (val == 5)
					lvs[1] = i;
				if (val == 0)
					lvs[2] = i;
			}
		}
		if (lvs[1] == 0)
			lvs[1] = inp.length;
		if (lvs[2] == 0)
			lvs[2] = inp.length;
	}

	protected UnitLevel(Pack p, int ind, InStream is) {
		id = p.id * 1000 + ind;
		zread(is);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof UnitLevel))
			return false;
		UnitLevel ul = (UnitLevel) o;
		if (lvs.length != ul.lvs.length)
			return false;
		for (int i = 0; i < lvs.length; i++)
			if (lvs[i] != ul.lvs[i])
				return false;
		return id / 1000 == 0 && ul.id / 1000 == 0;
	}

	public double getMult(int lv) {
		int dec = lv;
		int pre = 0, mul = 20;
		double d = 0.8;
		for (int i = 0; i < lvs.length; i++) {
			int dur = lvs[i] - pre;
			if (dec > dur * 10) {
				d += mul * dur * 0.1;
				dec -= dur * 10;
			} else {
				d += mul * dec * 0.01;
				break;
			}
			mul /= 2;
			pre = lvs[i];
		}
		return d;
	}

	@Override
	public String toString() {
		String ans = "{";
		for (int set : lvs) {
			if (ans.length() > 1)
				ans += ", ";
			ans += set;
		}
		ans += "}";
		return ans;
	}

	protected void write(OutStream os) {
		os.writeInt(1);// save for version check
		os.writeIntB(lvs);
	}

	private void zread(InStream is) {
		int ver = is.nextInt();
		if (ver == 1) {
			int[] vs = is.nextIntsB();
			lvs[0] = vs[0];
			lvs[1] = vs[1];
			lvs[2] = vs[2];
		} else {
			int[][] vs = is.nextIntsBB();
			lvs[0] = vs[1][0];
			lvs[1] = vs[2][0];
			lvs[2] = vs[3][0];
		}
	}

}
