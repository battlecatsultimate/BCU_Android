package com.mandarin.bcu.util.system.fake;

import java.io.IOException;

public abstract class ImageBuilder {

	public static ImageBuilder builder;

	public static boolean icon = false;

	public abstract FakeImage build(Object o) throws IOException;

	public abstract boolean write(FakeImage o, String fmt, Object out) throws IOException;

}
