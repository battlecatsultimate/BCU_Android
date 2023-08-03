package com.mandarin.bcu

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.music.adapters.MusicListAdapter
import com.mandarin.bcu.androidutil.supports.AlphaAnimator
import com.mandarin.bcu.androidutil.supports.AnimatorConst
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.ScaleAnimator
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import common.util.stage.Music
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.Locale
import kotlin.math.round

@Suppress("DEPRECATION")
class MusicPlayer : AppCompatActivity() {
    var destroyed = false
    private var isControlling = false

    companion object {
        var sound = SoundPlayer()
        var music: Identifier<Music>? = null
        var next = false
        var looping = false
        var completed = false
        var paused = false
        var prog = 0
        var volume = 99
        var opened = false

        fun reset() {
            next = false
            looping = false
            completed = false
            paused = false
            opened = false
            prog = 0
            volume = 99
        }
    }

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

        LeakCanaryManager.initCanary(shared, application)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        registerReceiver(musicreceive, IntentFilter(Intent.ACTION_HEADSET_PLUG))

        setContentView(R.layout.activity_music_player)

        lifecycleScope.launch {
            try {
                val bundle = intent.extras ?: return@launch

                if (!opened) {
                    music = StaticStore.transformIdentifier(bundle.getString("Data"))

                    opened = true
                }

                //Prepare
                val name = findViewById<TextView>(R.id.musicname)
                val current = findViewById<TextView>(R.id.musiccurrent)
                val max = findViewById<TextView>(R.id.musicmaxdu)
                val muprog = findViewById<SeekBar>(R.id.musicprogress)
                val mulist = findViewById<ListView>(R.id.musiclist)
                val mumenu = findViewById<FloatingActionButton>(R.id.musicmenu)
                val close = findViewById<FloatingActionButton>(R.id.musicclose)
                val album = findViewById<ImageView>(R.id.musicalbum)
                val playlayout = findViewById<ConstraintLayout>(R.id.musicplayerlayout)
                val loop = findViewById<FloatingActionButton>(R.id.musicloop)
                val play = findViewById<FloatingActionButton>(R.id.musicplay)
                val back = findViewById<FloatingActionButton>(R.id.musicbck)
                val vol = findViewById<SeekBar>(R.id.musicsound)
                val nextsong = findViewById<FloatingActionButton>(R.id.musicshuff)
                val nex = findViewById<FloatingActionButton>(R.id.musicnext)
                val prev = findViewById<FloatingActionButton>(R.id.musicprev)
                val st = findViewById<TextView>(R.id.status)
                val progress = findViewById<ProgressBar>(R.id.prog)

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

                StaticStore.setDisappear(name, current, max, muprog, mulist, mumenu, close, album, playlayout, loop, play, vol, nextsong, nex, prev)

                //Load Data
                withContext(Dispatchers.IO) {
                    Definer.define(this@MusicPlayer, { _ -> }, { t -> runOnUiThread { st.text = t }})
                }

                st.setText(R.string.load_music_duratoin)

                withContext(Dispatchers.IO) {
                    if(StaticStore.musicnames.size != UserProfile.getAllPacks().size || StaticStore.musicData.isEmpty()) {
                        StaticStore.musicnames.clear()
                        StaticStore.musicData.clear()

                        for (p in UserProfile.getAllPacks()) {
                            val names = SparseArray<String>()

                            for (j in p.musics.list.indices) {
                                val f = StaticStore.getMusicDataSource(p.musics.list[j]) ?: continue

                                val sp = SoundPlayer()
                                sp.setDataSource(f.absolutePath)
                                sp.prepare()
                                StaticStore.durations.add(sp.duration)

                                var time = sp.duration.toFloat() / 1000f

                                sp.release()

                                var min = (time / 60f).toInt()

                                time -= min.toFloat() * 60f

                                var sec = round(time).toInt()

                                if (sec == 60) {
                                    min += 1
                                    sec = 0
                                }

                                val mins = min.toString()

                                val secs = if (sec < 10) "0$sec"
                                else sec.toString()

                                names.append(p.musics.list[j].id.id, "$mins:$secs")

                                StaticStore.musicData.add(p.musics.list[j].id)
                            }

                            if(p is PackData.DefPack) {
                                StaticStore.musicnames[Identifier.DEF] = names
                            } else if(p is PackData.UserPack) {
                                StaticStore.musicnames[p.desc.id] = names
                            }
                        }
                    }
                }

                //Load UI
                if(destroyed)
                    return@launch

                val m0 = music ?: return@launch

                StaticStore.musicnames[m0.pack] ?: return@launch

                progress.isIndeterminate = true

                close.hide()

                val f = StaticStore.getMusicDataSource(Identifier.get(music)) ?: return@launch

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

                var ch = -1
                var ah = -1

                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    val w = StaticStore.getScreenWidth(this@MusicPlayer, false)

                    ah = w / 2

                    val constlay = playlayout.layoutParams
                    val allay = album.layoutParams
                    val mullay = mulist.layoutParams

                    constlay.width = w / 2
                    allay.width = w / 2
                    mullay.width = w / 2

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
                        play.setImageDrawable(ContextCompat.getDrawable(this@MusicPlayer, R.drawable.ic_play_arrow_black_24dp))
                    } else {
                        paused = false
                        if (completed) {
                            sound.seekTo(0)
                            sound.start()
                            completed = false
                        } else {
                            sound.start()
                        }

                        play.setImageDrawable(ContextCompat.getDrawable(this@MusicPlayer, R.drawable.ic_pause_black_24dp))
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

                        var index = indexOf(music) + 1

                        if(index >= StaticStore.musicData.size) {
                            index = 0
                        }

                        music = StaticStore.musicData[index]

                        val m = music ?: return

                        val g = StaticStore.getMusicDataSource(Identifier.get(music)) ?: return

                        sound.setDataSource(g.absolutePath)

                        sound.isLooping = looping

                        val names = StaticStore.musicnames[m.pack] ?: return

                        name.text = StaticStore.generateIdName(m, this@MusicPlayer)
                        max.text = names[m.id]

                        val mmr = MediaMetadataRetriever()
                        mmr.setDataSource(g.absolutePath)

                        muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0

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
                            var index = indexOf(music) - 1

                            if (index < 0)
                                index = 0

                            music = StaticStore.musicData[index]

                            val m = music ?: return

                            val g = StaticStore.getMusicDataSource(Identifier.get(m)) ?: return

                            sound.reset()

                            sound.setDataSource(g.absolutePath)

                            sound.isLooping = looping

                            val names = StaticStore.musicnames[m.pack] ?: return

                            name.text = StaticStore.generateIdName(m, this@MusicPlayer)
                            max.text = names[m.id]

                            val mmr = MediaMetadataRetriever()
                            mmr.setDataSource(g.absolutePath)

                            muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0

                            muprog.progress = sound.currentPosition
                            current.text = getTime(sound.currentPosition)

                            if (!paused)
                                sound.start()
                        }
                    }

                })

                mulist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    completed = false
                    paused = false

                    if (sound.isPlaying || sound.isRunning) {
                        sound.pause()
                        sound.stop()
                    }

                    sound.reset()

                    music = StaticStore.musicData[position]

                    val m = music ?: return@OnItemClickListener

                    val g = StaticStore.getMusicDataSource(Identifier.get(m)) ?: return@OnItemClickListener

                    sound.setDataSource(g.absolutePath)

                    sound.isLooping = looping

                    val names = StaticStore.musicnames[m.pack] ?: return@OnItemClickListener

                    name.text = StaticStore.generateIdName(m, this@MusicPlayer)
                    max.text = names[m.id]

                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(g.absolutePath)

                    muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0
                    play.setImageDrawable(ContextCompat.getDrawable(this@MusicPlayer, R.drawable.ic_pause_black_24dp))

                    sound.start()
                }

                sound.setOnCompletionListener {
                    if (!sound.isLooping && !next) {
                        play.setImageDrawable(ContextCompat.getDrawable(this@MusicPlayer, R.drawable.ic_play_arrow_black_24dp))
                        completed = true
                        sound.isRunning = false
                        paused = true
                    }

                    if (next && !sound.isLooping) {
                        var index = indexOf(music) + 1

                        if (index >= StaticStore.musicData.size) {
                            index = 0
                        }

                        music = StaticStore.musicData[index]

                        val m = music ?: return@setOnCompletionListener

                        val g = StaticStore.getMusicDataSource(Identifier.get(m)) ?: return@setOnCompletionListener

                        sound.reset()

                        sound.setDataSource(g.absolutePath)

                        muprog.max = StaticStore.durations[index]

                        val names = StaticStore.musicnames[m.pack] ?: return@setOnCompletionListener

                        name.text = StaticStore.generateIdName(m, this@MusicPlayer)
                        max.text = names[m.id]

                        sound.start()
                    }
                }

                val index = indexOf(music)

                val m = music ?: return@launch

                muprog.max = StaticStore.durations[index]
                muprog.progress = prog

                val fname = StaticStore.generateIdName(m, this@MusicPlayer)

                name.text = fname

                val mnames = StaticStore.musicnames[m.pack] ?: return@launch

                max.text = mnames[m.id]

                val names = ArrayList<Identifier<Music>>()

                for(i in UserProfile.getAllPacks()) {
                    for(j in i.musics.list.indices) {
                        names.add(i.musics.list[j].id)
                    }
                }

                val adapter = MusicListAdapter(this@MusicPlayer, names, m.pack, true)
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

                                val h = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    val me = windowManager.currentWindowMetrics

                                    me.bounds.top
                                } else {
                                    val display = windowManager.defaultDisplay
                                    val p = Point()

                                    display.getSize(p)

                                    p.y
                                }

                                h - toolh - sh
                            } else {
                                playlayout.height
                            }
                        }

                        if (ah == -1) {
                            ah = album.height
                        }

                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            val aanim = ScaleAnimator(album, AnimatorConst.Dimension.WIDTH, 200, AnimatorConst.Accelerator.ACCELDECEL, ah, 0)
                            aanim.start()

                            val aalanim = AlphaAnimator(album, 200, AnimatorConst.Accelerator.ACCELDECEL, 1f, 0f)
                            aalanim.start()

                            mulist.postDelayed({
                                val mulanim = ScaleAnimator(mulist, AnimatorConst.Dimension.HEIGHT, 200, AnimatorConst.Accelerator.ACCELDECEL, 0, ch)
                                mulanim.start()

                                val mulalanim = AlphaAnimator(mulist, 200, AnimatorConst.Accelerator.ACCELDECEL, 0f, 1f)
                                mulalanim.start()
                            }, 200)
                        } else {
                            val canim = ScaleAnimator(playlayout, AnimatorConst.Dimension.HEIGHT, 200, AnimatorConst.Accelerator.ACCELDECEL, ch, StaticStore.dptopx(254f, this@MusicPlayer))
                            canim.start()

                            val aanim = AlphaAnimator(album, 200, AnimatorConst.Accelerator.ACCELDECEL, 1f, 0f)
                            aanim.start()
                        }
                    }

                })

                close.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        mumenu.show()
                        close.hide()

                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            val mulanim = ScaleAnimator(mulist, AnimatorConst.Dimension.HEIGHT, 200, AnimatorConst.Accelerator.ACCELDECEL, ch, 0)
                            mulanim.start()

                            val mulalanim = AlphaAnimator(mulist, 200, AnimatorConst.Accelerator.ACCELDECEL, 1f, 0f)
                            mulalanim.start()

                            mulist.postDelayed({
                                val aanim = ScaleAnimator(album, AnimatorConst.Dimension.WIDTH, 200, AnimatorConst.Accelerator.ACCELDECEL, 0, ah)
                                aanim.start()

                                val aalanim = AlphaAnimator(album, 200, AnimatorConst.Accelerator.ACCELDECEL, 0f, 1f)
                                aalanim.start()
                            }, 200)
                        } else {
                            val canim = ScaleAnimator(playlayout, AnimatorConst.Dimension.HEIGHT, 200, AnimatorConst.Accelerator.ACCELDECEL, StaticStore.dptopx(254f, this@MusicPlayer), ch)
                            canim.start()

                            val aanim = AlphaAnimator(album, 200, AnimatorConst.Accelerator.ACCELDECEL, 0f, 1f)
                            aanim.start()
                        }
                    }

                })

                val handler = Handler(Looper.getMainLooper())

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
                    play.setImageDrawable(ContextCompat.getDrawable(this@MusicPlayer, R.drawable.ic_play_arrow_black_24dp))
                }

                onBackPressedDispatcher.addCallback(this@MusicPlayer, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        back.performClick()
                    }
                })

                StaticStore.setAppear(name, current, max, muprog, mulist, mumenu, close, album, playlayout, loop, play, vol, nextsong, nex, prev)
                StaticStore.setDisappear(progress, st)
            } catch (e: IllegalStateException) {
                StaticStore.showShortMessage(this@MusicPlayer, R.string.music_err)

                ErrorLogWriter.writeLog(e, StaticStore.upload, this@MusicPlayer)

                finish()
            }
        }
    }

    override fun onDestroy() {
        musicreceive.unregister()
        destroyed = true
        unregisterReceiver(musicreceive)
        StaticStore.toast = null
        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val lang = shared?.getInt("Language",0) ?: 0

        val config = Configuration()
        var language = StaticStore.lang[lang]
        var country = ""

        if(language == "") {
            language = Resources.getSystem().configuration.locales.get(0).language
            country = Resources.getSystem().configuration.locales.get(0).country
        }

        val loc = if(country.isNotEmpty()) {
            Locale(language, country)
        } else {
            Locale(language)
        }

        config.setLocale(loc)
        applyOverrideConfiguration(config)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
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

    fun indexOf(id: Identifier<Music>?) : Int {
        if(id == null)
            return -1
        else {
            for (i in StaticStore.musicData.indices)
                if (StaticStore.musicData[i].equals(id))
                    return i

            return -1
        }
    }

    @Suppress("unused")
    class MusicReceiver : BroadcastReceiver {
        private var unregister = false
        private var rotated = true
        private val ac: WeakReference<Activity>?

        constructor() {
            ac = null
        }

        constructor(ac: Activity) {
            this.ac = WeakReference(ac)
        }

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
                            val play: FloatingActionButton = ac?.get()?.findViewById(R.id.musicplay) ?: return

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
