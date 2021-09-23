package com.mandarin.bcu.androidutil.music.coroutine

import android.app.Activity
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.MusicPlayer
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer
import com.mandarin.bcu.androidutil.music.adapters.MusicListAdapter
import com.mandarin.bcu.androidutil.supports.*
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import common.util.stage.Music
import java.lang.ref.WeakReference
import kotlin.math.round

@Suppress("DEPRECATION")
class MusicLoader(activity: Activity) : CoroutineTask<String>() {
    private val weak = WeakReference(activity)
    private var ch = -1
    private var ah = -1
    var destroyed = false
    private var isControlling = false

    private val done = "done"

    override fun prepare() {
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

    override fun doSomething() {
        val ac = weak.get() ?: return

        Definer.define(ac, this::updateProg, this::updateText)

        publishProgress(StaticStore.TEXT, ac.getString(R.string.load_music_duratoin))

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

        publishProgress(done)
    }

    override fun progressUpdate(vararg data: String) {
        val ac = weak.get() ?: return

        val st = ac.findViewById<TextView>(R.id.status)

        when(data[0]) {
            StaticStore.TEXT -> st.text = data[1]
            StaticStore.PROG -> {
                val prog = ac.findViewById<ProgressBar>(R.id.prog)

                if(data[1].toInt() == -1) {
                    prog.isIndeterminate = true

                    return
                }

                prog.isIndeterminate = false
                prog.max = 10000
                prog.progress = data[1].toInt()
            }
            done -> {
                if(destroyed)
                    return

                val m0 = MusicPlayer.music ?: return

                StaticStore.musicnames[m0.pack] ?: return

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
                val prog = ac.findViewById<ProgressBar>(R.id.prog)

                prog.isIndeterminate = true

                close.hide()

                val f = StaticStore.getMusicDataSource(Identifier.get(MusicPlayer.music)) ?: return

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
                    val w = StaticStore.getScreenWidth(ac, false)

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
                        play.setImageDrawable(ContextCompat.getDrawable(ac, R.drawable.ic_play_arrow_black_24dp))
                    } else {
                        MusicPlayer.paused = false
                        if (MusicPlayer.completed) {
                            MusicPlayer.sound.seekTo(0)
                            MusicPlayer.sound.start()
                            MusicPlayer.completed = false
                        } else {
                            MusicPlayer.sound.start()
                        }

                        play.setImageDrawable(ContextCompat.getDrawable(ac, R.drawable.ic_pause_black_24dp))
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

                        var index = indexOf(MusicPlayer.music) + 1

                        if(index >= StaticStore.musicData.size) {
                            index = 0
                        }

                        MusicPlayer.music = StaticStore.musicData[index]

                        val m = MusicPlayer.music ?: return

                        val g = StaticStore.getMusicDataSource(Identifier.get(MusicPlayer.music)) ?: return

                        MusicPlayer.sound.setDataSource(g.absolutePath)

                        MusicPlayer.sound.isLooping = MusicPlayer.looping

                        val names = StaticStore.musicnames[m.pack] ?: return

                        name.text = StaticStore.generateIdName(m, ac)
                        max.text = names[m.id]

                        val mmr = MediaMetadataRetriever()
                        mmr.setDataSource(g.absolutePath)

                        muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0

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
                            var index = indexOf(MusicPlayer.music) - 1

                            if (index < 0)
                                index = 0

                            MusicPlayer.music = StaticStore.musicData[index]

                            val m = MusicPlayer.music ?: return

                            val g = StaticStore.getMusicDataSource(Identifier.get(m)) ?: return

                            MusicPlayer.sound.reset()

                            MusicPlayer.sound.setDataSource(g.absolutePath)

                            MusicPlayer.sound.isLooping = MusicPlayer.looping

                            val names = StaticStore.musicnames[m.pack] ?: return

                            name.text = StaticStore.generateIdName(m, ac)
                            max.text = names[m.id]

                            val mmr = MediaMetadataRetriever()
                            mmr.setDataSource(g.absolutePath)

                            muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0

                            muprog.progress = MusicPlayer.sound.currentPosition
                            current.text = getTime(MusicPlayer.sound.currentPosition)

                            if (!MusicPlayer.paused)
                                MusicPlayer.sound.start()
                        }
                    }

                })

