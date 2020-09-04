package com.mandarin.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.JsonParser
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.animation.asynchs.EAnimationLoader
import com.mandarin.bcu.androidutil.animation.asynchs.UAnimationLoader
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.io.MediaScanner
import common.CommonStatic
import common.io.json.JsonDecoder
import common.pack.Identifier
import common.util.pack.Background
import common.util.stage.CastleImg
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageViewer : AppCompatActivity() {
    private val bg = 0
    private val castle = 1
    private val animu = 2
    private val anime = 3

    private val skyUpper = 0
    private val skyBelow = 1
    private val groundUpper = 2
    private val groundBelow = 3

    private var img = -1
    private var bgnum = -1
    private var form = -1

    @SuppressLint("ClickableViewAccessibility", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: Editor

        if (!shared.contains("initial")) {
            ed = shared.edit()
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.apply()
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_night)
            } else {
                setTheme(R.style.AppTheme_day)
            }
        }

        when {
            shared.getInt("Orientation", 0) == 1 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            shared.getInt("Orientation", 0) == 2 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            shared.getInt("Orientation", 0) == 0 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        if (!shared.getBoolean("DEV_MODE", false)) {
            AppWatcher.config = AppWatcher.config.copy(enabled = false)
            LeakCanary.showLeakDisplayActivityLauncherIcon(false)
        } else {
            AppWatcher.config = AppWatcher.config.copy(enabled = true)
            LeakCanary.showLeakDisplayActivityLauncherIcon(true)
        }

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_image_viewer)

        val result = intent
        val extra = result.extras ?: return

        img = extra.getInt("Img")
        bgnum = extra.getInt("BGNum")
        form = extra.getInt("Form")

        val bck = findViewById<FloatingActionButton>(R.id.imgviewerbck)
        val row = findViewById<TableRow>(R.id.palyrow)
        val seekBar = findViewById<SeekBar>(R.id.animframeseek)
        val frame = findViewById<TextView>(R.id.animframe)
        val fpsind = findViewById<TextView>(R.id.imgviewerfps)
        val gif = findViewById<TextView>(R.id.imgviewergiffr)
        val prog = findViewById<ProgressBar>(R.id.prog)

        bck.setOnClickListener {
            StaticStore.play = true
            StaticStore.frame = 0
            StaticStore.animposition = 0
            StaticStore.formposition = 0
            finish()
        }

        val anims = findViewById<Spinner>(R.id.animselect)
        val forms = findViewById<Spinner>(R.id.formselect)
        val option = findViewById<FloatingActionButton>(R.id.imgvieweroption)

        when (img) {
            bg -> {
                row.visibility = View.GONE
                seekBar.visibility = View.GONE
                frame.visibility = View.GONE
                anims.visibility = View.GONE
                fpsind.visibility = View.GONE
                gif.visibility = View.GONE
                prog.visibility = View.GONE
                forms.visibility = View.GONE

                val display = windowManager.defaultDisplay
                val size = Point()

                display.getSize(size)

                val width = size.x
                val height = size.y
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)

                paint.isFilterBitmap = true
                paint.isAntiAlias = true
                paint.isDither = true

                val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(b)

                val gh = height.toFloat() * 0.1f

                val data = StaticStore.transformIdentifier<Background>(JsonDecoder.decode(JsonParser.parseString(extra.getString("Data")), Identifier::class.java)) ?: return

                val bg = Identifier.get(data) ?: return

                bg.load()

                if(bg.top) {
                    val b1 = getImg(bg, b.height, (height.toFloat() * 2f)/ (height.toFloat() - gh))
                    val b2 = getImg2(bg, b.height, (height.toFloat() * 2f)/ (height.toFloat() - gh))
                    val h = b1.height
                    val w = b1.width
                    var i = 0

                    while (i < 1 + width / w) {
                        canvas.drawBitmap(b2, w * i.toFloat(), 0f, paint)
                        canvas.drawBitmap(b1, w * i.toFloat(), h.toFloat(), paint)
                        i++
                    }

                    val gshader: Shader = LinearGradient(0f, h.toFloat() * 2, 0f, height.toFloat(), getColorData(bg, groundUpper), getColorData(bg, groundBelow), Shader.TileMode.CLAMP)

                    paint.shader = gshader

                    canvas.drawRect(RectF(0f, h.toFloat() * 2, width.toFloat(), height.toFloat()), paint)
                } else {
                    val b1 = getImg(bg, b.height, (height.toFloat() * 2f)/ (height.toFloat() - gh))
                    val h = b1.height
                    val w = b1.width
                    var i = 0

                    while (i < 1 + width / w) {
                        canvas.drawBitmap(b1, w * i.toFloat(), h.toFloat(), paint)
                        i++
                    }

                    val shader: Shader = LinearGradient(0f, 0f, 0f, h.toFloat(), getColorData(bg, skyUpper), getColorData(bg, skyBelow), Shader.TileMode.CLAMP)

                    paint.shader = shader
                    canvas.drawRect(RectF(0f, 0f, width.toFloat(), h.toFloat()), paint)

                    val gshader: Shader = LinearGradient(0f, h.toFloat() * 2, 0f, height.toFloat(), getColorData(bg, groundUpper), getColorData(bg, groundBelow), Shader.TileMode.CLAMP)

                    paint.shader = gshader

                    canvas.drawRect(RectF(0f, h.toFloat() * 2, width.toFloat(), height.toFloat()), paint)
                }

                val img = findViewById<ImageView>(R.id.imgviewerimg)

                img.setImageBitmap(b)

                val popup = PopupMenu(this, option)
                val menu = popup.menu

                popup.menuInflater.inflate(R.menu.bg_menu, menu)

                popup.setOnMenuItemClickListener { item ->
                    if (item.itemId == R.id.anim_option_png) {
                        val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                        val date = Date()
                        val name = if(data.pack == Identifier.DEF) {
                            dateFormat.format(date) + "-BG-" + bgnum
                        } else {
                            dateFormat.format(date) + "-BG-"+ data.pack +"-"+bgnum
                        }

                        try {
                            val pngpath = MediaScanner.putImage(this@ImageViewer, b, name)

                            if(pngpath == MediaScanner.ERRR_WRONG_SDK) {
                                StaticStore.showShortMessage(this@ImageViewer, R.string.anim_png_fail)
                            } else {
                                StaticStore.showShortMessage(this@ImageViewer, getString(R.string.anim_png_success).replace("-", pngpath))
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            StaticStore.showShortMessage(this@ImageViewer, R.string.anim_png_fail)
                        }
                    }

                    false
                }

                option.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        popup.show()
                    }
                })
            }

            castle -> {
                anims.visibility = View.GONE
                row.visibility = View.GONE
                seekBar.visibility = View.GONE
                frame.visibility = View.GONE
                option.hide()
                fpsind.visibility = View.GONE
                gif.visibility = View.GONE
                prog.visibility = View.GONE
                forms.visibility = View.GONE

                val data = StaticStore.transformIdentifier<CastleImg>(JsonDecoder.decode(JsonParser.parseString(extra.getString("Data")), Identifier::class.java)) ?: return

                val c = Identifier.get(data) ?: return

                val bd = BitmapDrawable(resources, c.img.img.bimg() as Bitmap)

                bd.isFilterBitmap = true
                bd.setAntiAlias(true)

                val img = findViewById<ImageView>(R.id.imgviewerimg)
                val constraintLayout = findViewById<ConstraintLayout>(R.id.imglayout)
                val toolbar = findViewById<Toolbar>(R.id.toolbar7)

                img.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                val set = ConstraintSet()

                set.clone(constraintLayout)
                set.connect(img.id, ConstraintSet.TOP, toolbar.id, ConstraintSet.BOTTOM, 4)
                set.connect(img.id, ConstraintSet.BOTTOM, constraintLayout.id, ConstraintSet.BOTTOM, 4)
                set.connect(img.id, ConstraintSet.LEFT, constraintLayout.id, ConstraintSet.LEFT, 4)
                set.connect(img.id, ConstraintSet.RIGHT, constraintLayout.id, ConstraintSet.RIGHT, 4)
                set.applyTo(constraintLayout)

                val castle = StaticStore.getResizeb(bd.bitmap, this, bd.bitmap.width.toFloat(), bd.bitmap.height.toFloat())

                img.setImageBitmap(castle)
            }

            animu -> UAnimationLoader(this, StaticStore.transformIdentifier(extra.getString("Data")), form).execute()
            anime -> EAnimationLoader(this, StaticStore.transformIdentifier(extra.getString("Data"))).execute()
        }
    }

    private fun getImg(bg: Background, height: Int, param: Float): Bitmap {
        return try {
            val b = bg.parts(0).bimg() as Bitmap
            val ratio = height.toFloat() / param / b.height.toFloat()

            StaticStore.getResizebp(b, this, ratio * b.width, ratio * b.height)
        } catch (e: Exception) {
            ErrorLogWriter.writeLog(e, StaticStore.upload, this)
            StaticStore.empty(this, 100f, 100f)
        }
    }

    private fun getImg2(bg: Background, height: Int, param: Float): Bitmap {
        return try {
            val b = bg.parts(20).bimg() as Bitmap
            val ratio = height.toFloat() / param / b.height.toFloat()

            StaticStore.getResizebp(b, this, ratio * b.width.toFloat(), ratio * b.height.toFloat())
        } catch (e: Exception) {
            ErrorLogWriter.writeLog(e, StaticStore.upload, this)
            StaticStore.empty(this, 100f, 100f)
        }
    }

    private fun getColorData(bg: Background, mode: Int) : Int {
        bg.cs ?: return 0

        if(bg.cs.isEmpty())
            return 0

        return when(mode) {
            skyUpper -> {
                val rgb = bg.cs[0] ?: return 0
                Color.rgb(rgb[0], rgb[1], rgb[2])
            }
            skyBelow -> {
                val rgb = bg.cs[1] ?: return 0
                Color.rgb(rgb[0], rgb[1], rgb[2])
            }
            groundUpper -> {
                val rgb = bg.cs[2] ?: return 0
                Color.rgb(rgb[0], rgb[1], rgb[2])
            }
            groundBelow -> {
                val rgb = bg.cs[3] ?: return 0
                Color.rgb(rgb[0], rgb[1], rgb[2])
            }
            else -> {
                0
            }
        }
    }

    override fun onBackPressed() {
        val bck = findViewById<FloatingActionButton>(R.id.imgviewerbck)

        bck.performClick()
    }

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val lang = shared?.getInt("Language",0) ?: 0

        val config = Configuration()
        var language = StaticStore.lang[lang]
        var country = ""

        if(language == "") {
            language = Resources.getSystem().configuration.locales.get(0).language
            country = Resources.getSystem().configuration.locales.get(0).country
        }

        val loc = if(country.isNotEmpty()) {
            Locale(language, country)
        } else {
            Locale(language)
        }

        config.setLocale(loc)
        applyOverrideConfiguration(config)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    public override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }
}