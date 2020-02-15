package com.mandarin.bcu

import android.app.Activity
import android.content.*
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.AlphaAnimator
import com.mandarin.bcu.androidutil.adapters.AnimatorConst
import com.mandarin.bcu.androidutil.adapters.ScaleAnimator
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.music.adapters.MusicListAdapter
import common.CommonStatic
import common.util.pack.Pack
import java.io.File
import kotlin.math.round

class MusicPlayer : AppCompatActivity() {
    companion object {
        private var sound = SoundPlayer()
        private var posit = 0
        private var next = false
        private var looping = false
        private var completed = false
        private var paused = false
        private var prog = 0
        private var volume = 99
        private var opened = false
    }

    private fun reset() {
        posit = 0
        next = false
        looping = false
        completed = false
        paused = false
        opened = false
        prog = 0
        volume = 99
    }

    private var ch = -1
    private var ah = -1
    private var destroyed = false
    private var isControlling = false
    private var musicreceive = MusicReceiver(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: SharedPreferences.Editor

        if (!shared.contains("initial")) {
            ed = shared.edit()
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.apply()
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_night)
            } else {
                setTheme(R.style.AppTheme_day)
            }
        }

        when {
            shared.getInt("Orientation", 0) == 1 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            shared.getInt("Orientation", 0) == 2 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            shared.getInt("Orientation", 0) == 0 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        registerReceiver(musicreceive, IntentFilter(Intent.ACTION_HEADSET_PLUG))

        setContentView(R.layout.activity_music_player)

        try {
            val bundle = intent.extras

            if (bundle != null) {
                if (!opened) {
                    posit = bundle.getInt("Music")
                    opened = true
                }

                if (posit >= StaticStore.musicnames.size) return

                val name: TextView = findViewById(R.id.musicname)
                val current: TextView = findViewById(R.id.musiccurrent)
                val max: TextView = findViewById(R.id.musicmaxdu)
                val muprog: SeekBar = findViewById(R.id.musicprogress)
                val mulist: ListView = findViewById(R.id.musiclist)
                val mumenu: FloatingActionButton = findViewById(R.id.musicmenu)
                val close: FloatingActionButton = findViewById(R.id.musicclose)
                val album: ImageView = findViewById(R.id.musicalbum)
                val playlayout: ConstraintLayout = findViewById(R.id.musicplayerlayout)
                val loop: FloatingActionButton = findViewById(R.id.musicloop)
                val play: FloatingActionButton = findViewById(R.id.musicplay)
                val back: FloatingActionButton = findViewById(R.id.musicbck)
                val vol: SeekBar = findViewById(R.id.musicsound)
                val nextsong: FloatingActionButton = findViewById(R.id.musicshuff)
                val nex: FloatingActionButton = findViewById(R.id.musicnext)
                val prev: FloatingActionButton = findViewById(R.id.musicprev)

                back.setOnClickListener {
                    destroyed = true
                    if (!sound.isReleased && sound.isInitialized) {
                        if (sound.isRunning || sound.isPlaying) {
                            sound.pause()
                            sound.stop()
                        }

                        sound.reset()
                    }
                    sound.setOnCompletionListener {

                    }
                    reset()
                    finish()
                }

                close.hide()

                val f = Pack.def.ms.get(posit)

                if (!f.exists()) return

                if (!sound.isInitialized)
                    sound.setDataSource(f.absolutePath)

                if (!sound.isPrepared) {
                    sound.prepareAsync()
                    sound.setOnPreparedListener {
                        sound.isPrepared = true
                        sound.start()
                    }
                }

                vol.max = 99
                vol.progress = volume

                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    val display = DisplayMetrics()
                    windowManager.defaultDisplay.getMetrics(display)

                    ah = display.widthPixels / 2

                    val constlay = playlayout.layoutParams
                    val allay = album.layoutParams
                    val mullay = mulist.layoutParams

                    constlay.width = display.widthPixels / 2
                    allay.width = display.widthPixels / 2
                    mullay.width = display.widthPixels / 2

                    playlayout.layoutParams = constlay
                    album.layoutParams = allay
                    mulist.layoutParams = mullay
                }

                vol.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        volume = progress

                        val scale = StaticStore.getVolumScaler(progress)

                        sound.setVolume(scale, scale)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {

                    }

                })

                play.setOnClickListener {
                    if (sound.isPlaying || sound.isRunning) {
                        paused = true
                        sound.pause()
                        play.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_black_24dp))
                    } else {
                        paused = false
                        if (completed) {
                            sound.seekTo(0)
                            sound.start()
                            completed = false
                        } else {
                            sound.start()
                        }

                        play.setImageDrawable(getDrawable(R.drawable.ic_pause_black_24dp))
                    }
                }

                nex.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        if (sound.isPlaying || sound.isRunning) {
                            sound.pause()
                            sound.stop()
                        }

                        completed = false

                        sound.reset()

                        posit += 1

                        if (posit >= Pack.def.ms.size()) {
                            posit = 0
                        }

                        val g = Pack.def.ms[posit]

                        if (!g.exists()) {
                            StaticStore.showShortMessage(this@MusicPlayer, "No File Found")
                            nex.performClick()
                        }

                        sound.setDataSource(g.absolutePath)

                        sound.isLooping = looping

                        name.text = g.name
                        max.text = StaticStore.musicnames[posit]

                        val mmr = MediaMetadataRetriever()
                        mmr.setDataSource(g.absolutePath)

                        muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()

                        muprog.progress = sound.currentPosition
                        current.text = getTime(sound.currentPosition)

                        if (!paused)
                            sound.start()
                    }

                })

                prev.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        var ratio = 0

                        completed = false

                        if (!sound.isReleased && sound.isInitialized)
                            ratio = (sound.currentPosition.toFloat() / muprog.max.toFloat() * 100).toInt()

                        if (ratio > 5) {
                            sound.pause()
                            sound.seekTo(0)
                            sound.start()
                        } else {
                            posit -= 1

                            if (posit < 0)
                                posit = 0

                            val g = Pack.def.ms[posit]

                            if (!g.exists()) {
                                StaticStore.showShortMessage(this@MusicPlayer, "No File Found")
                                nex.performClick()
                            }

                            sound.reset()

                            sound.setDataSource(g.absolutePath)

                            sound.isLooping = looping

                            name.text = g.name
                            max.text = StaticStore.musicnames[posit]

                            val mmr = MediaMetadataRetriever()
                            mmr.setDataSource(g.absolutePath)

                            muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()

                            muprog.progress = sound.currentPosition
                            current.text = getTime(sound.currentPosition)

                            if (!paused)
                                sound.start()
                        }
                    }

                })

                mulist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    posit = position

                    completed = false
                    paused = false

                    if (sound.isPlaying || sound.isRunning) {
                        sound.pause()
                        sound.stop()
                    }

                    sound.reset()

                    val g = Pack.def.ms[posit]

                    if (!g.exists()) {
                        StaticStore.showShortMessage(this@MusicPlayer, "No File Found")
                        nex.performClick()
                    }

                    sound.setDataSource(g.absolutePath)

                    sound.isLooping = looping

                    name.text = g.name
                    max.text = StaticStore.musicnames[posit]

                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(g.absolutePath)

                    muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()
                    play.setImageDrawable(getDrawable(R.drawable.ic_pause_black_24dp))

                    sound.start()
                }

                sound.setOnCompletionListener {
                    if (!sound.isLooping && !next) {
                        play.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_black_24dp))
                        completed = true
                        sound.isRunning = false
                        paused = true
                    }

                    if (next && !sound.isLooping) {
                        posit += 1

                        if (posit >= Pack.def.ms.size()) {
                            posit = 0
                        }

                        val g = Pack.def.ms.get(posit)

                        if (!g.exists()) {
                            play.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_black_24dp))
                            StaticStore.showShortMessage(this@MusicPlayer, "No File Found")
                            return@setOnCompletionListener
                        }

                        sound.reset()

                        sound.setDataSource(g.absolutePath)

                        muprog.max = StaticStore.durations[posit]
                        name.text = g.name
                        max.text = StaticStore.musicnames[posit]

                        sound.start()
                    }
                }

                muprog.max = StaticStore.durations[posit]
                muprog.progress = prog
                name.text = f.name
                max.text = StaticStore.musicnames[posit]

                val loc = ArrayList<Int>()
                val names = arrayOfNulls<String>(Pack.def.ms.size())

                for (i in Pack.def.ms.list.indices) {
                    loc.add(getFileLoc(Pack.def.ms.list[i]))
                    names[i] = Pack.def.ms.list[i].name
                }

                val adapter = MusicListAdapter(this, names, loc)
                mulist.adapter = adapter

                loop.setOnClickListener {
                    if (sound.isLooping) {
                        looping = false
                        loop.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(this@MusicPlayer, R.attr.HintPrimary))
                    } else {
                        loop.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(this@MusicPlayer, R.attr.colorAccent))
                        next = false
                        looping = true
                        nextsong.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(this@MusicPlayer, R.attr.HintPrimary))
                    }

                    sound.isLooping = !sound.isLooping
                }

                nextsong.setOnClickListener {
                    if (next) {
                        nextsong.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(this@MusicPlayer, R.attr.HintPrimary))
                    } else {
                        nextsong.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(this@MusicPlayer, R.attr.colorAccent))
                        sound.isLooping = false
                        looping = false
                        loop.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(this@MusicPlayer, R.attr.HintPrimary))
                    }

                    next = !next
                }

                muprog.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            current.text = getTime(progress)
                        }

                        completed = false
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        isControlling = true
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        if (!sound.isReleased && sound.isInitialized) {
                            sound.seekTo(seekBar?.progress ?: 0)
                        }

                        isControlling = false
                    }

                })

                mumenu.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        mumenu.hide()
                        close.show()

                        if (ch == -1) {
                            ch = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                val toolbar: Toolbar = findViewById(R.id.musictool)
                                val toolh = toolbar.height

                                val rect = Rect()

                                window.decorView.getWindowVisibleDisplayFrame(rect)

                                val sh = rect.top

                                val display = windowManager.defaultDisplay
                                val p = Point()

                                display.getSize(p)

                                val h = p.y

                                h - toolh - sh
                            } else {
                                playlayout.height
                            }
                        }

                        if (ah == -1) {
                            ah = album.height
                        }

                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            val aanim = ScaleAnimator(album, AnimatorConst.WIDTH, 200, AnimatorConst.DECELERATE, ah, 0)
                            aanim.start()

                            val aalanim = AlphaAnimator(album, 200, AnimatorConst.DECELERATE, 1f, 0f)
                            aalanim.start()

                            mulist.postDelayed({
                                val mulanim = ScaleAnimator(mulist, AnimatorConst.HEIGHT, 200, AnimatorConst.DECELERATE, 0, ch)
                                mulanim.start()

                                val mulalanim = AlphaAnimator(mulist, 200, AnimatorConst.DECELERATE, 0f, 1f)
                                mulalanim.start()
                            }, 200)
                        } else {
                            val canim = ScaleAnimator(playlayout, AnimatorConst.HEIGHT, 200, AnimatorConst.DECELERATE, ch, StaticStore.dptopx(254f, this@MusicPlayer))
                            canim.start()

                            val aanim = AlphaAnimator(album, 200, AnimatorConst.DECELERATE, 1f, 0f)
                            aanim.start()
                        }
                    }

                })

                close.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        mumenu.show()
                        close.hide()

                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            val mulanim = ScaleAnimator(mulist, AnimatorConst.HEIGHT, 200, AnimatorConst.DECELERATE, ch, 0)
                            mulanim.start()

                            val mulalanim = AlphaAnimator(mulist, 200, AnimatorConst.DECELERATE, 1f, 0f)
                            mulalanim.start()

                            mulist.postDelayed({
                                val aanim = ScaleAnimator(album, AnimatorConst.WIDTH, 200, AnimatorConst.DECELERATE, 0, ah)
                                aanim.start()

                                val aalanim = AlphaAnimator(album, 200, AnimatorConst.DECELERATE, 0f, 1f)
                                aalanim.start()
                            }, 200)
                        } else {
                            val canim = ScaleAnimator(playlayout, AnimatorConst.HEIGHT, 200, AnimatorConst.DECELERATE, StaticStore.dptopx(254f, this@MusicPlayer), ch)
                            canim.start()

                            val aanim = AlphaAnimator(album, 200, AnimatorConst.DECELERATE, 0f, 1f)
                            aanim.start()
                        }
                    }

                })

                val handler = Handler()

                val runnable = object : Runnable {
                    override fun run() {
                        if (sound.isInitialized && !sound.isReleased && !isControlling) {
                            if (sound.isPlaying || sound.isRunning) {
                                muprog.progress = sound.currentPosition
                                current.text = getTime(sound.currentPosition)
                            }
                        }

                        if (!destroyed)
                            handler.postDelayed(this, 10)
                    }
                }

                handler.postDelayed(runnable, 10)

                if (looping) {
                    loop.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(this@MusicPlayer, R.attr.colorAccent))
                }

                if (next) {
                    nextsong.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(this@MusicPlayer, R.attr.colorAccent))
                }

                if (paused || completed) {
                    play.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_black_24dp))
                }
            }
        } catch (e: IllegalStateException) {
            StaticStore.showShortMessage(this@MusicPlayer, R.string.music_err)
            ErrorLogWriter.writeLog(e, StaticStore.upload)
            finish()
        }
    }

    override fun onBackPressed() {
        val back: FloatingActionButton = findViewById(R.id.musicbck)
        back.performClick()
    }

    override fun onDestroy() {
        destroyed = true
        musicreceive.unregister()
        unregisterReceiver(musicreceive)
        super.onDestroy()
    }

    private fun getTime(time: Int) : String {
        val t: String

        var tim = time.toFloat()/1000f

        var min = (tim / 60f).toInt()

        tim -= min*60f

        var sec = round(tim).toInt()

        if(sec == 60) {
            sec = 0
            min += 1
        }

        t = if(sec < 10)
            "$min:0$sec"
        else
            "$min:$sec"

        return t
    }

    private fun getFileLoc(f: File): Int {
        var name = f.name

        if(!name.endsWith(".ogg")) return 0

        name = name.replace(".ogg","")

        return CommonStatic.parseIntN(name)
    }

    private class MusicReceiver(private val ac: Activity) : BroadcastReceiver() {
        private var unregister = false
        private var rotated = true

        override fun onReceive(context: Context?, intent: Intent?) {
            if(rotated) {
                rotated = false
                return
            }

            if(unregister) return

            if(intent?.action.equals(Intent.ACTION_HEADSET_PLUG)) {

                when(intent?.getIntExtra("state",-1) ?: -1) {
                    0 -> if(sound.isInitialized && !sound.isReleased) {
                        if(sound.isRunning || sound.isPlaying) {
                            val play: FloatingActionButton = ac.findViewById(R.id.musicplay)

                            play.performClick()
                        }
                    }
                }
            }
        }

        fun unregister() {
            unregister = true
        }

    }
}
