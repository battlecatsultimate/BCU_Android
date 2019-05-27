package com.mandarin.bcu.util.entity.attack;

import com.mandarin.bcu.util.pack.NyCastle;
import com.mandarin.bcu.util.system.P;
import com.mandarin.bcu.util.system.fake.FakeGraphics;

public class ContWaveCanon extends ContWaveAb {

	private final int canid;

	public ContWaveCanon(AttackWave a, double p, int id) {
		super(a, p, NyCastle.atks[id].getEAnim(1), 9);
		canid = id;
	}

	@Override
	public void draw(FakeGraphics gra, P p, double psiz) {
		drawAxis(gra, p, psiz);
		if (canid == 0)
			psiz *= 1.25;
		else
			psiz *= 0.5 * 1.25;
		P pus = canid == 0 ? new P(9, 40) : new P(-72, 0);
		anim.draw(gra, p.plus(pus, -psiz), psiz * 2);
	}

	public double getSize() {
		return 2.5;
	}

	@Override
	protected void nextWave() {
		double np = pos - 405;
		new ContWaveCanon(new AttackWave(atk, np, NYRAN[canid]), np, canid);
	}

}
