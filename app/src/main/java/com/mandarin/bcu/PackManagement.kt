package com.mandarin.bcu

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ListView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.Revalidater
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.pack.PackConflict
import com.mandarin.bcu.androidutil.pack.adapters.PackManagementAdapter
import com.mandarin.bcu.androidutil.pack.coroutine.PackManager
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.pack.PackData
import common.pack.UserProfile
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Runnable
import java.text.DecimalFormat
import java.util.*

class PackManagement : AppCompatActivity() {
    companion object {
        var handlingPacks = false
        var needReload = false
    }

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            val path = result.data?.data ?: return@registerForActivityResult

            val intent = result.data ?: return@registerForActivityResult

            println(intent.action)
            println(intent.scheme)
            println(intent.categories.joinToString(", "))
            println(intent.data)

            Log.i("PackManagement", "Got URI : $path")

            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

            val resolver = applicationContext.contentResolver
            val cursor = resolver.query(path, projection, null, null, null)

            if(cursor != null) {
                if(cursor.moveToFirst()) {
                    val name = cursor.getString(0)

                    if(!name.endsWith(".pack.bcuzip")) {
                        StaticStore.showShortMessage(this, R.string.pack_import_invalid)

                        return@registerForActivityResult
                    }

                    val pack = File(StaticStore.getExternalPack(this), name)

                    if(!pack.exists()) {
                        pack.createNewFile()

                        val ins = resolver.openInputStream(path) ?: return@registerForActivityResult
                        val fos = FileOutputStream(pack)

                        showWritingDialog(ins, fos, pack)

                    } else {
                        StaticStore.fixOrientation(this)

                        val dialog = AlertDialog.Builder(this)

                        dialog.setTitle(R.string.pack_import_exist)
                        dialog.setMessage(R.string.pack_import_exist_msg)

                        dialog.setPositiveButton(R.string.replace) { _, _ ->
                            val ins = resolver.openInputStream(path) ?: return@setPositiveButton
                            val fos = FileOutputStream(pack)

                            showWritingDialog(ins, fos, pack)
                        }

                        dialog.setNegativeButton(R.string.main_file_cancel) {_, _ ->
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                        }

                        dialog.show()
                    }


                }

                cursor.close()
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: SharedPreferences.Editor

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

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

        LeakCanaryManager.initCanary(shared)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_pack_management)

        PackManager(this).execute()

        val swipe = findViewById<SwipeRefreshLayout>(R.id.pmanrefresh)
        val list = findViewById<ListView>(R.id.pmanlist)

        swipe.setColorSchemeColors(StaticStore.getAttributeColor(this, R.attr.colorAccent))

        swipe.setOnRefreshListener {
            handlingPacks = true
            StaticStore.fixOrientation(this)

            reloadPack(swipe, list)
        }

        val bck = findViewById<FloatingActionButton>(R.id.pmanbck)

        bck.setOnClickListener {
            if(!handlingPacks && !needReload) {
                val intent = Intent(this, MainActivity::class.java)

                startActivity(intent)

                finish()

                return@setOnClickListener
            }

            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.loading_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) ?: return@setOnClickListener
            val v = dialog.window?.decorView ?: return@setOnClickListener

            val title = v.findViewById<TextView>(R.id.loadtitle)
            val progress = v.findViewById<TextView>(R.id.loadprogress)

            progress.visibility = View.GONE

            title.text = getString(R.string.pack_reload)

            dialog.setCancelable(false)

            dialog.show()

