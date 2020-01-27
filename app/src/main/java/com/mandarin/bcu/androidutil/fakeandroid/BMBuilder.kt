package com.mandarin.bcu.androidutil.fakeandroid

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import common.system.fake.FakeImage
import common.system.fake.ImageBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class BMBuilder : ImageBuilder() {
    override fun build(o: Any): FIBM? {
        if (o is Bitmap) return FIBM(o)
        if (o is FIBM) return o
        var b: Bitmap? = null
        if (o is File) b = BitmapFactory.decodeFile(o.absolutePath) else if (o is ByteArray) {
            b = BitmapFactory.decodeByteArray(o, 0, o.size)
        }
        return b?.let { FIBM(it) }
    }

    @Throws(IOException::class)
    override fun write(img: FakeImage, fmt: String, o: Any): Boolean {
        val b = img.bimg() as Bitmap
        if (o is File) {
            val os: OutputStream = FileOutputStream(o)
            return b.compress(Bitmap.CompressFormat.PNG, 100, os)
        }
        return if (o is OutputStream) b.compress(Bitmap.CompressFormat.PNG, 100, o) else false
    }
}