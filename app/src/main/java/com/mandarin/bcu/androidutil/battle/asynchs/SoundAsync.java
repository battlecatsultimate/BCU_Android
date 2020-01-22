package com.mandarin.bcu.androidutil.battle.asynchs;

import android.media.MediaPlayer;
import android.os.AsyncTask;

import com.mandarin.bcu.androidutil.adapters.MediaPrepare;
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler;
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer;

import java.io.File;
import java.io.IOException;

import common.util.pack.Pack;

import static com.mandarin.bcu.androidutil.battle.sound.SoundHandler.ReleaseAll;
import static com.mandarin.bcu.androidutil.battle.sound.SoundHandler.available;
import static com.mandarin.bcu.androidutil.battle.sound.SoundHandler.inBattle;
import static com.mandarin.bcu.androidutil.battle.sound.SoundHandler.returnBack;

public class SoundAsync extends AsyncTask<Void,Void,Void> {
    private final int ind;

    public SoundAsync(int ind) {
        this.ind = ind;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if(!inBattle) {
            ReleaseAll();
            return null;
        }

        if(available == 0) return null;

        SoundPlayer mp = SoundHandler.getMP(ind);

        try {
            if(!mp.isInitialized()) {

                if(ind == 45 && SoundHandler.twoMusic && SoundHandler.haveToChange) {
                    if(!SoundHandler.Changed) {
                        SoundHandler.returnBack(mp,ind);

                        if(SoundHandler.MUSIC.isRunning())
                            SoundHandler.MUSIC.pause();

                        SoundHandler.MUSIC.reset();
                        SoundHandler.MUSIC.setLooping(false);

                        File g = Pack.def.ms.get(ind);

                        if (g != null) {
                            try {
                                SoundHandler.MUSIC.setDataSource(g.getAbsolutePath());
                                SoundHandler.MUSIC.setVolume(SoundHandler.se_vol,SoundHandler.se_vol);
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
                                        SoundHandler.MUSIC.reset();

                                        File h = Pack.def.ms.get(SoundHandler.mu1);

                                        if(h != null) {
                                            if (h.exists()) {
                                                SoundHandler.MUSIC.setLooping(true);
                                                try {
                                                    SoundHandler.MUSIC.setDataSource(h.getAbsolutePath());
                                                    SoundHandler.MUSIC.setVolume(SoundHandler.mu_vol,SoundHandler.mu_vol);
                                                    SoundHandler.MUSIC.prepareAsync();
                                                    SoundHandler.MUSIC.setOnPreparedListener(new MediaPrepare() {
                                                        @Override
                                                        public void PrePare(MediaPlayer mp) {
                                                            if(SoundHandler.musicPlay) {
                                                                SoundHandler.MUSIC.start();
                                                            }
                                                            SoundHandler.Changed = true;
                                                            SoundHandler.haveToChange = false;
                                                        }
                                                    });
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }

                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        return null;
                    }
                }

                if(SoundHandler.se_vol == 0) return null;

                File f = Pack.def.ms.get(ind);

                if (f == null) return null;

                if (!f.exists()) return null;

                String path = f.getAbsolutePath();

                mp.setDataSource(path);

                mp.setLooping(false);

                mp.prepareAsync();

                mp.setOnPreparedListener(new MediaPrepare() {
                    @Override
                    public void PrePare(MediaPlayer mpp) {
                        mp.start(true);
                    }
                });

                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mmp) {

                        mp.setRunning(false);
                        returnBack(mp, ind);
                    }
                });
            } else {
                if(!mp.isRunning() && mp.isInitialized()) {
                    mp.start(true);
                } else {
                    mp.reset();
                    returnBack(mp, ind);
                }
            }
        } catch (IOException | IllegalStateException e) {
            mp.release();
        }

        return null;
    }
}
