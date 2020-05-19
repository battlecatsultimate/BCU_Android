package com.mandarin.bcu.androidutil.pack;

import com.mandarin.bcu.androidutil.fakeandroid.FIBM;

import java.io.File;

import common.CommonStatic;
import common.system.VImg;
import common.system.fake.FakeImage;

public class AImageWriter implements CommonStatic.ImgWriter {
    @Override
    public String saveFile(File f) {
        return f.getAbsolutePath();
    }

    @Override
    public String writeImg(FakeImage img) {
        if(img != null) {
            return ((FIBM)img).reference;
        } else {
            return "";
        }
    }

    @Override
    public String writeImgOptional(VImg img) {
        if(img == null) {
            return "";
        } else {
            if(img.bimg == null) {
                return "";
            } else {
                return ((FIBM)img.bimg).reference;
            }
        }
    }
}
