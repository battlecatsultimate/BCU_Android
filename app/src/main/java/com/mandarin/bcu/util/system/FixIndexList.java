package com.mandarin.bcu.util.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import com.mandarin.bcu.util.Data;

public class FixIndexList<T> extends Data {

	private final T[] arr;
	private int size = 0;

	public FixIndexList(T[] ar) {
		arr = ar;
		int n = 0;
		for (T t : arr)
			if (t != null)
				n++;
		size = n;
	}

	public void add(T t) {
		arr[nextInd()] = t;
		if (t != null)
			size++;
	}

	public void clear() {
		for (int i = 0; i < arr.length; i++)
			arr[i] = null;
		size = 0;
	}

	public boolean contains(T t) {
		if (t == null)
			return false;
		for (T a : arr)
			if (t.equals(a))
				return true;
		return false;
	}

	public void forEach(BiConsumer<Integer, T> c) {
		for (int i = 0; i < arr.length; i++)
			if (arr[i] != null)
				c.accept(i, arr[i]);
	}

	public T get(int ind) {
		if (ind < 0 || ind >= arr.length)
			return null;
		return arr[ind];
	}

	public int getFirstInd() {
		if (size == 0)
			return -1;
		for (int i = 0; i < arr.length; i++)
			if (arr[i] != null)
				return i;
		return -1;
	}

	public List<T> getList() {
		List<T> ans = new ArrayList<>(size);
		for (T t : arr)
			if (t != null)
				ans.add(t);
		return ans;
	}

	public Map<Integer, T> getMap() {
		Map<Integer, T> map = new TreeMap<>();
		for (int i = 0; i < arr.length; i++)
			if (arr[i] != null)
				map.put(i, arr[i]);
		return map;
	}

	public int indexOf(T tar) {
		for (int i = 0; i < arr.length; i++)
			if (arr[i] != null && arr[i].equals(tar))
				return i;
		return -1;
	}

	public int nextInd() {
		for (int i = 0; i < arr.length; i++)
			if (arr[i] == null)
				return i;
		return -1;
	}

	public void remove(T t) {
		if (t == null)
			return;
		for (int i = 0; i < arr.length; i++)
			if (arr[i] == t) {
				arr[i] = null;
				size--;
			}
	}

	public void set(int ind, T t) {
		if (arr[ind] != null)
			size--;
		if (t != null)
			size++;
		arr[ind] = t;

	}

	public int size() {
		return size;
	}

}
