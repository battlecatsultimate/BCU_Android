package com.mandarin.bcu.androidutil.pack;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import common.CommonStatic;
import common.io.InStream;
import common.system.VImg;
import common.system.fake.FakeImage;
import common.util.Data;

public class AMusicLoader implements CommonStatic.ImgReader {
    private final int pid;
    private final int mid;

    public AMusicLoader(int pid, int mid) {
        this.pid = pid;
        this.mid = mid;
    }

    @Override
    public File readFile(InStream is) {
        byte[] bs = is.subStream().nextBytesI();

        String path = "./res/music/"+ Data.hex(pid)+"/";
        String name = Data.trio(mid)+".ogg";

        File f = CommonStatic.def.route(path);

        if(!f.exists()) {
            boolean done = f.mkdirs();

            if(!done) {
                Log.e("AMusicLoader","Failed to create folder "+path);
                return null;
            }
        }

        try {
            File g = CommonStatic.def.route(path+name);

            if(!g.exists()) {
                boolean done = g.createNewFile();

                if(!done) {
                    Log.e("AMusicLoader","Failed to create file "+path+name);
                    return null;
                }
            }

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(g));

            bos.write(bs);
            bos.flush();
            bos.close();

            return g;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public FakeImage readImg(String str) {
        return null;
    }

    @Override
    public VImg readImgOptional(String str) {
        return null;
    }
}
