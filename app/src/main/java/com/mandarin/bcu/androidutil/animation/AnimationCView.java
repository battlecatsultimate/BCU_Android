package com.mandarin.bcu.androidutil.animation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics;

import common.system.P;
import common.util.anim.EAnimU;

public class AnimationCView extends View {
    public EAnimU anim;
    private boolean night;

    private Canvas c;
    private Paint p = new Paint();
    private Paint p1 = new Paint();
    private CVGraphics cv;

    private P p2;

    public float siz = 1;

    public float x = 0;
    public float y = 0;

    long t = 0;
    long t2 = 0;

    long sleeptime;

    public AnimationCView(Context context, int id, int form, int mode, boolean night) {
        super(context);

        anim = StaticStore.units.get(id).forms[form].getEAnim(mode);

        if(night)
            p.setColor(0x363636);
        else
            p.setColor(Color.WHITE);

        p1.setFilterBitmap(true);
        p1.setDither(true);
        p1.setAntiAlias(true);

        p2 = new P((float)getWidth()/2,(float)getHeight()*2f/3f);
        cv = new CVGraphics(c, p1,night);

        this.night = night;
    }

    public AnimationCView(Context context, int id, int mode, boolean night) {
        super(context);

        anim = StaticStore.enemies.get(id).getEAnim(mode);

        if(night)
            p.setColor(0x363636);
        else
            p.setColor(Color.WHITE);

        p1.setFilterBitmap(true);
        p1.setDither(true);
        p1.setAntiAlias(true);

        p2 = new P((float)getWidth()/2,(float)getHeight()*2f/3f);
        cv = new CVGraphics(c,p1,night);

        this.night = night;
    }

    @Override
    public void onDraw(Canvas canvas) {
        c = canvas;

        p2 = new P((float)getWidth()/2+x,(float)getHeight()*2/3+y);
        cv = new CVGraphics(c,p1,night);

        c.drawRect(0,0,getWidth(),getHeight(),p);

        anim.draw(cv, p2,siz);
        anim.update(true);

        t2 = System.currentTimeMillis();

        sleeptime = t != 0 ? (1000L/15L)+t-t2 < 0?  1 : (1000L/15L)+t-t2 : (1000L/30L);

        sleeptime = sleeptime > 1000L/30L ? 1000L/30L : sleeptime;

        try {
            Thread.sleep(sleeptime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        t = t2;

        invalidate();
    }
}
