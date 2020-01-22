package com.mandarin.bcu.androidutil.battle.sound;

import android.media.MediaPlayer;
import android.util.Log;

import com.mandarin.bcu.androidutil.adapters.MediaPrepare;

import java.io.IOException;

/**
 * This class extends MediaPlayer.
 * It has more states than normal MediaPlayer, so users can easily get state of specific SoundPlayer.
 * It can handle itself by checking if called method will cause IllegalStateException.
 */
public class SoundPlayer extends MediaPlayer {
    /**
     * State which tells if SoundPlayer is initialized.
     */
    private boolean isInitialized;
    /**
     * State which tells if SoundPlayer is running (playing).
     */
    private boolean isRunning;
    /**
     * State which tells if SoundPlayer is prepared.
     */
    private boolean isPrepared;

    /**
     * Improved MediaPlayer by releasing itself when it has to be finalized.
     */
    @Override
    public void finalize() {
        super.finalize();

        release();
    }

    @Override
    public void setDataSource(String path) throws IOException {
        super.setDataSource(path);

        isInitialized = true;
    }

    @Override
    public void reset() {
        super.reset();

        isInitialized = false;
        isRunning = false;
        isPrepared = false;
    }

    @Override
    public void prepareAsync() {
        super.prepareAsync();
    }

    /**
     * Improved start() method by checking SoundPlayer is prepared.
     */
    @Override
    public void start() {
        if(!isInitialized) {
            isRunning = false;
            if(isPlaying()) {
                stop();
            }
            isPrepared = false;

            reset();

            return;
        } else if(isRunning || isPlaying()) return;

        if (!isPrepared) {
            try {
                prepareAsync();
                setOnPreparedListener(new MediaPrepare() {
                    @Override
                    public void PrePare(MediaPlayer mp) {
                        mp.start();
                    }
                });
            } catch (IllegalStateException ignored) {
                Log.e("SoundPlayerIllegal", "Something went wrong while calling SoundPlayer line 47");
            }
        } else {

            super.start();

            isRunning = true;
        }
    }

    public void start(boolean se) {
        if (se) {
            setVolume(SoundHandler.se_vol, SoundHandler.se_vol);
            start();
        } else {
            start();
        }
    }

    @Override
    public void stop() {
        super.start();

        isRunning = false;
    }

    @Override
    public void pause() {
        super.pause();

        isRunning = false;
    }

    public void setRunning(boolean run) {
        this.isRunning = run;
    }

    public void setPrepared(boolean prepare) {
        this.isPrepared = prepare;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }
}
