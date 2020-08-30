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
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
import common.util.Data
import common.util.pack.Pack
import java.lang.ref.WeakReference
import kotlin.math.round

class MusicLoader(activity: Activity) : AsyncTask<Void, Int, Void>() {
    private val weak = WeakReference(activity)
    private var ch = -1
    private var ah = -1
    var destroyed = false
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
            if (MusicPlayer.sound?.isReleased == false && MusicPlayer.sound?.isInitialized == true) {
                if (MusicPlayer.sound?.isRunning == true || MusicPlayer.sound?.isPlaying == true) {
                    MusicPlayer.sound?.pause()
                    MusicPlayer.sound?.stop()
                }

                MusicPlayer.sound?.reset()
            }
            MusicPlayer.sound?.setOnCompletionListener {

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

        if(StaticStore.musicnames.size != Pack.map.size || StaticStore.musicData.isEmpty()) {
            StaticStore.musicnames.clear()
            StaticStore.musicData.clear()

            for (i in Pack.map) {
                val names = ArrayList<String>()

                for (j in i.value.ms.list.indices) {
                    val f = i.value.ms.list[j]

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

                    names.add("$mins:$secs")

                    StaticStore.musicData.add(i.key.toString() + "\\" + j)
                }

                StaticStore.musicnames[i.key] = names
            }
        }

        publishProgress(0)

        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val ac = weak.get() ?: return

        val music = StaticStore.musicnames[MusicPlayer.pid] ?: return

        if (MusicPlayer.posit >= music.size)
            MusicPlayer.posit = 0
        
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

            val fp = Pack.map[MusicPlayer.pid] ?: return

            val f = fp.ms.list[MusicPlayer.posit]

            if (!f.exists())
                return

            if (MusicPlayer.sound?.isInitialized == false)
                MusicPlayer.sound?.setDataSource(f.absolutePath)

            if (MusicPlayer.sound?.isPrepared == false) {
                MusicPlayer.sound?.prepareAsync()
                MusicPlayer.sound?.setOnPreparedListener {
                    MusicPlayer.sound?.isPrepared = true
                    MusicPlayer.sound?.start()
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

                    MusicPlayer.sound?.setVolume(scale, scale)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }

            })

            play.setOnClickListener {
                if (MusicPlayer.sound?.isPlaying == true || MusicPlayer.sound?.isRunning == true) {
                    MusicPlayer.paused = true
                    MusicPlayer.sound?.pause()
                    play.setImageDrawable(ContextCompat.getDrawable(ac, R.drawable.ic_play_arrow_black_24dp))
                } else {
                    MusicPlayer.paused = false
                    if (MusicPlayer.completed) {
                        MusicPlayer.sound?.seekTo(0)
                        MusicPlayer.sound?.start()
                        MusicPlayer.completed = false
                    } else {
                        MusicPlayer.sound?.start()
                    }

                    play.setImageDrawable(ContextCompat.getDrawable(ac, R.drawable.ic_pause_black_24dp))
                }
            }

            nex.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    if (MusicPlayer.sound?.isPlaying == true || MusicPlayer.sound?.isRunning == true) {
                        MusicPlayer.sound?.pause()
                        MusicPlayer.sound?.stop()
                    }

                    MusicPlayer.completed = false

                    MusicPlayer.sound?.reset()

                    val currentData = MusicPlayer.pid.toString() + "\\" + MusicPlayer.posit

                    var index = StaticStore.musicData.indexOf(currentData) + 1

                    if(index >= StaticStore.musicData.size) {
                        index = 0
                    }

                    val info = StaticStore.musicData[index].split("\\")

                    if(info.size != 2) {
                        Log.e("MusicLoader", "Invalid String format : " + StaticStore.musicData[index])
                        return
                    }

                    MusicPlayer.pid = info[0].toInt()
                    MusicPlayer.posit = info[1].toInt()

                    val p = Pack.map[MusicPlayer.pid] ?: return

