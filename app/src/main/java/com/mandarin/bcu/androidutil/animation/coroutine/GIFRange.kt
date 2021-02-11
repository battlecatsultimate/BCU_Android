package com.mandarin.bcu.androidutil.animation.coroutine

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.AnimatedGifEncoder
import com.mandarin.bcu.androidutil.StaticJava
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.animation.AnimationCView
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import common.pack.Identifier
import common.system.P
import common.util.Data
import common.util.anim.EAnimD
import common.util.pack.EffAnim
import common.util.pack.NyCastle
import common.util.pack.Soul
import common.util.unit.AbEnemy
import common.util.unit.Enemy
import common.util.unit.Unit
import java.io.ByteArrayOutputStream
import java.lang.IllegalStateException
import java.lang.ref.WeakReference

class GIFRange : CoroutineTask<String> {
    private val we: WeakReference<Activity>
    val form: Int
    val data: Any
    val type: Int
    val index: Int

    val w: Int
    val h: Int
    val siz: Double
    val p: P
    val night: Boolean

    val frames: ArrayList<Array<Int>>
    val enables: Array<Boolean>

    constructor(ac: Activity, data: Identifier<Unit>, form: Int, view: AnimationCView, night: Boolean, frames: ArrayList<Array<Int>>, enables: Array<Boolean>) : super() {
        we = WeakReference(ac)
        this.form = form
        this.data = data
        this.type = AnimationCView.UNIT
        this.index = -1

        val shared = ac.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        val ratio = shared.getInt("gif", 100).toDouble() / 100.0

        this.w = (view.width * ratio).toInt()
        this.h = (view.height * ratio).toInt()
        this.siz = view.size.toDouble() * ratio
        this.p = P.newP((view.width.toFloat() / 2 + view.posx).toDouble(), (view.height.toFloat() * 2 / 3 + view.posy).toDouble())
        this.p.x *= ratio
        this.p.y *= ratio
        this.night = night

        this.frames = frames
        this.enables = enables

        if(AddGIF.encoder.frameRate != 30f) {
            AddGIF.encoder.frameRate = 30f
            AddGIF.encoder.start(AddGIF.bos)
            AddGIF.encoder.setRepeat(0)
        }

        StaticStore.fixOrientation(ac)
    }

    constructor(ac: Activity, data: Identifier<AbEnemy>, view: AnimationCView, night: Boolean, frames: ArrayList<Array<Int>>, enables: Array<Boolean>) : super() {
        we = WeakReference(ac)

        this.form = -1
        this.data = data
        this.type = AnimationCView.ENEMY
        this.index = -1

        val shared = ac.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        val ratio = shared.getInt("gif", 100).toDouble() / 100.0

        this.w = (view.width * ratio).toInt()
        this.h = (view.height * ratio).toInt()
        this.siz = view.size.toDouble() * ratio
        this.p = P.newP((view.width.toFloat() / 2 + view.posx).toDouble(), (view.height.toFloat() * 2 / 3 + view.posy).toDouble())
        this.p.x *= ratio
        this.p.y *= ratio
        this.night = night

        this.frames = frames
        this.enables = enables

        if(AddGIF.encoder.frameRate != 30f) {
            AddGIF.encoder.frameRate = 30f
            AddGIF.encoder.start(AddGIF.bos)
            AddGIF.encoder.setRepeat(0)
        }

        StaticStore.fixOrientation(ac)
    }

    constructor(ac: Activity, data: Any, type: Int, index: Int, view: AnimationCView, night: Boolean, frames: ArrayList<Array<Int>>, enables: Array<Boolean>) : super() {
        we = WeakReference(ac)

        this.form = -1
        this.data = data
        this.type = type
        this.index = index

        val shared = ac.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        val ratio = shared.getInt("gif", 100).toDouble() / 100.0

        this.w = (view.width * ratio).toInt()
        this.h = (view.height * ratio).toInt()
        this.siz = view.size.toDouble() * ratio
        this.p = P.newP((view.width.toFloat() / 2 + view.posx).toDouble(), (view.height.toFloat() * 2 / 3 + view.posy).toDouble())
        this.p.x *= ratio
        this.p.y *= ratio
        this.night = night

        this.frames = frames
        this.enables = enables

        checkValidClasses()

        if(AddGIF.encoder.frameRate != 30f) {
            AddGIF.encoder.frameRate = 30f
            AddGIF.encoder.start(AddGIF.bos)
            AddGIF.encoder.setRepeat(0)
        }

        StaticStore.fixOrientation(ac)
    }
    override fun prepare() {
        AddGIF.frame = 0
        StaticStore.gifFrame = getTotalFrame()
    }

