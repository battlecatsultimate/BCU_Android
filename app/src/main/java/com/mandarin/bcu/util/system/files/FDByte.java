package com.mandarin.bcu.util.system.files;

public class FDByte extends FileByte {

	public FDByte(byte[] bs) {
		super(bs);
	}

}

class FileByte implements ByteData {

	private byte[] data;

	public FileByte(byte[] bs) {
		data = bs;
	}

	@Override
	public byte[] getBytes() {
		return data;
	}

}
