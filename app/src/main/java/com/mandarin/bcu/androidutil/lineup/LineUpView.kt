package com.mandarin.bcu.androidutil.lineup

import android.content.Context
import android.graphics.*
import android.os.CountDownTimer
import android.view.View
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import common.battle.BasisSet
import common.battle.LineUp
import common.util.unit.Form
import java.io.File
import java.util.*

class LineUpView(context: Context?) : View(context) {
    private val postionReplace = 600

    /**
     * Bitmap list of unit icons
     */
    private val units: MutableList<Bitmap?> = ArrayList()
    /**
     * Bitmap of empty icon
     */
    private var empty: Bitmap? = null
    /**
     * Paint for whole lineup view
     */
    private val p = Paint()
    /**
     * Paint for whole icons
     */
    private val icon = Paint()
    /**
     * Paint for whole floating icons
     */
    private val floating = Paint()
    /**
     * Float value of 8 dpi
     */
    private val f: Float
    /**
     * Bitmap from delete icon drawable
     */
    private val bd: Bitmap
    /**
     * Bitmap from replace icon drawable
     */
    private val replace: Bitmap
    /**
     * Positions of each units
     */
    var position = Array(2) { Array(5) { FloatArray(2) } }
    /**
     * This boolean decides if floating image can be drawn or not
     */
    @JvmField
    var drawFloating = false
    /**
     * Bitmap about floating unit icon
     */
    @JvmField
    var floatB: Bitmap? = null
    /**
     * This boolean decides whether view calls onDraw
     */
    @JvmField
    var touched = false
    /**
     * Position where user firstly touched
     */
    @JvmField
    var prePosit = IntArray(2)
    /**
     * Last position of lineup
     */
    private var lastPosit = 1
    /**
     * X coordinate where user is touching
     */
    var posx = -1f
    /**
     * Y coordinate where user is touching
     */
    var posy = -1f
    /**
     * Width/Height of unit icons
     */
    private var bw = 128f
    /**
     * Replacing area's form data
     */
    @JvmField
    var repform: Form? = null

    var yello = false

    private val timer: CountDownTimer = object : CountDownTimer(100, 100) {
        override fun onFinish() {
            yello = !yello

            invalidate()

            start()
        }

        override fun onTick(millisUntilFinished: Long) {}
    }

    init {
        timer.start()
    }

    public override fun onDraw(c: Canvas) {
        getPosition()
        drawUnits(c)
        drawDeleteBox(c)
        drawReplaceBox(c)
        drawSelectedOutLine(c)
        if (drawFloating) drawFloatingImage(c)
        if (touched) {
            invalidate()
        }
    }

    /**
     * If floatB is true, draws floating unit image
     */
    private fun drawFloatingImage(c: Canvas) {
        if (floatB != empty) c.drawBitmap(StaticStore.getResizebp(floatB, bw * 1.5f, bw * 1.5f), posx - bw * 1.5f / 2, posy - bw * 1.5f / 2, floating)
    }

