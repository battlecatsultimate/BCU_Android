package com.mandarin.bcu.androidutil.supports

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.lang.IllegalStateException

class ColorPickerView : View {
    enum class MODE {
        HUE,
        SATURATION,
        BRIGHTNESS,
        RED,
        GREEN,
        BLUE
    }

    private enum class DRAGMODE {
        NONE,
        FIELD,
        BAR
    }

    private val hsb = floatArrayOf(0f, 1f, 1f)
    private val rgb = intArrayOf(255, 0, 0)

    private var colorField = Bitmap.createBitmap(360, 360, Bitmap.Config.ARGB_8888)
    private var colorBar = Bitmap.createBitmap(360, 360, Bitmap.Config.ARGB_8888)

    private var circleX = 0
    private var circleY = 0

    private var barPos = 0

    private var dragMode = DRAGMODE.NONE
    private var mode = MODE.HUE

    private val p = Paint()

    constructor(context: Context) : super(context) {
        p.isAntiAlias = true
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        p.isAntiAlias = true
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        canvas ?: return

        val w = width
        val h = height


        //Portrait
        if(w < h) {
            val ihw = (w * 0.9).toInt()
        }
    }

    private fun updateField() {
        for(x in 0 until colorField.width) {
            for(y in 0 until colorField.height) {
                if(mode == MODE.RED || mode == MODE.GREEN || mode == MODE.BLUE) {
                    val c = when(mode) {
                        MODE.RED -> Color.rgb(rgb[0], (x * 255 / 360.0).toInt(), ((360 - y) * 255 / 360.0).toInt())
                        MODE.GREEN -> Color.rgb((x * 255 / 360.0).toInt(), rgb[1], ((360 - y) * 255 / 360.0).toInt())
                        MODE.BLUE -> Color.rgb((x * 255 / 360.0).toInt(), ((360 - y) * 255 / 360.0).toInt(), rgb[2])
                        else -> break
                    }

                    colorField.setPixel(x, y, c)
                } else {
                    val hsv = when(mode) {
                        MODE.HUE -> floatArrayOf(hsb[0], x / 360f, (360f - y) / 360f)
                        MODE.SATURATION -> floatArrayOf(x / 360f, hsb[1], (360f - y) / 360f)
                        MODE.BRIGHTNESS -> floatArrayOf(x / 360f, (360f - y) / 360f, hsb[2])
                        else -> break
                    }

                    colorField.setPixel(x, y, Color.HSVToColor(hsv))
                }
            }
        }
    }

    private fun updateBar() {
        for(y in 0 until colorBar.height) {
            if(mode == MODE.RED || mode == MODE.GREEN || mode == MODE.BLUE) {
                val c = when(mode) {
                    MODE.RED -> Color.rgb(((360 - y) * 255 / 360.0).toInt(), rgb[1], rgb[2])
                    MODE.GREEN -> Color.rgb(rgb[0], ((360 - y) * 255 / 360.0).toInt(), rgb[2])
                    MODE.BLUE -> Color.rgb(rgb[0], rgb[1], ((360 - y) * 255 / 360.0).toInt())
                    else -> break
                }

                for(x in 0 until colorBar.width)
                    colorBar.setPixel(x, y, c)
            } else {
                val hsv = when(mode) {
                    MODE.HUE -> floatArrayOf(1f - y / 360f, 1f, 1f)
                    MODE.SATURATION -> floatArrayOf(hsb[0], 1f - y / 360f, hsb[2])
                    MODE.BRIGHTNESS -> floatArrayOf(hsb[0], hsb[1], 1f - y / 360f)
                    else -> break
                }

                for(x in 0 until colorBar.width)
                    colorBar.setPixel(x, y, Color.HSVToColor(hsv))
            }
        }
    }

    private fun changeBarPos() {
        barPos = when(mode) {
            MODE.HUE -> ((1f - hsb[0]) * 360.0).toInt()
            MODE.SATURATION -> ((1f - hsb[1]) * 360.0).toInt()
            MODE.BRIGHTNESS -> ((1f - hsb[2]) * 360.0).toInt()
            MODE.RED -> ((255 - rgb[0]) * 360.0 / 255.0).toInt()
            MODE.GREEN -> ((255 - rgb[1]) * 360 / 255.0).toInt()
            MODE.BLUE -> ((255 - rgb[2]) * 360 / 255.0).toInt()
        }
    }

    private fun changeCirclePos() {
        when(mode) {
            MODE.HUE -> {
                circleX = (hsb[1] * 360).toInt()
                circleY = ((1 - hsb[2]) * 360).toInt()
            }
            MODE.SATURATION -> {
                circleX = (hsb[0] * 360).toInt()
                circleY = ((1 - hsb[2]) * 360).toInt()
            }
            MODE.BRIGHTNESS -> {
                circleX = (hsb[0] * 360).toInt()
                circleY = ((1 - hsb[1]) * 360).toInt()
            }
            MODE.RED -> {
                circleX = (rgb[1] * 360 / 255.0).toInt()
                circleY = ((255 - rgb[2]) * 360 / 255.0).toInt()
            }
            MODE.GREEN -> {
                circleX = (rgb[0] * 360 / 255.0).toInt()
                circleY = ((255 - rgb[2]) * 360 / 255.0).toInt()
            }
            MODE.BLUE -> {
                circleX = (rgb[0] * 360 / 255.0).toInt()
                circleY = ((255 - rgb[1]) * 360 / 255.0).toInt()
            }
        }
    }
}