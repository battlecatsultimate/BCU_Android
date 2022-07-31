package com.mandarin.bcu.androidutil.fakeandroid

import android.graphics.*
import common.system.fake.FakeGraphics
import common.system.fake.FakeImage
import common.system.fake.FakeTransform
import java.util.*

class CVGraphics : FakeGraphics {
    companion object {
        private val ftmt: Deque<FTMT> = ArrayDeque()
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
    private val cp: Paint
    private val bp: Paint
    private val gp: Paint
    private val negative = ColorMatrixColorFilter(negate)
    private val m = Matrix()
    private val m2 = Matrix()
    private var color = 0
    @JvmField
    var neg = false
    var independent = false

    constructor(c: Canvas, cp: Paint, bp: Paint, night: Boolean) {
        this.c = c
        this.cp = cp
        this.bp = bp
        gp = cp
        color = if (night) Color.WHITE else Color.BLACK
        this.cp.color = color
        this.bp.isFilterBitmap = true
        this.bp.isAntiAlias = true
        gp.style = Paint.Style.FILL
        gp.alpha = 255
    }

    constructor(c: Canvas, cp: Paint, bp: Paint, gp: Paint, night: Boolean) {
        this.c = c
        this.cp = cp
        this.bp = bp
        this.gp = gp
        color = if (night) Color.WHITE else Color.BLACK
        this.cp.color = color
        this.bp.isFilterBitmap = true
        this.bp.isAntiAlias = true
        this.gp.style = Paint.Style.FILL
        this.gp.alpha = 255
    }

    fun setCanvas(c: Canvas) {
        this.c = c
    }

    override fun drawImage(bimg: FakeImage, x: Double, y: Double) {
        val b = bimg.bimg() as Bitmap
        c.drawBitmap(b, x.toFloat(), y.toFloat(), bp)
    }

    override fun drawImage(bimg: FakeImage, x: Double, y: Double, d: Double, e: Double) {
        val b = bimg.bimg() as Bitmap
        m2.reset()
        c.setMatrix(m2)
        val w = b.width.toFloat()
        val h = b.height.toFloat()
        val wr = d.toFloat() / w
        val hr = e.toFloat() / h
        m2.set(m)
        m2.preTranslate(x.toFloat(), y.toFloat())
        m2.preScale(wr, hr)
        c.drawBitmap(b, m2, bp)
    }

    override fun drawLine(i: Int, j: Int, x: Int, y: Int) {
        c.drawLine(i.toFloat(), j.toFloat(), x.toFloat(), y.toFloat(), cp)
    }

    override fun drawOval(i: Int, j: Int, k: Int, l: Int) {
        cp.style = Paint.Style.STROKE
        c.drawOval(i.toFloat(), j.toFloat(), k.toFloat(), l.toFloat(), cp)
    }

    override fun drawRect(x: Int, y: Int, x2: Int, y2: Int) {
        cp.style = Paint.Style.STROKE
        c.drawRect(x.toFloat(), y.toFloat(), x + x2.toFloat(), y + y2.toFloat(), cp)
    }

    override fun fillOval(i: Int, j: Int, k: Int, l: Int) {
        cp.style = Paint.Style.FILL
        c.drawOval(i.toFloat(), j.toFloat(), k.toFloat(), l.toFloat(), cp)
    }

    override fun fillRect(x: Int, y: Int, w: Int, h: Int) {
        cp.style = Paint.Style.FILL
        c.drawRect(x.toFloat(), y.toFloat(), x + w.toFloat(), y + h.toFloat(), cp)
    }

    @Synchronized
    override fun getTransform(): FakeTransform {
        if(independent)
            return FTMT(m)

        if (!ftmt.isEmpty()) {
            val f = ftmt.pollFirst() ?: return FTMT(m)

            f.updateMatrix(m)

            return f
        }
        return FTMT(m)
    }

    override fun gradRect(x: Int, y: Int, w: Int, h: Int, a: Int, b: Int, c: IntArray, d: Int, e: Int, f: IntArray) {
        val s: Shader = LinearGradient(x.toFloat(), y.toFloat(), x.toFloat(), (y + h).toFloat(), Color.rgb(c[0], c[1], c[2]), Color.rgb(f[0], f[1], f[2]), Shader.TileMode.CLAMP)

        gp.shader = s

        this.c.drawRect(x.toFloat(), y.toFloat(), x + w.toFloat(), y + h.toFloat(), gp)
    }

    override fun gradRectAlpha(x: Int, y: Int, w: Int, h: Int, a: Int, b: Int, al: Int, c: IntArray, d: Int, e: Int, al2: Int, f: IntArray) {
        val s: Shader = LinearGradient(x.toFloat(), y.toFloat(), x.toFloat(), (y + h).toFloat(), Color.argb(al, c[0], c[1], c[2]), Color.argb(al2, f[0], f[1], f[2]), Shader.TileMode.CLAMP)

        gp.shader = s

        this.c.drawRect(x.toFloat(), y.toFloat(), x + w.toFloat(), y + h.toFloat(), gp)
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
                cp.color = Color.RED
            }
            FakeGraphics.YELLOW -> {
                color = Color.YELLOW
                cp.color = Color.YELLOW
            }
            FakeGraphics.BLACK -> {
                color = Color.BLACK
                cp.color = Color.BLACK
            }
            FakeGraphics.MAGENTA -> {
                color = Color.MAGENTA
                cp.color = Color.MAGENTA
            }
            FakeGraphics.BLUE -> {
                color = Color.BLUE
                cp.color = Color.BLUE
            }
            FakeGraphics.CYAN -> {
                color = Color.CYAN
                cp.color = Color.CYAN
            }
            FakeGraphics.WHITE -> {
                color = Color.WHITE
                cp.color = Color.WHITE
            }
        }
    }

    override fun setColor(r: Int, g: Int, b: Int) {
        color = Color.rgb(r, g, b)
        cp.color = Color.rgb(r, g, b)
    }

    override fun setComposite(mode: Int, p0: Int, p1: Int) {
        var alpha = p0
        if (alpha < 0) alpha = 0
        if (alpha > 255) alpha = 255
        when (mode) {
            FakeGraphics.DEF -> {
                bp.xfermode = src
                bp.alpha = 255
            }
            FakeGraphics.TRANS -> {
                bp.xfermode = src
                bp.alpha = alpha
            }
            FakeGraphics.BLEND -> {
                when (p1) {
                    0 -> {
                        bp.xfermode = src
                        bp.alpha = alpha
                    }
                    1 -> {
                        bp.xfermode = add
                        bp.alpha = alpha
                    }
                    2 -> {
                        bp.xfermode = multi
                        bp.alpha = alpha
                    }
                    3 -> {
                        bp.xfermode = screen
                        bp.alpha = alpha
                    }
                    -1 -> {
                        bp.xfermode = darken
                        bp.alpha = alpha
                    }
                }
            }
            FakeGraphics.GRAY -> {
                bp.colorFilter = negative
                gp.colorFilter = negative
                neg = true
            }
            POSITIVE -> {
                bp.colorFilter = null
                gp.colorFilter = null

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
        if (a1 < 0) a1 = 0
        if (a1 > 255) a1 = 255
        val rgba = Color.argb(a1, r, g, b)
        cp.reset()
        cp.color = rgba
        cp.style = Paint.Style.FILL
        c.drawRect(x.toFloat(), y.toFloat(), x + w.toFloat(), y + h.toFloat(), cp)
    }

    override fun delete(at: FakeTransform) {
        if(independent) return

        ftmt.add(at as FTMT)
    }
}