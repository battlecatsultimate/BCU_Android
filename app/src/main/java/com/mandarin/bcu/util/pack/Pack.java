package com.mandarin.bcu.util.pack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.main.Opts;
import com.mandarin.bcu.util.Data;
import com.mandarin.bcu.util.anim.AnimC;
import com.mandarin.bcu.util.entity.data.CustomEnemy;
import com.mandarin.bcu.util.entity.data.MaskAtk;
import com.mandarin.bcu.util.stage.AbCastle;
import com.mandarin.bcu.util.stage.Castles;
import com.mandarin.bcu.util.stage.CharaGroup;
import com.mandarin.bcu.util.stage.LvRestrict;
import com.mandarin.bcu.util.stage.MapColc;
import com.mandarin.bcu.util.stage.Stage;
import com.mandarin.bcu.util.stage.StageMap;
import com.mandarin.bcu.util.unit.Enemy;
import com.mandarin.bcu.util.unit.EnemyStore;
import com.mandarin.bcu.util.unit.Form;
import com.mandarin.bcu.util.unit.Unit;
import com.mandarin.bcu.util.unit.UnitStore;

public class Pack extends Data {

	public static final Map<Integer, Pack> map = new TreeMap<>();

	public static final Pack def = new Pack();

	public static final int RELY_DEF = 0, RELY_CAS = 1, RELY_BG = 2, RELY_MUS = 3, RELY_ENE = 4, RELY_UNI = 5,
			RELY_CG = 6, RELY_LR = 7, RELY_ABI = 8;
	public static final int M_ES = 0, M_UL = 1, M_US = 2, M_CG = 3, M_LR = 4, M_BG = 5, M_CS = 6, M_MS = 7;

	public static String getAvailable(String str) {
		while (contains(str))
			str += "'";
		return str;
	}

	public static List<Pack> getEditable(int[] alr) {
		List<Pack> ans = new ArrayList<>();
		for (Pack p : map.values())
			if (p.editable) {
				boolean ava = true;
				for (int id : alr)
					ava &= p.id != id;
				if (ava)
					ans.add(p);
			}
		return ans;
	}

	public static Pack getNewPack() {
		return new Pack(new Random().nextInt(900000) + 100000);
	}

	private static boolean contains(String str) {
		for (Pack p : map.values())
			if (p.name.equals(str))
				return true;
		return false;
	}

	public final int id;

	public final CasStore cs;
	public final EnemyStore es = new EnemyStore(this);
	public final BGStore bg = new BGStore(this);
	public final UnitStore us = new UnitStore(this);
	public final SoulStore ss = new SoulStore(this);

	public final MusicStore ms = new MusicStore(this);
	public MapColc mc;
	public final List<Integer> rely = new ArrayList<>();
	public boolean editable = true;
	public String name = "custom pack", time = "", author = "";
	public File file;
	public int version;
	private InStream res;

	private int ver, bcuver;

	public Pack(InStream is, boolean reg) {
		ver = getVer(is.nextString());
		id = is.nextInt();
		res = is;
		int n = is.nextByte();
		for (int i = 0; i < n; i++)
			rely.add(is.nextInt());
		if (reg)
			map.put(id, this);
		cs = new CasStore(this, reg);
	}

	public Pack(int hash) {
		map.put(id = hash, this);
		name = getAvailable(name);
		mc = new MapColc(this);
		rely.add(0);
		cs = new CasStore(this, true);
	}

	private Pack() {
		map.put(id = 0, this);
		name = "default";
		editable = false;
		mc = null;
		cs = new CasStore(this, false);

	}

	public Collection<AbCastle> casList() {
		List<AbCastle> ans = new ArrayList<>();
		ans.addAll(Castles.defcas);
		for (int i : rely)
			ans.add(map.get(i).cs);
		ans.add(cs);
		return ans;
	}

	public void forceRemoveParent(int p) {
		if (!rely.contains(p))
			return;
		if (p < 1000)
			return;
		if (p == id)
			return;
		for (StageMap sm : mc.maps)
			for (Stage st : sm.list)
				st.removePack(p);
		for (CharaGroup cg : mc.groups.getList())
			cg.set.removeIf(u -> u.pack.id == p);
		for (LvRestrict lr : mc.lvrs.getList())
			lr.res.entrySet().removeIf(ent -> ent.getKey().pack.id == p);

		for (Enemy e : es.getList())
			for (int i = 0; i < e.de.getAtkCount(); i++) {
				MaskAtk am = e.de.getAtkModel(i);
				if (am.getProc(Data.P_SUMMON)[1] / 1000 == p)
					am.getProc(Data.P_SUMMON)[1] = 0;
				if (am.getProc(Data.P_THEME)[2] / 1000 == p)
					am.getProc(Data.P_THEME)[2] = 0;
			}
		for (Unit u : us.ulist.getList())
			for (Form e : u.forms)
				for (int i = 0; i < e.du.getAtkCount(); i++) {
					MaskAtk am = e.du.getAtkModel(i);
					if (am.getProc(Data.P_SUMMON)[1] / 1000 == p)
						am.getProc(Data.P_SUMMON)[1] = 0;
					if (am.getProc(Data.P_THEME)[2] / 1000 == p)
						am.getProc(Data.P_THEME)[2] = 0;
				}
	}

