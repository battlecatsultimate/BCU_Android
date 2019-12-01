package com.mandarin.bcu.androidutil.animation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.animation.asynchs.AddGIF;
import com.mandarin.bcu.androidutil.animation.asynchs.GIFAsync;
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics;

import common.system.P;
import common.util.ImgCore;
import common.util.anim.EAnimU;

public class AnimationCView extends View {
    public EAnimU anim;
    private boolean night;
    public boolean trans = false;
    private Renderer renderer;

    private int id;
    private int form = -1;

    private TextView textView;
    private SeekBar seekBar;
    private TextView fpsind;
    public TextView gif;

    private Canvas c;
    private Paint p = new Paint();
    private Paint p1 = new Paint();
    private Paint range = new Paint();
    private CVGraphics cv;

    private P p2;

    private GIFAsync async;

    private long t = -1;
    private long t1 = -1;
    long fps;

    public float siz = 1;

    public float x = 0;
    public float y = 0;

    long sleeptime;

    boolean started = false;

    public AnimationCView(Context context, int id, int form, int mode, boolean night,boolean axis, TextView textView, SeekBar seekBar, TextView fpsind, TextView gif) {
        super(context);

        renderer = new Renderer();

        this.id = id;
        this.form = form;

        anim = StaticStore.units.get(id).forms[form].getEAnim(mode);

        anim.setTime(StaticStore.frame);

        this.textView = textView;
        this.seekBar = seekBar;
        this.fpsind = fpsind;
        this.gif = gif;

        ImgCore.ref = axis;

        range.setStyle(Paint.Style.STROKE);

        if(night) {
            p.setColor(Color.argb(255,54,54,54));
            range.setColor(Color.GREEN);
        } else {
            p.setColor(Color.argb(255,255,255,255));
            range.setColor(Color.RED);
        }

        p1.setFilterBitmap(true);

        p2 = new P((float)getWidth()/2,(float)getHeight()*2f/3f);
        cv = new CVGraphics(c, p1,night);

        this.night = night;
    }

    public AnimationCView(Context context, int id, int mode, boolean night, boolean axis, TextView textView, SeekBar seekBar, TextView fpsind, TextView gif) {
        super(context);

        this.id = id;

        anim = StaticStore.enemies.get(id).getEAnim(mode);

        anim.setTime(StaticStore.frame);

        this.textView = textView;
        this.seekBar = seekBar;
        this.fpsind = fpsind;
        this.gif = gif;

        renderer = new Renderer();

        ImgCore.ref = axis;

        if(night) {
            p.setColor(0x363636);
        } else {
            p.setColor(Color.WHITE);
        }

        p1.setFilterBitmap(true);

        p2 = new P((float)getWidth()/2,(float)getHeight()*2f/3f);
        cv = new CVGraphics(c,p1,night);

        this.night = night;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        postDelayed(renderer,0);
        started = true;
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        c = canvas;

        if(StaticStore.gifisSaving && !StaticStore.keepDoing) {
            async.keepDoing = false;
            StaticStore.keepDoing = true;
        }

        if(StaticStore.enableGIF) {
            p2 = new P((float) getWidth() / 2 + x, (float) getHeight() * 2 / 3 + y);

            if(form != -1)
                new AddGIF(anim,getWidth(),getHeight(),p2,siz,night,id,true).execute();
            else
                new AddGIF(anim,getWidth(),getHeight(),p2,siz,night,id,false).execute();
        }

        if(StaticStore.play) {
            if (t1 != -1 && t - t1 != 0) {
                fps = 1000L / (t - t1);
            }

            p2 = new P((float) getWidth() / 2 + x, (float) getHeight() * 2 / 3 + y);
            cv = new CVGraphics(c, p1, night);

            if (fps < 30)
                sleeptime = (int)(sleeptime*0.9 - 0.1);
            else if (fps > 30)
                sleeptime = (int)(sleeptime*0.9 + 0.1);

            if(!trans)
                c.drawRect(0, 0, getWidth(), getHeight(), p);

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

            if (fps < 30)
                sleeptime = (int)(sleeptime*0.9 - 0.1);
            else if (fps > 30)
                sleeptime = (int)(sleeptime*0.9 + 0.1);

            if(!trans)
                c.drawRect(0, 0, getWidth(), getHeight(), p);

            anim.draw(cv, p2, siz);

            t1 = t;
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        removeCallbacks(renderer);
    }

    public void StartAsync(Activity activity) {
        if(form != -1) {
            async = new GIFAsync(this, activity,id,form);
        } else {
            async = new GIFAsync(this,activity,id);
        }
        async.execute();
    }

    private class Renderer implements Runnable {

        @Override
        public void run() {
            t = System.currentTimeMillis();

            invalidate();

            textView.setText(getContext().getText(R.string.anim_frame).toString().replace("-",""+StaticStore.frame));
            fpsind.setText(getContext().getText(R.string.def_fps).toString().replace("-",""+fps));
            seekBar.setProgress(StaticStore.frame);
            gif.setText(getContext().getText(R.string.anim_gif_frame).toString().replace("-",""+StaticStore.gifFrame));

            if(started)
                postDelayed(this,1000L/30L+sleeptime);
        }
    }
}
