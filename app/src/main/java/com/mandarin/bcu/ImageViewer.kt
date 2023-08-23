package com.mandarin.bcu

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.JsonParser
import com.mandarin.bcu.androidutil.AnimatedGifEncoder
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticJava
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.animation.AnimationCView
import com.mandarin.bcu.androidutil.animation.GifSession
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.MediaScanner
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.supports.adapter.GIFRangeRecycle
import common.CommonStatic
import common.io.json.JsonDecoder
import common.pack.Identifier
import common.pack.UserProfile
import common.system.P
import common.util.Data
import common.util.anim.EAnimD
import common.util.anim.EAnimU
import common.util.pack.Background
import common.util.pack.DemonSoul
import common.util.pack.EffAnim
import common.util.pack.NyCastle
import common.util.pack.Soul
import common.util.stage.CastleImg
import common.util.unit.Enemy
import common.util.unit.Unit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.system.measureTimeMillis

class ImageViewer : AppCompatActivity() {
    enum class ViewerType {
        BACKGROUND,
        CASTLE,
        UNIT,
        ENEMY,
        EFFECT,
        SOUL,
        CANNON,
        DEMON_SOUL
    }

    companion object {
        private val animationTypeText = intArrayOf(R.string.anim_move, R.string.anim_wait, R.string.anim_atk, R.string.anim_kb, R.string.anim_burrow, R.string.anim_under, R.string.anim_burrowup)
    }

    private val skyUpper = 0
    private val skyBelow = 1
    private val groundUpper = 2
    private val groundBelow = 3

    private var previousTime = System.currentTimeMillis()
    private var fps = if (CommonStatic.getConfig().performanceMode) {
        60L
    } else {
        30L
    }
    private val targetFPS = if (CommonStatic.getConfig().performanceMode) {
        1000L / 60L
    } else {
        1000L / 30L
    }

    private val recorder = GifRecorder()

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

