package com.mandarin.bcu.io;

public class BCJSON {

}
/*


import static io.WebPack.packlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import org.json.JSONArray;
import org.json.JSONObject;

import decode.ZipLib;
import main.MainBCU;
import main.Opts;
import page.LoadPage;
import page.MainLocale;
import util.Data;
import util.pack.Pack;

public class BCJSON extends WebFileIO {

	public static String USERNAME = "";
	public static long PASSWORD = 0;
	public static int ID = 0;
	public static int cal_ver = 0;

	private static final String req = "http://battlecatsultimate.cf/api/java/";
	private static final String ast = "http://battlecatsultimate.cf/api/resources/";
	private static final String path = "./assets/";
	private static final String login = "login.php";
	private static final String upload = "upload.php";
	private static final String rate = "rate.php";
	private static final String getinfo = "getinfo.php";
	private static final String delete = "delete.php";
	private static final String fileio = "fileio.php";
	private static final String logio = "logio.php";
	private static final String retrieve = "acquire.php";

	private static final String[] cals;

	static {
		cals = new String[33];
		String cal = "calendar/";
		cals[0] = cal + "event ID.txt";
		cals[1] = cal + "gacha ID.txt";
		cals[2] = cal + "item ID.txt";
		cals[3] = cal + "group event.txt";
		cals[4] = cal + "group hour.txt";

		for (int i = 0; i < 4; i++) {
			String lang = "lang/" + MainLocale.LOC_CODE[i] + "/";
			cals[i * 7 + 5] = lang + "util.properties";
			cals[i * 7 + 6] = lang + "page.properties";
			cals[i * 7 + 7] = lang + "info.properties";
			cals[i * 7 + 8] = lang + "internet.properties";
			cals[i * 7 + 9] = lang + "StageName.txt";
			cals[i * 7 + 10] = lang + "UnitName.txt";
			cals[i * 7 + 11] = lang + "EnemyName.txt";
			// TODO tutorial
		}

	}

	public static void checkDownload() {
		LoadPage.prog("check download");
		File f;
		JSONObject data = null;
		JSONObject lib = null;
		try {
			data = getJSON("getupdate.php");
			lib = getJSON("getAssets.php");
		} catch (IOException e) {
		}
		checkLib(lib);

		if (data != null && data.length() >= 7) {
			LoadPage.prog("check jar update...");
			if (MainBCU.ver < data.getInt("jar")) {
				if (Opts.updateCheck("JAR", data.getString("jar-desc"))) {
					String name = "BCU " + Data.revVer(data.getInt("jar")) + ".jar";
					if (download(ast + "jar/" + name, new File("./" + name), LoadPage.lp)) {
						Writer.logClose(false);
						System.exit(0);
					} else
						Opts.dloadErr(name);
				}

			}
			LoadPage.prog("check text update...");
			for (int i = 0; i < cals.length; i++)
				if (!(f = new File(path + cals[i])).exists() && !download(ast + cals[i], f, null))
					Opts.dloadErr(cals[i]);
			if (cal_ver < data.getInt("cal")) {
				if (Opts.updateCheck("text", "")) {
					for (int i = 0; i < cals.length; i++)
						if (!download(ast + cals[i], new File(path + cals[i]), null))
							Opts.dloadErr(cals[i]);
					cal_ver = data.getInt("cal");
				}
			}

			LoadPage.prog("check music update...");
			int music = data.getInt("music");
			boolean[] mus = new boolean[music];
			File[] fs = new File[music];
			boolean down = false;
			for (int i = 0; i < music; i++)
				down |= mus[i] = !(fs[i] = new File(path + "music/" + Data.trio(i) + ".ogg")).exists();
			if (down && Opts.updateCheck("music", ""))
				for (int i = 0; i < music; i++)
					if (mus[i]) {
						LoadPage.prog("download musics: " + i + "/" + mus.length);
						if (!download(ast + "music/" + Data.trio(i) + ".ogg", fs[i], LoadPage.lp))
							Opts.dloadErr("music #" + i);
					}
		}

		boolean need = ZipLib.info == null;
		f = new File(path + "calendar/");
		if (need |= !f.exists())
			f.mkdirs();
		for (int i = 0; i < cals.length; i++)
			need |= !new File(path + cals[i]).exists();
		if (need) {
			Opts.pop(Opts.REQITN);
			Writer.logClose(false);
			System.exit(0);
		}
	}

	public static boolean delete(int pid) {
		JSONObject inp = new JSONObject();
		inp.put("uid", ID);
		inp.put("password", PASSWORD);
		inp.put("pid", pid);
		try {
			JSONObject ans = read(inp.toString(), delete);
			int ret = ans.getInt("ret");
			if (ret == 0) {
				WebPack wp = packlist.get(pid);
				wp.state = 1 - wp.state;
			}
			return ret == 0;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static int getID(String str) throws IOException {
		JSONObject inp = new JSONObject();
		inp.put("name", str);
		if (PASSWORD == 0)
			PASSWORD = new Random().nextLong();
		inp.put("password", PASSWORD);
		inp.put("bcuver", MainBCU.ver);
		JSONObject ans = read(inp.toString(), login);
		int ret = ans.getInt("ret");
		if (ret == 0)
			return ans.getInt("id");
		if (ret == 1)
			return -1;
		else if (ret == 2)
			return -100;
		throw new IOException(ans.getString("message"));
	}

	public static List<WebPack> getPacks(int uid, int sort) throws IOException {
		if (packlist.size() == 0)
			refreshPacks();
		List<WebPack> l = new ArrayList<>();
		for (WebPack wp : packlist.values())
			if ((uid == -1 || wp.uid == uid) && wp.state == 0 || wp.uid == uid && uid == ID)
				l.add(wp);
		l.sort(WebPack.getComp(sort));

		return l;
	}

	public static int getPassword(String str) throws IOException {
		JSONObject inp = new JSONObject();
		inp.put("name", str);
		JSONObject ans = read(inp.toString(), retrieve);
		int ret = ans.getInt("ret");
		if (ret == 0) {
			PASSWORD = ans.getLong("password");
			return ans.getInt("id");
		}
		if (ret == 1)
			return -1;
		else if (ret == 2)
			return -100 - ans.getInt("err");
		throw new IOException(ans.getString("message"));
	}

	public static int[][] getRate(JSONObject arr) {
		int[][] ans = new int[2][6];
		JSONArray tot = arr.getJSONArray("tot");
		JSONArray cur = arr.getJSONArray("cur");
		for (int i = 0; i < 6; i++) {
			ans[0][i] = tot.getInt(i);
			ans[1][i] = cur.getInt(i);
		}
		return ans;
	}

	/** upload a pack */
