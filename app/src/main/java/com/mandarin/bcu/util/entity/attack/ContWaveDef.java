package com.mandarin.bcu.util.entity.attack;

public class ContWaveDef extends ContWaveAb {

	protected ContWaveDef(AttackWave a, double p, int layer) {
		super(a, p, a.model.b.bg.getEAnim((1 - a.model.getDire()) / 2 + 1), layer);
	}

	@Override
	protected void nextWave() {
		int dire = atk.model.getDire();
		double np = pos + W_PROG * dire;
		int wid = dire == 1 ? W_E_WID : W_U_WID;
		new ContWaveDef(new AttackWave(atk, np, wid), np, layer);
	}

}
