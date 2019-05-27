package com.mandarin.bcu.util.entity.attack;

import com.mandarin.bcu.util.BattleObj;
import com.mandarin.bcu.util.basis.StageBasis;
import com.mandarin.bcu.util.system.P;
import com.mandarin.bcu.util.system.fake.FakeGraphics;

public abstract class ContAb extends BattleObj {

	protected final StageBasis sb;

	public double pos;
	public boolean activate = true;
	public int layer;

	protected ContAb(StageBasis b, double p, int lay) {
		sb = b;
		pos = p;
		layer = lay;
		sb.tlw.add(this);
	}

	public abstract void draw(FakeGraphics gra, P p, double psiz);

	public abstract void update();

}
