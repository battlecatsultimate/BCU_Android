package com.mandarin.bcu.androidutil.fakeandroid

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import common.system.fake.FakeImage
import common.system.fake.ImageBuilder
import java.io.*
import java.util.function.Supplier

class BMBuilder : ImageBuilder<Bitmap>() {
    companion object {
        val option = BitmapFactory.Options().apply {
            inScaled = true
            inDensity = inTargetDensity
        }
    }

    @Throws(IOException::class)
    override fun write(img: FakeImage, fmt: String, o: Any): Boolean {
        val b = img.bimg() as Bitmap

        if (o is File) {
            val os: OutputStream = FileOutputStream(o)
            return b.compress(Bitmap.CompressFormat.PNG, 100, os)
        }

        return if (o is OutputStream)
            b.compress(Bitmap.CompressFormat.PNG, 100, o)
        else
            false
    }

    override fun build(f: File?): FakeImage {
        f ?: return FIBM()

        val b = BitmapFactory.decodeFile(f.absolutePath, option)

        return FIBM(b)
    }

    override fun build(sup: Supplier<InputStream>?): FakeImage {
        sup ?: return FIBM()

        val ins = sup.get()

        val b = BitmapFactory.decodeStream(ins, null, option) ?: return FIBM()

        return FIBM(b)
    }

    override fun build(o: Bitmap?): FakeImage {
        o ?: return FIBM()

        return FIBM(o)
    }

    override fun build(w: Int, h: Int): FakeImage {
        return FIBM(Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888))
    }
}