package com.mandarin.bcu.util.anim;

import com.mandarin.bcu.util.system.P;
import com.mandarin.bcu.util.system.fake.FakeGraphics;

public class EAnimS extends EAnimI {

	public EAnimS(AnimI ia, MaModel mm) {
		super(ia, mm);
	}

	@Override
	public void draw(FakeGraphics g, P ori, double siz) {
		set(g);
		g.translate(ori.x, ori.y);
		if (ref && !battle) {
			P p0 = new P(-200, 0).times(siz);
			P p1 = new P(400, 100).times(siz);
			P p2 = new P(0, -300).times(siz);
			g.drawRect((int) p0.x, (int) p0.y, (int) p1.x, (int) p1.y);
			g.setColor(FakeGraphics.RED);
			g.drawLine(0, 0, (int) p2.x, (int) p2.y);
		}
		for (EPart e : order)
			e.drawPart(g, new P(siz, siz));
		if (sele >= 0 && sele < ent.length)
			ent[sele].drawScale(g, new P(siz, siz));
	}

	@Override
	public int ind() {
		return 0;
	}

	@Override
	public int len() {
		return 0;
	}

	@Override
	public void setTime(int value) {
	}

	@Override
	public void update(boolean b) {
	}

}
