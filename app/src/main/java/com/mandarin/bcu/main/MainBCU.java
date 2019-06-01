package com.mandarin.bcu.main;

import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainBCU {

	public static final int ver = 5000;

	public static int FILTER_TYPE = 0;
	public static final boolean WRITE = !new File("./.project").exists();
	public static boolean preload = false, trueRun = false, loaded = false, USE_JOGL = false;

	public static String getTime() {
		return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	}

	public static void CheckMem(Context context) {
		trueRun = true;
		long mem = Runtime.getRuntime().maxMemory();
		if (mem >> 28 == 0) {
			Opts.pop(Opts.MEMORY, "" + (mem >> 20));
			((Activity)context).finish();
		}

		loaded = true;
	}

	public static String validate(String str) {
		char[] chs = new char[] { '.', '/', '\\', ':', '*', '?', '"', '<', '>', '|' };
		for (char c : chs)
			str = str.replace(c, '#');
		return str;
	}

}
