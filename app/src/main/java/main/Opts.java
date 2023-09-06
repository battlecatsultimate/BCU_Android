package main;

import android.util.Log;

import com.mandarin.bcu.androidutil.pack.PackConflict;

import java.util.ArrayList;
import java.util.List;

public class Opts {
	public static final String ERR_PACK_READ = "Error in reading pack ";
	public static final String ERR_FAIL = "failed to load pack ";
	public static final String ERR_PACK_SAME_ID = " conflict with pack ";
	public static final String ERR_PACK_PARENT = " require parent packs: ";

	public static final int MEMORY = 1001, SECTY = 1002, REQITN = 1003;

	private static boolean nshowi, nshowu;

	public static void animErr(String f) {
		if (nshowi)
			return;
		nshowi = !warning("error in reading file " + f + ", Click Cancel to supress this popup?", "IO error");
	}

	public static void backupErr(String t) {
		pop("failed to " + t + " backup", "backup access error");
	}

	public static boolean conf() {
		return warning("", "confirmation");
	}

	public static boolean conf(String text) {
		return warning(text, "confirmation");
	}

	public static void dloadErr(String text) {
		pop("failed to download " + text, "download error");
	}

	public static void ioErr(String text) {
		pop(text, "IO error");
	}

	public static void loadErr(String text) {
		Log.e("Opts", text);

		if(text.contains(ERR_PACK_PARENT)) {
			String[] info = text.split(ERR_PACK_PARENT);

			String op;

			if(info[0].contains("pack id")) {
				op = info[0].replace("pack id ","").replace(".bcudata", ".bcupack");
			} else {
				op = info[0].replace("pack ","").replace(".bcudata", ".bcupack");
			}

			String others = info[1];

			List<String> confs = new ArrayList<>();
			confs.add(op);
			confs.add(others);

			new PackConflict(PackConflict.ID_PARENT, confs, !info[0].contains("pack id "));
		} else if(text.contains(ERR_PACK_READ)) {
			String op = text.replace(ERR_PACK_READ, "").replace(" at initialization", "");

			List<String> confs = new ArrayList<>();
			confs.add(op);

			new PackConflict(PackConflict.ID_CORRUPTED, confs, true);
		} else if (text.contains(ERR_FAIL)) {
			String[] op = text.split(" - ");

			List<String> confs = new ArrayList<>();
			confs.add(op[op.length - 1]);

			new PackConflict(PackConflict.ID_CORRUPTED, confs, true);
		}
	}

	public static boolean packConf(String text) {
		Log.e("Opts", text);
		if(text.contains(ERR_PACK_SAME_ID)) {
			String op = text.split("\\. Do you want to use")[0].split(ERR_PACK_SAME_ID)[0].replace("pack ", "");
			String np = text.split("\\. Do you want to use")[0].split(ERR_PACK_SAME_ID)[1];

			Log.e("Opts", "CONFLIST : "+op+", "+np);

			List<String> confs = new ArrayList<>();
			confs.add(op.replace(".bcudata",".bcupack"));
			confs.add(np.replace(".bcudata",".bcupack"));

			new PackConflict(PackConflict.ID_SAME_ID, confs, true);

			return false;
		}

		return false;
	}

	public static void pop(int id, String... is) {
		if (id == MEMORY) {
			Log.e("Opts", "not enough memory. Current memory: " + is[0] + "MB. not enough memory");
			pop("not enough memory. Current memory: " + is[0] + "MB.", "not enough memory");
		} if (id == SECTY) {
			Log.e("Opts", "Failed to access files. Please move BCU to another place");
			pop("Failed to access files. Please move BCU to another place", "file permission error");
		} if (id == REQITN) {
			Log.e("Opts", "failed to connect to internet while download is necessary");
			pop("failed to connect to internet while download is necessary", "download error");
		} else {
			StringBuilder msg = new StringBuilder();

			for(String s : is) {
				msg.append(s);
			}

			Log.e("Opts",msg.toString());
		}
	}

	public static void recdErr(String name, String suf) {
		pop("replay " + name + " uses unavailable " + suf, "replay read error");
	}

	public static void servErr(String text) {
		pop(text, "server error");
	}

	public static void success(String text) {
		pop(text, "success");
	}

	public static void unitErr(String f) {
		if (nshowu)
			return;
		nshowu = !warning(f + ", Click Cancel to supress this popup?", "can't find unit");
	}

	public static boolean updateCheck(String s, String p) {
		return warning(s + " update available. do you want to update? " + p, "update check");
	}

	public static void verErr(String o, String v, String p) {
		pop(o + " version is too old" + (p.isEmpty() ? "" : " for " + p) + ", use BCU " + v + " or " + (o.equals("BCU") ? "newer" : "older")
				+ " version to open it", "version error");

		Log.e("Opts", "UNSUPP | CONFLIST : "+p+", "+v);

		List<String> confs = new ArrayList<>();
		confs.add(p.replace(".bcudata",".bcupack"));
		confs.add(v);

		new PackConflict(PackConflict.ID_UNSUPPORTED_CORE_VERSION, confs, true);
	}

	public static boolean writeErr0(String f) {
		return Opts.warning("failed to write file: " + f + " do you want to retry?", "IO error");
	}

	public static boolean writeErr1(String f) {
		return Opts.warning("failed to write file: " + f + " do you want to save it in another place?", "IO error");
	}

	public static void pop(String text, String title) {
		Log.e("Opts/pop", title+" : "+text);
	}

	private static boolean warning(String text, String title) {
		Log.e("Opts/warning", title+" : "+text);
		return false;
	}

}
