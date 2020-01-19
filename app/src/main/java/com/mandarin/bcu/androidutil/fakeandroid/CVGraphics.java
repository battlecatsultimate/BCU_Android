package com.mandarin.bcu.androidutil.fakeandroid;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;

import java.util.ArrayDeque;
import java.util.Deque;

import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.system.fake.FakeTransform;

public class CVGraphics implements FakeGraphics {

    private Canvas c;
    private final Paint cp;
    private final Paint bp;
    private final Paint gp;
    private static Deque<FTMT> ftmt = new ArrayDeque<>();

    private PorterDuffXfermode src = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
    private PorterDuffXfermode add = new PorterDuffXfermode(PorterDuff.Mode.ADD);

    private Matrix m = new Matrix();
    private Matrix m2 = new Matrix();

    private int color;

    public static void clear() {
        ftmt.clear();
    }

    public CVGraphics(Canvas c, Paint cp, Paint bp, boolean night) {
        this.c = c;
        this.cp = cp;
        this.bp = bp;
        this.gp = cp;

        if (night)
            color = Color.WHITE;
        else
            color = Color.BLACK;

        this.cp.setColor(color);

        this.bp.setFilterBitmap(true);
        this.bp.setAntiAlias(true);

        gp.setStyle(Paint.Style.FILL);
        gp.setAlpha(Color.argb(255, 255, 255, 255));
    }

    public CVGraphics(Canvas c, Paint cp, Paint bp, Paint gp, boolean night) {
        this.c = c;
        this.cp = cp;
        this.bp = bp;
        this.gp = gp;

        if (night)
            color = Color.WHITE;
        else
            color = Color.BLACK;

        this.cp.setColor(color);

        this.bp.setFilterBitmap(true);
        this.bp.setAntiAlias(true);

        this.gp.setStyle(Paint.Style.FILL);
        this.gp.setAlpha(Color.argb(255, 255, 255, 255));
    }

    public void setCanvas(Canvas c) {
        this.c = c;
    }

    @Override
    public void drawImage(FakeImage bimg, double x, double y) {
        Bitmap b = (Bitmap) bimg.bimg();

        c.drawBitmap(b, (float) x, (float) y, bp);
    }

    @Override
    public void drawImage(FakeImage bimg, double x, double y, double d, double e) {
        Bitmap b = (Bitmap) bimg.bimg();

        m2.reset();

        c.setMatrix(m2);

        float w = b.getWidth();
        float h = b.getHeight();

        float wr = (float) d / w;
        float hr = (float) e / h;

        m2.set(m);

        m2.preTranslate((float) x, (float) y);
        m2.preScale(wr, hr);

        c.drawBitmap(b, m2, bp);
    }

    @Override
    public void drawLine(int i, int j, int x, int y) {
        c.drawLine(i, j, x, y, cp);
    }

    @Override
    public void drawOval(int i, int j, int k, int l) {
        cp.setStyle(Paint.Style.STROKE);

        c.drawOval(i, j, k, l, cp);
    }

    @Override
    public void drawRect(int x, int y, int x2, int y2) {
        cp.setStyle(Paint.Style.STROKE);
        c.drawRect(x, y, x + x2, y + y2, cp);
    }

    @Override
    public void fillOval(int i, int j, int k, int l) {
        cp.setStyle(Paint.Style.FILL);

        c.drawOval(i, j, k, l, cp);
    }

    @Override
    public void fillRect(int x, int y, int w, int h) {
        cp.setStyle(Paint.Style.FILL);

        c.drawRect(x, y, x + w, y + h, cp);
    }

    @Override
    public FakeTransform getTransform() {
        if (!ftmt.isEmpty()) {
            FTMT f = ftmt.pollFirst();
            f.updateMatrix(m);

            return f;
        }

        return new FTMT(m);
    }

