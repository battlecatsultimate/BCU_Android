package com.mandarin.bcu.util.entity;

import com.mandarin.bcu.util.BattleObj;

public class EntCont extends BattleObj {

	public Entity ent;

	public int t;

	public EntCont(Entity e, int time) {
		ent = e;
		t = time;
	}

	public void update() {
		if (t > 0)
			t--;
	}

}
