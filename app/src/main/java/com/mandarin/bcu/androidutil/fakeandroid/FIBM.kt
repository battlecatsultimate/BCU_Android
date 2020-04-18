package com.mandarin.bcu.androidutil.fakeandroid

import android.graphics.Bitmap
import com.mandarin.bcu.androidutil.StaticStore
import common.system.fake.FakeImage
import common.system.fake.ImageBuilder
import java.io.IOException

class FIBM : FakeImage {
    private val bit: Bitmap

    @JvmField
    var reference: String = ""
    @JvmField
    var password: String = ""

    constructor(read: Bitmap) {
        bit = read.copy(Bitmap.Config.ARGB_8888, true)
    }

    constructor(ref: String) {
        bit = StaticStore.empty(1,1)
        bit.recycle()
        reference = ref
    }

    override fun bimg(): Bitmap {
        return bit
    }

    override fun getHeight(): Int {
        return try {
            bit.height
        } catch(e: Exception) {
            0
        }
    }

    override fun getWidth(): Int {
        return try {
            bit.width
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
        try {
            bit.setPixel(i, j, p)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun unload() {
        bit.recycle()
    }

    companion object {
        @JvmField
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