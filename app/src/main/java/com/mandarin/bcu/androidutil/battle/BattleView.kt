package com.mandarin.bcu.androidutil.battle

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaPlayer
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mandarin.bcu.BattleSimulation
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.MediaPrepare
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.util.page.BBCtrl
import com.mandarin.bcu.util.page.BattleBox
import com.mandarin.bcu.util.page.BattleBox.BBPainter
import com.mandarin.bcu.util.page.BattleBox.OuterBox
import common.battle.BattleField
import common.battle.SBCtrl
import common.battle.entity.EEnemy
import common.util.ImgCore
import common.util.anim.ImgCut
import common.util.pack.Pack
import java.io.IOException
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.round

@SuppressLint("ViewConstructor")
class BattleView(context: Context, field: BattleField?, type: Int, axis: Boolean, private val activity: Activity) : View(context), BattleBox, OuterBox {
    @JvmField
    var painter: BBPainter = if (type == 0) BBPainter(this, field, this) else BBCtrl(this, field as SBCtrl?, this, StaticStore.dptopx(32f, context).toFloat())

    @JvmField
    var initialized = false
    @JvmField
    var paused = false
    var battleEnd = false
    var musicChanged = false
    @JvmField
    var spd = 0
    private var upd = 0
    private val cv: CVGraphics
    private val updater: Updater