    /**
     * Decides unit icons position using its width and height
     */
    private fun getPosition() {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w != 0f && h != 0f) {
            bw = w / 5.0f
            for (i in position.indices) {
                for (j in position[i].indices) {
                    position[i][j][0] = bw * j
                    position[i][j][1] = bw * i
                }
            }
        }
    }

    /**
     * Remove unit from lineup and itself using position
     */
    private fun removeUnit(posit: IntArray) {
        if (StaticStore.currentForms == null) {
            toFormList()
        }

        if (posit[0] * 5 + posit[1] >= StaticStore.currentForms.size || posit[0] * 5 + posit[1] < 0) if (posit[0] != 100) return
        if (posit[0] != 100) {
            if (posit[0] * 5 + posit[1] < StaticStore.currentForms.size) {
                StaticStore.currentForms.removeAt(posit[0] * 5 + posit[1])
                StaticStore.currentForms.add(null)
                units.removeAt(posit[0] * 5 + posit[1])
                units.add(empty)
            }
            lastPosit--
            toFormArray()
        } else {
            repform = null
        }
    }

    /**
     * Draws deleting area
     */
    private fun drawDeleteBox(c: Canvas) {
        c.drawRoundRect(RectF(bw + f, bw * 2 + f, bw * 5 - f, bw * 3 - f), f, f, p)
        c.drawBitmap(bd, bw + bw * 4 / 2 - bd.width.toFloat() / 2, bw * 2.5f - bd.height.toFloat() / 2, icon)
    }

    /**
     * Draws replacing area using repform
     */
    private fun drawReplaceBox(c: Canvas) {
        if (repform == null) {
            c.drawBitmap(StaticStore.getResizebp(empty, bw, bw), 0f, bw * 2, p)
            c.drawBitmap(replace, bw / 2 - replace.width.toFloat() / 2, bw * 2.5f - replace.height.toFloat() / 2, icon)
        } else {
            var icon = repform!!.anim.uni.img.bimg() as Bitmap
            if (icon.width != icon.height) icon = StaticStore.MakeIcon(context, icon, 48f)
            c.drawBitmap(StaticStore.getResizebp(icon, bw, bw), 0f, bw * 2, p)
        }
    }

    /**
     * Draws unit icons using units
     */
    private fun drawUnits(c: Canvas) {
        var k = 0
        for (floats in position) {
            for (aFloat in floats) {
                if (k >= units.size) c.drawBitmap(StaticStore.getResizebp(empty, bw, bw), aFloat[0], aFloat[1], p) else c.drawBitmap(StaticStore.getResizebp(units[k], bw, bw), aFloat[0], aFloat[1], p)
                k += 1
            }
        }
    }

    private fun drawSelectedOutLine(c: Canvas) {
        val color = p.color

        p.color = if(yello) {
            Color.YELLOW
        } else {
            Color.MAGENTA
        }

        if(StaticStore.position == null) {
            p.color = color
            return
        }

        if(StaticStore.position[0] == -1) {
            p.color = color
            return
        } else if(StaticStore.position[0] == 100) {
            if(repform == null) {
                p.color = color
                return
            }

            p.strokeWidth = 5f / 128f * bw
            p.style = Paint.Style.STROKE

            val w = 110f / 128f * bw / 2f - p.strokeWidth / 2f - 2f
            val h = 85f / 128f * bw / 2f - p.strokeWidth / 2f - 2f

            val centerX = bw * 0.5f
            val centerY = bw * 2.5f

            c.drawRect(centerX-w, centerY-h, centerX+w, centerY+h, p)

            p.style = Paint.Style.FILL
            p.color = color
        } else {
            if(StaticStore.position[0] * 5 + StaticStore.position[1] < StaticStore.currentForms.size) {
                if(StaticStore.currentForms[StaticStore.position[0] * 5 + StaticStore.position[1]] == null) {
                    p.color = color
                    return
                }

                p.strokeWidth = 5f / 128f * bw
                p.style = Paint.Style.STROKE

                val w = 110f / 128f * bw / 2f - p.strokeWidth / 2f - 2f
                val h = 85f / 128f * bw / 2f - p.strokeWidth / 2f - 2f

                val centerX = bw * (StaticStore.position[1] + 0.5f)
                val centerY = bw * (StaticStore.position[0] + 0.5f)

                c.drawRect(centerX-w, centerY-h, centerX+w, centerY+h, p)

                p.style = Paint.Style.FILL
                p.color = color
            }
        }
    }

    /**
     * Changes 2 units' position using 2 ints. from is first unit's position, and to is second unit's position
     */
    private fun changeUnitPosition(from: Int, to: Int) {
        if (from < 0 || from >= units.size || to < 0 || to >= units.size) return
        val b = units[from]
        val b2 = units[to]
        if (b == empty) return
        val f = StaticStore.currentForms[from]
        if (b2 != empty) {
            units.removeAt(from)
            StaticStore.currentForms.removeAt(from)
            units.add(to, b)
            StaticStore.currentForms.add(to, f)
        } else {
            units.removeAt(from)
            StaticStore.currentForms.removeAt(from)
            units.add(lastPosit - 1, b)
            StaticStore.currentForms.add(lastPosit - 1, f)
        }
        toFormArray()
    }

    /**
     * Replaces specific unit and unit which is in replacing area
     */
    private fun replaceUnit(from: Int, to: Int) {
        var f: Form? = null
        var f2: Form? = null
        var b: Bitmap? = null
        var b2: Bitmap? = null

        //mode makes app able to distinguish if source is in replacing place or not

        var mode = true

        //if target's position is empty then return

        if (from == postionReplace && (to < 0 || to >= units.size))
            return

        //if source's position is empty then return

        if (to == postionReplace && (from < 0 || from >= units.size))
            return

        if (from == postionReplace) {
            if (repform != null) {
                val icon = repform?.anim?.uni?.img?.bimg()

                b = if(icon == null) {
                    StaticStore.MakeIcon(context, null, 48f)
                } else {
                    StaticStore.MakeIcon(context, icon as Bitmap, 48f)
                }
            }

            b2 = units[to]

            f = repform

            if (b2 != empty)
                f2 = StaticStore.currentForms[to]

            mode = false
        } else {
            b = units[from]

            if (repform != null) {
                val icon = repform?.anim?.uni?.img?.bimg()

                b2 = if(icon == null) {
                    StaticStore.MakeIcon(context, null, 48f)
                } else {
                    StaticStore.MakeIcon(context, icon as Bitmap, 48f)
                }
            }

            if (b != empty)
                f = StaticStore.currentForms[from]

            f2 = repform
        }

        if (b != null) {
            if (b.height != b.height)
                b = StaticStore.MakeIcon(context, b, 48f)
        }

        if (b2 != null) {
            if (b2.height != b2.width)
                b2 = StaticStore.MakeIcon(context, b2, 48f)
        }

        if (f == null && f2 == null)
            return

        repform = if (mode) {
            if (f == null)
                return

            if (f2 == null) {
                units.removeAt(from)
                units.add(empty)
                StaticStore.currentForms.removeAt(from)
                StaticStore.currentForms.add(null)
                lastPosit--
                f
            } else {
                units.removeAt(from)
                StaticStore.currentForms.removeAt(from)
                units.add(from, b2)
                StaticStore.currentForms.add(from, f2)
                f
            }
        } else {
            if (f == null)
                return
            if (f2 == null) {
                units.removeAt(to)
                units.add(lastPosit, b)
                StaticStore.currentForms.removeAt(to)
                StaticStore.currentForms.add(lastPosit, f)
                lastPosit++
                null
            } else {
                units.removeAt(to)
                StaticStore.currentForms.removeAt(to)
                units.add(to, b)
                StaticStore.currentForms.add(to, f)
                f2
            }
        }
        toFormArray()
    }

    /**
     * Checks whether user tried to changed lineup
     */
    fun checkChange() {
        val posit = getTouchedUnit(posx, posy) ?: return
        if (posit[0] == -100 && posit[1] == -100) {
            removeUnit(prePosit)
        } else if (posit[0] == 100 || prePosit[0] == 100) {
            replaceUnit(prePosit[0] * 5 + prePosit[1], posit[0] * 5 + posit[1])
        } else {
            changeUnitPosition(5 * prePosit[0] + prePosit[1], 5 * posit[0] + posit[1])
        }
        BasisSet.current.sele.lu.renew()

        try {
            StaticStore.saveLineUp(context)
        } catch(e: Exception) {
            ErrorLogWriter.writeLog(e, StaticStore.upload, context)
            StaticStore.showShortMessage(context, R.string.err_lusave_fail)
        }
    }

    /**
     * Changes unit icon
     */
    private fun changeUnitImage(position: Int, newb: Bitmap?) {
        units[position] = newb
    }

    /**
     * Clears all unit icons
     */
    private fun clearAllUnit() {
        units.clear()
        for (i in 0..14) units.add(empty)
    }

    /**
     * Gets unit's icon using position vlaues
     */
    fun getUnitImage(x: Int, y: Int): Bitmap? {
        if (x == 100) return if (repform != null) {
            var b = repform!!.anim.uni.img.bimg() as Bitmap
            if (b.height != b.width) b = StaticStore.MakeIcon(context, b, 48f)
            b
        } else empty
        return if (5 * x + y >= units.size || 5 * x + y < 0) empty else units[5 * x + y]
    }

    /**
     * Gets unit which user touched
     */
    fun getTouchedUnit(x: Float, y: Float): IntArray? {
        for (i in position.indices) {
            for (j in position[i].indices) {
                if (x - position[i][j][0] >= 0 && y - position[i][j][1] > 0 && x - bw - position[i][j][0] < 0 && y - bw - position[i][j][1] < 0) return intArrayOf(i, j)
            }
        }
        if (y > bw * 2 && y <= bw * 3 && x >= bw) return intArrayOf(-100, -100)
        return if (y > bw * 2 && y <= bw * 3 && x <= bw) intArrayOf(100, 100) else null
    }

    /**
     * Gets form data from array of forms in Lineup and converts it to list of forms in StaticStore
     */
    private fun toFormList() {
        val forms = BasisSet.current.sele.lu.fs
        StaticStore.currentForms = ArrayList()
        for (form in forms) {
            StaticStore.currentForms.addAll(listOf(*form))
        }
    }

    /**
     * Gets form data from list of forms in StaticStore and converts it to array of forms in Lineup
     */
    fun toFormArray() {
        for (i in BasisSet.current.sele.lu.fs.indices) {
            for (j in BasisSet.current.sele.lu.fs[i].indices) {
                if (i * 5 + j < StaticStore.currentForms.size) BasisSet.current.sele.lu.fs[i][j] = StaticStore.currentForms[i * 5 + j] else BasisSet.current.sele.lu.fs[i][j] = null
            }
        }
    }

    /**
     * Update Lineup view using form data in Lineup
     */
    fun updateLineUp() {
        clearAllUnit()
        toFormList()
        lastPosit = 0
        for (i in BasisSet.current.sele.lu.fs.indices) {
            for (j in BasisSet.current.sele.lu.fs[i].indices) {
                val f = BasisSet.current.sele.lu.fs[i][j]
                if (f != null) {
                    if (repform != null) {
                        if (f.unit === repform!!.unit) {
                            repform = null
                            StaticStore.position = intArrayOf(-1, -1)
                            StaticStore.updateForm = true
                            StaticStore.updateOrb = true
                        }
                    }
                    var b = f.anim.uni.img.bimg() as Bitmap
                    if (b.width != b.height) b = StaticStore.MakeIcon(context, b, 48f)
                    changeUnitImage(i * 5 + j, b)
                    lastPosit += 1
                }
            }
        }
        BasisSet.current.sele.lu.renew()
    }

    /**
     * Change specific unit's form
     */
    fun changeFroms(lu: LineUp) {
        for (i in lu.fs.indices) {
            System.arraycopy(lu.fs[i], 0, BasisSet.current.sele.lu.fs[i], 0, lu.fs[i].size)
        }
        updateLineUp()
    }

    override fun onDetachedFromWindow() {
        timer.cancel()
        super.onDetachedFromWindow()
    }

    init {
        val path = StaticStore.getExternalPath(context)+"org/page/uni.png"
        val f = File(path)
        empty = if (!f.exists()) {
            StaticStore.empty(context, 10f, 10f)
        } else {
            BitmapFactory.decodeFile(path)
        }
        this.f = StaticStore.dptopx(8f, context).toFloat()
        bd = StaticStore.getResizebp(StaticStore.getBitmapFromVector(context, R.drawable.ic_delete_forever_black_24dp), bw * 2 / 3, bw * 2 / 3)
        replace = StaticStore.getResizebp(StaticStore.getBitmapFromVector(context, R.drawable.ic_autorenew_black_24dp), bw * 2 / 5, bw * 2 / 5)
        p.isFilterBitmap = true
        p.isAntiAlias = true
        p.color = StaticStore.getAttributeColor(context, R.attr.ButtonPrimary)
        floating.isFilterBitmap = true
        floating.isAntiAlias = true
        floating.alpha = 255 / 2
        for (i in position.indices) {
            for (j in position[i].indices) {
                position[i][j][0] = bw * i
                position[i][j][1] = bw * i
            }
        }
        for (i in 0..14) units.add(empty)
    }
}