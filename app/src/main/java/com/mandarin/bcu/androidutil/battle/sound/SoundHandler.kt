package com.mandarin.bcu.androidutil.battle.sound

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.MediaPrepare
import common.pack.UserProfile
import java.io.File

object SoundHandler {
    const val SE_SE = 0
    const val SE_ATK = 1
    const val SE_BASE = 2
    const val SE_UI = 3

    var MUSIC = SoundPlayer()

    /** SoundPool for all other sound effects **/
    var SE: SoundPool? = SoundPool.Builder().setMaxStreams(50).build()
    /** SoundPool for all attack sounds **/
    var ATK : SoundPool? = SoundPool.Builder().build()
    /** SoundPool for base attack sounds **/
    var BASE : SoundPool? = SoundPool.Builder().build()

    var SPAWN_FAIL : SoundPool? = SoundPool.Builder().build()
    var TOUCH : SoundPool? = SoundPool.Builder().build()

    var play: BooleanArray = BooleanArray(0)

    var inBattle = false

    var battleEnd = false

    var twoMusic = false

    var haveToChange = false

    var Changed = false

    var musicPlay = true

    var mu1: File? = null

    var lop: Long = 0L

    var lop1: Long = 0L

    var sePlay = true

    var se_vol = 1f

    var mu_vol = 1f

    var uiPlay = true

    var ui_vol = 1f

    var speed = 0

    var map = HashMap<Int, Int>()

    private val atk = listOf(20, 21)
    private val ui = listOf(10, 15, 19, 27, 28)

    var timer: PauseCountDown? = null

