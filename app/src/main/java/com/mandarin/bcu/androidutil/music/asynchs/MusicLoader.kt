package com.mandarin.bcu.androidutil.music.asynchs

import android.app.Activity
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.os.Handler
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.MusicPlayer
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.AlphaAnimator
import com.mandarin.bcu.androidutil.adapters.AnimatorConst
import com.mandarin.bcu.androidutil.adapters.ScaleAnimator
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer
import com.mandarin.bcu.androidutil.music.adapters.MusicListAdapter
import common.CommonStatic
import common.util.pack.Pack
import java.io.File
import java.lang.ref.WeakReference
import kotlin.math.round

class MusicLoader(activity: Activity) : AsyncTask<Void, Int, Void>() {
    private val weak = WeakReference(activity)
    private var ch = -1
    private var ah = -1
    private var destroyed = false
    private var isControlling = false

    override fun onPreExecute() {
        val ac = weak.get() ?: return

        val name: TextView = ac.findViewById(R.id.musicname)
        val current: TextView = ac.findViewById(R.id.musiccurrent)
        val max: TextView = ac.findViewById(R.id.musicmaxdu)
        val muprog: SeekBar = ac.findViewById(R.id.musicprogress)
        val mulist: ListView = ac.findViewById(R.id.musiclist)
        val mumenu: FloatingActionButton = ac.findViewById(R.id.musicmenu)
        val close: FloatingActionButton = ac.findViewById(R.id.musicclose)
        val album: ImageView = ac.findViewById(R.id.musicalbum)
        val playlayout: ConstraintLayout = ac.findViewById(R.id.musicplayerlayout)
        val loop: FloatingActionButton = ac.findViewById(R.id.musicloop)
        val play: FloatingActionButton = ac.findViewById(R.id.musicplay)
        val back: FloatingActionButton = ac.findViewById(R.id.musicbck)
        val vol: SeekBar = ac.findViewById(R.id.musicsound)
        val nextsong: FloatingActionButton = ac.findViewById(R.id.musicshuff)
        val nex: FloatingActionButton = ac.findViewById(R.id.musicnext)
        val prev: FloatingActionButton = ac.findViewById(R.id.musicprev)

        back.setOnClickListener {
            destroyed = true
            if (!MusicPlayer.sound.isReleased && MusicPlayer.sound.isInitialized) {
                if (MusicPlayer.sound.isRunning || MusicPlayer.sound.isPlaying) {
                    MusicPlayer.sound.pause()
                    MusicPlayer.sound.stop()
                }

                MusicPlayer.sound.reset()
            }
            MusicPlayer.sound.setOnCompletionListener {

            }
            MusicPlayer.reset()
            ac.finish()
        }

        setDisappear(name, current, max, muprog, mulist, mumenu, close, album, playlayout, loop, play, vol, nextsong, nex, prev)
    }

