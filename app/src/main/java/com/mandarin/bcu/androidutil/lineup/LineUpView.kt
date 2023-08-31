package com.mandarin.bcu.androidutil.lineup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.mandarin.bcu.LineUpScreen
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import common.battle.BasisSet
import common.battle.LineUp
import common.system.files.VFile
import common.util.stage.Limit
import common.util.stage.Stage
import common.util.unit.Form
import java.util.Collections
import kotlin.math.max
import kotlin.math.min

class LineUpView : View {
    companion object {
        const val REPLACE = 100
        const val REMOVE = -100
    }

    private val pager: ViewPager2?
    private var limit: Limit? = null
    private var price = 0

    /**
     * Bitmap list of unit icons
     */
    private val units: Array<Array<Bitmap>>
    /**
     * Flag whether draw unusable red box or not
     */
    private val isUnusable = Array(2) { Array(5) { false } }
    /**
     * Bitmap of empty icon
     */
    private lateinit var empty: Bitmap
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
    private val f: Float = StaticStore.dptopx(8f, context).toFloat()

    /**
     * Bitmap from delete icon drawable
     */
    private lateinit var bd: Bitmap
    /**
     * Bitmap from replace icon drawable
     */
    private lateinit var replace: Bitmap

    /**
     * Bitmap that is used when unit is unusable
     */
    private lateinit var unusable: Bitmap
    /**
     * Currently selected lineup
     */
    val lu: LineUp
        get() {
            return BasisSet.current().sele.lu
        }
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

    private lateinit var replaceFormIcon: Bitmap
    private var isReplaceFormUnusable = false

    var yellow = false

    constructor(context: Context?) : super(context) {
        this.pager = null
    }

    constructor(context: Context?, pager: ViewPager2) : super(context) {
        this.pager = pager
    }

    init {
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

        units = Array(2) { Array(5) { StaticStore.empty(1, 1) }}

        isHapticFeedbackEnabled = false
    }

    private val timer: CountDownTimer = object : CountDownTimer(100, 100) {
        override fun onFinish() {
            yellow = !yellow

            invalidate()

            start()
        }

        override fun onTick(millisUntilFinished: Long) {}
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        getPosition()

        val path = "./org/page/uni.png"
        val originalIcon = VFile.get(path).data.img.bimg() as Bitmap

        empty = StaticStore.getResizebp(originalIcon, bw, bw)
        bd = StaticStore.getResizebp(StaticStore.getBitmapFromVector(context, R.drawable.ic_delete_forever_black_24dp), 128f * 2 / 3, 128f * 2 / 3)
        replace = StaticStore.getResizebp(StaticStore.getBitmapFromVector(context, R.drawable.ic_autorenew_black_24dp), 128f * 2 / 5, 128f * 2 / 5)

        val preUnusable = StaticStore.empty(110, 85)
        val canvas = Canvas(preUnusable)

        val paint = Paint()
        paint.color = StaticStore.getAttributeColor(context, R.attr.SemiWarningPrimary)

        canvas.drawRect(0f, 0f, originalIcon.width.toFloat(), originalIcon.height.toFloat(), paint)

        unusable = StaticStore.getResizebp(StaticStore.makeIcon(context, preUnusable, 48f), bw, bw)


        syncLineUp()

        invalidate()
    }

    init {
        timer.start()
    }

    override fun onDraw(c: Canvas) {
        getPosition()

        if (!this::empty.isInitialized || !this::bd.isInitialized || !this::replace.isInitialized) {
            return
        }

        drawUnits(c)
        drawDeleteBox(c)
        drawReplaceBox(c)
        drawSelectedOutLine(c)

        if (drawFloating)
            drawFloatingImage(c)

        if (touched) {
            invalidate()
        }
    }

    fun attachStageLimit(stage: Stage, star: Int) {
        val container = stage.cont ?: return

        limit = stage.getLim(star)
        price = container.price
    }

