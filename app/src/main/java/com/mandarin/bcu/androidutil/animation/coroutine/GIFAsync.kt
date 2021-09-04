package com.mandarin.bcu.androidutil.animation.coroutine

import android.app.Activity
import android.content.pm.ActivityInfo
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.AnimatedGifEncoder
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.StaticStore.keepDoing
import com.mandarin.bcu.androidutil.animation.AnimationCView
import com.mandarin.bcu.androidutil.io.MediaScanner
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import common.pack.Identifier
import common.util.Data
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class GIFAsync : CoroutineTask<Void> {
    private val context: WeakReference<Activity?>
    private val type: Int
    private var form = -1
    private var done = false
    private val pack: String
    private val id: String

    private var result = ""

    constructor(context: Activity?, data: Identifier<*>, form: Int) : super() {
        this.form = form
        this.context = WeakReference(context)
        this.type = AnimationCView.UNIT
        this.pack = data.pack
        this.id = Data.trio(data.id)
    }

    constructor(context: Activity?, data: Identifier<*>) : super() {
        this.type = AnimationCView.ENEMY
        this.pack = data.pack
        this.id = Data.trio(data.id)
        this.context = WeakReference(context)
    }

    constructor(context: Activity?, type: Int, id: String) : super() {
        this.type = type
        this.pack = ""
        this.id = id
        this.context = WeakReference(context)
    }

    override fun prepare() {
    }

    override fun doSomething() {
        val c = context.get() ?: return

        val buffer = AddGIF.bos.toByteArray()

        val name = generateName()

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

    private fun generateName() : String {
        val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        val date = Date()

        when(type) {
            AnimationCView.UNIT -> {
                return dateFormat.format(date) + "-U-" + pack + "-" + id + "-" + form
            }
            AnimationCView.ENEMY -> {
                return dateFormat.format(date) + "-E-" + id
            }
            AnimationCView.EFFECT -> {
                return dateFormat.format(date) + "-EFF-" + id
            }
            AnimationCView.SOUL -> {
                return dateFormat.format(date) + "-S-" + id
            }
            AnimationCView.CANNON -> {
                return dateFormat.format(date) + "-C-" + id
            }
            AnimationCView.DEMONSOUL -> {
                return dateFormat.format(date) + "-DS-" + id
            }
            else -> {
                return dateFormat.format(date)
            }
        }
    }
}