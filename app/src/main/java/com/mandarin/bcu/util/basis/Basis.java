package com.mandarin.bcu.util.basis;

import com.mandarin.bcu.util.Data;

public abstract class Basis extends Data {

	public String name;

	/** get combo effect data */
	public abstract int getInc(int type);

	/** get Treasure object */
	public abstract Treasure t();

	@Override
	public String toString() {
		return name;
	}

}
