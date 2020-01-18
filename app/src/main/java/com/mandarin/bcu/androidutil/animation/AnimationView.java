package com.mandarin.bcu.androidutil.animation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics;

import common.system.P;
import common.util.anim.EAnimU;

public class AnimationView extends SurfaceView implements SurfaceHolder.Callback {
    final SurfaceHolder holder;
    UnitAnimation animation;
    EAnimU anim;
    Handler h = new Handler();

    Canvas c;
    Paint p = new Paint();
    Paint bp = new Paint();
    Paint p2 = new Paint();
    P p1;

    boolean stop = false;

    public AnimationView(Context context, int id, int form) {
        super(context);

        holder = getHolder();
        holder.addCallback(this);
        anim = StaticStore.units.get(id).forms[form].getEAnim(0);
        animation = new AnimationView.UnitAnimation();

        p2.setColor(Color.WHITE);

        p.setFilterBitmap(true);
        p.setAntiAlias(true);
        p.setDither(true);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        h.postDelayed(animation, 0);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop = true;
        h.removeCallbacks(animation);
    }

    class UnitAnimation implements Runnable {

        @Override
        public void run() {
            long t;
            long t2;

            if (!stop) {
                t2 = System.currentTimeMillis();

                c = holder.lockCanvas();

                p1 = new P((float) getWidth() / 2, (float) getHeight() / 2);

                if (c == null) return;

                CVGraphics graphics = new CVGraphics(c, p, bp, false);

                try {
                    synchronized (holder) {
                        c.drawRect(new RectF(0, 0, getWidth(), getHeight()), p2);

                        anim.draw(graphics, p1, 1);
                        anim.update(true);

                        t = System.currentTimeMillis();

                        long d = (1000 / 30) - (t - t2);

                        if (d > 0)
                            h.postDelayed(this, d);
                        else
                            h.postDelayed(this, 0);
                    }
                } finally {
                    holder.unlockCanvasAndPost(c);
                }
            }
        }
    }
}
