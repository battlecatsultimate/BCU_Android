package com.mandarin.bcu.util.entity.data;

import com.mandarin.bcu.io.Reader;
import com.mandarin.bcu.main.Printer;
import com.mandarin.bcu.util.unit.Form;
import com.mandarin.bcu.util.unit.Unit;

public class DataUnit extends DefaultData implements MaskUnit, Cloneable {

	private final Form form;
	private final Unit unit;
	public int price, respawn;
	private int front, back;

	public PCoin pcoin = null;

	public DataUnit(Form f, Unit u, String[] data) {
		form = f;
		unit = u;
		int[] ints = new int[data.length];
		for (int i = 0; i < data.length; i++)
			ints[i] = Reader.parseIntN(data[i]);
		hp = ints[0];
		hb = ints[1];
		speed = ints[2];
		atk = ints[3];
		tba = ints[4];
		range = ints[5];
		price = ints[6];
		respawn = ints[7] * 2;
		if (ints[8] != 0)
			Printer.p("DataUnit", 30, unit.id + "-new 0: " + ints[8]);
		width = ints[9];
		int t = 0;
		if (ints[10] == 1)
			t |= TB_RED;
		if (ints[11] != 0)
			Printer.p("DataUnit", 40, unit.id + "new 2: " + ints[11]);
		isrange = ints[12] == 1;
		pre = ints[13];
		front = ints[14];
		back = ints[15];
		if (ints[16] == 1)
			t |= TB_FLOAT;
		if (ints[17] == 1)
			t |= TB_BLACK;
		if (ints[18] == 1)
			t |= TB_METAL;
		if (ints[19] == 1)
			t |= TB_WHITE;
		if (ints[20] == 1)
			t |= TB_ANGEL;
		if (ints[21] == 1)
			t |= TB_ALIEN;
		if (ints[22] == 1)
			t |= TB_ZOMBIE;
		int a = 0;
		if (ints[23] == 1)
			a |= AB_GOOD;
		proc = new int[PROC_TOT][PROC_WIDTH];
		proc[P_KB][0] = ints[24];
		proc[P_STOP][0] = ints[25];
		proc[P_STOP][1] = ints[26];
		proc[P_SLOW][0] = ints[27];
		proc[P_SLOW][1] = ints[28];
		if (ints[29] == 1)
			a |= AB_RESIST;
		if (ints[30] == 1)
			a |= AB_MASSIVE;
		proc[P_CRIT][0] = ints[31];
		if (ints[32] == 1)
			a |= AB_ONLY;
		if (ints[33] == 1)
			a |= AB_EARN;
		if (ints[34] == 1)
			a |= AB_BASE;
		proc[P_WAVE][0] = ints[35];
		proc[P_WAVE][1] = ints[36];
		proc[P_WEAK][0] = ints[37];
		proc[P_WEAK][1] = ints[38];
		proc[P_WEAK][2] = ints[39];
		proc[P_STRONG][0] = ints[40];
		proc[P_STRONG][1] = ints[41];
		proc[P_LETHAL][0] = ints[42];
		if (ints[43] == 1)
			a |= AB_METALIC;
		lds = ints[44];
		ldr = ints[45];

		if (ints[46] == 1)
			proc[P_IMUWAVE][0] = 100;
		if (ints[47] == 1)
			a |= AB_WAVES;
		if (ints[48] == 1)
			proc[P_IMUKB][0] = 100;
		if (ints[49] == 1)
			proc[P_IMUSTOP][0] = 100;
		if (ints[50] == 1)
			proc[P_IMUSLOW][0] = 100;
		if (ints[51] == 1)
			proc[P_IMUWEAK][0] = 100;
		try {
			if (ints[52] == 1)
				a |= AB_ZKILL;
			if (ints[53] == 1)
				a |= AB_WKILL;
			if (ints[54] != 0)
				Printer.p("DataUnit", 79, unit.id + "-new 3: " + ints[54]);
			loop = ints[55];
			if (ints[56] != 0)
				a |= AB_IMUSW;
			if (ints[57] != -1)
				Printer.p("DataUnit", 86, unit.id + "-new 6: " + ints[57]);
			if (ints[58] == 2)
				a |= AB_GLASS;
			atk1 = ints[59];
			atk2 = ints[60];
			pre1 = ints[61];
			pre2 = ints[62];
			abi0 = ints[63];
			abi1 = ints[64];
			abi2 = ints[65];
			if (ints[66] != -1)
				Printer.p("DataUnit", 98, unit.id + "-new 8: " + ints[66]);
			death = ints[67];
			if (ints[68] != 0)
				Printer.p("DataUnit", 101, unit.id + "-new 9: " + ints[68]);
			if (ints[69] != 0)
				Printer.p("DataUnit", 103, unit.id + "-new 10: " + ints[69]);
			proc[P_BREAK][0] = ints[70];
			if (ints[71] != 0)
				Printer.p("DataUnit", 106, unit.id + "-new 11: " + ints[71]);
			if (ints[72] != 0)
				Printer.p("DataUnit", 108, unit.id + "-new 12: " + ints[72]);
			if (ints[73] != 0)
				Printer.p("DataUnit", 110, unit.id + "-new 13: " + ints[73]);
			if (ints[74] != 0)
				Printer.p("DataUnit", 112, unit.id + "-new 14: " + ints[74]);
			if (ints[75] == 1)
				proc[P_IMUWARP][0] = 100;
			if (ints[76] != 0)
				Printer.p("DataUnit", 115, unit.id + "-new 15: " + ints[76]);
			if (ints[77] == 1)
				a |= AB_EKILL;
			if (ints[78] == 1)
				t |= TB_RELIC;
			if (ints[79] == 1)
				proc[P_IMUCURSE][0] = 100;
			if (ints[80] == 1)
				a |= AB_RESISTS;
			if (ints[81] == 1)
				a |= AB_MASSIVES;

		} catch (IndexOutOfBoundsException e) {
		}

		type = t;
		abi = a;
	}

	@Override
	public int getBack() {
		return back;
	}

	@Override
	public int getFront() {
		return front;
	}

	@Override
	public Form getPack() {
		return form;
	}

	@Override
	public int getPrice() {
		return price;
	}

	@Override
	public int getRespawn() {
		return respawn;
	}

	@Override
	protected DataUnit clone() {
		DataUnit ans;
		try {
			ans = (DataUnit) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		ans.proc = new int[PROC_TOT][];
		for (int i = 0; i < PROC_TOT; i++)
			ans.proc[i] = proc[i].clone();
		return ans;
	}

}