    init {
        painter.dpi = StaticStore.dptopx(32f, context)
        ImgCore.ref = axis
        updater = Updater()
        for (i in SoundHandler.SE.indices) {
            SoundHandler.SE[i].clear()
        }
        for (fs in painter.bf.sb.b.lu.fs) {
            for (f in fs) {
                if (f != null) {
                    if (f.anim.uni.img.height == f.anim.uni.img.width) {
                        val cut = ImgCut.newIns("./org/data/uni.imgcut")
                        f.anim.uni.setCut(cut)
                        f.anim.uni.setImg(f.anim.uni.img)
                        f.anim.check()
                    } else {
                        f.anim.check()
                    }
                }
            }
        }
        SoundHandler.MUSIC = SoundPlayer()
        val preferences = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val musvol = (1 - ln(100 - preferences.getInt("mus_vol", 99).toDouble()) / ln(100.0)).toFloat()
        SoundHandler.MUSIC.setVolume(musvol, musvol)
        val f = Pack.def.ms[painter.bf.sb.st.mus0]
        SoundHandler.twoMusic = painter.bf.sb.st.mush != 0 && painter.bf.sb.st.mush != 100 && painter.bf.sb.st.mus0 != painter.bf.sb.st.mus1
        if (SoundHandler.twoMusic) SoundHandler.mu1 = painter.bf.sb.st.mus1
        if (f != null) {
            if (f.exists()) {
                try {
                    SoundHandler.MUSIC.isLooping = true
                    SoundHandler.MUSIC.setDataSource(f.absolutePath)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        for (e in painter.bf.sb.st.data.allEnemy) e.anim.check()
        updater.run()
        val cp = Paint()
        val bp = Paint()
        val gp = Paint()
        cv = CVGraphics(Canvas(), cp, bp, gp, true)
    }

    public override fun onDraw(c: Canvas) {
        if (initialized) {
            cv.setCanvas(c)
            painter.draw(cv)
            if (!paused) {
                if (spd > 0) {
                    var i = 0
                    while (i < 2.0.pow(spd.toDouble())) {
                        painter.bf.update()
                        i++
                    }
                    resetSE()
                } else if (spd < 0) {
                    if (upd / (1 - spd) >= 1) {
                        painter.bf.update()
                        resetSE()
                        upd = 0
                    } else {
                        upd++
                    }
                } else {
                    painter.bf.update()
                    resetSE()
                }
            }
        }
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(updater)
    }

    override fun getSpeed(): Int {
        return spd
    }

    override fun callBack(o: Any) {}
    private inner class Updater : Runnable {
        override fun run() {
            if (!paused) invalidate()
            if (!musicChanged) {
                if (haveToChangeMusic()) {
                    SoundHandler.haveToChange = true
                    SoundHandler.MUSIC.stop()
                    SoundHandler.MUSIC.reset()
                    SoundHandler.MUSIC.isLooping = true
                    val f = Pack.def.ms[painter.bf.sb.st.mus1]
                    if (f != null) {
                        try {
                            SoundHandler.MUSIC.setDataSource(f.absolutePath)
                            SoundHandler.MUSIC.prepareAsync()
                            SoundHandler.MUSIC.setOnPreparedListener(object : MediaPrepare() {
                                override fun prepare(mp: MediaPlayer?) {
                                    if(SoundHandler.musicPlay) {
                                        try {
                                            SoundHandler.MUSIC.start()
                                        } catch(e: NullPointerException) {
                                            ErrorLogWriter.writeLog(e, StaticStore.upload)
                                        }
                                    }
                                }
                            })
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    musicChanged = true
                }
            }
            if (!battleEnd) {
                checkWin()
                checkLose()
            }
            postDelayed(this, 1000L / 30L)
        }
    }

    override fun getPainter(): BBPainter {
        return painter
    }

    override fun paint() {}
    override fun reset() {}
    private fun haveToChangeMusic(): Boolean {
        if (painter.bf.sb.st.mush == 0 || painter.bf.sb.st.mush == 100) musicChanged = true
        return (painter.bf.sb.ebase.health.toFloat() / painter.bf.sb.ebase.maxH.toFloat() * 100).toInt() <painter.bf.sb.st.mush && painter.bf.sb.st.mush != 0 && painter.bf.sb.st.mush != 100
    }

    private fun resetSE() {
        for (i in SoundHandler.play!!.indices) SoundHandler.play!![i] = false
    }

    private fun checkWin() {
        if (painter.bf.sb.ebase.health <= 0) {
            SoundHandler.MUSIC.stop()
            SoundHandler.MUSIC.reset()
            SoundHandler.MUSIC.isLooping = false
            val f = Pack.def.ms[8]
            if (f != null) {
                if (f.exists()) {
                    try {
                        SoundHandler.MUSIC.setDataSource(f.absolutePath)
                        SoundHandler.MUSIC.prepareAsync()
                        SoundHandler.MUSIC.setOnPreparedListener(object : MediaPrepare() {
                            override fun prepare(mp: MediaPlayer?) {
                                SoundHandler.MUSIC.start()
                            }
                        })
                        SoundHandler.MUSIC.setOnCompletionListener {
                            SoundHandler.MUSIC.release()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            battleEnd = true
            SoundHandler.battleEnd = true

            val parentlay = activity.findViewById<CoordinatorLayout>(R.id.battlecoordinate)

            StaticStore.showShortSnack(parentlay,R.string.battle_won,BaseTransientBottomBar.LENGTH_LONG)
        }
    }

    private fun checkLose() {
        if (painter.bf.sb.ubase.health <= 0) {
            SoundHandler.MUSIC.stop()
            SoundHandler.MUSIC.reset()
            SoundHandler.MUSIC.isLooping = false
            val f = Pack.def.ms[9]
            if (f != null) {
                if (f.exists()) {
                    try {
                        SoundHandler.MUSIC.setDataSource(f.absolutePath)
                        SoundHandler.MUSIC.prepareAsync()
                        SoundHandler.MUSIC.setOnPreparedListener(object : MediaPrepare() {
                            override fun prepare(mp: MediaPlayer?) {
                                SoundHandler.MUSIC.start()
                            }
                        })
                        SoundHandler.MUSIC.setOnCompletionListener {
                            SoundHandler.MUSIC.release()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            battleEnd = true
            SoundHandler.battleEnd = true

            val bh = getBossHealth()

            val parentlay = activity.findViewById<CoordinatorLayout>(R.id.battlecoordinate)

            if(bh == -1) {
                val snack = Snackbar.make(parentlay,R.string.battle_lost,BaseTransientBottomBar.LENGTH_LONG)
                snack.setAction(R.string.battle_retry) {
                    retry()
                }
                snack.setActionTextColor(StaticStore.getAttributeColor(activity,R.attr.colorAccent))

                snack.show()
            } else {
                val snack = Snackbar.make(parentlay, context.getString(R.string.battle_lost_boss).replace("_", bh.toString()), BaseTransientBottomBar.LENGTH_LONG)
                snack.setAction(R.string.battle_retry) {
                    retry()
                }
                snack.setActionTextColor(StaticStore.getAttributeColor(activity,R.attr.colorAccent))

                snack.show()
            }
        }
    }

    private fun getBossHealth() : Int {
        var res = -1

        val bosses = ArrayList<EEnemy>()

        for(entity in painter.bf.sb.le) {
            if(entity.dire == 1) {

                if ((entity as EEnemy).mark == 1) {
                    bosses.add(entity)
                }
            }
        }

        if(bosses.isEmpty()) return res

        val boss = bosses[0]

        res = round(boss.health.toDouble()/boss.maxH*100).toInt()

        return res
    }

    fun retry() {
        val intent = Intent(activity,BattleSimulation::class.java)
        intent.putExtra("mapcode",painter.bf.sb.st.map.mc.id)
        intent.putExtra("stid",painter.bf.sb.st.map.id)
        intent.putExtra("stage",painter.bf.sb.st.id())
        intent.putExtra("star",painter.bf.sb.est.star)
        intent.putExtra("item",painter.bf.sb.conf[0])
        if(SoundHandler.MUSIC.isInitialized && !SoundHandler.MUSIC.isReleased) {
            if(SoundHandler.MUSIC.isRunning || SoundHandler.MUSIC.isPlaying) {
                SoundHandler.MUSIC.pause()
                SoundHandler.MUSIC.stop()
                SoundHandler.MUSIC.reset()
            }
        }

        SoundHandler.MUSIC.setOnCompletionListener {

        }
        SoundHandler.resetHandler()
        activity.startActivity(intent)
        activity.finish()
    }

    fun unload() {
        initialized = false

        for (fs in painter.bf.sb.b.lu.fs) {
            for (f in fs) {
                f?.anim?.unload()
            }
        }

        for (e in painter.bf.sb.st.data.allEnemy) e?.anim?.unload()
    }
}