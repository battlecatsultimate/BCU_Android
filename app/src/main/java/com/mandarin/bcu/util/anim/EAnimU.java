package com.mandarin.bcu.util.anim;

import com.mandarin.bcu.util.system.P;
import com.mandarin.bcu.util.system.fake.FakeGraphics;

public class EAnimU extends EAnimD {

	public int type;

	protected EAnimU(AnimU ani, int i) {
		super(ani, ani.mamodel, ani.anims[i]);
		type = i;
	}

	@Override
	public AnimU anim() {
		return (AnimU) a;
	}

	/** change the animation state, for entities only */
	public void changeAnim(int t) {
		if (t >= anim().anims.length)
			return;
		f = -1;
		ma = anim().anims[t];
		type = t;
	}

	@Override
	public void draw(FakeGraphics g, P ori, double siz) {
		if (f == -1) {
			f = 0;
			setup();
		}
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

	/** make this animation a component of another, used in warp and kb */
	public void paraTo(EAnimD base) {
		if (base == null)
			ent[0].setPara(null);
		else
			ent[0].setPara(base.ent[1]);
	}

}
