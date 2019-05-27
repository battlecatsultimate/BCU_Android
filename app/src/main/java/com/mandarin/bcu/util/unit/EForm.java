package com.mandarin.bcu.util.unit;

import com.mandarin.bcu.util.Data;
import com.mandarin.bcu.util.basis.StageBasis;
import com.mandarin.bcu.util.entity.EUnit;
import com.mandarin.bcu.util.entity.data.MaskUnit;
import com.mandarin.bcu.util.entity.data.PCoin;

public class EForm extends Data {

	private final Form f;
	private final int[] lvs;

	public final MaskUnit du;

	public EForm(Form form, int... level) {
		f = form;
		lvs = level;
		PCoin pc = f.getPCoin();
		if (pc != null)
			du = pc.improve(lvs);
		else
			du = form.du;
	}

	public EUnit getEntity(StageBasis b) {
		double d = f.unit.lv.getMult(lvs[0]);
		EUnit e = new EUnit(b, du, f.getEAnim(0), d);
		return e;
	}

	public int getPrice(int sta) {
		return (int) (du.getPrice() * (1 + sta * 0.5));
	}

}
