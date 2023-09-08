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

    override fun drawImage(bimg: FakeImage, x: Float, y: Float) {
        if (bimg !is FIBM)
            return

        if (bimg.offsetLeft != 0 || bimg.offsetTop != 0) {
            c.drawBitmap(bimg.bimg(), x - bimg.offsetLeft, y - bimg.offsetTop, bitmapPaint)
        } else {
            c.drawBitmap(bimg.bimg(), x, y, bitmapPaint)
        }
    }

    override fun drawImage(bimg: FakeImage, x: Float, y: Float, d: Float, e: Float) {
        if (bimg !is FIBM || d * e == 0f)
            return

        m2.reset()

        c.setMatrix(m2)

        m2.set(m)

        val wr = d / bimg.width
        val hr = e / bimg.height

        if (bimg.offsetLeft != 0 || bimg.offsetTop != 0) {
            val calibrationX = if (bimg.offsetLeft == 0)
                0f
            else
                bimg.offsetLeft.toFloat()

            val calibrationY = if (bimg.offsetTop == 0)
                0f
            else
                bimg.offsetTop.toFloat()

            m2.preTranslate(x - calibrationX, y - calibrationY)
            m2.preScale(wr, hr, bimg.offsetLeft.toFloat(), bimg.offsetTop.toFloat())
        } else {
            m2.preTranslate(x, y)
            m2.preScale(wr, hr)
        }

        c.drawBitmap(bimg.bimg(), m2, bitmapPaint)
    }

    override fun drawLine(i: Float, j: Float, x: Float, y: Float) {
        c.drawLine(i, j, x, y, colorPaint)
    }

    override fun drawOval(i: Float, j: Float, k: Float, l: Float) {
        colorPaint.style = Paint.Style.STROKE

        c.drawOval(i, j, k, l, colorPaint)
    }

    override fun drawRect(x: Float, y: Float, x2: Float, y2: Float) {
        colorPaint.style = Paint.Style.STROKE
        c.drawRect(x, y, x + x2, y + y2, colorPaint)
    }

    override fun fillOval(i: Float, j: Float, k: Float, l: Float) {
        colorPaint.style = Paint.Style.FILL
        c.drawOval(i, j, k, l, colorPaint)
    }

    override fun fillRect(x: Float, y: Float, w: Float, h: Float) {
        colorPaint.style = Paint.Style.FILL
        c.drawRect(x, y, x + w, y + h, colorPaint)
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

    override fun gradRect(x: Float, y: Float, w: Float, h: Float, a: Float, b: Float, c: IntArray, d: Float, e: Float, f: IntArray) {
        val s: Shader = LinearGradient(x, y, x, (y + h), Color.rgb(c[0], c[1], c[2]), Color.rgb(f[0], f[1], f[2]), Shader.TileMode.CLAMP)

        gradientPaint.shader = s

        this.c.drawRect(x, y, x + w, y + h, gradientPaint)
    }

    override fun gradRectAlpha(x: Float, y: Float, w: Float, h: Float, a: Float, b: Float, al: Int, c: IntArray, d: Float, e: Float, al2: Int, f: IntArray) {
        val s: Shader = LinearGradient(x, y, x, (y + h), Color.argb(al, c[0], c[1], c[2]), Color.argb(al2, f[0], f[1], f[2]), Shader.TileMode.CLAMP)

        gradientPaint.shader = s

        this.c.drawRect(x, y, x + w, y + h, gradientPaint)
    }

    override fun rotate(d: Float) {
        m.preRotate(Math.toDegrees(d.toDouble()).toFloat())
        c.setMatrix(m)
    }

    override fun scale(hf: Float, vf: Float) {
        m.preScale(hf, vf)
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

    override fun translate(x: Float, y: Float) {
        m.preTranslate(x, y)
        c.setMatrix(m)
    }

    override fun colRect(x: Float, y: Float, w: Float, h: Float, r: Int, g: Int, b: Int, a: Int) {
        var a1 = a

        if (a1 < 0)
            a1 = 0

        if (a1 > 255)
            a1 = 255

        val rgba = Color.argb(a1, r, g, b)

        colorPaint.reset()
        colorPaint.color = rgba
        colorPaint.style = Paint.Style.FILL

        c.drawRect(x, y, x + w, y + h, colorPaint)
    }

    override fun delete(at: FakeTransform) {
        if(independent)
            return

        ftmt.add(at as FTMT)
    }
}