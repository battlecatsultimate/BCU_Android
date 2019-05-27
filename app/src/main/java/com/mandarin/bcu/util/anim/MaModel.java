package com.mandarin.bcu.util.anim;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.main.Opts;
import com.mandarin.bcu.util.Data;
import com.mandarin.bcu.util.BattleStatic;
import com.mandarin.bcu.util.system.files.FileData;

public class MaModel extends Data implements Cloneable, BattleStatic {

	public static MaModel newIns(FileData f) {
		try {
			return new MaModel(f.readLine());
		} catch (Exception e) {
			e.printStackTrace();
			Opts.animErr(f.toString());
			return new MaModel();
		}

	}

	public static MaModel newIns(String path) {
		return readSave(path, f -> f == null ? new MaModel() : new MaModel(f));
	}

	public int n, m;
	public int[] ints = new int[3];
	public int[][] confs, parts;
	public String[] strs0, strs1;

	public Map<int[], Integer> status = new HashMap<>();

	public MaModel() {
		n = 1;
		m = 1;
		parts = new int[][] { { -1, -1, 0, 0, 0, 0, 0, 0, 1000, 1000, 0, 1000, 0, 0 } };
		ints = new int[] { 1000, 3600, 1000 };
		confs = new int[][] { { 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 } };
		strs0 = new String[] { "default" };
		strs1 = new String[] { "default" };
	}

	protected MaModel(Queue<String> qs) {
		qs.poll();
		if (!qs.poll().trim().equals("3"))
			;// Printer.p("MaModel", 24, "new MaModel type");
		n = Integer.parseInt(qs.poll().trim());
		parts = new int[n][14];
		strs0 = new String[n];
		for (int i = 0; i < n; i++) {
			String[] ss = qs.poll().trim().split(",");
			for (int j = 0; j < 13; j++)
				parts[i][j] = Integer.parseInt(ss[j].trim());
			if (ss.length == 14)
				strs0[i] = ss[13];
			else
				strs0[i] = "";
		}
		String[] ss = qs.poll().trim().split(",");
		for (int i = 0; i < 3; i++)
			ints[i] = Integer.parseInt(ss[i].trim());
		m = Integer.parseInt(qs.poll().trim());
		confs = new int[m][6];
		strs1 = new String[m];
		for (int i = 0; i < m; i++) {
			ss = qs.poll().trim().split(",");
			for (int j = 0; j < 6; j++)
				confs[i][j] = Integer.parseInt(ss[j].trim());
			if (ss.length == 7)
				strs1[i] = ss[6];
			else
				strs1[i] = "";
		}
	}

	private MaModel(MaModel mm) {
		n = mm.n;
		m = mm.m;
		ints = mm.ints.clone();
		parts = new int[n][];
		confs = new int[m][];
		for (int i = 0; i < n; i++)
			parts[i] = mm.parts[i].clone();
		for (int i = 0; i < m; i++)
			confs[i] = mm.confs[i].clone();
		strs0 = mm.strs0.clone();
		strs1 = mm.strs1.clone();
	}

	/** regulate check imgcut id and detect parent loop */
	public void check(AnimD anim) {
		int ics = anim.imgcut.n;
		for (int[] p : parts) {
			if (p[2] >= ics)
				p[2] = 0;
			if (p[0] > n)
				p[0] = 0;
		}
		int[] temp = new int[parts.length];
		for (int i = 0; i < parts.length; i++)
			check(temp, i);
		for (int i = 0; i < parts.length; i++)
			if (temp[i] == 2)
				parts[i][0] = -1;
	}

	public void clearAnim(boolean[] bs, MaAnim[] as) {
		for (MaAnim ma : as) {
			List<Part> lp = new ArrayList<>();
			for (Part p : ma.parts)
				if (!bs[p.ints[0]])
					lp.add(p);
			ma.parts = lp.toArray(new Part[0]);
			ma.n = ma.parts.length;
		}
	}

	@Override
	public MaModel clone() {
		return new MaModel(this);
	}

	public int getChild(boolean[] bs) {
		int total = 0;
		int count = 1;
		while (count > 0) {
			count = 0;
			for (int i = 0; i < n; i++)
				if (!bs[i] && parts[i][0] >= 0 && bs[parts[i][0]]) {
					count++;
					total++;
					bs[i] = true;
				}
		}
		return total;
	}

	public void reorder(int[] move) {
		int[][] data = parts;
		String[] name = strs0;
		parts = new int[move.length][];
		strs0 = new String[move.length];
		for (int i = 0; i < n; i++)
			if (move[i] < 0 || move[i] >= data.length) {
				parts[i] = new int[] { 0, -1, 0, 0, 0, 0, 0, 0, 1000, 1000, 0, 1000, 0, 0 };
				strs0[i] = "new part";
			} else {
				parts[i] = data[move[i]];
				strs0[i] = name[move[i]];
			}

	}

	public void revert() {
		parts[0][8] *= -1;
		for (int[] sets : parts)
			sets[10] *= -1;
	}

	protected EPart[] arrange(EAnimI e) {
		EPart[] ents = new EPart[n];
		for (int i = 0; i < n; i++)
			ents[i] = new EPart(this, e.anim(), parts[i], strs0[i], ents);
		return ents;
	}

	protected void restore(InStream is) {
		n = is.nextInt();
		m = is.nextInt();
		ints = is.nextIntsB();
		parts = is.nextIntsBB();
		confs = is.nextIntsBB();
		strs0 = new String[n];
		for (int i = 0; i < n; i++)
			strs0[i] = is.nextString();
		strs1 = new String[m];
		for (int i = 0; i < m; i++)
			strs1[i] = is.nextString();
	}

	protected void write(OutStream os) {
		os.writeInt(n);
		os.writeInt(m);
		os.writeIntB(ints);
		os.writeIntBB(parts);
		os.writeIntBB(confs);
		for (String str : strs0)
			os.writeString(str);
		for (String str : strs1)
			os.writeString(str);
	}

	protected void write(PrintStream ps) {
		ps.println("[mamodel]");
		ps.println(3);
		ps.println(n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < 13; j++)
				ps.print(parts[i][j] + ",");
			ps.println(strs0[i]);
		}
		ps.println(ints[0] + "," + ints[1] + "," + ints[2]);
		ps.println(m);
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < confs[i].length; j++)
				ps.print(confs[i][j] + ",");
			ps.println(strs1[i]);
		}
	}

	/** detect loop */
	private int check(int[] temp, int p) {
		if (temp[p] > 0)
			return temp[p];
		if (parts[p][0] == -1)
			return temp[p] = 1;
		temp[p] = 2;
		return temp[p] = check(temp, parts[p][0]);
	}

}
