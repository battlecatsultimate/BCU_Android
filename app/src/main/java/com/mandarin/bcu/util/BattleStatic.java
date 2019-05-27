package com.mandarin.bcu.util;

public interface BattleStatic {

	/**
	 * designed to prevent a class from extending BattleObj and implementing
	 * BattleStatic
	 */
	public default void conflict() {
	}

}
