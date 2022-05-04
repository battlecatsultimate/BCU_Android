package com.mandarin.bcu.androidutil.supports

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics
import common.system.fake.FakeGraphics
import java.io.File

class StageBitmapGenerator(c: Context) {
    val font: Typeface
    val fontPainter: Paint

    init {
        val fontFile = StaticStore.getExternalFont(c)
        font = Typeface.createFromFile(File(fontFile, "enFont.ttf"))
        fontPainter = Paint()
        fontPainter.typeface = font
    }

    fun drawText(g: FakeGraphics, text: String) {
        val cv = g as CVGraphics


    }
}