    override fun doInBackground(vararg params: Void?): Void? {
        val ac = weak.get() ?: return null

        if(!StaticStore.musicread) {
            SoundHandler.read(ac)
            StaticStore.musicread = true
        }

        if(StaticStore.musicnames.size != Pack.def.ms.size())
            for(i in Pack.def.ms.list.indices) {
                val f = Pack.def.ms[i]

                val sp = SoundPlayer()
                sp.setDataSource(f.toString())
                sp.prepare()
                StaticStore.durations.add(sp.duration)

                var time = sp.duration.toFloat()/1000f

                sp.release()

                var min = (time/60f).toInt()

                time -= min.toFloat()*60f

                var sec = round(time).toInt()

                if(sec == 60) {
                    min += 1
                    sec = 0
                }

                val mins = min.toString()

                val secs = if(sec < 10) "0$sec"
                else sec.toString()

                StaticStore.musicnames.add("$mins:$secs")
            }

        publishProgress(0)

        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val ac = weak.get() ?: return

        if (MusicPlayer.posit >= StaticStore.musicnames.size)
            return
        
        if(values[0] == 0) {
            val name: TextView = ac.findViewById(R.id.musicname)
            val current: TextView = ac.findViewById(R.id.musiccurrent)
            val max: TextView = ac.findViewById(R.id.musicmaxdu)
            val muprog: SeekBar = ac.findViewById(R.id.musicprogress)
            val mulist: ListView = ac.findViewById(R.id.musiclist)
            val mumenu: FloatingActionButton = ac.findViewById(R.id.musicmenu)
            val close: FloatingActionButton = ac.findViewById(R.id.musicclose)
            val album: ImageView = ac.findViewById(R.id.musicalbum)
            val playlayout: ConstraintLayout = ac.findViewById(R.id.musicplayerlayout)
            val loop: FloatingActionButton = ac.findViewById(R.id.musicloop)
            val play: FloatingActionButton = ac.findViewById(R.id.musicplay)
            val vol: SeekBar = ac.findViewById(R.id.musicsound)
            val nextsong: FloatingActionButton = ac.findViewById(R.id.musicshuff)
            val nex: FloatingActionButton = ac.findViewById(R.id.musicnext)
            val prev: FloatingActionButton = ac.findViewById(R.id.musicprev)

            close.hide()

            val f = Pack.def.ms.get(MusicPlayer.posit)

            if (!f.exists()) return

            if (!MusicPlayer.sound.isInitialized)
                MusicPlayer.sound.setDataSource(f.absolutePath)

            if (!MusicPlayer.sound.isPrepared) {
                MusicPlayer.sound.prepareAsync()
                MusicPlayer.sound.setOnPreparedListener {
                    MusicPlayer.sound.isPrepared = true
                    MusicPlayer.sound.start()
                }
            }

            vol.max = 99
            vol.progress = MusicPlayer.volume

            if (ac.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                val display = DisplayMetrics()
                ac.windowManager.defaultDisplay.getMetrics(display)

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
                    MusicPlayer.volume = progress

                    val scale = StaticStore.getVolumScaler(progress)

                    MusicPlayer.sound.setVolume(scale, scale)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }

            })

