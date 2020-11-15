package com.mandarin.bcu.androidutil.animation.coroutine

import android.app.Activity
import android.content.pm.ActivityInfo
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.AnimatedGifEncoder
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.StaticStore.keepDoing
import com.mandarin.bcu.androidutil.io.MediaScanner
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import common.pack.Identifier
import common.util.unit.AbEnemy
import common.util.unit.Unit
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class GIFAsync : CoroutineTask<Void> {
    private val context: WeakReference<Activity?>
    private val data: Identifier<*>
    private var form = -1
    private var done = false

    private var result = ""

    constructor(context: Activity?, data: Identifier<*>, form: Int) : super() {
        this.form = form
        this.context = WeakReference(context)
        this.data = data
    }

    constructor(context: Activity?, data: Identifier<*>) : super() {
        this.data = data
        this.context = WeakReference(context)
    }

    override fun prepare() {
    }

    override fun doSomething() {
        val c = context.get() ?: return

        val buffer = AddGIF.bos.toByteArray()

        val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        val date = Date()
        val name: String

        val d = if(form != -1) {
            StaticStore.transformIdentifier<Unit>(data)
        } else {
            StaticStore.transformIdentifier<AbEnemy>(data)
        } ?: return

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

        c.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        done = true
    }

    override fun finish() {
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