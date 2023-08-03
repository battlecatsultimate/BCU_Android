package com.mandarin.bcu.androidutil.fakeandroid

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.applyCanvas
import com.mandarin.bcu.androidutil.StaticStore
import common.system.fake.FakeGraphics
import common.system.fake.FakeImage
import common.system.fake.ImageBuilder
import java.io.IOException

class FIBM : FakeImage {
    companion object {
        const val offset = 2

        @JvmField
        val builder: ImageBuilder<Bitmap> = BMBuilder()

        private val imagePaint = Paint().apply {
            isAntiAlias = false
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
    var appended: Boolean
        private set

    constructor() {
        bit = StaticStore.empty(1, 1)
        bit.recycle()
        appended = false
    }

    constructor(read: Bitmap) {
        bit = read.copy(Bitmap.Config.ARGB_8888, true)
        appended = false
    }

    constructor(read: Bitmap, appended: Boolean) {
        bit = read.copy(Bitmap.Config.ARGB_8888, true)
        this.appended = appended
    }

    override fun bimg(): Bitmap {
        return bit
    }

    override fun getHeight(): Int {
        return try {
            if (appended) {
                bit.height - offset * 2
            } else {
                bit.height
            }
        } catch(e: Exception) {
            0
        }
    }

    override fun getWidth(): Int {
        return try {
            if (appended) {
                bit.width - offset * 2
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
            val cropped = Bitmap.createBitmap(bit, i, j, k, l)

            val appended = Bitmap.createBitmap(cropped.width + offset * 2, cropped.height + offset * 2, Bitmap.Config.ARGB_8888)

            appended.applyCanvas {
                drawBitmap(cropped, offset.toFloat(), offset.toFloat(), imagePaint)
            }

            builder.build(appended, true) as FIBM
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

}