package com.mandarin.bcu.androidutil.animation.asynchs

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.AsyncTask
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics
import common.system.P
import common.util.anim.EAnimU

class AddGIF(w: Int, h: Int, p: P, siz: Float, night: Boolean, id: Int, unit: Boolean) : AsyncTask<Void?, Void?, Void?>() {
    private var animU: EAnimU? = null
    private val w: Int
    private val h: Int
    private val siz: Float
    private val p: P
    private val night: Boolean
    override fun doInBackground(vararg voids: Void?): Void? {
        val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444)
        val c = Canvas(b)
        val p1 = Paint()
        val bp = Paint()
        p1.isFilterBitmap = true
        p1.isAntiAlias = true
        val back = Paint()
        if (night) back.color = Color.argb(255, 54, 54, 54) else back.color = Color.WHITE
        val c2 = CVGraphics(c, p1, bp, night)
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), back)
        animU!!.draw(c2, p, siz.toDouble())
        StaticStore.frames.add(b)
        StaticStore.gifFrame++
        return null
    }

    init {
        if (unit) {
            this.animU = StaticStore.units[id].forms[StaticStore.formposition].getEAnim(StaticStore.animposition)
            this.animU?.setTime(StaticStore.frame)
        } else {
            this.animU = StaticStore.enemies[id].getEAnim(StaticStore.animposition)
            this.animU?.setTime(StaticStore.frame)
        }
        this.w = w
        this.h = h
        this.siz = siz
        this.p = p
        this.night = night
    }
}