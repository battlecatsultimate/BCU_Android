package com.mandarin.bcu.util.basis;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.util.Data;

public class Treasure extends Data {

	public final Basis b;
	public int[] tech = new int[LV_TOT];
	public int[] trea = new int[T_TOT];
	public int[] bslv = new int[BASE_TOT];
	public int[] fruit = new int[7], gods = new int[3];
	public int alien, star;

	/** new Treasure object */
	protected Treasure(Basis bas) {
		b = bas;
		zread$000000();
	}

	/** read Treasure from data */
	protected Treasure(Basis bas, int ver, InStream is) {
		b = bas;
		zread(ver, is);
	}

	/** copy Treasure object */
	protected Treasure(Basis bas, Treasure t) {
		b = bas;
		tech = t.tech.clone();
		trea = t.trea.clone();
		fruit = t.fruit.clone();
		gods = t.gods.clone();
		alien = t.alien;
		star = t.star;
		bslv = t.bslv.clone();
	}

	/** get multiplication of non-starred alien */
	public double getAlienMulti() {
		return 7 - alien * 0.01;
	}

	/** get cat attack multiplication */
	public double getAtkMulti() {
		double ini = 1 + trea[T_ATK] * 0.005;
		double com = 1 + b.getInc(C_ATK) * 0.01;
		return ini * com;
	}

	/** get base health */
	public int getBaseHealth() {
		int t = tech[LV_BASE];
		int base = t < 6 ? t * 1000 : t < 8 ? 5000 + (t - 5) * 2000 : 9000 + (t - 7) * 3000;
		base += trea[T_BASE] * 70;
		if (bslv[0] > 10)
			base += 36000 + 4000 * (bslv[0] - 10);
		else
			base += 3600 * bslv[0];
		return base * (100 + b.getInc(C_BASE)) / 100;
	}

	/** get normal canon attack */
	public int getCanonAtk() {
		int base = 50 + tech[LV_CATK] * 50 + trea[T_CATK] * 5;
		return base * (100 + b.getInc(C_C_ATK)) / 100;
	}

	/** get special canon data 1 */
	public double getCanonMulti(int type) {
		if (type == 2)
			if (bslv[2] > 10)
				return 298 + 3.2 * (bslv[2] - 10);
			else
				return 100 + 19.8 * bslv[2];
		else if (type == 3)
			if (bslv[3] > 10)
				return 39 + 0.9 * (bslv[3] - 10);
			else
				return 30 + 0.9 * bslv[3];
		else if (type == 4)
			if (bslv[4] > 10)
				return 200 + 15 * (bslv[4] - 10);
			else
				return 110 + 9 * bslv[4];
		else if (type == 5)
			if (bslv[5] > 10)
				return 25 + 7.5 * (bslv[5] - 10);
			else
				return 5 + 2 * bslv[5];
		else if (type == -5)
			if (bslv[5] > 10)
				return 150 + 15 * (bslv[5] - 10);
			else
				return 100 + 5 * bslv[5];
		else if (type == 6)
			if (bslv[6] > 10)
				return 50 + 5 * (bslv[6] - 10);
			else
				return 30 + 2 * bslv[6];
		return 0;
	}

	/** get special canon data 2, usually proc time */
	public int getCanonProcTime(int type) {
		if (type == 1)
			if (bslv[1] > 10)
				return 50 + 5 * (bslv[1] - 10);
			else
				return 30 + 2 * bslv[1];
		else if (type == 2)
			if (bslv[2] > 10)
				return 90 + 9 * (bslv[2] - 10) / 2;
			else
				return 60 + 3 * bslv[2];
		else if (type == 3)
			if (bslv[3] > 10)
				return 30 + 3 * (bslv[3] - 10);
			else
				return 15 + 3 * bslv[3] / 2;
		else if (type == 5)
			if (bslv[5] > 10)
				return 45 + 3 * (bslv[3] - 10) / 2;
			else
				return 30 + 3 * bslv[3] / 2;
		else if (type == 6)
			if (bslv[6] > 10)
				return 250 + 15 * (bslv[6] - 10);
			else
				return 200 + 5 * bslv[6];
		return 0;
	}

