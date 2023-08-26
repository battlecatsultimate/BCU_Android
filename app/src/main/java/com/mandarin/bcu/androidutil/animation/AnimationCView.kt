package com.mandarin.bcu.androidutil.animation

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.mandarin.bcu.ImageViewer
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticJava
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics
import common.CommonStatic
import common.pack.Identifier
import common.system.P
import common.util.anim.EAnimD
import common.util.pack.DemonSoul
import common.util.pack.EffAnim
import common.util.pack.NyCastle
import common.util.pack.Soul
import common.util.unit.AbEnemy
import common.util.unit.Unit

@SuppressLint("ViewConstructor")
class AnimationCView(
    context: ImageViewer,
    data: Any,
    private val session: GifSession,
    val type: AnimationType,
    dataId: Int,
    night: Boolean,
    axis: Boolean,
) : View(context) {
    enum class AnimationType {
        UNIT,
        ENEMY,
        EFFECT,
        SOUL,
        CANNON,
        DEMON_SOUL
    }

    val activity = context
    val data: Any

    private val backgroundPaint = Paint()
    private val colorPaint = Paint()
    private val bitmapPaint = Paint()
    private val range = Paint()
    private val cv: CVGraphics

    @JvmField
    var anim: EAnimD<*>
    @JvmField
    var trans = false

    var fps: Long = 0
    var size = 1f
    var posx = 0f
    var posy = 0f
    var started = false

    private var p2: P
    private var animP = P(0.0, 0.0)
    init {
        when(type) {
            AnimationType.ENEMY -> {
                if(data !is Identifier<*> || !AbEnemy::class.java.isAssignableFrom(data.cls)) {
                    throw IllegalStateException("Invalid data type : ${data::class.java.name} in AnimationCView with type $type")
                }

                this.data = data
            }
            AnimationType.UNIT -> {
                if(data !is Identifier<*> || !Unit::class.java.isAssignableFrom(data.cls)) {
                    throw IllegalStateException("Invalid data type : ${data::class.java.name} in AnimationCView with type $type")
                }

                this.data = data
            }
            AnimationType.EFFECT -> {
                if(data !is EffAnim<*>)
                    throw IllegalStateException("Invalid data type : ${data::class.java.name} in AnimationCView with type $type")

                this.data = data
            }
            AnimationType.SOUL -> {
                if(data !is Soul)
                    throw IllegalStateException("Invalid data type : ${data::class.java.name} in AnimationCView with type $type")

                this.data = data
            }
            AnimationType.CANNON -> {
                if(data !is NyCastle)
                    throw IllegalStateException("Invalid data type : ${data::class.java.name} in AnimationCView with type $type")

                this.data = data
            }
            AnimationType.DEMON_SOUL -> {
                if(data !is DemonSoul)
                    throw IllegalStateException("Invalid data type : ${data::class.java.name} in AnimationCView with type $type")

                this.data = data
            }
        }

        this.anim = if (type == AnimationType.UNIT) {
            StaticJava.generateEAnimD(data, dataId, 0)
        } else {
            StaticJava.generateEAnimD(data , 0, 0)
        }

        CommonStatic.getConfig().ref = axis

        if(CommonStatic.getConfig().viewerColor != -1) {
            backgroundPaint.color = CommonStatic.getConfig().viewerColor
            range.color = 0xFFFFFF - CommonStatic.getConfig().viewerColor
        } else {
            if (night) {
                backgroundPaint.color = 0x363636
            } else {
                backgroundPaint.color = Color.WHITE
            }

            range.color = StaticStore.getAttributeColor(context, R.attr.TextPrimary)
        }

        colorPaint.isFilterBitmap = true
        p2 = P((width.toFloat() / 2).toDouble(), (height.toFloat() * 2f / 3f).toDouble())

        cv = CVGraphics(Canvas(), colorPaint, bitmapPaint, night)

        StaticStore.keepDoing = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        started = true
    }

    override fun onDraw(canvas: Canvas) {
        if (StaticStore.gifisSaving && !StaticStore.keepDoing) {
            StaticStore.keepDoing = true
        }

        if (StaticStore.enableGIF) {
            animP = P.newP((width.toFloat() / 2 + posx).toDouble(), (height.toFloat() * 2 / 3 + posy).toDouble())

            session.pushFrame(this, StaticStore.animposition, StaticStore.formposition, anim.f)

            StaticStore.gifFrame++
        }

        if (StaticStore.play) {
            p2 = P.newP(width / 2.0 + posx, height * 2.0 / 3 + posy)

            cv.setCanvas(canvas)

            if (!trans)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

            cv.setColor(range.color)

            anim.draw(cv, p2, size.toDouble())
            anim.update(true)

            if (CommonStatic.getConfig().performanceModeAnimation) {
                StaticStore.frame += 0.5f
            } else {
                StaticStore.frame++
            }

            P.delete(p2)
        } else {
            p2 = P.newP(width / 2.0 + posx, height * 2.0 / 3 + posy)
            
            cv.setCanvas(canvas)

            if (!trans)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

            cv.setColor(range.color)

            anim.draw(cv, p2, size.toDouble())

            P.delete(p2)
        }
    }
}