package com.mandarin.bcu.util.stage;

import java.util.Map;
import java.util.TreeMap;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.main.Opts;
import com.mandarin.bcu.util.Data;
import com.mandarin.bcu.util.basis.BasisLU;
import com.mandarin.bcu.util.pack.Pack;

public class Recd extends Data {

	public static Map<String, Recd> map = new TreeMap<>();

	public static String getAvailable(String str) {
		while (map.containsKey(str))
			str += "'";
		return str;
	}

	private static Recd zread$000400(InStream is, String name) {
		long seed = is.nextLong();
		int[] conf = is.nextIntsB();
		int star = is.nextInt();
		BasisLU lu = BasisLU.zread(is.subStream());
		InStream action = is.subStream();
		int pid = is.nextInt();
		String mcn = is.nextString();
		String smid = is.nextString();
		String stid = is.nextString();
		Pack pack = Pack.map.get(pid);
		if (pack == null) {
			Opts.recdErr(name, "pack " + pid);
			return null;
		}
		MapColc mc = null;
		if (pack == Pack.def)
			mc = MapColc.getMap(mcn);
		else
			mc = pack.mc;
		if (!mc.name.equals(mcn)) {
			Opts.recdErr(name, "map set");
			return null;
		}
		StageMap sm = null;
		for (StageMap map : mc.maps)
			if (map.name.equals(smid))
				sm = map;
		if (sm == null) {
			Opts.recdErr(name, "stage map");
			return null;
		}
		Stage st = null;
		for (Stage s : sm.list)
			if (s.name.equals(stid))
				st = s;
		if (st == null) {
			Opts.recdErr(name, "stage");
			return null;
		}
		Recd ans = new Recd(lu, st, star, conf, seed);
		ans.action = action.translate();
		ans.name = name;
		return ans;
	}

	private static Recd zread$000401(InStream is, String name) {
		long seed = is.nextLong();
		int[] conf = is.nextIntsB();
		int star = is.nextInt();
		BasisLU lu = BasisLU.zread(is.subStream());
		InStream action = is.subStream();
		int pid = is.nextInt();
		Stage st = null;
		if (pid == 0) {
			int id = is.nextInt();
			StageMap sm = MapColc.getMap(id / 1000);
			st = sm.list.get(id % 1000);
			if (st == null) {
				Opts.recdErr(name, "stage " + id);
				return null;
			}
		} else {
			st = zreads$000401(is, pid, name);
		}

		Recd ans = new Recd(lu, st, star, conf, seed);
		ans.action = action.translate();
		ans.name = name;
		return ans;
	}

	private static Stage zreads$000401(InStream is, int pid, String name) {
		String mcn = is.nextString();
		String smid = is.nextString();
		String stid = is.nextString();
		Pack pack = Pack.map.get(pid);
		if (pack == null) {
			Opts.recdErr(name, "pack " + pid);
			return null;
		}
		MapColc mc = null;
		if (pack == Pack.def)
			mc = MapColc.getMap(mcn);
		else
			mc = pack.mc;
		if (!mc.name.equals(mcn)) {
			Opts.recdErr(name, "map set " + mcn);
			return null;
		}
		StageMap sm = null;
		for (StageMap map : mc.maps)
			if (map.name.equals(smid))
				sm = map;
		if (sm == null) {
			Opts.recdErr(name, "stage map " + smid);
			return null;
		}
		Stage st = null;
		for (Stage s : sm.list)
			if (s.name.equals(stid))
				st = s;

		if (st == null) {
			Opts.recdErr(name, "stage " + stid);
			return null;
		}
		return st;
	}

	public String name = "new record";
	public long seed;
	public int[] conf;
	public int star, len;
	public BasisLU lu;
	public boolean avail;
	public boolean marked;

	public OutStream action;

	public Stage st;

	public Recd(BasisLU blu, Stage sta, int stars, int[] con, long se) {
		lu = blu;
		st = sta;
		star = stars;
		conf = con;
		seed = se;
		avail = st != null;
	}

	@Override
	public Recd clone() {
		return new Recd(lu.copy(), st, star, conf.clone(), seed);
	}

	public int getLen() {
		if (len > 0)
			return len;
		InStream is = action.translate();
		int n = is.nextInt();
		for (int i = 0; i < n / 2; i++) {
			is.nextInt();
			len += is.nextInt();
		}
		return len;
	}

	@Override
	public String toString() {
		return name;
	}

	public void write() {
		OutStream os = OutStream.getIns();
		os.writeString("0.4.1");
		os.writeLong(seed);
		os.writeIntB(conf);
		os.writeInt(star);
		os.accept(lu.write());
		os.accept(action);
		int pid = st.map.mc.pack.id;
		os.writeInt(pid);
		if (pid > 0) {
			os.writeString(st.map.mc.name);
			os.writeString(st.map.name);
			os.writeString(st.name);
		} else {
			int id = st.map.mc.id;
			id = id * 1000000 + st.map.id * 1000 + st.id();
			os.writeInt(id);
		}
		os.terminate();
		// marked = !Writer.writeBytes(os, "./replay/" + name + ".replay");
	}

}
