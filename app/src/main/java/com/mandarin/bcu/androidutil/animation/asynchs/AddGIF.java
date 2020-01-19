package com.mandarin.bcu.androidutil.animation.asynchs;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;

import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics;

import common.system.P;
import common.util.anim.EAnimU;

public class AddGIF extends AsyncTask<Void, Void, Void> {
    private EAnimU animU;
    private final int w;
    private final int h;
    private final float siz;
    private final P p;
    private final boolean night;

    public AddGIF(EAnimU animU, int w, int h, P p, float siz, boolean night, int id, boolean unit) {
        if (unit) {
            this.animU = StaticStore.units.get(id).forms[StaticStore.formposition].getEAnim(StaticStore.animposition);
            this.animU.setTime(StaticStore.frame);
        } else {
            this.animU = StaticStore.enemies.get(id).getEAnim(StaticStore.animposition);
            this.animU.setTime(StaticStore.frame);
        }

        this.w = w;
        this.h = h;
        this.siz = siz;
        this.p = p;
        this.night = night;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas c = new Canvas(b);
        Paint p1 = new Paint();
        Paint bp = new Paint();

        p1.setFilterBitmap(true);
        p1.setAntiAlias(true);

        Paint back = new Paint();

        if (night)
            back.setColor(Color.argb(255, 54, 54, 54));
        else
            back.setColor(Color.WHITE);

        CVGraphics c2 = new CVGraphics(c, p1, bp, night);

        c.drawRect(0, 0, w, h, back);
        animU.draw(c2, p, siz);

        StaticStore.frames.add(b);
        StaticStore.gifFrame++;

        return null;
    }
}
