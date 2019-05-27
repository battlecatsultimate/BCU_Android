package com.mandarin.bcu.util.basis;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.util.stage.EStage;
import com.mandarin.bcu.util.stage.Recd;

public class SBRply extends Mirror {

	private final Recd r;
	private final MirrorSet mir;

	public SBRply(Recd re) {
		super(re);
		r = re;
		mir = new MirrorSet(r);
	}

	public void back(int t) {
		Mirror m = mir.getReal(sb.time - t);
		sb = m.sb;
		rl = m.rl;
	}

	public int prog() {
		return Math.min(mir.size - 1, sb.time * mir.size / r.len);
	}

	public void restoreTo(int perc) {
		Mirror m = mir.getRaw(perc);
		if (m == null)
			return;
		sb = m.sb;
		rl = m.rl;
		while (prog() < perc)
			update();
	}

	public int size() {
		return mir.size - 1;
	}

	@Override
	public void update() {
		super.update();
		mir.add(this);
	}

}

class Mirror extends BattleField {

	protected Release rl;

	protected Mirror(Mirror sr) {
		super((StageBasis) sr.sb.clone());
		rl = sr.rl.clone();
	}

	protected Mirror(Recd r) {
		super(new EStage(r.st, r.star), r.lu, r.conf, r.seed);
		rl = new Release(r.action.translate());
	}

	/** process the user action */
	@Override
	protected void actions() {
		int rec = rl.get();
		if ((rec & 1) > 0)
			act_mon();
		if ((rec & 2) > 0)
			act_can();
		if ((rec & 4) > 0)
			act_sniper();
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 5; j++) {
				if ((rec & (1 << (i * 5 + j + 3))) > 0)
					act_lock(i, j);
				act_spawn(i, j, (rec & (1 << (i * 5 + j + 13))) > 0);
			}
		sb.rx.add(rec);
	}

}

class MirrorSet {

	private final Mirror[] mis;
	private final int len;
	protected final int size;

	protected MirrorSet(Recd r) {
		len = r.len + 1;
		size = (int) Math.sqrt(len);
		mis = new Mirror[size];
	}

	protected void add(SBRply sb) {
		int t = sb.sb.time;
		if (t * size / len >= size)
			return;
		if (mis[t * size / len] == null)
			mis[t * size / len] = new Mirror(sb);
	}

	protected Mirror getRaw(int t) {
		Mirror mr = mis[t];
		if (mr == null) {
			for (int i = t - 1; i >= 0; i--)
				if (mis[i] != null)
					return new Mirror(mis[i]);
			return null;
		}
		return new Mirror(mr);
	}

	protected Mirror getReal(int t) {
		Mirror m = getRaw(t * size / len);
		while (m.sb.time < t)
			m.update();
		return m;
	}

}

class Release {

	protected final int[] recd;
	private int ind, rec, rex;

	protected Release(InStream in) {
		int n = in.nextInt();
		recd = new int[n];
		for (int i = 0; i < n; i++)
			recd[i] = in.nextInt();
	}

	private Release(Release r) {
		recd = r.recd;
		ind = r.ind;
		rec = r.rec;
		rex = r.rex;
	}

	@Override
	protected Release clone() {
		return new Release(this);
	}

	protected int get() {
		if (rex == 0)
			if (recd.length <= ind)
				rec = 0;
			else {
				rec = recd[ind++];
				rex = recd[ind++];
			}
		rex--;
		return rec;
	}

}
