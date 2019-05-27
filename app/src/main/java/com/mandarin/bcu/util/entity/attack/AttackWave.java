package com.mandarin.bcu.util.entity.attack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mandarin.bcu.util.entity.AbEntity;
import com.mandarin.bcu.util.entity.Entity;

public class AttackWave extends AttackAb {

	protected final Set<Entity> incl;

	public AttackWave(AttackSimple a, double p0, double wid, int wt) {
		super(a, p0, wid);
		waveType = wt;
		incl = new HashSet<>();
		proc[P_WAVE][0]--;
	}

	public AttackWave(AttackWave a, double p0, double wid) {
		super(a, p0, wid);
		waveType = a.waveType;
		incl = a.incl;
		proc[P_WAVE][0]--;
	}

	@Override
	public void capture() {
		List<AbEntity> le = model.b.inRange(touch, dire, sta, end);
		if (incl != null)
			le.removeIf(e -> incl.contains(e));
		capt.clear();
		if ((abi & AB_ONLY) == 0)
			capt.addAll(le);
		else
			for (AbEntity e : le)
				if (e.targetable(type))
					capt.add(e);
	}

	@Override
	public void excuse() {
		for (AbEntity e : capt) {
			if (e.isBase())
				continue;
			if (e instanceof Entity) {
				e.damaged(this);
				incl.add((Entity) e);
			}
		}
	}

}
