package com.mandarin.bcu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.DataResetHandler
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.supports.adapter.DataResetAdapter
import common.CommonStatic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class DataResetManager : AppCompatActivity() {
    var performed = false
    var resetting = false

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

        LeakCanaryManager.initCanary(shared, application)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_data_reset_manager)

        val list = findViewById<ListView>(R.id.dataresetlist)
        val reset = findViewById<MaterialButton>(R.id.dataresetbutton)
        val bck = findViewById<FloatingActionButton>(R.id.dataresetbck)

        val handlers = ArrayList<DataResetHandler>()

        val assets = File(StaticStore.getExternalAsset(this)+"assets/")

        if(assets.exists()) {
            val assetList = assets.listFiles()

            if(assetList != null) {
                for(asset in assetList) {
                    if(asset.name.endsWith("assets.bcuzips")) {
                        var text = getString(R.string.datareset_asset).replace("_AAA_", asset.name)
                        val loading = getString(R.string.datareset_assetreset).replace("_", asset.name)

                        val names = asset.name.split("xxxx")

                        if(names.size >= 2) {
                            try {
                                val version = names[0].toInt()

                                val extension = if(version == 0) {
                                    getString(R.string.datareset_prebc)
                                } else {
                                    getString(R.string.datareset_assetbc).replace("_VVV_", version.toString())
                                }

                                text += extension
                            } catch (ignored: Exception) {}
                        }

                        handlers.add(DataResetHandler(text, loading, DataResetHandler.TYPE.ASSET, asset.name))
                    }
                }
            }
        }

        handlers.add(DataResetHandler(getString(R.string.datareset_lang), getString(R.string.datareset_langreset), DataResetHandler.TYPE.LANG))
        handlers.add(DataResetHandler(getString(R.string.datareset_music), getString(R.string.datareset_musicreset), DataResetHandler.TYPE.MUSIC))
        handlers.add(DataResetHandler(getString(R.string.datareset_lineup), getString(R.string.datareset_lineupreset), DataResetHandler.TYPE.LINEUP))
        handlers.add(DataResetHandler(getString(R.string.datareset_log), getString(R.string.datareset_logreset), DataResetHandler.TYPE.LOG))
        handlers.add(DataResetHandler(getString(R.string.datareset_pack), getString(R.string.datareset_packreset), DataResetHandler.TYPE.PACK))

        val adapter = DataResetAdapter(this, handlers)

        list.adapter = adapter

        reset.isEnabled = false

        reset.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                StaticStore.fixOrientation(this@DataResetManager)

                val builder = AlertDialog.Builder(this@DataResetManager)
                val inflater = LayoutInflater.from(this@DataResetManager)
                val view = inflater.inflate(R.layout.data_reset_dialog, null)

                builder.setView(view)

                val dialogReset = view.findViewById<Button>(R.id.resetdialogreset)
                val cancel = view.findViewById<Button>(R.id.resetdialogcancel)

                val dialog = builder.create()

                dialog.setCancelable(false)

                if (!isDestroyed && !isFinishing) {
                    dialog.show()
                }

                dialogReset.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        performed = true

                        dialog.dismiss()

                        val progressView = inflater.inflate(R.layout.loading_dialog, null)

                        builder.setView(progressView)

                        val progress = progressView.findViewById<TextView>(R.id.loadprogress)
                        val loading = progressView.findViewById<TextView>(R.id.loadtitle)

                        progress.visibility = View.GONE

                        val progressDialog = builder.create()

                        progressDialog.show()
                        progressDialog.setCancelable(false)

                        val run = Runnable {
                            var result = true

                            for(handler in handlers) {
                                if(handler.doPerform) {
                                    runOnUiThread {
                                        loading.text = handler.loading
                                    }

                                    result = result and handler.performReset(this@DataResetManager)
                                }
                            }

                            runOnUiThread {
                                progressDialog.dismiss()

                                if(result) {
                                    StaticStore.showShortMessage(this@DataResetManager, R.string.datareset_restart)
                                } else {
                                    StaticStore.showShortMessage(this@DataResetManager, R.string.datareset_fail)
                                }

                                StaticStore.unfixOrientation(this@DataResetManager)
                            }
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            run.run()
                        }
                    }
                })

                cancel.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        StaticStore.unfixOrientation(this@DataResetManager)
                        dialog.dismiss()
                    }
                })
            }
        })

        bck.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                if(resetting)
                    return

                if(performed) {
                    finish()
                } else {
                    val intent = Intent(this@DataResetManager, ConfigScreen::class.java)

                    startActivity(intent)
                    finish()
                }
            }
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                bck.performClick()
            }
        })
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
}