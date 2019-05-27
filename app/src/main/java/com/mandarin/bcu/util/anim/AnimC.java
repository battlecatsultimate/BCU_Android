package com.mandarin.bcu.util.anim;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.mandarin.bcu.io.InStream;
import com.mandarin.bcu.io.OutStream;
import com.mandarin.bcu.main.Opts;
import com.mandarin.bcu.util.system.VImg;
import com.mandarin.bcu.util.system.fake.FakeImage;
import com.mandarin.bcu.util.system.files.FDByte;
import com.mandarin.bcu.util.system.files.FileData;
import com.mandarin.bcu.util.system.files.VFile;

public class AnimC extends AnimU {

	public static String getAvailable(String str) {
		File folder = new File("./res/anim/");
		if (!folder.exists())
			return str;
		File[] fs = new File("./res/anim/").listFiles();
		Set<String> strs = new HashSet<>();
		for (int i = 0; i < fs.length; i++)
			strs.add(fs[i].getName());
		while (strs.contains(str))
			str += "'";
		return str;
	}

	private boolean saved = false;
	public boolean inPool;
	public Stack<History> history = new Stack<>();
	public String name = "";
	public String prev;

	public AnimC(InStream is) {
		name = "local animation";
		inPool = false;
		loaded = true;
		partial = true;
		saved = true;
		try {
			num = FakeImage.read(is.nextBytesI());
		} catch (IOException e) {
			e.printStackTrace();
		}
		imgcut = ImgCut.newIns(new FDByte(is.nextBytesI()));
		mamodel = MaModel.newIns(new FDByte(is.nextBytesI()));
		int n = is.nextInt();
		anims = new MaAnim[n];
		for (int i = 0; i < n; i++)
			anims[i] = MaAnim.newIns(new FDByte(is.nextBytesI()));
		parts = imgcut.cut(num);
		if (!is.end()) {

			VImg vimg = new VImg(is.nextBytesI());
			if (vimg.getImg().getHeight() == 32)
				edi = vimg;
			else
				uni = vimg;
		}
		if (!is.end()) {

			uni = new VImg(is.nextBytesI());

		}
	}

	public AnimC(String st) {
		inPool = true;
		prev = "./res/anim/";
		name = st;
		VFile<? extends FileData> f = VFile.getFile(prev + name + "/edi.png");
		if (f != null)
			edi = new VImg(f);
		f = VFile.getFile(prev + name + "/uni.png");
		if (f != null)
			uni = new VImg(f);
	}

