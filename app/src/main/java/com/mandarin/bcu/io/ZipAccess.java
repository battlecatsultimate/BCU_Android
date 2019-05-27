package com.mandarin.bcu.io;

/*
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//import main.MainBCU;
import main.Printer;
import com.mandarin.bcu.util.Data;
import com.mandarin.bcu.util.system.Backup;
import com.mandarin.bcu.util.system.files.BackupData;
import com.mandarin.bcu.util.system.files.VFile;
import com.mandarin.bcu.util.system.files.VFileRoot;

public class ZipAccess {

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static final Map<String, Backup> bacmap = new TreeMap<>();

	public static boolean delete(List<Backup> strs) throws IOException {
		if (strs == null)
			return false;
		File f = new File("./user/backup.zip");
		if (!f.exists())
			return false;
		FileSystem fs = FileSystems.newFileSystem(f.toPath(), null);
		Set<String> set = new TreeSet<>();
		Files.list(fs.getPath("/MD5")).forEach(elem -> set.add(elem.getFileName().toString()));
		for (Backup bac : strs)
			Files.deleteIfExists(fs.getPath("/backups/" + bac.time));
		Files.list(fs.getPath("/backups")).forEach(elem -> {
			try {
				Queue<String> qs = new ArrayDeque<>(Files.readAllLines(elem));
				int size = Reader.parseIntN(qs.poll());
				for (int i = 0; i < size && !qs.isEmpty(); i++) {
					qs.poll();
					String md5 = qs.poll().trim();
					set.remove(md5);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		for (String str : set)
			Files.deleteIfExists(fs.getPath("/MD5/" + str));
		fs.close();
		for (Backup bac : strs)
			bacmap.remove(bac.time);
		return true;
	}

	public static VFileRoot<BackupData> difference(String t0, String t1) throws IOException {
		File f = new File("./user/backup.zip");
		if (!f.exists())
			return null;
		FileSystem fs = FileSystems.newFileSystem(f.toPath(), null);
		Path ind0 = fs.getPath("/backups/" + t0);
		Path ind1 = fs.getPath("/backups/" + t1);
		if (!Files.exists(ind0) || !Files.exists(ind1)) {
			fs.close();
			return null;
		}
		VFileRoot<BackupData> ans = new VFileRoot<>(t0 + "-" + t1);
		Queue<String> qs = new ArrayDeque<>(Files.readAllLines(ind0));
		int size = Reader.parseIntN(qs.poll());
		for (int i = 0; i < size; i++)
			ans.build(qs.poll(), new BackupData(qs.poll()));
		qs = new ArrayDeque<>(Files.readAllLines(ind1));
		size = Reader.parseIntN(qs.poll());
		for (int i = 0; i < size && !qs.isEmpty(); i++) {
			VFile<BackupData> vf = ans.find(qs.poll());
			String str1 = qs.poll();
			if (vf != null && vf.getData().toString().equals(str1))
				vf.delete();
		}
		fs.close();
		return ans;
	}

	public static boolean export(String time, String part, Path exp) throws IOException {
		File f = new File("./user/backup.zip");
		if (!f.exists())
			return false;
		FileSystem fs = FileSystems.newFileSystem(f.toPath(), null);
		Path index = fs.getPath("/backups/" + time);
		if (!Files.exists(index)) {
			fs.close();
			return false;
		}
		Queue<String> qs = new ArrayDeque<>(Files.readAllLines(index));
		int size = Reader.parseIntN(qs.poll());
		for (int i = 0; i < size && !qs.isEmpty(); i++) {
			String loc = qs.poll().trim();
			String md5 = qs.poll().trim();
			if (!loc.equals(part))
				continue;
			Writer.check(new File(loc));
			Files.copy(fs.getPath("/MD5/" + md5), exp, StandardCopyOption.REPLACE_EXISTING);
		}
		fs.close();
		return true;
	}

	public static boolean extract(Backup bac) throws IOException {
		File f = new File("./user/backup.zip");
		if (!f.exists())
			return false;
		FileSystem fs = FileSystems.newFileSystem(f.toPath(), null);
		Path index = fs.getPath("/backups/" + bac.time);
		if (!Files.exists(index)) {
			fs.close();
			return false;
		}
		Queue<String> qs = new ArrayDeque<>(Files.readAllLines(index));
		int size = Reader.parseIntN(qs.poll());
		for (int i = 0; i < size && !qs.isEmpty(); i++) {
			String loc = qs.poll().trim();
			String md5 = qs.poll().trim();
			Writer.check(new File(loc));
			Files.copy(fs.getPath("/MD5/" + md5), Paths.get(loc), StandardCopyOption.REPLACE_EXISTING);
		}
		fs.close();
		return true;
	}

	public static VFileRoot<BackupData> extractAllList() throws IOException {
		File f = new File("./user/backup.zip");
		if (!f.exists())
			return null;
		FileSystem fs = FileSystems.newFileSystem(f.toPath(), null);
		VFileRoot<BackupData> ans = new VFileRoot<>("all versions");
		Files.list(fs.getPath("/backups")).forEach(elem -> {
			try {
				String fn = elem.getFileName().toString();
				Queue<String> qs = new ArrayDeque<>(Files.readAllLines(elem));
				int size = Reader.parseIntN(qs.poll());
				for (int i = 0; i < size && !qs.isEmpty(); i++) {
					String pat = qs.poll().trim();
					String md5 = qs.poll().trim();
					long fsize = Files.size(fs.getPath("/MD5/" + md5));
					VFile<BackupData> vf = ans.build(pat, null);
					vf.mark = 1;
					boolean b = true;
					for (VFile<BackupData> c : vf.list())
						if (c.getData() != null && c.getData().toString().equals(md5)) {
							b = false;
							c.name = fn;
						}
					if (b)
						ans.build(pat + "\\" + fn, new BackupData(md5, fsize));
				}
			} catch (Exception e) {
				Printer.p("ZipAccess", 174, elem.getFileName().toString());
				e.printStackTrace();
			}
		});
		fs.close();
		ans.getIf(v -> v.mark == 1 && v.list().size() == 1).forEach(v -> v.replace(v.list().get(0).getData()));
		return ans;
	}

	public static VFileRoot<BackupData> extractList(Backup bac) throws IOException {
		if (bac.files != null)
			return bac.files;
		File f = new File("./user/backup.zip");
		if (!f.exists())
			return null;
		FileSystem fs = FileSystems.newFileSystem(f.toPath(), null);
		Path index = fs.getPath("/backups/" + bac.time);
		if (!Files.exists(index)) {
			fs.close();
			return null;
		}
		VFileRoot<BackupData> ans = new VFileRoot<BackupData>(bac.name);
		Queue<String> qs = new ArrayDeque<>(Files.readAllLines(index));
		int size = Reader.parseIntN(qs.poll());
		for (int i = 0; i < size && !qs.isEmpty(); i++) {
			String p = qs.poll();
			String md5 = qs.poll();
			long fsize = Files.size(fs.getPath("/MD5/" + md5));
			ans.build(p, new BackupData(md5, fsize));

		}
		fs.close();
		bac.files = ans;
		return ans;
	}

	public static boolean extractPartial(String md5, VFile<BackupData> vf) throws IOException {
		File f = new File("./user/backup.zip");
		if (!f.exists())
			return false;
		FileSystem fs = FileSystems.newFileSystem(f.toPath(), null);
		Path file = fs.getPath("/MD5/" + md5);
		if (!Files.exists(file)) {
			fs.close();
			return false;
		}
		String loc = vf.getParent().mark == 1 ? vf.getParent().getPath() : vf.getPath();
		Writer.check(new File(loc));
		Files.copy(file, Paths.get(loc), StandardCopyOption.REPLACE_EXISTING);
		fs.close();
		return true;
	}

	public static List<Backup> getList() throws IOException {
		List<Backup> ans = new ArrayList<>();
		File f = new File("./user/backup.zip");
		if (!f.exists())
			return ans;
		FileSystem fs = FileSystems.newFileSystem(f.toPath(), null);
		Files.list(fs.getPath("/backups")).forEach(elem -> {
			String str = elem.getFileName().toString();
			Backup bac = bacmap.get(str);
			if (bac == null)
				bacmap.put(str, bac = new Backup(str));
			ans.add(bac);
		});
		Path name = fs.getPath("/backupNames.txt");
		if (Files.exists(name))
			zreadName(new ArrayDeque<>(Files.readAllLines(name)));
		fs.close();
		return ans;
	}

	public static Queue<String> readLine(String md5) throws IOException {
		File f = new File("./user/backup.zip");
		if (!f.exists())
			return null;
		FileSystem fs = FileSystems.newFileSystem(f.toPath(), null);
		Path p = fs.getPath("/MD5/" + md5);
		Queue<String> ans = null;
		if (Files.exists(p))
			ans = new ArrayDeque<>(Files.readAllLines(p));
		fs.close();
		return ans;
	}

	public static InStream readStream(String md5) throws IOException {
		File f = new File("./user/backup.zip");
		if (!f.exists())
			return null;
		FileSystem fs = FileSystems.newFileSystem(f.toPath(), null);
		Path p = fs.getPath("/MD5/" + md5);
		InStream ans = null;
		if (Files.exists(p)) {
			byte[] bs = Files.readAllBytes(p);
			ans = InStream.getIns(bs);
		}
		fs.close();
		return ans;
	}

	public static void saveWork(String time) throws Exception {
		File f = new File("./user/backup.zip");
		if (!f.exists()) {
			Writer.check(f);
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
			ZipEntry ze = new ZipEntry("data.ini");
			out.putNextEntry(ze);
			String str = "Created at " + MainBCU.getTime();
			out.write(str.getBytes());
			out.close();
		}
		FileSystem fs = FileSystems.newFileSystem(f.toPath(), null);
		File res = new File("./res/");
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		checkDict(fs, "/MD5");
		checkDict(fs, "/backups");
		Path target = fs.getPath("/backups/" + time);
		BufferedWriter writer = Files.newBufferedWriter(target);
		int size = Files.walk(res.toPath()).mapToInt(path -> Files.isDirectory(path) ? 0 : 1).sum() + 1;
		writer.write(size + "\r\n");
		write(writer, md5, fs, Paths.get("./user/basis.v"));
		Files.walk(res.toPath()).forEach(elem -> write(writer, md5, fs, elem));
		writer.close();

		Iterator<Path> itr = Files.list(fs.getPath("/backups")).iterator();
		itr.next();
		if (itr.hasNext()) {
			md5.update(Files.readAllBytes(target));
			String bs0 = bytesToHex(md5.digest());
			Path prev = itr.next();
			md5.update(Files.readAllBytes(prev));
			String bs1 = bytesToHex(md5.digest());
			if (bs0.equals(bs1))
				Files.deleteIfExists(prev);
		}
		try {
			Path file = fs.getPath("/backupNames.txt");
			Files.deleteIfExists(file);
			BufferedWriter wname = Files.newBufferedWriter(file);
			wname.write("0.4.0\r\n");
			int n = bacmap.size();
			wname.write(n + "\r\n");
			for (Backup bac : bacmap.values()) {
				wname.write(bac.time + "\t" + bac.name + "\r\n");
			}
			wname.write("0\r\n");
			wname.close();
		} catch (Exception e) {
			if (!MainBCU.WRITE)
				e.printStackTrace();
		}
		fs.close();
	}

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	private static void checkDict(FileSystem fs, String str) throws IOException {
		Path path = fs.getPath(str);
		if (!Files.exists(path))
			Files.createDirectory(path);
	}

	private static void write(BufferedWriter writer, MessageDigest md5, FileSystem fs, Path elem) {
		try {
			if (Files.isDirectory(elem))
				return;
			md5.update(Files.readAllBytes(elem));
			String code = bytesToHex(md5.digest());
			Path dst = fs.getPath("/MD5/" + code);
			if (!Files.exists(dst))
				Files.copy(elem, dst);
			writer.write(elem.toString() + "\r\n");
			writer.write(code + "\r\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void zreadName(Queue<String> qs) {
		try {
			int ver = Data.getVer(qs.poll().trim());
			if (ver == 400) {
				int n = Reader.parseIntN(qs.poll());
				for (int i = 0; i < n; i++) {
					String[] strs = qs.poll().trim().split("\t");
					if (bacmap.containsKey(strs[0]))
						bacmap.get(strs[0]).name = strs[1];
				}
				int m = Reader.parseIntN(qs.poll());
				for (int i = 0; i < m; i++) {
					// TODO
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
*/