            CoroutineScope(Dispatchers.IO).launch {
                StaticStore.resetUserPacks()

                Definer.define(this@PackManagement, {prog -> println(prog)}, this@PackManagement::updateText)

                val l = Locale.getDefault().language
                Revalidater.validate(l, this@PackManagement)

                dialog.dismiss()

                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                handlingPacks = false

                withContext(Dispatchers.Main) {
                    if(PackConflict.conflicts.isNotEmpty()) {
                        val intent = Intent(this@PackManagement, PackConflictSolve::class.java)

                        startActivity(intent)

                        finish()
                    } else {
                        val intent = Intent(this@PackManagement, MainActivity::class.java)

                        startActivity(intent)

                        finish()
                    }
                }

                needReload = false
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                bck.performClick()
            }
        })
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }

    public override fun onDestroy() {
        super.onDestroy()

        StaticStore.toast = null
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

    private fun showWritingDialog(ins: InputStream, fos: FileOutputStream, pack: File) {
        needReload = true

        val swipe = findViewById<SwipeRefreshLayout>(R.id.pmanrefresh)
        val list = findViewById<ListView>(R.id.pmanlist)

        StaticStore.fixOrientation(this)
        handlingPacks = true

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.loading_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                ?: return
        val v = dialog.window?.decorView ?: return

        val title = v.findViewById<TextView>(R.id.loadtitle)
        val progress = v.findViewById<TextView>(R.id.loadprogress)

        title.text = getString(R.string.pack_import_importing).replace("_", pack.name)

        dialog.setCancelable(false)

        dialog.show()

        CoroutineScope(Dispatchers.Main).launch {
            val total = ins.available().toLong()
            var prog = 0L
            val df = DecimalFormat("#.##")

            val b = ByteArray(65536)
            var len: Int

            while(ins.read(b).also { len = it } != -1) {
                fos.write(b, 0, len)

                prog += len

                val msg = if(total >= 50000000) {
                    "${byteToMB(prog, df)} MB / ${byteToMB(total, df)} MB (${(prog*100.0/total).toInt()}%)"
                } else {
                    "${byteToKB(prog, df)} KB / ${byteToKB(total, df)} KB (${(prog*100.0/total).toInt()}%)"
                }

                runOnUiThread {
                    progress.text = msg
                }
            }

            ins.close()
            fos.close()

            dialog.dismiss()

            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

            handlingPacks = false

            runOnUiThread {
                title.setText(R.string.pack_reload)
                progress.visibility = View.GONE
            }

            StaticStore.resetUserPacks()

            Definer.define(this@PackManagement, {p -> println(p)}, this@PackManagement::updateText)

            val l = Locale.getDefault().language
            Revalidater.validate(l, this@PackManagement)

            dialog.dismiss()

            val packList = ArrayList<PackData.UserPack>()

            for(p in UserProfile.getUserPacks()) {
                packList.add(p)
            }

            runOnUiThread {
                list.adapter = PackManagementAdapter(this@PackManagement, packList)
            }

            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            handlingPacks = false

            runOnUiThread {
                swipe?.isRefreshing = false
            }

            if(PackConflict.conflicts.isNotEmpty()) {
                StaticStore.showShortSnack(findViewById(R.id.pmanlayout), R.string.pack_manage_warn)
            }
        }
    }

    private fun reloadPack(swipe: SwipeRefreshLayout?, list: ListView) {
        needReload = true

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.loading_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) ?: return
        val v = dialog.window?.decorView ?: return

        val title = v.findViewById<TextView>(R.id.loadtitle)
        val progress = v.findViewById<TextView>(R.id.loadprogress)

        progress.visibility = View.GONE

        title.text = getString(R.string.pack_reload)

        dialog.setCancelable(false)

        dialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            StaticStore.resetUserPacks()

            Definer.define(this@PackManagement, {prog -> println(prog)}, this@PackManagement::updateText)

            val l = Locale.getDefault().language
            Revalidater.validate(l, this@PackManagement)

            dialog.dismiss()

            val packList = ArrayList<PackData.UserPack>()

            for(pack in UserProfile.getUserPacks()) {
                packList.add(pack)
            }

            runOnUiThread {
                list.adapter = PackManagementAdapter(this@PackManagement, packList)
            }

            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            handlingPacks = false

            runOnUiThread {
                swipe?.isRefreshing = false
            }

            if(PackConflict.conflicts.isNotEmpty()) {
                StaticStore.showShortSnack(findViewById(R.id.pmanlayout), R.string.pack_manage_warn)
            }
        }
    }

    private fun byteToKB(bytes: Long, df: DecimalFormat) : String {
        return df.format(bytes.toDouble()/1024.0)
    }

    private fun byteToMB(bytes: Long, df: DecimalFormat) : String {
        return df.format(bytes.toDouble()/(1024.0 * 1024))
    }

    private fun updateText(info: String) {
        val st = findViewById<TextView>(R.id.status)

        st.text = StaticStore.getLoadingText(this, info)
    }
}