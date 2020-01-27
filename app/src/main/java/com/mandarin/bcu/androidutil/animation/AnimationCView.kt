package com.mandarin.bcu.androidutil.animation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.animation.asynchs.AddGIF
import com.mandarin.bcu.androidutil.animation.asynchs.GIFAsync
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics
import common.system.P
import common.util.ImgCore
import common.util.anim.EAnimU

class AnimationCView : View {
    @JvmField
    var anim: EAnimU? = null
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
    private var async: GIFAsync? = null
    private var t: Long = -1
    private var t1: Long = -1
    var fps: Long = 0
    var size = 1f
    var posx = 0f
    var posy = 0f
    var sleeptime: Long = 0
    var started = false

    constructor(context: Context?, id: Int, form: Int, mode: Int, night: Boolean, axis: Boolean, textView: TextView?, seekBar: SeekBar?, fpsind: TextView?, gif: TextView?) : super(context) {
        renderer = Renderer()
        this.id = id
        this.form = form
        anim = StaticStore.units[id].forms[form].getEAnim(mode)
        anim?.setTime(StaticStore.frame)
        this.textView = textView
        this.seekBar = seekBar
        this.fpsind = fpsind
        this.gif = gif
        ImgCore.ref = axis
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
    }

    constructor(context: Context?, id: Int, mode: Int, night: Boolean, axis: Boolean, textView: TextView?, seekBar: SeekBar?, fpsind: TextView?, gif: TextView?) : super(context) {
        this.id = id
        anim = StaticStore.enemies[id].getEAnim(mode)
        anim?.setTime(StaticStore.frame)
        this.textView = textView
        this.seekBar = seekBar
        this.fpsind = fpsind
        this.gif = gif
        renderer = Renderer()
        ImgCore.ref = axis
        if (night) {
            p.color = 0x363636
        } else {
            p.color = Color.WHITE
        }
        p1.isFilterBitmap = true
        p2 = P((width.toFloat() / 2).toDouble(), (height.toFloat() * 2f / 3f).toDouble())
        cv = CVGraphics(Canvas(), p1, bp, night)
        this.night = night
    }

    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postDelayed(renderer, 0)
        started = true
    }

    @SuppressLint("DrawAllocation")
    public override fun onDraw(canvas: Canvas) {
        if (StaticStore.gifisSaving && !StaticStore.keepDoing) {
            async!!.keepDoing = false
            StaticStore.keepDoing = true
        }
        if (StaticStore.enableGIF) {
            p2 = P((width.toFloat() / 2 + posx).toDouble(), (height.toFloat() * 2 / 3 + posy).toDouble())
            if (form != -1) AddGIF(width, height, p2 ?: P((width.toFloat() / 2 + posx).toDouble(), (height.toFloat() * 2 / 3 + posy).toDouble()), size, night, id, true).execute() else AddGIF(width, height, p2 ?: P((width.toFloat() / 2 + posx).toDouble(), (height.toFloat() * 2 / 3 + posy).toDouble()), size, night, id, false).execute()
        }
        if (StaticStore.play) {
            if (t1 != -1L && t - t1 != 0L) {
                fps = 1000L / (t - t1)
            }
            p2 = P.newP(width.toFloat() / 2 + posx.toDouble(), height.toFloat() * 2 / 3 + posy.toDouble())
            cv = CVGraphics(canvas, p1, bp, night)
            if (fps < 30) sleeptime = (sleeptime * 0.9 - 0.1).toLong() else if (fps > 30)sleeptime = (sleeptime * 0.9+0.1).toLong()
            if (!trans) canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), p)
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
            if (fps < 30) sleeptime = (sleeptime * 0.9 - 0.1).toLong() else if (fps > 30)sleeptime = (sleeptime * 0.9+0.1).toLong()
            if (!trans) canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), p)
            anim!!.draw(cv, p2, size.toDouble())
            P.delete(p2)
            t1 = t
        }
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(renderer)
    }

    fun startAsync(activity: Activity) {
        async = if (form != -1) {
            GIFAsync(this, activity, id, form)
        } else {
            GIFAsync(this, activity, id)
        }
        async!!.execute()
    }

    private inner class Renderer : Runnable {
        override fun run() {
            t = System.currentTimeMillis()
            invalidate()
            textView!!.text = context.getText(R.string.anim_frame).toString().replace("-", "" + StaticStore.frame)
            fpsind!!.text = context.getText(R.string.def_fps).toString().replace("-", "" + fps)
            seekBar!!.progress = StaticStore.frame
            gif!!.text = context.getText(R.string.anim_gif_frame).toString().replace("-", "" + StaticStore.gifFrame)
            if (started) postDelayed(this, 1000L / 30L + sleeptime)
        }
    }
}