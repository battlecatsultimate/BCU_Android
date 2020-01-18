package com.mandarin.bcu.androidutil.battle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics;
import com.mandarin.bcu.util.page.BBCtrl;
import com.mandarin.bcu.util.page.BattleBox;

import common.battle.BattleField;
import common.battle.SBCtrl;
import common.util.ImgCore;
import common.util.anim.ImgCut;
import common.util.unit.Enemy;
import common.util.unit.Form;

@SuppressLint("ViewConstructor")
public class BattleView extends View implements BattleBox, BattleBox.OuterBox {
    public BBPainter painter;

    public boolean initialized = false;
    public boolean paused = false;

    public int spd = 0;
    public int upd = 0;

    private CVGraphics cv;
    private Updater updater;

    public BattleView(Context context, BattleField field, int type, boolean axis) {
        super(context);

        this.painter = type == 0 ? new BBPainter(this,field,this) : new BBCtrl(this,(SBCtrl) field,this);

        ImgCore.ref = axis;

        updater = new Updater();

        for(Form [] fs : painter.bf.sb.b.lu.fs)
            for(Form f : fs)
                if(f != null) {
                    if(f.anim.uni.getImg().getHeight() == f.anim.uni.getImg().getWidth()) {
                        ImgCut cut = ImgCut.newIns("./org/data/uni.imgcut");

                        f.anim.uni.setCut(cut);
                        f.anim.uni.setImg(f.anim.uni.getImg());

                        f.anim.check();
                    }
                }

        for(Enemy e : painter.bf.sb.st.data.getAllEnemy())
            e.anim.check();
        updater.run();

        Paint cp = new Paint();
        Paint bp = new Paint();
        Paint gp = new Paint();
        cv = new CVGraphics(null, cp, bp, gp,true);
    }

    @Override
    public void onDraw(Canvas c) {
        if(initialized) {
            cv.setCanvas(c);

            painter.draw(cv);

            if(!paused) {
                if(spd > 0) {
                    for (int i = 0; i < Math.pow(2, spd); i++)
                        painter.bf.update();
                } else if(spd < 0) {
                    if((int)(upd/Math.pow(2,-spd)) == 1) {
                        painter.bf.update();
                        upd = 0;
                    } else {
                        upd++;
                    }
                } else
                    painter.bf.update();
            }
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        removeCallbacks(updater);
    }

    @Override
    public int getSpeed() {
        return spd;
    }

    @Override
    public void callBack(Object o) {

    }

    private class Updater implements Runnable {
        @Override
        public void run() {
            if(!paused)
                invalidate();

            postDelayed(this,1000L/30L);
        }
    }

    @Override
    public BBPainter getPainter() {
        return painter;
    }

    @Override
    public void paint() {

    }

    @Override
    public void reset() {

    }
}
