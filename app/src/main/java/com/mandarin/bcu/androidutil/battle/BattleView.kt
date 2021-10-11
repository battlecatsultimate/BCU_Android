package com.mandarin.bcu.androidutil.battle

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mandarin.bcu.BattleSimulation
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.sound.PauseCountDown
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.supports.MediaPrepare
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.util.page.BBCtrl
import com.mandarin.bcu.util.page.BattleBox
import com.mandarin.bcu.util.page.BattleBox.BBPainter
import com.mandarin.bcu.util.page.BattleBox.OuterBox
import common.CommonStatic
import common.battle.BattleField
import common.battle.SBCtrl
import common.battle.entity.EEnemy
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.pack.UserProfile
import common.system.P
import common.util.Data
import common.util.anim.ImgCut
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.tan

@SuppressLint("ViewConstructor")
class BattleView(context: Context, field: BattleField?, type: Int, axis: Boolean, private val activity: Activity, cutout: Double) : View(context), BattleBox, OuterBox {
    @JvmField
    var painter: BBPainter = if (type == 0)
        BBPainter(this, field, this)
    else
        BBCtrl(this, field as SBCtrl?, this, StaticStore.dptopx(32f, context).toFloat(), cutout)

    var initialized = false
    var paused = false
    var battleEnd = false
    var musicChanged = false
    var spd = 0
    var velocity = 0.0
    private var upd = 0
    private val cv: CVGraphics
    private val updater: Updater

    var initPoint: P? = null
    var endPoint: P? = null
    var dragFrame = 0
    var isSliding = false
    var performed = false

    private var continued = false

    init {
        painter.dpi = StaticStore.dptopx(32f, context)
        CommonStatic.getConfig().ref = axis
        updater = Updater()

        val aa = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build()

        SoundHandler.SE = SoundPool.Builder().setMaxStreams(50).setAudioAttributes(aa).build()
        SoundHandler.ATK = SoundPool.Builder().setAudioAttributes(aa).build()
        SoundHandler.BASE = SoundPool.Builder().setAudioAttributes(aa).build()
        SoundHandler.TOUCH = SoundPool.Builder().setAudioAttributes(aa).build()
        SoundHandler.SPAWN_FAIL = SoundPool.Builder().setAudioAttributes(aa).build()

        //Preparing frequently used SE

        loadSE(SoundHandler.SE_SE, 25, 26)
        loadSE(SoundHandler.SE_ATK, 20, 21)
        loadSE(SoundHandler.SE_BASE, 22)
        loadSE(SoundHandler.SE_UI, 10, 15, 19, 27, 28)

        for (fs in painter.bf.sb.b.lu.fs) {
            for (f in fs) {
                if (f != null) {
                    if (f.anim.uni.img.height == f.anim.uni.img.width) {
                        val cut = ImgCut.newIns("./org/data/uni.imgcut")
                        f.anim.uni.setCut(cut)
                        f.anim.uni.img = f.anim.uni.img
                        f.anim.check()
                    } else {
                        f.anim.check()
                    }
                }
            }
        }

        if(painter.bf.sb.st.mus0 != null) {
            SoundHandler.lop = painter.bf.sb.st.loop0
        }

        for (e in painter.bf.sb.st.data.allEnemy)
            e.anim.check()

        updater.run()

        val cp = Paint()
        val bp = Paint()
        val gp = Paint()

        cv = CVGraphics(Canvas(), cp, bp, gp, true)

        isHapticFeedbackEnabled = false
    }

