
package com.mandarin.bcu.io;

public class BCMusic {

}
/*
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import util.pack.MusicStore;
import util.pack.Pack;

public class BCMusic {

	public static final BCMusic DEF = new BCMusic();

	public static boolean play = false;
	public static int music = -1;

	private static boolean allow = false;

	public static void play(int ind) {
		music = ind;
		File f = MusicStore.getMusic(ind);
		if (f != null)
			DEF.set(f);
	}

	public static void read() {
		File dict = new File("./assets/music/");
		if (!dict.exists())
			return;
		File[] fs = dict.listFiles();
		for (File f : fs) {
			String str = f.getName();
			if (str.length() != 7)
				continue;
			if (!str.endsWith(".ogg"))
				continue;
			int id = Reader.parseIntN(str.substring(0, 3));
			if (id == -1)
				continue;
			Pack.def.ms.set(id, f);
		}
	}

	private SourceDataLine line;
	private AudioInputStream ais;
	private Thread player;

	public void set(File file) {
		stop();
		if (!play)
			return;
		Thread prev = player;
		allow = true;
		player = new Thread() {

			@Override
			public void run() {
				try {
					if (prev != null)
						prev.join();
					if (!allow || player != this)
						return;

					AudioInputStream raw = AudioSystem.getAudioInputStream(file);
					AudioFormat out = getOutFormat(raw.getFormat());
					DataLine.Info info = new DataLine.Info(SourceDataLine.class, out);
					line = (SourceDataLine) AudioSystem.getLine(info);
					if (line != null) {
						line.open(out);
						line.start();
						ais = AudioSystem.getAudioInputStream(out, raw);
						stream();
						line.drain();
						line.stop();
						line.close();
						line = null;
					}
					if (allow && player == this)
						set(file);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}

		};

		player.start();

	}

	public void stop() {
		if (line != null)
			line.stop();
		allow = false;
	}

	private AudioFormat getOutFormat(AudioFormat inFormat) {
		final int ch = inFormat.getChannels();
		final float rate = inFormat.getSampleRate();
		AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
		return audioFormat;
	}

	private void stream() throws IOException {
		final byte[] buffer = new byte[65536];
		int num = ais.read(buffer, 0, buffer.length);
		while (num != -1) {
			line.write(buffer, 0, num);
			num = ais.read(buffer, 0, buffer.length);
		}
	}

}
*/