/*
	public static int initUpload(int pid, String name, String desc) {
		JSONObject inp = new JSONObject();
		inp.put("uid", ID);
		inp.put("password", PASSWORD);
		inp.put("pid", pid);
		inp.put("name", process(name));
		inp.put("desc", process(desc));
		inp.put("bcuver", MainBCU.ver);

		try {
			JSONObject ans = read(inp.toString(), upload);
			int ret = ans.getInt("ret");
			if (ret == 0) {
				WebPack wp = new WebPack(pid);
				wp.author = USERNAME;
				wp.desp = desc;
				wp.name = name;
				wp.uid = ID;
				wp.url = req + "downloadpack.php?packid=" + pid;
				boolean b = reversion(pid);
				return b ? 0 : 5;
			} else if (ret == 2)
				return ans.getInt("err");
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
			return 4;
		}
	}

	public static int[][] rate(int pid, int val) {
		JSONObject inp = new JSONObject();
		inp.put("uid", ID);
		inp.put("password", PASSWORD);
		inp.put("pid", pid);
		inp.put("rate", val + 1);

		try {
			JSONObject ans = read(inp.toString(), rate);
			int ret = ans.getInt("ret");
			return ret == 0 ? getRate(ans.getJSONObject("rate")) : null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void refreshPacks() throws IOException {
		JSONObject inp = new JSONObject();
		inp.put("user", ID);
		JSONObject res = read(inp.toString(), getinfo);
		JSONArray packs = res.getJSONArray("pack");
		int len = packs.length();
		packlist.clear();
		for (int i = 0; i < len; i++) {
			new WebPack(packs.getJSONObject(i));
		}
	}

	public static boolean report(File f) {
		try {
			return upload(f, req + logio);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/** update a pack */
