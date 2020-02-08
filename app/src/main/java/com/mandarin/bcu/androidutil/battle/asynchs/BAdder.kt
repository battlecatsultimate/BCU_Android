package com.mandarin.bcu.androidutil.battle.asynchs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Point
import android.media.MediaPlayer
import android.os.AsyncTask
import android.view.*
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View.OnTouchListener
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.MediaPrepare
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.battle.BDefinder
import com.mandarin.bcu.androidutil.battle.BattleView
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler.resetHandler
import com.mandarin.bcu.androidutil.enemy.EDefiner
import com.mandarin.bcu.androidutil.fakeandroid.AndroidKeys
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics.Companion.clear
import com.mandarin.bcu.androidutil.stage.MapDefiner
import com.mandarin.bcu.androidutil.unit.Definer
import com.mandarin.bcu.util.page.BBCtrl
import common.battle.BasisSet
import common.battle.SBCtrl
import common.system.P
import java.lang.ref.WeakReference
import kotlin.math.ln

class BAdder(activity: Activity, private val mapcode: Int, private val stid: Int, private val stage: Int, private val star: Int, private val item: Int) : AsyncTask<Void?, Int?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    private var x = 0f
    private var y = 0f
    public override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val fab: FloatingActionButton = activity.findViewById(R.id.battlepause)
        val fast: FloatingActionButton = activity.findViewById(R.id.battlefast)
        val slow: FloatingActionButton = activity.findViewById(R.id.battleslow)
        fab.hide()
        fast.hide()
        slow.hide()
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        Definer().define(activity)
        publishProgress(0)
        EDefiner().define(activity)
        publishProgress(1)
        MapDefiner().define(activity)
        publishProgress(2)
        BDefinder().define()
        publishProgress(3)
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onProgressUpdate(vararg result: Int?) {
        val activity = weakReference.get() ?: return
        val loadt = activity.findViewById<TextView>(R.id.battleloadt)
        when (result[0]) {
            0 -> loadt.setText(R.string.stg_info_enem)
            1 -> loadt.setText(R.string.stg_list_stl)
            2 -> loadt.setText(R.string.battle_loading)
            3 -> {
                val layout = activity.findViewById<LinearLayout>(R.id.battlelayout)
                val mc = StaticStore.map[mapcode] ?: return
                val stm = mc.maps[stid] ?: return
                val stg = stm.list[stage] ?: return
                val ctrl = SBCtrl(AndroidKeys(), stg, star, BasisSet.current.sele, intArrayOf(item), 0L)
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
                skipframe.setOnClickListener {
                    battleView.painter.bf.update()
                    battleView.invalidate()
                }
                actionButton.isExpanded = false
                actionButton.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        actionButton.isExpanded = true
                        battleView.paused = true
                        fast.hide()
                        slow.hide()
                    }
                })
                play.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        actionButton.isExpanded = false
                        battleView.paused = false
                        fast.show()
                        slow.show()
                    }
                })
                battleView.setOnTouchListener(object : OnTouchListener {
                    var preid = -1
                    var preX = 0
                    override fun onTouch(v: View, event: MotionEvent): Boolean {
                        detector.onTouchEvent(event)
                        if (preid == -1) preid = event.getPointerId(0)
                        val id = event.getPointerId(0)
                        val x2 = event.x.toInt()
                        val action = event.action
                        if (action == MotionEvent.ACTION_DOWN) {
                            x = event.x
                            y = event.y
                        } else if (action == MotionEvent.ACTION_UP) {
                            if (battleView.painter.bf.sb.ubase.health > 0 && battleView.painter.bf.sb.ebase.health > 0) {
                                battleView.getPainter().click(Point(event.x.toInt(), event.y.toInt()), action)
                            }
                        } else if (action == MotionEvent.ACTION_MOVE) {
                            if (event.pointerCount == 1 && id == preid) {
                                battleView.painter.pos += x2 - preX
                                if (battleView.paused) {
                                    battleView.invalidate()
                                }
                            }
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
                            activity.finish()
                        }
                        dialog.show()
                    } else {
                        P.stack.clear()
                        clear()
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
                retry.setOnClickListener {
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

                val mus = activity.findViewById<Switch>(R.id.switchmus)
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
                    } else {
                        val editor = shared.edit()
                        editor.putBoolean("music", false)
                        editor.apply()
                        SoundHandler.musicPlay = false
                        musvol.isEnabled = false
                        if (SoundHandler.MUSIC.isInitialized && SoundHandler.MUSIC.isRunning) {
                            if (SoundHandler.MUSIC.isPlaying) SoundHandler.MUSIC.pause()
                        }
                    }
                }
                musvol.progress = shared.getInt("mus_vol", 99)
                musvol.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            if (progress >= 100 || progress < 0) return
                            val editor = shared.edit()
                            editor.putInt("mus_vol", progress)
                            editor.apply()
                            val log1 = (1 - ln(100 - progress.toDouble()) / ln(100.0)).toFloat()
                            SoundHandler.MUSIC.setVolume(log1, log1)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
                val switchse = activity.findViewById<Switch>(R.id.switchse)
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
                        SoundHandler.se_vol = StaticStore.getVolumScaler((shared.getInt("se_vol", 99) * 0.85).toInt())
                        seekse.isEnabled = true
                    } else {
                        val editor = shared.edit()
                        editor.putBoolean("SE", false)
                        editor.apply()
                        SoundHandler.se_vol = 0f
                        seekse.isEnabled = false
                    }
                }
                seekse.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            if (progress >= 100 || progress < 0) return
                            val editor = shared.edit()
                            editor.putInt("se_vol", progress)
                            editor.apply()
                            SoundHandler.se_vol = StaticStore.getVolumScaler((progress * 0.85).toInt())
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get() ?: return
        val battleView: BattleView = activity.findViewById(R.id.battleView)
        val prog = activity.findViewById<ProgressBar>(R.id.battleprog)
        val loadt = activity.findViewById<TextView>(R.id.battleloadt)
        val fab: FloatingActionButton = activity.findViewById(R.id.battlepause)
        val fast: FloatingActionButton = activity.findViewById(R.id.battlefast)
        val slow: FloatingActionButton = activity.findViewById(R.id.battleslow)
        setAppear(battleView)
        fab.show()
        fast.show()
        slow.show()
        (prog.parent as ViewManager).removeView(prog)
        (loadt.parent as ViewManager).removeView(loadt)
        battleView.initialized = true
        if (SoundHandler.MUSIC.isInitialized && !SoundHandler.MUSIC.isRunning) {
            SoundHandler.MUSIC.prepareAsync()
            SoundHandler.MUSIC.setOnPreparedListener(object : MediaPrepare() {
                override fun prepare(mp: MediaPlayer?) {
                    if (SoundHandler.musicPlay) SoundHandler.MUSIC.start()
                }
            })
        }
    }

    private fun setAppear(vararg views: View) {
        for (v in views) v.visibility = View.VISIBLE
    }

    private inner class ScaleListener internal constructor(private val cView: BattleView) : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            cView.painter.siz *= detector.scaleFactor.toDouble()
            if (cView.paused) {
                cView.invalidate()
            }
            return true
        }

    }

}