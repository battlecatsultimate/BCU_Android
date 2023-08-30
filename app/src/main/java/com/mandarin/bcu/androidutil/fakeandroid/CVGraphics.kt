package com.mandarin.bcu.androidutil.fakeandroid

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import common.system.fake.FakeGraphics
import common.system.fake.FakeImage
import common.system.fake.FakeTransform
import java.util.ArrayDeque

class CVGraphics : FakeGraphics {
    companion object {
        private val ftmt = ArrayDeque<FTMT>()

        @JvmStatic
        fun clear() {
            ftmt.clear()
        }

        private val negate = floatArrayOf(-1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f ,255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f)

        const val POSITIVE = 100

        private val src = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        private val add = PorterDuffXfermode(PorterDuff.Mode.ADD)
        private val multi = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        private val screen = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        private val darken = PorterDuffXfermode(PorterDuff.Mode.DARKEN)
    }

    private var c: Canvas

    /**
     * Used for solid color rendering such as oval, rect, etc.
     */
    private val colorPaint: Paint

    /**
     * Used for bitmap rendering
     */
    private val bitmapPaint: Paint

    /**
     * Used for gradient rendering
     */
    private val gradientPaint: Paint

    private val negative = ColorMatrixColorFilter(negate)

    private val m = Matrix()
    private val m2 = Matrix()

    private var color = 0
    @JvmField
    var neg = false
    var independent = false

    constructor(c: Canvas, colorPaint: Paint, bitmapPaint: Paint, night: Boolean) {
        this.c = c
        this.colorPaint = colorPaint
        this.bitmapPaint = bitmapPaint

        gradientPaint = Paint(colorPaint)

        color = if (night) Color.WHITE else Color.BLACK

        this.colorPaint.color = color

        this.bitmapPaint.apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        gradientPaint.style = Paint.Style.FILL
        gradientPaint.alpha = 255
    }

    constructor(c: Canvas, colorPaint: Paint, bitmapPaint: Paint, gradientPaint: Paint, night: Boolean) {
        this.c = c
        this.colorPaint = colorPaint
        this.bitmapPaint = bitmapPaint
        this.gradientPaint = gradientPaint

        color = if (night) Color.WHITE else Color.BLACK

        this.colorPaint.color = color

        this.bitmapPaint.apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        this.gradientPaint.style = Paint.Style.FILL
        this.gradientPaint.alpha = 255
    }

    fun setCanvas(c: Canvas) {
        this.c = c

        m.reset()
        m2.reset()
    }

    override fun drawImage(bimg: FakeImage, x: Double, y: Double) {
        if (bimg !is FIBM)
            return

        if (bimg.offsetLeft != 0 || bimg.offsetTop != 0) {
            c.drawBitmap(bimg.bimg(), (x - bimg.offsetLeft).toFloat(), (y - bimg.offsetTop).toFloat(), bitmapPaint)
        } else {
            c.drawBitmap(bimg.bimg(), x.toFloat(), y.toFloat(), bitmapPaint)
        }
    }

    override fun drawImage(bimg: FakeImage, x: Double, y: Double, d: Double, e: Double) {
        if (bimg !is FIBM || d * e == 0.0)
            return

        m2.reset()

        c.setMatrix(m2)

        m2.set(m)

        val wr = if (bimg.offsetLeft != 0) {
            (d + FIBM.CALIBRATOR) / bimg.width
        } else {
            d / bimg.width
        }

        val hr = if (bimg.offsetTop != 0) {
            (e + FIBM.CALIBRATOR) / bimg.height
        } else {
            e / bimg.height
        }

        if (bimg.offsetLeft != 0 || bimg.offsetTop != 0) {
            val calibrationX = if (bimg.offsetLeft == 0)
                0.0
            else
                bimg.offsetLeft + FIBM.CALIBRATOR / 2.0

            val calibrationY = if (bimg.offsetTop == 0)
                0.0
            else
                bimg.offsetTop + FIBM.CALIBRATOR / 2.0

            m2.preTranslate((x - calibrationX).toFloat(), (y - calibrationY).toFloat())
            m2.preScale(wr.toFloat(), hr.toFloat(), bimg.offsetLeft.toFloat(), bimg.offsetTop.toFloat())
        } else {
            m2.preTranslate(x.toFloat(), y.toFloat())
            m2.preScale(wr.toFloat(), hr.toFloat())
        }

        c.drawBitmap(bimg.bimg(), m2, bitmapPaint)
    }

    override fun drawLine(i: Int, j: Int, x: Int, y: Int) {
        c.drawLine(i.toFloat(), j.toFloat(), x.toFloat(), y.toFloat(), colorPaint)
    }

    override fun drawOval(i: Int, j: Int, k: Int, l: Int) {
        colorPaint.style = Paint.Style.STROKE

        c.drawOval(i.toFloat(), j.toFloat(), k.toFloat(), l.toFloat(), colorPaint)
    }

    override fun drawRect(x: Int, y: Int, x2: Int, y2: Int) {
        colorPaint.style = Paint.Style.STROKE
        c.drawRect(x.toFloat(), y.toFloat(), x + x2.toFloat(), y + y2.toFloat(), colorPaint)
    }

    override fun fillOval(i: Int, j: Int, k: Int, l: Int) {
        colorPaint.style = Paint.Style.FILL
        c.drawOval(i.toFloat(), j.toFloat(), k.toFloat(), l.toFloat(), colorPaint)
    }

    override fun fillRect(x: Int, y: Int, w: Int, h: Int) {
        colorPaint.style = Paint.Style.FILL
        c.drawRect(x.toFloat(), y.toFloat(), x + w.toFloat(), y + h.toFloat(), colorPaint)
    }

