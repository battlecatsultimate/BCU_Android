package com.mandarin.bcu.androidutil.pack;

import android.graphics.Bitmap;
import android.util.Log;

import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.fakeandroid.FIBM;
import com.mandarin.bcu.androidutil.io.DefferedLoader;

import java.io.File;
import java.io.FileOutputStream;
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
import common.system.files.FDByte;
import common.util.Res;
import common.util.anim.AnimCI;
import common.util.anim.ImgCut;
import common.util.anim.MaAnim;
import common.util.anim.MaModel;

public class AACLoader implements AnimCI.AnimLoader {
    private String name;
    private FakeImage num;
    private ImgCut imgcut;
    private MaModel mamodel;
    private MaAnim[] anims;
    private VImg uni, edi;

    public AACLoader(InStream is, String dir, String name) {
        this.name = "local anim";

        String path;

        if(!name.equals("")) {
            path = dir+"/res/img/" + name + "/";
        } else {
            path = dir+"/res/img/" + findName(dir+"/res/img/","") + "/";
        }

        String nam = findName(path,".png");

        try {
            num = FakeImage.read(is.nextBytesI());

            String [] numInfo = extractImage(num, path, nam+".png", true);

            if(numInfo.length == 2) {
                ((FIBM)num).reference = numInfo[0];
                ((FIBM)num).password = numInfo[1];
            } else if(numInfo.length == 1) {
                ((FIBM)num).reference = numInfo[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        imgcut = ImgCut.newIns(new FDByte(is.nextBytesI()));
        mamodel = MaModel.newIns(new FDByte(is.nextBytesI()));

        int n = is.nextInt();

        anims = new MaAnim[n];

        for(int i = 0; i < n; i++) {
            anims[i] = MaAnim.newIns(new FDByte(is.nextBytesI()));
        }

        if(!is.end()) {
            VImg img = new VImg(is.nextBytesI());
            if(img.getImg().getHeight() == 32) {
                img.mark(FakeImage.Marker.EDI);
                edi = img;
            } else {
                img.mark(FakeImage.Marker.UNI);
                uni = img;
            }
        }

        if(!is.end())
            uni = new VImg(is.nextBytesI());

        if(uni != null && uni != Res.slot[0]) {
            uni.mark(FakeImage.Marker.UNI);

            try {
                String [] uniInfo = extractImage(uni.bimg, path, nam+"-uni.png", false);

                if(uniInfo.length == 2) {
                    ((FIBM)uni.bimg).reference = uniInfo[0]+"\\"+uniInfo[1];
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(edi != null) {
            edi.mark(FakeImage.Marker.EDI);

            try {
                String[] ediInfo = extractImage(edi.bimg, path, nam+"-edi.png", false);

                if(ediInfo.length == 2) {
                    ((FIBM)edi.bimg).reference = ediInfo[0]+"\\"+ediInfo[1];
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public AACLoader(InStream is, CommonStatic.ImgReader reader) {
        is.nextString();

        String numRef = is.nextString();

        num = reader.readImg(numRef);

        try {
            DefferedLoader.Companion.getPending().add(new DefferedLoader<>("Context", num, num.getClass().getDeclaredField("password"), c -> StaticStore.getPassword(((AImageReader)reader).name, ((FIBM) num).reference, c)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        edi = reader.readImgOptional(is.nextString());
        uni = reader.readImgOptional(is.nextString());
        imgcut = ImgCut.newIns(new FDByte(is.nextBytesI()));
        mamodel = MaModel.newIns(new FDByte(is.nextBytesI()));

        int n = is.nextInt();

        anims = new MaAnim[n];

        for(int i = 0; i < n; i++) {
            anims[i] = MaAnim.newIns(new FDByte(is.nextBytesI()));
        }
    }

    @Override
    public VImg getEdi() {
        return edi;
    }

    @Override
    public ImgCut getIC() {
        return imgcut;
    }

    @Override
    public MaAnim[] getMA() {
        return anims;
    }

    @Override
    public MaModel getMM() {
        return mamodel;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public FakeImage getNum(boolean load) {
        if(load) {
            check();
        }

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

    private String findName(String path, String extension) {
        int i = 0;

        while(true) {
            File f = new File(path+number(i)+extension);

            if(f.exists()) {
                i += 1;
            } else {
                return number(i);
            }
        }
    }

    private String number(int i) {
        if(0 <= i && i < 10) {
            return "00"+i;
        } else if(10 <= i && i < 100) {
            return "0"+i;
        } else {
            return Integer.toString(i);
        }
    }

    private String[] extractImage(FakeImage img, String path, String name, boolean unload) throws IOException {
        if(img == null)
            return new String[] {"",""};

        File f = new File(path);

        if(!f.exists()) {
            if(!f.mkdirs()) {
                Log.e("AACLoader", "Failed to create directory " + f.getAbsolutePath());
                return new String[] {"", ""};
            }
        }

        File g =  new File(path, name);

        if(!g.exists()) {
            if(!g.createNewFile()) {
                Log.e("AACLoader", "Failed to create file " + g.getAbsolutePath());
                return new String [] {"", ""};
            }
        }

        ((Bitmap)img.bimg()).compress(Bitmap.CompressFormat.PNG, 0, new FileOutputStream(g));

        if(unload) {
            img.unload();
        }

        try {
            return new String[] {g.getAbsolutePath().replace(".png",".bcuimg"), StaticStore.fileToMD5(g)};
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new String [] {g.getAbsolutePath().replace(".png",".bcuimg"), ""};
        }
    }

    private void check() {
        FIBM fibm = (FIBM) num;

        if(fibm.reference.equals("") || fibm.password.equals(""))
            return;

        if(((Bitmap)num.bimg()).isRecycled()) {
            try {
                String ref = fibm.reference;
                String password = fibm.password;

                String path = StaticStore.decryptPNG(ref, password, StaticStore.IV);

                File f = new File(path);

                if(f.exists()) {
                    num = FIBM.builder.build(f);

                    ((FIBM)num).reference = ref;
                    ((FIBM)num).password = password;

                    if(!f.delete()) {
                        Log.e("AACLoader", "Failed to delete file "+f.getAbsolutePath());
                    }
                }
            } catch (NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | IOException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }
    }
}
