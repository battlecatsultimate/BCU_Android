package com.mandarin.bcu.util.basis;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.util.BattleStatic;
import com.mandarin.bcu.util.system.Copable;

public class BasisLU extends Basis implements Copable<BasisLU>, BattleStatic {

	public static BasisLU zread(InStream is) {
		int ver = getVer(is.nextString());
		if (ver >= 308)
			return zread$000308(is);
		return zread$000307(is);
	}

	private static BasisLU zread$000307(InStream is) {
		String name = is.nextString();
		BasisLU ans = new BasisLU(307, is);
		ans.nyc = is.nextIntsB();
		ans.name = name;
		return ans;
	}

	private static BasisLU zread$000308(InStream is) {
		String name = is.nextString();
		BasisLU ans = new BasisLU(308, is);
		ans.nyc = is.nextIntsB();
		ans.name = name;
		return ans;
	}

	private final Treasure t;
	public final LineUp lu;
	public int[] nyc = new int[3];

	public BasisLU(BasisSet bs, LineUp line, String str) {
		t = new Treasure(this, bs.t());
		name = str;
		lu = line;
	}

	public BasisLU(BasisSet bs, LineUp line, String str, int[] ints) {
		this(bs, line, str);
		nyc = ints;
	}

	protected BasisLU(BasisSet bs) {
		t = new Treasure(this, bs.t());
		lu = new LineUp();
		name = "lineup " + bs.lb.size();
	}

	protected BasisLU(BasisSet bs, BasisLU bl) {
		t = new Treasure(this, bs.t());
		lu = new LineUp(bl.lu);
		name = "lineup " + bs.lb.size();
		nyc = bl.nyc.clone();
	}

	private BasisLU(int ver, InStream is) {
		lu = new LineUp(ver, ver >= 308 ? is.subStream() : is);
		t = new Treasure(this, ver, is);
	}

	@Override
	public BasisLU copy() {
		return new BasisLU(BasisSet.current, this);
	}

	@Override
	public int getInc(int type) {
		return lu.inc[type];
	}

	/**
	 * although the Treasure information is the same, this includes the effects of
	 * combo, so need to be an independent Treasure Object
	 */
	@Override
	public Treasure t() {
		return t;
	}

	public OutStream write() {
		OutStream os = OutStream.getIns();
		os.writeString("0.3.8");
		os.writeString(name);
		os.accept(lu.write());
		t.write(os);
		os.writeIntB(nyc);
		os.terminate();
		return os;
	}

}
