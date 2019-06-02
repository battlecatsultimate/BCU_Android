package com.mandarin.bcu.decode;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import com.mandarin.bcu.main.Opts;
import com.mandarin.bcu.util.system.files.AssetData;
import com.mandarin.bcu.util.system.files.VFile;

public class ZipLib {

	public static final String[] LIBREQS = { "000001", "000002", "000003", "080602", "080603" };
	public static final String[] OPTREQS = { "080504" };

	public static String lib;
	public static LibInfo info;

	public static void check() {
		for (String req : LIBREQS)
			if (info == null || !info.merge.set.contains(req)) {
				Opts.loadErr("this version requires lib " + req);
				// Writer.logClose(false);
				System.exit(0);
			}
	}

	public static void init() {

		File f = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/");
		if (!f.exists())
			return;
		info = new LibInfo(Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files");// TODO
	}

	public static void merge(File f) {
		try {

			LibInfo nlib = new LibInfo("");// TODO
			info.merge(nlib);
			f.delete();
		} catch (IOException e) {
			Opts.loadErr("failed to merge lib");
			e.printStackTrace();
		}
	}

	public static void read() {
		String prev = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files";// TODO
		for (PathInfo pi : info.merge.paths.values()) {
			if (pi.type != 0)
				continue;
			VFile.root.build(pi.path, AssetData.getAsset(new File(prev + pi.path.substring(1))));
			// LoadPage.prog("reading assets " + i++ + "/" + tot);
		}

		VFile.root.sort();
	}

}