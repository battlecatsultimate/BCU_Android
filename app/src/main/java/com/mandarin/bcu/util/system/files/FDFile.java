package com.mandarin.bcu.util.system.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Queue;

import com.mandarin.bcu.util.system.fake.FakeImage;

public class FDFile implements FileData {

	private final File file;

	public FDFile(File f) {
		file = f;
	}

	@Override
	public FakeImage getImg() {
		try {
			return FakeImage.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Queue<String> readLine() {
		try {
			BufferedReader input = new BufferedReader(new FileReader(file.toString()));
			if(!input.ready()) {
				throw new IOException();
			}
			String line;
			ArrayDeque<String> res = new ArrayDeque<>();
			while((line = input.readLine()) != null) {
				res.add(line);
			}
			input.close();
			return res;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		return file.getName();
	}

}
