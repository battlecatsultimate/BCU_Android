package com.mandarin.bcu.androidutil.supports

import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer

abstract class MediaPrepare : OnPreparedListener {
    abstract fun prepare(mp: MediaPlayer?)
    override fun onPrepared(mp: MediaPlayer) {
        (mp as SoundPlayer).isPrepared = true
        prepare(mp)
    }
}