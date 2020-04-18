package com.mandarin.bcu.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import common.io.DataIO;
import main.Opts;

import static java.lang.Character.isDigit;

public class Reader extends DataIO {

	public static int parseIntN(String str) {
		int ans;
		try {
			ans = parseIntsN(str)[0];
		} catch (Exception e) {
			ans = -1;
		}
		return ans;
	}

	public static int[] parseIntsN(String str) {
		ArrayList<String> lstr = new ArrayList<>();
		int t = -1;
		for (int i = 0; i < str.length(); i++)
			if (t == -1) {
				if (isDigit(str.charAt(i)) || str.charAt(i) == '-' || str.charAt(i) == '+')
					t = i;
			} else if (!isDigit(str.charAt(i))) {
				lstr.add(str.substring(t, i));
				t = -1;
			}
		if (t != -1)
			lstr.add(str.substring(t));
		int ind = 0;
		while (ind < lstr.size()) {
			if (isDigit(lstr.get(ind).charAt(0)) || lstr.get(ind).length() > 1)
				ind++;
			else
				lstr.remove(ind);
		}
		int[] ans = new int[lstr.size()];
		for (int i = 0; i < lstr.size(); i++)
			ans[i] = Integer.parseInt(lstr.get(i));
		return ans;
	}

	public static long parseLongN(String str) {
		long ans;
		try {
			ans = parseLongsN(str)[0];
		} catch (Exception e) {
			ans = -1;
		}
		return ans;
	}

	public static long[] parseLongsN(String str) {
		ArrayList<String> lstr = new ArrayList<>();
		int t = -1;
		for (int i = 0; i < str.length(); i++)
			if (t == -1) {
				if (isDigit(str.charAt(i)) || str.charAt(i) == '-' || str.charAt(i) == '+')
					t = i;
			} else if (!isDigit(str.charAt(i))) {
				lstr.add(str.substring(t, i));
				t = -1;
			}
		if (t != -1)
			lstr.add(str.substring(t));
		int ind = 0;
		while (ind < lstr.size()) {
			if (isDigit(lstr.get(ind).charAt(0)) || lstr.get(ind).length() > 1)
				ind++;
			else
				lstr.remove(ind);
		}
		long[] ans = new long[lstr.size()];
		for (int i = 0; i < lstr.size(); i++)
			ans[i] = Long.parseLong(lstr.get(i));
		return ans;
	}

	public static Queue<String> readLines(String path) {
		File file = new File(path);
		Queue<String> ans = new ArrayDeque<>();
		BufferedReader reader;

		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
			reader = new BufferedReader(isr);
			String temp;

			while ((temp = reader.readLine()) != null)
				ans.add(temp);
			reader.close();
		} catch (Exception e) {
			Opts.ioErr("failed to read file " + file);
			e.printStackTrace();
		}
		return ans;
	}

	public static Set<String> getInfo(String path) {
		String infopath;
		if(path.endsWith("/"))
			infopath = path + "info";
		else
			infopath = path + "/info";

		String filename = "info_android.ini";

		File f = new File(infopath, filename);

		try {
			String line;

			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			ArrayList<String> lines = new ArrayList<>();

			while ((line = br.readLine()) != null) {
				lines.add(line);
			}

			return new TreeSet<>(Arrays.asList(lines.get(2).split("=")[1].split(",")));
		} catch (IOException e) {
			e.printStackTrace();
			return new TreeSet<>();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