    public override fun onDraw(c: Canvas) {
        if (initialized) {
            if(continued) {
                continued = false
                battleEnd = false
                SoundHandler.battleEnd = false
            }

            cv.setCanvas(c)

            try {
                painter.draw(cv)
            } catch(e: Exception) {
                for (fs in painter.bf.sb.b.lu.fs) {
                    for (f in fs) {
                        if (f != null) {
                            if (f.anim.uni.img.height == f.anim.uni.img.width) {
                                val cut = ImgCut.newIns("./org/data/uni.imgcut")
                                f.anim.uni.setCut(cut)
                                f.anim.uni.img = f.anim.uni.img
                                f.anim.check()
                            } else {
                                f.anim.check()
                            }
                        }
                    }
                }

                for (es in painter.bf.sb.st.data.allEnemy)
                    es.anim.check()
            }

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

                painter.pos += velocity.toInt()
            }
        }
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(updater)
        painter.bf.sb.release()
    }

    override fun getSpeed(): Int {
        return spd
    }

    override fun callBack(o: Any) {}
    private inner class Updater : Runnable {
        override fun run() {
            if (!paused)
                invalidate()

            if (!musicChanged) {
                if (haveToChangeMusic()) {
                    SoundHandler.haveToChange = true
                    SoundHandler.MUSIC.stop()
                    SoundHandler.MUSIC.reset()
                    val f = StaticStore.getMusicDataSource(Identifier.get(painter.bf.sb.st.mus1))

                    if (f != null) {
                        this@BattleView.postDelayed({
                            if(battleEnd)
                                return@postDelayed

                            SoundHandler.MUSIC.setDataSource(f.absolutePath)
                            SoundHandler.MUSIC.prepareAsync()
                            SoundHandler.MUSIC.setOnPreparedListener(object : MediaPrepare() {
                                override fun prepare(mp: MediaPlayer?) {
                                    if(SoundHandler.musicPlay) {
                                        try {
                                            if(SoundHandler.timer != null && SoundHandler.timer?.isRunning == true) {
                                                SoundHandler.timer?.cancel()
                                            }

                                            if(painter.bf.sb.st.loop1 > 0 && painter.bf.sb.st.loop1 < SoundHandler.MUSIC.duration) {
                                                SoundHandler.timer = object : PauseCountDown((SoundHandler.MUSIC.duration-1).toLong(), (SoundHandler.MUSIC.duration-1).toLong(), true) {
                                                    override fun onFinish() {
                                                        SoundHandler.MUSIC.seekTo(painter.bf.sb.st.loop1.toInt(), true)

                                                        SoundHandler.timer = object : PauseCountDown((SoundHandler.MUSIC.duration-1).toLong()-painter.bf.sb.st.loop1, (SoundHandler.MUSIC.duration-1).toLong()-painter.bf.sb.st.loop1, true) {
                                                            override fun onFinish() {
                                                                SoundHandler.MUSIC.seekTo(painter.bf.sb.st.loop1.toInt(), true)

                                                                SoundHandler.timer?.create()
                                                            }

                                                            override fun onTick(millisUntilFinished: Long) {}

                                                        }

                                                        SoundHandler.timer?.create()
                                                    }

                                                    override fun onTick(millisUntilFinished: Long) {}
                                                }

                                                SoundHandler.timer?.create()
                                            } else {
                                                SoundHandler.timer = null
                                                SoundHandler.MUSIC.isLooping = true
                                            }

                                            SoundHandler.MUSIC.start()
                                        } catch(e: NullPointerException) {
                                            ErrorLogWriter.writeLog(e, StaticStore.upload, context)
                                        }
                                    }
                                }
                            })
                        }, Data.MUSIC_DELAY.toLong())
                    }
                    musicChanged = true
                }
            }

            if(isSliding) {
                dragFrame++
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
        if (painter.bf.sb.st.mush == 0 || painter.bf.sb.st.mush == 100)
            musicChanged = true

        return (painter.bf.sb.ebase.health.toFloat() / painter.bf.sb.ebase.maxH.toFloat() * 100).toInt() <painter.bf.sb.st.mush && painter.bf.sb.st.mush != 0 && painter.bf.sb.st.mush != 100
    }

    private fun resetSE() {
        for (i in SoundHandler.play.indices)
            SoundHandler.play[i] = false
    }

    private fun checkWin() {
        if (painter.bf.sb.ebase.health <= 0) {
            SoundHandler.MUSIC.stop()
            SoundHandler.MUSIC.reset()
            SoundHandler.MUSIC.isLooping = false

            if(SoundHandler.sePlay) {
                val f = StaticStore.getMusicDataSource(UserProfile.getBCData().musics[8])

                if (f != null) {
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
                }
            }

            battleEnd = true
            SoundHandler.battleEnd = true

            showBattleResult(true)
        }
    }

    private fun checkLose() {
        if (painter.bf.sb.ubase.health <= 0) {
            SoundHandler.MUSIC.stop()
            SoundHandler.MUSIC.reset()
            SoundHandler.MUSIC.isLooping = false

            if(SoundHandler.sePlay) {
                val f = StaticStore.getMusicDataSource(UserProfile.getBCData().musics[9])

                if (f != null) {
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
                }
            }

            battleEnd = true
            SoundHandler.battleEnd = true

            showBattleResult(false)
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

        if(bosses.isEmpty())
            return res

        var boss: EEnemy = bosses[0]

        for(b in bosses) {
            if(b.health > 0) {
                boss = b
                break
            }
        }

        res = if(boss.health <= 0)
            -1
        else
            round(boss.health.toDouble()/boss.maxH*100).toInt()

        return res
    }

    fun retry() {
        battleEnd = true

        val intent = Intent(activity,BattleSimulation::class.java)

        intent.putExtra("Data", JsonEncoder.encode(painter.bf.sb.st.id).toString())
        intent.putExtra("star",painter.bf.sb.est.star)
        intent.putExtra("item",painter.bf.sb.conf[0])
        intent.putExtra("size", painter.siz)
        intent.putExtra("pos", painter.pos)

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

    private fun continueBattle() {
        if(painter.bf is SBCtrl) {
            (painter.bf as SBCtrl).action.add(-4)

            continued = true
        }
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

    fun checkSlideUpDown() {
        val e = endPoint ?: return
        val i = initPoint ?: return

        if(battleEnd || painter.bf.sb.lineupChanging || painter.bf.sb.isOneLineup || painter.bf.sb.ubase.health == 0.toLong() || dragFrame == 0 || performed)
            return

        val minDistance = height * 0.15

        val dy = e.y - i.y
        val v = dy / dragFrame

        if(abs(dy) >= minDistance) {
            performed = true

            if(painter is BBCtrl) {
                if(v < 0) {
                    (painter as BBCtrl).perform(BBCtrl.ACTION_LINEUP_CHANGE_UP)
                } else {
                    (painter as BBCtrl).perform(BBCtrl.ACTION_LINEUP_CHANGE_DOWN)
                }
            } else {
                painter.bf.sb.lineupChanging = true
                painter.bf.sb.changeFrame = Data.LINEUP_CHANGE_TIME
                painter.bf.sb.changeDivision = painter.bf.sb.changeFrame / 2

                painter.bf.sb.goingUp = v < 0
            }
        }
    }

    fun isInSlideRange() : Boolean {
        val e = endPoint ?: return false
        val i = initPoint ?: return false

        val dx = e.x - i.x
        val dy = e.y - i.y

        return tan(Math.toRadians(50.0)) >= abs(dx) / abs(dy)
    }

    private fun loadSE(type: Int, vararg ind: Int) {
        for(i in ind) {
            val result = SoundHandler.load(type, i, play = false)

            if(result != -1) {
                SoundHandler.map[i] = result
            }
        }
    }

    private fun showBattleResult(win: Boolean) {
        val dialog = BottomSheetDialog(context)

        dialog.setContentView(R.layout.battle_result_bottom_dialog)

        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        dialog.setCanceledOnTouchOutside(false)

        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val text = dialog.findViewById<TextView>(R.id.resulttext) ?: return

        val primary = dialog.findViewById<Button>(R.id.resultprimary) ?: return
        val secondary = dialog.findViewById<Button>(R.id.resultsecondary) ?: return

        var dismissed = false

        if(win) {
            text.setText(R.string.battle_won)

            primary.visibility = GONE
            secondary.visibility = GONE
        } else {
            val bh = getBossHealth()

            if(bh == -1)
                text.setText(R.string.battle_lost)
            else {
                val t = activity.getText(R.string.battle_lost_boss).toString().replace("_", bh.toString())

                text.text = t
            }

            if(painter.bf.sb.st.non_con) {
                secondary.visibility = GONE
            } else {
                secondary.setOnClickListener {
                    dialog.dismiss()

                    continueBattle()
                }
            }

            primary.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    dialog.dismiss()

                    retry()
                }
            })
        }

        dialog.setOnDismissListener {
            dismissed = true
        }

        dialog.show()

        postDelayed({
            if(!dismissed)
                dialog.dismiss()
        }, 6000)
    }
}