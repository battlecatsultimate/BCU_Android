package com.mandarin.bcu.util.stage;

import com.mandarin.bcu.util.Data;
import com.mandarin.bcu.util.system.BasedCopable;

public class SCGroup extends Data implements BasedCopable<SCGroup, Integer> {

	public int id, max;

	public SCGroup(int ID, int n) {
		id = ID;
		max = n;
	}

	@Override
	public SCGroup copy(Integer id) {
		return new SCGroup(id, max);
	}

	@Override
	public String toString() {
		return trio(id) + " - " + max;
	}

}