    @Override
    public void gradRect(int x, int y, int w, int h, int a, int b, int[] c, int d, int e, int[] f) {
        Shader s = new LinearGradient(x, y, x, x + h, Color.rgb(c[0], c[1], c[2]), Color.rgb(f[0], f[1], f[2]), Shader.TileMode.CLAMP);

        gp.setShader(s);

        this.c.drawRect(x, y, x + w, y + h, gp);
    }

    @Override
    public void rotate(double d) {
        m.preRotate((float) Math.toDegrees(d));
        c.setMatrix(m);
    }

    @Override
    public void scale(int hf, int vf) {
        m.preScale(hf, vf);
        c.setMatrix(m);
    }

    @Override
    public void setColor(int c) {
        switch (c) {
            case RED:
                color = Color.RED;
                cp.setColor(Color.RED);
                break;
            case YELLOW:
                color = Color.YELLOW;
                cp.setColor(Color.YELLOW);
                break;
            case BLACK:
                color = Color.BLACK;
                cp.setColor(Color.BLACK);
                break;
            case MAGENTA:
                color = Color.MAGENTA;
                cp.setColor(Color.MAGENTA);
                break;
            case BLUE:
                color = Color.BLUE;
                cp.setColor(Color.BLUE);
                break;
            case CYAN:
                color = Color.CYAN;
                cp.setColor(Color.CYAN);
                break;
            case WHITE:
                color = Color.WHITE;
                cp.setColor(Color.WHITE);
                break;
        }
    }

    @Override
    public void setComposite(int mode, int p0, int p1) {
        int alpha = p0;

        if (alpha < 0) alpha = 0;

        if (alpha > 255) alpha = 255;

        if (mode == DEF) {
            bp.setXfermode(src);
            bp.setAlpha(255);
        } else if (mode == TRANS) {
            bp.setXfermode(src);

            bp.setAlpha(alpha);
        } else if (mode == BLEND) {

            switch (p1) {
                case 0:
                    bp.setXfermode(src);
                    bp.setAlpha(alpha);
                    break;
                case 1:
                    bp.setXfermode(add);
                    bp.setAlpha(alpha);
                    break;
            }
        }
    }

    @Override
    public void setRenderingHint(int key, int object) {

    }

    @Override
    public void setTransform(FakeTransform at) {
        ((FTMT) at).setMatrix(m);

        c.setMatrix(m);
    }

    @Override
    public void translate(double x, double y) {
        m.preTranslate((float) x, (float) y);

        c.setMatrix(m);
    }

    @Override
    public void colRect(int x, int y, int w, int h, int r, int g, int b, int a) {
        int a1 = a;

        if (a1 < 0) a1 = 0;

        if (a1 > 255) a1 = 255;

        int rgba = Color.argb(a1, r, g, b);

        cp.reset();

        cp.setColor(rgba);

        cp.setStyle(Paint.Style.FILL);

        c.drawRect(x, y, x + w, y + h, cp);
    }

    @Override
    public void delete(FakeTransform at) {
        ftmt.add((FTMT) at);
    }

    private void Ctranslate(float x, float y) {
        m.preTranslate(x, y);
        c.setMatrix(m);
    }

    private void CsetTransform(FakeTransform at) {
        m.set((Matrix) at.getAT());

        c.setMatrix(m);
    }

    private void CScale(int hf, int vf) {
        m.preScale(hf, vf);
        c.setMatrix(m);
    }

    private void Crotate(double d) {
        m.preRotate((float) Math.toDegrees(d));
        c.setMatrix(m);
    }

    private void CdrawImage(FakeImage bimg, double x, double y, double d, double e) {
        Bitmap b = (Bitmap) bimg.bimg();

        m2.reset();

        c.setMatrix(m2);

        float w = b.getWidth();
        float h = b.getHeight();

        float wr = (float) d / w;
        float hr = (float) e / h;

        m2.set(m);

        m2.preTranslate((float) x, (float) y);
        m2.preScale(wr, hr);

        c.drawBitmap(b, m2, bp);
    }
}
