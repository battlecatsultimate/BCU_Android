package com.mandarin.bcu.util.entity.data;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.util.unit.Form;

public class CustomUnit extends CustomEntity implements MaskUnit {

	public Form pack;
	public int price, resp;

	public CustomUnit() {
		rep = new AtkDataModel(this);
		atks = new AtkDataModel[1];
		atks[0] = new AtkDataModel(this);
		width = 320;
		speed = 8;
		hp = 1000;
		hb = 1;
		type = 0;
		price = 50;
		resp = 60;
	}

	public void fillData(int ver, InStream is) {
		zread(ver, is);
	}

	@Override
	public int getBack() {
		return 9;
	}

	@Override
	public int getFront() {
		return 0;
	}

	@Override
	public Form getPack() {
		return pack;
	}

	@Override
	public int getPrice() {
		return price;
	}

	@Override
	public int getRespawn() {
		return resp;
	}

	@Override
	public void importData(MaskEntity de) {
		super.importData(de);
		if (de instanceof MaskUnit) {
			MaskUnit mu = (MaskUnit) de;
			price = mu.getPrice();
			resp = mu.getRespawn();
		}
	}

	@Override
	public void write(OutStream os) {
		os.writeString("0.4.0");
		super.write(os);
		os.writeInt(price);
		os.writeInt(resp);
	}

	private void zread(int val, InStream is) {
		val = getVer(is.nextString());
		if (val >= 400)
			zread$000400(is);
		else if (val >= 308)
			zread$000308(is);
		else if (val >= 307)
			zread$000307(is);

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
		price = is.nextInt();
		resp = is.nextInt();
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
		price = is.nextInt();
		resp = is.nextInt();
	}

	private void zread$000400(InStream is) {
		zreada(is);
		price = is.nextInt();
		resp = is.nextInt();
	}

}
