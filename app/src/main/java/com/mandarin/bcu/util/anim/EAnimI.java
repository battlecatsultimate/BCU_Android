package com.mandarin.bcu.util.anim;

import java.util.ArrayList;
import java.util.List;

import com.mandarin.bcu.util.BattleObj;
import com.mandarin.bcu.util.system.P;
import com.mandarin.bcu.util.system.fake.FakeGraphics;

public abstract class EAnimI extends BattleObj {

	public int sele = -1;
	public EPart[] ent = null;

	protected final AnimI a;
	protected final MaModel mamodel;
	protected List<EPart> order;

	public EAnimI(AnimI ia, MaModel mm) {
		a = ia;
		mamodel = mm;
		organize();
	}

	public AnimI anim() {
		return a;
	}

	public abstract void draw(FakeGraphics g, P ori, double siz);

	public abstract int ind();

	public abstract int len();

	public void organize() {
		ent = mamodel.arrange(this);
		order = new ArrayList<>();
		for (EPart e : ent) {
			e.ea = this;
			order.add(e);
		}
		order.sort(null);
	}

	public abstract void setTime(int value);

	public abstract void update(boolean b);

	@Override
	protected void performDeepCopy() {
		((EAnimI) copy).organize();
	}

	@Override
	protected void terminate() {
		copy = null;
	}

}
