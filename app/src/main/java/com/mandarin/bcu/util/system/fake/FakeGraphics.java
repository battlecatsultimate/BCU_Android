package com.mandarin.bcu.util.system.fake;

public interface FakeGraphics {

	public static final int RED = 0, YELLOW = 1, BLACK = 2, MAGENTA = 3, BLUE = 4, CYAN = 5, WHITE = 6;
	public static final int DEF = 0, TRANS = 1, BLEND = 2, GRAY = 3;

	public void colRect(int x, int y, int w, int h, int r, int g, int b, int... a);

	public void drawImage(FakeImage bimg, double x, double y);

	public void drawImage(FakeImage bimg, double x, double y, double d, double e);

	public void drawLine(int i, int j, int x, int y);

	public void drawOval(int i, int j, int k, int l);

	public void drawRect(int x, int y, int x2, int y2);

	public void fillOval(int i, int j, int k, int l);

	public void fillRect(int x, int y, int w, int h);

	public FakeTransform getTransform();

	public void gradRect(int x, int y, int w, int h, int a, int b, int[] c, int d, int e, int[] f);

	public void rotate(double d);

	public void scale(int hf, int vf);

	public void setColor(int c);

	public void setComposite(int mode, int... para);

	public void setRenderingHint(int key, int object);

	public void setTransform(FakeTransform at);

	public void translate(double x, double y);

}
