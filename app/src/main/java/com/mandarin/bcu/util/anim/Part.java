package com.mandarin.bcu.util.anim;

import java.io.PrintStream;
import java.util.Queue;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;

public class Part implements Cloneable, Comparable<Part> {

	public int[] ints = new int[5];
	public String name;
	public int n, max, off, fir;
	public int frame, vd;// for editor only
	public int[][] moves;

	public Part() {
		ints = new int[] { 0, 5, -1, 0, 0 };
		name = "";
		n = 0;
		moves = new int[0][];
	}

	protected Part(Queue<String> qs) {
		String[] ss = qs.poll().trim().split(",");
		for (int i = 0; i < 5; i++)
			ints[i] = Integer.parseInt(ss[i].trim());
		if (ss.length == 6)
			name = ss[5];
		else
			name = "";
		n = Integer.parseInt(qs.poll().trim());
		moves = new int[n][4];
		for (int i = 0; i < n; i++) {
			ss = qs.poll().trim().split(",");
			for (int j = 0; j < 4; j++)
				moves[i][j] = Integer.parseInt(ss[j].trim());
		}
		validate();
	}

	private Part(Part p) {
		ints = p.ints.clone();
		name = p.name;
		n = p.n;
		moves = new int[n][];
		for (int i = 0; i < n; i++)
			moves[i] = p.moves[i].clone();
		off = p.off;
		validate();
	}

	public void check(AnimD anim) {
		int mms = anim.mamodel.n;
		int ics = anim.imgcut.n;
		if (ints[0] >= mms)
			ints[0] = 0;
		if (ints[0] < 0)
			ints[0] = 0;
		if (ints[1] == 2)
			for (int[] move : moves)
				if (move[1] >= ics)
					move[1] = 0;
	}

	@Override
	public Part clone() {
		return new Part(this);
	}

	@Override
	public int compareTo(Part o) {
		return Integer.compare(ints[0], o.ints[0]);
	}

	public void validate() {
		int doff = 0;
		if (n != 0 && moves[0][0] - off < 0)
			doff -= moves[0][0];
		for (int i = 0; i < n; i++)
			moves[i][0] += doff;
		off += doff;
		fir = moves.length == 0 ? 0 : moves[0][0];
		max = n > 0 ? moves[n - 1][0] : 0;
	}

	protected void ensureLast(EPart[] es) {
		if (n == 0)
			return;
		frame = moves[n - 1][0];
		es[ints[0]].alter(ints[1], vd = moves[n - 1][1]);
	}

	protected int getMax() {
		return ints[2] > 1 ? fir + (max - fir) * ints[2] : max;
	}

	protected void restore(InStream is) {
		n = is.nextInt();
		max = is.nextInt();
		off = is.nextInt();
		ints = is.nextIntsB();
		moves = is.nextIntsBB();
		name = is.nextString();
		validate();
	}

	protected void update(int f, EPart[] es) {
		frame = f;
		for (int i = 0; i < n; i++)
			if (frame == moves[i][0])
				es[ints[0]].alter(ints[1], vd = moves[i][1]);
			else if (i < n - 1 && frame > moves[i][0] && frame < moves[i + 1][0]) {
				if (ints[1] > 1) {
					int f0 = moves[i][0];
					int v0 = moves[i][1];
					int f1 = moves[i + 1][0];
					int v1 = moves[i + 1][1];
					double ti = 1.0 * (frame - f0) / (f1 - f0);
					if (moves[i][2] == 0)
						;
					else if (moves[i][2] == 1)
						ti = 0;
					else if (moves[i][2] == 2)
						if (moves[i][3] >= 0)
							ti = 1 - Math.sqrt(1 - Math.pow(ti, moves[i][3]));
						else
							ti = Math.sqrt(1 - Math.pow(1 - ti, -moves[i][3]));
					else if (moves[i][2] == 3) {
						vd = ease3(i, frame);
						es[ints[0]].alter(ints[1], vd);
						break;
					} else if (moves[i][2] == 4)
						if (moves[i][3] > 0)
							ti = 1 - Math.cos(ti * Math.PI / 2);
						else if (moves[i][3] < 0)
							ti = Math.sin(ti * Math.PI / 2);
						else
							ti = (1 - Math.cos(ti * Math.PI)) / 2;
					vd = (int) ((v1 - v0) * ti + v0);
					es[ints[0]].alter(ints[1], vd);
					break;
				}
			}
		if (n > 0 && frame > moves[n - 1][0])
			ensureLast(es);
	}

	protected void write(OutStream os) {
		os.writeInt(n);
		os.writeInt(max);
		os.writeInt(off);
		os.writeIntB(ints);
		os.writeIntBB(moves);
		os.writeString(name);
	}

	protected void write(PrintStream ps) {
		for (int val : ints)
			ps.print(val + ",");
		ps.println(name);
		ps.println(moves.length);
		for (int[] move : moves) {
			ps.print(move[0] - off + ",");
			for (int i = 1; i < move.length; i++)
				ps.print(move[i] + ",");
			ps.println();
		}
	}

	private int ease3(int i, int frame) {
		int low = i;
		int high = i;
		for (int j = i - 1; j >= 0; j--)
			if (moves[j][2] == 3)
				low = j;
			else
				break;
		for (int j = i + 1; j < moves.length; j++)
			if (moves[high = j][2] != 3)
				break;
		double sum = 0;
		for (int j = low; j <= high; j++) {
			double val = moves[j][1] * 4096;
			for (int k = low; k <= high; k++)
				if (j != k)
					val *= 1.0 * (frame - moves[k][0]) / (moves[j][0] - moves[k][0]);
			sum += val;
		}
		return (int) (sum / 4096);
	}

}