        LeakCanaryManager.initCanary(shared, application)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_image_viewer)

        val result = intent
        val extra = result.extras ?: return

        val img = ViewerType.valueOf(extra.getString("Img", ""))

        lifecycleScope.launch {
            //Prepare
            val anims = findViewById<Spinner>(R.id.animselect)
            val forms = findViewById<Spinner>(R.id.formselect)
            val option = findViewById<FloatingActionButton>(R.id.imgvieweroption)
            val player = findViewById<TableRow>(R.id.palyrow)
            val controller = findViewById<SeekBar>(R.id.animframeseek)
            val frame = findViewById<TextView>(R.id.animframe)
            val fpsIndicator = findViewById<TextView>(R.id.imgviewerfps)
            val gif = findViewById<TextView>(R.id.imgviewergiffr)
            val cViewlayout = findViewById<LinearLayout>(R.id.imgviewerln)
            val imageViewer = findViewById<ImageView>(R.id.imgviewerimg)
            val loadst = findViewById<TextView>(R.id.status)
            val bck = findViewById<FloatingActionButton>(R.id.imgviewerbck)
            val prog = findViewById<ProgressBar>(R.id.prog)

            loadst.setText(R.string.load_effect)

            //Load Data
            withContext(Dispatchers.IO) {
                Definer.define(this@ImageViewer, { _ -> }, { t -> runOnUiThread { loadst.text = t }})
            }

            //Load UI

            bck.setOnClickListener {
                StaticStore.play = true
                StaticStore.frame = 0f
                StaticStore.animposition = 0
                StaticStore.formposition = 0
                finish()
            }

            when (img) {
                ViewerType.BACKGROUND -> {
                    val bgnum = extra.getInt("BGNum")

                    StaticStore.setDisappear(player, controller, frame, anims, fpsIndicator, gif, prog, forms, loadst)

                    val width = StaticStore.getScreenWidth(this@ImageViewer, false)
                    val height = StaticStore.getScreenHeight(this@ImageViewer, false)
                    val paint = Paint().apply {
                        isFilterBitmap = true
                        isAntiAlias = true
                        isDither = true
                    }

                    val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(b)

                    val data = StaticStore.transformIdentifier<Background>(JsonDecoder.decode(JsonParser.parseString(extra.getString("Data")), Identifier::class.java)) ?: return@launch

                    val bg = Identifier.get(data) ?: return@launch

                    bg.load()

                    val cv = CVGraphics(canvas, Paint(), paint, false)

                    if(bg.top) {
                        val groundPart = bg.parts[Background.BG]
                        val skyPart = bg.parts[Background.TOP]

                        val totalHeight = groundPart.height + skyPart.height

                        //This must take 80% of viewer height
                        val ratio = height * 0.8 / totalHeight.toDouble()

                        val w = groundPart.width * ratio

                        var i = 0

                        while (i < 1 + width / w) {
                            cv.drawImage(skyPart, w * i, height * 0.1, w, skyPart.height * ratio)
                            cv.drawImage(groundPart, w * i, height * 0.1 + skyPart.height * ratio, w, groundPart.height * ratio)

                            i++
                        }

                        cv.gradRect(0, round(height * 0.9).toInt(), width, round(height * 0.1).toInt(), 0, round(height * 0.9).toInt(), getColorData(bg, groundUpper), 0, height, getColorData(bg, groundBelow))
                        cv.gradRect(0, 0, width, round(height * 0.1).toInt(), 0, 0, getColorData(bg, skyUpper), 0, round(height * 0.1).toInt(), getColorData(bg, skyBelow))
                    } else {
                        val groundPart = bg.parts[Background.BG]

                        //This must take 80% of viewer height
                        val ratio = height * 0.8 / (groundPart.height * 2.0)

                        val w = groundPart.width * ratio

                        var i = 0

                        while (i < 1 + width / w) {
                            cv.drawImage(groundPart, w * i, height * 0.1 + groundPart.height * ratio, w, groundPart.height * ratio)

                            i++
                        }

                        cv.gradRect(0, round(height * 0.9).toInt(), width, round(height * 0.1).toInt(), 0, round(height * 0.9).toInt(), getColorData(bg, groundUpper), 0, height, getColorData(bg, groundBelow))
                        cv.gradRect(0, 0, width, round(height * 0.5).toInt(), 0, 0, getColorData(bg, skyUpper), 0, round(height * 0.5).toInt(), getColorData(bg, skyBelow))
                    }

                    imageViewer.setImageBitmap(b)

                    val popup = PopupMenu(this@ImageViewer, option)
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
                                val pngPath = MediaScanner.putImage(this@ImageViewer, b, name)

                                if(pngPath == MediaScanner.ERRR_WRONG_SDK) {
                                    StaticStore.showShortMessage(this@ImageViewer, R.string.anim_png_fail)
                                } else {
                                    StaticStore.showShortMessage(this@ImageViewer, getString(R.string.anim_png_success).replace("-", pngPath))
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
                ViewerType.CASTLE -> {
                    StaticStore.setDisappear(anims, player, controller, frame, fpsIndicator, gif, prog, forms, loadst)
                    option.hide()

                    val data = StaticStore.transformIdentifier<CastleImg>(JsonDecoder.decode(JsonParser.parseString(extra.getString("Data")), Identifier::class.java)) ?: return@launch

                    val c = Identifier.get(data) ?: return@launch

                    val bd = BitmapDrawable(resources, c.img.img.bimg() as Bitmap)

                    bd.isFilterBitmap = true
                    bd.setAntiAlias(true)

                    val constraintLayout = findViewById<ConstraintLayout>(R.id.imglayout)
                    val toolbar = findViewById<Toolbar>(R.id.toolbar7)

                    imageViewer.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                    val set = ConstraintSet()

                    set.clone(constraintLayout)

                    set.connect(imageViewer.id, ConstraintSet.TOP, toolbar.id, ConstraintSet.BOTTOM, 4)
                    set.connect(imageViewer.id, ConstraintSet.BOTTOM, constraintLayout.id, ConstraintSet.BOTTOM, 4)
                    set.connect(imageViewer.id, ConstraintSet.LEFT, constraintLayout.id, ConstraintSet.LEFT, 4)
                    set.connect(imageViewer.id, ConstraintSet.RIGHT, constraintLayout.id, ConstraintSet.RIGHT, 4)

                    set.applyTo(constraintLayout)

                    val castle = StaticStore.getResizeb(bd.bitmap, this@ImageViewer, bd.bitmap.width.toFloat(), bd.bitmap.height.toFloat())

                    imageViewer.setImageBitmap(castle)
                }
                else -> {
                    StaticStore.setDisappear(anims, forms, option, player, controller, frame, fpsIndicator, gif, cViewlayout, imageViewer)

                    val index = extra.getInt("Index")
                    val form = extra.getInt("Form")

                    val type = when(img) {
                        ViewerType.UNIT -> AnimationCView.AnimationType.UNIT
                        ViewerType.ENEMY -> AnimationCView.AnimationType.ENEMY
                        ViewerType.EFFECT -> AnimationCView.AnimationType.EFFECT
                        ViewerType.SOUL -> AnimationCView.AnimationType.SOUL
                        ViewerType.CANNON -> AnimationCView.AnimationType.CANNON
                        ViewerType.DEMON_SOUL -> AnimationCView.AnimationType.DEMON_SOUL
                        else -> throw IllegalStateException("Invalid unreachable type $img found")
                    }

                    val content = when(type) {
                        AnimationCView.AnimationType.UNIT -> {
                            StaticStore.transformIdentifier(extra.getString("Data")) ?: return@launch
                        }
                        AnimationCView.AnimationType.ENEMY -> {
                            StaticStore.transformIdentifier(extra.getString("Data")) ?: return@launch
                        }
                        AnimationCView.AnimationType.EFFECT -> {
                            CommonStatic.getBCAssets().effas.values()[index]
                        }
                        AnimationCView.AnimationType.SOUL -> {
                            UserProfile.getBCData().souls.list[index]
                        }
                        AnimationCView.AnimationType.CANNON -> {
                            CommonStatic.getBCAssets().atks[index]
                        }
                        AnimationCView.AnimationType.DEMON_SOUL -> {
                            UserProfile.getBCData().demonSouls.list[index]
                        }
                    }

                    val pack = if (type == AnimationCView.AnimationType.UNIT || type == AnimationCView.AnimationType.ENEMY) {
                        if ((content as Identifier<*>).pack == Identifier.DEF) {
                            "Default"
                        } else {
                            content.pack
                        }
                    } else {
                        "Default"
                    }

                    val entityId = if (type == AnimationCView.AnimationType.UNIT || type == AnimationCView.AnimationType.ENEMY) {
                        (content as Identifier<*>).id
                    } else {
                        -1
                    }

                    recorder.session = GifSession(recorder, type, content, pack, entityId)

                    val buttons = arrayOf<FloatingActionButton>(findViewById(R.id.animbackward), findViewById(R.id.animplay), findViewById(R.id.animforward))

                    val cView = AnimationCView(
                        this@ImageViewer,
                        content,
                        recorder.session,
                        type,
                        if (type == AnimationCView.AnimationType.UNIT) form else index,
                        !shared.getBoolean("theme", false),
                        shared.getBoolean("Axis", true),
                        gif
                    )
                    cView.size = StaticStore.dptopx(1f, this@ImageViewer).toFloat() / 1.25f
                    cView.id = R.id.animationView

                    val scaleListener = ScaleListener(cView)
                    val detector = ScaleGestureDetector(this@ImageViewer, scaleListener)

                    cView.setOnTouchListener(object : View.OnTouchListener {
                        var preid = -1

                        var preX = 0f
                        var preY = 0f

                        override fun onTouch(v: View, event: MotionEvent): Boolean {
                            detector.onTouchEvent(event)

                            if (preid == -1)
                                preid = event.getPointerId(0)

                            val id = event.getPointerId(0)

                            val x = event.x
                            val y = event.y

                            if (event.action == MotionEvent.ACTION_DOWN) {
                                scaleListener.updateScale = true
                            }

                            if (event.action == MotionEvent.ACTION_MOVE) {
                                if (event.pointerCount == 1 && id == preid) {
                                    val dx = x - preX
                                    val dy = y - preY

                                    cView.posx += dx
                                    cView.posy += dy
                                }
                            }

                            preX = x
                            preY = y

                            preid = id

                            return true
                        }
                    })

                    val name = generateNames(content)

                    val adapter = ArrayAdapter(this@ImageViewer, R.layout.spinneradapter, name)

                    anims.adapter = adapter

                    cView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    cViewlayout.addView(cView)

                    if (type == AnimationCView.AnimationType.UNIT) {
                        StaticStore.formposition = form

                        val u = (content as Identifier<*>).get() ?: return@launch

                        if (u !is Unit)
                            return@launch

                        val formAdapter = ArrayAdapter(this@ImageViewer, R.layout.spinneradapter, u.forms.map {
                            f -> if (content.pack == Identifier.DEF)
                                "Default-${content.id}-${f.fid}"
                            else
                                "${content.pack}-${content.id}-${f.fid}"
                        })

                        forms.adapter = formAdapter
                        forms.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, ids: Long) {
                                if (StaticStore.formposition != position) {
                                    StaticStore.formposition = position
                                    cView.anim = u.forms[position].getEAnim(StaticStore.getAnimType(anims.selectedItemPosition, u.forms[position].anim.anims.size))

                                    val max = cView.anim.len()

                                    controller.max = if (CommonStatic.getConfig().performanceMode) {
                                        (max - 1) * 2
                                    } else {
                                        max - 1
                                    }

                                    controller.progress = 0
                                    StaticStore.frame = 0f
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }

                        forms.setSelection(form)
                    }

                    anims.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            if (StaticStore.animposition != position) {
                                StaticStore.animposition = position

                                if (type == AnimationCView.AnimationType.UNIT || type == AnimationCView.AnimationType.ENEMY) {
                                    (cView.anim as EAnimU).changeAnim(StaticStore.getAnimType(position, (cView.anim as EAnimU).anim().anims.size), false)
                                } else {
                                     cView.anim = StaticJava.generateEAnimD(content, position)
                                }

                                controller.max = if (CommonStatic.getConfig().performanceMode) {
                                    cView.anim.len() * 2
                                } else {
                                    cView.anim.len()
                                }
                                controller.progress = 0

                                StaticStore.frame = 0f
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                    buttons[1].setOnClickListener {
                        frame.setTextColor(StaticStore.getAttributeColor(this@ImageViewer, R.attr.TextPrimary))

                        if (StaticStore.play) {
                            buttons[1].setImageDrawable(ContextCompat.getDrawable(this@ImageViewer, R.drawable.ic_play_arrow_black_24dp))

                            buttons[0].show()
                            buttons[2].show()

                            controller.isEnabled = true
                        } else {
                            buttons[1].setImageDrawable(ContextCompat.getDrawable(this@ImageViewer, R.drawable.ic_pause_black_24dp))

                            buttons[0].hide()
                            buttons[2].hide()

                            controller.isEnabled = false
                        }

                        StaticStore.play = !StaticStore.play
                    }

                    buttons[0].setOnClickListener {
                        buttons[2].isEnabled = false

                        if (StaticStore.frame > 0) {
                            if (CommonStatic.getConfig().performanceMode) {
                                StaticStore.frame -= 0.5f
                            } else {
                                StaticStore.frame--
                            }

                            cView.anim.setTime(StaticStore.frame)
                        } else {
                            frame.setTextColor(Color.rgb(227, 66, 66))

                            StaticStore.showShortMessage(this@ImageViewer, R.string.anim_warn_frame)
                        }

                        buttons[2].isEnabled = true
                    }

                    buttons[2].setOnClickListener {
                        buttons[0].isEnabled = false

                        if (CommonStatic.getConfig().performanceMode) {
                            StaticStore.frame += 0.5f
                        } else {
                            StaticStore.frame++
                        }

                        cView.anim.setTime(StaticStore.frame)

                        frame.setTextColor(StaticStore.getAttributeColor(this@ImageViewer, R.attr.TextPrimary))

                        buttons[0].isEnabled = true
                    }

                    controller.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(controller: SeekBar, progress: Int, fromUser: Boolean) {
                            if (fromUser) {
                                StaticStore.frame = if (CommonStatic.getConfig().performanceMode) {
                                    progress / 2f
                                } else {
                                    progress.toFloat()
                                }

                                cView.anim.setTime(StaticStore.frame)
                            }
                        }

                        override fun onStartTrackingTouch(controller: SeekBar) {}
                        override fun onStopTrackingTouch(controller: SeekBar) {}
                    })

                    frame.text = getString(R.string.anim_frame).replace("-", "" + StaticStore.frame)

                    controller.progress = if (CommonStatic.getConfig().performanceMode) {
                        (StaticStore.frame * 2).toInt()
                    } else {
                        StaticStore.frame.toInt()
                    }

                    anims.setSelection(StaticStore.animposition)

                    if (type != AnimationCView.AnimationType.UNIT && type != AnimationCView.AnimationType.ENEMY) {
                        cView.anim = StaticJava.generateEAnimD(content, StaticStore.animposition)
                    }

                    cView.anim.setTime(StaticStore.frame)

                    controller.max = if (CommonStatic.getConfig().performanceMode) {
                        cView.anim.len() * 2
                    } else {
                        cView.anim.len()
                    }

                    val popup = PopupMenu(this@ImageViewer, option)
                    val menu = popup.menu

                    popup.menuInflater.inflate(R.menu.animation_menu, menu)

                    popup.menu.getItem(3).isEnabled = StaticStore.enableGIF

                    popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.anim_option_reset -> {
                                cView.posx = 0f
                                cView.posy = 0f

                                cView.size = StaticStore.dptopx(1f, this@ImageViewer) / 1.25f

                                return@OnMenuItemClickListener true
                            }
                            R.id.anim_png_normal -> {
                                val b = Bitmap.createBitmap(cView.width, cView.height, Bitmap.Config.ARGB_8888)
                                val c = Canvas(b)

                                val p = Paint()

                                if (!shared.getBoolean("theme", false))
                                    p.color = Color.argb(255, 54, 54, 54)
                                else
                                    p.color = Color.argb(255, 255, 255, 255)

                                c.drawRect(0f, 0f, b.width.toFloat(), b.height.toFloat(), p)

                                cView.draw(c)

                                val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                                val date = Date()

                                val imageName = when(content) {
                                    is Identifier<*> -> {
                                        val packName = if (content.pack == Identifier.DEF) {
                                            "Default"
                                        } else {
                                            content.pack
                                        }

                                        if (type == AnimationCView.AnimationType.UNIT) {
                                            "${dateFormat.format(date)}-F-$packName-${StaticStore.trio(content.id)}-${Data.trio(StaticStore.formposition)}"
                                        } else {
                                            "${dateFormat.format(date)}-E-$packName-${StaticStore.trio(content.id)}"
                                        }
                                    }
                                    is EffAnim<*> -> {
                                        "${dateFormat.format(date)}-EFF-Default-${StaticStore.trio(index)}"
                                    }
                                    is Soul -> {
                                        "${dateFormat.format(date)}-S-Default-${StaticStore.trio(index)}"
                                    }
                                    is NyCastle -> {
                                        "${dateFormat.format(date)}-C-Default-${StaticStore.trio(index)}"
                                    }
                                    is DemonSoul -> {
                                        "${dateFormat.format(date)}-DS-Default-${StaticStore.trio(index)}"
                                    }
                                    else -> {
                                        throw IllegalStateException("E/ImageViewer::onCreate - Invalid content type : ${content::class.java.name}")
                                    }
                                }

                                try {
                                    val path = MediaScanner.putImage(this@ImageViewer, b, imageName)

                                    if(path == MediaScanner.ERRR_WRONG_SDK) {
                                        StaticStore.showShortMessage(this@ImageViewer, R.string.anim_png_fail)
                                    } else {
                                        StaticStore.showShortMessage(this@ImageViewer, getString(R.string.anim_png_success).replace("-", path))
                                    }
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                    StaticStore.showShortMessage(this@ImageViewer, R.string.anim_png_fail)
                                }

                                return@OnMenuItemClickListener true
                            }
                            R.id.anim_png_transp -> {
                                val b = Bitmap.createBitmap(cView.width, cView.height, Bitmap.Config.ARGB_8888)
                                val c = Canvas(b)

                                val p = Paint()

                                if (!shared.getBoolean("theme", false))
                                    p.color = Color.argb(255, 54, 54, 54)
                                else
                                    p.color = Color.argb(255, 255, 255, 255)

                                cView.trans = true

                                cView.draw(c)

                                cView.trans = false

                                val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                                val date = Date()

                                val imageName = when(content) {
                                    is Identifier<*> -> {
                                        val packName = if (content.pack == Identifier.DEF) {
                                            "Default"
                                        } else {
                                            content.pack
                                        }

                                        if (type == AnimationCView.AnimationType.UNIT) {
                                            "${dateFormat.format(date)}-F-Trans-$packName-${StaticStore.trio(content.id)}-${Data.trio(StaticStore.formposition)}"
                                        } else {
                                            "${dateFormat.format(date)}-E-Trans-$packName-${StaticStore.trio(content.id)}"
                                        }
                                    }
                                    is EffAnim<*> -> {
                                        "${dateFormat.format(date)}-EFF-Trans-Default-${StaticStore.trio(index)}"
                                    }
                                    is Soul -> {
                                        "${dateFormat.format(date)}-S-Trans-Default-${StaticStore.trio(index)}"
                                    }
                                    is NyCastle -> {
                                        "${dateFormat.format(date)}-C-Trans-Default-${StaticStore.trio(index)}"
                                    }
                                    is DemonSoul -> {
                                        "${dateFormat.format(date)}-DS-Default-${StaticStore.trio(index)}"
                                    }
                                    else -> {
                                        throw IllegalStateException("E/ImageViewer::onCreate - Invalid content type : ${content::class.java.name}")
                                    }
                                }

                                try {
                                    val path = MediaScanner.putImage(this@ImageViewer, b, imageName)

                                    if(path == MediaScanner.ERRR_WRONG_SDK) {
                                        StaticStore.showShortMessage(this@ImageViewer, R.string.anim_png_fail)
                                    } else {
                                        StaticStore.showShortMessage(this@ImageViewer, getString(R.string.anim_png_success).replace("-", path))
                                    }
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                    StaticStore.showShortMessage(this@ImageViewer, R.string.anim_png_fail)
                                }
                                return@OnMenuItemClickListener true
                            }
                            R.id.anim_gif_normal -> {
                                if (!StaticStore.gifisSaving) {
                                    gif.visibility = View.VISIBLE

                                    popup.menu.getItem(3).isEnabled = true
                                    popup.menu.getItem(2).isEnabled = false

                                    StaticStore.enableGIF = !StaticStore.enableGIF

                                    recorder.session.startSession(this@ImageViewer)
                                } else {
                                    StaticStore.showShortMessage(this@ImageViewer, R.string.gif_saving)
                                }

                                return@OnMenuItemClickListener true
                            }
                            R.id.anim_option_gif_finish -> {
                                StaticStore.enableGIF = !StaticStore.enableGIF
                                StaticStore.gifisSaving = true

                                item.isEnabled = false

                                popup.menu.getItem(2).isEnabled = true

                                recorder.session.closeSession()

                                return@OnMenuItemClickListener true
                            }
                            R.id.anim_gif_range -> {
                                if(!StaticStore.gifisSaving) {
                                    StaticStore.fixOrientation(this@ImageViewer)

                                    val builder = AlertDialog.Builder(this@ImageViewer)
                                    val inflater = LayoutInflater.from(this@ImageViewer)
                                    val v = inflater.inflate(R.layout.gif_range_dialog, null)

                                    builder.setView(v)

                                    val list = v.findViewById<RecyclerView>(R.id.gifrecycle)
                                    val gen = v.findViewById<Button>(R.id.gifgen)
                                    val can = v.findViewById<Button>(R.id.gifcancel)

                                    val gifAdapter = GIFRangeRecycle(name, this@ImageViewer, type, content, StaticStore.formposition)

                                    list.adapter = gifAdapter

                                    list.layoutManager = LinearLayoutManager(this@ImageViewer)
                                    list.isNestedScrollingEnabled = false

                                    val dialog = builder.create()

                                    dialog.show()

                                    dialog.setOnDismissListener {
                                        StaticStore.unfixOrientation(this@ImageViewer)
                                    }

                                    gen.setOnClickListener(object : SingleClick() {
                                        override fun onSingleClick(v: View?) {
                                            StaticStore.gifisSaving = true

                                            gif.visibility = View.VISIBLE

                                            dialog.dismiss()

                                            lifecycleScope.launch {
                                                withContext(Dispatchers.IO) {
                                                    when(type) {
                                                        AnimationCView.AnimationType.UNIT -> {
                                                            recorder.recordGifWithRange(cView, type, gifAdapter.getData(), gifAdapter.getEnables(), !shared.getBoolean("theme", false), content, id = (content as Identifier<*>).id, form = StaticStore.formposition)
                                                        }
                                                        AnimationCView.AnimationType.ENEMY -> {
                                                            recorder.recordGifWithRange(cView, type, gifAdapter.getData(), gifAdapter.getEnables(), !shared.getBoolean("theme", false), content, id = (content as Identifier<*>).id)
                                                        }
                                                        else -> {
                                                            recorder.recordGifWithRange(cView, type, gifAdapter.getData(), gifAdapter.getEnables(), !shared.getBoolean("theme", false), content, id = index)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    })

                                    can.setOnClickListener(object : SingleClick() {
                                        override fun onSingleClick(v: View?) {
                                            dialog.dismiss()
                                        }
                                    })
                                } else {
                                    StaticStore.showShortMessage(this@ImageViewer, R.string.gif_saving)
                                }

                                return@OnMenuItemClickListener true
                            }
                        }
                        false
                    })

                    option.setOnClickListener(object : SingleClick() {
                        override fun onSingleClick(v: View?) {
                            popup.show()
                        }
                    })

                    bck.setOnClickListener {
                        if (!StaticStore.gifisSaving) {
                            StaticStore.play = true
                            StaticStore.frame = 0f
                            StaticStore.animposition = 0
                            StaticStore.formposition = 0
                            StaticStore.enableGIF = false
                            StaticStore.gifFrame = 0

                            recorder.session.closeSession()
                            recorder.frame = 0
                            recorder.encoder = AnimatedGifEncoder()
                            recorder.bos = ByteArrayOutputStream()

                            finish()
                        } else {
                            val builder = AlertDialog.Builder(this@ImageViewer)

                            builder.setTitle(R.string.anim_gif_warn)
                            builder.setMessage(R.string.anim_gif_recording)
                            builder.setPositiveButton(R.string.gif_exit) { _, _ ->
                                StaticStore.play = true
                                StaticStore.frame = 0f
                                StaticStore.animposition = 0
                                StaticStore.formposition = 0
                                StaticStore.keepDoing = false
                                StaticStore.enableGIF = false
                                StaticStore.gifisSaving = false
                                StaticStore.gifFrame = 0

                                StaticStore.showShortMessage(this@ImageViewer, R.string.anim_gif_cancel)

                                recorder.session.closeSession()
                                recorder.frame = 0
                                recorder.encoder = AnimatedGifEncoder()
                                recorder.bos = ByteArrayOutputStream()

                                finish()
                            }

                            builder.setNegativeButton(R.string.main_file_cancel) { _, _ -> }

                            val dialog = builder.create()

                            dialog.show()
                        }
                    }

                    StaticStore.setAppear(anims, option, player, controller, frame, fpsIndicator, cViewlayout)
                    StaticStore.setDisappear(prog, loadst)

                    if (type == AnimationCView.AnimationType.UNIT) {
                        StaticStore.setAppear(forms)
                    }

                    if (StaticStore.enableGIF || StaticStore.gifisSaving)
                        StaticStore.setAppear(gif)

                    if (StaticStore.play) {
                        buttons[0].hide()
                        buttons[2].hide()

                        controller.isEnabled = false
                    } else {
                        buttons[1].setImageDrawable(ContextCompat.getDrawable(this@ImageViewer, R.drawable.ic_pause_black_24dp))
                    }

                    if (!shared.getBoolean("FPS", true))
                        StaticStore.setDisappear(fpsIndicator)

                    activateAnimator()
                }
            }

            onBackPressedDispatcher.addCallback(this@ImageViewer, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })
        }
    }

    private fun getColorData(bg: Background, mode: Int) : IntArray {
        bg.cs ?: return intArrayOf(0, 0, 0)

        if(bg.cs.isEmpty())
            return intArrayOf(0, 0, 0)

        return when(mode) {
            skyUpper -> {
                val rgb = bg.cs[0] ?: return intArrayOf(0, 0, 0)
                intArrayOf(rgb[0], rgb[1], rgb[2])
            }
            skyBelow -> {
                val rgb = bg.cs[1] ?: return intArrayOf(0, 0, 0)
                intArrayOf(rgb[0], rgb[1], rgb[2])
            }
            groundUpper -> {
                val rgb = bg.cs[2] ?: return intArrayOf(0, 0, 0)
                intArrayOf(rgb[0], rgb[1], rgb[2])
            }
            groundBelow -> {
                val rgb = bg.cs[3] ?: return intArrayOf(0, 0, 0)
                intArrayOf(rgb[0], rgb[1], rgb[2])
            }
            else -> {
                intArrayOf(0, 0, 0)
            }
        }
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

    override fun onDestroy() {
        super.onDestroy()

        StaticStore.toast = null
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }

    private fun generateNames(content: Any) : ArrayList<String> {
        val res = ArrayList<String>()

        when(content) {
            is Identifier<*> -> {
                val entity = content.get() ?: return res

                val animationTypes = when (entity) {
                    is Unit -> {
                        entity.forms[0].anim.anims
                    }
                    is Enemy -> {
                        entity.anim.anims
                    }
                    else -> {
                        return res
                    }
                }

                animationTypes.forEachIndexed { index, _ ->
                    if(index == 4 && animationTypes.size == 5)
                        res.add(getString(R.string.anim_entry))
                    else
                        res.add(getString(animationTypeText[index]))
                }
            }
            is EffAnim<*> -> {
                for(t in content.names()) {
                    res.add(seekName(t))
                }
            }
            is Soul -> {
                res.add(seekName("default"))
            }
            is NyCastle -> {
                for(t in content.names()) {
                    res.add(seekName(t))
                }
            }
            is DemonSoul -> {
                for(t in content.anim.names()) {
                    res.add(seekName(t))
                }
            }
        }

        return res
    }

    private fun seekName(name: String?) : String {
        name ?: getString(R.string.eff_def)

        return when(name) {
            "default" -> getString(R.string.eff_def)
            "break" -> getString(R.string.eff_break)
            "destroy" -> getString(R.string.eff_destroy)
            "start" -> getString(R.string.eff_start)
            "on" -> getString(R.string.eff_on)
            "end" -> getString(R.string.eff_end)
            "enter" -> getString(R.string.eff_enter)
            "exit" -> getString(R.string.eff_exit)
            "revive" -> getString(R.string.eff_revive)
            "knock down" -> getString(R.string.eff_kd)
            "knock back" -> getString(R.string.eff_kb)
            "shockwave" -> getString(R.string.eff_shock)
            "sniper" -> getString(R.string.eff_sn)
            "idle" -> getString(R.string.eff_idle)
            "attack" -> getString(R.string.eff_satk)
            "buff" -> getString(R.string.eff_buff)
            "debuff" -> getString(R.string.eff_dbuff)
            "increase" -> getString(R.string.eff_inc)
            "decrease" -> getString(R.string.eff_dec)
            "castle" -> getString(R.string.eff_castle)
            "atk" -> getString(R.string.eff_catk)
            "ext" -> getString(R.string.eff_ext)
            "none" -> getString(R.string.eff_none)
            "full" -> getString(R.string.eff_full)
            "half" -> getString(R.string.eff_half)
            "breaker" -> getString(R.string.eff_breaker)
            "broken" -> getString(R.string.eff_broken)
            "regeneration" -> getString(R.string.eff_regen)
            "fail" -> getString(R.string.eff_fail)
            "success" -> getString(R.string.eff_success)
            "back" -> getString(R.string.eff_back)
            else -> getString(R.string.eff_def)
        }
    }

    private fun activateAnimator() {
        lifecycleScope.launch {
            val controller = findViewById<SeekBar>(R.id.animframeseek)
            val frameIndicator = findViewById<TextView>(R.id.animframe)
            val animator = findViewById<AnimationCView>(R.id.animationView)
            val fpsIndicator = findViewById<TextView>(R.id.imgviewerfps)
            val gif = findViewById<TextView>(R.id.imgviewergiffr)

            val targetTime = if (CommonStatic.getConfig().performanceMode) {
                60L
            } else {
                30L
            }

            withContext(Dispatchers.IO) {
                while(true) {
                    if (!animator.started)
                        continue

                    val time = measureTimeMillis {
                        animator.postInvalidate()

                        withContext(Dispatchers.Main) {
                            frameIndicator.text = getText(R.string.anim_frame).toString().replace("-", "" + StaticStore.frame)
                            fpsIndicator.text = getText(R.string.def_fps).toString().replace("-", "" + fps)
                        }

                        val maxValue = if (CommonStatic.getConfig().performanceMode) {
                            controller.max / 2f
                        } else {
                            controller.max * 1f
                        }

                        controller.progress = if(StaticStore.frame >= maxValue && StaticStore.play) {
                            StaticStore.frame = 0f
                            0
                        } else {
                            if (CommonStatic.getConfig().performanceMode) {
                                (StaticStore.frame * 2).toInt()
                            } else {
                                StaticStore.frame.toInt()
                            }
                        }

                        if(StaticStore.enableGIF || StaticStore.gifisSaving) {
                            val giftext = if (StaticStore.gifFrame != 0)
                                getText(R.string.anim_gif_frame).toString().replace("-", "" + StaticStore.gifFrame) + " (" + (recorder.frame.toFloat() / StaticStore.gifFrame.toFloat() * 100f).toInt() + "%)"
                            else
                                getText(R.string.anim_gif_frame).toString().replace("-", "" + StaticStore.gifFrame)

                            withContext(Dispatchers.Main) {
                                gif.text = giftext
                            }
                        }

                        fps = min(targetTime, 1000L / max(1L, System.currentTimeMillis() - previousTime))
                        previousTime = System.currentTimeMillis()
                    }

                    delay(max(0, targetFPS - time))
                }
            }
        }
    }

    inner class ScaleListener(private val cView: AnimationCView) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        var updateScale = false

        private var realFX = 0f
        private var previousX = 0f

        private var realFY = 0f
        private var previousY = 0f

        private var previousScale = 0f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            cView.size *= detector.scaleFactor

            val diffX = (realFX - previousX) * (cView.size / previousScale - 1)
            val diffY = (realFY - previousY) * (cView.size / previousScale - 1)

            cView.posx = previousX - diffX
            cView.posy = previousY - diffY

            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            if (updateScale) {
                realFX = detector.focusX - cView.width / 2f
                previousX = cView.posx

                realFY = detector.focusY - cView.height * 2f / 3f
                previousY = cView.posy

                previousScale = cView.size

                updateScale = false
            }

            return super.onScaleBegin(detector)
        }
    }

    inner class GifRecorder {
        lateinit var session: GifSession

        var frame = 0
        var bos = ByteArrayOutputStream()
        var encoder = AnimatedGifEncoder()

        fun recordGifWithRange(view: AnimationCView, type: AnimationCView.AnimationType, frames: ArrayList<Array<Int>>, enabled: Array<Boolean>, isNightMode: Boolean, data: Any, id: Int = 0, form: Int = 0, pack: String = "000000") {
            checkValidClasses(data, type)

            val targetFPS = if (CommonStatic.getConfig().performanceMode) {
                60f
            } else {
                30f
            }

            if(encoder.frameRate != targetFPS) {
                encoder.frameRate = targetFPS

                encoder.start(bos)

                encoder.setRepeat(0)
            }

            val gif = findViewById<TextView>(R.id.imgviewergiffr)

            StaticStore.setAppear(gif)

            val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

            val ratio = shared.getInt("gif", 100).toDouble() / 100.0

            val w = (view.width * ratio).toInt()
            val h = (view.height * ratio).toInt()
            val siz = view.size.toDouble() * ratio

            val p = P.newP((view.width.toFloat() / 2 + view.posx).toDouble(), (view.height.toFloat() * 2 / 3 + view.posy).toDouble()).apply {
                x *= ratio
                y *= ratio
            }

            frame = 0

            StaticStore.gifFrame = getTotalFrame(frames, enabled)

            if(!StaticStore.keepDoing) {
                return
            }

            frames.forEachIndexed { i, range ->
                if(!StaticStore.keepDoing) {
                    return@forEachIndexed
                }

                if (!enabled[i])
                    return@forEachIndexed

                val anim = if (type == AnimationCView.AnimationType.UNIT) {
                    getEanimD(type, i, data, form = StaticStore.formposition)
                } else {
                    getEanimD(type, i, data)
                }

                val bitmapPaint = Paint()
                val bp = Paint()
                val back = Paint()

                back.color = StaticStore.getAttributeColor(this@ImageViewer, R.attr.backgroundPrimary)

                bitmapPaint.isFilterBitmap = true
                bitmapPaint.isAntiAlias = true

                for(j in range[0]..range[1]) {
                    if(!StaticStore.keepDoing) {
                        break
                    }

                    val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    val c = Canvas(b)

                    val cv = CVGraphics(c, bp, bitmapPaint, isNightMode)
                    cv.independent = true

                    c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), back)

                    anim.setTime(if (CommonStatic.getConfig().performanceMode) {
                        j / 2f
                    } else {
                        j.toFloat()
                    })
                    anim.draw(cv, p, siz)

                    encoder.addFrame(b)

                    frame++

                    val gifText = if (StaticStore.gifFrame != 0)
                        getText(R.string.anim_gif_frame).toString().replace("-", "" + StaticStore.gifFrame) + " (" + (frame.toFloat() / StaticStore.gifFrame.toFloat() * 100f).toInt() + "%)"
                    else
                        getText(R.string.anim_gif_frame).toString().replace("-", "" + StaticStore.gifFrame)

                    runOnUiThread {
                        gif.text = gifText
                    }
                }
            }

            encoder.finish()

            saveGif(type, pack, Data.trio(id), Data.trio(form))
        }

        fun saveGif(type: AnimationCView.AnimationType, pack: String = "", id: String = "000", form: String = "000") {
            val buffer = bos.toByteArray()

            val name = generateName(type, pack, id, form)

            val result = if(StaticStore.keepDoing) {
                try {
                    MediaScanner.writeGIF(this@ImageViewer, buffer, name)
                } catch (e: IOException) {
                    e.printStackTrace()

                    MediaScanner.ERRR_WRONG_SDK
                }
            } else {
                MediaScanner.ERRR_WRONG_SDK
            }

            val gif = findViewById<TextView>(R.id.imgviewergiffr)

            runOnUiThread {
                StaticStore.setDisappear(gif)
            }

            StaticStore.gifFrame = 0
            frame = 0
            encoder = AnimatedGifEncoder()
            bos = ByteArrayOutputStream()

            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

            runOnUiThread {
                if (StaticStore.keepDoing && result != MediaScanner.ERRR_WRONG_SDK)
                    StaticStore.showShortMessage(this@ImageViewer, getText(R.string.anim_png_success).toString().replace("-", result))
                else if (!StaticStore.keepDoing)
                    StaticStore.showShortMessage(this@ImageViewer, R.string.anim_gif_cancel)
                else
                    StaticStore.showShortMessage(this@ImageViewer, R.string.anim_png_fail)
            }

            StaticStore.gifisSaving = false
            StaticStore.enableGIF = false
        }

        private fun getTotalFrame(frames: ArrayList<Array<Int>>, enables: Array<Boolean>) : Int {
            return frames.filterIndexed { i, _ -> enables[i] }.sumOf { f -> f[1] - f[0] + 1 }
        }

        private fun getEanimD(type: AnimationCView.AnimationType, index: Int, data: Any, form: Int = 0) : EAnimD<*> {
            when(type) {
                AnimationCView.AnimationType.UNIT -> {
                    val u = Identifier.get(data as Identifier<*>)

                    return (u as Unit).forms[form].getEAnim(StaticStore.getAnimType(index, u.forms[form].anim.anims.size))
                }
                AnimationCView.AnimationType.ENEMY -> {
                    val e = Identifier.get(data as Identifier<*>)

                    return (e as Enemy).getEAnim(StaticStore.getAnimType(index, e.anim.anims.size))
                }
                AnimationCView.AnimationType.EFFECT -> {
                    val d = data as EffAnim<*>

                    return StaticJava.generateEAnimD(d, index)
                }
                AnimationCView.AnimationType.SOUL -> {
                    val d = data as Soul

                    return StaticJava.generateEAnimD(d, index)
                }
                AnimationCView.AnimationType.CANNON -> {
                    val d = data as NyCastle

                    return StaticJava.generateEAnimD(d, index)
                }
                AnimationCView.AnimationType.DEMON_SOUL -> {
                    val d = data as DemonSoul

                    return StaticJava.generateEAnimD(d, index)
                }
                else -> {
                    throw IllegalStateException("Invalid type $type")
                }
            }
        }

        fun checkValidClasses(data: Any, type: AnimationCView.AnimationType) {
            when(data) {
                is Soul -> {
                    if(type != AnimationCView.AnimationType.SOUL)
                        throw IllegalStateException("Invalid data ${data::class.java.name} with type $type")
                }
                is EffAnim<*> -> {
                    if(type != AnimationCView.AnimationType.EFFECT)
                        throw IllegalStateException("Invalid data ${data::class.java.name} with type $type")
                }
                is NyCastle -> {
                    if(type != AnimationCView.AnimationType.CANNON)
                        throw IllegalStateException("Invalid data ${data::class.java.name} with type $type")
                }
                is DemonSoul -> {
                    if(type != AnimationCView.AnimationType.DEMON_SOUL)
                        throw IllegalStateException("Invalid data ${data::class.java.name} with type $type")
                }
                is Identifier<*> -> {
                    val entity = data.get() ?: throw NullPointerException("Data must not be null")

                    if (type == AnimationCView.AnimationType.UNIT && entity !is Unit) {
                        throw IllegalStateException("Invalid data ${data::class.java.name} with type $type")
                    } else if (type == AnimationCView.AnimationType.ENEMY && entity !is Enemy) {
                        throw IllegalStateException("Invalid data ${data::class.java.name} with type $type")
                    }
                }
                else -> {
                    throw IllegalStateException("Invalid data ${data::class.java.name} with type $type")
                }
            }
        }

        private fun generateName(type: AnimationCView.AnimationType, pack: String, id: String, form: String) : String {
            val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
            val date = Date()

            when(type) {
                AnimationCView.AnimationType.UNIT -> {
                    return dateFormat.format(date) + "-U-" + pack + "-" + id + "-" + form
                }
                AnimationCView.AnimationType.ENEMY -> {
                    return dateFormat.format(date) + "-E-" + id
                }
                AnimationCView.AnimationType.EFFECT -> {
                    return dateFormat.format(date) + "-EFF-" + id
                }
                AnimationCView.AnimationType.SOUL -> {
                    return dateFormat.format(date) + "-S-" + id
                }
                AnimationCView.AnimationType.CANNON -> {
                    return dateFormat.format(date) + "-C-" + id
                }
                AnimationCView.AnimationType.DEMON_SOUL -> {
                    return dateFormat.format(date) + "-DS-" + id
                }
                else -> {
                    return dateFormat.format(date)
                }
            }
        }
    }
}