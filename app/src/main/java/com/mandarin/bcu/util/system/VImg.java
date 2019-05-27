package com.mandarin.bcu.util.system;

import java.io.IOException;

import com.mandarin.bcu.util.ImgCore;
import com.mandarin.bcu.util.anim.ImgCut;
import com.mandarin.bcu.util.system.fake.FakeImage;
import com.mandarin.bcu.util.system.files.FileData;
import com.mandarin.bcu.util.system.files.VFile;

public class VImg extends ImgCore {

	private final VFile<? extends FileData> file;

	public String name = "";

	private FakeImage bimg = null;
	private boolean loaded = false;
	private ImgCut ic;

	public VImg(Object o) {
		if (o instanceof String)
			file = VFile.getFile((String) o);
		else if (o instanceof VFile)
			file = (VFile<?>) o;
		else
			file = null;

		if (file == null)
			try {
				bimg = FakeImage.read(o);
			} catch (IOException e) {
				e.printStackTrace();
			}
		loaded = bimg != null;
	}

	public synchronized void check() {
		if (!loaded)
			load();
	}

	public FakeImage getImg() {
		check();
		return bimg;
	}

	public void setCut(ImgCut cut) {
		ic = cut;
	}

	public void setImg(Object img) {
		try {
			bimg = FakeImage.read(img);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (ic != null)
			bimg = ic.cut(bimg)[0];
		loaded = true;
	}

	@Override
	public String toString() {
		return file == null ? name.length() == 0 ? "img" : name : file.getName();
	}

	private void load() {
		loaded = true;
		if (file == null)
			return;
		bimg = file.getData().getImg();
		if (ic != null)
			bimg = ic.cut(bimg)[0];
	}

}