	/** get cat health multiplication */
	public double getDefMulti() {
		double ini = 1 + trea[T_DEF] * 0.005;
		double com = 1 + b.getInc(C_DEF) * 0.01;
		return ini * com;
	}

	/** get accounting multiplication */
	public double getDropMulti() {
		return (0.95 + 0.05 * tech[LV_ACC] + 0.005 * trea[T_ACC]) * (1 + b.getInc(C_MEAR) * 0.01);
	}

	/** get EVA kill ability attack multiplication */
	public double getEKAtk() {
		return 0.05 * (100 + b.getInc(C_EKILL));
	}

	/** get EVA kill ability reduce damage multiplication */
	public double getEKDef() {
		return 20.0 / (100 + b.getInc(C_EKILL));
	}

	/** get processed cat cool down time */
	public int getFinRes(int ori) {
		double dec = 6 - tech[LV_RES] * 6 - trea[T_RES] * 0.3 - b.getInc(C_RESP);
		return (int) Math.max(60, ori + 10 + dec);
	}

	/** get maximum fruit of certain trait bitmask */
	public double getFruit(int type) {
		double ans = 0;
		if ((type & TB_RED) != 0)
			ans = Math.max(ans, fruit[T_RED]);
		if ((type & TB_BLACK) != 0)
			ans = Math.max(ans, fruit[T_BLACK]);
		if ((type & TB_ANGEL) != 0)
			ans = Math.max(ans, fruit[T_ANGEL]);
		if ((type & TB_FLOAT) != 0)
			ans = Math.max(ans, fruit[T_FLOAT]);
		if ((type & TB_ALIEN) != 0)
			ans = Math.max(ans, fruit[T_ALIEN]);
		if ((type & TB_METAL) != 0)
			ans = Math.max(ans, fruit[T_METAL]);
		if ((type & TB_ZOMBIE) != 0)
			ans = Math.max(ans, fruit[T_ZOMBIE]);
		return ans * 0.01;
	}

	/** get attack multiplication from strong against ability */
	public double getGOODATK(int type) {
		double ini = 1.5 * (1 + 0.2 / 3 * getFruit(type));
		double com = 1 + b.getInc(C_GOOD) * 0.01;
		return ini * com;
	}

	/** get damage reduce multiplication from strong against ability */
	public double getGOODDEF(int type) {
		double ini = 0.5 - 0.1 / 3 * getFruit(type);
		double com = 1 - b.getInc(C_GOOD) * 0.01;
		return ini * com;
	}

	/** get attack multiplication from massive damage ability */
	public double getMASSIVEATK(int type) {
		double ini = 3 + 1.0 / 3 * getFruit(type);
		double com = 1 + b.getInc(C_MASSIVE) * 0.01;
		return ini * com;
	}

	/** get attack multiplication from super massive damage ability */
	public double getMASSIVESATK(int type) {
		double ini = 5 + 1.0 / 3 * getFruit(type);
		return ini;
	}

	/** get damage reduce multiplication from resistant ability */
	public double getRESISTDEF(int type) {
		double ini = 0.25 - 0.05 / 3 * getFruit(type);
		double com = 1 - b.getInc(C_RESIST) * 0.01;
		return ini * com;
	}

	/** get damage reduce multiplication from super resistant ability */
	public double getRESISTSDEF(int type) {
		double ini = 1.0 / 6 - 1.0 / 126 * getFruit(type);
		return ini;
	}

	/** get reverse cat cool down time */
	public int getRevRes(int res) {
		if (res < 60)
			res = 60;
		double dec = 6 - tech[LV_RES] * 6 - trea[T_RES] * 0.3 - b.getInc(C_RESP);
		return (int) (res - 10 - dec);

	}

	/** get multiplication of starred enemy */
	public double getStarMulti(int st) {
		if (st == 1)
			return 16 - star * 0.01;
		else
			return 11 - 0.1 * gods[st - 2];
	}

	/** get witch kill ability attack multiplication */
	public double getWKAtk() {
		return 0.05 * (100 + b.getInc(C_WKILL));
	}

	/** get witch kill ability reduce damage multiplication */
	public double getWKDef() {
		return 10.0 / (100 + b.getInc(C_WKILL));
	}

