package com.mandarin.bcu.io;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

public class WebPack {

	public static final int SORT_ID = 0, SORT_RATE = 1, SORT_POP = 2, SORT_NEW = 3;

	protected static Map<Integer, WebPack> packlist = new TreeMap<>();

	public static Comparator<WebPack> getComp(int t) {
		return new WebPackComp(t);
	}

	public int pid, uid, version, bcuver, vote, state;
	public String name, author, desp, url, time;
	public int[][] rate;

	protected WebPack(int pack) {
		pid = pack;
		rate = new int[2][6];
		packlist.put(pid, this);
	}

	protected WebPack(JSONObject pack) {
		try {
			pid = pack.getInt("pid");
			uid = pack.getInt("uid");
			author = pack.getString("author");
			name = pack.getString("name");
			url = pack.getString("url");
			desp = pack.getString("desp");
			version = pack.getInt("version");
			bcuver = pack.getInt("bcuver");
			vote = pack.getInt("vote");
			state = pack.getInt("state");
			time = pack.getString("time");
//			rate = BCJSON.getRate(pack.getJSONObject("rate"));
			packlist.put(pid, this);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public int getRate_0() {
		int ans = 0;
		for (int i = 1; i <= 5; i++)
			ans += (i - 3) * rate[0][i];
		return ans;
	}

	public int getRate_1() {
		int ans = 0;
		int c = 0;
		for (int i = 1; i <= 5; i++) {
			ans += i * rate[0][i];
			c += rate[0][i];
		}
		if (c == 0)
			return 0;
		return ans * 100 / c;
	}

	@Override
	public String toString() {
		return pid + "-" + version + ": " + name;
	}

}

class WebPackComp implements Comparator<WebPack> {

	private static int comp(int t, WebPack o1, WebPack o2, boolean rep) {
		if (t == WebPack.SORT_POP) {
			int val = -Integer.compare(o1.getRate_0(), o2.getRate_0());
			if (val == 0 && rep)
				val = comp(WebPack.SORT_RATE, o1, o2, false);
			return val;
		}
		if (t == WebPack.SORT_RATE) {
			int val = -Integer.compare(o1.getRate_1(), o2.getRate_1());
			if (val == 0 && rep)
				val = comp(WebPack.SORT_POP, o1, o2, false);
			return val;
		}
		if (t == WebPack.SORT_NEW)
			return -o1.time.compareTo(o2.time);
		return Integer.compare(o1.pid, o2.pid);
	}

	private final int type;

	WebPackComp(int typ) {
		type = typ;
	}

	@Override
	public int compare(WebPack o1, WebPack o2) {
		return comp(type, o1, o2, true);
	}

}