package com.mandarin.bcu.androidutil.animation

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.animation.asynchs.AddGIF
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics
import common.CommonStatic
import common.pack.Identifier
import common.pack.UserProfile
import common.system.P
import common.util.anim.EAnimU
import common.util.unit.AbEnemy
import common.util.unit.Enemy
import common.util.unit.Unit

@SuppressLint("ViewConstructor")
class AnimationCView : View {
    @JvmField
    var anim: EAnimU? = null
    val activity: Activity?
    private var night = false
    @JvmField
    var trans = false
    private var renderer: Renderer? = null
    private var form = -1
    private var textView: TextView? = null
    private var seekBar: SeekBar? = null
    private var fpsind: TextView? = null
    @JvmField
    var gif: TextView? = null
    private val p = Paint()
    private val p1 = Paint()
    private val bp = Paint()
    private val range = Paint()
    private var cv: CVGraphics? = null
    private var p2: P? = null
    private var animP: P? = null
    private var t: Long = -1
    private var t1: Long = -1
    var fps: Long = 0
    var size = 1f
    var posx = 0f
    var posy = 0f
    var sleeptime: Long = 0
    var started = false
    val data: Identifier<*>

    constructor(context: Activity?, data: Identifier<Unit>, form: Int, mode: Int, night: Boolean, axis: Boolean, textView: TextView?, seekBar: SeekBar?, fpsind: TextView?, gif: TextView?) : super(context) {
        activity = context
        renderer = Renderer()
        this.form = form
        this.data = data

        val value = StaticStore.getAnimType(mode)

        val u = Identifier.get(data)

        if(u != null) {
            anim = u.forms[form].getEAnim(value)
            anim?.setTime(StaticStore.frame)
            this.textView = textView
            this.seekBar = seekBar
            this.fpsind = fpsind
            this.gif = gif
            CommonStatic.getConfig().ref = axis
            range.style = Paint.Style.STROKE
            if (night) {
                p.color = Color.argb(255, 54, 54, 54)
                range.color = Color.GREEN
            } else {
                p.color = Color.argb(255, 255, 255, 255)
                range.color = Color.RED
            }
            p1.isFilterBitmap = true
            p2 = P((width.toFloat() / 2).toDouble(), (height.toFloat() * 2f / 3f).toDouble())
            cv = CVGraphics(Canvas(), p1, bp, night)
            this.night = night
            StaticStore.keepDoing = true
        }
    }

    constructor(context: Activity?, data: Identifier<AbEnemy>, mode: Int, night: Boolean, axis: Boolean, textView: TextView?, seekBar: SeekBar?, fpsind: TextView?, gif: TextView?) : super(context) {
        val e = data.get() ?: UserProfile.getBCData().enemies[0]
        activity = context

        val value = StaticStore.getAnimType(mode)

        this.data = data

        if(e is Enemy) {
            e.anim.load()
            anim = e.getEAnim(value)
            anim?.setTime(StaticStore.frame)
            this.textView = textView
            this.seekBar = seekBar
            this.fpsind = fpsind
            this.gif = gif
            renderer = Renderer()
            CommonStatic.getConfig().ref = axis
            if (night) {
                p.color = 0x363636
            } else {
                p.color = Color.WHITE
            }
            p1.isFilterBitmap = true
            p2 = P((width.toFloat() / 2).toDouble(), (height.toFloat() * 2f / 3f).toDouble())
            cv = CVGraphics(Canvas(), p1, bp, night)
            this.night = night
            StaticStore.keepDoing = true
        }
    }

    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postDelayed(renderer, 0)
        started = true
    }

    @SuppressLint("DrawAllocation")
    public override fun onDraw(canvas: Canvas) {
        if (StaticStore.gifisSaving && !StaticStore.keepDoing) {
            StaticStore.keepDoing = true
        }
        if (StaticStore.enableGIF) {
            animP = P.newP((width.toFloat() / 2 + posx).toDouble(), (height.toFloat() * 2 / 3 + posy).toDouble())
            AddGIF(activity, width, height, animP, size, night, data, form != -1).execute()
            StaticStore.gifFrame++
        }
        if (StaticStore.play) {
            if (t1 != -1L && t - t1 != 0L) {
                fps = 1000L / (t - t1)
            }

            p2 = P.newP(width.toFloat() / 2 + posx.toDouble(), height.toFloat() * 2 / 3 + posy.toDouble())

            cv = CVGraphics(canvas, p1, bp, night)

            if (fps < 30)
                sleeptime = (sleeptime * 0.9 - 0.1).toLong()
            else if (fps > 30)
                sleeptime = (sleeptime * 0.9+0.1).toLong()

            if (!trans)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), p)

            anim!!.draw(cv, p2, size.toDouble())
            anim!!.update(true)

            StaticStore.frame++

            t1 = t

            P.delete(p2)
        } else {
            if (t1 != -1L && t - t1 != 0L) {
                fps = 1000L / (t - t1)
            }

            p2 = P.newP(width.toFloat() / 2 + posx.toDouble(), height.toFloat() * 2 / 3 + posy.toDouble())
            cv = CVGraphics(canvas, p1, bp, night)

            if (fps < 30)
                sleeptime = (sleeptime * 0.9 - 0.1).toLong()
            else if (fps > 30)
                sleeptime = (sleeptime * 0.9+0.1).toLong()

            if (!trans)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), p)

            anim!!.draw(cv, p2, size.toDouble())

            P.delete(p2)

            t1 = t
        }
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(renderer)
    }

    private inner class Renderer : Runnable {
        override fun run() {
            t = System.currentTimeMillis()
            invalidate()
            textView!!.text = context.getText(R.string.anim_frame).toString().replace("-", "" + StaticStore.frame)
            fpsind!!.text = context.getText(R.string.def_fps).toString().replace("-", "" + fps)
            seekBar!!.progress = if(StaticStore.frame >= seekBar!!.max && StaticStore.play) {
                StaticStore.frame = 0
                0
            } else {
                StaticStore.frame
            }

            if(StaticStore.enableGIF || StaticStore.gifisSaving) {
                val giftext = if (StaticStore.gifFrame != 0)
                    context.getText(R.string.anim_gif_frame).toString().replace("-", "" + StaticStore.gifFrame) + " (" + (AddGIF.frame.toFloat() / StaticStore.gifFrame.toFloat() * 100f).toInt() + "%)"
                else
                    context.getText(R.string.anim_gif_frame).toString().replace("-", "" + StaticStore.gifFrame)
                gif!!.text = giftext
            } else {
                if(gif?.visibility != GONE) {
                    gif?.visibility = GONE
                }
            }
            if (started) postDelayed(this, 1000L / 30L + sleeptime)
        }
    }
}