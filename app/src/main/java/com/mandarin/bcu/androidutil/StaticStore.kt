package com.mandarin.bcu.androidutil

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseArray
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonParser
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StatFilterElement.Companion.statFilter
import com.mandarin.bcu.androidutil.animation.AnimationCView
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.ErrorLogWriter.Companion.writeDriveLog
import com.mandarin.bcu.androidutil.pack.PackConflict
import common.CommonStatic
import common.battle.BasisLU
import common.battle.BasisSet
import common.io.json.JsonDecoder
import common.pack.Identifier
import common.pack.IndexContainer.Indexable
import common.pack.UserProfile
import common.system.fake.FakeImage
import common.system.files.VFile
import common.util.Data
import common.util.anim.AnimU.UType
import common.util.anim.ImgCut
import common.util.lang.MultiLangCont
import common.util.stage.Music
import common.util.unit.*
import common.util.unit.Unit
import java.io.*
import java.math.BigInteger
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.ln

object StaticStore {
    //System & IO variables
    /**Version of Application */
    const val VER = "0.16.4"

    /**File ID of google drive log folder */
    const val ERR_FILE_ID = "1F60YLwsJ_zrJOh0IczUuf-Q1QyJftWzK"

    /**Locale codes list */
    val lang = arrayOf("", "en", "zh", "ko", "ja", "ru", "de", "fr", "nl", "es", "it")

    /**List of language files */
    val langfile = arrayOf("EnemyName.txt", "StageName.txt", "UnitName.txt", "UnitExplanation.txt", "EnemyExplanation.txt", "CatFruitExplanation.txt", "RewardName.txt", "ComboName.txt", "MedalName.txt", "MedalExplanation.txt")


    /**Shared preferences name */
    const val CONFIG = "configuration"

    const val LANG = "language"

    const val PACK = "pack"

    const val PROG = "prog"
    const val TEXT = "text"

    /**
     * This value prevents button is performed less than every 1 sec<br></br>
     * Used when preventing activity is opened double
     */
    const val INTERVAL: Long = 1000

    /**
     * This value prevents button is performed less than every 350 ms<br></br>
     * Used when preventing animation working incorrectly
     */
    const val INFO_INTERVAL: Long = 350

    /** Value which tells if Unit language data is loaded  */
    var unitlang = 1

    /** Value which tells if Enemy language data is loaded  */
    var enemeylang = 1

    /** Value which tells if Stage language data is loaded  */
    var stagelang = 1

    /** Value which tells if Medal language data is loaded  */
    var medallang = 1

    /** Boolean which tells if error log dialog is already opened once  */
    var dialogisShowed = false

    /** Boolean which tells if user allowed auto error log uploading  */
    var upload = false

    /**
     * Toast which is used in every activity<br></br>
     * Must be null when activity is destroyed to prevent memory leaks
     */
    var toast: Toast? = null

    /** Value which tells if file paths are added to memory  */
    var init = false

    /** Value which tells if app already read pack or not */
    var packRead = false

    /** Initial vector used when encrypt/decrypt images  */
    @JvmField
    var IV = "1234567812345678"
    //Image/Text variable

    /** img15.png's parts  */
    var img15: Array<FakeImage>? = null

    /** Ability icons  */
    var icons: Array<Bitmap>? = null

    /** Proc icons  */
    var picons: Array<Bitmap>? = null

    /** Cat fruit icons  */
    var fruit: Array<Bitmap>? = null

    /** Star used for difficulty **/
    var starDifficulty: Array<Bitmap>? = null

    /** Additional ability explanation texts  */
    var addition: Array<String> = Array(0) {""}

    /** Imgcut index list of ablities  */
    var anumber = intArrayOf(203, 204, 206, 202, 205, 200, 209, 218, 227, 227, 227, 260, 258, 227, 227, 110, 227, 122, 114)

    /** Imgcut index list of procs  */
    var pnumber = intArrayOf(195, 197, 198, 207, 266, 289, 231, 196, 199, 201, 264, 296, 229, 293, 208, 239, 213, 214, 215, 216, 210, 243, 262, 116, 237, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 227, 43, 45, 47, 49, 51, 53, 109, 235, 241)

    /** File index list of abilities  */
    var afiles = arrayOf("", "", "", "", "", "", "", "", "SnipeX.png", "TimeX.png", "Ghost.png", "", "", "Suicide.png", "ThemeX.png", "", "BossWaveX.png", "", "")

