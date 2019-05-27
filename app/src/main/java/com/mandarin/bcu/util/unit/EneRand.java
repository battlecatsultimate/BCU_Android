package com.mandarin.bcu.util.unit;

import java.util.Set;
import java.util.TreeSet;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.util.EREnt;
import com.mandarin.bcu.util.EntRand;
import com.mandarin.bcu.util.Res;
import com.mandarin.bcu.util.basis.StageBasis;
import com.mandarin.bcu.util.entity.EEnemy;
import com.mandarin.bcu.util.pack.Pack;
import com.mandarin.bcu.util.system.VImg;

public class EneRand extends EntRand<Integer> implements AbEnemy {

	public final Pack pack;
	public final int id;

	public String name = "";

	public EneRand(Pack p, int ID) {
		pack = p;
		id = ID;
	}

	public void fillPossible(Set<Enemy> se, Set<EneRand> sr) {
		sr.add(this);
		for (EREnt<Integer> e : list) {
			AbEnemy ae = EnemyStore.getAbEnemy(e.ent, false);
			if (ae instanceof Enemy)
				se.add((Enemy) ae);
			if (ae instanceof EneRand) {
				EneRand er = (EneRand) ae;
				if (!sr.contains(er))
					er.fillPossible(se, sr);
			}
		}
	}

	@Override
	public EEnemy getEntity(StageBasis sb, Object obj, double mul, int d0, int d1, int m) {
		sb.rege.add(this);
		return get(getSelection(sb, obj), sb, obj, mul, d0, d1, m);
	}

	@Override
	public VImg getIcon() {
		return Res.ico[0][0];
	}

	@Override
	public int getID() {
		return pack.id * 1000 + id;
	}

	@Override
	public Set<Enemy> getPossible() {
		Set<Enemy> te = new TreeSet<>();
		fillPossible(te, new TreeSet<EneRand>());
		return te;
	}

	@Override
	public String toString() {
		return trio(id) + " - " + name;
	}

	protected OutStream write() {
		OutStream os = OutStream.getIns();
		os.writeString("0.4.0");
		os.writeString(name);
		os.writeInt(type);
		os.writeInt(list.size());
		for (EREnt<Integer> e : list) {
			os.writeInt(e.ent);
			os.writeInt(e.multi);
			os.writeInt(e.share);
		}
		os.terminate();
		return os;
	}

	protected void zread(InStream is) {
		int ver = getVer(is.nextString());
		if (ver >= 400)
			zread$000400(is);
	}

	private EEnemy get(EREnt<Integer> x, StageBasis sb, Object obj, double mul, int d0, int d1, int m) {
		if (x == null || x.ent == null)
			return EnemyStore.getEnemy(0).getEntity(sb, obj, mul, d0, d1, m);
		return EnemyStore.getAbEnemy(x.ent, false).getEntity(sb, obj, x.multi * mul / 100, d0, d1, m);
	}

	private void zread$000400(InStream is) {
		name = is.nextString();
		type = is.nextInt();
		int n = is.nextInt();
		for (int i = 0; i < n; i++) {
			EREnt<Integer> ere = new EREnt<Integer>();
			list.add(ere);
			ere.ent = is.nextInt();
			ere.multi = is.nextInt();
			ere.share = is.nextInt();
		}
	}

}
