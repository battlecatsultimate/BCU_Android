package com.mandarin.bcu.androidutil.io;

import java.io.IOException;

import common.io.InStream;
import common.system.VImg;
import common.system.fake.FakeImage;
import common.system.files.FDByte;
import common.system.files.FileData;
import common.util.Res;
import common.util.anim.AnimC;

public class AACLoader implements AnimC.AnimLoader {
    private String name;
    private FakeImage num;
    private FileData imgcut;
    private FileData mamodel;
    private FileData[] anims;
    private VImg uni, edi;

    public AACLoader(InStream is) {
        name = "local anim";

        try {
            num = FakeImage.read(is.nextBytesI());
        } catch (IOException e) {
            e.printStackTrace();
        }

        imgcut = new FDByte(is.nextBytesI());
        mamodel = new FDByte(is.nextBytesI());
        int n = is.nextInt();

        anims = new FileData[n];

        for(int i = 0; i < n; i++) {
            anims[i] = new FDByte(is.nextBytesI());
        }

        if(!is.end()) {
            VImg img = new VImg(is.nextBytesI());
            img.mark("unit or edi");
            if(img.getImg().getHeight() == 32)
                edi = img;
            else
                uni = img;
        }

        if(!is.end())
            uni = new VImg(is.nextBytesI());

        if(uni != null && uni != Res.slot[0])
            uni.mark("uni");

        if(edi != null)
            edi.mark("edi");
    }

    @Override
    public VImg getEdi() {
        return edi;
    }

    @Override
    public FileData getIC() {
        return imgcut;
    }

    @Override
    public FileData[] getMA() {
        return anims;
    }

    @Override
    public FileData getMM() {
        return mamodel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FakeImage getNum() {
        return num;
    }

    @Override
    public int getStatus() {
        return 1;
    }

    @Override
    public VImg getUni() {
        return uni;
    }
}
