package com.mandarin.bcu.androidutil.battle.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.MediaPrepare
import common.CommonStatic
import common.util.pack.Pack
import java.io.File

object SoundHandler {
    const val SE_SE = 0
    const val SE_ATK = 1
    const val SE_BASE = 2

    @JvmField
    var MUSIC = SoundPlayer()

    /** SoundPool for all other sound effects **/
    var SE: SoundPool? = SoundPool.Builder().setMaxStreams(50).build()
    /** SoundPool for all attack sounds **/
    var ATK : SoundPool? = SoundPool.Builder().build()
    /** SoundPool for base attack sounds **/
    var BASE : SoundPool? = SoundPool.Builder().build()

    @JvmField
    var play: BooleanArray = BooleanArray(1)

    @JvmField
    var inBattle = false

    @JvmField
    var battleEnd = false

    @JvmField
    var twoMusic = false

    @JvmField
    var haveToChange = false

    @JvmField
    var Changed = false

    @JvmField
    var musicPlay = true

    @JvmField
    var mu1 = 3

    var sePlay = true

    @JvmField
    var se_vol = 1f

    @JvmField
    var mu_vol = 1f

    @JvmField
    var speed = 0

    var map = HashMap<Int, Int>()

    private val atk = listOf(20, 21)

    @JvmStatic
    fun read(c: Context) {
        val path = StaticStore.getExternalPath(c)+"music/"
        val mf = File(path)
        if (!mf.exists())
            return

        val mflit = mf.listFiles() ?: return

        play = BooleanArray(mflit.size)

        for (f in mflit) {
            val name = f.name
            if (name.length != 7) continue
            if (!name.endsWith("ogg")) continue
            val id = CommonStatic.parseIntN(name.substring(0, 3))
            if (id < 0) continue
            Pack.def.ms[id] = f
        }
    }

    @JvmStatic
    fun setSE(ind: Int) {
        if (speed > 3) return
        if (play[ind]) return
        if (battleEnd) return

        if(ind == 45 && twoMusic && haveToChange) {
            if(!Changed) {
                if(MUSIC.isRunning)
                    MUSIC.pause()

                MUSIC.reset()
                MUSIC.isLooping = false

                val g = Pack.def.ms[ind] ?: return

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

                        val h = if(mu1 < 1000) {
                            Pack.def.ms[mu1]
                        } else {
                            val p = Pack.map[StaticStore.getPID(mu1)]

                            if(p == null) {
                                Pack.def.ms[3]
                            } else {
                                p.ms.list[StaticStore.getMusicIndex(mu1)]
                            }
                        }

                        h ?: return@setOnCompletionListener

                        MUSIC.isLooping = true

                        try {
                            MUSIC.setDataSource(h.absolutePath)
                            MUSIC.prepareAsync()
                            MUSIC.setOnPreparedListener(object : MediaPrepare() {
                                override fun prepare(mp: MediaPlayer?) {
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
        mu1 = 3
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
    }

    fun load(type: Int, ind: Int, play: Boolean) : Int {
        val f = Pack.def.ms[ind] ?: return -1

        check()

        return when(type) {
            SE_SE -> {
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
                BASE?.setOnLoadCompleteListener {
                    s, i, _ ->
                    val id = map[ind] ?: return@setOnLoadCompleteListener

                    if(play && id == i) {
                        s.play(i, se_vol, se_vol, 0, 0, 1f)
                    }
                }

                BASE?.load(f.absolutePath, 0) ?: return -1
            }

            else -> {
                return -1
            }
        }
    }
}