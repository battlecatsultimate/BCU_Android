package com.mandarin.bcu.androidutil.animation.coroutine

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.AnimatedGifEncoder
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.animation.AnimationCView
import com.mandarin.bcu.androidutil.io.MediaScanner
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import common.pack.Identifier
import common.util.unit.AbEnemy
import common.util.unit.Enemy
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class EAnimationLoader(activity: Activity, private val data: Identifier<AbEnemy>?) : CoroutineTask<String>() {
    companion object {
        const val PROCCESS = "process"
    }

    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    private val animS = intArrayOf(R.string.anim_move, R.string.anim_wait, R.string.anim_atk, R.string.anim_kb, R.string.anim_burrow, R.string.anim_under, R.string.anim_burrowup)

    private var realFX = 0f
    private var previousX = 0f

    private var realFY = 0f
    private var previousY = 0f

    private var previousScale = 0f
    private var updateScale = false

    override fun prepare() {
        val activity = weakReference.get() ?: return
        val anims = activity.findViewById<Spinner>(R.id.animselect)
        val forms = activity.findViewById<Spinner>(R.id.formselect)
        val setting: FloatingActionButton = activity.findViewById(R.id.imgvieweroption)
        val player = activity.findViewById<TableRow>(R.id.palyrow)
        val controller = activity.findViewById<SeekBar>(R.id.animframeseek)
        val frame = activity.findViewById<TextView>(R.id.animframe)
        val fps = activity.findViewById<TextView>(R.id.imgviewerfps)
        val gif = activity.findViewById<TextView>(R.id.imgviewergiffr)
        val cViewlayout = activity.findViewById<LinearLayout>(R.id.imgviewerln)
        val img = activity.findViewById<ImageView>(R.id.imgviewerimg)
        val loadst = activity.findViewById<TextView>(R.id.status)
        loadst.setText(R.string.load_enemy)
        setDisappear(anims, forms, setting, player, controller, frame, fps, gif, cViewlayout, img)
    }

    override fun doSomething() {
        val activity = weakReference.get() ?: return

        Definer.define(activity, this::updateProg, this::updateText)

        publishProgress(PROCCESS, "")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun progressUpdate(vararg data: String) {
        val activity = weakReference.get() ?: return

        this.data ?: return

        if(data.size < 2)
            return

        val st = activity.findViewById<TextView>(R.id.status)

        when (data[0]) {
            StaticStore.TEXT -> st.text = data[1]
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
            PROCCESS -> {
                val anims = activity.findViewById<Spinner>(R.id.animselect)
                val controller = activity.findViewById<SeekBar>(R.id.animframeseek)
                val frame = activity.findViewById<TextView>(R.id.animframe)
                val fps = activity.findViewById<TextView>(R.id.imgviewerfps)
                val gif = activity.findViewById<TextView>(R.id.imgviewergiffr)
                val buttons = arrayOf<FloatingActionButton>(activity.findViewById(R.id.animbackward), activity.findViewById(R.id.animplay), activity.findViewById(R.id.animforward))
                val cViewlayout = activity.findViewById<LinearLayout>(R.id.imgviewerln)
                val option: FloatingActionButton = activity.findViewById(R.id.imgvieweroption)
                val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
                val cView = AnimationCView(activity, this.data, 0, !shared.getBoolean("theme", false), shared.getBoolean("Axis", true), frame, controller, fps, gif)
                cView.size = StaticStore.dptopx(1f, activity).toFloat() / 1.25f
                val detector = ScaleGestureDetector(activity, ScaleListener(cView))
                cView.setOnTouchListener(object : OnTouchListener {
                    var preid = -1
                    var preX = 0f
                    var preY = 0f
                    override fun onTouch(v: View, event: MotionEvent): Boolean {
                        detector.onTouchEvent(event)
                        if (preid == -1) preid = event.getPointerId(0)
                        val id = event.getPointerId(0)
                        val x = event.x
                        val y = event.y
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            updateScale = true
                        }
                        if (event.action == MotionEvent.ACTION_MOVE) {
                            if (event.pointerCount == 1 && id == preid) {
                                val dx = x - preX
                                val dy = y - preY
                                cView.posx += dx
                                cView.posy += dy
                            }
                        }
                        preX = x
                        preY = y
                        preid = id
                        return true
                    }
                })
                val name: MutableList<String?> = ArrayList()
                var i = 0

                val e = this.data.get() ?: return

                if(e !is Enemy)
                    return

                while (i < e.anim.anims.size) {
                    name.add(activity.getString(animS[i]))
                    i++
                }
                val adapter = ArrayAdapter(activity, R.layout.spinneradapter, name)
                anims.adapter = adapter
                cView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                cViewlayout.addView(cView)
                anims.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (StaticStore.animposition != position) {
                            StaticStore.animposition = position
                            cView.anim!!.changeAnim(StaticStore.getAnimType(position))
                            controller.max = cView.anim!!.len()
                            controller.progress = 0
                            StaticStore.frame = 0
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
                buttons[1].setOnClickListener {
                    frame.setTextColor(StaticStore.getAttributeColor(activity, R.attr.TextPrimary))
                    if (StaticStore.play) {
                        buttons[1].setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_play_arrow_black_24dp))
                        buttons[0].show()
                        buttons[2].show()
                        controller.isEnabled = true
                    } else {
                        buttons[1].setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_pause_black_24dp))
                        buttons[0].hide()
                        buttons[2].hide()
                        controller.isEnabled = false
                    }
                    StaticStore.play = !StaticStore.play
                }
                buttons[0].setOnClickListener {
                    if (StaticStore.frame > 0) {
                        StaticStore.frame--
                        cView.anim!!.setTime(StaticStore.frame)
                    } else {
                        frame.setTextColor(Color.rgb(227, 66, 66))
                        StaticStore.showShortMessage(activity, R.string.anim_warn_frame)
                    }
                }
                buttons[2].setOnClickListener {
                    StaticStore.frame++
                    cView.anim!!.setTime(StaticStore.frame)
                    frame.setTextColor(StaticStore.getAttributeColor(activity, R.attr.TextPrimary))
                }
                controller.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(controller: SeekBar, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            StaticStore.frame = progress
                            cView.anim!!.setTime(StaticStore.frame)
                        }
                    }

                    override fun onStartTrackingTouch(controller: SeekBar) {}
                    override fun onStopTrackingTouch(controller: SeekBar) {}
                })
                frame.text = activity.getString(R.string.anim_frame).replace("-", "" + StaticStore.frame)
                controller.progress = StaticStore.frame
                anims.setSelection(StaticStore.animposition)
                cView.anim!!.changeAnim(StaticStore.getAnimType(StaticStore.animposition))
                cView.anim!!.setTime(StaticStore.frame)
                controller.max = cView.anim!!.len()
                val popup = PopupMenu(activity, option)
                val menu = popup.menu
                popup.menuInflater.inflate(R.menu.animation_menu, menu)
                if (StaticStore.enableGIF) popup.menu.getItem(3).setTitle(R.string.anim_option_gifstop)
                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.anim_option_reset -> {
                            cView.posx = 0f
                            cView.posy = 0f
                            cView.size = StaticStore.dptopx(1f, activity) / 1.25f
                            return@OnMenuItemClickListener true
                        }
                        R.id.anim_png_normal -> {
                            val b = Bitmap.createBitmap(cView.width, cView.height, Bitmap.Config.ARGB_8888)
                            val c = Canvas(b)
                            val p = Paint()
                            if (!shared.getBoolean("theme", false)) p.color = Color.argb(255, 54, 54, 54) else p.color = Color.argb(255, 255, 255, 255)
                            c.drawRect(0f, 0f, b.width.toFloat(), b.height.toFloat(), p)
                            cView.draw(c)

                            val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                            val date = Date()
                            val name2 = if(this.data.pack == Identifier.DEF) {
                                "${dateFormat.format(date)}-E-Default-${StaticStore.trio(this.data.id)}"
                            } else {
                                "${dateFormat.format(date)}-E-${this.data.pack}-${StaticStore.trio(this.data.id)}"
                            }

                            try {
                                val path = MediaScanner.putImage(activity, b, name2)

                                if(path == MediaScanner.ERRR_WRONG_SDK) {
                                    StaticStore.showShortMessage(activity, R.string.anim_png_fail)
                                } else {
                                    StaticStore.showShortMessage(activity, activity.getString(R.string.anim_png_success).replace("-", path))
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                                StaticStore.showShortMessage(activity, R.string.anim_png_fail)
                            }
                            return@OnMenuItemClickListener true
                        }
                        R.id.anim_png_transp -> {
                            val b = Bitmap.createBitmap(cView.width, cView.height, Bitmap.Config.ARGB_8888)
                            val c = Canvas(b)
                            val p = Paint()
                            if (!shared.getBoolean("theme", false)) p.color = Color.argb(255, 54, 54, 54) else p.color = Color.argb(255, 255, 255, 255)
                            cView.trans = true
                            cView.draw(c)
                            cView.trans = false

                            val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                            val date = Date()
                            val name3 = if(this.data.pack == Identifier.DEF) {
                                "${dateFormat.format(date)}-E-Trans-Default-${StaticStore.trio(this.data.id)}"
                            } else {
                                "${dateFormat.format(date)}-E-Trans-${this.data.pack}-${StaticStore.trio(this.data.id)}"
                            }

                            try {
                                val path = MediaScanner.putImage(activity, b, name3)

                                if(path == MediaScanner.ERRR_WRONG_SDK) {
                                    StaticStore.showShortMessage(activity, R.string.anim_png_fail)
                                } else {
                                    StaticStore.showShortMessage(activity, activity.getString(R.string.anim_png_success).replace("-", path))
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                                StaticStore.showShortMessage(activity, R.string.anim_png_fail)
                            }
                            return@OnMenuItemClickListener true
                        }
                        R.id.anim_option_gif -> {
                            if (!StaticStore.gifisSaving) {
                                if (!StaticStore.enableGIF) {
                                    gif.visibility = View.VISIBLE
                                    item.setTitle(R.string.anim_option_gifstop)
                                } else {
                                    item.setTitle(R.string.anim_option_gifstart)
                                    StaticStore.gifisSaving = true
                                }
                                StaticStore.enableGIF = !StaticStore.enableGIF
                            } else {
                                StaticStore.showShortMessage(activity, R.string.gif_saving)
                            }
                            return@OnMenuItemClickListener true
                        }
                    }
                    false
                })
                option.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        popup.show()
                    }
                })
                val bck: FloatingActionButton = activity.findViewById(R.id.imgviewerbck)
                bck.setOnClickListener {
                    if (!StaticStore.gifisSaving) {
                        StaticStore.play = true
                        StaticStore.frame = 0
                        StaticStore.animposition = 0
                        StaticStore.formposition = 0
                        StaticStore.enableGIF = false
                        StaticStore.gifFrame = 0
                        AnimationCView.gifTask.clear()
                        AddGIF.frame = 0
                        AddGIF.encoder = AnimatedGifEncoder()
                        AddGIF.bos = ByteArrayOutputStream()
                        activity.finish()
                    } else {
                        val builder = AlertDialog.Builder(activity)
                        builder.setTitle(R.string.anim_gif_warn)
                        builder.setMessage(R.string.anim_gif_recording)
                        builder.setPositiveButton(R.string.gif_exit) { _, _ ->
                            StaticStore.play = true
                            StaticStore.frame = 0
                            StaticStore.animposition = 0
                            StaticStore.formposition = 0
                            StaticStore.keepDoing = false
                            StaticStore.enableGIF = false
                            StaticStore.gifisSaving = false
                            StaticStore.gifFrame = 0
                            StaticStore.showShortMessage(activity, R.string.anim_gif_cancel)
                            AnimationCView.gifTask.clear()
                            AddGIF.frame = 0
                            AddGIF.encoder = AnimatedGifEncoder()
                            AddGIF.bos = ByteArrayOutputStream()
                            activity.finish()
                        }
                        builder.setNegativeButton(R.string.main_file_cancel) { _, _ -> }
                        val dialog = builder.create()
                        dialog.show()
                    }
                }
            }
        }
    }

    override fun finish() {
        val activity = weakReference.get() ?: return
        val prog = activity.findViewById<ProgressBar>(R.id.prog)
        val st = activity.findViewById<TextView>(R.id.status)
        setDisappear(prog, st)
        val anims = activity.findViewById<Spinner>(R.id.animselect)
        val setting: FloatingActionButton = activity.findViewById(R.id.imgvieweroption)
        val player = activity.findViewById<TableRow>(R.id.palyrow)
        val controller = activity.findViewById<SeekBar>(R.id.animframeseek)
        val frame = activity.findViewById<TextView>(R.id.animframe)
        val fps = activity.findViewById<TextView>(R.id.imgviewerfps)
        val gif = activity.findViewById<TextView>(R.id.imgviewergiffr)
        val cViewlayout = activity.findViewById<LinearLayout>(R.id.imgviewerln)
        val buttons = arrayOf<FloatingActionButton>(activity.findViewById(R.id.animbackward), activity.findViewById(R.id.animplay), activity.findViewById(R.id.animforward))
        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        setAppear(anims, setting, player, controller, frame, fps, cViewlayout)
        if (StaticStore.enableGIF || StaticStore.gifisSaving) gif.visibility = View.VISIBLE else gif.visibility = View.GONE
        if (StaticStore.play) {
            buttons[0].hide()
            buttons[2].hide()
            controller.isEnabled = false
        } else {
            buttons[1].setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_pause_black_24dp))
        }
        if (!shared.getBoolean("FPS", true)) fps.visibility = View.GONE
    }

    private fun setDisappear(vararg views: View) {
        for (v in views) {
            v.visibility = View.GONE
        }
    }

    private fun setAppear(vararg views: View) {
        for (v in views) {
            v.visibility = View.VISIBLE
        }
    }

    private fun updateText(info: String) {
        val ac = weakReference.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProg(prog: Double) {
        publishProgress(StaticStore.PROG, (prog * 10000.0).toInt().toString())
    }

    private inner class ScaleListener(private val cView: AnimationCView) : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            cView.size *= detector.scaleFactor

            val diffX = (realFX - previousX) * (cView.size / previousScale - 1)
            val diffY = (realFY - previousY) * (cView.size / previousScale - 1)

            cView.posx = previousX - diffX
            cView.posy = previousY - diffY

            return true
        }


        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            if (updateScale) {
                realFX = detector.focusX - cView.width / 2f
                previousX = cView.posx

                realFY = detector.focusY - cView.height * 2f / 3f
                previousY = cView.posy

                previousScale = cView.size

                updateScale = false
            }

            return super.onScaleBegin(detector)
        }
    }
}