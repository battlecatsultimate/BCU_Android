package com.mandarin.bcu.androidutil.supports

import android.content.Context
import android.graphics.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.applyCanvas
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.fakeandroid.FIBM
import common.system.fake.FakeImage
import kotlin.math.roundToInt

class StageBitmapGenerator(c: Context, m: FONTMODE, texts: String) {
    enum class FONTMODE {
        EN,
        GLOBAL
    }

    private val font: Typeface
    private val fontPaint: Paint = Paint()
    private val fontOutline: Paint

    private val mode = m
    private val text = texts

    private var defaultTextHeight = 0f
    private var offset = 0f
    var default = false

    init {
        font = when (mode) {
            FONTMODE.EN -> {
                val tf = ResourcesCompat.getFont(c, R.font.stage_font)

                if(tf == null) {
                    default = true
                    Typeface.DEFAULT
                } else {

                    fontPaint.typeface = tf

                    for (element in text) {
                        val letter = element.toString()

                        if (!fontPaint.hasGlyph(letter)) {
                            default = true
                            Typeface.DEFAULT
                            break
                        }
                    }

                    tf
                }
            }
            else ->{
                default = true
                Typeface.DEFAULT_BOLD
            }
        }

        fontPaint.typeface = font

        fontOutline = Paint()
        fontOutline.typeface = font
        fontOutline.style = Paint.Style.STROKE

        offset = StaticStore.dptopx(6f, c).toFloat()
        defaultTextHeight = StaticStore.dptopx(40f, c).toFloat()

        fontPaint.textSize = StaticStore.dptopx(24f, c).toFloat()

        fontOutline.textSize = StaticStore.dptopx(24f, c).toFloat()
        fontOutline.strokeWidth = offset

        if(default) {
            fontPaint.shader = LinearGradient(0f, offset, 0f, defaultTextHeight + offset, intArrayOf(Color.rgb(255, 245, 0), Color.rgb(255, 245, 0), Color.rgb(236, 156, 0)), floatArrayOf(0f, 1f/5f, 4f/5f), Shader.TileMode.CLAMP)

            fontOutline.strokeCap = Paint.Cap.ROUND
            fontOutline.strokeJoin = Paint.Join.ROUND
        } else {
            fontPaint.shader = LinearGradient(0f, offset, 0f, defaultTextHeight + offset, intArrayOf(Color.rgb(255, 245, 0), Color.rgb(255, 245, 0), Color.rgb(236, 156, 0)), floatArrayOf(0f, 1f/5f, 3f/5f), Shader.TileMode.CLAMP)
        }
    }

    fun generateTextImage() : FakeImage {
        val w = fontOutline.measureText(text)
        val h = getTextHeight()

        val ratio = defaultTextHeight * 1f / h

        val resizer = h / 45f

        val result = Bitmap.createBitmap((w + offset * 2).roundToInt(), (h + offset * 2.5).roundToInt(), Bitmap.Config.ARGB_8888)

        result.applyCanvas {
            drawText(text, offset, h + offset, fontOutline)
            drawText(text, offset, h + offset, fontPaint)
        }

        return if(default && w > 255f * resizer) {
            val resized = StaticStore.getResizebp(result, 255f * resizer, h)

            FIBM.builder.build(StaticStore.getResizebp(resized, resized.width * ratio * 0.66f, defaultTextHeight))
        } else {
            if(default) {
                FIBM.builder.build(StaticStore.getResizebp(result, w * ratio * 0.66f, defaultTextHeight))
            } else {
                FIBM.builder.build(StaticStore.getResizebp(result, w * ratio, defaultTextHeight))
            }
        }
    }

    private fun getTextHeight() : Float {
        val fm = fontOutline.fontMetrics

        return fm.bottom - fm.top + fm.leading
    }
}