    @Synchronized
    override fun getTransform(): FakeTransform {
        if(independent)
            return FTMT(m)

        if (ftmt.isNotEmpty()) {
            val f = ftmt.pollFirst() ?: return FTMT(m)

            f.updateMatrix(m)

            return f
        }

        return FTMT(m)
    }

    override fun gradRect(x: Int, y: Int, w: Int, h: Int, a: Int, b: Int, c: IntArray, d: Int, e: Int, f: IntArray) {
        val s: Shader = LinearGradient(x.toFloat(), y.toFloat(), x.toFloat(), (y + h).toFloat(), Color.rgb(c[0], c[1], c[2]), Color.rgb(f[0], f[1], f[2]), Shader.TileMode.CLAMP)

        gradientPaint.shader = s

        this.c.drawRect(x.toFloat(), y.toFloat(), x + w.toFloat(), y + h.toFloat(), gradientPaint)
    }

    override fun gradRectAlpha(x: Int, y: Int, w: Int, h: Int, a: Int, b: Int, al: Int, c: IntArray, d: Int, e: Int, al2: Int, f: IntArray) {
        val s: Shader = LinearGradient(x.toFloat(), y.toFloat(), x.toFloat(), (y + h).toFloat(), Color.argb(al, c[0], c[1], c[2]), Color.argb(al2, f[0], f[1], f[2]), Shader.TileMode.CLAMP)

        gradientPaint.shader = s

        this.c.drawRect(x.toFloat(), y.toFloat(), x + w.toFloat(), y + h.toFloat(), gradientPaint)
    }

    override fun rotate(d: Double) {
        m.preRotate(Math.toDegrees(d).toFloat())
        c.setMatrix(m)
    }

    override fun scale(hf: Int, vf: Int) {
        m.preScale(hf.toFloat(), vf.toFloat())
        c.setMatrix(m)
    }

    override fun setColor(c: Int) {
        when (c) {
            FakeGraphics.RED -> {
                color = Color.RED
                colorPaint.color = Color.RED
            }
            FakeGraphics.YELLOW -> {
                color = Color.YELLOW
                colorPaint.color = Color.YELLOW
            }
            FakeGraphics.BLACK -> {
                color = Color.BLACK
                colorPaint.color = Color.BLACK
            }
            FakeGraphics.MAGENTA -> {
                color = Color.MAGENTA
                colorPaint.color = Color.MAGENTA
            }
            FakeGraphics.BLUE -> {
                color = Color.BLUE
                colorPaint.color = Color.BLUE
            }
            FakeGraphics.CYAN -> {
                color = Color.CYAN
                colorPaint.color = Color.CYAN
            }
            FakeGraphics.WHITE -> {
                color = Color.WHITE
                colorPaint.color = Color.WHITE
            }
            else -> {
                this.color = c
                colorPaint.color = c
            }
        }
    }

    override fun setColor(r: Int, g: Int, b: Int) {
        color = Color.rgb(r, g, b)
        colorPaint.color = Color.rgb(r, g, b)
    }

    override fun setComposite(mode: Int, p0: Int, p1: Int) {
        var alpha = p0

        if (alpha < 0)
            alpha = 0

        if (alpha > 255)
            alpha = 255

        when (mode) {
            FakeGraphics.DEF -> {
                bitmapPaint.xfermode = src
                bitmapPaint.alpha = 255
            }
            FakeGraphics.TRANS -> {
                bitmapPaint.xfermode = src
                bitmapPaint.alpha = alpha
            }
            FakeGraphics.BLEND -> {
                when (p1) {
                    0 -> {
                        bitmapPaint.xfermode = src
                        bitmapPaint.alpha = alpha
                    }
                    1 -> {
                        bitmapPaint.xfermode = add
                        bitmapPaint.alpha = alpha
                    }
                    2 -> {
                        bitmapPaint.xfermode = multi
                        bitmapPaint.alpha = alpha
                    }
                    3 -> {
                        bitmapPaint.xfermode = screen
                        bitmapPaint.alpha = alpha
                    }
                    -1 -> {
                        bitmapPaint.xfermode = darken
                        bitmapPaint.alpha = alpha
                    }
                }
            }
            FakeGraphics.GRAY -> {
                bitmapPaint.colorFilter = negative
                gradientPaint.colorFilter = negative
                neg = true
            }
            POSITIVE -> {
                bitmapPaint.colorFilter = null
                gradientPaint.colorFilter = null

                if(p0 == 1) {
                    neg = false
                }
            }
        }
    }

    override fun setRenderingHint(key: Int, `object`: Int) {}
    override fun setTransform(at: FakeTransform) {
        (at as FTMT).setMatrix(m)
        c.setMatrix(m)
    }

    override fun translate(x: Double, y: Double) {
        m.preTranslate(x.toFloat(), y.toFloat())
        c.setMatrix(m)
    }

    override fun colRect(x: Int, y: Int, w: Int, h: Int, r: Int, g: Int, b: Int, a: Int) {
        var a1 = a

        if (a1 < 0)
            a1 = 0

        if (a1 > 255)
            a1 = 255

        val rgba = Color.argb(a1, r, g, b)

        colorPaint.reset()
        colorPaint.color = rgba
        colorPaint.style = Paint.Style.FILL

        c.drawRect(x.toFloat(), y.toFloat(), x + w.toFloat(), y + h.toFloat(), colorPaint)
    }

    override fun delete(at: FakeTransform) {
        if(independent)
            return

        ftmt.add(at as FTMT)
    }
}