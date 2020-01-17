package com.mandarin.bcu.androidutil.fakeandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import common.system.fake.FakeImage;
import common.system.fake.ImageBuilder;

public class BMBuilder extends ImageBuilder {
    @Override
    public FIBM build(Object o) {
        if(o == null)
            return null;
        if(o instanceof Bitmap)
            return new FIBM((Bitmap)o);
        if(o instanceof FIBM)
            return (FIBM) o;

        Bitmap b = null;

        if(o instanceof File)
            b = BitmapFactory.decodeFile(((File) o).getAbsolutePath());
        else if (o instanceof byte[]) {
            b = BitmapFactory.decodeByteArray((byte[])o,0,((byte[]) o).length);
        }
        return b == null ? null:new FIBM(b);
    }

    @Override
    public boolean write(FakeImage img, String fmt, Object o) throws IOException {
        Bitmap b = (Bitmap)img.bimg();

        if(b == null)
            return false;
        if(o instanceof File) {
            OutputStream os = new FileOutputStream((File)o);
            return b.compress(Bitmap.CompressFormat.PNG,100,os);
        }
        if(o instanceof OutputStream)
            return b.compress(Bitmap.CompressFormat.PNG,100,(OutputStream) o);
        return false;
    }
}
