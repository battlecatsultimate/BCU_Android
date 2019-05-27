package com.mandarin.bcu.util.entity.data;

import java.util.Set;
import java.util.TreeSet;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.util.basis.Basis;
import com.mandarin.bcu.util.unit.AbEnemy;
import com.mandarin.bcu.util.unit.Enemy;
import com.mandarin.bcu.util.unit.EnemyStore;

public class CustomEnemy extends CustomEntity implements MaskEnemy {

	public Enemy pack;
	public int star, drop;

	public CustomEnemy() {
		rep = new AtkDataModel(this);
		atks = new AtkDataModel[1];
		atks[0] = new AtkDataModel(this);
		width = 320;
		speed = 8;
		hp = 10000;
		hb = 1;
		type = 1;
	}

	public CustomEnemy copy(Enemy e) {
		CustomEnemy ce = new CustomEnemy();
		ce.importData(this);
		ce.pack = e;

		return ce;
	}

	public void fillData(int ver, InStream is) {
		zread(ver, is);
	}

	@Override
	public double getDrop() {
		return drop;
	}

	@Override
	public Enemy getPack() {
		return pack;
	}

	@Override
	public int getStar() {
		return star;
	}

	@Override
	public Set<AbEnemy> getSummon() {
		Set<AbEnemy> ans = new TreeSet<>();
		for (AtkDataModel adm : atks)
			if (adm.proc[P_SUMMON][0] > 0)
				ans.add(EnemyStore.getAbEnemy(adm.proc[P_SUMMON][1], false));
		return ans;
	}

	@Override
	public void importData(MaskEntity de) {
		super.importData(de);
		if (de instanceof MaskEnemy) {
			MaskEnemy me = (MaskEnemy) de;
			star = me.getStar();
			drop = (int) me.getDrop();
		}
	}

	@Override
	public double multi(Basis b) {
		if (star > 0)
			return b.t().getStarMulti(star);
		if ((type & TB_ALIEN) > 0)
			return b.t().getAlienMulti();
		return 1;
	}

	@Override
	public void write(OutStream os) {
		os.writeString("0.4.0");
		super.write(os);
		os.writeByte((byte) star);
		os.writeInt(drop);
	}

	@SuppressWarnings("deprecation")
	private void zread(int val, InStream is) {
		if (val >= 307)
			val = getVer(is.nextString());
		if (val >= 400)
			zread$000400(is);
		else if (val >= 308)
			zread$000308(is);
		else if (val >= 307)
			zread$000307(is);
		else if (val >= 305)
			zread$000305(is);
		else if (val >= 301)
			zread$000301(is);

		// eliminate old stuff
		if (val < 307) {
			if ((abi & AB_MOVEI) > 0)
				rep.proc[P_IMUWAVE][0] = 100;
			if ((abi & AB_GHOST) > 0)
				rep.proc[P_IMUSLOW][0] = 100;
			if ((abi & AB_POII) > 0)
				rep.proc[P_IMUWEAK][0] = 100;
			if ((abi & AB_THEMEI) > 0)
				rep.proc[P_IMUWARP][0] = 100;
			if ((abi & AB_SEALI) > 0)
				rep.proc[P_IMUCURSE][0] = 100;
			if ((abi & AB_TIMEI) > 0)
				rep.proc[P_IMUSTOP][0] = 100;
			if ((abi & AB_SNIPERI) > 0)
				rep.proc[P_IMUKB][0] = 100;
			abi &= AB_ELIMINATOR;
		}
	}

	@Deprecated
	private void zread$000301(InStream is) {
		hp = is.nextInt();
		hb = is.nextInt();
		speed = is.nextByte();
		range = is.nextShort();
		abi = is.nextInt();
		type = is.nextInt();
		width = is.nextShort();
		shield = is.nextInt();
		boolean isrange = is.nextByte() == 1;
		tba = is.nextInt();
		base = is.nextShort();
		star = is.nextByte();
		drop = is.nextInt();
		common = false;
		rep = new AtkDataModel(this, is, "0.3.1");
		int m = is.nextByte();
		AtkDataModel[] set = new AtkDataModel[m];
		for (int i = 0; i < m; i++) {
			set[i] = new AtkDataModel(this, is, "0.3.1");
			set[i].range = isrange;
		}
		int n = is.nextByte();
		atks = new AtkDataModel[n];
		for (int i = 0; i < n; i++)
			atks[i] = set[is.nextByte()];
	}

	@Deprecated
	private void zread$000305(InStream is) {
		hp = is.nextInt();
		hb = is.nextInt();
		speed = is.nextByte();
		range = is.nextShort();
		abi = is.nextInt();
		type = is.nextInt();
		width = is.nextShort();
		shield = is.nextInt();
		boolean isrange = is.nextByte() == 1;
		tba = is.nextInt();
		base = is.nextShort();
		star = is.nextByte();
		drop = is.nextInt();
		common = is.nextByte() == 1;
		rep = new AtkDataModel(this, is, "0.3.5");
		int m = is.nextByte();
		AtkDataModel[] set = new AtkDataModel[m];
		for (int i = 0; i < m; i++) {
			set[i] = new AtkDataModel(this, is, "0.3.5");
			set[i].range = isrange;
		}
		int n = is.nextByte();
		atks = new AtkDataModel[n];
		for (int i = 0; i < n; i++)
			atks[i] = set[is.nextByte()];
	}

	private void zread$000307(InStream is) {
		hp = is.nextInt();
		hb = is.nextInt();
		speed = is.nextByte();
		range = is.nextShort();
		abi = is.nextInt();
		if ((abi & AB_GLASS) > 0)
			loop = 1;
		type = is.nextInt();
		width = is.nextShort();
		shield = is.nextInt();
		boolean isrange = is.nextByte() == 1;
		tba = is.nextInt();
		base = is.nextShort();
		star = is.nextByte();
		drop = is.nextInt();
		common = is.nextByte() == 1;
		rep = new AtkDataModel(this, is);
		int m = is.nextByte();
		AtkDataModel[] set = new AtkDataModel[m];
		for (int i = 0; i < m; i++) {
			set[i] = new AtkDataModel(this, is);
			set[i].range = isrange;
		}
		int n = is.nextByte();
		atks = new AtkDataModel[n];
		for (int i = 0; i < n; i++)
			atks[i] = set[is.nextByte()];
	}

	private void zread$000308(InStream is) {
		zreada$000308(is);
		star = is.nextByte();
		drop = is.nextInt();
	}

	private void zread$000400(InStream is) {
		zreada(is);
		star = is.nextByte();
		drop = is.nextInt();
	}

}
