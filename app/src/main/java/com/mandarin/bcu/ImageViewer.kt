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
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.animation.asynchs.EAnimationLoader
import com.mandarin.bcu.androidutil.animation.asynchs.UAnimationLoader
import com.mandarin.bcu.androidutil.io.MediaScanner
import common.system.fake.FakeImage
import common.system.files.VFile
import common.util.anim.ImgCut
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageViewer : AppCompatActivity() {
    private val bg = 0
    private val castle = 1
    private val animu = 2
    private val anime = 3
    private var path: String? = null
    private var img = -1
    private var bgnum = -1
    private var id = -1
    private var form = -1
    @SuppressLint("ClickableViewAccessibility")
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

        if (shared.getInt("Orientation", 0) == 1)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        else if (shared.getInt("Orientation", 0) == 2)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        else if (shared.getInt("Orientation", 0) == 0)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        setContentView(R.layout.activity_image_viewer)

        val result = intent
        val extra = result.extras
        if (extra != null) {
            path = extra.getString("Path")
            img = extra.getInt("Img")
            bgnum = extra.getInt("BGNum")
            id = extra.getInt("ID")
            form = extra.getInt("Form")
        }
        val bck = findViewById<FloatingActionButton>(R.id.imgviewerbck)
        val row = findViewById<TableRow>(R.id.palyrow)
        val seekBar = findViewById<SeekBar>(R.id.animframeseek)
        val frame = findViewById<TextView>(R.id.animframe)
        val fpsind = findViewById<TextView>(R.id.imgviewerfps)
        val gif = findViewById<TextView>(R.id.imgviewergiffr)
        val prog = findViewById<ProgressBar>(R.id.imgviewerprog)
        bck.setOnClickListener {
            StaticStore.play = true
            StaticStore.frame = 0
            StaticStore.animposition = 0
            StaticStore.formposition = 0
            finish()
        }
        val anims = findViewById<Spinner>(R.id.animselect)
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
                if (imgcut == 1 || imgcut == 8) {
                    val b1 = getImg(b.height, 2f)
                    val b2 = getImg2(b.height, 2f)
                    val h = b1.height
                    val w = b1.width
                    var i = 0
                    while (i < 1 + width / w) {
                        canvas.drawBitmap(b2, w * i.toFloat(), height - 2 * h.toFloat(), paint)
                        canvas.drawBitmap(b1, w * i.toFloat(), height - h.toFloat(), paint)
                        i++
                    }
                } else {
                    val b1 = getImg(b.height, 2f)
                    val h = b1.height
                    val w = b1.width
                    var i = 0
                    while (i < 1 + width / w) {
                        canvas.drawBitmap(b1, w * i.toFloat(), height - h.toFloat(), paint)
                        i++
                    }
                    val shader: Shader = LinearGradient(0f, 0f, 0f, height.toFloat() - h.toFloat(), skyUpper, skyBelow, Shader.TileMode.CLAMP)
                    paint.shader = shader
                    canvas.drawRect(RectF(0f, 0f, width.toFloat(), (height - h).toFloat()), paint)
                }
                val img = findViewById<ImageView>(R.id.imgviewerimg)
                img.setImageBitmap(b)
                val popup = PopupMenu(this, option)
                val menu = popup.menu
                popup.menuInflater.inflate(R.menu.bg_menu, menu)
                popup.setOnMenuItemClickListener { item ->
                    if (item.itemId == R.id.anim_option_png) {
                        val path = Environment.getExternalStorageDirectory().path + "/BCU/img/"
                        val f = File(path)
                        if (!f.exists()) f.mkdirs()
                        val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                        val date = Date()
                        val name = dateFormat.format(date) + "-BG-" + bgnum + ".png"
                        val g = File(path, name)
                        try {
                            if (!g.exists()) g.createNewFile()
                            val fos = FileOutputStream(g)
                            b.compress(Bitmap.CompressFormat.PNG, 100, fos)
                            fos.close()
                            MediaScanner(this@ImageViewer, g)
                            StaticStore.showShortMessage(this@ImageViewer, getString(R.string.anim_png_success).replace("-", "/BCU/img/$name"))
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
                val b2 = Objects.requireNonNull(VFile.getFile(path)).data.img.bimg() as Bitmap
                val bd = BitmapDrawable(resources, b2)
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
            animu -> UAnimationLoader(this, id, form).execute()
            anime -> EAnimationLoader(this, id).execute()
        }
    }

    private val data: Array<String>?
        get() {
            val datapath = "./org/battle/bg/bg.csv"
            val qs = Objects.requireNonNull(VFile.getFile(datapath)).data.readLine()
            for (s in qs) {
                val data = s.trim { it <= ' ' }.split(",").toTypedArray()
                try {
                    if (data[0].toInt() == bgnum) return data
                } catch (ignored: Exception) {
                }
            }
            return null
        }

    private fun getImg(height: Int, param: Float): Bitmap {
        val data = data ?: return StaticStore.empty(this, 100f, 100f)
        val imgPath: String
        imgPath = if (data[13].toInt() == 8) "./org/battle/bg/bg0" + 1 + ".imgcut" else "./org/battle/bg/bg0" + data[13] + ".imgcut"
        val img = ImgCut.newIns(imgPath)
        val f = File(path)
        return try {
            val png = FakeImage.read(f)
            val imgs = img.cut(png)
            val b = imgs[0].bimg() as Bitmap
            val ratio = height / param / b.height
            StaticStore.getResizebp(b, this, ratio * b.width, ratio * b.height)
        } catch (e: IOException) {
            e.printStackTrace()
            StaticStore.empty(this, 100f, 100f)
        }
    }

    private fun getImg2(height: Int, param: Float): Bitmap {
        val data = data ?: return StaticStore.empty(this, 100f, 100f)
        val imgPath: String
        imgPath = if (data[13].toInt() == 8) "./org/battle/bg/bg0" + 1 + ".imgcut" else "./org/battle/bg/bg0" + data[13] + ".imgcut"
        val img = ImgCut.newIns(imgPath)
        val f = File(path)
        return try {
            val png = FakeImage.read(f)
            val imgs = img.cut(png)
            val b = imgs[20].bimg() as Bitmap
            val ratio = height / param / b.height
            StaticStore.getResizebp(b, this, ratio * b.width, ratio * b.height)
        } catch (e: IOException) {
            e.printStackTrace()
            StaticStore.empty(this, 100f, 100f)
        }
    }

    private val imgcut: Int
        get() {
            val data = data ?: return -1
            return data[13].toInt()
        }

    private val skyUpper: Int
        get() {
            val data = data ?: return 0
            val r = data[1].toInt()
            val g = data[2].toInt()
            val b = data[3].toInt()
            return Color.rgb(r, g, b)
        }

    private val skyBelow: Int
        get() {
            val data = data ?: return 0
            val r = data[4].toInt()
            val g = data[5].toInt()
            val b = data[6].toInt()
            return Color.rgb(r, g, b)
        }

    private val groundUpper: Int
        get() {
            val data = data ?: return 0
            val r = data[7].toInt()
            val g = data[8].toInt()
            val b = data[9].toInt()
            return Color.rgb(r, g, b)
        }

    private val groundBelow: Int
        get() {
            val data = data ?: return 0
            val r = data[10].toInt()
            val g = data[11].toInt()
            val b = data[12].toInt()
            return Color.rgb(r, g, b)
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

        if(language == "")
            language = Resources.getSystem().configuration.locales.get(0).toString()

        config.setLocale(Locale(language))
        applyOverrideConfiguration(config)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    public override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
        mustDie(this)
    }

    fun mustDie(`object`: Any?) {
        if (MainActivity.watcher != null) {
            MainActivity.watcher!!.watch(`object`)
        }
    }
}