            play.setOnClickListener {
                if (MusicPlayer.sound.isPlaying || MusicPlayer.sound.isRunning) {
                    MusicPlayer.paused = true
                    MusicPlayer.sound.pause()
                    play.setImageDrawable(ac.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                } else {
                    MusicPlayer.paused = false
                    if (MusicPlayer.completed) {
                        MusicPlayer.sound.seekTo(0)
                        MusicPlayer.sound.start()
                        MusicPlayer.completed = false
                    } else {
                        MusicPlayer.sound.start()
                    }

                    play.setImageDrawable(ac.getDrawable(R.drawable.ic_pause_black_24dp))
                }
            }

            nex.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    if (MusicPlayer.sound.isPlaying || MusicPlayer.sound.isRunning) {
                        MusicPlayer.sound.pause()
                        MusicPlayer.sound.stop()
                    }

                    MusicPlayer.completed = false

                    MusicPlayer.sound.reset()

                    MusicPlayer.posit += 1

                    if (MusicPlayer.posit >= Pack.def.ms.size()) {
                        MusicPlayer.posit = 0
                    }

                    val g = Pack.def.ms[MusicPlayer.posit]

                    if (!g.exists()) {
                        StaticStore.showShortMessage(ac, "No File Found")
                        nex.performClick()
                    }

                    MusicPlayer.sound.setDataSource(g.absolutePath)

                    MusicPlayer.sound.isLooping = MusicPlayer.looping

                    name.text = g.name
                    max.text = StaticStore.musicnames[MusicPlayer.posit]

                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(g.absolutePath)

                    muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()

                    muprog.progress = MusicPlayer.sound.currentPosition
                    current.text = getTime(MusicPlayer.sound.currentPosition)

                    if (!MusicPlayer.paused)
                        MusicPlayer.sound.start()
                }

            })

            prev.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    var ratio = 0

                    MusicPlayer.completed = false

                    if (!MusicPlayer.sound.isReleased && MusicPlayer.sound.isInitialized)
                        ratio = (MusicPlayer.sound.currentPosition.toFloat() / muprog.max.toFloat() * 100).toInt()

                    if (ratio > 5) {
                        MusicPlayer.sound.pause()
                        MusicPlayer.sound.seekTo(0)
                        MusicPlayer.sound.start()
                    } else {
                        MusicPlayer.posit -= 1

                        if (MusicPlayer.posit < 0)
                            MusicPlayer.posit = 0

                        val g = Pack.def.ms[MusicPlayer.posit]

                        if (!g.exists()) {
                            StaticStore.showShortMessage(ac, "No File Found")
                            nex.performClick()
                        }

                        MusicPlayer.sound.reset()

                        MusicPlayer.sound.setDataSource(g.absolutePath)

                        MusicPlayer.sound.isLooping = MusicPlayer.looping

                        name.text = g.name
                        max.text = StaticStore.musicnames[MusicPlayer.posit]

                        val mmr = MediaMetadataRetriever()
                        mmr.setDataSource(g.absolutePath)

                        muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()

                        muprog.progress = MusicPlayer.sound.currentPosition
                        current.text = getTime(MusicPlayer.sound.currentPosition)

                        if (!MusicPlayer.paused)
                            MusicPlayer.sound.start()
                    }
                }

            })

            mulist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                MusicPlayer.posit = position

                MusicPlayer.completed = false
                MusicPlayer.paused = false

                if (MusicPlayer.sound.isPlaying || MusicPlayer.sound.isRunning) {
                    MusicPlayer.sound.pause()
                    MusicPlayer.sound.stop()
                }

                MusicPlayer.sound.reset()

                val g = Pack.def.ms[MusicPlayer.posit]

                if (!g.exists()) {
                    StaticStore.showShortMessage(ac, "No File Found")
                    nex.performClick()
                }

                MusicPlayer.sound.setDataSource(g.absolutePath)

                MusicPlayer.sound.isLooping = MusicPlayer.looping

                name.text = g.name
                max.text = StaticStore.musicnames[MusicPlayer.posit]

                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(g.absolutePath)

                muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()
                play.setImageDrawable(ac.getDrawable(R.drawable.ic_pause_black_24dp))

                MusicPlayer.sound.start()
            }

            MusicPlayer.sound.setOnCompletionListener {
                if (!MusicPlayer.sound.isLooping && !MusicPlayer.next) {
                    play.setImageDrawable(ac.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                    MusicPlayer.completed = true
                    MusicPlayer.sound.isRunning = false
                    MusicPlayer.paused = true
                }

                if (MusicPlayer.next && !MusicPlayer.sound.isLooping) {
                    MusicPlayer.posit += 1

                    if (MusicPlayer.posit >= Pack.def.ms.size()) {
                        MusicPlayer.posit = 0
                    }

                    val g = Pack.def.ms.get(MusicPlayer.posit)

                    if (!g.exists()) {
                        play.setImageDrawable(ac.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                        StaticStore.showShortMessage(ac, "No File Found")
                        return@setOnCompletionListener
                    }

                    MusicPlayer.sound.reset()

                    MusicPlayer.sound.setDataSource(g.absolutePath)

                    muprog.max = StaticStore.durations[MusicPlayer.posit]
                    name.text = g.name
                    max.text = StaticStore.musicnames[MusicPlayer.posit]

                    MusicPlayer.sound.start()
                }
            }

            muprog.max = StaticStore.durations[MusicPlayer.posit]
            muprog.progress = MusicPlayer.prog
            name.text = f.name
            max.text = StaticStore.musicnames[MusicPlayer.posit]

            val loc = ArrayList<Int>()
            val names = arrayOfNulls<String>(Pack.def.ms.size())

            for (i in Pack.def.ms.list.indices) {
                loc.add(getFileLoc(Pack.def.ms.list[i]))
                names[i] = Pack.def.ms.list[i].name
            }

            val adapter = MusicListAdapter(ac, names, loc)
            mulist.adapter = adapter

            loop.setOnClickListener {
                if (MusicPlayer.sound.isLooping) {
                    MusicPlayer.looping = false
                    loop.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(ac, R.attr.HintPrimary))
                } else {
                    loop.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(ac, R.attr.colorAccent))
                    MusicPlayer.next = false
                    MusicPlayer.looping = true
                    nextsong.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(ac, R.attr.HintPrimary))
                }

                MusicPlayer.sound.isLooping = !MusicPlayer.sound.isLooping
            }

            nextsong.setOnClickListener {
                if (MusicPlayer.next) {
                    nextsong.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(ac, R.attr.HintPrimary))
                } else {
                    nextsong.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(ac, R.attr.colorAccent))
                    MusicPlayer.sound.isLooping = false
                    MusicPlayer.looping = false
                    loop.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(ac, R.attr.HintPrimary))
                }

                MusicPlayer.next = !MusicPlayer.next
            }

            muprog.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        current.text = getTime(progress)
                    }

                    MusicPlayer.completed = false
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    isControlling = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (!MusicPlayer.sound.isReleased && MusicPlayer.sound.isInitialized) {
                        MusicPlayer.sound.seekTo(seekBar?.progress ?: 0)
                    }

                    isControlling = false
                }

            })

            mumenu.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    mumenu.hide()
                    close.show()

                    if (ch == -1) {
                        ch = if (ac.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            val toolbar: Toolbar = ac.findViewById(R.id.musictool)
                            val toolh = toolbar.height

                            val rect = Rect()

                            ac.window.decorView.getWindowVisibleDisplayFrame(rect)

                            val sh = rect.top

                            val display = ac.windowManager.defaultDisplay
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

                    if (ac.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
                        val canim = ScaleAnimator(playlayout, AnimatorConst.HEIGHT, 200, AnimatorConst.DECELERATE, ch, StaticStore.dptopx(254f, ac))
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

                    if (ac.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
                        val canim = ScaleAnimator(playlayout, AnimatorConst.HEIGHT, 200, AnimatorConst.DECELERATE, StaticStore.dptopx(254f, ac), ch)
                        canim.start()

                        val aanim = AlphaAnimator(album, 200, AnimatorConst.DECELERATE, 0f, 1f)
                        aanim.start()
                    }
                }

            })

            val handler = Handler()

            val runnable = object : Runnable {
                override fun run() {
                    if (MusicPlayer.sound.isInitialized && !MusicPlayer.sound.isReleased && !isControlling) {
                        if (MusicPlayer.sound.isPlaying || MusicPlayer.sound.isRunning) {
                            muprog.progress = MusicPlayer.sound.currentPosition
                            current.text = getTime(MusicPlayer.sound.currentPosition)
                        }
                    }

                    if (!destroyed)
                        handler.postDelayed(this, 10)
                }
            }

            handler.postDelayed(runnable, 10)

            if (MusicPlayer.looping) {
                loop.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(ac, R.attr.colorAccent))
            }

            if (MusicPlayer.next) {
                nextsong.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(ac, R.attr.colorAccent))
            }

            if (MusicPlayer.paused || MusicPlayer.completed) {
                play.setImageDrawable(ac.getDrawable(R.drawable.ic_play_arrow_black_24dp))
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        val ac = weak.get() ?: return

        val name: TextView = ac.findViewById(R.id.musicname)
        val current: TextView = ac.findViewById(R.id.musiccurrent)
        val max: TextView = ac.findViewById(R.id.musicmaxdu)
        val muprog: SeekBar = ac.findViewById(R.id.musicprogress)
        val mulist: ListView = ac.findViewById(R.id.musiclist)
        val mumenu: FloatingActionButton = ac.findViewById(R.id.musicmenu)
        val close: FloatingActionButton = ac.findViewById(R.id.musicclose)
        val album: ImageView = ac.findViewById(R.id.musicalbum)
        val playlayout: ConstraintLayout = ac.findViewById(R.id.musicplayerlayout)
        val loop: FloatingActionButton = ac.findViewById(R.id.musicloop)
        val play: FloatingActionButton = ac.findViewById(R.id.musicplay)
        val vol: SeekBar = ac.findViewById(R.id.musicsound)
        val nextsong: FloatingActionButton = ac.findViewById(R.id.musicshuff)
        val nex: FloatingActionButton = ac.findViewById(R.id.musicnext)
        val prev: FloatingActionButton = ac.findViewById(R.id.musicprev)
        val progbar: ProgressBar = ac.findViewById(R.id.musicprogbar)
        val loadt: TextView = ac.findViewById(R.id.musicloadt)

        setAppear(name, current, max, muprog, mulist, mumenu, close, album, playlayout, loop, play, vol, nextsong, nex, prev)
        setDisappear(progbar, loadt)
    }

    private fun setAppear(vararg view: View) {
        for (v in view) {
            v.visibility = View.VISIBLE
        }
    }

    private fun setDisappear(vararg view: View) {
        for (v in view) {
            v.visibility = View.GONE
        }
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
}