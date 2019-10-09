package com.mandarin.bcu.androidutil.animation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics;

import common.battle.data.MaskAtk;
import common.system.P;
import common.util.ImgCore;
import common.util.anim.EAnimU;
import common.util.unit.Form;

public class AnimationCView extends View {
    public EAnimU anim;
    private boolean night;
    private Renderer renderer;

    private TextView textView;
    private SeekBar seekBar;

    private Canvas c;
    private Paint p = new Paint();
    private Paint p1 = new Paint();
    private Paint fp = new Paint();
    private Paint range = new Paint();
    private CVGraphics cv;

    private P p2;

    private boolean isUnit;

    private float start;
    private float end;
    private float tb;
    private boolean l;

    private long t = -1;
    private long t1 = -1;
    long fps;
    boolean drawFPS;

    public float siz = 1;

    public float x = 0;
    public float y = 0;

    long sleeptime;

    boolean started = false;

    public AnimationCView(Context context, int id, int form, int mode, boolean night,boolean axis, boolean fps, TextView textView, SeekBar seekBar) {
        super(context);

        renderer = new Renderer();

        anim = StaticStore.units.get(id).forms[form].getEAnim(mode);

        anim.setTime(StaticStore.frame);

        this.textView = textView;
        this.seekBar = seekBar;

        ImgCore.ref = axis;

        drawFPS = fps;

        range.setStyle(Paint.Style.STROKE);

        if(night) {
            p.setColor(0x363636);
            range.setColor(Color.GREEN);
            fp.setColor(Color.WHITE);
        } else {
            p.setColor(Color.WHITE);
            range.setColor(Color.RED);
            fp.setColor(Color.BLACK);
        }

        fp.setTextSize(StaticStore.dptopx(20f,context));

        p1.setFilterBitmap(true);

        p2 = new P((float)getWidth()/2,(float)getHeight()*2f/3f);
        cv = new CVGraphics(c, p1,night);

        isUnit = true;

        Form f = StaticStore.units.get(id).forms[form];

        int tb = f.du.getRange();
        MaskAtk atk = f.du.getRepAtk();

        int lds = atk.getShortPoint();
        int ldr = atk.getLongPoint()-atk.getShortPoint();

        start = Math.min(lds,lds+ldr);
        end = Math.max(lds,lds+ldr);

        this.tb = tb;

        l = lds>0;

        this.night = night;
    }

    public AnimationCView(Context context, int id, int mode, boolean night, boolean axis, boolean fps, TextView textView, SeekBar seekBar) {
        super(context);

        anim = StaticStore.enemies.get(id).getEAnim(mode);

        anim.setTime(StaticStore.frame);

        this.textView = textView;
        this.seekBar = seekBar;

        renderer = new Renderer();

        ImgCore.ref = axis;

        drawFPS = fps;

        if(night) {
            p.setColor(0x363636);
            fp.setColor(Color.WHITE);
        } else {
            p.setColor(Color.WHITE);
            fp.setColor(Color.BLACK);
        }

        fp.setTextSize(StaticStore.dptopx(20f,context));

        p1.setFilterBitmap(true);

        p2 = new P((float)getWidth()/2,(float)getHeight()*2f/3f);
        cv = new CVGraphics(c,p1,night);

        isUnit = false;

        this.night = night;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        postDelayed(renderer,0);
        started = true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        c = canvas;

        if(StaticStore.play) {
            if (t1 != -1 && t - t1 != 0) {
                fps = 1000L / (t - t1);
            }

            p2 = new P((float) getWidth() / 2 + x, (float) getHeight() * 2 / 3 + y);
            cv = new CVGraphics(c, p1, night);

            if (fps < 25)
                sleeptime = (long) (sleeptime * 0.9 - 0.1);
            else if (fps > 25)
                sleeptime = (long) (sleeptime * 0.9 + 0.1);

            c.drawRect(0, 0, getWidth(), getHeight(), p);

            if (drawFPS)
                c.drawText("FPS : " + fps, 50, 50, fp);

            anim.draw(cv, p2, siz);
            anim.update(true);

            StaticStore.frame++;

            t1 = t;
        } else {
            if (t1 != -1 && t - t1 != 0) {
                fps = 1000L / (t - t1);
            }

            p2 = new P((float) getWidth() / 2 + x, (float) getHeight() * 2 / 3 + y);
            cv = new CVGraphics(c, p1, night);

            if (fps < 25)
                sleeptime = (long) (sleeptime * 0.9 - 0.1);
            else if (fps > 25)
                sleeptime = (long) (sleeptime * 0.9 + 0.1);

            c.drawRect(0, 0, getWidth(), getHeight(), p);

            if (drawFPS)
                c.drawText("FPS : " + fps, 50, 50, fp);

            anim.draw(cv, p2, siz);

            t1 = t;
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        removeCallbacks(renderer);
    }

    private class Renderer implements Runnable {

        @Override
        public void run() {
            t = System.currentTimeMillis();

            invalidate();

            textView.setText(getContext().getText(R.string.anim_frame).toString().replace("-",""+StaticStore.frame));

            seekBar.setProgress(StaticStore.frame);

            if(started)
                postDelayed(this,1000L/30L+sleeptime);
        }
    }
}