    /** File index list of procs  */
    var pfiles = arrayOf("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "BCPoison.png", "Barrier.png", "DemonShield.png", "DeathSurge.png", "Burrow.png", "Revive.png", "Snipe.png", "Seal.png", "Time.png", "Summon.png", "Moving.png", "Theme.png", "Poison.png", "BossWave.png", "ArmorBreak.png", "Speed.png", "Counter.png", "DmgCut.png", "DmgCap.png", "CritX.png", "PoisonX.png", "SealX.png", "MovingX.png", "SummonX.png", "ArmorBreakX.png", "SpeedX.png", "", "", "", "", "", "", "", "", "")

    /** String ID list of traits  */
    var colorid = intArrayOf(R.string.sch_red, R.string.sch_fl, R.string.sch_bla, R.string.sch_me, R.string.sch_an, R.string.sch_al, R.string.sch_zo, R.string.sch_de, R.string.sch_re, R.string.sch_wh, R.string.esch_eva, R.string.esch_witch, R.string.sch_ba)

    /** String ID list of star and mask treasure  */
    var starid = intArrayOf(R.string.unit_info_starred, R.string.unit_info_god1, R.string.unit_info_god2, R.string.unit_info_god3)

    /** String ID list of abilities  */
    var abiid = intArrayOf(R.string.sch_abi_st, R.string.sch_abi_re, R.string.sch_abi_md, R.string.sch_abi_ao, R.string.sch_abi_em, R.string.sch_abi_bd, R.string.sch_abi_me, R.string.sch_abi_ws, R.string.abi_isnk, R.string.abi_istt, R.string.abi_gh, R.string.sch_abi_zk, R.string.sch_abi_wk, R.string.abi_sui, R.string.abi_ithch, R.string.sch_abi_eva, R.string.abi_iboswv, R.string.sch_abi_it, R.string.sch_abi_id)

    //Variables for Unit

    private var unitinflistClick = SystemClock.elapsedRealtime()
    var UisOpen = false
    var unittabposition = 0
    var unitinfreset = false

    //Variables for Enemy

    var enemyinflistClick = SystemClock.elapsedRealtime()
    var EisOpen = false

    //Variables for Map/Stage

    var SisOpen = false
    var bcMapNames = intArrayOf(R.string.stage_sol, R.string.stage_event, R.string.stage_collabo, R.string.stage_eoc, R.string.stage_ex, R.string.stage_dojo, R.string.stage_heavenly, R.string.stage_ranking, R.string.stage_challenge, R.string.stage_uncanny, R.string.stage_night, R.string.stage_baron, R.string.stage_enigma, R.string.stage_CA)
    var mapcolcname: ArrayList<String> = ArrayList()
    var mapcode: ArrayList<String> = ArrayList(listOf("000000", "000001", "000002", "000003", "000004", "000006", "000007", "000011", "000012", "000013", "000014", "000024", "000025", "000027"))
    var BCmaps = mapcode.size
    val BCMapCode = listOf("000000", "000001", "000002", "000003", "000004", "000006", "000007", "000011", "000012", "000013", "000014", "000024", "000025", "000027")
    var eicons: Array<Bitmap>? = null
    var maplistClick = SystemClock.elapsedRealtime()
    var stglistClick = SystemClock.elapsedRealtime()
    var infoClick = SystemClock.elapsedRealtime()
    var treasure: Bitmap? = null
    var infoOpened: BooleanArray? = null
    var stageSpinner = -1
    private var bgnumber = 0
    var bglistClick = SystemClock.elapsedRealtime()
    var cslistClick = SystemClock.elapsedRealtime()
    var stgenem: ArrayList<Identifier<AbEnemy>> = ArrayList()
    var stgenemorand = true
    var stgmusic = ""
    var stgbg = ""
    var stgstar = 0
    var stgbh = -1
    var bhop = -1
    var stgcontin = -1
    var stgboss = -1
    var filter: Map<String, SparseArray<ArrayList<Int>>>? = null

    /**
     * Variables for Medal
     */
    var medalnumber = 0
    val medals: ArrayList<Bitmap> = ArrayList()
    val MEDNAME = MultiLangCont<Int, String>()
    val MEDEXP = MultiLangCont<Int, String>()

    /**
     * Variables for Music
     */
    var musicnames: MutableMap<String, SparseArray<String>> = HashMap()
    var musicData: MutableList<Identifier<Music>> = ArrayList()
    var durations: MutableList<Int> = ArrayList()

    /**
     * Variables for Animation
     */
    var play = true
    var frame = 0
    var formposition = 0
    var animposition = 0
    var gifFrame = 0
    var gifisSaving = false
    var enableGIF = false
    var keepDoing = true

    /**
     * Variables for LineUp
     */
    var ludata = ArrayList<Identifier<Unit>>()
    var LULoading = false
    var LUread = false
    var LUtabPosition = 0
    var currentForms: ArrayList<Form?> = ArrayList()
    var position = intArrayOf(-1, -1)
    var combos: ArrayList<Combo> = ArrayList()
    var set: BasisSet? = null
    var lu: BasisLU? = null

    /**
     * Search Filter Variables
     */
    var tg = ArrayList<Identifier<Trait>>()
    var rare = ArrayList<String>()
    var ability = ArrayList<ArrayList<Int>>()
    var attack = ArrayList<String>()
    var tgorand = true
    var atksimu = true
    var aborand = true
    var atkorand = true
    var empty = true
    var talents = false
    var starred = false
    var entityname = ""
    var stgschname = ""
    var stmschname = ""
    var filterEntityList = BooleanArray(1)

    /**
     * Resets all values stored in StaticStore<br></br>
     * It will also reset whole data of BCU
     */
    fun clear() {
        unitlang = 1
        enemeylang = 1
        stagelang = 1
        medallang = 1
        toast = null
        img15 = null
        icons = null
        picons = null
        fruit = null
        addition = Array(0) {""}
        unitinflistClick = SystemClock.elapsedRealtime()
        UisOpen = false
        unittabposition = 0
        unitinfreset = false
        enemyinflistClick = SystemClock.elapsedRealtime()
        EisOpen = false
        medalnumber = 0
        medals.clear()
        MEDNAME.clear()
        MEDEXP.clear()
        musicnames.clear()
        musicData.clear()
        durations.clear()
        mapcolcname = ArrayList()
        eicons = null
        maplistClick = SystemClock.elapsedRealtime()
        stglistClick = SystemClock.elapsedRealtime()
        infoClick = SystemClock.elapsedRealtime()
        treasure = null
        infoOpened = null
        stageSpinner = -1
        bgnumber = 0
        bglistClick = SystemClock.elapsedRealtime()
        stgenem = ArrayList()
        stgenemorand = true
        stgmusic = ""
        stgbg = ""
        stgstar = 0
        stgbh = -1
        bhop = -1
        stgcontin = -1
        stgboss = -1
        stgschname = ""
        stmschname = ""
        filterEntityList = BooleanArray(1)
        ludata = ArrayList()
        LULoading = false
        LUread = false
        LUtabPosition = 0
        currentForms.clear()
        position = intArrayOf(-1, -1)
        combos.clear()
        set = null
        lu = null
        play = true
        frame = 0
        formposition = 0
        animposition = 0
        gifFrame = 0
        gifisSaving = false
        enableGIF = false
        keepDoing = true

        AnimationCView.gifTask.clear()

        filterReset()
        stgFilterReset()
        resetUserPacks()
    }

    fun resetUserPacks() {
        UserProfile.unloadAllUserPacks()

        mapcode = ArrayList(listOf("000000", "000001", "000002", "000003", "000004", "000006", "000007", "000011", "000012", "000013", "000014", "000024", "000025", "000027"))
        mapcolcname.clear()
        PackConflict.conflicts.clear()

        packRead = false
    }

    fun getResize(drawable: Drawable, context: Context, dp: Float): Bitmap {
        val r = context.resources
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.displayMetrics)
        val b = (drawable as BitmapDrawable).bitmap
        return Bitmap.createScaledBitmap(b, px.toInt(), px.toInt(), false)
    }