                mulist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    MusicPlayer.completed = false
                    MusicPlayer.paused = false

                    if (MusicPlayer.sound.isPlaying || MusicPlayer.sound.isRunning) {
                        MusicPlayer.sound.pause()
                        MusicPlayer.sound.stop()
                    }

                    MusicPlayer.sound.reset()

                    MusicPlayer.music = StaticStore.musicData[position]

                    val m = MusicPlayer.music ?: return@OnItemClickListener

                    val g = StaticStore.getMusicDataSource(Identifier.get(m)) ?: return@OnItemClickListener

                    MusicPlayer.sound.setDataSource(g.absolutePath)

                    MusicPlayer.sound.isLooping = MusicPlayer.looping

                    val names = StaticStore.musicnames[m.pack] ?: return@OnItemClickListener

                    name.text = StaticStore.generateIdName(m, ac)
                    max.text = names[m.id]

                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(g.absolutePath)

                    muprog.max = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0
                    play.setImageDrawable(ContextCompat.getDrawable(ac, R.drawable.ic_pause_black_24dp))

                    MusicPlayer.sound.start()
                }

                MusicPlayer.sound.setOnCompletionListener {
                    if (!MusicPlayer.sound.isLooping && !MusicPlayer.next) {
                        play.setImageDrawable(ContextCompat.getDrawable(ac, R.drawable.ic_play_arrow_black_24dp))
                        MusicPlayer.completed = true
                        MusicPlayer.sound.isRunning = false
                        MusicPlayer.paused = true
                    }

                    if (MusicPlayer.next && !MusicPlayer.sound.isLooping) {
                        var index = indexOf(MusicPlayer.music) + 1

                        if (index >= StaticStore.musicData.size) {
                            index = 0
                        }

                        MusicPlayer.music = StaticStore.musicData[index]

                        val m = MusicPlayer.music ?: return@setOnCompletionListener

                        val g = StaticStore.getMusicDataSource(Identifier.get(m)) ?: return@setOnCompletionListener

                        MusicPlayer.sound.reset()

                        MusicPlayer.sound.setDataSource(g.absolutePath)

                        muprog.max = StaticStore.durations[index]

                        val names = StaticStore.musicnames[m.pack] ?: return@setOnCompletionListener

                        name.text = StaticStore.generateIdName(m, ac)
                        max.text = names[m.id]

                        MusicPlayer.sound.start()
                    }
                }

                val index = indexOf(MusicPlayer.music)

                val m = MusicPlayer.music ?: return

                muprog.max = StaticStore.durations[index]
                muprog.progress = MusicPlayer.prog

                val fname = StaticStore.generateIdName(m, ac)

                name.text = fname

                val mnames = StaticStore.musicnames[m.pack] ?: return

                max.text = mnames[m.id]

                val names = ArrayList<Identifier<Music>>()

                for(i in UserProfile.getAllPacks()) {
                    for(j in i.musics.list.indices) {
                        names.add(i.musics.list[j].id)
                    }
                }

                val adapter = MusicListAdapter(ac, names, m.pack, true)
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

                                val h = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    val me = ac.windowManager.currentWindowMetrics

                                    me.bounds.top
                                } else {
                                    val display = ac.windowManager.defaultDisplay
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

                val handler = Handler(Looper.getMainLooper())

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
                    play.setImageDrawable(ContextCompat.getDrawable(ac, R.drawable.ic_play_arrow_black_24dp))
                }
            }
        }
    }

    override fun finish() {
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
        val progbar: ProgressBar = ac.findViewById(R.id.prog)
        val loadt: TextView = ac.findViewById(R.id.status)

        setAppear(name, current, max, muprog, mulist, mumenu, close, album, playlayout, loop, play, vol, nextsong, nex, prev)
        setDisappear(progbar, loadt)
    }

    private fun updateText(info: String) {
        val ac = weak.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProg(p: Double) {
        publishProgress(StaticStore.PROG, (p * 10000.0).toInt().toString())
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
}