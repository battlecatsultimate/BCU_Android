package com.mandarin.bcu.util.system.files;

import java.io.File;

public interface AssetData extends FileData {

	public static AssetData getAsset(File bs) {
		return new DefAsset(bs);
	}

}

class DefAsset extends FDFile implements AssetData {

	public DefAsset(File bs) {
		super(bs);
	}

}
