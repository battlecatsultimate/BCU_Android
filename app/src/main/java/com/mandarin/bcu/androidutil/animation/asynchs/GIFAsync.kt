package com.mandarin.bcu.androidutil.animation.asynchs

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.AsyncTask
import android.os.Environment
import android.view.View
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.AnimatedGifEncoder
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.animation.AnimationCView
import com.mandarin.bcu.androidutil.io.MediaScanner
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class GIFAsync : AsyncTask<Void?, Void?, Void?> {
    private val weakReference: WeakReference<AnimationCView>
    private val activityWeakReference: WeakReference<Activity>
    private var id = -1
    private var form = -1
    var keepDoing = true
    private var done = false

    constructor(cView: AnimationCView, activity: Activity, id: Int, form: Int) {
        weakReference = WeakReference(cView)
        activityWeakReference = WeakReference(activity)
        this.id = id
        this.form = form
    }

    constructor(cView: AnimationCView, activity: Activity, id: Int) {
        weakReference = WeakReference(cView)
        activityWeakReference = WeakReference(activity)
        this.id = id
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val c = activityWeakReference.get()
        val cView = weakReference.get()
        if (c == null || cView == null) return null
        c.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        val buffer = generateGIF()
        if (buffer == null) {
            StaticStore.frames.clear()
            StaticStore.gifFrame = 0
            return null
        }
        val path = Environment.getExternalStorageDirectory().path + "/BCU/gif/"
        val f = File(path)
        if (!f.exists()) f.mkdirs()
        val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        val date = Date()
        val name: String
        name = if (id != -1) {
            if (form != -1) {
                dateFormat.format(date) + "-U-" + id + "-" + form + ".gif"
            } else {
                dateFormat.format(date) + "-E-" + id + ".gif"
            }
        } else {
            dateFormat.format(date) + ".gif"
        }
        val g = File(path, name)
        try {
            if (!g.exists()) g.createNewFile()
            val fos = FileOutputStream(g)
            fos.write(buffer)
            fos.close()
            MediaScanner(c, g)
            done = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        StaticStore.frames.clear()
        return null
    }

    public override fun onPostExecute(result: Void?) {
        val c = activityWeakReference.get()
        val cView = weakReference.get()
        if (c == null || cView == null) return
        val shared = c.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        if (shared.getInt("Orientation", 0) == 1) c.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else if (shared.getInt("Orientation", 0) == 2) c.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT else if (shared.getInt("Orientation", 0) == 0) c.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        cView.gif!!.visibility = View.GONE
        val path = Environment.getExternalStorageDirectory().path + "/BCU/gif/"
        val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        val date = Date()
        val name = dateFormat.format(date) + ".gif"
        if (!keepDoing) {
            val f = File(path, name)
            if (f.exists()) f.delete()
        }
        if (done && keepDoing) StaticStore.showShortMessage(c, c.getText(R.string.anim_png_success).toString().replace("-", path + name)) else if (!keepDoing) StaticStore.showShortMessage(c, R.string.anim_gif_cancel) else StaticStore.showShortMessage(c, R.string.anim_png_fail)
        StaticStore.gifisSaving = false
    }

    private fun generateGIF(): ByteArray? {
        val bos = ByteArrayOutputStream()
        val encoder = AnimatedGifEncoder()
        encoder.setFrameRate(30f)
        encoder.start(bos)
        for (bitmap in StaticStore.frames) {
            if (keepDoing) {
                encoder.addFrame(bitmap)
                StaticStore.gifFrame--
            } else {
                return null
            }
        }
        encoder.finish()
        return bos.toByteArray()
    }
}