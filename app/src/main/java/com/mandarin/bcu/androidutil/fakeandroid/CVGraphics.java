package com.mandarin.bcu.androidutil.fakeandroid;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;

import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.system.fake.FakeTransform;

public class CVGraphics implements FakeGraphics {
    private Canvas c;
    private final Paint p;

    private Matrix m = new Matrix();

    private int color;

    public CVGraphics(Canvas c, Paint p, boolean night) {
        this.c = c;
        this.p = p;

        if(night)
            color = Color.WHITE;
        else
            color = Color.BLACK;

        p.setColor(color);
    }


    @Override
    public void colRect(int x, int y, int w, int h, int r, int g, int b, int... a) {
        int a1 = a.length != 0 ? a[0] : 255;

        int rgba = Color.argb(a1,r,g,b);

        p.setColor(rgba);

        c.drawRect(new RectF(x,y,w,h),p);

        p.setColor(color);
    }

    @Override
    public void drawImage(FakeImage bimg, double x, double y) {
        Bitmap b = (Bitmap)bimg.bimg();

        c.drawBitmap(b,(float)x,(float)y,p);
    }

    @Override
    public void drawImage(FakeImage bimg, double x, double y, double d, double e) {
        Bitmap b = (Bitmap)bimg.bimg();

        Matrix m2 = new Matrix();

        c.setMatrix(m2);

        float w = b.getWidth();
        float h = b.getHeight();

        float wr = (float)d/w;
        float hr = (float)e/h;

        m2.set(m);

        m2.preTranslate((float)x,(float)y);
        m2.preScale(wr,hr);

        c.drawBitmap(b,m2,p);
    }

    @Override
    public void drawLine(int i, int j, int x, int y) {
        c.drawLine(i,j,x,y,p);
    }

    @Override
    public void drawOval(int i, int j, int k, int l) {
        p.setStyle(Paint.Style.STROKE);

        c.drawOval(i,j,k,l,p);
    }

    @Override
    public void drawRect(int x, int y, int x2, int y2) {
        p.setStyle(Paint.Style.STROKE);

        c.drawRect(x,y,x2,y2,p);
    }

    @Override
    public void fillOval(int i, int j, int k, int l) {
        p.setStyle(Paint.Style.FILL);

        c.drawOval(i,j,k,l,p);
    }

    @Override
    public void fillRect(int x, int y, int w, int h) {
        p.setStyle(Paint.Style.FILL);

        c.drawRect(x,y,x+w,y+h,p);
    }

    @Override
    public FakeTransform getTransform() {
        Matrix mc = new Matrix();

        mc.set(m);

        return new FTMT(mc);
    }

    @Override
    public void gradRect(int x, int y, int w, int h, int a, int b, int[] c, int d, int e, int[] f) {
        Shader s = new LinearGradient(x,y,x,x+h,Color.rgb(c[0],c[1],c[2]),Color.rgb(f[0],f[1],f[2]), Shader.TileMode.CLAMP);

        p.setShader(s);

        this.c.drawRect(x,y,x+w,y+h,p);
    }

    @Override
    public void rotate(double d) {
        m.preRotate((float)Math.toDegrees(d));
        c.setMatrix(m);
    }

    @Override
    public void scale(int hf, int vf) {
        m.preScale(hf,vf);
        c.setMatrix(m);
    }

    @Override
    public void setColor(int c) {
        color = c;
    }

    @Override
    public void setComposite(int mode, int... para) {
        if(mode == DEF) {
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
            p.setAlpha(255);
        } else if(mode == TRANS) {
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

            int alpha = para.length != 0 ? para[0] > 255 ? 255 : para[0] : 255;

            p.setAlpha(alpha);
        } else if(mode == BLEND) {
            int alpha = para.length != 0 ? para[0] > 255 ? 255 : para[0] : 255;

            int m = para.length > 1 ? para[1] : 0;

            switch (m) {
                case 0:
                    p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                    p.setAlpha(alpha);
                    break;
                case 1:
                    p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
                    p.setAlpha(alpha);
                    break;
            }
        }
    }

    @Override
    public void setRenderingHint(int key, int object) {

    }

    @Override
    public void setTransform(FakeTransform at) {
        Matrix m1 = (Matrix)at.getAT();
        m = m1;

        c.setMatrix(m1);
    }

    @Override
    public void translate(double x, double y) {
        m.preTranslate((float)x,(float)y);
        c.setMatrix(m);
    }

    public void reset() {
        m.reset();
    }
}
