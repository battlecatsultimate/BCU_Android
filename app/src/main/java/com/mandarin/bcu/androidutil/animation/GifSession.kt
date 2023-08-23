package com.mandarin.bcu.androidutil.animation

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.TextView
import com.mandarin.bcu.ImageViewer
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticJava
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics
import common.CommonStatic
import common.pack.Identifier
import common.system.P
import common.util.Data
import common.util.anim.EAnimD
import common.util.unit.AbEnemy
import common.util.unit.Enemy
import common.util.unit.Unit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GifSession(val recorder: ImageViewer.GifRecorder, private val type: AnimationCView.AnimationType, private val data: Any, private val pack: String, private val id: Int) {
    private val queuedTasks = ArrayDeque<Runnable>()
    private val watcher = Watcher()

    lateinit var bitmap: Bitmap
    private lateinit var cvGraphics: CVGraphics
    lateinit var animation: EAnimD<*>

    private var currentAnimationType = 0
    private var currentForm = 0

    private var ratio = 1.0
    private var night = false
    private var color = Color.WHITE
    private var boxColor = Color.WHITE

    fun startSession(ac: Activity) {
        initializeAnimation(StaticStore.animposition, StaticStore.formposition)

        recorder.checkValidClasses(data, type)

        val targetFPS = if (CommonStatic.getConfig().performanceMode) {
            60f
        } else {
            30f
        }

        if(recorder.encoder.frameRate != targetFPS) {
            recorder.encoder.frameRate = targetFPS

            recorder.encoder.start(recorder.bos)

            recorder.encoder.setRepeat(0)
        }

        val gif = ac.findViewById<TextView>(R.id.imgviewergiffr)

        StaticStore.setAppear(gif)

        val shared = ac.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        ratio = shared.getInt("gif", 100).toDouble() / 100.0
        night = !shared.getBoolean("theme", false)

        color = if(CommonStatic.getConfig().viewerColor != -1) {
            CommonStatic.getConfig().viewerColor
        } else {
            if (night) {
                Color.parseColor("#FF363636")
            } else {
                Color.WHITE
            }
        }

        boxColor = StaticStore.getAttributeColor(ac, R.attr.TextPrimary)

        recorder.frame = 0

        watcher.start()
    }

    fun pushFrame(view: AnimationCView, animationType: Int, form: Int, frame: Float) {
        queuedTasks.addLast {
            if (!StaticStore.keepDoing)
                return@addLast

            if (currentAnimationType != animationType || currentForm != form) {
                initializeAnimation(animationType, form)
            }

            animation.setTime(frame)

            val w = (view.width * ratio).toInt()
            val h = (view.height * ratio).toInt()
            val siz = view.size.toDouble() * ratio

            val p = P.newP((view.width.toFloat() / 2 + view.posx).toDouble(), (view.height.toFloat() * 2 / 3 + view.posy).toDouble()).apply {
                x *= ratio
                y *= ratio
            }

            if (!this::bitmap.isInitialized) {
                val bitmapPaint = Paint()
                val bp = Paint()
                val back = Paint()

                back.color = StaticStore.getAttributeColor(view.context, R.attr.backgroundPrimary)

                bitmapPaint.isFilterBitmap = true
                bitmapPaint.isAntiAlias = true

                bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val c = Canvas(bitmap)

                cvGraphics = CVGraphics(c, bitmapPaint, bp, night)
                cvGraphics.independent = true
            }

            cvGraphics.setColor(color)
            cvGraphics.fillRect(0, 0, w, h)

            cvGraphics.setColor(boxColor)

            animation.setTime(frame)
            animation.draw(cvGraphics, p, siz)

            recorder.encoder.addFrame(bitmap)

            recorder.frame++
        }
    }

    fun closeSession() {
        queuedTasks.addLast {
            recorder.saveGif(type, pack, Data.trio(id), Data.trio(currentForm))

            watcher.close()
        }
    }

    private fun initializeAnimation(animationType: Int, form: Int) {
        when(type) {
            AnimationCView.AnimationType.UNIT -> {
                val d = StaticStore.transformIdentifier<Unit>(data as Identifier<*>)

                if(d != null) {
                    val u = d.get()

                    animation = u.forms[form].getEAnim(StaticStore.getAnimType(animationType, u.forms[form].anim.anims.size))

                    currentForm = form
                    currentAnimationType = animationType
                } else {
                    throw IllegalStateException("Not an unit! : $data")
                }
            }
            AnimationCView.AnimationType.ENEMY -> {
                val d = StaticStore.transformIdentifier<AbEnemy>(data as Identifier<*>)

                if(d != null) {
                    val e = d.get()

                    if(e != null && e is Enemy) {
                        animation = e.getEAnim(StaticStore.getAnimType(animationType, e.anim.anims.size))

                        currentAnimationType = animationType
                    } else {
                        throw IllegalStateException("Not an enemy! : $data")
                    }
                } else {
                    throw IllegalStateException("Not an enemy! : $data")
                }
            }
            AnimationCView.AnimationType.EFFECT,
            AnimationCView.AnimationType.SOUL,
            AnimationCView.AnimationType.CANNON,
            AnimationCView.AnimationType.DEMON_SOUL -> {
                animation = StaticJava.generateEAnimD(data, animationType)

                currentAnimationType = animationType
            }
        }
    }

    private inner class Watcher {
        val viewScope = CoroutineScope(Dispatchers.IO)

        var canceled = false

        fun start() {
            canceled = false

            viewScope.launch {
                while(!canceled) {
                    if (queuedTasks.isNotEmpty()) {
                        queuedTasks.removeFirst().run()
                    }
                }
            }
        }

        fun close() {
            canceled = true
        }
    }
}