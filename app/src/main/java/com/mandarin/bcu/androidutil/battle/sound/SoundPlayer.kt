package com.mandarin.bcu.androidutil.battle.sound

import android.media.MediaPlayer
import android.util.Log
import com.mandarin.bcu.androidutil.adapters.MediaPrepare
import java.io.IOException

/**
 * This class extends MediaPlayer.
 * It has more states than normal MediaPlayer, so users can easily get state of specific SoundPlayer.
 * It can handle itself by checking if called method will cause IllegalStateException.
 */
class SoundPlayer : MediaPlayer() {
    /**
     * State which tells if SoundPlayer is initialized.
     */
    var isInitialized = false
        private set
    /**
     * State which tells if SoundPlayer is running (playing).
     */
    var isRunning = false
    /**
     * State which tells if SoundPlayer is prepared.
     */
    private var isPrepared = false

    /**
     * State which tells if SoundPlayer is released
     */
    var isReleased = false
        private set

    /**
     * Improved MediaPlayer by releasing itself when it has to be finalized.
     */
    override fun finalize() {
        super.finalize()
        isReleased = true
        release()
    }

    override fun isPlaying(): Boolean {
        if(isReleased) return false

        return super.isPlaying()
    }

    override fun release() {
        isReleased = true
        super.release()
    }

    @Throws(IOException::class)
    override fun setDataSource(path: String) {
        super.setDataSource(path)
        isInitialized = true
    }

    override fun reset() {
        super.reset()
        isInitialized = false
        isRunning = false
        isPrepared = false
    }

    /**
     * Improved start() method by checking SoundPlayer is prepared.
     */
    override fun start() {
        if (!isInitialized) {
            isRunning = false
            if (isPlaying) {
                stop()
            }
            isPrepared = false
            reset()
            return
        } else if (isRunning || isPlaying) return

        if (!isPrepared) {
            try {
                prepareAsync()
                setOnPreparedListener(object : MediaPrepare() {
                    override fun prepare(mp: MediaPlayer?) {
                        mp!!.start()
                    }
                })
            } catch (ignored: IllegalStateException) {
                Log.e("SoundPlayerIllegal", "Something went wrong while calling SoundPlayer line 47")
            }
        } else {
            super.start()
            isRunning = true
        }
    }

    fun start(se: Boolean) {
        if (se) {
            setVolume(SoundHandler.se_vol, SoundHandler.se_vol)
            start()
        } else {
            start()
        }
    }

    override fun stop() {
        super.start()
        isRunning = false
    }

    override fun pause() {
        super.pause()
        isRunning = false
    }

    fun setPrepared(prepare: Boolean) {
        isPrepared = prepare
    }

}