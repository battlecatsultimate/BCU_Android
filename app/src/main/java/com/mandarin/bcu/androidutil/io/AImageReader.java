package com.mandarin.bcu.androidutil.io;

import android.util.Log;

import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.fakeandroid.FIBM;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import common.CommonStatic;
import common.io.InStream;
import common.system.VImg;
import common.system.fake.FakeImage;

public class AImageReader implements CommonStatic.ImgReader {
    public String name;

    private boolean isNull;

    AImageReader(String n, boolean isNull) {
        name = n;
        this.isNull = isNull;
    }

    @Override
    public File readFile(InStream is) {
        return null;
    }

    /**
     * Generates FakeImage using specified path
     *
     * @param str Path of file
     *
     * @return Creates Bitmap from file and returns FakeImage
     */
    @Override
    public FakeImage readImg(String str) {
        if(str == null || str.equals(""))
            return null;

        try {
            return FIBM.builder.build(str);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public VImg readImgOptional(String str) {
        if(str.equals("")) {
            return null;
        }

        String[] info = str.split("\\\\");

        if(info.length != 2) {
            Log.e("ImageReader", "Invalid format : "+str);
            return new VImg(new FIBM(str));
        }

        String path = info[0];
        String password = info[1];

        try {
            String temp = StaticStore.decryptPNG(path, password, StaticStore.IV);

            File g = new File(temp);

            if(g.exists()) {
                return new VImg(FIBM.builder.build(g));
            } else {
                Log.e("ImageReader", "No such file found : "+g.getAbsolutePath());
                return new VImg(new FIBM(str));
            }
        } catch (NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | IOException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return new VImg(new FIBM(str));
        }
    }

    @Override
    public boolean isNull() {
        return isNull;
    }
}
