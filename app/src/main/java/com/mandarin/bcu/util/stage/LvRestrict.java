package com.mandarin.bcu.util.stage;

import java.util.Map;
import java.util.TreeMap;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.util.Data;
import com.mandarin.bcu.util.basis.LineUp;
import com.mandarin.bcu.util.pack.Pack;
import com.mandarin.bcu.util.unit.Form;

public class LvRestrict extends Data {

	public static final int[] MAX = new int[] { 120, 10, 10, 10, 10, 10 };

	public final Map<CharaGroup, int[]> res = new TreeMap<>();
	public final Pack pack;

	public int[][] rares = new int[RARITY_TOT][6];
	public int[] all = new int[6];
	public int id;
	public String name = "";

	public LvRestrict(MapColc mc, InStream is) {
		pack = mc.pack;
		zread(mc, is);
	}

	public LvRestrict(Pack pac, int ID) {
		pack = pac;
		all = MAX.clone();
		for (int i = 0; i < RARITY_TOT; i++)
			rares[i] = MAX.clone();
		id = ID;
	}

	public LvRestrict(Pack pac, int ID, LvRestrict lvr) {
		pack = pac;
		id = ID;
		all = lvr.all.clone();
		for (int i = 0; i < RARITY_TOT; i++)
			rares[i] = lvr.rares[i].clone();
		for (CharaGroup cg : lvr.res.keySet())
			res.put(cg, lvr.res.get(cg).clone());
	}

	private LvRestrict(LvRestrict lvr) {
		id = -1;
		pack = lvr.pack;
		for (CharaGroup cg : lvr.res.keySet())
			res.put(cg, lvr.res.get(cg).clone());
	}

	public LvRestrict combine(LvRestrict lvr) {
		LvRestrict ans = new LvRestrict(this);
		for (int i = 0; i < 6; i++)
			ans.all[i] = Math.min(lvr.all[i], all[i]);
		for (int i = 0; i < RARITY_TOT; i++)
			for (int j = 0; j < 6; j++)
				ans.rares[i][j] = Math.min(lvr.rares[i][j], rares[i][j]);
		for (CharaGroup cg : lvr.res.keySet())
			if (res.containsKey(cg)) {
				int[] lv0 = res.get(cg);
				int[] lv1 = lvr.res.get(cg);
				int[] lv = new int[6];
				for (int i = 0; i < 6; i++)
					lv[i] = Math.min(lv0[i], lv1[i]);
				ans.res.put(cg, lv);
			} else
				ans.res.put(cg, lvr.res.get(cg).clone());
		return ans;
	}

	public boolean isValid(LineUp lu) {
		for (Form[] fs : lu.fs)
			for (Form f : fs)
				if (f != null) {
					int[] mlv = valid(f);
					int[] flv = lu.map.get(f.unit);
					for (int i = 0; i < 6; i++)
						if (mlv[i] < flv[i])
							return false;
				}
		return true;
	}

	@Override
	public String toString() {
		return trio(id) + "-" + name;
	}

	public boolean used() {
		for (StageMap sm : pack.mc.maps)
			for (Stage st : sm.list)
				if (st.lim != null && st.lim.lvr == this)
					return true;
		return false;
	}

	public int[] valid(Form f) {
		int[] lv = MAX.clone();
		boolean mod = false;
		for (CharaGroup cg : res.keySet())
			if (cg.set.contains(f.unit)) {
				int[] rst = res.get(cg);
				for (int i = 0; i < 6; i++)
					lv[i] = Math.min(lv[i], rst[i]);
				mod = true;
			}
		if (mod)
			return f.regulateLv(null, lv);
		for (int i = 0; i < 6; i++)
			lv[i] = Math.min(lv[i], rares[f.unit.rarity][i]);
		for (int i = 0; i < 6; i++)
			lv[i] = Math.min(lv[i], all[i]);
		return f.regulateLv(null, lv);
	}

	public void validate(LineUp lu) {
		for (Form[] fs : lu.fs)
			for (Form f : fs)
				if (f != null)
					lu.map.put(f.unit, valid(f));
		lu.renew();
	}

	protected void write(OutStream os) {
		os.writeString("0.3.8");
		os.writeString(name);
		os.writeInt(id);
		os.writeIntB(all);
		os.writeIntBB(rares);
		os.writeInt(res.size());
		for (CharaGroup cg : res.keySet()) {
			os.writeInt(cg.id);
			os.writeIntB(res.get(cg));
		}
	}

	private void zread(MapColc mc, InStream is) {
		int ver = getVer(is.nextString());
		if (ver >= 308)
			zread$000308(mc, is);
		else if (ver >= 307)
			zread$000307(mc, is);
	}

	private void zread$000307(MapColc mc, InStream is) {
		id = is.nextInt();
		int[] tb = is.nextIntsB();
		for (int i = 0; i < tb.length; i++)
			all[i] = tb[i];
		int[][] tbb = is.nextIntsBB();
		for (int i = 0; i < tbb.length; i++)
			for (int j = 0; j < tbb[i].length; j++)
				rares[i][j] = tbb[i][j];
		int n = is.nextInt();
		for (int i = 0; i < n; i++) {
			int cg = is.nextInt();
			int[] vals = new int[6];
			tb = is.nextIntsB();
			for (int j = 0; j < tb.length; j++)
				vals[j] = tb[j];
			CharaGroup cgs = mc.groups.get(cg);
			if (cgs != null)
				res.put(cgs, vals);
		}
	}

	private void zread$000308(MapColc mc, InStream is) {
		name = is.nextString();
		zread$000307(mc, is);
	}

}
