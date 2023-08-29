package com.mandarin.bcu.androidutil.battle.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mandarin.bcu.androidutil.StaticStore
import common.pack.Identifier
import common.pack.UserProfile
import common.util.stage.Music
import java.io.File

object SoundHandler {
    const val SE_SE = 0
    const val SE_ATK = 1
    const val SE_BASE = 2
    const val SE_UI = 3

    lateinit var MUSIC: Player
        private set
    private val listeners = ArrayList<Player.Listener>()

    /** SoundPool for all other sound effects **/
    var SE: SoundPool? = SoundPool.Builder().setMaxStreams(50).build()
    /** SoundPool for all attack sounds **/
    var ATK : SoundPool? = SoundPool.Builder().build()
    /** SoundPool for base attack sounds **/
    var BASE : SoundPool? = SoundPool.Builder().build()

    var SPAWN_FAIL : SoundPool? = SoundPool.Builder().build()
    var TOUCH : SoundPool? = SoundPool.Builder().build()

    var play: BooleanArray = BooleanArray(0)
    var playCustom = HashMap<Identifier<Music>, Boolean>()

    var inBattle = false

    var battleEnd = false

    var twoMusic = false

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
    private var customMap = HashMap<Identifier<Music>, Int>()

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
    fun setSE(mus: Identifier<Music>) {
        if (speed > 3)
            return

        if (playCustom.containsKey(mus) && playCustom[mus] == true)
            return

        if (battleEnd)
            return

        check()

        val id = customMap[mus]

        if(id == null) {
            val result = load(mus, play = true)

            if(result == -1)
                return

            customMap[mus] = result
        } else {
            SE?.play(id, se_vol, se_vol, 0, 0, 1f)
        }

        playCustom[mus] = true
    }

    fun initializePlayer(context: Context, directPlay: Boolean = true, repeat: Boolean = true) {
        if (this::MUSIC.isInitialized && MUSIC.isCommandAvailable(Player.COMMAND_RELEASE)) {
            MUSIC.release()
        }

        listeners.clear()

        val player = ExoPlayer.Builder(context).build()

        player.playWhenReady = directPlay
        player.repeatMode = if (repeat) {
            Player.REPEAT_MODE_ONE
        } else {
            Player.REPEAT_MODE_OFF
        }

        MUSIC = player
    }

    val isMusicPossible: Boolean
        get() {
            return this::MUSIC.isInitialized
        }

    fun setBGM(music: Identifier<Music>, onReady: () -> Unit = { }, onComplete: () -> Unit = { }) {
        if (!this::MUSIC.isInitialized)
            return

        val m = StaticStore.getMusicDataSource(Identifier.get(music)) ?: return
        val loop = music.get()?.loop ?: 0

        if (MUSIC.isLoading) {
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)

                    if (playbackState == Player.STATE_READY) {
                        listeners.remove(this)
                        MUSIC.removeListener(this)

                        loadMusic(m, loop, onReady, onComplete)
                    }
                }
            }

            MUSIC.addListener(listener)
            listeners.add(listener)
        } else {
            loadMusic(m, loop, onReady, onComplete)
        }
    }

    @JvmStatic
    @Synchronized
    fun releaseAll() {
        map.clear()
        customMap.clear()
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
        playCustom.clear()
        inBattle = false
        battleEnd = false
        twoMusic = false
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

    fun loadMusic(m: File, loop: Long, onReady: () -> Unit, onComplete: () -> Unit) {
        if (!MUSIC.isCommandAvailable(Player.COMMAND_SET_MEDIA_ITEM) || !MUSIC.isCommandAvailable(Player.COMMAND_PREPARE))
            return

        if (MUSIC.currentMediaItem != null) {
            if (MUSIC.isCommandAvailable(Player.COMMAND_CHANGE_MEDIA_ITEMS)) {
                MUSIC.clearMediaItems()
            }

            MUSIC.stop()
            MUSIC.seekTo(0)

            timer?.cancel()
            timer = null

            listeners.removeIf {
                MUSIC.removeListener(it)

                true
            }
        }

        val item = MediaItem.fromUri(m.toUri())

        MUSIC.setMediaItem(item)
        MUSIC.prepare()

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                if (playbackState == Player.STATE_READY) {
                    if(musicPlay) {
                        if(loop > 0 && loop < MUSIC.duration) {
                            timer = object : PauseCountDown((MUSIC.duration - 1), (MUSIC.duration - 1), true) {
                                override fun onFinish() {
                                    MUSIC.seekTo(loop)

                                    timer = object : PauseCountDown((MUSIC.duration - 1), (MUSIC.duration - 1), true) {
                                        override fun onFinish() {
                                            MUSIC.seekTo(loop)

                                            timer?.create()
                                        }

                                        override fun onTick(millisUntilFinished: Long) {}
                                    }
                                }

                                override fun onTick(millisUntilFinished: Long) {}
                            }

                            timer?.create()
                        } else {
                            timer = null
                        }

                        onReady()
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    onComplete()
                }
            }
        }

        MUSIC.addListener(listener)
        listeners.add(listener)
    }

    fun load(mus: Identifier<Music>, play: Boolean) : Int {
        mus.get() ?: return -1

        val f = StaticStore.getMusicDataSource(mus.get()) ?: return -1

        check()

        if(!sePlay)
            return -1

        SE?.setOnLoadCompleteListener {
                s, i, _ ->
            val id = customMap[mus] ?: return@setOnLoadCompleteListener

            if(play && id == i) {
                s.play(i, se_vol, se_vol, 0, 0, 1f)
            }
        }

        return SE?.load(f.absolutePath, 0) ?: -1
    }
}