    fun getResizeb(b: Bitmap?, context: Context, dp: Float): Bitmap {
        if (b == null || b.isRecycled)
            return empty(context, dp, dp)

        val px = dptopx(dp, context)

        val bd = BitmapDrawable(context.resources, Bitmap.createScaledBitmap(b, px, px, true))

        bd.isFilterBitmap = true
        bd.setAntiAlias(true)

        return bd.bitmap
    }

    /**
     * Gets resized Bitmap.
     *
     * @param b       Source Bitmap.
     * @param context Used when converting dpi value to pixel value.
     * @param w       Width of generated Bitmap. Must be dpi value.
     * @param h       Height of generated Bitmap. Must be dpi value.
     * @return Returns resized Bitmap using specified dpi value.
     */
    fun getResizeb(b: Bitmap?, context: Context, w: Float, h: Float): Bitmap {
        if (b == null || b.isRecycled) return empty(context, w, h)
        val r = context.resources
        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, r.displayMetrics)
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, r.displayMetrics)
        val bd = BitmapDrawable(context.resources, Bitmap.createScaledBitmap(b, width.toInt(), height.toInt(), true))
        bd.isFilterBitmap = true
        bd.setAntiAlias(true)
        return bd.bitmap
    }

    /**
     * Generates empty Bitmap.
     *
     * @param context Used when converting dpi value to pixel value.
     * @param w       Width of generated Bitmap. Must be dpi value.
     * @param h       Height of generated Bitmap. Must be dpi value.
     * @return Returns empty Bitmap using specified dpi value.
     */
    fun empty(context: Context?, w: Float, h: Float): Bitmap {
        context ?: return empty(1, 1)

        val r = context.resources
        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, r.displayMetrics)
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, r.displayMetrics)
        val conf = Bitmap.Config.ARGB_8888
        return Bitmap.createBitmap(width.toInt(), height.toInt(), conf)
    }

    /**
     * Generates empty Bitmap.
     *
     * @param w Width of generated Bitmap. Must be pixel value.
     * @param h Height of generated Bitmap. Must be pixel value.
     * @return Returns empty Bitmap using specified pixel value.
     */
    fun empty(w: Int, h: Int): Bitmap {
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    }

    /**
     * Gets resized bitmap using antialiasing.
     *
     * @param b       Source Bitmap.
     * @param context Used when initializing BitmapDrawable.
     * @param w       Width of generated Bitmap. Must be pixel value.
     * @param h       Height of generated Bitmap. Must be pixel value.
     * @return Returns resized bitmap using antialiasing.
     */
    fun getResizebp(b: Bitmap?, context: Context, w: Float, h: Float): Bitmap {
        if(b == null || b.isRecycled)
            return empty(context, w, h)

        val bd = BitmapDrawable(context.resources, Bitmap.createScaledBitmap(b, w.toInt(), h.toInt(), true))
        bd.isFilterBitmap = true
        bd.setAntiAlias(true)
        return bd.bitmap
    }

    /**
     * Gets resized bitmap.
     *
     * @param b Source Bitmap.
     * @param w Width of generated Bitmap. Must be pixel value.
     * @param h Height of generated Bitmap. Must be pixel value.
     * @return Returns resized bitmap using specified width and height.
     */
    fun getResizebp(b: Bitmap, w: Float, h: Float): Bitmap {
        val matrix = Matrix()
        if (w < 0 || h < 0) {
            if (w < 0 && h < 0) {
                matrix.setScale(-1f, -1f)
            } else if (w < 0) {
                matrix.setScale(-1f, 1f)
            } else if (h < 0) {
                matrix.setScale(1f, -1f)
            }
        }
        var reversed = Bitmap.createBitmap(b, 0, 0, b.width, b.height, matrix, false)
        reversed = Bitmap.createScaledBitmap(reversed, abs(w).toInt(), abs(h).toInt(), true)
        return reversed
    }

    fun dptopx(dp: Float, context: Context?): Int {
        context ?: return 1

        val r = context.resources
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.displayMetrics).toInt()
    }

    /**
     * Saves img15 as cut state by img015.imgcut.
     */
    fun readImg() {
        val path = "./org/page/img015.png"
        val imgcut = "./org/page/img015.imgcut"

        val img = ImgCut.newIns(imgcut)

        img15 = try {
            val png = VFile.get(path).data.img
            img.cut(png)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Reads Treasure Radar icon.
     */
    fun readTreasureIcon() {
        val path = "./org/page/img002.png"
        val imgcut = "./org/page/img002.imgcut"
        val img = ImgCut.newIns(imgcut)
        try {
            val png = VFile.get(path).data.img
            val imgs = img.cut(png)
            treasure = imgs[28].bimg() as Bitmap
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Decides CommonStatic.lang value.
     *
     * @param lan Code of language refers to StaticStore.lang.<br></br>
     * 0 is Auto.
     */
    fun getLang(lan: Int) {
        val language: String
        if (lan == 0) {
            language = Resources.getSystem().configuration.locales[0].language
            CommonStatic.getConfig().lang = listOf(*lang).indexOf(language) - 1
            println("Auto Set : $language")
        } else {
            println(lang[lan])
            CommonStatic.getConfig().lang = lan - 1
        }
        println(CommonStatic.getConfig().lang)

        if (CommonStatic.getConfig().lang >= CommonStatic.Lang.LOC_CODE.size || CommonStatic.getConfig().lang < 0)
            CommonStatic.getConfig().lang = 0
    }

    /**
     * Resets entity filter data<br></br>
     * Must be called when exiting Entity list.
     */
    fun filterReset() {
        tg = ArrayList()
        rare = ArrayList()
        ability = ArrayList()
        attack = ArrayList()
        tgorand = true
        atksimu = true
        aborand = true
        atkorand = true
        empty = true
        talents = false
        starred = false
        statFilter.clear()
    }

    /**
     * Resets stage filter data<br></br>
     * Must be called when exiting Map list
     */
    fun stgFilterReset() {
        stgenem = ArrayList()
        stgenemorand = true
        stgmusic = ""
        stgbg = ""
        stgstar = 0
        stgbh = -1
        bhop = -1
        stgcontin = -1
        stgboss = -1
        stgschname = ""
        stmschname = ""
        filter = null
    }

    /**
     * Gets possible position in specific lineup.
     *
     * @param f Arrays of forms in Lineup.
     * @return Returns first empty position in Lineup.
     * If Lineup is full, it will return position of replacing area.
     */
    fun getPossiblePosition(f: Array<Array<Form?>>): IntArray {
        for (i in f.indices) {
            for (j in f[i].indices) {
                if (f[i][j] == null) return intArrayOf(i, j)
            }
        }
        return intArrayOf(100, 100)
    }

    /**
     * Get Color value using Attr ID.
     *
     * @param context     Decides TypedValue using Theme from Context.
     * @param attributeId ID of color from Attr. Format must be color.
     * @return Gets real ID of color considering Theme.
     * It will return Color value as Hex.
     */
    fun getAttributeColor(context: Context?, attributeId: Int): Int {
        context ?: return 0

        val typedValue = TypedValue()
        context.theme.resolveAttribute(attributeId, typedValue, true)
        val colorRes = typedValue.resourceId
        var color = -1
        try {
            color = ContextCompat.getColor(context, colorRes)
        } catch (e: NotFoundException) {
            e.printStackTrace()
        }
        return color
    }

    /**
     * Generate Bitmap from Vector Asset.
     * Icon's tint color is ?attr/TextPrimary.
     *
     * @param context Get drawable and set tint color to it.
     * @param vectid  Id from Vector Asset. Use "R.drawable._ID_".
     * @return Returns created Bitmap using Vector Asset.
     * If vectid returns null, then it will generate empty icon.
     */
    fun getBitmapFromVector(context: Context?, vectid: Int): Bitmap {
        context ?: return empty(1,1)

        val drawable = ContextCompat.getDrawable(context, vectid)
                ?: return empty(context, 100f, 100f)
        drawable.setTint(getAttributeColor(context, R.attr.TextPrimary))
        val res = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(res)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return res
    }

    /**
     * Check if specified icon has different width and height.
     * If they are different, it will generate icon which has same width and height.
     * Width and Height's default value is 128 pixels.
     *
     * @param context Using Context to convert dpi value to pixel value.
     * @param b       Source Bitmap.
     * @param wh      This parameter decides width and height of created icon.
     * It must be dpi value.
     * @return If source has same width and height, it will return source.
     * If not, it will return icon which has same width and height.
     */
    fun makeIcon(context: Context, b: Bitmap?, wh: Float): Bitmap {
        if (b == null || b.isRecycled) return empty(context, 24f, 24f)
        if (b.height == b.width) return getResizeb(b, context, wh)
        val before = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val c = Canvas(before)
        val p = Paint()
        c.drawBitmap(b, 64 - b.width / 2f, 64 - b.height / 2f, p)
        return getResizeb(before, context, wh)
    }

    /**
     * Check if specified icon has different width and height<br></br>
     * If they are different, it will generate icon which has same width and height<br></br>
     * Width and Height's default value is 128 pixels
     *
     * @param b  Source Bitmap
     * @param wh This parameter decides width and height of created icon<br></br>
     * It must be pixel value
     * @return If source has same width and height, it will return source<br></br>
     * If not, it will return icon which has same width and height
     */
    fun makeIconp(b: Bitmap?, wh: Float): Bitmap {
        if (b == null) return empty(128, 128)
        if (b.height == b.width) return getResizebp(b, wh, wh)
        val before = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val c = Canvas(before)
        val p = Paint()
        c.drawBitmap(b, 64 - b.width / 2f, 64 - b.height / 2f, p)
        return if (wh == 128f) before else getResizebp(before, wh, wh)
    }

    /**
     * Saves lineup file.
     */
    @Throws(Exception::class)
    fun saveLineUp(c: Context) {
        val direct = getExternalUser(c)
        val path = "${direct}basis.json"
        val g = File(direct)
        if (!g.exists()) if (!g.mkdirs()) {
            Log.e("SaveLineUp", "Failed to create directory " + g.absolutePath)

            showShortMessage(c, "Failed to create directory " + g.absolutePath)

            return
        }
        val f = File(path)
        if (!f.exists()) if (!f.createNewFile()) {
            Log.e("SaveLineUp", "Failed to create file " + f.absolutePath)

            showShortMessage(c, "Failed to create file " + f.absolutePath)

            return
        }

        BasisSet.write()
    }

    /**
     * Get RGB value from specified HEX color value
     *
     * @param hex Color HEX value which will be converted to RGB values
     * @return Return as three integer array, first is R, second is G, and third is B
     */
    fun getRGB(hex: Int): IntArray {
        val r = hex and 0xFF0000 shr 16
        val g = hex and 0xFF00 shr 8
        val b = hex and 0xFF
        return intArrayOf(r, g, b)
    }

    /**
     * Get scaled volume value considering log calculation
     *
     * @param v This parameter must be 0 ~ 99<br></br>
     * If vol is lower than 0, then it will consider as 0<br></br>
     * If vol is larger than 99, then it will consider as 99<br></br>
     * @return Volume is scaled as logarithmically, it will return calculated value
     */
    fun getVolumScaler(v: Int): Float {
        var vol = v
        if (vol < 0) vol = 0
        if (vol >= 100) vol = 99
        return (1 - ln(100 - vol.toDouble()) / ln(100.0)).toFloat()
    }

    /**
     * Show Toast message using specified String message
     * @param context This parameter is used for Toast.makeText()
     * @param msg String message
     */
    @SuppressLint("ShowToast")
    fun showShortMessage(context: Context?, msg: String?) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        } else {
            toast!!.setText(msg)
        }
        toast!!.show()
    }

    /**
     * Show Toast message using specified resource ID
     * @param context Used when Toast.makeText() and getting String from resource ID
     * @param resid Resource ID of String
     */
    @SuppressLint("ShowToast")
    fun showShortMessage(context: Context, resid: Int) {
        val msg = context.getString(resid)
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        } else {
            toast!!.setText(msg)
        }
        toast!!.show()
    }

    /**
     * Show Snackbar message using resource ID
     * @param view Targeted view which snackbar will be shown
     * @param resid Resource ID of String
     */
    fun showShortSnack(view: View?, resid: Int) {
        Snackbar.make(view!!, resid, BaseTransientBottomBar.LENGTH_SHORT).show()
    }

    /**
     * Show Snackbar message with specified length using resource ID
     *
     * @param view Targeted view which snackbar will be shown
     * @param resid Resource ID of String
     * @param length Length which snackbar will be shown
     */
    fun showShortSnack(view: View?, resid: Int, length: Int) {
        val snack = Snackbar.make(view!!, resid, length)
        val v = snack.view
        val params = v.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        v.layoutParams = params
        snack.show()
    }

    /**
     * Show Snackbar message with specified String
     *
     * @param view Targeted view which snackbar will be shown
     * @param msg Message as String format
     */
    fun showShortSnack(view: View?, msg: String?) {
        Snackbar.make(view!!, msg!!, BaseTransientBottomBar.LENGTH_SHORT).show()
    }

    /**
     * Show Snackbar message with specified String and length
     *
     * @param view Targeted view which snackbar will be shown
     * @param msg Message as String format
     * @param length Length which snackbar will be shown
     */
    fun showShortSnack(view: View?, msg: String?, length: Int) {
        val snack = Snackbar.make(view!!, msg!!, length)
        val v = snack.view
        val params = v.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        v.layoutParams = params
        snack.show()
    }

    fun getExternalPath(c: Context?): String {
        if (c == null) {
            return ""
        }
        val d = c.getExternalFilesDir(null)
        return if (d != null) {
            d.absolutePath + "/"
        } else {
            ""
        }
    }

    fun getExternalTemp(c: Context?) : String {
        return getExternalPath(c) + "temp/"
    }

    fun getExternalAsset(c: Context?): String {
        return getExternalPath(c) + "assets/"
    }

    @JvmStatic
    fun getExternalPack(c: Context?): String {
        return getExternalPath(c) + "packs/"
    }

    fun getExternalLog(c: Context?): String {
        return getExternalPath(c) + "logs/"
    }

    @JvmStatic
    fun getExternalRes(c: Context?): String {
        return getExternalPath(c) + "res/"
    }

    val dataPath: String
        get() = Environment.getDataDirectory().absolutePath + "/data/com.mandarin.bcu/"

    fun getExternalWorkspace(c: Context?): String {
        return getExternalPath(c) + "workspace/"
    }

    fun getExternalUser(c: Context?): String {
        return getExternalPath(c) + "user/"
    }

    fun getExternalBackup(c: Context?) : String {
        return getExternalPath(c) + "backups/"
    }

    fun checkFolders(vararg pathes: String) {
        for (path in pathes) {
            val f = File(path)
            if (!f.exists()) {
                if (!f.mkdirs()) {
                    Log.e("checkFolders", "Failed to create directory " + f.absolutePath)
                }
            }
        }
    }

    /**
     * Encrypts png file to bcuimg files with specified password and initial vector
     * @param path Path of source file
     * @param key Password
     * @param iv Initial Vector
     * @param delete If this boolean is true, source file will be deleted
     */
    @JvmStatic
    @Throws(IOException::class, NoSuchPaddingException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class, InvalidKeyException::class, BadPaddingException::class, IllegalBlockSizeException::class)
    fun encryptPNG(path: String, key: String, iv: String, delete: Boolean) {
        val fis = FileInputStream(path)
        val encPath = path.replace(".png", ".bcuimg")
        val f = File(encPath)
        if (!f.exists()) {
            if (!f.createNewFile()) {
                Log.e("PngEncrypter", "Failed to create file $encPath")
            }
        }
        val fos = FileOutputStream(f)
        val k = key.toByteArray().copyOf(16)
        val v = iv.toByteArray()
        val sks = SecretKeySpec(k, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val parameter = IvParameterSpec(v)
        cipher.init(Cipher.ENCRYPT_MODE, sks, parameter)
        val input = ByteArray(65536)
        var i: Int
        while (fis.read(input).also { i = it } != -1) {
            val output = cipher.update(input, 0, i)
            if (output != null) {
                fos.write(output)
            }
        }
        val output = cipher.doFinal()
        if (output != null) fos.write(output)
        if (delete) {
            val g = File(path)
            if (!g.delete()) {
                Log.e("PngEncrypter", "Failed to delete source image $path")
            }
        }
        fis.close()
        fos.close()
    }

    /**
     * Decrypts bcuimg file to png file<br></br>Must be temporary for security
     * @param path Path of encrypted file
     * @param key Password
     * @param iv Initial Vector
     */
    @JvmStatic
    @Throws(IOException::class, NoSuchPaddingException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class, InvalidKeyException::class, BadPaddingException::class, IllegalBlockSizeException::class)
    fun decryptPNG(path: String, key: String, iv: String): String {
        val dirs = path.split("/".toRegex()).toTypedArray()
        val name = dirs[dirs.size - 1].replace(".bcuimg", ".png")
        val temp = Environment.getDataDirectory().absolutePath + "/data/com.mandarin.bcu/temp/"
        val g = File(temp)
        if (!g.exists()) {
            if (!g.mkdirs()) {
                Log.e("PngDecrypter", "Failed to create directory $temp")
            }
        }
        val h = File(temp, name)
        if (!h.exists()) {
            if (!h.createNewFile()) {
                Log.e("PngDecrypter", "Failed to create file " + h.absolutePath)
            }
        }
        val fis = FileInputStream(path)
        val fos = FileOutputStream(h)
        val k = key.toByteArray().copyOf(16)
        val v = iv.toByteArray()
        val sks = SecretKeySpec(k, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val parameter = IvParameterSpec(v)
        cipher.init(Cipher.DECRYPT_MODE, sks, parameter)
        val input = ByteArray(64)
        var i: Int
        while (fis.read(input).also { i = it } != -1) {
            val output = cipher.update(input, 0, i)
            if (output != null) {
                fos.write(output)
            }
        }
        val output = cipher.doFinal()
        if (output != null) {
            fos.write(output)
        }
        fis.close()
        fos.close()
        return h.absolutePath
    }

    /**
     * Converts file contents to MD5 code
     *
     * @param f File which will be converted to MD5
     * @return If file doesn't exist it will return empty String<br></br>If there were no problems, it will return converted MD5 code
     */
    @JvmStatic
    @Throws(IOException::class, NoSuchAlgorithmException::class)
    fun fileToMD5(f: File?): String {
        if (f == null || !f.exists()) return ""
        val fis = FileInputStream(f)
        val buffer = ByteArray(1024)
        val md5 = MessageDigest.getInstance("MD5")
        var n: Int
        while (fis.read(buffer).also { n = it } != -1) {
            md5.update(buffer, 0, n)
        }
        val msg = md5.digest()
        val i = BigInteger(1, msg)
        val str = i.toString(16)
        return String.format("%32s", str).replace(' ', '0')
    }

    /**
     * Get MD5 hash from specified stream
     *
     * @param stream Stream which will offer MD5 hash code
     *
     * @return returns MD5 hashed codes
     */
    @Throws(NoSuchAlgorithmException::class)
    fun streamToMD5(stream: InputStream) : String {
        val buffer = ByteArray(65536)
        val md5 = MessageDigest.getInstance("MD5")

        var len: Int

        while(stream.read(buffer).also { len = it } != -1) {
            md5.update(buffer, 0, len)
        }

        val i = BigInteger(1, md5.digest()).toString(16)

        return String.format("%32s", i).replace(' ', '0')
    }

    /**
     * Get password from specified shared preference with specified reference
     * @param name Name of shared preferences
     * @param ref Reference of password
     * @param c Context
     * @return It will return password as String if it worked properly
     */
    @JvmStatic
    fun getPassword(name: String?, ref: String?, c: Context): String? {
        val shared = c.getSharedPreferences(name, Context.MODE_PRIVATE)
        return shared.getString(ref, "")
    }

    val isEnglish: Boolean
        get() {
            val lang = Locale.getDefault().language
            return lang != "zh" && lang != "ko" && lang != "ja"
        }

    /**
     * Generate 3 digit formats
     * @param id ID of specific object
     * @return If id is 1 for example, it will return 001
     */
    fun trio(id: Int): String {
        return when {
            id < 0 -> {
                id.toString()
            }
            id < 10 -> {
                "00$id"
            }
            id < 100 -> {
                "0$id"
            }
            else -> {
                id.toString()
            }
        }
    }

    @Suppress("unchecked_cast")
    fun <T : Indexable<*, T>?> transformIdentifier(origin: Identifier<*>?): Identifier<T>? {
        return try {
            origin as Identifier<T>
        } catch (e: ClassCastException) {
            writeDriveLog(e)
            null
        }
    }

    @Suppress("unchecked_cast")
    fun <T : Indexable<*, T>?> transformIdentifier(data: String?): Identifier<T>? {
        return try {
            JsonDecoder.decode(JsonParser.parseString(data), Identifier::class.java) as Identifier<T>
        } catch (e: ClassCastException) {
            writeDriveLog(e)
            null
        }
    }

    /**
     * Returns animation type by checking specified mode
     *
     * @param mode Mode of animation
     *
     * @return Returns [UType] by checking specified [mode]. If [mode] is invalid, it will return [UType.WALK] as default
     */
    fun getAnimType(mode: Int, max: Int): UType {
        return when (mode) {
            1 -> UType.IDLE
            2 -> UType.ATK
            3 -> UType.HB
            4 -> if(max == 5)
                UType.ENTER
            else
                UType.BURROW_DOWN
            5 -> UType.BURROW_MOVE
            6 -> UType.BURROW_UP
            else -> UType.WALK
        }
    }

    /**
     * Get pack name from specified pack id
     *
     * @param id ID of pack
     *
     * @return If app can't find pack name with offered ID, it will return [id]
     */
    fun getPackName(id: String): String {
        return if (id == Identifier.DEF) {
            Identifier.DEF
        } else {
            UserProfile.getUserPack(id)?.desc?.name ?: id
        }
    }

    /**
     * This method must be used only to music which is saved in device's storage space
     *
     * @param m Music of sound effect
     *
     * @return File which leads to ogg file where specified sound effect existing
     */
    fun getMusicDataSource(m: Music?): File? {
        m ?: return null

        if(CommonStatic.ctx == null || CommonStatic.ctx !is AContext)
            return null

        val f = (CommonStatic.ctx as AContext).getMusicFile(m)

        return if(!f.exists()) {
            Log.e("StaticStore::getMusicFile","Music File not existing : ${f.absolutePath}")

            null
        } else {
            Log.i("StaticStore::getMusicFile", "Music file found : ${f.absolutePath}")
            f
        }
    }

    fun extractMusic(m: Music, target: File) : File {
        try {
            if(!target.exists()) {
                target.parentFile?.mkdirs()
                target.createNewFile()
            }

            val fos = FileOutputStream(target)

            val b = ByteArray(65536)

            var len: Int

            val inp = m.data.stream

            while(inp.read(b).also { len = it } != -1) {
                fos.write(b, 0, len)
            }

            fos.close()
            inp.close()
        } catch (e: Exception) {
            Log.e("StaticStore::extractMusic", "Couldn't extract music")
            e.printStackTrace()
        }

        return target
    }

    /**
     * Generate name with specific format with offered [id]
     *
     * @param id ID of specific object
     * @param c Context to get string
     *
     * @return Returns "Pack Name - ID" as format
     */
    fun generateIdName(id: Identifier<*>?, c: Context?) : String {
        if(id == null)
            return c?.getString(R.string.unit_info_t_none) ?: "None"

        return if(id.pack == Identifier.DEF || id.pack == "000001" || id.pack == "000002" || id.pack == "000003") {
            (c?.getString(R.string.pack_default) ?: "Default") +" - "+ Data.trio(id.id)
        } else {
            getPackName(id.pack)+" - "+Data.trio(id.id)
        }
    }

    /**
     * Generate loading texts by checking message app got
     *
     * @param ac Activity to get proper message
     * @param info Information string which app got
     *
     * @return Returns converted message string after checking [info]. If [info] has different format, just return [info]
     */
    fun getLoadingText(ac: Activity, info: String) : String {
        return when(info) {
            "loading basic images" -> ac.getString(R.string.load_bascimg)
            "loading enemies" -> ac.getString(R.string.load_enemy)
            "loading units" -> ac.getString(R.string.load_unit)
            "loading auxiliary data" -> ac.getString(R.string.load_aux)
            "loading effects" -> ac.getString(R.string.load_effect)
            "loading backgrounds" -> ac.getString(R.string.load_bg)
            "loading cat castles" -> ac.getString(R.string.load_castle)
            "loading souls" -> ac.getString(R.string.load_soul)
            "loading stages" -> ac.getString(R.string.load_stage)
            "loading orbs" -> ac.getString(R.string.load_orb)
            "loading musics" -> ac.getString(R.string.load_music)
            "process data" -> ac.getString(R.string.load_process)
            else -> info
        }
    }

    /**
     * Delete specified file
     *
     * @param f Targeted [File]
     * @param deleteItself If this is true, this method will delete [f] too
     * @return Return result whether it successfully deleted all files or not
     *
     */
    fun deleteFile(f: File, deleteItself: Boolean) : Boolean {
        var result = true

        if(f.isFile) {
            result = result and f.delete()
        } else if(f.isDirectory) {
            val lit = f.listFiles()

            if(lit != null) {
                for(g in lit) {
                    if(g.isFile) {
                        result = result and g.delete()
                    } else if(g.isDirectory) {
                        result = result and deleteFile(g, deleteItself)
                    }
                }
            }

            if(deleteItself)
                result = result and f.delete()
        }

        return result
    }

    fun fixOrientation(ac: Activity) {
        ac.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }

    fun unfixOrientation(ac: Activity) {
        ac.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    @Suppress("DEPRECATION")
    fun getScreenWidth(ac: Activity, stretch: Boolean) : Int {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val m = ac.windowManager.currentWindowMetrics
            val i = m.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())

            if(stretch) {
                m.bounds.width() - i.left - i.right
            } else {
                val cutout = m.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.displayCutout())

                m.bounds.width() - i.left - i.right - cutout.left - cutout.right
            }
        } else {
            val d = DisplayMetrics()

            ac.windowManager.defaultDisplay.getMetrics(d)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val cutout = ac.windowManager.defaultDisplay.cutout

                if(cutout != null) {
                    val rectl = cutout.boundingRectLeft
                    val rectr = cutout.boundingRectRight

                    d.widthPixels - rectl.width() - rectr.width()
                } else {
                    d.widthPixels
                }
            } else {
                d.widthPixels
            }
        }
    }

    @Suppress("DEPRECATION")
    fun getScreenHeight(ac: Activity, stretch: Boolean) : Int {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val m = ac.windowManager.currentWindowMetrics
            val i = m.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())

            if(stretch) {
                m.bounds.height() - i.top - i.bottom
            } else {
                val cutout = m.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.displayCutout())

                m.bounds.height() - i.top - i.bottom - cutout.top - cutout.bottom
            }
        } else {
            val d = DisplayMetrics()

            ac.windowManager.defaultDisplay.getMetrics(d)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val cutout = ac.windowManager.defaultDisplay.cutout

                if(cutout != null) {
                    val rectt = cutout.boundingRectTop
                    val rectb = cutout.boundingRectBottom

                    d.heightPixels - rectt.height() - rectb.height()
                } else {
                    d.heightPixels
                }
            } else {
                d.heightPixels
            }
        }
    }
}