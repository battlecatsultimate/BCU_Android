package com.mandarin.bcu.decode;

import com.mandarin.bcu.io.Reader;
import com.mandarin.bcu.io.Writer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class LibInfo {

	protected final int ver;

	protected final String fs;

	protected final Map<String, VerInfo> libver = new TreeMap<>();

	protected final MergedLib merge = new MergedLib();

	protected LibInfo(String sys) {
		fs = sys;
		Queue<String> qs = Reader.readLines(fs + "/info/info.ini");
		ver = Reader.parseIntN(qs.poll());
		int n = Reader.parseIntN(qs.poll());
		for (int i = 0; i < n; i++) {
			String v = qs.poll().trim();
			VerInfo vi = new VerInfo(fs, v);
			libver.put(v, vi);
			merge.add(vi);
		}
	}

	public void merge(LibInfo li) throws IOException {
		List<PathInfo> ls = merge.merge(li.merge);
		for (PathInfo p : ls)
			if (p.type == 0) {
				// Path target = fs.getPath(p.path); TODO
				// Files.createDirectories(target.getParent());
				// Files.copy(li.fs.getPath(p.path), target, RE);
			} else if (p.type == 1)
				;// TODO Files.deleteIfExists(fs.getPath(p.path));
		for (String v : li.libver.keySet())
			if (!libver.containsKey(v)) {
				// TODO Files.copy(li.fs.getPath("/info/" + v + ".verinfo"), fs.getPath("/info/"
				// + v + ".verinfo"));
				libver.put(v, li.libver.get(v));
			}
		File info = new File(fs + "/info/info.ini");
		if (info.exists())
			info.delete();
		PrintStream ps = Writer.newFile(fs + "/info/info.ini");
		ps.println("file_version = 00040510");
		ps.println("number_of_libs = " + libver.size());
		for (VerInfo vi : libver.values())
			ps.println(vi.ver);
		ps.close();
	}

	public List<String> update(Collection<String> list) {
		List<String> ans = new ArrayList<>();
		for (String s : list)
			if (!merge.set.contains(s))
				ans.add(s);
		return ans;
	}

}

class MergedLib {

	protected final Set<String> set = new TreeSet<>();

	protected final Map<String, PathInfo> paths = new TreeMap<>();

	protected void add(VerInfo vi) {
		set.add(vi.ver);
		for (PathInfo p : vi.paths)
			if (isNew(p))
				paths.put(p.path, p);

	}

	protected List<PathInfo> merge(MergedLib ml) {
		List<PathInfo> ans = new ArrayList<>();
		ml.paths.forEach((p, i) -> {
			if (isNew(i)) {
				ans.add(i);
				paths.put(p, i);
			}
		});
		return ans;
	}

	private boolean isNew(PathInfo p) {
		return !paths.containsKey(p.path) || paths.get(p.path).update(p);
	}

}

final class PathInfo implements Comparable<PathInfo> {

	private static final String[] types = { "add:", "delete:" };

	private static int getType(String str) {
		for (int i = 0; i < types.length; i++)
			if (str.equals(types[i]))
				return i;
		return -1;
	}

	protected final String path;
	protected final String ver;
	protected final int type;

	protected PathInfo(String input, String v) {
		ver = v;
		String[] strs = input.split("\t");
		path = strs[1].trim();
		type = getType(strs[0].trim());
	}

	protected PathInfo(String p, String v, int t) {
		path = p;
		ver = v;
		type = t;
	}

	@Override
	public int compareTo(PathInfo o) {
		return path.compareTo(o.path);
	}

	@Override
	public String toString() {
		return types[type] + "\t" + path;
	}

	protected boolean update(PathInfo p) {
		assert path.equals(p.path);
		return Reader.parseIntN(ver) < Reader.parseIntN(p.ver);
	}

}

class VerInfo implements Comparable<VerInfo> {

	protected final String ver;

	protected final Set<PathInfo> paths = new TreeSet<>();

	protected VerInfo(String fs, String v) {
		ver = v;
		Queue<String> qs = Reader.readLines(fs + "/info/" + v + ".verinfo");
		qs.poll();
		qs.poll();
		int n = Reader.parseIntN(qs.poll());
		for (int i = 0; i < n; i++)
			paths.add(new PathInfo(qs.poll(), v));
	}

	@Override
	public int compareTo(VerInfo o) {
		return ver.compareTo(o.ver);
	}

	protected void write(String str) {
		PrintStream ps = Writer.newFile(str);
		ps.println("file_version = 00040510");
		ps.println("lib_version = " + ver);
		ps.println("number_of_paths = " + paths.size());
		for (PathInfo p : paths)
			ps.println(p.toString());
		ps.close();
	}

}