package com.mandarin.bcu.util.system.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

public class VFile<T extends FileData> implements Comparable<VFile<T>> {

	public static final VFileRoot<AssetData> root = new VFileRoot<>(".");

	public static VFile<AssetData> get(String str) {
		return root.find(str);
	}

	public static VFile<? extends FileData> getFile(String path) {
		if (path.startsWith("./org/"))
			return root.find(path);
		if (path.startsWith("./res/")) {
			File f = new File(path);
			if (!f.exists())
				return null;
			return new VFile<FDFile>(null, f.getName(), new FDFile(f));
		}
		if(path.startsWith("./lang/"))
			return root.find(path);
		return null;
	}

	public static Queue<String> readLine(String str) {
		return getFile(str).getData().readLine();
	}

	public String name;

	protected final VFile<T> parent;

	private final List<VFile<T>> subs;

	private final T data;

	public int mark;

	/** constructor for root directory */
	protected VFile(String str) {
		this(null, str);
	}

	/** constructor for directory */
	protected VFile(VFile<T> par, String str) {
		parent = par;
		name = str;
		subs = new ArrayList<VFile<T>>();
		data = null;
		if (parent != null)
			parent.subs.add(this);
	}

	/** constructor for data file */
	protected VFile(VFile<T> par, String str, T fd) {
		parent = par;
		name = str;
		subs = null;
		data = fd;
		if (parent != null)
			parent.subs.add(this);
	}

	@Override
	public int compareTo(VFile<T> o) {
		return name.compareTo(o.name);
	}

	public int countSubDire() {
		int ans = 0;
		for (VFile<T> f : subs)
			if (f.subs != null)
				ans++;
		return ans;
	}

	public void delete() {
		parent.subs.remove(this);
	}

	public T getData() {
		return data;
	}

	public List<VFile<T>> getIf(Predicate<VFile<T>> p) {
		List<VFile<T>> ans = new ArrayList<VFile<T>>();
		for (VFile<T> v : list()) {
			if (p.test(v))
				ans.add(v);
			if (v.subs != null)
				ans.addAll(v.getIf(p));
		}
		return ans;
	}

	public String getName() {
		return name;
	}

	public VFile<T> getParent() {
		return parent;
	}

	public String getPath() {
		if (parent != null)
			return parent.getPath() + "/" + name;
		return name;
	}

	public List<VFile<T>> list() {
		return subs;
	}

	public void replace(T t) {
		delete();
		new VFile<T>(parent, name, t);
	}

	public void sort() {
		if (subs == null)
			return;
		subs.sort(null);
		for (VFile<T> v : subs)
			v.sort();
	}

	@Override
	public String toString() {
		return name;
	}

}
