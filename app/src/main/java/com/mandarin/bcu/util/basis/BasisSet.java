package com.mandarin.bcu.util.basis;

import java.util.ArrayList;
import java.util.List;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.main.Opts;
import com.mandarin.bcu.util.system.Copable;

public class BasisSet extends Basis implements Copable<BasisSet> {

	public static final List<BasisSet> list = new ArrayList<>();
	public static final BasisSet def = new BasisSet();
	public static BasisSet current;

	public static void read(InStream is) {
		zreads(is, false);
	}

	public static BasisSet[] readBackup(InStream is) {
		return zreads(is, true).toArray(new BasisSet[0]);
	}

	public static OutStream writeAll() {
		OutStream os = OutStream.getIns();
		os.writeString("0.3.8");
		os.writeInt(list.size());
		for (BasisSet bs : list)
			if (bs != def)
				os.accept(bs.write());
		os.writeInt((byte) list.indexOf(current));
		os.terminate();
		return os;
	}

	private static List<BasisSet> zreads(InStream is, boolean bac) {
		int ver = getVer(is.nextString());
		if (ver >= 308)
			return zreads$000308(ver, is, bac);
		return zreads$000307(ver, is, bac);
	}

	private static List<BasisSet> zreads$000307(int ver, InStream is, boolean bac) {
		List<BasisSet> ans = bac ? new ArrayList<BasisSet>() : list;
		int n = is.nextByte();
		for (int i = 1; i < n; i++)
			ans.add(new BasisSet(ver, is));
		int ind = is.nextByte();
		if (!bac)
			current = list.get(ind);
		return ans;
	}

	private static List<BasisSet> zreads$000308(int ver, InStream is, boolean bac) {
		List<BasisSet> ans = bac ? new ArrayList<BasisSet>() : list;
		int n = is.nextInt();
		for (int i = 1; i < n; i++)
			try {
				BasisSet bs = new BasisSet(ver, is.subStream());
				ans.add(bs);
			} catch (Exception e) {
				e.printStackTrace();
				Opts.loadErr("error in reading basis #" + i);
			}
		int ind = Math.max(is.nextInt(), ans.size() - 1);
		if (!bac)
			current = list.get(ind);
		return ans;
	}

	private final Treasure t;
	public final List<BasisLU> lb = new ArrayList<>();
	public BasisLU sele;

	public BasisSet() {
		if (list.size() == 0)
			name = "temporary";
		else
			name = "set " + list.size();
		t = new Treasure(this);
		current = this;
		lb.add(sele = new BasisLU(this));
		list.add(this);
	}

	public BasisSet(BasisSet ref) {
		name = "set " + list.size();
		list.add(this);
		t = new Treasure(this, ref.t);
		current = this;
		for (BasisLU blu : ref.lb)
			lb.add(sele = new BasisLU(this, blu));
	}

	private BasisSet(int ver, InStream is) {
		name = is.nextString();
		t = new Treasure(this, ver, is);
		zread(ver, is);
	}

	public BasisLU add() {
		lb.add(sele = new BasisLU(this));
		return sele;
	}

	@Override
	public BasisSet copy() {
		return new BasisSet(this);
	}

	public BasisLU copyCurrent() {
		lb.add(sele = new BasisLU(this, sele));
		return sele;
	}

	/** BasisSet are used in data display, so cannot be effected by combo */
	@Override
	public int getInc(int type) {
		return 0;
	}

	public BasisLU remove() {
		lb.remove(sele);
		return sele = lb.get(0);
	}

	@Override
	public Treasure t() {
		return t;
	}

	public OutStream write() {
		OutStream os = OutStream.getIns();
		os.writeString(name);
		t.write(os);
		os.writeString("0.3.8");
		os.writeInt(lb.size());
		for (BasisLU lu : lb) {
			os.writeString(lu.name);
			os.writeIntB(lu.nyc);
			os.accept(lu.lu.write());
		}
		os.writeInt(lb.indexOf(sele));
		os.terminate();
		return os;
	}

	private void zread(int val, InStream is) {
		if (val >= 307)
			val = getVer(is.nextString());
		if (val >= 308)
			zread$000308(is);
		else if (val >= 307)
			zread$000307(is);
		else
			zread$000000(val, is);
	}

	private void zread$000000(int ver, InStream is) {
		int n = is.nextByte();
		for (int i = 0; i < n; i++) {
			String str = is.nextString();
			int[] ints = is.nextIntsB();
			lb.add(new BasisLU(this, new LineUp(ver, is), str, ints));
		}
		int ind = is.nextByte();
		sele = lb.get(ind);
	}

	private void zread$000307(InStream is) {
		int n = is.nextByte();
		for (int i = 0; i < n; i++) {
			String str = is.nextString();
			int[] ints = is.nextIntsB();
			lb.add(new BasisLU(this, new LineUp(307, is), str, ints));
		}
		int ind = is.nextByte();
		sele = lb.get(ind);
	}

	private void zread$000308(InStream is) {
		int n = is.nextInt();
		for (int i = 0; i < n; i++) {
			String str = is.nextString();
			int[] ints = is.nextIntsB();
			InStream sub = is.subStream();
			try {
				BasisLU bl = new BasisLU(this, new LineUp(308, sub), str, ints);
				lb.add(bl);
			} catch (Exception e) {
				e.printStackTrace();
				Opts.loadErr("error in reading lineup " + name + " - " + str);
			}

		}
		int ind = is.nextInt();
		sele = lb.get(ind);
	}

}
