package com.mandarin.bcu.androidutil.animation.asynchs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.AnimatedGifEncoder
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.StaticStore.keepDoing
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class GIFAsync : AsyncTask<Void?, Void?, Void?> {
    private val context: WeakReference<Activity?>
    private var id = -1
    private var form = -1
    private var done = false

    constructor(context: Activity?, id: Int, form: Int) {
        this.id = id
        this.form = form
        this.context = WeakReference(context)
    }

    constructor(context: Activity?, id: Int) {
        this.id = id
        this.context = WeakReference(context)
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val c = context.get() ?: return null

        val buffer = AddGIF.bos.toByteArray()
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
            val uri = Uri.fromFile(g)
            val mediaIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaIntent.data = uri
            c.sendBroadcast(mediaIntent)
            StaticStore.gifFrame = 0
            AddGIF.frame = 0
            AddGIF.encoder = AnimatedGifEncoder()
            AddGIF.bos = ByteArrayOutputStream()

            val shared = c.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

            when {
                shared.getInt("Orientation", 0) == 1 -> c.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                shared.getInt("Orientation", 0) == 2 -> c.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                shared.getInt("Orientation", 0) == 0 -> c.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }

            done = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    public override fun onPostExecute(result: Void?) {
        val c = context.get() ?: return

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
        StaticStore.enableGIF = false
    }
}