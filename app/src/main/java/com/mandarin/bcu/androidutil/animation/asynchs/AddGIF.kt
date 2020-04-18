package com.mandarin.bcu.androidutil.animation.asynchs

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.AsyncTask
import com.mandarin.bcu.androidutil.AnimatedGifEncoder
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics
import common.system.P
import common.util.anim.EAnimU
import common.util.pack.Pack
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference

class AddGIF(c: Activity?, w: Int, h: Int, p: P?, siz: Float, night: Boolean, private val pid: Int, private val id: Int, private val unit: Boolean) : AsyncTask<Void?, Void?, Void?>() {
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

    override fun doInBackground(vararg voids: Void?): Void? {
        if(!StaticStore.keepDoing) {
            encoder = AnimatedGifEncoder()
            bos = ByteArrayOutputStream()
            frame = 0
            StaticStore.gifFrame = 0
            StaticStore.enableGIF = false
            StaticStore.gifisSaving = false
            return null
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

        return null
    }

    override fun onPostExecute(result: Void?) {
        if(frame == StaticStore.gifFrame) {
            encoder.finish()
            if(unit) {
                GIFAsync(this.c.get(),pid, id,StaticStore.formposition).execute()
            } else {
                GIFAsync(this.c.get(),id).execute()
            }
        }
    }

    init {
        if (unit) {
            val pack = Pack.map[pid] ?: Pack.def
            this.animU = pack.us.ulist[id].forms[StaticStore.formposition].getEAnim(StaticStore.animposition)
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
        this.c = WeakReference(c)

        if(encoder.frameRate != 30f) {
            encoder.frameRate = 30f
            encoder.start(bos)
        }

        if(c?.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LOCKED)
            c?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }
}