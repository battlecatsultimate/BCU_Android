package com.mandarin.bcu.util.system.fake;

import java.io.IOException;

public interface FakeImage {

	public static FakeImage read(Object o) throws IOException {
		return ImageBuilder.builder.build(o);
	}

	public static boolean write(FakeImage img, String str, Object o) throws IOException {
		return ImageBuilder.builder.write(img, str, o);
	}

	public Object bimg();

	public int getHeight();

	public int getRGB(int i, int j);

	public FakeImage getSubimage(int i, int j, int k, int l);

	public int getWidth();

	public Object gl();

	public void setRGB(int i, int j, int p);

}
