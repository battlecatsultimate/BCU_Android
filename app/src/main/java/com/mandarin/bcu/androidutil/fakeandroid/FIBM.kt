package com.mandarin.bcu.androidutil.fakeandroid

import android.graphics.Bitmap
import common.system.fake.FakeImage
import common.system.fake.ImageBuilder
import java.io.IOException

class FIBM internal constructor(read: Bitmap) : FakeImage {
    private val bit: Bitmap = read.copy(Bitmap.Config.ARGB_8888, true)
    override fun bimg(): Bitmap {
        return bit
    }

    override fun getHeight(): Int {
        return bit.height
    }

    override fun getWidth(): Int {
        return bit.width
    }

    override fun getRGB(i: Int, j: Int): Int {
        return bit.getPixel(i, j)
    }

    override fun getSubimage(i: Int, j: Int, k: Int, l: Int): FIBM? {
        return try {
            builder.build(Bitmap.createBitmap(bit, i, j, k, l)) as FIBM
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun gl(): Any? {
        return null
    }

    override fun setRGB(i: Int, j: Int, p: Int) {
        bit.setPixel(i, j, p)
    }

    companion object {
        val builder: ImageBuilder = BMBuilder()
        fun build(bimg2: Bitmap?): FakeImage? {
            return try {
                builder.build(bimg2)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

}