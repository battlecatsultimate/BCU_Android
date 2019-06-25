package main;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Printer {

	public static void p(String string, int i, String string2) {
		// TODO Auto-generated method stub

	}

	public static void e(String source, int line, String description) {
		print(source, line, description);
	}

	private static void print(String source, int line, String description) {
		Date date = new Date();
		String h = new SimpleDateFormat("HH").format(date);
		String m = new SimpleDateFormat("mm").format(date);
		String s = new SimpleDateFormat("ss").format(date);

		System.out.println("[" + source + ":#" + line + "," + h + ":" + m + ":" + s + "]:" + description);
	}

}
