package com.mandarin.bcu.androidutil.battle.coroutine

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.media.MediaPlayer
import android.view.*
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View.OnTouchListener
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.transition.TransitionManager
import androidx.transition.TransitionValues
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.BattleView
import com.mandarin.bcu.androidutil.battle.sound.PauseCountDown
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler.resetHandler
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer
import com.mandarin.bcu.androidutil.fakeandroid.AndroidKeys
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics.Companion.clear
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import com.mandarin.bcu.androidutil.supports.MediaPrepare
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.util.page.BBCtrl
import common.battle.BasisSet
import common.battle.SBCtrl
import common.battle.Treasure
import common.io.json.JsonDecoder
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.system.P
import common.util.stage.Stage
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.abs
import kotlin.math.ln

class BAdder(activity: Activity, private val data: Identifier<Stage>, private val star: Int, private val item: Int) : CoroutineTask<String>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    private var x = 0f
    private var y = 0f

    private var realFX = 0f
    private var previousX = 0f
    private var previousScale = 0f
    private var updateScale = false

    private val done = "3"

    override fun prepare() {
        val activity = weakReference.get() ?: return
        val fab: FloatingActionButton = activity.findViewById(R.id.battlepause)
        val fast: FloatingActionButton = activity.findViewById(R.id.battlefast)
        val slow: FloatingActionButton = activity.findViewById(R.id.battleslow)
        fab.hide()
        fast.hide()
        slow.hide()
    }

    override fun doSomething() {
        val activity = weakReference.get() ?: return

        Definer.define(activity, this::updateProg, this::updateText)

        publishProgress(done)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun progressUpdate(vararg data: String) {
        val activity = weakReference.get() ?: return
        val loadt = activity.findViewById<TextView>(R.id.status)
        when (data[0]) {
            StaticStore.TEXT -> loadt.text = data[1]
            StaticStore.PROG -> {
                val prog = activity.findViewById<ProgressBar>(R.id.prog)

                if(data[1].toInt() == -1) {
                    prog.isIndeterminate = true

                    return
                }

                prog.isIndeterminate = false
                prog.max = 10000
                prog.progress = data[1].toInt()
            }
            done -> {
                val layout = activity.findViewById<LinearLayout>(R.id.battlelayout)

                val stg = Identifier.get(this.data) ?: return

                val r = Random(System.currentTimeMillis())

                JsonDecoder.inject(JsonEncoder.encode(BasisSet.current().t()), Treasure::class.java, BasisSet.current().sele.t())

                val ctrl = SBCtrl(AndroidKeys(), stg, star, BasisSet.current().sele, intArrayOf(item), r.nextLong())

                val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

                val axis = shared.getBoolean("Axis", true)

                val view = BattleView(activity, ctrl, 1, axis,activity)

                view.initialized = false
                view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                view.id = R.id.battleView
                view.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

                layout.addView(view)

                loadt.setText(R.string.battle_prepare)

                val battleView: BattleView = activity.findViewById(R.id.battleView)
                val detector = ScaleGestureDetector(activity, ScaleListener(battleView))
                val actionButton: FloatingActionButton = activity.findViewById(R.id.battlepause)
                val play: FloatingActionButton = activity.findViewById(R.id.battleplay)
                val skipframe: FloatingActionButton = activity.findViewById(R.id.battlenextframe)
                val fast: FloatingActionButton = activity.findViewById(R.id.battlefast)
                val slow: FloatingActionButton = activity.findViewById(R.id.battleslow)
                val reveal: RelativeLayout = activity.findViewById(R.id.battleconfiglayout)
                val root: ConstraintLayout = activity.findViewById(R.id.battleroot)
                val prog = activity.findViewById<ProgressBar>(R.id.prog)

                prog.isIndeterminate = true

                val t = MaterialContainerTransform().apply {
                    startView = actionButton
                    endView = reveal

                    addTarget(endView!!)

                    setPathMotion(MaterialArcMotion())

                    scrimColor = Color.TRANSPARENT

                    fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
                }

                val t1 = MaterialContainerTransform().apply {
                    startView = reveal
                    endView = actionButton

                    addTarget(endView!!)

                    createAnimator(root, TransitionValues(reveal), TransitionValues(actionButton))

                    setPathMotion(MaterialArcMotion())

                    scrimColor = Color.TRANSPARENT

                    fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
                }


                skipframe.setOnClickListener {
                    battleView.painter.bf.update()
                    battleView.invalidate()
                }

                try {
                    actionButton.isExpanded = false
                } catch(e: Exception) {
                    activity.finish()
                }

                actionButton.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        TransitionManager.beginDelayedTransition(root, t)

                        actionButton.visibility = View.GONE
                        reveal.visibility = View.VISIBLE
                        battleView.paused = true

                        fast.hide()
                        slow.hide()
                    }
                })

                play.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        TransitionManager.beginDelayedTransition(root, t1)

                        actionButton.visibility = View.VISIBLE
                        reveal.visibility = View.GONE
                        battleView.paused = false

                        fast.show()
                        slow.show()
                    }
                })
                battleView.setOnTouchListener(object : OnTouchListener {
                    var preid = -1
                    var preX = 0

                    var velocity: VelocityTracker? = null

                    var horizontal = false
                    var vertical = false
                    var twoTouched = false

                    val six = StaticStore.dptopx(6f, activity)

                    @SuppressLint("Recycle")
                    override fun onTouch(v: View, event: MotionEvent): Boolean {
                        detector.onTouchEvent(event)

                        if (preid == -1)
                            preid = event.getPointerId(0)

                        val id = event.getPointerId(0)

                        val x2 = event.x.toInt()

                        val action = event.action

                        if (action == MotionEvent.ACTION_DOWN) {
                            updateScale = true
                            battleView.velocity = 0.0
                            x = event.x
                            y = event.y
                            if(event.pointerCount == 1) {
                                if(velocity == null)
                                    velocity = VelocityTracker.obtain()
                                else
                                    velocity?.clear()

                                if(battleView.initPoint == null)
                                    battleView.initPoint = P.newP(0.0, 0.0)

                                battleView.dragFrame = 1

                                battleView.initPoint?.x = x.toDouble()
                                battleView.initPoint?.y = y.toDouble()
                                battleView.isSliding = true

                                velocity?.addMovement(event)
                            } else if(event.pointerCount == 2)
                                twoTouched = true
                        } else if (action == MotionEvent.ACTION_UP) {
                            battleView.endPoint = null
                            battleView.initPoint = null
                            battleView.isSliding = false
                            battleView.dragFrame = 0
                            battleView.performed = false

                            if (battleView.painter.bf.sb.ubase.health > 0 && battleView.painter.bf.sb.ebase.health > 0) {
                                battleView.getPainter().click(Point(event.x.toInt(), event.y.toInt()), action)
                            }

                            if (!twoTouched && horizontal) {
                                battleView.velocity = (velocity?.xVelocity?.toDouble() ?: 0.0) * 0.5
                            }

                            horizontal = false
                            vertical = false
                            twoTouched = false

                            velocity?.recycle()
                            velocity = null
                        } else if (action == MotionEvent.ACTION_MOVE) {
                            if (event.pointerCount == 1 && id == preid) {
                                if(battleView.endPoint == null)
                                    battleView.endPoint = P.newP(0.0, 0.0)

                                battleView.endPoint?.x = event.x.toDouble()
                                battleView.endPoint?.y = event.y.toDouble()

                                velocity?.addMovement(event)
                                velocity?.computeCurrentVelocity(1000/30)

                                if(!twoTouched && !vertical && (horizontal || (!battleView.isInSlideRange() && abs(velocity?.xVelocity ?: 0f) > abs(velocity?.yVelocity ?: 0f) && abs(velocity?.xVelocity ?: 0f) > six))) {
                                    battleView.painter.pos += x2 - preX
                                    horizontal = true
                                } else {
                                    battleView.checkSlideUpDown()

                                    if(battleView.performed)
                                        vertical = true
                                }

                                if (battleView.paused) {
                                    battleView.invalidate()
                                }
                            }
                        } else if(action == MotionEvent.ACTION_CANCEL) {
                            velocity?.recycle()
                            velocity = null
                        }

                        preX = x2
                        preid = id

                        return false
                    }
                })

                battleView.isLongClickable = true

                battleView.setOnLongClickListener {
                    if (battleView.painter.bf.sb.ubase.health > 0 && battleView.painter.bf.sb.ebase.health > 0) {
                        battleView.getPainter().click(Point(x.toInt(), y.toInt()), BBCtrl.ACTION_LONG)
                    }

                    true
                }

                fast.setOnClickListener {
                    if (battleView.spd < (if (battleView.painter is BBCtrl) 5 else 7)) {
                        battleView.spd++
                        SoundHandler.speed++
                        battleView.painter.reset()
                    }
                }

                slow.setOnClickListener {
                    if (battleView.spd > -7) {
                        battleView.spd--
                        SoundHandler.speed--
                        battleView.painter.reset()
                    }
                }

                val exitbattle = activity.findViewById<Button>(R.id.battleexit)

                exitbattle.setOnClickListener {
                    if (battleView.painter.bf.sb.ebase.health > 0 && battleView.painter.bf.sb.ubase.health > 0 && shared.getBoolean("show", true)) {
                        val alert = AlertDialog.Builder(activity, R.style.AlertDialog)
                        val inflater = LayoutInflater.from(activity)
                        val layouts = inflater.inflate(R.layout.do_not_show_dialog, null)
                        val donotshow = layouts.findViewById<CheckBox>(R.id.donotshowcheck)
                        val cancel = layouts.findViewById<Button>(R.id.battlecancel)
                        val exit = layouts.findViewById<Button>(R.id.battledexit)

                        alert.setView(layouts)

                        donotshow.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                val editor = shared.edit()
                                editor.putBoolean("show", false)
                                editor.apply()
                            } else {
                                val editor = shared.edit()
                                editor.putBoolean("show", true)
                                editor.apply()
                            }
                        }

                        val dialog = alert.create()

                        cancel.setOnClickListener { dialog.cancel() }

                        exit.setOnClickListener {
                            P.stack.clear()

                            clear()

                            dialog.dismiss()

                            resetHandler()

                            if (SoundHandler.MUSIC.isInitialized) {
                                if (SoundHandler.MUSIC.isRunning) {
                                    SoundHandler.MUSIC.stop()
                                    SoundHandler.MUSIC.release()
                                } else {
                                    SoundHandler.MUSIC.release()
                                }
                            }

                            if(SoundHandler.timer != null) {
                                SoundHandler.timer?.cancel()
                                SoundHandler.timer = null
                            }

                            battleView.unload()

                            activity.finish()
                        }

                        dialog.show()
                    } else {
                        P.stack.clear()

                        clear()

                        SoundHandler.timer?.cancel()
                        SoundHandler.timer = null

                        battleView.unload()

                        resetHandler()

                        if (SoundHandler.MUSIC.isInitialized) {
                            if (SoundHandler.MUSIC.isRunning) {
                                SoundHandler.MUSIC.stop()
                                SoundHandler.MUSIC.release()
                            } else {
                                SoundHandler.MUSIC.release()
                            }
                        }
                        activity.finish()
                    }
                }
                val retry = activity.findViewById<Button>(R.id.battleretry)

                retry.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        if (battleView.painter.bf.sb.ebase.health > 0 && battleView.painter.bf.sb.ubase.health > 0 && shared.getBoolean("retry_show", true)) {
                            val alert = AlertDialog.Builder(activity, R.style.AlertDialog)

                            val inflater = LayoutInflater.from(activity)

                            val layouts = inflater.inflate(R.layout.do_not_show_dialog, null)

                            val donotshow = layouts.findViewById<CheckBox>(R.id.donotshowcheck)
                            val content = layouts.findViewById<TextView>(R.id.donotshowcontent)
                            val cancel = layouts.findViewById<Button>(R.id.battlecancel)
                            val exit = layouts.findViewById<Button>(R.id.battledexit)

                            exit.text = activity.getString(R.string.battle_retry)

                            content.text = activity.getString(R.string.battle_sure_retry)

                            alert.setView(layouts)

                            donotshow.setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked) {
                                    val editor = shared.edit()

                                    editor.putBoolean("retry_show", false)
                                    editor.apply()
                                } else {
                                    val editor = shared.edit()

                                    editor.putBoolean("retry_show", true)
                                    editor.apply()
                                }
                            }
                            val dialog = alert.create()
                            cancel.setOnClickListener { dialog.cancel() }
                            exit.setOnClickListener {
                                battleView.retry()
                                P.stack.clear()
                                clear()
                                dialog.dismiss()
                            }
                            dialog.show()
                        } else {
                            battleView.retry()
                        }
                    }

                })
                val mus = activity.findViewById<SwitchMaterial>(R.id.switchmus)
                val musvol = activity.findViewById<SeekBar>(R.id.seekmus)

                mus.isChecked = shared.getBoolean("music", true)
                musvol.isEnabled = shared.getBoolean("music", true)
                musvol.max = 99

                mus.setOnCheckedChangeListener { _, isChecked ->
                    mus.isClickable = false
                    mus.postDelayed({ mus.isClickable = true }, 1000)
                    if (isChecked) {
                        val editor = shared.edit()

                        editor.putBoolean("music", true)
                        editor.apply()

                        SoundHandler.musicPlay = true

                        musvol.isEnabled = true

                        if (!SoundHandler.MUSIC.isPlaying && SoundHandler.MUSIC.isInitialized) {
                            SoundHandler.MUSIC.start()
                        }

                        if(SoundHandler.timer == null && (battleView.painter.bf.sb.st.loop0 > 0 || battleView.painter.bf.sb.st.loop1 > 0)) {
                            val lop = if(SoundHandler.twoMusic && SoundHandler.Changed) {
                                SoundHandler.lop1
                            } else {
                                SoundHandler.lop
                            }

                            if(lop != 0L) {
                                SoundHandler.timer = object : PauseCountDown((SoundHandler.MUSIC.duration - 1).toLong(), (SoundHandler.MUSIC.duration - 1).toLong(), true) {
                                    override fun onFinish() {
                                        SoundHandler.MUSIC.seekTo(lop.toInt(), true)

                                        SoundHandler.timer = object : PauseCountDown((SoundHandler.MUSIC.duration - 1).toLong() - lop, (SoundHandler.MUSIC.duration - 1).toLong() - lop, true) {
                                            override fun onFinish() {

                                                SoundHandler.MUSIC.seekTo(lop.toInt(), true)

                                                SoundHandler.timer?.create()
                                            }

                                            override fun onTick(millisUntilFinished: Long) {
                                            }
                                        }

                                        SoundHandler.timer?.create()
                                    }

                                    override fun onTick(millisUntilFinished: Long) {
                                    }
                                }

                                SoundHandler.timer?.create()
                            }
                        } else if(SoundHandler.timer != null) {
                            SoundHandler.timer?.resume()
                        }
                    } else {
                        val editor = shared.edit()

                        editor.putBoolean("music", false)
                        editor.apply()

                        SoundHandler.musicPlay = false

                        musvol.isEnabled = false

                        if (SoundHandler.MUSIC.isInitialized && SoundHandler.MUSIC.isRunning) {
                            if (SoundHandler.MUSIC.isPlaying)
                                SoundHandler.MUSIC.pause()
                        }

                        SoundHandler.timer?.pause()
                    }
                }

                musvol.progress = shared.getInt("mus_vol", 99)

                musvol.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            if (progress >= 100 || progress < 0)
                                return

                            val editor = shared.edit()

                            editor.putInt("mus_vol", progress)
                            editor.apply()

                            SoundHandler.mu_vol = StaticStore.getVolumScaler(progress)

                            val log1 = (1 - ln(100 - progress.toDouble()) / ln(100.0)).toFloat()

                            if(!battleView.battleEnd) {
                                SoundHandler.MUSIC.setVolume(log1, log1)
                            }
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })

                val switchse = activity.findViewById<SwitchMaterial>(R.id.switchse)
                val seekse = activity.findViewById<SeekBar>(R.id.seekse)

                switchse.isChecked = shared.getBoolean("SE", true)

                seekse.isEnabled = shared.getBoolean("SE", true)
                seekse.max = 99
                seekse.progress = shared.getInt("se_vol", 99)

                switchse.setOnCheckedChangeListener { _, isChecked ->
                    switchse.isClickable = false
                    switchse.postDelayed({ switchse.isClickable = true }, 1000)
                    if (isChecked) {
                        val editor = shared.edit()

                        editor.putBoolean("SE", true)
                        editor.apply()

                        SoundHandler.sePlay = true
                        SoundHandler.se_vol = StaticStore.getVolumScaler((shared.getInt("se_vol", 99) * 0.85).toInt())

                        seekse.isEnabled = true
                    } else {
                        val editor = shared.edit()

                        editor.putBoolean("SE", false)
                        editor.apply()

                        SoundHandler.sePlay = false
                        SoundHandler.se_vol = 0f

                        seekse.isEnabled = false
                    }
                }

                seekse.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            if (progress >= 100 || progress < 0)
                                return

                            val editor = shared.edit()

                            editor.putInt("se_vol", progress)
                            editor.apply()

                            SoundHandler.se_vol = StaticStore.getVolumScaler((progress * 0.85).toInt())
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })

                val ui = activity.findViewById<SwitchMaterial>(R.id.switchui)
                val seekui = activity.findViewById<SeekBar>(R.id.seekui)

                ui.isChecked = SoundHandler.uiPlay
                seekui.isEnabled = SoundHandler.uiPlay
                seekui.max = 99
                seekui.progress = shared.getInt("ui_vol", 99)

                ui.setOnCheckedChangeListener { _, c ->
                    ui.isClickable = false
                    ui.postDelayed({ui.isClickable = true}, 1000)

                    val editor = shared.edit()

                    editor.putBoolean("UI", c)
                    editor.apply()

                    SoundHandler.uiPlay = c
                    SoundHandler.ui_vol = if(c) {
                        StaticStore.getVolumScaler((shared.getInt("ui_vol", 99) * 0.85).toInt())
                    } else {
                        0f
                    }

                    seekui.isEnabled = c
                }

                seekui.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            if (progress >= 100 || progress < 0)
                                return

                            val editor = shared.edit()

                            editor.putInt("ui_vol", progress)
                            editor.apply()

                            SoundHandler.ui_vol = StaticStore.getVolumScaler((progress * 0.85).toInt())
                        }
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {}

                    override fun onStopTrackingTouch(p0: SeekBar?) {}
                })

                SoundHandler.MUSIC = SoundPlayer()

                val preferences = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

                val muvol = (1 - ln(100 - preferences.getInt("mus_vol", 99).toDouble()) / ln(100.0)).toFloat()

                SoundHandler.MUSIC.setVolume(muvol, muvol)

                SoundHandler.twoMusic = battleView.painter.bf.sb.st.mush != 0 && battleView.painter.bf.sb.st.mush != 100 && battleView.painter.bf.sb.st.mus0 != battleView.painter.bf.sb.st.mus1

                SoundHandler.lop = battleView.painter.bf.sb.st.loop0

                if (SoundHandler.twoMusic) {
                    SoundHandler.lop1 = battleView.painter.bf.sb.st.loop1
                    SoundHandler.mu1 = StaticStore.getMusicDataSource(Identifier.get(battleView.painter.bf.sb.st.mus1))
                }

                if(battleView.painter.bf.sb.st.mus0 != null) {
                    val f = StaticStore.getMusicDataSource(Identifier.get(battleView.painter.bf.sb.st.mus0))

                    if (f != null) {
                        SoundHandler.MUSIC.setDataSource(f.absolutePath)

                        SoundHandler.MUSIC.prepareAsync()

                        SoundHandler.MUSIC.setOnPreparedListener(object : MediaPrepare() {
                            override fun prepare(mp: MediaPlayer?) {
                                if (SoundHandler.musicPlay) {
                                    if(battleView.painter.bf.sb.st.loop0 > 0) {
                                        if(battleView.painter.bf.sb.st.loop0 < SoundHandler.MUSIC.duration) {
                                            SoundHandler.timer = object : PauseCountDown((SoundHandler.MUSIC.duration-1).toLong(), (SoundHandler.MUSIC.duration-1).toLong(), true) {
                                                override fun onFinish() {
                                                    SoundHandler.MUSIC.seekTo(battleView.painter.bf.sb.st.loop0.toInt(), true)

                                                    SoundHandler.timer = object : PauseCountDown((SoundHandler.MUSIC.duration-1).toLong()-battleView.painter.bf.sb.st.loop0, (SoundHandler.MUSIC.duration-1).toLong()-battleView.painter.bf.sb.st.loop0, true) {
                                                        override fun onFinish() {
                                                            SoundHandler.MUSIC.seekTo(battleView.painter.bf.sb.st.loop0.toInt(), true)

                                                            SoundHandler.timer?.create()
                                                        }

                                                        override fun onTick(millisUntilFinished: Long) {}

                                                    }

                                                    SoundHandler.timer?.create()
                                                }

                                                override fun onTick(millisUntilFinished: Long) {}

                                            }

                                            SoundHandler.timer?.create()
                                        }
                                    } else {
                                        SoundHandler.timer = null
                                        SoundHandler.MUSIC.isLooping = true
                                    }

                                    SoundHandler.MUSIC.start()
                                }
                            }
                        })
                    }
                }
            }
        }
    }

    override fun finish() {
        val activity = weakReference.get() ?: return

        val battleView: BattleView = activity.findViewById(R.id.battleView)
        val prog = activity.findViewById<ProgressBar>(R.id.prog)
        val loadt = activity.findViewById<TextView>(R.id.status)
        val fab: FloatingActionButton = activity.findViewById(R.id.battlepause)
        val fast: FloatingActionButton = activity.findViewById(R.id.battlefast)
        val slow: FloatingActionButton = activity.findViewById(R.id.battleslow)

        setAppear(battleView)

        fab.show()

        fast.show()

        slow.show()

        prog.visibility = View.GONE
        loadt.visibility = View.GONE

        battleView.initialized = true
    }

    private fun updateText(info: String) {
        val ac = weakReference.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProg(p: Double) {
        publishProgress(StaticStore.PROG, (p * 10000.0).toInt().toString())
    }

    private fun setAppear(vararg views: View) {
        for (v in views) v.visibility = View.VISIBLE
    }

    private inner class ScaleListener(private val cView: BattleView) : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val firstDistance = realFX - previousX

            cView.painter.siz *= detector.scaleFactor.toDouble()
            cView.painter.regulate()

            val difference = firstDistance * (cView.painter.siz / previousScale - 1)

            cView.painter.pos = (previousX - difference).toInt()

            if (cView.paused) {
                cView.invalidate()
            }

            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            if(updateScale) {
                realFX = detector.focusX
                previousX = cView.painter.pos.toFloat()
                previousScale = cView.painter.siz.toFloat()

                updateScale = false
            }

            return super.onScaleBegin(detector)
        }
    }
}