package com.mandarin.bcu.util.system.files;

public interface AssetData extends ByteData {

	public static AssetData getAsset(byte[] bs) {
		return new DefAsset(bs);
	}

}

class DefAsset extends FileByte implements AssetData {

	public DefAsset(byte[] bs) {
		super(bs);
	}

}
