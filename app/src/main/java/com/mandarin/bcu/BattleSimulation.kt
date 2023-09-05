package com.mandarin.bcu

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import androidx.transition.TransitionValues
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.BBCtrl
import com.mandarin.bcu.androidutil.battle.BattleView
import com.mandarin.bcu.androidutil.battle.sound.PauseCountDown
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.fakeandroid.AndroidKeys
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.supports.StageBitmapGenerator
import common.CommonStatic
import common.battle.BasisLU
import common.battle.BasisSet
import common.battle.SBCtrl
import common.battle.Treasure
import common.io.json.JsonDecoder
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.system.P
import common.util.lang.MultiLangCont
import common.util.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

class BattleSimulation : AppCompatActivity() {
    var paused = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences("configuration", Context.MODE_PRIVATE)
        val ed: Editor

        if (!shared.contains("initial")) {
            ed = shared.edit()
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.apply()
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_designNight)
            } else {
                setTheme(R.style.AppTheme_designDay)
            }
        }

        LeakCanaryManager.initCanary(shared, application)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_battle_simulation)

        SoundHandler.initializePlayer(this@BattleSimulation, directPlay = SoundHandler.musicPlay)

        val att = window.attributes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            att.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        @Suppress("DEPRECATION")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController

            if(controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        SoundHandler.inBattle = true

        val intent = intent
        val bundle = intent.extras

        if (bundle != null) {
            val data = StaticStore.transformIdentifier<Stage>(bundle.getString("Data")) ?: return
            val star = bundle.getInt("star")
            val item = bundle.getInt("item")
            val siz = bundle.getDouble("size", 1.0)
            val pos = bundle.getInt("pos", 0)
            
            lifecycleScope.launch {
                //Prepare
                val pauseButton= findViewById<FloatingActionButton>(R.id.battlepause)
                val fast = findViewById<FloatingActionButton>(R.id.battlefast)
                val slow = findViewById<FloatingActionButton>(R.id.battleslow)
                val progress = findViewById<ProgressBar>(R.id.prog)
                val st = findViewById<TextView>(R.id.status)
                val play: FloatingActionButton = findViewById(R.id.battleplay)
                val skipFrame: FloatingActionButton = findViewById(R.id.battlenextframe)
                val configPanel = findViewById<RelativeLayout>(R.id.battleconfiglayout)
                val root = findViewById<ConstraintLayout>(R.id.battleroot)
                val row = findViewById<SwitchMaterial>(R.id.switchrow)
                val exitBattle = findViewById<Button>(R.id.battleexit)
                val switchSFX = findViewById<SwitchMaterial>(R.id.switchse)
                val volumeSFX = findViewById<SeekBar>(R.id.seekse)
                val switchMusic = findViewById<SwitchMaterial>(R.id.switchmus)
                val volumeMusic = findViewById<SeekBar>(R.id.seekmus)
                val switchUI = findViewById<SwitchMaterial>(R.id.switchui)
                val volumeUI = findViewById<SeekBar>(R.id.seekui)
                val layout = findViewById<LinearLayout>(R.id.battlelayout)
                val retry = findViewById<Button>(R.id.battleretry)
                
                pauseButton.hide()
                fast.hide()
                slow.hide()
                
                progress.isIndeterminate = true
                
                //Load Data
                withContext(Dispatchers.IO) {
                    Definer.define(this@BattleSimulation, { _ -> }, { t -> runOnUiThread { st.text = t }})
                }
                
                //Load UI
                val stg = Identifier.get(data) ?: return@launch

                val r = Random(System.currentTimeMillis())

                var stgName = MultiLangCont.get(stg)
                var locale = MultiLangCont.getGrabbedLocale(stg)

                if(stgName == null || stgName.isBlank()) {
                    stgName = stg.names.toString()
                    locale = stg.names.grabbedLocale
                }

                if(stgName.isBlank())
                    stgName = ""

                val fontMod = when(locale) {
                    0 -> StageBitmapGenerator.FONTMODE.EN
                    else -> StageBitmapGenerator.FONTMODE.GLOBAL
                }

                JsonDecoder.inject(JsonEncoder.encode(BasisSet.current().t()), Treasure::class.java, BasisSet.current().sele.t())

                val lu = BasisSet.current().sele.copy()

                val restricted = restrictLevel(stg, lu)

                if(restricted) {
                    StaticStore.showShortMessage(this@BattleSimulation, R.string.battle_restricted)
                }

                if(CommonStatic.getConfig().realLevel)
                    lu.performRealisticLeveling()

                val ctrl = SBCtrl(AndroidKeys(), stg, star, lu, intArrayOf(item), r.nextLong())

                val axis = shared.getBoolean("Axis", true)

                val battleView = BattleView(this@BattleSimulation, ctrl, 1, axis, this@BattleSimulation, getCutoutWidth(this@BattleSimulation), stgName, fontMod).apply {
                    painter.cutout = getCutoutWidth(this@BattleSimulation)

                    initialized = false
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    id = R.id.battleView
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

                    post {
                        painter.preSiz = siz
                        painter.prePos = pos
                    }

                    StaticStore.setDisappear(this)
                }

                layout.addView(battleView)

                val detector = ScaleGestureDetector(this@BattleSimulation, ScaleListener(battleView))

                st.setText(R.string.battle_prepare)

                val transitionPause = MaterialContainerTransform().apply {
                    startView = pauseButton
                    endView = configPanel

                    addTarget(configPanel)

                    setPathMotion(MaterialArcMotion())

                    scrimColor = Color.TRANSPARENT

                    fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
                }

                val transitionContinue = MaterialContainerTransform().apply {
                    startView = configPanel
                    endView = pauseButton

                    addTarget(endView!!)

                    createAnimator(root, TransitionValues(configPanel), TransitionValues(pauseButton))

                    setPathMotion(MaterialArcMotion())

                    scrimColor = Color.TRANSPARENT

                    fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
                }

                skipFrame.setOnClickListener {
                    battleView.painter.bf.update()
                    battleView.invalidate()
                }

                try {
                    pauseButton.isExpanded = false
                } catch(e: Exception) {
                    finish()
                }

                pauseButton.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        TransitionManager.beginDelayedTransition(root, transitionPause)

                        pauseButton.visibility = View.GONE
                        configPanel.visibility = View.VISIBLE
                        battleView.paused = true

                        fast.hide()
                        slow.hide()
                    }
                })

                play.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        TransitionManager.beginDelayedTransition(root, transitionContinue)

                        pauseButton.visibility = View.VISIBLE
                        configPanel.visibility = View.GONE
                        battleView.paused = false

                        battleView.invalidate()

                        fast.show()
                        slow.show()
                    }
                })

                var x = 0f
                var y = 0f

                battleView.setOnTouchListener(object : View.OnTouchListener {
                    private var previousID = -1
                    private var preX = 0

                    private var velocity: VelocityTracker? = null

                    private var horizontal = false
                    private var vertical = false

                    private var twoTouched = false

                    private val six = StaticStore.dptopx(6f, this@BattleSimulation)

                    @SuppressLint("Recycle")
                    override fun onTouch(v: View, event: MotionEvent): Boolean {
                        if(event.pointerCount > 1)
                            detector.onTouchEvent(event)

                        if (previousID == -1)
                            previousID = event.getPointerId(0)

                        val id = event.getPointerId(0)

                        val x2 = event.x.toInt()

                        val action = event.action

                        if (action == MotionEvent.ACTION_DOWN) {
                            battleView.scaleMode = true

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

                                battleView.initPoint?.x = event.x.toDouble()
                                battleView.initPoint?.y = event.y.toDouble()

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
                            if (event.pointerCount == 1 && id == previousID) {
                                if(battleView.endPoint == null)
                                    battleView.endPoint = P.newP(0.0, 0.0)

                                battleView.endPoint?.x = event.x.toDouble()
                                battleView.endPoint?.y = event.y.toDouble()

                                velocity?.addMovement(event)
                                velocity?.computeCurrentVelocity(1000/30)

                                if(!twoTouched && !vertical && (horizontal || (!battleView.isInSlideRange() && abs(velocity?.xVelocity ?: 0f) > abs(velocity?.yVelocity ?: 0f) && abs(velocity?.xVelocity ?: 0f) > six))) {
                                    battleView.painter.bf.sb.pos += x2 - preX
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
                        previousID = id

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

                exitBattle.setOnClickListener {
                    if (battleView.painter.bf.sb.ebase.health > 0 && battleView.painter.bf.sb.ubase.health > 0 && shared.getBoolean("show", true)) {
                        val alert = AlertDialog.Builder(this@BattleSimulation, R.style.AlertDialog)
                        val inflater = LayoutInflater.from(this@BattleSimulation)
                        val layouts = inflater.inflate(R.layout.do_not_show_dialog, null)
                        val stopShow = layouts.findViewById<CheckBox>(R.id.donotshowcheck)
                        val cancel = layouts.findViewById<Button>(R.id.battlecancel)
                        val exit = layouts.findViewById<Button>(R.id.battledexit)

                        alert.setView(layouts)

                        stopShow.setOnCheckedChangeListener { _, isChecked ->
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

                            CVGraphics.clear()

                            dialog.dismiss()

                            SoundHandler.resetHandler()

                            if (SoundHandler.MUSIC.currentMediaItem != null) {
                                if (SoundHandler.MUSIC.isPlaying) {
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

                            battleView.painter.bf.sb.release()

                            finish()
                        }

                        dialog.show()
                    } else {
                        P.stack.clear()

                        CVGraphics.clear()

                        SoundHandler.timer?.cancel()
                        SoundHandler.timer = null

                        battleView.unload()

                        SoundHandler.resetHandler()

                        if (SoundHandler.MUSIC.currentMediaItem != null) {
                            if (SoundHandler.MUSIC.isPlaying) {
                                SoundHandler.MUSIC.stop()
                                SoundHandler.MUSIC.release()
                            } else {
                                SoundHandler.MUSIC.release()
                            }
                        }

                        battleView.painter.bf.sb.release()

                        finish()
                    }
                }

                retry.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        if (battleView.painter.bf.sb.ebase.health > 0 && battleView.painter.bf.sb.ubase.health > 0 && shared.getBoolean("retry_show", true)) {
                            val alert = AlertDialog.Builder(this@BattleSimulation, R.style.AlertDialog)

                            val inflater = LayoutInflater.from(this@BattleSimulation)

                            val layouts = inflater.inflate(R.layout.do_not_show_dialog, null)

                            val stopShow = layouts.findViewById<CheckBox>(R.id.donotshowcheck)
                            val content = layouts.findViewById<TextView>(R.id.donotshowcontent)
                            val cancel = layouts.findViewById<Button>(R.id.battlecancel)
                            val exit = layouts.findViewById<Button>(R.id.battledexit)

                            exit.text = getString(R.string.battle_retry)

                            content.text = getString(R.string.battle_sure_retry)

                            alert.setView(layouts)

                            stopShow.setOnCheckedChangeListener { _, isChecked ->
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
                                battleView.reopenStage(battleView.painter.bf.sb.st, false)
                                P.stack.clear()
                                CVGraphics.clear()
                                dialog.dismiss()
                            }
                            dialog.show()
                        } else {
                            battleView.reopenStage(battleView.painter.bf.sb.st, false)
                        }
                    }

                })

                switchMusic.isChecked = shared.getBoolean("music", true)

                volumeMusic.isEnabled = shared.getBoolean("music", true)
                volumeMusic.max = 99

                switchMusic.setOnCheckedChangeListener { _, isChecked ->
                    switchMusic.isClickable = false
                    switchMusic.postDelayed({ switchMusic.isClickable = true }, 1000)

                    if (isChecked) {
                        val editor = shared.edit()

                        editor.putBoolean("music", true)
                        editor.apply()

                        SoundHandler.musicPlay = true

                        volumeMusic.isEnabled = true

                        if (!SoundHandler.MUSIC.isPlaying) {
                            SoundHandler.MUSIC.play()
                        }

                        val firstLoop = battleView.painter.bf.sb.st.mus0?.get()?.loop ?: 0
                        val secondLoop = battleView.painter.bf.sb.st.mus1?.get()?.loop ?: 0

                        if(SoundHandler.timer == null && (firstLoop > 0 || secondLoop > 0)) {
                            val lop = if(SoundHandler.twoMusic && SoundHandler.Changed) {
                                SoundHandler.lop1
                            } else {
                                SoundHandler.lop
                            }

                            if(lop != 0L) {
                                SoundHandler.timer = object : PauseCountDown(SoundHandler.MUSIC.duration - 1, SoundHandler.MUSIC.duration - 1, true) {
                                    override fun onFinish() {
                                        SoundHandler.MUSIC.seekTo(lop)

                                        SoundHandler.timer = object : PauseCountDown(SoundHandler.MUSIC.duration - 1 - lop, SoundHandler.MUSIC.duration - 1 - lop, true) {
                                            override fun onFinish() {

                                                SoundHandler.MUSIC.seekTo(lop)

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

                        volumeMusic.isEnabled = false

                        if (SoundHandler.MUSIC.isPlaying) {
                            SoundHandler.MUSIC.pause()
                        }

                        SoundHandler.timer?.pause()
                    }
                }

                volumeMusic.progress = shared.getInt("mus_vol", 99)

                volumeMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            if (progress >= 100 || progress < 0)
                                return

                            val editor = shared.edit()

                            editor.putInt("mus_vol", progress)
                            editor.apply()

                            SoundHandler.mu_vol = 0.01f + progress / 100f

                            if(!battleView.battleEnd) {
                                SoundHandler.MUSIC.volume = SoundHandler.mu_vol
                            }
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })

                switchSFX.isChecked = shared.getBoolean("SE", true)

                volumeSFX.isEnabled = shared.getBoolean("SE", true)
                volumeSFX.max = 99
                volumeSFX.progress = shared.getInt("se_vol", 99)

                switchSFX.setOnCheckedChangeListener { _, isChecked ->
                    switchSFX.isClickable = false
                    switchSFX.postDelayed({ switchSFX.isClickable = true }, 1000)
                    if (isChecked) {
                        val editor = shared.edit()

                        editor.putBoolean("SE", true)
                        editor.apply()

                        SoundHandler.sePlay = true
                        SoundHandler.se_vol = (0.01f + (shared.getInt("se_vol", 99) / 100f)) * 0.85f

                        volumeSFX.isEnabled = true
                    } else {
                        val editor = shared.edit()

                        editor.putBoolean("SE", false)
                        editor.apply()

                        SoundHandler.sePlay = false
                        SoundHandler.se_vol = 0f

                        volumeSFX.isEnabled = false
                    }
                }

                volumeSFX.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            if (progress >= 100 || progress < 0)
                                return

                            val editor = shared.edit()

                            editor.putInt("se_vol", progress)
                            editor.apply()

                            SoundHandler.se_vol = (0.01f + progress / 100f) * 0.85f
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })

                switchUI.isChecked = SoundHandler.uiPlay

                volumeUI.isEnabled = SoundHandler.uiPlay
                volumeUI.max = 99
                volumeUI.progress = shared.getInt("ui_vol", 99)

                switchUI.setOnCheckedChangeListener { _, c ->
                    switchUI.isClickable = false
                    switchUI.postDelayed({switchUI.isClickable = true}, 1000)

                    val editor = shared.edit()

                    editor.putBoolean("UI", c)
                    editor.apply()

                    SoundHandler.uiPlay = c

                    SoundHandler.ui_vol = if(c) {
                        (0.01f + shared.getInt("ui_vol", 99) / 100f) * 0.85f
                    } else {
                        0f
                    }

                    volumeUI.isEnabled = c
                }

                volumeUI.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            if (progress >= 100 || progress < 0)
                                return

                            val editor = shared.edit()

                            editor.putInt("ui_vol", progress)
                            editor.apply()

                            SoundHandler.ui_vol = (0.01f + progress / 100f) * 0.85f
                        }
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {}

                    override fun onStopTrackingTouch(p0: SeekBar?) {}
                })

                SoundHandler.MUSIC.volume = 0.01f + shared.getInt("mus_vol", 99) / 100f

                SoundHandler.twoMusic = battleView.painter.bf.sb.st.mush != 0 && battleView.painter.bf.sb.st.mush != 100 && battleView.painter.bf.sb.st.mus0 != battleView.painter.bf.sb.st.mus1

                SoundHandler.lop = battleView.painter.bf.sb.st.mus0?.get()?.loop ?: 0

                if (SoundHandler.twoMusic) {
                    SoundHandler.lop1 = (battleView.painter.bf.sb.st.mus1?.get()?.loop ?: 0)
                    SoundHandler.mu1 = StaticStore.getMusicDataSource(Identifier.get(battleView.painter.bf.sb.st.mus1))
                }

                if(battleView.painter.bf.sb.st.mus0 != null) {
                    SoundHandler.setBGM(battleView.painter.bf.sb.st.mus0)
                }

                row.isChecked = shared.getBoolean("rowlayout", true)

                row.text = if(CommonStatic.getConfig().twoRow)
                    getString(R.string.battle_tworow)
                else
                    getString(R.string.battle_onerow)

                row.setOnCheckedChangeListener { _, isChecked ->
                    val editor = shared.edit()

                    CommonStatic.getConfig().twoRow = isChecked
                    editor.putBoolean("rowlayout", isChecked)

                    editor.apply()

                    row.text = if (isChecked)
                        getText(R.string.battle_tworow)
                    else
                        getText(R.string.battle_onerow)

                    battleView.invalidate()
                }

                onBackPressedDispatcher.addCallback(this@BattleSimulation, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        exitBattle.performClick()
                    }
                })

                StaticStore.setAppear(battleView)

                pauseButton.show()
                fast.show()
                slow.show()

                StaticStore.setDisappear(st, progress)

                battleView.initialized = true
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
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

    override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
    }

    override fun onPause() {
        super.onPause()

        paused = true

        if (SoundHandler.MUSIC.isPlaying && SoundHandler.MUSIC.currentMediaItem != null && SoundHandler.musicPlay) {
            SoundHandler.MUSIC.pause()
        }

        SoundHandler.timer?.pause()
    }

    override fun onResume() {
        super.onResume()

        paused = false

        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        if (SoundHandler.isMusicPossible && !SoundHandler.MUSIC.isPlaying && SoundHandler.MUSIC.currentMediaItem != null && SoundHandler.musicPlay) {
            SoundHandler.MUSIC.play()

            SoundHandler.timer?.resume()
        }
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
    }

    @Suppress("DEPRECATION")
    private fun getCutoutWidth(ac: Activity) : Double {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val m = ac.windowManager.currentWindowMetrics

            val cutout = m.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.displayCutout())

            val result = cutout.left.coerceAtLeast(cutout.right).toDouble()

            if(result == 0.0) {
                val cut = m.windowInsets.displayCutout ?: return result

                return cut.boundingRectLeft.width().coerceAtLeast(cut.boundingRectRight.width()).toDouble()
            } else {
                result
            }
        } else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val cutout = ac.windowManager.defaultDisplay.cutout ?: return 0.0

                return cutout.boundingRectLeft.width().coerceAtLeast(cutout.boundingRectRight.width()).toDouble()
            } else {
                0.0
            }
        }
    }

    private fun restrictLevel(st: Stage, lu: BasisLU) : Boolean {
        var changed = false

        if(st.lim != null && st.lim.lvr != null) {
            println(st.lim.lvr.all.contentToString())
            st.lim.lvr.rares.forEach {
                println(it.contentToString())
            }

            for(forms in lu.lu.fs) {
                for(form in forms) {
                    form ?: continue

                    val level = lu.lu.map[form.unit.id] ?: continue

                    var temp = level.lv

                    level.setLevel(min(level.lv, st.lim.lvr.all[0]))

                    if(!changed && temp != level.lv)
                        changed = true

                    temp = level.plusLv

                    level.setPlusLevel(min(level.plusLv, st.lim.lvr.all[1]))

                    if(!changed && temp != level.plusLv)
                        changed = true

                    for(i in 2 until st.lim.lvr.all.size) {
                        if (i - 2 >= level.talents.size)
                            break

                        temp = level.talents[i - 2]

                        level.talents[i - 2] = min(level.talents[i - 2], st.lim.lvr.all[i])

                        if(!changed && temp != level.talents[i - 2])
                            changed = true
                    }

                    st.lim.lvr.rares.forEachIndexed { index, levels ->
                        if (form.unit.rarity == index) {
                            temp = level.lv

                            level.setLevel(min(level.lv, levels[0]))

                            if(!changed && temp != level.lv)
                                changed = true

                            temp = level.plusLv

                            level.setPlusLevel(min(level.plusLv, levels[1]))

                            if(!changed && temp != level.plusLv)
                                changed = true

                            for(i in 2 until levels.size) {
                                if (i - 2 >= level.talents.size)
                                    break

                                temp = level.talents[i - 2]

                                level.talents[i - 2] = min(level.talents[i - 2], levels[i])

                                if(!changed && temp != level.talents[i - 2])
                                    changed = true
                            }
                        }
                    }

                    println("$form = ${level.lv} + ${level.plusLv} | ${level.talents.contentToString()}")
                }
            }
        }

        return changed
    }

    inner class ScaleListener(private val cView: BattleView) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var realFX = 0f
        private var previousX = 0f
        private var previousScale = 0f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val firstDistance = realFX - previousX

            cView.painter.bf.sb.siz *= detector.scaleFactor.toDouble()
            cView.painter.regulate()

            val difference = firstDistance * (cView.painter.bf.sb.siz / previousScale - 1)

            cView.painter.bf.sb.pos = (previousX - difference).toInt()

            if (cView.paused) {
                cView.invalidate()
            }

            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            if(cView.scaleMode) {
                realFX = detector.focusX
                previousX = cView.painter.bf.sb.pos.toFloat()
                previousScale = cView.painter.bf.sb.siz.toFloat()

                cView.scaleMode = false
            }

            return super.onScaleBegin(detector)
        }
    }
}