	/** get canon recharge time */
	protected int CanonTime(int map) {
		int base = 1503 + 50 * (tech[LV_CATK] - tech[LV_RECH]);
		if (trea[T_RECH] <= 300)
			base -= (int) (1.5 * trea[T_RECH]);
		else
			base -= 3 * trea[T_RECH] - 450;
		base -= b.getInc(C_C_SPE);
		base = Math.max(950, base + map * 450);
		return base;
	}

	/** get the cost to upgrade worker cat */
	protected int getLvCost(int lv) {
		int t = tech[LV_WORK];
		int base = t < 8 ? 30 + 10 * t : 20 * t - 40;
		return base * lv;
	}

	/** get wallet capacity */
	protected int getMaxMon(int lv) {
		int base = Math.max(25, 50 * tech[LV_WALT]);
		base = base * (1 + lv);
		base += trea[T_WALT] * 10;
		return base * (100 + b.getInc(C_M_MAX)) / 100;
	}

	/** get money increase rate */
	protected double getMonInc(int lv) {
		return (0.15 + 0.1 * tech[LV_WORK]) * (1 + (lv - 1) * 0.1) + trea[T_WORK] * 0.01;
	}

	/** save data to file */
	protected void write(OutStream os) {
		os.writeString("0.4.0");
		os.writeIntB(tech);
		os.writeIntB(trea);
		os.writeInt(alien);
		os.writeInt(star);
		os.writeIntB(fruit);
		os.writeIntB(gods);
		os.writeIntB(bslv);
	}

	/** read date from file, support multiple versions */
	private void zread(int val, InStream is) {
		zread$000000();

		if (val >= 305)
			val = getVer(is.nextString());

		if (val >= 400)
			zread$000400(is);
		else if (val >= 305)
			zread$000305(is);
		else if (val >= 304)
			zread$000304(is);
		else if (val >= 301)
			zread$000301(is);
		else if (val >= 203)
			zread$000203(is);
	}

	private void zread$000000() {
		for (int i = 0; i < LV_TOT; i++)
			tech[i] = MLV[i];
		for (int i = 0; i < T_TOT; i++)
			trea[i] = MT[i];
		fruit[T_RED] = fruit[T_BLACK] = fruit[T_FLOAT] = fruit[T_ANGEL] = 300;
		fruit[T_METAL] = fruit[T_ZOMBIE] = fruit[T_ALIEN] = 300;
		for (int i = 0; i < BASE_TOT; i++)
			bslv[i] = 20;
		gods[0] = gods[1] = gods[2] = 100;
		alien = 600;
		star = 1500;
	}

	private void zread$000203(InStream is) {
		for (int i = 0; i < 8; i++)
			tech[i] = is.nextByte();
		for (int i = 0; i < 9; i++)
			trea[i] = is.nextShort();
		alien = is.nextInt();
		star = is.nextInt();
		fruit = is.nextIntsB();
		gods = is.nextIntsB();
	}

	private void zread$000301(InStream is) {
		zread$000203(is);
		for (int i = 0; i < 5; i++)
			bslv[i] = is.nextByte();
	}

	private void zread$000304(InStream is) {
		zread$000203(is);
		for (int i = 0; i < 6; i++)
			bslv[i] = is.nextByte();

	}

	private void zread$000305(InStream is) {
		zread$000203(is);
		int[] temp = is.nextIntsB();
		for (int i = 0; i < temp.length; i++)
			bslv[i] = temp[i];
	}

	private void zread$000400(InStream is) {
		int[] lv = is.nextIntsB();
		int[] tr = is.nextIntsB();
		for (int i = 0; i < Math.min(LV_TOT, lv.length); i++)
			tech[i] = lv[i];
		for (int i = 0; i < Math.min(T_TOT, tr.length); i++)
			trea[i] = tr[i];
		alien = is.nextInt();
		star = is.nextInt();
		fruit = is.nextIntsB();
		gods = is.nextIntsB();
		int[] bs = is.nextIntsB();
		for (int i = 0; i < bs.length; i++)
			bslv[i] = bs[i];
	}

}