	public int relyOn(int p) {
		if (!rely.contains(p))
			return -1;
		if (p < 1000)
			return 0;
		if (p == id)
			return -1;
		for (StageMap sm : mc.maps)
			for (Stage st : sm.list) {
				int rel = st.relyOn(p);
				if (rel >= 0)
					return rel;
			}
		for (CharaGroup cg : mc.groups.getList())
			for (Unit u : cg.set)
				if (u.pack.id == p)
					return RELY_UNI;
		for (LvRestrict lr : mc.lvrs.getList())
			for (CharaGroup cg : lr.res.keySet())
				if (cg.pack.id == p)
					return RELY_CG;
		for (Enemy e : es.getList())
			for (int i = 0; i < e.de.getAtkCount(); i++) {
				MaskAtk am = e.de.getAtkModel(i);
				if (am.getProc(Data.P_SUMMON)[1] / 1000 == p)
					return RELY_ABI;
				if (am.getProc(Data.P_THEME)[2] / 1000 == p)
					return RELY_ABI;
			}
		for (Unit u : us.ulist.getList())
			for (Form e : u.forms)
				for (int i = 0; i < e.du.getAtkCount(); i++) {
					MaskAtk am = e.du.getAtkModel(i);
					if (am.getProc(Data.P_SUMMON)[1] / 1000 == p)
						return RELY_ABI;
					if (am.getProc(Data.P_THEME)[2] / 1000 == p)
						return RELY_ABI;
				}

		return -1;
	}

	@Override
	public String toString() {
		return hex(id) + " - " + name;
	}

	public boolean usable(int p) {
		if (p < 1000)
			return true;
		if (p == id)
			return true;
		for (int rel : rely)
			if (Pack.map.get(rel).id == p)
				return true;
		return false;
	}

	public OutStream write() {
		mc.name = name;
		OutStream os = OutStream.getIns();
		os.writeString("0.4.1");
		os.writeInt(id);
		os.writeByte((byte) rely.size());
		for (int val : rely)
			os.writeInt(val);
		os.writeString(name);
		os.accept(es.write());
		os.accept(cs.write());
		os.accept(bg.write());
		os.accept(us.write());
		mc.write(os);
		return os;
	}

	public void zreadt() {
		if (ver >= 400)
			zreadt$000400(res);
		else if (ver >= 308)
			zreadt$000308(res);
		else if (ver >= 306)
			zreadt$000306(res);
		else
			Opts.verErr("custom pack", "0-4-1-3");
		err("stages", () -> mc = new MapColc(this, res));
		err("music", () -> ms.load());
		res = null;
	}

	private void err(String str, Runnable c) {
		try {
			c.run();
		} catch (Exception e) {
			e.printStackTrace();
			Opts.loadErr("error at reading " + str + " in pack " + id);
		}
	}

	private void zreadp() {
		if (ver >= 401)
			zreadp$000401(res);
		else if (ver >= 306)
			zreadp$000306(res);
		else if (ver >= 303)
			zreadp$000303(res);
		mc = new MapColc(this, res);
		res = null;
	}

	private void zreadp$000303(InStream is) {
		name = is.nextString();
		int n = is.nextInt();
		for (int i = 0; i < n; i++) {
			int hash = is.nextInt();
			String str = is.nextString();
			CustomEnemy ce = new CustomEnemy();
			ce.fillData(ver, is);
			AnimC ac = new AnimC(is.subStream());
			Enemy e = new Enemy(hash, ac, ce);
			e.name = str;
			es.set(hash % 1000, e);
		}
	}

	private void zreadp$000306(InStream is) {
		zreadp$000303(res);
		cs.zreadp(ver, is.subStream());
		bg.zreadp(ver, is.subStream());
	}

	private void zreadp$000401(InStream is) {
		name = is.nextString();
		err("enemies", () -> es.zreadp(is.subStream()));
		err("units", () -> us.zreadp(is.subStream()));
		err("castles", () -> cs.zreadp(ver, is.subStream()));
		err("backgrounds", () -> bg.zreadp(ver, is.subStream()));
	}

	private void zreadt$000306(InStream is) {
		name = is.nextString();
		es.zreadt(ver, is);
		cs.zreadt(ver, is);
		is.nextInt();
	}

	private void zreadt$000308(InStream is) {
		name = is.nextString();
		es.zreadt(ver, is);
		cs.zreadt(ver, is);
		bg.zreadt(ver, is);
		us.zreadt(is.subStream());
	}

	private void zreadt$000400(InStream is) {
		name = is.nextString();
		err("enemies", () -> es.zreadt(ver, is.subStream()));
		err("castles", () -> cs.zreadt(ver, is.subStream()));
		err("backgrounds", () -> bg.zreadt(ver, is.subStream()));
		err("units", () -> us.zreadt(is.subStream()));

	}

}