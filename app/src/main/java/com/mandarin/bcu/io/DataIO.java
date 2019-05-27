package com.mandarin.bcu.io;

import com.mandarin.bcu.util.system.P;

public strictfp abstract class DataIO {

	protected static final byte BYTE = 1;
	protected static final byte SHORT = 2;
	protected static final byte INT = 3;
	protected static final byte LONG = 4;
	protected static final byte FLOAT = 5;
	protected static final byte DOUBLE = 6;
	protected static final byte BYTESI = 7;
	protected static final byte INTSB = 8;
	protected static final byte INTSI = 9;
	protected static final byte STRING = 10;
	protected static final byte BYTESB = 11;
	protected static final byte INTSSBB = 12;
	protected static final byte DOUBLESB = 13;
	protected static final byte SUBS = 14;

	protected static final String[] names = { "unknown", "byte", "short", "int", "long", "float", "double", "byte[int]",
			"int[byte]", "String", "byte[byte]", "int[byte][byte]", "double[byte" };

	public static void fromABP(byte[] b, int index, P n) {
		fromShort(b, index, (short) (n.x * 10 + 2000));
		index += 2;
		fromShort(b, index, (short) (n.y * 10 + 2000));
	}

	/** write a number n into a byte[] start from index */

	public static void fromByte(byte[] b, int index, byte n) {
		b[index] = n;
	}

	public static void fromDouble(byte[] b, int index, double n) {
		long l = Double.doubleToLongBits(n);
		fromLong(b, index, l);
	}

	public static void fromFloat(byte[] b, int index, float n) {
		int l = Float.floatToIntBits(n);
		fromInt(b, index, l);
	}

	public static void fromInt(byte[] b, int index, int n) {
		for (int i = 0; i < 4; i++)
			b[index + i] = (byte) (n >> (i * 8) & 0xff);
	}

	public static void fromLong(byte[] b, int index, long n) {
		for (int i = 0; i < 8; i++)
			b[index + i] = (byte) (n >> (i * 8) & 0xff);
	}

	public static void fromShort(byte[] b, int index, short n) {
		for (int i = 0; i < 2; i++)
			b[index + i] = (byte) (n >> (i * 8) & 0xff);
	}

	public static byte[] getSignature(int n) {
		byte[] ans = new byte[4];
		fromInt(ans, 0, n);
		return ans;
	}

	public static P toABP(int[] datas, int index) {
		double x = toShort(datas, index);
		double y = toShort(datas, index + 2);
		return new P(x * 0.1 - 200, y * 0.1 - 200);
	}

	/** read a number from a byte[] start from index */

	public static int toByte(int[] datas, int index) {
		return datas[index];
	}

	public static double toDouble(int[] datas, int index) {
		return Double.longBitsToDouble(toLong(datas, index));
	}

	public static float toFloat(int[] datas, int index) {
		return Float.intBitsToFloat(toInt(datas, index));
	}

	public static int toInt(int[] datas, int index) {
		int ans = 0;
		for (int i = 0; i < 4; i++)
			ans += (datas[index + i]) << (i * 8);
		return ans;
	}

	public static long toLong(int[] datas, int index) {
		long l = 0;
		for (int i = 0; i < 8; i++) {
			long temp = datas[index + i];
			temp <<= i * 8;
			l += temp;
		}
		return l;
	}

	public static int toShort(int[] datas, int index) {
		short ans = 0;
		for (int i = 0; i < 2; i++)
			ans += (datas[index + i]) << (i * 8);
		return ans;
	}

	/** translate byte[] to int[] in order to make all the bytes unsigned */

	public static int[] translate(byte[] datas) {
		int[] ans = new int[datas.length];
		for (int i = 0; i < ans.length; i++)
			ans[i] = Byte.toUnsignedInt(datas[i]);
		return ans;
	}

	public static byte[] translate(int[] datas) {
		byte[] ans = new byte[datas.length];
		for (int i = 0; i < ans.length; i++)
			ans[i] = (byte) datas[i];
		return ans;
	}

}