                    if(MusicPlayer.posit >= p.ms.size())
                        MusicPlayer.posit = 0

                    val g = p.ms.list[MusicPlayer.posit]

                    if (!g.exists()) {
                        StaticStore.showShortMessage(ac, "No File Found")
                        nex.performClick()
                    }

                    MusicPlayer.sound?.setDataSource(g.absolutePath)

                    MusicPlayer.sound?.isLooping = MusicPlayer.looping

                    val names = StaticStore.musicnames[MusicPlayer.pid] ?: return

                    val gname = Data.hex(MusicPlayer.pid) + " - " + g.name
                    name.text = gname
                    max.text = names[MusicPlayer.posit]

                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(g.absolutePath)

                    muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()

                    muprog.progress = MusicPlayer.sound?.currentPosition ?: 0
                    current.text = getTime(MusicPlayer.sound?.currentPosition ?: 0)

                    if (!MusicPlayer.paused)
                        MusicPlayer.sound?.start()
                }

            })

            prev.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    var ratio = 0

                    MusicPlayer.completed = false

                    if (MusicPlayer.sound?.isReleased == false && MusicPlayer.sound?.isInitialized == true)
                        ratio = (MusicPlayer.sound?.currentPosition?.toFloat() ?: 0 / muprog.max.toFloat() * 100).toInt()

                    if (ratio > 5) {
                        MusicPlayer.sound?.pause()
                        MusicPlayer.sound?.seekTo(0)
                        MusicPlayer.sound?.start()
                    } else {
                        var index = StaticStore.musicData.indexOf(MusicPlayer.pid.toString() + "\\" + MusicPlayer.posit) - 1

                        if (index < 0)
                            index = 0

                        val info = StaticStore.musicData[index].split("\\")

                        if(info.size != 2) {
                            Log.e("MusicLoader", "Invalid String Format : "+StaticStore.musicData[index])
                            return
                        }

                        val p = Pack.map[info[0].toInt()] ?: return

                        val g = p.ms.list[info[1].toInt()]

                        if (!g.exists()) {
                            StaticStore.showShortMessage(ac, "No File Found")
                            nex.performClick()
                            return
                        }

                        MusicPlayer.sound?.reset()

                        MusicPlayer.sound?.setDataSource(g.absolutePath)

                        MusicPlayer.sound?.isLooping = MusicPlayer.looping

                        MusicPlayer.pid = info[0].toInt()
                        MusicPlayer.posit = info[1].toInt()

                        val names = StaticStore.musicnames[MusicPlayer.pid] ?: return

                        if(MusicPlayer.posit >= p.ms.list.size)
                            MusicPlayer.posit = 0

                        val gname = Data.hex(MusicPlayer.pid) + " - " + g.name

                        name.text = gname
                        max.text = names[MusicPlayer.posit]

                        val mmr = MediaMetadataRetriever()
                        mmr.setDataSource(g.absolutePath)

                        muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()

                        muprog.progress = MusicPlayer.sound?.currentPosition ?: 0
                        current.text = getTime(MusicPlayer.sound?.currentPosition ?: 0)

                        if (!MusicPlayer.paused)
                            MusicPlayer.sound?.start()
                    }
                }

            })

            mulist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                MusicPlayer.posit = position

                MusicPlayer.completed = false
                MusicPlayer.paused = false

                if (MusicPlayer.sound?.isPlaying == true || MusicPlayer.sound?.isRunning == true) {
                    MusicPlayer.sound?.pause()
                    MusicPlayer.sound?.stop()
                }

                MusicPlayer.sound?.reset()

                val info = StaticStore.musicData[position].split("\\")

                if(info.size != 2) {
                    Log.e("MusicLoader", "Invalid String Format : "+StaticStore.musicData[position])
                    return@OnItemClickListener
                }

                MusicPlayer.pid = info[0].toInt()

                val p = Pack.map[MusicPlayer.pid] ?: return@OnItemClickListener

                MusicPlayer.posit = info[1].toInt()

                if(MusicPlayer.posit >= p.ms.list.size)
                    MusicPlayer.posit = 0

                val g = p.ms.list[MusicPlayer.posit]

                if (!g.exists()) {
                    StaticStore.showShortMessage(ac, "No File Found")
                    nex.performClick()
                }

                MusicPlayer.sound?.setDataSource(g.absolutePath)

                MusicPlayer.sound?.isLooping = MusicPlayer.looping

                val names = StaticStore.musicnames[MusicPlayer.pid] ?: return@OnItemClickListener

                val gname = Data.hex(MusicPlayer.pid) + " - " + g.name

                    name.text = gname
                max.text = names[MusicPlayer.posit]

                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(g.absolutePath)

                muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()
                play.setImageDrawable(ContextCompat.getDrawable(ac, R.drawable.ic_pause_black_24dp))

                MusicPlayer.sound?.start()
            }

            MusicPlayer.sound?.setOnCompletionListener {
                if (MusicPlayer.sound?.isLooping == true && !MusicPlayer.next) {
                    play.setImageDrawable(ContextCompat.getDrawable(ac, R.drawable.ic_play_arrow_black_24dp))
                    MusicPlayer.completed = true
                    MusicPlayer.sound?.isRunning = false
                    MusicPlayer.paused = true
                }

                if (MusicPlayer.next && MusicPlayer.sound?.isLooping == false) {
                    var index = StaticStore.musicData.indexOf(MusicPlayer.pid.toString() + "\\" + MusicPlayer.posit) + 1

                    if (index >= StaticStore.musicData.size) {
                        index = 0
                    }

                    val info = StaticStore.musicData[index].split("\\")

                    if(info.size != 2) {
                        Log.e("MusicLoader", "Invalid String Format : "+StaticStore.musicData[index])
                        return@setOnCompletionListener
                    }

                    MusicPlayer.pid = info[0].toInt()

                    val p = Pack.map[MusicPlayer.pid] ?: return@setOnCompletionListener

                    MusicPlayer.posit = info[1].toInt()

                    if(MusicPlayer.posit >= p.ms.list.size)
                        MusicPlayer.posit = 0

                    val g = p.ms.list[MusicPlayer.posit]

                    if (!g.exists()) {
                        play.setImageDrawable(ContextCompat.getDrawable(ac, R.drawable.ic_play_arrow_black_24dp))
                        StaticStore.showShortMessage(ac, "No File Found")
                        return@setOnCompletionListener
                    }

                    MusicPlayer.sound?.reset()

                    MusicPlayer.sound?.setDataSource(g.absolutePath)

                    muprog.max = StaticStore.durations[index]

                    val names = StaticStore.musicnames[MusicPlayer.pid] ?: return@setOnCompletionListener

                    val gname = Data.hex(MusicPlayer.pid) + " - " + g.name

                    name.text = gname
                    max.text = names[MusicPlayer.posit]

                    MusicPlayer.sound?.start()
                }
            }

            val index = StaticStore.musicData.indexOf(MusicPlayer.pid.toString()+"\\"+MusicPlayer.posit)

            muprog.max = StaticStore.durations[index]
            muprog.progress = MusicPlayer.prog

            val fname = Data.hex(MusicPlayer.pid) + " - " + f.name

            name.text = fname

            val mnaems = StaticStore.musicnames[MusicPlayer.pid] ?: return

            max.text = mnaems[MusicPlayer.posit]

            val names = ArrayList<String>()

            for(i in Pack.map) {
                for(j in i.value.ms.list.indices) {
                    names.add(Data.hex(i.key)+" - "+i.value.ms.list[j].name)
                }
            }

            val adapter = MusicListAdapter(ac, names, MusicPlayer.pid, true)
            mulist.adapter = adapter

            loop.setOnClickListener {
                if (MusicPlayer.sound?.isLooping == true) {
                    MusicPlayer.looping = false
                    loop.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(ac, R.attr.HintPrimary))
                } else {
                    loop.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(ac, R.attr.colorAccent))
                    MusicPlayer.next = false
                    MusicPlayer.looping = true
                    nextsong.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(ac, R.attr.HintPrimary))
                }

                MusicPlayer.sound?.isLooping = MusicPlayer.sound?.isLooping ?: false
            }

            nextsong.setOnClickListener {
                if (MusicPlayer.next) {
                    nextsong.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(ac, R.attr.HintPrimary))
                } else {
                    nextsong.imageTintList = ColorStateList.valueOf(StaticStore.getAttributeColor(ac, R.attr.colorAccent))
                    MusicPlayer.sound?.isLooping = false
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
                    if (MusicPlayer.sound?.isReleased == false && MusicPlayer.sound?.isInitialized == true) {
                        MusicPlayer.sound?.seekTo(seekBar?.progress ?: 0)
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
                        val aanim = ScaleAnimator(album, AnimatorConst.WIDTH, 200, AnimatorConst.ACCELDECEL, ah, 0)
                        aanim.start()

                        val aalanim = AlphaAnimator(album, 200, AnimatorConst.ACCELDECEL, 1f, 0f)
                        aalanim.start()

                        mulist.postDelayed({
                            val mulanim = ScaleAnimator(mulist, AnimatorConst.HEIGHT, 200, AnimatorConst.ACCELDECEL, 0, ch)
                            mulanim.start()

                            val mulalanim = AlphaAnimator(mulist, 200, AnimatorConst.ACCELDECEL, 0f, 1f)
                            mulalanim.start()
                        }, 200)
                    } else {
                        val canim = ScaleAnimator(playlayout, AnimatorConst.HEIGHT, 200, AnimatorConst.ACCELDECEL, ch, StaticStore.dptopx(254f, ac))
                        canim.start()

                        val aanim = AlphaAnimator(album, 200, AnimatorConst.ACCELDECEL, 1f, 0f)
                        aanim.start()
                    }
                }

            })

            close.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    mumenu.show()
                    close.hide()

                    if (ac.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        val mulanim = ScaleAnimator(mulist, AnimatorConst.HEIGHT, 200, AnimatorConst.ACCELDECEL, ch, 0)
                        mulanim.start()

                        val mulalanim = AlphaAnimator(mulist, 200, AnimatorConst.ACCELDECEL, 1f, 0f)
                        mulalanim.start()

                        mulist.postDelayed({
                            val aanim = ScaleAnimator(album, AnimatorConst.WIDTH, 200, AnimatorConst.ACCELDECEL, 0, ah)
                            aanim.start()

                            val aalanim = AlphaAnimator(album, 200, AnimatorConst.ACCELDECEL, 0f, 1f)
                            aalanim.start()
                        }, 200)
                    } else {
                        val canim = ScaleAnimator(playlayout, AnimatorConst.HEIGHT, 200, AnimatorConst.ACCELDECEL, StaticStore.dptopx(254f, ac), ch)
                        canim.start()

                        val aanim = AlphaAnimator(album, 200, AnimatorConst.ACCELDECEL, 0f, 1f)
                        aanim.start()
                    }
                }

            })

            val handler = Handler()

            val runnable = object : Runnable {
                override fun run() {
                    if (MusicPlayer.sound?.isInitialized == true && MusicPlayer.sound?.isReleased == false && !isControlling) {
                        if (MusicPlayer.sound?.isPlaying == true || MusicPlayer.sound?.isRunning == true) {
                            muprog.progress = MusicPlayer.sound?.currentPosition ?: 0
                            current.text = getTime(MusicPlayer.sound?.currentPosition ?: 0)
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
                play.setImageDrawable(ContextCompat.getDrawable(ac, R.drawable.ic_play_arrow_black_24dp))
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
}