/*
	public static boolean reversion(int pid) {
		Pack p = Pack.map.get(pid);
		WebPack wp = packlist.get(pid);
		wp.version++;
		p.version = wp.version;
		p.packUp();
		File f = new File("./pack/" + pid + ".bcupack");
		if (f.exists())
			try {
				boolean b = upload(f, req + fileio);
				if (b) {
					JSONObject inp = new JSONObject();
					inp.put("uid", ID);
					inp.put("password", PASSWORD);
					inp.put("pid", pid);
					inp.put("rev", 1);
					inp.put("bcuver", MainBCU.ver);
					JSONObject ans = read(inp.toString(), upload);
					if (ans.getInt("ret") == 0)
						return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		wp.version--;
		p.version = wp.version;
		return false;
	}

	/** update pack information */
/*
	public static boolean update(int pid, String name, String desc) {
		JSONObject inp = new JSONObject();
		inp.put("uid", ID);
		inp.put("password", PASSWORD);
		inp.put("pid", pid);
		inp.put("name", process(name));
		inp.put("desc", process(desc));

		try {
			JSONObject ans = read(inp.toString(), upload);
			int ret = ans.getInt("ret");
			if (ret == 0) {
				WebPack wp = packlist.get(pid);
				wp.desp = desc;
				wp.name = name;
				wp.version++;
			}
			return ret == 0;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void checkLib(JSONObject lib) {
		if (lib != null && lib.length() > 1) {
			Map<String, String> libmap = new TreeMap<>();
			JSONArray ja = lib.getJSONArray("assets");
			int n = ja.length();
			for (int i = 0; i < n; i++) {
				JSONArray ent = ja.getJSONArray(i);
				libmap.put(ent.getString(0), ent.getString(1));
			}
			List<String> libs;
			if (ZipLib.info != null)
				libs = ZipLib.info.update(libmap.keySet());
			else
				libs = new ArrayList<String>(libmap.keySet());
			boolean updated = false;
			for (String str : libs) {
				if (!str.startsWith("00000"))
					if (!Opts.conf("do you want to download lib update " + str + "? " + libmap.get(str)))
						continue;
				LoadPage.prog("downloading asset: " + str + ".zip");
				File temp = new File(path + (ZipLib.lib == null ? "assets.zip" : "temp.zip"));
				if (download(ast + "assets/" + str + ".zip", temp, LoadPage.lp))
					if (ZipLib.info == null)
						ZipLib.init();
					else
						ZipLib.merge(temp);
				updated = true;
			}
			if (updated) {
				try {
					ZipLib.lib.close();
				} catch (IOException e) {
					Opts.ioErr("failed to save downloads");
					e.printStackTrace();
				}
				ZipLib.init();
			}
		}
		ZipLib.check();

	}

	private static JSONObject getJSON(String url) throws IOException {
		JSONObject inp = new JSONObject();
		inp.put("bcuver", MainBCU.ver);
		JSONObject ans = read(inp.toString(), url);
		int ret = ans.getInt("ret");
		if (ret == 0)
			return ans;
		throw new IOException(ans.getString("message"));
	}

	private static String process(String str) {
		str = str.replaceAll("\\'", "\\\\'");
		return str;
	}

	private static JSONObject read(String json, String app) throws IOException {

		URL url = new URL(req + app);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestMethod("POST");
		OutputStream os = conn.getOutputStream();
		os.write(json.getBytes("UTF-8"));
		os.close();

		InputStream in = conn.getInputStream();
		InputStreamReader isr = new InputStreamReader(in, "UTF-8");
		String result = readAll(new BufferedReader(isr));
		if (!MainBCU.WRITE)
			System.out.println("result: " + result);
		if (!result.startsWith("{"))
			throw new IOException(result);
		JSONObject ans = new JSONObject(result);
		in.close();
		conn.disconnect();
		return ans;
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
}
*/