    @JvmStatic
    fun setSE(ind: Int) {
        if (speed > 3)
            return

        if (play[ind])
            return

        if (battleEnd)
            return

        if(ind == 45 && twoMusic && haveToChange) {
            if(!Changed) {
                if(MUSIC.isRunning)
                    MUSIC.pause()

                MUSIC.reset()
                MUSIC.isLooping = false

                if(timer != null && timer?.isRunning == true) {
                    timer?.cancel()
                }

                val g = StaticStore.getMusicDataSource(UserProfile.getBCData().musics[ind]) ?: return

                try {
                    MUSIC.setDataSource(g.absolutePath)
                    MUSIC.prepareAsync()
                    MUSIC.setOnPreparedListener(object : MediaPrepare() {
                        override fun prepare(mp: MediaPlayer?) {
                            MUSIC.start(true)
                        }
                    })

                    MUSIC.setOnCompletionListener {
                        MUSIC.reset()

                        val h = mu1

                        h ?: return@setOnCompletionListener

                        try {
                            MUSIC.setVolume(mu_vol, mu_vol)
                            MUSIC.setDataSource(h.absolutePath)
                            MUSIC.prepareAsync()
                            MUSIC.setOnPreparedListener(object : MediaPrepare() {
                                override fun prepare(mp: MediaPlayer?) {
                                    if(lop1 > 0 && lop1 < MUSIC.duration) {
                                        if(timer != null && timer?.isRunning == true) {
                                            timer?.cancel()
                                        }

                                        timer = object : PauseCountDown((MUSIC.duration-1).toLong(), (MUSIC.duration-1).toLong(), true) {
                                            override fun onFinish() {
                                                MUSIC.seekTo(lop1.toInt(), true)

                                                timer = object : PauseCountDown((MUSIC.duration-1).toLong()-lop1, (MUSIC.duration-1).toLong()-lop1, true) {
                                                    override fun onFinish() {
                                                        MUSIC.seekTo(lop1.toInt(), true)

                                                        create()
                                                    }

                                                    override fun onTick(millisUntilFinished: Long) {
                                                        MUSIC.seekTo(lop1.toInt(), true)
                                                    }

                                                }

                                                timer?.create()
                                            }

                                            override fun onTick(millisUntilFinished: Long) {
                                                MUSIC.seekTo(lop1.toInt(), true)
                                            }

                                        }

                                        timer?.create()
                                    } else {
                                        timer = null
                                        MUSIC.isLooping = true
                                    }

                                    if(musicPlay) {
                                        MUSIC.start(false)
                                    }

                                    Changed = true
                                    haveToChange = false
                                }
                            })
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return
            }
        }

        check()

        val id = map[ind]

        if(id == null) {
            val result = when {
                ui.contains(ind) -> {
                    load(SE_UI, ind, play = true)
                }
                atk.contains(ind) -> {
                    load(SE_ATK, ind, play = true)
                }
                ind == 22 -> {
                    load(SE_BASE, ind, play = true)
                }
                else -> {
                    load(SE_SE, ind, play = true)
                }
            }

            if(result == -1)
                return

            map[ind] = result
        } else {
            when {
                atk.contains(ind) -> {
                    ATK?.play(id, se_vol, se_vol, 0, 0, 1f)
                }
                ind == 22 -> {
                    BASE?.play(id, se_vol, se_vol, 0, 0, 1f)
                }
                ui.contains(ind) -> {
                    when (ind) {
                        10 -> {
                            TOUCH?.play(id, ui_vol, ui_vol, 0, 0, 1f)
                        }
                        15 -> {
                            SPAWN_FAIL?.play(id, ui_vol, ui_vol, 0, 0, 1f)
                        }
                        else -> {
                            SE?.play(id, ui_vol, ui_vol, 0, 0, 1f)
                        }
                    }
                }
                else -> {
                    SE?.play(id, se_vol, se_vol, 0, 0, 1f)
                }
            }
        }

        play[ind] = true
    }

    @JvmStatic
    @Synchronized
    fun releaseAll() {
        map.clear()
        SE?.release()
        SE = null
        ATK?.release()
        ATK = null
        TOUCH?.release()
        TOUCH = null
        SPAWN_FAIL?.release()
        SPAWN_FAIL = null
    }

    @JvmStatic
    fun resetHandler() {
        releaseAll()
        for (i in play.indices) play[i] = false
        inBattle = false
        battleEnd = false
        twoMusic = false
        haveToChange = false
        Changed = false
        mu1 = null
        lop = 0
        lop1 = 0
        speed = 0
    }

    private fun check() {
        if(SE == null) {
            val aa = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build()

            SE = SoundPool.Builder().setMaxStreams(50).setAudioAttributes(aa).build()
        }

        if(ATK == null) {
            val aa = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build()

            ATK = SoundPool.Builder().setAudioAttributes(aa).build()
        }

        if(BASE == null) {
            val aa = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build()

            BASE = SoundPool.Builder().setAudioAttributes(aa).build()
        }

        if(TOUCH == null) {
            val aa = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build()

            TOUCH = SoundPool.Builder().setAudioAttributes(aa).build()
        }

        if(SPAWN_FAIL == null) {
            val aa = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build()

            SPAWN_FAIL = SoundPool.Builder().setAudioAttributes(aa).build()
        }
    }

    fun load(type: Int, ind: Int, play: Boolean) : Int {
        val f = StaticStore.getMusicDataSource(UserProfile.getBCData().musics[ind]) ?: return -1

        check()

        return when(type) {
            SE_SE -> {
                if(!sePlay)
                    return -1

                SE?.setOnLoadCompleteListener {
                    s, i, _ ->
                    val id = map[ind] ?: return@setOnLoadCompleteListener

                    if(play && id == i) {
                        s.play(i, se_vol, se_vol, 0, 0, 1f)
                    }
                }

                SE?.load(f.absolutePath, 0) ?: return -1
            }

            SE_ATK -> {
                if(!sePlay)
                    return -1

                ATK?.setOnLoadCompleteListener {
                    s, i, _ ->
                    val id = map[ind] ?: return@setOnLoadCompleteListener

                    if(play && id == i) {
                        s.play(i, se_vol, se_vol, 0, 0, 1f)
                    }
                }

                ATK?.load(f.absolutePath, 0) ?: return -1
            }

            SE_BASE -> {
                if(!sePlay)
                    return -1

                BASE?.setOnLoadCompleteListener {
                    s, i, _ ->
                    val id = map[ind] ?: return@setOnLoadCompleteListener

                    if(play && id == i) {
                        s.play(i, se_vol, se_vol, 0, 0, 1f)
                    }
                }

                BASE?.load(f.absolutePath, 0) ?: return -1
            }

            SE_UI -> {
                if(!uiPlay)
                    return -1

                when(ind) {
                    10 -> {
                        TOUCH?.setOnLoadCompleteListener { s, i, _ ->
                            val id = map[ind] ?: return@setOnLoadCompleteListener

                            if(play && id == i) {
                                s.play(i, ui_vol, ui_vol, 0, 0, 1f)
                            }
                        }

                        TOUCH?.load(f.absolutePath, 0) ?: return -1
                    }

                    15 -> {
                        SPAWN_FAIL?.setOnLoadCompleteListener { s, i, _ ->
                            val id = map[ind] ?: return@setOnLoadCompleteListener

                            if(play && id == i) {
                                s.play(i, ui_vol, ui_vol, 0, 0, 1f)
                            }
                        }

                        setSE(10)

                        SPAWN_FAIL?.load(f.absolutePath, 0) ?: return -1
                    }

                    else -> {
                        SE?.setOnLoadCompleteListener {
                            s, i, _ ->
                            val id = map[ind] ?: return@setOnLoadCompleteListener

                            if(play && id == i) {
                                s.play(i, se_vol, se_vol, 0, 0, 1f)
                            }
                        }

                        if(ind == 19)
                            setSE(10)

                        SE?.load(f.absolutePath, 0) ?: return -1
                    }
                }
            }

            else -> {
                return -1
            }
        }
    }
}