    /**
     * If floatB is true, draws floating unit image
     */
    private fun drawFloatingImage(c: Canvas) {
        val f = floatB ?: return

        if (f != empty)
            c.drawBitmap(StaticStore.getResizebp(f, bw * 1.5f, bw * 1.5f), posx - bw * 1.5f / 2, posy - bw * 1.5f / 2, floating)
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
        if (posit[0] != REPLACE && posit[1] != REPLACE) {
            lu.fs[posit[0]][posit[1]] ?: return
        } else {
            repform ?: return
        }

        if (posit[0] != REPLACE) {
            lu.fs[posit[0]][posit[1]] = null
        } else {
            repform = null
        }

        syncUnitList()
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
        c.drawBitmap(replaceFormIcon, 0f, bw * 2, p)

        if (repform == null) {
            c.drawBitmap(replace, bw / 2 - replace.width.toFloat() / 2, bw * 2.5f - replace.height.toFloat() / 2, icon)
        }

        if (isReplaceFormUnusable) {
            c.drawBitmap(unusable, 0f, bw * 2, p)
        }
    }

    /**
     * Draws unit icons using units
     */
    private fun drawUnits(c: Canvas) {
        position.forEachIndexed { x, icons ->
            icons.forEachIndexed { y, coordinate ->
                c.drawBitmap(units[x][y], coordinate[0], coordinate[1], p)

                if (isUnusable[x][y]) {
                    c.drawBitmap(unusable, coordinate[0], coordinate[1], p)
                }
            }
        }
    }

