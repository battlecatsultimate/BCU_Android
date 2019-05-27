package com.mandarin.bcu.util.unit;

import java.util.ArrayList;
import java.util.List;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.util.anim.AnimC;
import com.mandarin.bcu.util.entity.data.CustomEnemy;
import com.mandarin.bcu.util.pack.Pack;
import com.mandarin.bcu.util.system.FixIndexList;

public class EnemyStore extends FixIndexList<Enemy> {

	public static AbEnemy getAbEnemy(int id, boolean cannull) {
		if (cannull && id < 0)
			return null;
		if (id < 0)
			id = 0;
		AbEnemy e = null;
		if (id < 1000)
			e = Pack.def.es.get(id);
		else {
			Pack p = Pack.map.get(id / 1000);
			if (p != null)
				e = id % 1000 < 500 ? p.es.get(id % 1000) : p.es.ers.get(id % 1000 - 500);
		}
		if (e == null)
			e = Pack.def.es.get(0);
		return e;
	}

	public static List<Enemy> getAll(Pack pack, boolean parent) {
		List<Enemy> ans = new ArrayList<>();
		if (pack != null) {
			if (parent)
				for (int id : pack.rely)
					ans.addAll(Pack.map.get(id).es.getList());
			ans.addAll(pack.es.getList());
		} else
			for (Pack p : Pack.map.values())
				ans.addAll(p.es.getList());
		return ans;
	}

	public static Enemy getEnemy(int id) {
		if (id < 0)
			id = 0;
		Enemy e = null;
		if (id < 1000)
			e = Pack.def.es.get(id);
		else {
			Pack p = Pack.map.get(id / 1000);
			if (p != null)
				e = p.es.get(id % 1000);
		}
		if (e == null)
			e = Pack.def.es.get(0);
		return e;
	}

	public final Pack pack;

	public final FixIndexList<EneRand> ers = new FixIndexList<>(new EneRand[500]);

	public EnemyStore(Pack p) {
		super(new Enemy[500]);
		pack = p;
	}

	public Enemy addEnemy(DIYAnim da, CustomEnemy ce) {
		int hash = nextInd() + pack.id * 1000;
		Enemy e = new Enemy(hash, da.getAnimC(), ce);
		add(e);
		return e;
	}

	public OutStream packup() {
		OutStream os = OutStream.getIns();
		os.writeString("0.4.2");
		List<Enemy> list = getList();
		os.writeInt(list.size());
		for (Enemy e : list) {
			os.writeInt(e.id);
			os.writeString(e.name);
			((CustomEnemy) e.de).write(os);
			os.accept(((AnimC) e.anim).write());
		}

		List<EneRand> lis = ers.getList();
		os.writeInt(lis.size());
		for (EneRand e : lis) {
			os.writeInt(e.id - 500);
			os.accept(e.write());
		}
		os.terminate();
		return os;
	}

	public OutStream write() {
		OutStream os = OutStream.getIns();
		os.writeString("0.4.2");
		List<Enemy> list = getList();
		os.writeInt(list.size());
		for (Enemy e : list) {
			((CustomEnemy) e.de).write(os);
			os.writeInt(e.id % 1000);
			os.accept(DIYAnim.writeAnim((AnimC) e.anim));
			os.writeString(e.name);
		}
		List<EneRand> lis = ers.getList();
		os.writeInt(lis.size());
		for (EneRand e : lis) {
			os.writeInt(e.id - 500);
			os.accept(e.write());
		}
		os.terminate();
		return os;
	}

	public void zreadp(InStream is) {
		int val = getVer(is.nextString());
		if (val >= 402)
			zreadp$000402(val, is);
		else if (val >= 401)
			zreadp$000401(val, is);
	}

	public void zreadt(int ver, InStream is) {
		if (ver >= 401)
			ver = getVer(is.nextString());
		if (ver >= 402)
			zreadt$000402(ver, is);
		else if (ver >= 401)
			zreadt$000401(ver, is);
		else if (ver >= 302)
			zreadt$000302(ver, is);
	}

	private void addEnemy(int hash, CustomEnemy ce, InStream nam, String na) {
		hash = pack.id * 1000 + hash % 1000;
		AnimC ac = DIYAnim.zread(nam, true);
		Enemy e = new Enemy(hash, ac, ce);
		e.name = na;
		set(hash % 1000, e);
	}

	private void addEnemy(int hash, CustomEnemy ce, String nam, String na) {
		hash = pack.id * 1000 + hash % 1000;
		AnimC ac = DIYAnim.getAnim(nam, true);
		Enemy e = new Enemy(hash, ac, ce);
		e.name = na;
		set(hash % 1000, e);
	}

	private void zreadp$000401(int ver, InStream is) {
		int n = is.nextInt();
		for (int i = 0; i < n; i++) {
			int hash = is.nextInt();
			String str = is.nextString();
			CustomEnemy ce = new CustomEnemy();
			ce.fillData(ver, is);
			AnimC ac = new AnimC(is.subStream());
			Enemy e = new Enemy(hash % 1000 + pack.id * 1000, ac, ce);
			e.name = str;
			set(hash % 1000, e);
		}
	}

	private void zreadp$000402(int ver, InStream is) {
		int n = is.nextInt();
		for (int i = 0; i < n; i++) {
			int hash = is.nextInt();
			String str = is.nextString();
			CustomEnemy ce = new CustomEnemy();
			ce.fillData(ver, is);
			AnimC ac = new AnimC(is.subStream());
			Enemy e = new Enemy(hash % 1000 + pack.id * 1000, ac, ce);
			e.name = str;
			set(hash % 1000, e);
		}
		n = is.nextInt();
		for (int i = 0; i < n; i++) {
			int hash = is.nextInt();
			EneRand e = new EneRand(pack, hash + 500);
			e.zread(is.subStream());
			ers.set(hash, e);
		}
	}

	private void zreadt$000302(int ver, InStream is) {
		int len = is.nextInt();
		for (int i = 0; i < len; i++) {
			CustomEnemy ce = new CustomEnemy();
			ce.fillData(ver, is);
			int hash = is.nextInt();
			String nam = is.nextString();
			String na = is.nextString();
			addEnemy(hash, ce, nam, na);
		}
	}

	private void zreadt$000401(int ver, InStream is) {
		int len = is.nextInt();
		for (int i = 0; i < len; i++) {
			CustomEnemy ce = new CustomEnemy();
			ce.fillData(ver, is);
			int hash = is.nextInt();
			InStream anim = is.subStream();
			String na = is.nextString();
			addEnemy(hash, ce, anim, na);
		}
	}

	private void zreadt$000402(int ver, InStream is) {
		int len = is.nextInt();
		for (int i = 0; i < len; i++) {
			CustomEnemy ce = new CustomEnemy();
			ce.fillData(ver, is);
			int hash = is.nextInt();
			InStream anim = is.subStream();
			String na = is.nextString();
			addEnemy(hash, ce, anim, na);
		}
		int n = is.nextInt();
		for (int i = 0; i < n; i++) {
			int hash = is.nextInt();
			EneRand e = new EneRand(pack, hash + 500);
			e.zread(is.subStream());
			ers.set(hash, e);
		}
	}

}