    override fun doSomething() {
        if(!StaticStore.keepDoing) {
            AddGIF.encoder = AnimatedGifEncoder()
            AddGIF.bos = ByteArrayOutputStream()
            AddGIF.frame = 0
            StaticStore.gifFrame = 0
            StaticStore.gifisSaving = false
            AnimationCView.gifTask.clear()
            return
        }

        val ac = we.get() ?: return

        for(i in frames.indices) {
            if(!StaticStore.keepDoing) {
                break
            }

            if(enables[i]) {
                val range = frames[i]
                val anim = getEanimD(i)

                val p1 = Paint()
                val bp = Paint()
                val back = Paint()

                back.color = StaticStore.getAttributeColor(ac, R.attr.backgroundPrimary)

                p1.isFilterBitmap = true
                p1.isAntiAlias = true

                for(j in range[0]..range[1]) {
                    if(!StaticStore.keepDoing) {
                        break
                    }

                    val b = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
                    val c = Canvas(b)

                    val cv = CVGraphics(c, p1, bp, night)
                    cv.independent = true

                    c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), back)

                    anim.setTime(j)
                    anim.draw(cv, p, siz)

                    AddGIF.encoder.addFrame(b)

                    AddGIF.frame++
                }
            }
        }
    }

    override fun finish() {
        AddGIF.encoder.finish()

        when(type) {
            AnimationCView.UNIT -> {
                GIFAsync(we.get(), data as Identifier<*>, form).execute()
            }
            AnimationCView.ENEMY -> {
                GIFAsync(we.get(), data as Identifier<*>).execute()
            }
            AnimationCView.EFFECT,
            AnimationCView.SOUL,
            AnimationCView.CANNON -> {
                GIFAsync(we.get(), type, Data.trio(index)).execute()
            }
        }
    }

    private fun checkValidClasses() {
        when(this.data) {
            is Soul -> {
                if(type != AnimationCView.SOUL)
                    throw IllegalStateException("Invalid data ${data::class.java.name} with type $type")
            }
            is EffAnim<*> -> {
                if(type != AnimationCView.EFFECT)
                    throw IllegalStateException("Invalid data ${data::class.java.name} with type $type")
            }
            is NyCastle -> {
                if(type != AnimationCView.CANNON)
                    throw IllegalStateException("Invalid data ${data::class.java.name} with type $type")
            }
            else -> {
                throw IllegalStateException("Invalid data ${data::class.java.name} with type $type")
            }
        }
    }

    private fun getEanimD(ind: Int) : EAnimD<*> {
        when(type) {
            AnimationCView.UNIT -> {
                val u = Identifier.get(data as Identifier<*>)

                return (u as Unit).forms[form].getEAnim(StaticStore.getAnimType(ind, u.forms[form].anim.anims.size))
            }
            AnimationCView.ENEMY -> {
                val e = Identifier.get(data as Identifier<*>)

                return (e as Enemy).getEAnim(StaticStore.getAnimType(ind, e.anim.anims.size))
            }
            AnimationCView.EFFECT -> {
                val d = data as EffAnim<*>

                return StaticJava.generateEAnimD(d, ind)
            }
            AnimationCView.SOUL -> {
                val d = data as Soul

                return StaticJava.generateEAnimD(d, ind)
            }
            AnimationCView.CANNON -> {
                val d = data as NyCastle

                return StaticJava.generateEAnimD(d, ind)
            }
            else -> {
                throw IllegalStateException("Invalid type $type")
            }
        }
    }

    private fun getTotalFrame() : Int {
        var res = 0

        for(i in frames.indices) {
            if(enables[i]) {
                res += frames[i][1] - frames[i][0] + 1
            }
        }

        return res
    }
}