package com.mandarin.bcu.util.entity;

import com.mandarin.bcu.util.anim.EAnimD;
import com.mandarin.bcu.util.anim.EAnimU;
import com.mandarin.bcu.util.pack.EffAnim;
import com.mandarin.bcu.util.system.P;
import com.mandarin.bcu.util.system.fake.FakeGraphics;
import com.mandarin.bcu.util.system.fake.FakeTransform;

public class WaprCont extends EAnimCont {

	private final EAnimU ent;
	private final EAnimD chara;

	public WaprCont(double p, int pa, int layer, EAnimU a) {
		super(p, layer, EffAnim.effas[A_W].getEAnim(pa));
		ent = a;
		chara = EffAnim.effas[A_W_C].getEAnim(pa);
	}

	@Override
	public void draw(FakeGraphics gra, P p, double psiz) {
		FakeTransform at = gra.getTransform();
		super.draw(gra, p, psiz);
		gra.setTransform(at);
		ent.paraTo(chara);
		ent.draw(gra, p, psiz);
		ent.paraTo(null);
	}

	@Override
	public void update() {
		super.update();
		chara.update(false);
	}

}
