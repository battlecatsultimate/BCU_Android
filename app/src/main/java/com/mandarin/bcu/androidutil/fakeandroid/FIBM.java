package com.mandarin.bcu.androidutil.fakeandroid;

import android.graphics.Bitmap;

import java.io.IOException;

import common.system.fake.FakeImage;
import common.system.fake.ImageBuilder;

public class FIBM implements FakeImage {

    public static final ImageBuilder builder = new BMBuilder();

    public static FakeImage build(Bitmap bimg2) {
        try {
            return builder.build(bimg2);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private final Bitmap bit;

    FIBM(Bitmap read) {
        bit = read.copy(Bitmap.Config.ARGB_8888, true);
    }

    @Override
    public Bitmap bimg() {
        return bit;
    }

    @Override
    public int getHeight() {
        return bit.getHeight();
    }

    @Override
    public int getWidth() {
        return bit.getWidth();
    }

    @Override
    public int getRGB(int i, int j) {
        return bit.getPixel(i, j);
    }

    @Override
    public FIBM getSubimage(int i, int j, int k, int l) {
        try {
            return (FIBM) builder.build(Bitmap.createBitmap(bit, i, j, k, l));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Object gl() {
        return null;
    }

    @Override
    public void setRGB(int i, int j, int p) {
        bit.setPixel(i, j, p);
    }
}