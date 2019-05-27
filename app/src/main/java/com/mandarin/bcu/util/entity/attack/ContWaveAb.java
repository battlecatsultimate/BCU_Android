package com.mandarin.bcu.util.entity.attack;

import com.mandarin.bcu.util.ImgCore;
import com.mandarin.bcu.util.anim.EAnimD;
import com.mandarin.bcu.util.entity.AbEntity;
import com.mandarin.bcu.util.entity.Entity;
import com.mandarin.bcu.util.system.P;
import com.mandarin.bcu.util.system.fake.FakeGraphics;
import com.mandarin.bcu.util.system.fake.FakeTransform;

public abstract class ContWaveAb extends ContAb {

	protected final AttackWave atk;
	protected final EAnimD anim;
	private int t = 0;
	private int maxt;
	private boolean tempAtk;

	protected ContWaveAb(AttackWave a, double p, EAnimD ead, int layer) {
		super(a.model.b, p, layer);
		atk = a;
		anim = ead;
		maxt = anim.len();
	}

	@Override
	public void draw(FakeGraphics gra, P p, double siz) {
		FakeTransform at = gra.getTransform();
		anim.draw(gra, p, siz);
		gra.setTransform(at);
		drawAxis(gra, p, siz);
	}

	@Override
	public void update() {
		tempAtk = false;
		if (t == W_TIME) {
			atk.capture();
			for (AbEntity e : atk.capt)
				if ((e.getAbi() & AB_WAVES) > 0) {
					if (e instanceof Entity)
						((Entity) e).anim.getEff(STPWAVE);
					activate = false;
					return;
				}
			sb.getAttack(atk);
			tempAtk = true;
			if (atk.getProc(P_WAVE)[0] > 0)
				nextWave();
		}
		if (maxt == t)
			activate = false;
		anim.update(false);
		t++;
	}

	protected void drawAxis(FakeGraphics gra, P p, double siz) {
		if (!ImgCore.ref)
			return;

		// after this is the drawing of hit boxes
		siz *= 1.25;
		double rat = 1;// BattleBox.BBPainter.ratio;
		int h = (int) (640 * rat * siz);
		gra.setColor(FakeGraphics.MAGENTA);
		double d0 = Math.min(atk.sta, atk.end);
		double ra = Math.abs(atk.sta - atk.end);
		int x = (int) ((d0 - pos) * rat * siz + p.x);
		int y = (int) p.y;
		int w = (int) (ra * rat * siz);
		if (tempAtk)
			gra.fillRect(x, y, w, h);
		else
			gra.drawRect(x, y, w, h);
	}

	/** generate the next wave container */
	protected abstract void nextWave();

}
