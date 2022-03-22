package com.mandarin.bcu.androidutil.supports

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
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
    private var mode = MODE.RED

    private val p = Paint()

    constructor(context: Context) : super(context) {
        p.isAntiAlias = true

        updateBar()
        updateField()

        changeCirclePos()
        changeBarPos()

        setOnTouchListener { _, motionEvent ->
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

            val filteredX = min(360, max(0, ((motionEvent.x - iGap) * 360f / ihw).toInt()))
            val filteredY = min(360, max(0, ((ihw - (motionEvent.y - iGap)) * 360f / ihw).toInt()))

            if(motionEvent.action == MotionEvent.ACTION_DOWN) {

                println("iGap : $iGap | ihw : $ihw | barH : $barH | X : ${motionEvent.x} | Y : ${motionEvent.y}")

                if(iGap <= motionEvent.x && motionEvent.x <= iGap + ihw && iGap <= motionEvent.y && motionEvent.y <= iGap + ihw) {
                    dragMode = DRAGMODE.FIELD

                    println("FIELD")

                    updateColorByPos(filteredX, filteredY, width < height)
                } else if(iGap <= motionEvent.x && motionEvent.x <= iGap + ihw && iGap + ihw + gap <= motionEvent.y && motionEvent.y <= iGap + ihw + gap + barH) {
                    dragMode = DRAGMODE.BAR

                    println("BAR")

                    updateColorByPos(filteredX, filteredY, width < height)
                } else {
                    dragMode = DRAGMODE.NONE
                }
            } else if(motionEvent.action == MotionEvent.ACTION_UP) {
                dragMode = DRAGMODE.NONE

                performClick()
            } else if(motionEvent.action == MotionEvent.ACTION_MOVE) {
                updateColorByPos(filteredX, filteredY, width < height)
            }

            return@setOnTouchListener true
        }
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        p.isAntiAlias = true

        updateBar()
        updateField()

        changeCirclePos()
        changeBarPos()

        setOnTouchListener { _, motionEvent ->
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

            val filteredX = min(360, max(0, ((motionEvent.x - iGap) * 360f / ihw).toInt()))
            val filteredY = min(360, max(0, ((ihw - (motionEvent.y - iGap)) * 360f / ihw).toInt()))

            if(motionEvent.action == MotionEvent.ACTION_DOWN) {

                println("iGap : $iGap | ihw : $ihw | barH : $barH | X : ${motionEvent.x} | Y : ${motionEvent.y}")

                if(iGap <= motionEvent.x && motionEvent.x <= iGap + ihw && iGap <= motionEvent.y && motionEvent.y <= iGap + ihw) {
                    dragMode = DRAGMODE.FIELD

                    println("FIELD")

                    updateColorByPos(filteredX, filteredY, width < height)
                } else if(iGap <= motionEvent.x && motionEvent.x <= iGap + ihw && iGap + ihw + gap <= motionEvent.y && motionEvent.y <= iGap + ihw + gap + barH) {
                    dragMode = DRAGMODE.BAR

                    println("BAR")

                    updateColorByPos(filteredX, filteredY, width < height)
                } else {
                    dragMode = DRAGMODE.NONE
                }
            } else if(motionEvent.action == MotionEvent.ACTION_UP) {
                dragMode = DRAGMODE.NONE

                performClick()
            } else if(motionEvent.action == MotionEvent.ACTION_MOVE) {
                updateColorByPos(filteredX, filteredY, width < height)
            }

            return@setOnTouchListener true
        }
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
        val triangleSize = (gap / sqrt(3.0)).toInt()
        val outerCircle = (ihw * 0.05 / 2.0).toInt()
        val innerCircle = (ihw * 0.0125 / 2.0).toInt()

        val cx = (circleX * ihw / 360.0).toInt()
        val cy = (circleY * ihw / 360.0).toInt()
        val bp = (barPos * ihw / 360.0).toInt()

        canvas.drawBitmap(colorField, null, Rect(iGap, iGap, iGap + ihw, iGap + ihw), p)

        if(w < h) {
            //Portrait
            canvas.save()

            canvas.translate((iGap).toFloat(), (iGap + ihw + gap + barH).toFloat())
            canvas.rotate(-90f)

            p.color = Color.argb(255, 0, 0, 0)

            canvas.drawBitmap(colorBar, null, Rect(0, 0,  barH, ihw), p)

            canvas.restore()
        } else {
            //Landscape
            canvas.drawBitmap(colorBar, null, Rect(iGap , 0, barH, ihw), p)
        }

        getPointerColor()

        p.style = Paint.Style.STROKE
        p.strokeWidth = if(w < h) {
            w * 0.005f
        } else {
            h * 0.005f
        }

        canvas.drawCircle((iGap + cx).toFloat(), (iGap + cy).toFloat(), outerCircle.toFloat(), p)

        p.style = Paint.Style.FILL

        canvas.drawCircle((iGap + cx).toFloat(), (iGap + cy).toFloat(), innerCircle.toFloat(), p)

        if(w < h) {
            //Portrait
            reverseColor()

            canvas.drawLine((iGap + bp).toFloat(), (iGap + gap + ihw).toFloat(), (iGap + bp).toFloat(), (iGap + gap + ihw + barH).toFloat(), p)

            p.style = Paint.Style.FILL

            val triangle = Path()

            triangle.moveTo((iGap + bp).toFloat(), (iGap + gap + ihw).toFloat())
            triangle.lineTo((iGap + bp - triangleSize / 2.0).toFloat(), (iGap + ihw + gap * 0.5).toFloat())
            triangle.lineTo((iGap + bp + triangleSize / 2.0).toFloat(), (iGap + ihw + gap * 0.5).toFloat())

            triangle.moveTo((iGap + bp).toFloat(), (iGap + gap + ihw + barH).toFloat())
            triangle.lineTo((iGap + bp - triangleSize / 2.0).toFloat(), (iGap + ihw + barH + gap * 1.5).toFloat())
            triangle.lineTo((iGap + bp + triangleSize / 2.0).toFloat(), (iGap + ihw + barH + gap * 1.5).toFloat())

            canvas.drawPath(triangle, p)

            p.color = Color.rgb(rgb[0], rgb[1], rgb[2])

            canvas.drawRect(Rect(iGap, iGap + ihw + gap + barH + gap, (iGap + w * 0.1).toInt(), iGap + ihw + gap + barH * 2 + gap), p)
        } else {
            //Landscape
            reverseColor()

            canvas.drawLine((iGap + gap + ihw).toFloat(), (iGap + bp).toFloat(), (iGap + gap + ihw + barH).toFloat(), (iGap + bp).toFloat(), p)

            p.style = Paint.Style.FILL

            val triangle = Path()

            triangle.moveTo((iGap + gap + ihw).toFloat(), (iGap + bp).toFloat())
            triangle.lineTo((iGap + ihw + gap * 0.5).toFloat(), (iGap + bp - triangleSize / 2.0).toFloat())
            triangle.lineTo((iGap + ihw + gap * 0.5).toFloat(), (iGap + bp + triangleSize / 2.0).toFloat())

            triangle.moveTo((iGap + gap + ihw + barH).toFloat(), (iGap + bp).toFloat())
            triangle.lineTo((iGap + ihw + barH + gap * 1.5).toFloat(), (iGap + bp - triangleSize / 2.0).toFloat())
            triangle.lineTo((iGap + ihw + barH + gap * 1.5).toFloat(), (iGap + bp + triangleSize / 2.0).toFloat())

            canvas.drawPath(triangle, p)

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
                    MODE.HUE -> floatArrayOf(360f - y, 1f, 1f)
                    MODE.SATURATION -> floatArrayOf(hsb[0] * 360f, 1f - y / 360f, hsb[2])
                    MODE.BRIGHTNESS -> floatArrayOf(hsb[0] * 360f, hsb[1], 1f - y / 360f)
                    else -> break
                }

                for(x in 0 until colorBar.width)
                    colorBar.setPixel(x, y, Color.HSVToColor(hsv))
            }
        }
    }

    private fun changeBarPos() {
        barPos = when(mode) {
            MODE.HUE -> (360f - hsb[0]).toInt()
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

    private fun getPointerColor() {
        val sum = ((rgb[0] + rgb[1] + rgb[2]) / 3.0).toInt()

        if(sum > 128)
            p.color = Color.argb(255, 20, 20, 20)
        else
            p.color = Color.argb(255, 255, 255, 255)
    }

    private fun reverseColor() {
        p.color = Color.argb(255, 255 - rgb[0], 255 - rgb[1], 255 - rgb[2])
    }

    private fun updateRgb() {
        val c = Color.HSVToColor(hsb)

        rgb[0] = Color.red(c)
        rgb[1] = Color.green(c)
        rgb[2] = Color.blue(c)
    }

    private fun updateHsb() {
        Color.colorToHSV(Color.rgb(rgb[0], rgb[1], rgb[2]), hsb)
    }

    private fun updateColorByPos(x: Int, y: Int, portrait: Boolean) {
        when(mode) {
            MODE.HUE -> {
                if(dragMode == DRAGMODE.FIELD) {
                    hsb[1] = x / 360f
                    hsb[2] = y / 360f
                } else if(dragMode == DRAGMODE.BAR) {
                    hsb[0] = 360f - (if(portrait) x else y)
                }

                updateRgb()
            }
            MODE.SATURATION -> {
                if(dragMode == DRAGMODE.FIELD) {
                    hsb[0] = x.toFloat()
                    hsb[2] = y / 360f
                } else if(dragMode == DRAGMODE.BAR) {
                    hsb[1] = (360f - (if(portrait) x else y)) / 360f
                }

                updateRgb()
            }
            MODE.BRIGHTNESS -> {
                if(dragMode == DRAGMODE.FIELD) {
                    hsb[0] = x.toFloat()
                    hsb[1] = y / 360f
                } else if(dragMode == DRAGMODE.BAR) {
                    hsb[2] = (360f - (if(portrait) x else y)) / 360f
                }

                updateRgb()
            }
            MODE.RED -> {
                if(dragMode == DRAGMODE.FIELD) {
                    rgb[1] = round(x / 360f * 255).toInt()
                    rgb[2] = round(y * 255 / 360f).toInt()
                } else if(dragMode == DRAGMODE.BAR) {
                    rgb[0] = round((360 - (if(portrait) x else y)) * 255 / 360f).toInt()
                }

                updateHsb()
            }
            MODE.GREEN -> {
                if(dragMode == DRAGMODE.FIELD) {
                    rgb[0] = round(x / 360f * 255).toInt()
                    rgb[2] = round(y * 255 / 360f).toInt()
                } else if(dragMode == DRAGMODE.BAR) {
                    rgb[1] = round((360 - (if(portrait) x else y)) * 255 / 360f).toInt()
                }

                updateHsb()
            }
            MODE.BLUE -> {
                if(dragMode == DRAGMODE.FIELD) {
                    rgb[0] = round(x / 360f * 255).toInt()
                    rgb[1] = round(y * 255 / 360f).toInt()
                } else if(dragMode == DRAGMODE.BAR) {
                    rgb[2] = round((360 - (if(portrait) x else y)) * 255 / 360f).toInt()
                }

                updateHsb()
            }
        }

        if(dragMode == DRAGMODE.FIELD) {
            changeCirclePos()

            if(mode != MODE.HUE)
                updateBar()
        } else if(dragMode == DRAGMODE.BAR) {
            changeBarPos()
            updateField()
        }

        invalidate()
    }
}