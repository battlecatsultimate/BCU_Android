package com.mandarin.bcu.util.anim;

import java.io.PrintStream;
import java.util.Queue;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.main.Opts;
import com.mandarin.bcu.util.BattleStatic;
import com.mandarin.bcu.util.Data;
import com.mandarin.bcu.util.system.files.FileData;

public class MaAnim extends Data implements BattleStatic {

	public static MaAnim newIns(FileData f) {
		try {
			return new MaAnim(f.readLine());
		} catch (Exception e) {
			e.printStackTrace();
			Opts.animErr(f.toString());
			return new MaAnim();
		}
	}

	public static MaAnim newIns(String str) {
		return readSave(str, f -> f == null ? new MaAnim() : new MaAnim(f));
	}

	public int n;
	public Part[] parts;

	public int max = 1, len = 1;

	public MaAnim() {
		n = 0;
		parts = new Part[0];
	}

	public MaAnim(Queue<String> qs) {
		qs.poll();
		qs.poll();
		n = Integer.parseInt(qs.poll().trim());
		parts = new Part[n];
		for (int i = 0; i < n; i++)
			parts[i] = new Part(qs);
		validate();
	}

	private MaAnim(MaAnim ma) {
		n = ma.n;
		parts = new Part[n];
		for (int i = 0; i < n; i++)
			parts[i] = ma.parts[i].clone();
		validate();
	}

	@Override
	public MaAnim clone() {
		return new MaAnim(this);
	}

	public void revert() {
		for (Part p : parts)
			if (p.ints[1] == 11)
				for (int[] move : p.moves)
					move[1] *= -1;
	}

	public void validate() {
		max = 1;
		for (int i = 0; i < n; i++)
			if (parts[i].getMax() > max)
				max = parts[i].getMax();
		len = 1;
		for (int i = 0; i < n; i++)
			if (parts[i].getMax() - parts[i].off > len)
				len = parts[i].getMax() - parts[i].off;
	}

	public void write(PrintStream ps) {
		ps.println("[maanim]");
		ps.println("1");
		ps.println(parts.length);
		for (Part p : parts)
			p.write(ps);
	}

	protected void restore(InStream is) {
		n = is.nextInt();
		parts = new Part[n];
		for (int i = 0; i < n; i++) {
			parts[i] = new Part();
			parts[i].restore(is);
		}
		validate();
	}

	protected void update(int f, EAnimD eAnim, boolean rotate) {
		if (f == 0 || rotate && f % max == 0) {
			for (EPart e : eAnim.ent)
				e.setValue();
			eAnim.order.sort(null);
		}
		for (int i = 0; i < n; i++) {
			int loop = parts[i].ints[2];
			int smax = parts[i].max;
			int fir = parts[i].fir;
			int lmax = smax - fir;
			boolean prot = rotate || loop == -1;
			int frame = 0;
			if (prot) {
				int mf = loop == -1 ? smax : max;
				frame = mf == 0 ? 0 : (f + parts[i].off) % mf;
				if (loop > 0 && lmax != 0) {
					if (frame > fir + loop * lmax) {
						parts[i].ensureLast(eAnim.ent);
						continue;
					}
					if (frame <= fir)
						;
					else if (frame < fir + loop * lmax)
						frame = fir + (frame - fir) % lmax;
					else
						frame = smax;
				}
			} else {
				frame = f + parts[i].off;
				if (loop > 0 && lmax != 0) {
					if (frame > fir + loop * lmax) {
						parts[i].ensureLast(eAnim.ent);
						continue;
					}
					if (frame <= fir)
						;
					else if (frame < fir + loop * lmax)
						frame = fir + (frame - fir) % lmax;
					else
						frame = smax;
				}
			}
			parts[i].update(frame, eAnim.ent);
		}
	}

	protected void write(OutStream os) {
		os.writeInt(n);
		for (Part p : parts)
			p.write(os);
	}

}
