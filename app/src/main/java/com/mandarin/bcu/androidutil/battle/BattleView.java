package com.mandarin.bcu.androidutil.battle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.view.View;

import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.MediaPrepare;
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler;
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer;
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics;
import com.mandarin.bcu.util.page.BBCtrl;
import com.mandarin.bcu.util.page.BattleBox;

import java.io.File;
import java.io.IOException;

import common.battle.BattleField;
import common.battle.SBCtrl;
import common.util.ImgCore;
import common.util.anim.ImgCut;
import common.util.pack.Pack;
import common.util.unit.Enemy;
import common.util.unit.Form;

@SuppressLint("ViewConstructor")
public class BattleView extends View implements BattleBox, BattleBox.OuterBox {
    public BBPainter painter;

    public boolean initialized = false;
    public boolean paused = false;
    public boolean battleEnd = false;
    public boolean musicChanged = false;

    public int spd = 0;
    public int upd = 0;

    private CVGraphics cv;
    private Updater updater;

    public BattleView(Context context, BattleField field, int type, boolean axis) {
        super(context);

        this.painter = type == 0 ? new BBPainter(this, field, this) : new BBCtrl(this, (SBCtrl) field, this,StaticStore.dptopx(32f,context));

        this.painter.dpi = StaticStore.dptopx(32f,context);

        ImgCore.ref = axis;

        updater = new Updater();

        for(int i = 0; i < SoundHandler.SE.size(); i++) {
            SoundHandler.SE.get(i).clear();
        }

        for (Form[] fs : painter.bf.sb.b.lu.fs) {
            for (Form f : fs) {
                if (f != null) {
                    if (f.anim.uni.getImg().getHeight() == f.anim.uni.getImg().getWidth()) {
                        ImgCut cut = ImgCut.newIns("./org/data/uni.imgcut");

                        f.anim.uni.setCut(cut);
                        f.anim.uni.setImg(f.anim.uni.getImg());

                        f.anim.check();
                    }
                }
            }
        }

        SoundHandler.MUSIC = new SoundPlayer();

        SharedPreferences preferences = context.getSharedPreferences(StaticStore.CONFIG,Context.MODE_PRIVATE);

        float musvol = (float)(1-(Math.log(100-preferences.getInt("mus_vol",99))/Math.log(100)));

        SoundHandler.MUSIC.setVolume(musvol,musvol);

        File f = Pack.def.ms.get(painter.bf.sb.st.mus0);

        SoundHandler.twoMusic = painter.bf.sb.st.mush != 0 && painter.bf.sb.st.mush != 100 && painter.bf.sb.st.mus0 != painter.bf.sb.st.mus1;

        if(SoundHandler.twoMusic) SoundHandler.mu1 = painter.bf.sb.st.mus1;

        if(f != null) {
            if(f.exists()) {
                try{
                    SoundHandler.MUSIC.setLooping(true);
                    SoundHandler.MUSIC.setDataSource(f.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for (Enemy e : painter.bf.sb.st.data.getAllEnemy())
            e.anim.check();
        updater.run();

        Paint cp = new Paint();
        Paint bp = new Paint();
        Paint gp = new Paint();
        cv = new CVGraphics(null, cp, bp, gp, true);
    }

    @Override
    public void onDraw(Canvas c) {
        if (initialized) {
            cv.setCanvas(c);

            painter.draw(cv);

            if (!paused) {
                if (spd > 0) {
                    for (int i = 0; i < Math.pow(2, spd); i++)
                        painter.bf.update();

                    ResetSE();
                } else if (spd < 0) {
                    if (upd / (1 - spd) >= 1) {
                        painter.bf.update();
                        ResetSE();
                        upd = 0;
                    } else {
                        upd++;
                    }
                } else {
                    painter.bf.update();
                    ResetSE();
                }


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
            if (!paused)
                invalidate();

            if(!musicChanged) {
                if (haveToChangeMusic()) {
                    SoundHandler.haveToChange = true;

                    SoundHandler.MUSIC.stop();
                    SoundHandler.MUSIC.reset();
                    SoundHandler.MUSIC.setLooping(true);

                    File f = Pack.def.ms.get(painter.bf.sb.st.mus1);

                    if (f != null) {
                        try {
                            SoundHandler.MUSIC.setDataSource(f.getAbsolutePath());
                            SoundHandler.MUSIC.prepareAsync();
                            SoundHandler.MUSIC.setOnPreparedListener(new MediaPrepare() {
                                @Override
                                public void PrePare(MediaPlayer mp) {
                                    SoundHandler.MUSIC.start();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    musicChanged = true;
                }
            }

            if(!battleEnd) {
                CheckWin();
                CheckLose();
            }

            postDelayed(this, 1000L / 30L);
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

    private boolean haveToChangeMusic() {
        if(painter.bf.sb.st.mush == 0 || painter.bf.sb.st.mush == 100)
            musicChanged = true;

        return (int)((float)(painter.bf.sb.ebase.health)/(float)(painter.bf.sb.ebase.maxH)*100) < painter.bf.sb.st.mush && painter.bf.sb.st.mush != 0 && painter.bf.sb.st.mush != 100;
    }

    private void ResetSE() {
        for(int i = 0; i < SoundHandler.play.length;i++)
            SoundHandler.play[i] = false;
    }

    private void CheckWin() {
        if(painter.bf.sb.ebase.health <= 0 && SoundHandler.MUSIC != null) {
            SoundHandler.MUSIC.stop();
            SoundHandler.MUSIC.reset();
            SoundHandler.MUSIC.setLooping(false);

            File f = Pack.def.ms.get(8);

            if(f != null) {
                if(f.exists()) {
                    try {
                        SoundHandler.MUSIC.setDataSource(f.getAbsolutePath());
                        SoundHandler.MUSIC.prepareAsync();
                        SoundHandler.MUSIC.setOnPreparedListener(new MediaPrepare() {
                            @Override
                            public void PrePare(MediaPlayer mp) {
                                SoundHandler.MUSIC.start();
                            }
                        });

                        SoundHandler.MUSIC.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                SoundHandler.MUSIC.release();
                                SoundHandler.MUSIC = null;
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            battleEnd = true;
            SoundHandler.battleEnd = true;
        }
    }

    private void CheckLose() {
        if(painter.bf.sb.ubase.health <= 0 && SoundHandler.MUSIC != null) {
            SoundHandler.MUSIC.stop();
            SoundHandler.MUSIC.reset();
            SoundHandler.MUSIC.setLooping(false);

            File f = Pack.def.ms.get(9);

            if(f != null) {
                if(f.exists()) {
                    try {
                        SoundHandler.MUSIC.setDataSource(f.getAbsolutePath());
                        SoundHandler.MUSIC.prepareAsync();
                        SoundHandler.MUSIC.setOnPreparedListener(new MediaPrepare() {
                            @Override
                            public void PrePare(MediaPlayer mp) {
                                SoundHandler.MUSIC.start();
                            }
                        });

                        SoundHandler.MUSIC.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                SoundHandler.MUSIC.release();
                                SoundHandler.MUSIC = null;
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            battleEnd = true;
            SoundHandler.battleEnd = true;
        }
    }
}
