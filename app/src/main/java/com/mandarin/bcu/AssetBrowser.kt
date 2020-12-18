package com.mandarin.bcu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.widget.AdapterView
import android.widget.ListView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.AssetListAdapter
import com.mandarin.bcu.androidutil.supports.AutoMarquee
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.system.files.VFile
import java.io.File
import java.io.FileOutputStream
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class AssetBrowser : AppCompatActivity() {
    companion object {
        var path = "./org"

        const val EXTRACT_FILE = 0

        var current: VFile? = null
    }

    val list = ArrayList<VFile>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: SharedPreferences.Editor

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

        setContentView(R.layout.activity_asset_browser)

        val fileList = findViewById<ListView>(R.id.assetlist)
        val filepath = findViewById<AutoMarquee>(R.id.assetpath)
        val bck = findViewById<FloatingActionButton>(R.id.assetbck)

        fileList.isFastScrollEnabled = true

        generateFileList()

        filepath.text = path

        val adapter = AssetListAdapter(this, list)

        fileList.adapter = adapter

        fileList.onItemClickListener = AdapterView.OnItemClickListener { _, _, p, _ ->
            if(isFile(list[p])) {
                val intent = Intent(this@AssetBrowser, FileViewer::class.java)

                intent.putExtra("path", list[p].path)

                startActivity(intent)

                return@OnItemClickListener
            }

            path = if(path == list[p].path) {
                list[p].parent.path
            } else{
                list[p].path
            }

            generateFileList()

            adapter.notifyDataSetChanged()

            filepath.text = path

            fileList.setSelection(0)
        }

        bck.setOnClickListener {
            path = "./org"
            finish()
        }
    }

    private fun generateFileList() {
        list.clear()

        val file = VFile.get(path)

        if(path != "./org")
            list.add(file)

        for(f in file.list()) {
            list.add(f)
        }

        sort()
    }

    private fun sort() {
        val values = list.toTypedArray()

        list.clear()

        val folder = ArrayList<VFile>()
        val file = ArrayList<VFile>()

        for(f in values) {
            if(isFile(f)) {
                file.add(f)
            } else {
                folder.add(f)
            }
        }

        list.addAll(folder)
        list.addAll(file)
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

    override fun onBackPressed() {
        if(path != "./org") {
            val fileList = findViewById<ListView>(R.id.assetlist)

            fileList.performItemClick(fileList.getChildAt(0), 0, fileList.adapter.getItemId(0))
        } else {
            val bck = findViewById<FloatingActionButton>(R.id.assetbck)

            bck.performClick()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == RESULT_OK && requestCode == EXTRACT_FILE) {
            if(data != null) {
                val file = current
                val uri = data.data

                if(uri == null || file == null) {
                    StaticStore.showShortMessage(this, getString(R.string.file_extract_cant))
                } else {
                    val pfd = contentResolver.openFileDescriptor(uri, "w")

                    if(pfd != null) {
                        val fos = FileOutputStream(pfd.fileDescriptor)
                        val ins = file.data.stream

                        val b = ByteArray(65536)
                        var len: Int

                        while(ins.read(b).also { len = it } != -1) {
                            fos.write(b, 0, len)
                        }

                        ins.close()
                        fos.close()

                        val path = uri.path

                        if(path == null) {
                            StaticStore.showShortMessage(this, getString(R.string.file_extract_semi).replace("_",file.name))
                            return
                        }

                        val f = File(path)

                        val p = f.absolutePath.split(":")[1]

                        StaticStore.showShortMessage(this, getString(R.string.file_extract_success).replace("_",file.name).replace("-",p))
                    } else {
                        StaticStore.showShortMessage(this, getString(R.string.file_extract_cant))
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun isFile(f: VFile) : Boolean {
        return f.list() == null
    }
}