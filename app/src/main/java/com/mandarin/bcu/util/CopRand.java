package com.mandarin.bcu.util;

import java.util.Random;

public class CopRand extends BattleObj {

	private long seed;

	public CopRand(long s) {
		seed = s;
	}

	public double nextDouble() {
		Random r = new Random(seed);
		seed = r.nextLong();
		return r.nextDouble();
	}

}
