package com.mandarin.bcu.util.system;

import java.util.HashMap;
import java.util.Map;

public interface BasedCopable<T, B> extends Cloneable, Copable<T> {

	public static Map<Class<?>, Object> map = new HashMap<>();

	@Override
	@SuppressWarnings("unchecked")
	public default T copy() {
		B base = (B) map.get(getClass());
		if (base == null)
			return null;
		return copy(base);
	}

	public T copy(B b);

}
