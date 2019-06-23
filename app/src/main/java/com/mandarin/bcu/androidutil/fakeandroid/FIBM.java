package com.mandarin.bcu.androidutil.fakeandroid;

import android.graphics.Bitmap;

import com.mandarin.bcu.util.system.fake.FakeImage;
import com.mandarin.bcu.util.system.fake.ImageBuilder;

import java.io.IOException;

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

    protected FIBM(Bitmap read) {
        bit = read;
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
    public int getRGB(int i,int j) {
        return bit.getPixel(i,j);
    }

    @Override
    public FIBM getSubimage(int i,int j,int k,int l) {
        try {
            return (FIBM) builder.build(Bitmap.createBitmap(bit,i,j,k,l));
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
    public void setRGB(int i,int j,int p) {
        bit.setPixel(i,j,p);
    }
}