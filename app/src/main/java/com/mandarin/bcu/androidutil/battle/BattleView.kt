package com.mandarin.bcu.androidutil.battle

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.media3.common.Player
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mandarin.bcu.BattleSimulation
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.BattleBox.BBPainter
import com.mandarin.bcu.androidutil.battle.BattleBox.OuterBox
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.supports.StageBitmapGenerator
import common.CommonStatic
import common.battle.BattleField
import common.battle.SBCtrl
import common.battle.entity.EEnemy
import common.io.json.JsonEncoder
import common.pack.UserProfile
import common.system.P
import common.util.Data
import common.util.anim.ImgCut
import common.util.lang.MultiLangCont
import common.util.stage.MapColc
import common.util.stage.Stage
import common.util.stage.info.DefStageInfo
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.tan
import kotlin.system.measureTimeMillis

@SuppressLint("ViewConstructor")
class BattleView(context: Context, field: BattleField?, type: Int, axis: Boolean, private val activity: BattleSimulation, cutout: Double, stageName: String, fontMode: StageBitmapGenerator.FONTMODE) : View(context),
    BattleBox, OuterBox {
    @JvmField
    var painter: BBPainter = if (type == 0)
        BBPainter(this, field, this)
    else
        BBCtrl(
            this,
            field as SBCtrl?,
            this,
            StaticStore.dptopx(32f, context).toFloat(),
            cutout
        )

    var initialized = false

    var paused = false
    var battleEnd = false
    var musicChanged = false

    var spd = 0

    var velocity = 0.0
    var scaleMode = false

    var initPoint: P? = null
    var endPoint: P? = null

    var dragFrame = 0

    var isSliding = false

    var performed = false

    private val cv: CVGraphics
    private val updater: Updater

    private var continued = false
    private var upd = 0

    init {
        var default = false

        val stgImage = if(stageName.isBlank())
            null
        else {
            val generator = StageBitmapGenerator(context, fontMode, stageName)

            default = generator.default

            generator.generateTextImage()
        }

        if(CommonStatic.getConfig().stageName)
            painter.stageImage = stgImage

        CommonStatic.getConfig().performanceModeAnimation = CommonStatic.getConfig().performanceModeBattle

        painter.dpi = StaticStore.dptopx(32f, context)
        painter.stmImageOffset = StaticStore.dptopx(52f, context)
        painter.stmImageYOffset = StaticStore.dptopx(if(default) 6f else 9f, context)
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

        var animationError = false

        for (fs in painter.bf.sb.b.lu.fs) {
            for (f in fs) {
                f ?: continue

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

        if(painter.bf.sb.st.mus0 != null) {
            SoundHandler.lop = painter.bf.sb.st.mus0.get()?.loop ?: 0
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

    override fun onDraw(c: Canvas) {
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

                painter.bf.sb.pos += velocity.toInt()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(updater)
    }

    override fun getSpeed(): Int {
        return spd
    }

    override fun callBack(o: Any) {}
    private inner class Updater : Runnable {
        var pauseTime = 1000L / 30L

        override fun run() {
            val time = measureTimeMillis {
                if (!paused)
                    invalidate()

                if (!musicChanged) {
                    if (haveToChangeMusic()) {
                        if (painter.bf.sb.st.mus1 != null) {
                            musicChanged = true
                        } else {
                            if (SoundHandler.MUSIC.isPlaying)
                                SoundHandler.MUSIC.pause()

                            this@BattleView.postDelayed({
                                if(battleEnd)
                                    return@postDelayed

                                SoundHandler.setBGM(painter.bf.sb.st.mus1, onReady = {
                                    if (!activity.paused && SoundHandler.musicPlay) {
                                        SoundHandler.MUSIC.play()
                                    } else {
                                        SoundHandler.MUSIC.pause()
                                    }
                                })
                            }, Data.MUSIC_DELAY.toLong())

                            musicChanged = true
                        }
                    }
                }

                if(isSliding) {
                    dragFrame++
                }

                if (!battleEnd) {
                    checkWin()
                    checkLose()
                }
            }

            val targetTime = if (CommonStatic.getConfig().performanceModeBattle) {
                1000 / 60.0
            } else {
                1000 / 30.0
            }

            pauseTime = max(0, (pauseTime * 0.5 + (targetTime - time) * 0.5).toLong())

            postDelayed(this, pauseTime)
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

        SoundHandler.playCustom.clear()
    }

    private fun checkWin() {
        if (painter.bf.sb.ebase.health <= 0) {
            SoundHandler.MUSIC.stop()
            SoundHandler.MUSIC.seekTo(0)
            SoundHandler.MUSIC.repeatMode = Player.REPEAT_MODE_OFF

            if(SoundHandler.sePlay) {
                SoundHandler.setBGM(UserProfile.getBCData().musics[8].id)
            }

            battleEnd = true
            SoundHandler.battleEnd = true

            showBattleResult(true)
        }
    }

    private fun checkLose() {
        if (painter.bf.sb.ubase.health <= 0) {
            SoundHandler.MUSIC.stop()
            SoundHandler.MUSIC.seekTo(0)
            SoundHandler.MUSIC.repeatMode = Player.REPEAT_MODE_OFF

            if(SoundHandler.sePlay) {
                SoundHandler.setBGM(UserProfile.getBCData().musics[9].id)
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

                if ((entity as EEnemy).mark > 0) {
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

    fun reopenStage(st: Stage, ex: Boolean) {
        battleEnd = true

        val intent = Intent(activity,BattleSimulation::class.java)

        intent.putExtra("Data", JsonEncoder.encode(st.id).toString())
        intent.putExtra("star",painter.bf.sb.est.star)
        intent.putExtra("item",if(ex) 0 else painter.bf.sb.conf[0])
        intent.putExtra("size", painter.bf.sb.siz)
        intent.putExtra("pos", painter.bf.sb.pos)

        if(SoundHandler.MUSIC.isPlaying && SoundHandler.MUSIC.currentMediaItem != null) {
            SoundHandler.MUSIC.stop()
        }

        SoundHandler.resetHandler()

        painter.bf.sb.release()

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

        painter.bf.sb.release()
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
                    (painter as BBCtrl).perform(
                        BBCtrl.ACTION_LINEUP_CHANGE_UP)
                } else {
                    (painter as BBCtrl).perform(
                        BBCtrl.ACTION_LINEUP_CHANGE_DOWN)
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
        val st = painter.bf.sb.st

        if(win && CommonStatic.getConfig().exContinuation && st.info != null && (st.info.exConnection() || st.info.exStages != null)) {
            if(CommonStatic.getConfig().realEx) {
                val stage = pickOneEXStage()

                if(stage != null) {
                    val dialog = Dialog(context)

                    dialog.setContentView(R.layout.battle_ex_picked_popup)

                    dialog.setCancelable(true)

                    val cont = dialog.findViewById<Button>(R.id.excontinue)
                    val cancel = dialog.findViewById<Button>(R.id.excancel)
                    val content = dialog.findViewById<TextView>(R.id.exdesc)

                    content.text = context.getString(R.string.ex_picked).replace("_", getMapStageName(stage))

                    cont.setOnClickListener(object : SingleClick() {
                        override fun onSingleClick(v: View?) {
                            reopenStage(stage, true)

                            dialog.dismiss()
                        }
                    })

                    cancel.setOnClickListener(object : SingleClick() {
                        override fun onSingleClick(v: View?) {
                            dialog.dismiss()
                        }
                    })

                    dialog.show()

                    return
                }
            } else {
                val dialog = Dialog(context)

                dialog.setContentView(R.layout.battle_ex_continue_popup)

                dialog.setCancelable(true)

                val exGroup = dialog.findViewById<RadioGroup>(R.id.exgroup)
                val cont = dialog.findViewById<Button>(R.id.excontinue)
                val cancel = dialog.findViewById<Button>(R.id.excancel)

                val stageData = getEXStages(st)

                for(s in stageData.indices) {
                    val radioButton = RadioButton(context)

                    radioButton.id = R.id.exstage + stageData[s].hashCode()

                    radioButton.setTextColor(StaticStore.getAttributeColor(context, R.attr.TextPrimary))

                    radioButton.text = getMapStageName(stageData[s])

                    exGroup.addView(radioButton)

                    if(s == 0)
                        radioButton.isChecked = true
                }

                cont.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        val ind = exGroup.indexOfChild(exGroup.findViewById(exGroup.checkedRadioButtonId))

                        if(ind >= 0 && ind < stageData.size) {
                            reopenStage(stageData[ind], true)

                            dialog.dismiss()
                        } else {
                            dialog.dismiss()
                        }
                    }
                })

                cancel.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        dialog.dismiss()
                    }
                })

                dialog.show()

                return
            }
        }

        if(!StaticStore.showResult)
            return

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

                    reopenStage(painter.bf.sb.st, false)
                }
            })
        }

        dialog.setOnDismissListener {
            dismissed = true
        }

        dialog.show()

        postDelayed({
            if(!dismissed && !activity.isDestroyed)
                dialog.dismiss()
        }, 6000)
    }

    private fun getEXStages(st: Stage) : List<Stage> {
        val res = ArrayList<Stage>()

        if(st.info.exConnection()) {
            val inf = st.info as DefStageInfo
            val min = inf.exStageIDMin
            val max = inf.exStageIDMax

            val map = MapColc.DefMapColc.getMap(4000 + inf.exMapID) ?: return res

            for(i in min..max) {
                val stg = map.list.list[i] ?: return res

                res.add(stg)
            }
        } else {
            res.addAll(st.info.exStages)
        }

        return res
    }

    private fun getMapStageName(st: Stage) : String {
        var mapName = MultiLangCont.get(st.cont)

        if(mapName == null || mapName.isBlank()) {
            mapName = st.cont.names.toString()

            if(mapName.isBlank())
                mapName = Data.hex(400000 + st.cont.id.id)
        }

        var stageName = MultiLangCont.get(st)

        if(stageName == null || stageName.isBlank()) {
            stageName = st.names.toString()

            if(stageName.isBlank())
                stageName = Data.trio(st.id.id)
        }

        return "$mapName - $stageName"
    }

    private fun pickOneEXStage() : Stage? {
        val chance = painter.bf.sb.r.nextDouble() * 100.0

        val st = painter.bf.sb.st

        if(st.info.exConnection()) {
            val inf = st.info as DefStageInfo
            val min = inf.exStageIDMin
            val max = inf.exStageIDMax

            val map = MapColc.DefMapColc.getMap(4000 + inf.exMapID) ?: return null

            for(i in 0..max - min) {
                if(chance < inf.exChance * 1.0 * (i + 1) / (max - min + 1))
                    return map.list[i]
            }
        } else if(st.info.exStages != null) {
            val inf = st.info

            var sum = 0f

            for(i in inf.exStages.indices) {
                sum += inf.exChances[i]

                if(chance < sum)
                    return inf.exStages[i]
            }
        }

        return null
    }
}