    private fun drawSelectedOutLine(c: Canvas) {
        val color = p.color

        p.color = if(yellow) {
            Color.YELLOW
        } else {
            Color.MAGENTA
        }

        if(StaticStore.position.isEmpty()) {
            p.color = color

            return
        }

        if(StaticStore.position[0] == -1) {
            p.color = color

            return
        } else if(StaticStore.position[0] == REPLACE) {
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
            val selectedForm = lu.fs[StaticStore.position[0]][StaticStore.position[1]]

            if (selectedForm == null) {
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

    /**
     * Changes 2 units' position using 2 ints. from is first unit's position, and to is second unit's position
     */
    private fun changeUnitPosition(from: IntArray, to: IntArray) {
        //Pointless move
        if (from[0] == to[0] && from[1] == to[1])
            return

        //If source unit is empty, don't do it
        lu.fs[from[0]][from[1]] ?: return

        //Check which coordinate is larger
        val fromIsBig = from[0] > to[0] || from[1] > to[1]

        //Shift units
        rotateUnit(from, to, fromIsBig)
    }

    private fun rotateUnit(from: IntArray, to: IntArray, toRight: Boolean) {
        //Make 2D array into 1D array
        val unitArray = Array<Form?>(10) {
            val x = if (it >= 5)
                1
            else
                0

            val y = it - x * 5

            lu.fs[x][y]
        }

        //Extract units with range of two coordinates
        val f = from[0] * 5 + from[1]
        val t = to[0] * 5 + to[1]

        val realFrom = min(f, t)
        val realTo = max(f, t)

        //For safety
        if (realTo - realFrom + 1 <= 0) {
            return
        }

        val extractedUnitArray = Array<Form?>(realTo - realFrom + 1) {
            val realIndex = it + realFrom

            val x = if (realIndex >= 5)
                1
            else
                0

            val y = realIndex - x * 5

            lu.fs[x][y]
        }.toList()

        //Rotate array
        Collections.rotate(extractedUnitArray, if (toRight) 1 else -1)

        //Inject rotated array into unit array
        extractedUnitArray.forEachIndexed { index, form ->
            val realIndex = index + realFrom

            unitArray[realIndex] = form
        }

        //Inject 1D array into 2D array
        unitArray.forEachIndexed { index, form ->
            val x = if (index >= 5)
                1
            else
                0

            val y = index - x * 5

            lu.fs[x][y] = form
        }
    }

    /**
     * Replaces specific unit and unit which is in replacing area
     */
    private fun replaceUnit(from: IntArray, to: IntArray) {
        //Replace must be call only when either from or to is replacing place
        if (from[0] == REPLACE && to[0] == REPLACE)
            return

        if (from[0] != REPLACE && to[0] != REPLACE)
            return

        val fromForm = if (from[0] == REPLACE) {
            repform
        } else {
            lu.fs[from[0]][from[1]]
        } ?: return

        val toForm = if (to[0] == REPLACE) {
            repform
        } else {
            lu.fs[to[0]][to[1]]
        }

        //mode makes app able to distinguish if source is in replacing place or not
        //if source form turned out to be replacing space, it's true
        val mode = from[0] == REPLACE

        if (mode) {
            lu.fs[to[0]][to[1]] = fromForm
            repform = toForm
        } else {
            repform = fromForm
            lu.fs[from[0]][from[1]] = toForm
        }
    }

    fun syncLineUp() {
        lu.renew()

        lu.fs.forEachIndexed { x, forms ->
            forms.forEachIndexed { y, form ->
                val icon = form?.anim?.uni?.img?.bimg()

                units[x][y] = if (icon is Bitmap)
                    StaticStore.getResizebp(StaticStore.makeIcon(context, icon, 48f), bw, bw)
                else
                    empty

                isUnusable[x][y] = limit != null && lu.fs[x][y] != null && limit?.unusable(lu.fs[x][y].du, price) == true
            }
        }

        if (repform != null) {
            val icon = repform?.anim?.uni?.img?.bimg()

            if (icon == null) {
                replaceFormIcon = StaticStore.getResizebp(StaticStore.makeIcon(context, empty, 48f), bw, bw)
                isReplaceFormUnusable = false

                return
            }

            if (icon !is Bitmap) {
                replaceFormIcon = StaticStore.getResizebp(StaticStore.makeIcon(context, empty, 48f), bw, bw)
                isReplaceFormUnusable = false

                return
            }

            replaceFormIcon = StaticStore.getResizebp(StaticStore.makeIcon(context, icon, 48f), bw, bw)
            isReplaceFormUnusable = limit != null && limit?.unusable(repform?.du, price) == true
        } else {
            replaceFormIcon = StaticStore.getResizebp(StaticStore.makeIcon(context, empty, 48f), bw, bw)
            isReplaceFormUnusable = false
        }
    }

    /**
     * Checks whether user tried to changed lineup
     */
    fun checkChange() {
        val posit = getTouchedUnit(posx, posy) ?: return

        //No need to check change at all if source was remove
        if (prePosit[0] == REMOVE || prePosit[1] == REMOVE)
            return

        if (posit[0] == REMOVE && posit[1] == REMOVE) {
            removeUnit(prePosit)
        } else if (posit[0] == REPLACE || prePosit[0] == REPLACE) {
            replaceUnit(prePosit, posit)
        } else {
            changeUnitPosition(prePosit, posit)
        }

        syncLineUp()

        try {
            StaticStore.saveLineUp(context, false)
        } catch(e: Exception) {
            ErrorLogWriter.writeLog(e, StaticStore.upload, context)
            StaticStore.showShortMessage(context, R.string.err_lusave_fail)
        }
    }

    /**
     * Changes unit icon
     */
    private fun changeUnitImage(position: IntArray, newIcon: Bitmap) {
        units[position[0]][position[1]] = newIcon
    }

    /**
     * Clears all unit icons
     */
    private fun clearAllUnit() {
        for(i in units.indices) {
            for(j in units[i].indices) {
                units[i][j] = empty
                isUnusable[i][j] = false
            }
        }
    }

    /**
     * Gets unit's icon using position vlaues
     */
    fun getUnitImage(x: Int, y: Int): Bitmap? {
        if (x == REMOVE)
            return null

        if (x == REPLACE)
            return if (repform != null) {
                var b = repform!!.anim.uni.img.bimg() as Bitmap

                if (b.height != b.width)
                    b = StaticStore.makeIcon(context, b, 48f)

                b
            } else
                null

        return if (units[x][y] == empty)
            null
        else
            units[x][y]
    }

    /**
     * Gets unit which user touched
     */
    fun getTouchedUnit(x: Float, y: Float): IntArray? {
        for (i in position.indices) {
            for (j in position[i].indices) {
                if (x - position[i][j][0] >= 0 && y - position[i][j][1] > 0 && x - bw - position[i][j][0] < 0 && y - bw - position[i][j][1] < 0)
                    return intArrayOf(i, j)
            }
        }

        if (y > bw * 2 && y <= bw * 3 && x >= bw && x < bw * 5)
            return intArrayOf(REMOVE, REMOVE)

        return if (y > bw * 2 && y <= bw * 3 && x <= bw && x > 0)
            intArrayOf(REPLACE, REPLACE)
        else
            null
    }

    /**
     * Update Lineup view using form data in Lineup
     */
    fun updateLineUp() {
        clearAllUnit()

        for (i in BasisSet.current().sele.lu.fs.indices) {
            for (j in BasisSet.current().sele.lu.fs[i].indices) {
                val f = BasisSet.current().sele.lu.fs[i][j]

                if (f != null) {
                    if (repform != null) {
                        if (f.unit === repform!!.unit) {
                            repform = null

                            StaticStore.position = intArrayOf(-1, -1)

                            updateUnitSetting()
                            updateUnitOrb()
                        }
                    }

                    var b = f.anim.uni.img.bimg() as Bitmap

                    if (b.width != b.height)
                        b = StaticStore.getResizebp(StaticStore.makeIcon(context, b, 48f), bw, bw)

                    changeUnitImage(intArrayOf(i, j), b)
                }
            }
        }

        syncLineUp()
    }

    /**
     * Change specific unit's form
     */
    fun changeForms(lu: LineUp) {
        for (i in lu.fs.indices) {
            System.arraycopy(lu.fs[i], 0, BasisSet.current().sele.lu.fs[i], 0, lu.fs[i].size)
        }

        updateLineUp()
    }

    override fun onDetachedFromWindow() {
        timer.cancel()
        super.onDetachedFromWindow()
    }

    fun updateUnitList() {
        Log.i("LineupView", "Updating unit list...")

        val adapter = pager?.adapter

        if(adapter != null && adapter is LineUpScreen.LUTab) {
            adapter.updateFragment(0)
        }
    }

    private fun syncUnitList() {
        Log.i("LineupView", "Updating unit list...")

        val adapter = pager?.adapter ?: return

        if (adapter is LineUpScreen.LUTab) {
            adapter.syncFragment(0)
        }
    }

    fun updateUnitSetting() {
        Log.i("LineupView", "Updating unit setting...")

        val adapter = pager?.adapter

        if(adapter != null && adapter is LineUpScreen.LUTab)
            adapter.updateFragment(1)
    }

    fun updateUnitOrb() {
        Log.i("LineupView", "Updating unit orb setting...")

        val adapter = pager?.adapter

        if(adapter != null && adapter is LineUpScreen.LUTab)
            adapter.updateFragment(2)
    }

    fun updateCastleSetting() {
        Log.i("LineupView", "Updating castle setting...")

        val adapter = pager?.adapter

        if(adapter != null && adapter is LineUpScreen.LUTab)
            adapter.updateFragment(3)
    }

    fun updateTreasureSetting() {
        Log.i("LineupView", "Updating treasure setting...")

        val adapter = pager?.adapter

        if(adapter != null && adapter is LineUpScreen.LUTab)
            adapter.updateFragment(4)
    }

    fun updateConstructionSetting() {
        Log.i("LineupView", "Updating construction setting...")

        val adapter = pager?.adapter

        if(adapter != null && adapter is LineUpScreen.LUTab)
            adapter.updateFragment(5)
    }

    fun updateFoundationSetting() {
        Log.i("LineupView", "Updating foundation setting...")

        val adapter = pager?.adapter

        if(adapter != null && adapter is LineUpScreen.LUTab)
            adapter.updateFragment(6)
    }

    fun updateDecorationSetting() {
        Log.i("LineupView", "Updating decoration setting...")

        val adapter = pager?.adapter

        if(adapter != null && adapter is LineUpScreen.LUTab)
            adapter.updateFragment(7)
    }
}