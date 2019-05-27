package com.mandarin.bcu.util.pack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.util.system.FixIndexList;

public class MusicStore extends FixIndexList<File> {

	public static List<File> getAll(Pack p) {
		List<File> ans = new ArrayList<>();
		if (p != null) {
			ans.addAll(p.ms.getList());
			for (int rel : p.rely)
				ans.addAll(Pack.map.get(rel).ms.getList());
		} else
			for (Pack pac : Pack.map.values())
				ans.addAll(pac.ms.getList());
		return ans;
	}

	public static int getID(File f) {
		for (Pack p : Pack.map.values())
			if (p.ms.contains(f))
				return p.id * 1000 + p.ms.indexOf(f);
		return -1;
	}

	public static File getMusic(int ind) {
		int pid = ind / 1000;
		Pack pack = Pack.map.get(pid);
		if (pack == null)
			return null;
		return pack.ms.get(ind % 1000);
	}

	private final Pack pack;

	public MusicStore(Pack p) {
		super(new File[1000]);
		pack = p;
	}

	public void load() {
		clear();
		File f = new File("./res/img/" + pack.id + "/music/");
		if (f.exists() && f.isDirectory()) {
			File[] fs = f.listFiles();
			for (File fi : fs) {
				String str = fi.getName();
				if (str.length() != 7)
					continue;
				if (!str.endsWith(".ogg"))
					continue;
				int val = -1;
				try {
					val = Integer.parseInt(str.substring(0, 3));
				} catch (NumberFormatException e) {
					e.printStackTrace();
					continue;
				}
				if (val >= 0)
					set(val, fi);
			}
		}
	}

	public String nameOf(File f) {
		return pack.id + trio(indexOf(f));
	}

	@Override
	public String toString() {
		return pack.toString();
	}

	protected OutStream packup() {
		// TODO not used
		OutStream mus = OutStream.getIns();
		mus.writeString("0.3.7");
		Map<Integer, File> mcas = getMap();
		mus.writeInt(mcas.size());
		for (int ind : mcas.keySet()) {
			mus.writeInt(ind);
			OutStream data = OutStream.getIns();
			try {
				byte[] bs = Files.readAllBytes(mcas.get(ind).toPath());
				data.writeBytesI(bs);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			data.terminate();
			mus.accept(data);

		}
		mus.terminate();
		return mus;
	}

	protected OutStream write() {
		OutStream os = OutStream.getIns();
		os.writeString("0.3.7");
		os.writeInt(0);
		os.terminate();
		return os;
	}

}
