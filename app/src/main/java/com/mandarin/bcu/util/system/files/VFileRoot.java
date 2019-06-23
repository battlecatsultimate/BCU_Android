package com.mandarin.bcu.util.system.files;

public class VFileRoot<T extends FileData> extends VFile<T> {

	public VFileRoot(String str) {
		super(str);
	}

	public VFile<T> build(String str, T fd) {
		String[] strs = str.split("/|\\\\");
		VFile<T> par = this;
		for (int i = 1; i < strs.length; i++) {
			VFile<T> next = null;
			for (VFile<T> ch : par.list())
				if (ch.name.equals(strs[i]))
					next = ch;
			if (next == null)
				if (i == strs.length - 1)
					if (fd != null)
						return new VFile<T>(par, strs[i], fd);
					else
						return new VFile<T>(par, strs[i]);
				else
					next = new VFile<T>(par, strs[i]);
			if (i == strs.length - 1) {
				if (fd == null)
					return next;
				// Printer.e("VFileRoot", 45, "overload");
				return null;
			}
			par = next;
		}
		return null;
	}

	public VFile<T> find(String str) {
		String[] strs = str.split("/|\\\\");
		VFile<T> par = this;
		for (int i = 1; i < strs.length; i++) {
			VFile<T> next = null;
			for (VFile<T> ch : par.list())
				if (ch.name.equals(strs[i]))
					next = ch;
			if (next == null)
				return null;
			if (i == strs.length - 1)
				return next;
			par = next;
		}
		return this;
	}

}
