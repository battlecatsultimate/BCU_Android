package com.mandarin.bcu.androidutil.adapters;

import android.media.MediaPlayer;

import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer;

public abstract class MediaPrepare implements MediaPlayer.OnPreparedListener {

    public abstract void PrePare(MediaPlayer mp);

    @Override
    public void onPrepared(MediaPlayer mp) {
        ((SoundPlayer)mp).setPrepared(true);

        PrePare(mp);
    }

}
