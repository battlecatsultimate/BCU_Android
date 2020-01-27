package com.mandarin.bcu.androidutil.animation.asynchs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Environment
import android.util.TypedValue
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
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.animation.AnimationCView
import com.mandarin.bcu.androidutil.io.MediaScanner
import com.mandarin.bcu.androidutil.unit.Definer
import common.system.MultiLangCont
import common.util.pack.Pack
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

open class UAnimationLoader(activity: Activity, private val id: Int, private val form: Int) : AsyncTask<Void?, Int?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    private val animS = intArrayOf(R.string.anim_move, R.string.anim_wait, R.string.anim_atk, R.string.anim_kb, R.string.anim_burrow, R.string.anim_under, R.string.anim_burrowup)
    override fun onPreExecute() {
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
        val loadst = activity.findViewById<TextView>(R.id.imgviewerst)
        loadst.setText(R.string.unit_list_unitload)
        setDisappear(anims, forms, setting, player, controller, frame, fps, gif, cViewlayout, img)
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        Definer().define(activity)
        publishProgress(0)
        if (StaticStore.names == null) {
            StaticStore.names = arrayOfNulls(StaticStore.unitnumber)
            for (i in StaticStore.names.indices) {
                StaticStore.names[i] = withID(i, MultiLangCont.FNAME.getCont(Pack.def.us.ulist[i].forms[0]))
            }
        }
        publishProgress(2)
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onProgressUpdate(vararg result: Int?) {
        val activity = weakReference.get() ?: return
        val st = activity.findViewById<TextView>(R.id.imgviewerst)
        when (result[0]) {
            0 -> st.setText(R.string.unit_list_unitname)
            1 -> st.setText(R.string.unit_list_unitic)
            2 -> {
                val anims = activity.findViewById<Spinner>(R.id.animselect)
                val forms = activity.findViewById<Spinner>(R.id.formselect)
                val controller = activity.findViewById<SeekBar>(R.id.animframeseek)
                val frame = activity.findViewById<TextView>(R.id.animframe)
                val fps = activity.findViewById<TextView>(R.id.imgviewerfps)
                val gif = activity.findViewById<TextView>(R.id.imgviewergiffr)
                val buttons = arrayOf<FloatingActionButton>(activity.findViewById(R.id.animbackward), activity.findViewById(R.id.animplay), activity.findViewById(R.id.animforward))
                val cViewlayout = activity.findViewById<LinearLayout>(R.id.imgviewerln)
                val option: FloatingActionButton = activity.findViewById(R.id.imgvieweroption)
                val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
                val cView = AnimationCView(activity, id, StaticStore.formposition, 0, !shared.getBoolean("theme", false), shared.getBoolean("Axis", true), frame, controller, fps, gif)
                cView.size = StaticStore.dptopx(1f, activity).toFloat() / 1.25f
                val detector = ScaleGestureDetector(activity, ScaleListener(cView))
                cView.setOnTouchListener(object : OnTouchListener {
                    var preid = -1f
                    var preX = 0f
                    var preY = 0f
                    override fun onTouch(v: View, event: MotionEvent): Boolean {
                        detector.onTouchEvent(event)
                        if (preid == -1f) preid = event.getPointerId(0).toFloat()
                        val id = event.getPointerId(0)
                        val x = event.x
                        val y = event.y
                        if (event.action == MotionEvent.ACTION_MOVE) {
                            if (event.pointerCount == 1 && id.toFloat() == preid) {
                                val dx = x - preX
                                val dy = y - preY
                                cView.posx += dx
                                cView.posy += dy
                            }
                        }
                        preX = x
                        preY = y
                        preid = id.toFloat()
                        return true
                    }
                })
                cView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                cViewlayout.addView(cView)
                forms.setSelection(form)
                val name: MutableList<String?> = ArrayList()
                run {
                    var i = 0
                    while (i < StaticStore.units[id].forms[0].anim.anims.size) {
                        name.add(activity.getString(animS[i]))
                        i++
                    }
                }
                val ids: MutableList<String> = ArrayList()
                var i = 0
                while (i < StaticStore.units[id].forms.size) {
                    ids.add("$id-$i")
                    i++
                }
                val adapter1 = ArrayAdapter(activity, R.layout.spinneradapter, ids)
                val adapter = ArrayAdapter(activity, R.layout.spinneradapter, name)
                anims.adapter = adapter
                forms.adapter = adapter1
                forms.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, ids: Long) {
                        if (StaticStore.formposition != position) {
                            StaticStore.formposition = position
                            cView.anim = StaticStore.units[id].forms[position].getEAnim(anims.selectedItemPosition)
                            controller.max = cView.anim?.len() ?: 1
                            controller.progress = 0
                            StaticStore.frame = 0
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
                anims.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                        if (StaticStore.animposition != position) {
                            StaticStore.animposition = position
                            cView.anim!!.changeAnim(position)
                            controller.max = cView.anim!!.len()
                            controller.progress = 0
                            StaticStore.frame = 0
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
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
                buttons[1].setOnClickListener {
                    frame.setTextColor(getAttributeColor(activity, R.attr.TextPrimary))
                    if (StaticStore.play) {
                        buttons[1].setImageDrawable(activity.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                        buttons[0].show()
                        buttons[2].show()
                        controller.isEnabled = true
                    } else {
                        buttons[1].setImageDrawable(activity.getDrawable(R.drawable.ic_pause_black_24dp))
                        buttons[0].hide()
                        buttons[2].hide()
                        controller.isEnabled = false
                    }
                    StaticStore.play = !StaticStore.play
                }
                buttons[2].setOnClickListener {
                    StaticStore.frame++
                    cView.anim!!.setTime(StaticStore.frame)
                    frame.setTextColor(getAttributeColor(activity, R.attr.TextPrimary))
                }
                controller.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            StaticStore.frame = progress
                            cView.anim!!.setTime(StaticStore.frame)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
                frame.text = activity.getString(R.string.anim_frame).replace("-", "" + StaticStore.frame)
                cView.anim!!.changeAnim(StaticStore.animposition)
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
                        R.id.anim_option_png -> {
                            val b = Bitmap.createBitmap(cView.width, cView.height, Bitmap.Config.ARGB_8888)
                            val c = Canvas(b)
                            val p = Paint()
                            if (!shared.getBoolean("theme", false)) p.color = Color.argb(255, 54, 54, 54) else p.color = Color.argb(255, 255, 255, 255)
                            c.drawRect(0f, 0f, b.width.toFloat(), b.height.toFloat(), p)
                            cView.draw(c)
                            val path = Environment.getExternalStorageDirectory().path + "/BCU/img/"
                            val g = File(path)
                            if (!g.exists()) g.mkdirs()
                            val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                            val date = Date()
                            val name2 = dateFormat.format(date) + "-U-" + id + "-" + form + ".png"
                            val f = File(path, name2)
                            try {
                                if (!f.exists()) f.createNewFile()
                                val fos = FileOutputStream(f)
                                b.compress(Bitmap.CompressFormat.PNG, 100, fos)
                                fos.close()
                                MediaScanner(activity, f)
                                StaticStore.showShortMessage(activity, activity.getString(R.string.anim_png_success).replace("-", "/BCU/img/$name2"))
                            } catch (e: IOException) {
                                e.printStackTrace()
                                StaticStore.showShortMessage(activity, R.string.anim_png_fail)
                            }
                            return@OnMenuItemClickListener true
                        }
                        R.id.anim_option_pngtr -> {
                            val b = Bitmap.createBitmap(cView.width, cView.height, Bitmap.Config.ARGB_8888)
                            val c = Canvas(b)
                            val p = Paint()
                            if (!shared.getBoolean("theme", false)) p.color = Color.argb(255, 54, 54, 54) else p.color = Color.argb(255, 255, 255, 255)
                            cView.trans = true
                            cView.draw(c)
                            cView.trans = false
                            val path = Environment.getExternalStorageDirectory().path + "/BCU/img/"
                            val g = File(path)
                            if (!g.exists()) g.mkdirs()
                            val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                            val date = Date()
                            val name1 = dateFormat.format(date) + "-U-Trans-" + id + "-" + form + ".png"
                            val f = File(path, name1)
                            try {
                                if (!f.exists()) f.createNewFile()
                                val fos = FileOutputStream(f)
                                b.compress(Bitmap.CompressFormat.PNG, 100, fos)
                                fos.close()
                                MediaScanner(activity, f)
                                StaticStore.showShortMessage(activity, activity.getString(R.string.anim_png_success).replace("-", "/BCU/img/$name1"))
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
                                    cView.startAsync(activity)
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
                controller.progress = StaticStore.frame
                anims.setSelection(StaticStore.animposition)
                forms.setSelection(StaticStore.formposition)
                val bck: FloatingActionButton = activity.findViewById(R.id.imgviewerbck)
                bck.setOnClickListener {
                    if (!StaticStore.gifisSaving) {
                        StaticStore.play = true
                        StaticStore.frame = 0
                        StaticStore.animposition = 0
                        StaticStore.formposition = 0
                        StaticStore.enableGIF = false
                        StaticStore.gifFrame = 0
                        StaticStore.frames.clear()
                        activity.finish()
                    } else {
                        val builder = AlertDialog.Builder(activity)
                        builder.setTitle(R.string.anim_gif_warn)
                        builder.setMessage(R.string.anim_gif_recording)
                        builder.setPositiveButton(R.string.main_file_ok) { _, _ ->
                            StaticStore.play = true
                            StaticStore.frame = 0
                            StaticStore.animposition = 0
                            StaticStore.formposition = 0
                            StaticStore.keepDoing = false
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

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get() ?: return
        val imgprog = activity.findViewById<ProgressBar>(R.id.imgviewerprog)
        val st = activity.findViewById<TextView>(R.id.imgviewerst)
        setDisappear(imgprog, st)
        val anims = activity.findViewById<Spinner>(R.id.animselect)
        val forms = activity.findViewById<Spinner>(R.id.formselect)
        val setting: FloatingActionButton = activity.findViewById(R.id.imgvieweroption)
        val player = activity.findViewById<TableRow>(R.id.palyrow)
        val controller = activity.findViewById<SeekBar>(R.id.animframeseek)
        val frame = activity.findViewById<TextView>(R.id.animframe)
        val fps = activity.findViewById<TextView>(R.id.imgviewerfps)
        val gif = activity.findViewById<TextView>(R.id.imgviewergiffr)
        val cViewlayout = activity.findViewById<LinearLayout>(R.id.imgviewerln)
        val buttons = arrayOf<FloatingActionButton>(activity.findViewById(R.id.animbackward), activity.findViewById(R.id.animplay), activity.findViewById(R.id.animforward))
        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        setAppear(anims, forms, setting, player, controller, frame, fps, cViewlayout)
        if (StaticStore.enableGIF || StaticStore.gifisSaving) gif.visibility = View.VISIBLE
        if (StaticStore.play) {
            buttons[0].hide()
            buttons[2].hide()
            controller.isEnabled = false
        } else {
            buttons[1].setImageDrawable(activity.getDrawable(R.drawable.ic_pause_black_24dp))
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

    private fun number(num: Int): String {
        return when (num) {
            in 0..9 -> {
                "00$num"
            }
            in 10..99 -> {
                "0$num"
            }
            else -> {
                num.toString()
            }
        }
    }

    private fun withID(id: Int, name: String): String {
        return if (name == "") {
            number(id)
        } else {
            number(id) + " - " + name
        }
    }

    private inner class ScaleListener internal constructor(private val cView: AnimationCView) : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            cView.size *= detector.scaleFactor
            return true
        }

    }

    companion object {
        private fun getAttributeColor(context: Context, attributeId: Int): Int {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(attributeId, typedValue, true)
            val colorRes = typedValue.resourceId
            var color = -1
            try {
                color = ContextCompat.getColor(context, colorRes)
            } catch (e: NotFoundException) {
                e.printStackTrace()
            }
            return color
        }
    }

}