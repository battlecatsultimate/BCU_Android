package com.mandarin.bcu.androidutil.animation.asynchs

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.AsyncTask
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.AnimatedGifEncoder
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.StaticStore.keepDoing
import com.mandarin.bcu.androidutil.io.MediaScanner
import common.pack.Identifier
import common.util.unit.AbEnemy
import common.util.unit.Unit
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class GIFAsync : AsyncTask<Void?, Void?, Void?> {
    private val context: WeakReference<Activity?>
    private val data: Identifier<*>
    private var form = -1
    private var done = false

    private var result = ""

    constructor(context: Activity?, data: Identifier<*>, form: Int) {
        this.form = form
        this.context = WeakReference(context)
        this.data = data
    }

    constructor(context: Activity?, data: Identifier<*>) {
        this.data = data
        this.context = WeakReference(context)
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val c = context.get() ?: return null

        val buffer = AddGIF.bos.toByteArray()

        val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        val date = Date()
        val name: String

        val d = if(form != -1) {
            StaticStore.transformIdentifier<Unit>(data)
        } else {
            StaticStore.transformIdentifier<AbEnemy>(data)
        } ?: return null

        name = if (form != -1) {
            dateFormat.format(date) + "-U-" + d.pack + "-" + d.id + "-" + form
        } else {
            dateFormat.format(date) + "-E-" + d.id
        }

        if(keepDoing) {
            try {
                result = MediaScanner.writeGIF(c, buffer, name)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

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

        return null
    }

    public override fun onPostExecute(result: Void?) {
        val c = context.get() ?: return

        if (done && keepDoing && this.result != MediaScanner.ERRR_WRONG_SDK)
            StaticStore.showShortMessage(c, c.getText(R.string.anim_png_success).toString().replace("-", this.result))
        else if (!keepDoing)
            StaticStore.showShortMessage(c, R.string.anim_gif_cancel)
        else
            StaticStore.showShortMessage(c, R.string.anim_png_fail)

        StaticStore.gifisSaving = false
        StaticStore.enableGIF = false
    }
}