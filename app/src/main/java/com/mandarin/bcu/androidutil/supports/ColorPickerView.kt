package com.mandarin.bcu.androidutil.supports

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.sqrt

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
    private var colorBar = Bitmap.createBitmap(36, 360, Bitmap.Config.ARGB_8888)

    private var circleX = 0
    private var circleY = 0

    private var barPos = 0

    private var dragMode = DRAGMODE.NONE
    private var mode = MODE.HUE

    private val p = Paint()

    constructor(context: Context) : super(context) {
        p.isAntiAlias = true

        updateBar();
        updateField();

        changeCirclePos();
        changeBarPos();
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        p.isAntiAlias = true

        updateBar();
        updateField();

        changeCirclePos();
        changeBarPos();
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        canvas ?: return

        val w = width
        val h = height


        val ihw = if(w < h) {
            (w * 0.9).toInt()
        } else {
            (h * 0.9).toInt()
        }

        val iGap = if(w < h) {
            (w * 0.05).toInt()
        } else {
            (h * 0.05).toInt()
        }

        val gap = if(w < h) {
            (w * 0.075).toInt()
        } else {
            (h * 0.075).toInt()
        }
        val barH = (ihw / 10.0).toInt()
        val triangleSize = (gap / sqrt(3.0) * 4.0).toInt()
        val outerCircle = (ihw * 0.05 / 2.0).toInt()
        val innerCircle = (ihw * 0.0125 / 2.0).toInt()

        val cx = (circleX * ihw / 360.0).toInt()
        val cy = (circleY * ihw / 360.0).toInt()
        val bp = (barPos * ihw / 360.0).toInt()

        canvas.drawBitmap(colorField, null, Rect(iGap, iGap, iGap + ihw, iGap + ihw), p)

        if(w < h) {
            //Portrait
            canvas.drawBitmap(colorBar, null, Rect(iGap, iGap + ihw + gap, iGap + ihw, iGap + ihw + gap + barH), p)
        } else {
            //Landscape
            canvas.drawBitmap(colorBar, null, Rect(0, 0, barH, ihw), p)
        }

        p.color = getPointerColor()

        p.style = Paint.Style.STROKE
        p.strokeWidth = if(w < h) {
            w * 0.001f
        } else {
            h * 0.001f
        }

        canvas.drawCircle((iGap + cx - outerCircle).toFloat(), (iGap + cy - outerCircle).toFloat(), outerCircle.toFloat(), p)

        p.style = Paint.Style.FILL

        canvas.drawCircle((iGap + cx - innerCircle).toFloat(), (iGap + cy - innerCircle).toFloat(), innerCircle.toFloat(), p)

        if(w < h) {
            //Portrait
            canvas.drawLine((iGap + bp).toFloat(), (iGap + gap + ihw).toFloat(), (iGap + bp).toFloat(), (iGap + gap + ihw + barH).toFloat(), p)

            p.style = Paint.Style.FILL

            canvas.drawPoints(floatArrayOf(
                (iGap + bp).toFloat(), (iGap + gap + ihw).toFloat(),
                (iGap + (bp - triangleSize / 2.0)).toFloat(), (iGap + ihw + gap * 0.875).toFloat(),
                (iGap + (bp + triangleSize / 2.0)).toFloat(), (iGap + ihw + gap * 0.875).toFloat()
            ), p)

            canvas.drawPoints(floatArrayOf(
                (iGap + bp).toFloat(), (iGap + gap + ihw + barH).toFloat(),
                (iGap + (bp - triangleSize / 2.0)).toFloat(), (iGap + ihw + barH + gap * 1.125).toFloat(),
                (iGap + (bp + triangleSize / 2.0)).toFloat(), (iGap + ihw + barH + gap * 1.125).toFloat()
            ), p)

            p.color = Color.rgb(rgb[0], rgb[1], rgb[2])

            canvas.drawRect(Rect(iGap, iGap + ihw + gap + barH + gap, iGap * 2, barH), p)
        } else {
            //Landscape
            canvas.drawLine((iGap + gap + ihw).toFloat(), (iGap + bp).toFloat(), (iGap + gap + ihw + barH).toFloat(), (iGap + bp).toFloat(), p)

            p.style = Paint.Style.FILL

            canvas.drawPoints(floatArrayOf(
                (iGap + gap + ihw).toFloat(), (iGap + bp).toFloat(),
                (iGap + ihw + gap * 0.875).toFloat(), (iGap + (bp - triangleSize / 2.0)).toFloat(),
                (iGap + ihw + gap * 0.875).toFloat(), (iGap + (bp + triangleSize / 2.0)).toFloat()
            ), p)

            canvas.drawPoints(floatArrayOf(
                (iGap + gap + ihw + barH).toFloat(), (iGap + bp).toFloat(),
                (iGap + ihw + barH + gap * 1.125).toFloat(), (iGap + (bp - triangleSize / 2.0)).toFloat(),
                (iGap + ihw + barH + gap * 1.125).toFloat(), (iGap + (bp + triangleSize / 2.0)).toFloat()
            ), p)

            p.color = Color.rgb(rgb[0], rgb[1], rgb[2])

            canvas.drawRect(Rect(iGap + ihw + gap + barH + gap, iGap, barH, iGap * 2), p)
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

    private fun getPointerColor() : Int {
        val sum = ((rgb[0] + rgb[1] + rgb[2]) / 3.0).toInt()

        return if(sum > 128)
            0x141414
        else
            0xFFFFFF
    }
}