	public AnimC(String str, AnimD ori) {
		inPool = true;
		prev = "./res/anim/";
		name = str;
		loaded = true;
		partial = true;
		imgcut = ori.imgcut.clone();
		mamodel = ori.mamodel.clone();
		if (mamodel.confs.length < 1)
			mamodel.confs = new int[2][6];
		anims = new MaAnim[7];
		for (int i = 0; i < 7; i++)
			if (i < ori.anims.length)
				anims[i] = ori.anims[i].clone();
			else
				anims[i] = new MaAnim();
		num = ori.getNum();
		parts = imgcut.cut(num);
		File f = new File(prev + name + "/" + name + ".png");
		// Writer.check(f);
		try {
			FakeImage.write(num, "PNG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		reloImg();
		if (ori instanceof AnimU) {
			AnimU au = (AnimU) ori;
			edi = au.edi;
			uni = au.uni;
		}
		// saveIcon();
		// saveUni();
		// history("initial");
	}

	public void delete() {
		// Writer.delete(new File(prev + name + "/"));
	}

	public String getUndo() {
		return history.peek().name;
	}

	public void ICedited() {
		check();
		parts = imgcut.cut(num);
	}

	public boolean isSaved() {
		return saved;
	}

	@Override
	public void load() {
		loaded = true;
		try {
			String pre = prev + name + "/" + name;
			num = VFile.getFile(pre + ".png").getData().getImg();
			imgcut = ImgCut.newIns(pre + ".imgcut");
			if (num == null) {
				// Printer.e("AnimC", 147, "can't read png: " + pre);
				Opts.loadErr("sprite missing: " + pre + ".png");
				// Writer.logClose(false);
				System.exit(0);
			}
			parts = imgcut.cut(num);
			partial();
			if (edi != null)
				edi.check();
			if (uni != null)
				uni.check();
			// history("initial");
		} catch (Exception e) {
			Opts.loadErr("Error in loading custom animation: " + name);
			e.printStackTrace();
			System.exit(0);
		}
		validate();
	}

	public void reloImg() {
		num = VFile.getFile(prev + name + "/" + name + ".png").getData().getImg();
	}

	public void restore() {
		history.pop();
		InStream is = history.peek().data.translate();
		imgcut.restore(is);
		ICedited();
		mamodel.restore(is);
		int n = is.nextInt();
		anims = new MaAnim[n];
		for (int i = 0; i < n; i++) {
			anims[i] = new MaAnim();
			anims[i].restore(is);
		}
		is = history.peek().mms.translate();
		n = is.nextInt();
		for (int i = 0; i < n; i++) {
			int ind = is.nextInt();
			int val = is.nextInt();
			if (ind >= 0 && ind < mamodel.n)
				mamodel.status.put(mamodel.parts[ind], val);
		}
		saved = false;
	}

	@Override
	public String toString() {
		return name;
	}

	public void updateStatus() {
		OutStream mms = OutStream.getIns();
		mms.writeInt(mamodel.status.size());
		mamodel.status.forEach((d, s) -> {
			int ind = -1;
			for (int i = 0; i < mamodel.n; i++)
				if (mamodel.parts[i] == d)
					ind = i;
			mms.writeInt(ind);
			mms.writeInt(s);
		});
		mms.terminate();
		history.peek().mms = mms;
	}

	public OutStream write() {
		OutStream osi = OutStream.getIns();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			FakeImage.write(num, "PNG", baos);
		} catch (IOException e1) {
			e1.printStackTrace();
			osi.terminate();
			return osi;
		}
		osi.writeBytesI(baos.toByteArray());
		try {
			baos = new ByteArrayOutputStream();
			imgcut.write(new PrintStream(baos, true, "UTF-8"));
			osi.writeBytesI(baos.toByteArray());
			baos = new ByteArrayOutputStream();
			mamodel.write(new PrintStream(baos, true, "UTF-8"));
			osi.writeBytesI(baos.toByteArray());
			osi.writeInt(anims.length);
			for (MaAnim ani : anims) {
				baos = new ByteArrayOutputStream();
				ani.write(new PrintStream(baos, true, "UTF-8"));
				osi.writeBytesI(baos.toByteArray());
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (edi != null && edi.getImg() != null) {
			baos = new ByteArrayOutputStream();
			try {
				FakeImage.write(edi.getImg(), "PNG", baos);
			} catch (IOException e1) {
				e1.printStackTrace();
				osi.terminate();
				return osi;
			}
			osi.writeBytesI(baos.toByteArray());
		}
		if (uni != null && uni.getImg() != null) {
			baos = new ByteArrayOutputStream();
			try {
				FakeImage.write(uni.getImg(), "PNG", baos);
			} catch (IOException e1) {
				e1.printStackTrace();
				osi.terminate();
				return osi;
			}
			osi.writeBytesI(baos.toByteArray());
		}
		osi.terminate();
		return osi;
	}

	@Override
	protected void partial() {
		if (!partial) {
			partial = true;
			String pre = prev + name + "/" + name;
			mamodel = MaModel.newIns(pre + ".mamodel");
			anims = new MaAnim[7];
			for (int i = 0; i < 4; i++)
				anims[i] = MaAnim.newIns(pre + "0" + i + ".maanim");
			for (int i = 0; i < 3; i++)
				anims[i + 4] = MaAnim.newIns(pre + "_zombie0" + i + ".maanim");
		}
	}

}

class History {

	protected final OutStream data;

	protected final String name;

	protected OutStream mms;

	protected History(String str, OutStream os) {
		name = str;
		data = os;
	}

}
