package com.mandarin.bcu.androidutil.animation.coroutine

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.mandarin.bcu.androidutil.AnimatedGifEncoder
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.animation.AnimationCView
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import common.pack.Identifier
import common.system.P
import common.util.anim.EAnimU
import common.util.unit.AbEnemy
import common.util.unit.Enemy
import common.util.unit.Unit
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.lang.ref.WeakReference

class AddGIF(c: Activity?, w: Int, h: Int, p: P?, siz: Float, night: Boolean, private val data: Identifier<*>, private val unit: Boolean) : CoroutineTask<Void>() {
    companion object {
        var frame = 0
        var bos = ByteArrayOutputStream()
        var encoder = AnimatedGifEncoder()
    }

    private var animU: EAnimU? = null
    private val w: Int
    private val h: Int
    private val siz: Float
    private val p: P?
    private val night: Boolean
    private val c: WeakReference<Activity?>

    override fun prepare() {
    }

    override fun doSomething() {
        if(!StaticStore.keepDoing) {
            encoder = AnimatedGifEncoder()
            bos = ByteArrayOutputStream()
            frame = 0
            StaticStore.gifFrame = 0
            StaticStore.enableGIF = false
            StaticStore.gifisSaving = false
            AnimationCView.gifTask.clear()
            return
        }

        val b = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
        val c = Canvas(b)
        val p1 = Paint()
        val bp = Paint()
        p1.isFilterBitmap = true
        p1.isAntiAlias = true
        val back = Paint()
        if (night) back.color = Color.argb(255, 54, 54, 54) else back.color = Color.WHITE
        val c2 = CVGraphics(c, p1, bp, night)
        c2.independent = true
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), back)
        animU!!.draw(c2, p, siz.toDouble())
        encoder.addFrame(b)

        frame++

        P.delete(p)

        if(AnimationCView.gifTask.isNotEmpty()) {
            AnimationCView.gifTask.removeAt(0)

            if(AnimationCView.gifTask.isNotEmpty()) {
                AnimationCView.trigger()
            }
        }
    }

    override fun finish() {
        if(frame == StaticStore.gifFrame) {
            encoder.finish()
            if(unit) {
                GIFAsync(this.c.get(), data, StaticStore.formposition).execute()
            } else {
                GIFAsync(this.c.get(), data).execute()
            }
        }
    }

    init {
        if (unit) {
            val d = StaticStore.transformIdentifier<Unit>(data)

            if(d != null) {
                val u = d.get()

                if(u != null) {
                    this.animU = u.forms[StaticStore.formposition].getEAnim(StaticStore.getAnimType(StaticStore.animposition))
                    this.animU?.setTime(StaticStore.frame)
                }
            }
        } else {
            val d = StaticStore.transformIdentifier<AbEnemy>(data)

            if(d != null) {
                val e = d.get()

                if(e != null && e is Enemy) {
                    this.animU = e.getEAnim(StaticStore.getAnimType(StaticStore.animposition))
                    this.animU?.setTime(StaticStore.frame)
                }
            }
        }
        this.w = w
        this.h = h
        this.siz = siz
        this.p = p
        this.night = night
        this.c = WeakReference(c)

        if(encoder.frameRate != 30f) {
            encoder.frameRate = 30f
            encoder.start(bos)
        }

        if(c?.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LOCKED)
            c?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }
}