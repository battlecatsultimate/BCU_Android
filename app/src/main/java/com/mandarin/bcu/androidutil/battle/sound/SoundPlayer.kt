package com.mandarin.bcu.androidutil.battle.sound

import android.media.MediaPlayer
import android.util.Log
import com.mandarin.bcu.androidutil.supports.MediaPrepare

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
    var isPrepared = false

    /**
     * State which tells if SoundPlayer is released
     */
    var isReleased = false
        private set

    var isPreparing = false

    /**
     * Improved MediaPlayer by releasing itself when it has to be finalized.
     */
    override fun finalize() {
        isReleased = true
        release()

        super.finalize()
    }

    override fun isPlaying(): Boolean {
        if(!isSafe()) return false

        if(isReleased) return false

        return super.isPlaying()
    }

    override fun release() {
        isReleased = true

        if (isPreparing)
            return

        super.release()
    }

    @Throws(Exception::class)
    override fun setDataSource(path: String) {
        if(isReleased) {
            Log.e("SoundPlayerIllegal", "This SoundPlayer is already released")
            return
        }

        if(isInitialized || isPrepared) {
            Log.e("SoundPlayerIllegal","This SoundPlayer is already initialized, need to be reset")
            return
        }

        super.setDataSource(path)
        isPrepared = false
        isInitialized = true
    }

    override fun reset() {
        if (!isSafe())
            return

        super.reset()
        isInitialized = false
        isRunning = false
        isPrepared = false
    }

    fun seekTo(millisec: Int, start: Boolean) {
        seekTo(millisec)

        if(!isPlaying && start) {
            start()
        }
    }

    /**
     * Improved start() method by checking SoundPlayer is prepared.
     */
    override fun start() {
        if(!isSafe()) {
            Log.e("SoundPlayerIllegal", "Music isn't initialized")
            return
        }

        if (!isInitialized) {
            Log.w("SoundPlayerIllegal", "Music isn't initialized")
            isRunning = false
            if (isPlaying) {
                stop()
            }
            isPrepared = false
            reset()
            return
        } else if (isPlaying) {
            return
        }

        if (!isPrepared) {
            try {
                Log.w("SoundPlayerIllegal", "Music isn't prepared, try to manually override")
                prepareAsync()
                setOnPreparedListener(object : MediaPrepare() {
                    override fun prepare(mp: MediaPlayer?) {
                        mp!!.start()
                    }
                })
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                Log.e("SoundPlayerIllegal", "Something went wrong while calling SoundPlayer line 111")
            }
        } else {
            super.start()
            isRunning = true
        }
    }

    fun start(se: Boolean) {
        if(!isSafe())
            return

        if (se) {
            setVolume(SoundHandler.se_vol, SoundHandler.se_vol)
            start()
        } else {
            setVolume(SoundHandler.mu_vol, SoundHandler.mu_vol)
            start()
        }
    }

    override fun stop() {
        if(!isSafe()) return

        super.start()
        isRunning = false
    }

    override fun pause() {
        if(!isSafe()) return



        super.pause()
        isRunning = false
    }

    private fun isSafe() : Boolean {
        if(isReleased) {
            Log.e("SoundPlayerIllegal","This SoundPlayer is already released")
            return false
        }

        if(!isInitialized) {
            Log.e("SoundPlayerIllegal","This SoundPlayer isn't initialized yet")
            return false
        }

        return true
    }

    override fun prepareAsync() {
        if(!isSafe()) {
            return
        }

        isPreparing = true

        super.prepareAsync()
    }
}