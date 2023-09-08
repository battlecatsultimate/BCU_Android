package com.mandarin.bcu.androidutil.fakeandroid

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.alpha
import androidx.core.graphics.applyCanvas
import com.mandarin.bcu.androidutil.StaticStore
import common.system.fake.FakeGraphics
import common.system.fake.FakeImage
import common.system.fake.ImageBuilder
import java.io.IOException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class FIBM : FakeImage {
    private enum class Snap {
        LEFT,
        RIGHT,
        BOTTOM,
        TOP
    }

    companion object {
        @JvmField
        val builder: ImageBuilder<Bitmap> = BMBuilder()

        const val CALIBRATOR = 0.75f

        private const val MAX_OFFSET = 5
        private const val CONNECTION_RATIO = 0.2f

        private val imagePaint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        fun build(bimg2: Bitmap?): FakeImage? {
            return try {
                builder.build(bimg2)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

    private val bit: Bitmap

    var offsetLeft = 0
        private set
    var offsetTop = 0
        private set
    private var offsetRight = 0
    private var offsetBottom = 0

    constructor() {
        bit = StaticStore.empty(1, 1)
        bit.recycle()
    }

    constructor(read: Bitmap) {
        bit = read.copy(Bitmap.Config.ARGB_8888, true)
    }

    override fun bimg(): Bitmap {
        return bit
    }

    override fun getHeight(): Int {
        return try {
            if (offsetTop != 0 || offsetBottom != 0) {
                bit.height - offsetTop - offsetBottom
            } else {
                bit.height
            }
        } catch(e: Exception) {
            0
        }
    }

    override fun getWidth(): Int {
        return try {
            if (offsetLeft != 0 || offsetRight != 0) {
                bit.width - offsetLeft - offsetRight
            } else {
                bit.width
            }
        } catch(e: Exception) {
            0
        }
    }

    override fun getRGB(i: Int, j: Int): Int {
        return try {
            bit.getPixel(i, j)
        } catch (e: Exception) {
            0
        }
    }

    override fun getSubimage(i: Int, j: Int, k: Int, l: Int): FIBM? {
        return try {
            val cropped = Bitmap.createBitmap(bit, max(0, i), max(0, j), max(1, k), max(1, l))

            val offsetLeft = if (isSegment(cropped, Snap.LEFT)) {
                0
            } else {
                scientificRound(min(MAX_OFFSET.toDouble(), k / 10.0))
            }

            val offsetRight = if (isSegment(cropped, Snap.RIGHT)) {
                0
            } else {
                scientificRound(min(MAX_OFFSET.toDouble(), k / 10.0))
            }

            val offsetTop = if (isSegment(cropped, Snap.TOP)) {
                0
            } else {
                scientificRound(min(MAX_OFFSET.toDouble(), l / 10.0))
            }

            val offsetBottom = if (isSegment(cropped, Snap.BOTTOM)) {
                0
            } else {
                scientificRound(min(MAX_OFFSET.toDouble(), l / 10.0))
            }

            if (offsetLeft + offsetRight + offsetTop + offsetBottom != 0) {
                val appended = Bitmap.createBitmap(cropped.width + offsetLeft + offsetRight, cropped.height + offsetTop + offsetBottom, Bitmap.Config.ARGB_8888).applyCanvas {
                    drawBitmap(cropped, offsetLeft.toFloat(), offsetTop.toFloat(), imagePaint)
                }

                val result = builder.build(appended) as FIBM

                result.offsetLeft = offsetLeft
                result.offsetTop = offsetTop
                result.offsetBottom = offsetBottom
                result.offsetRight = offsetRight

                result
            } else {
                builder.build(cropped) as FIBM
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun gl(): Any? {
        return null
    }

    override fun setRGB(i: Int, j: Int, p: Int) {
        try {
            bit.setPixel(i, j, p)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun isValid(): Boolean {
        return !bit.isRecycled
    }

    override fun unload() {
        bit.recycle()
    }

    override fun cloneImage(): FakeImage {
        val copy = if(bit.isRecycled)
            StaticStore.empty(1, 1)
        else
            bit.copy(bit.config, true)

        if(bit.isRecycled)
            copy.recycle()

        return FIBM(copy)
    }

    override fun getGraphics(): FakeGraphics {
        val p = Paint()

        p.isFilterBitmap = true

        return CVGraphics(Canvas(bit), p, Paint(), false)
    }

    private fun scientificRound(value: Double) : Int {
        val toInt = value.toInt()

        val decimals = value - toInt

        return if (decimals == 0.5) {
            if (toInt % 2 == 1)
                round(value).toInt()
            else
                toInt
        } else {
            round(value).toInt()
        }
    }

    private fun isSegment(image: Bitmap, snap: Snap) : Boolean {
        val w = image.width
        val h = image.height

        return when(snap) {
            Snap.LEFT -> {
                val pixels = IntArray(h)
                image.getPixels(pixels, 0, 1, 0, 0, 1, h)

                pixels.count { c -> c.alpha == 255 } >= h * CONNECTION_RATIO
            }
            Snap.RIGHT -> {
                val pixels = IntArray(h)
                image.getPixels(pixels, 0, 1, w - 1, 0, 1, h)

                pixels.count { c -> c.alpha == 255 } >= h * CONNECTION_RATIO
            }
            Snap.BOTTOM -> {
                val pixels = IntArray(w)
                image.getPixels(pixels, 0, w, 0, h - 1, w, 1)

                pixels.count { c -> c.alpha == 255 } >= w * CONNECTION_RATIO
            }
            Snap.TOP -> {
                val pixels = IntArray(w)
                image.getPixels(pixels, 0, w, 0, 0, w, 1)


                pixels.count { c -> c.alpha == 255 } >= w * CONNECTION_RATIO
            }
        }
    }
}