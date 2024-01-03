package com.mandarin.bcu

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.Locale

class ApkDownload : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        if (!shared.contains("initial")) {
            val ed = shared.edit()

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

        setContentView(R.layout.activity_apk_download)

        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 786)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(this@ApkDownload, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val result = intent

            if (result.getStringExtra("ver") != null) {
                val ver = (result.getStringExtra("ver") ?: StaticStore.VER).replace(Regex("b_.+?\$"), "")

                lifecycleScope.launch {
                    val retry = findViewById<Button>(R.id.apkretry)
                    val progress = findViewById<ProgressBar>(R.id.apkprog)
                    val state = findViewById<TextView>(R.id.apkstate)

                    retry.visibility = View.GONE

                    progress.isIndeterminate = true
                    progress.max = 100

                    state.setText(R.string.down_state_rea)

                    retry.setOnClickListener(object : SingleClick() {
                        override fun onSingleClick(v: View?) {
                            StaticStore.setDisappear(retry)

                            lifecycleScope.launch {
                                downloadAndInstall(ver)
                            }
                        }
                    })

                    downloadAndInstall(ver)
                }
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

    private suspend fun downloadAndInstall(version: String) {
        val output = downloadApk(version)

        val retry = findViewById<Button>(R.id.apkretry)
        val progress = findViewById<ProgressBar>(R.id.apkprog)
        val state = findViewById<TextView>(R.id.apkstate)

        if (output.name.isBlank()) {
            state.setText(R.string.down_state_no)

            progress.isIndeterminate = false
            progress.max = 1
            progress.progress = 0

            StaticStore.setAppear(retry)
        } else {
            val apkuri = FileProvider.getUriForFile(this@ApkDownload, "com.mandarin.bcu.provider", output)

            val intent = Intent(Intent.ACTION_VIEW).setDataAndType(apkuri, "application/vnd.android.package-archive")

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            startActivity(intent)
            finish()
        }
    }

    private suspend fun downloadApk(version: String) : File {
        var internetConnected = true

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetwork

        if (networkInfo == null) {
            internetConnected = false
        } else {
            val capability = connectivityManager.getNetworkCapabilities(networkInfo)

            if (capability == null) {
                internetConnected = false
            } else if (!capability.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && !capability.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) && !capability.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                internetConnected = false
            }
        }

        if (!internetConnected) {
            return File("")
        }

        val progress = findViewById<ProgressBar>(R.id.apkprog)
        val state = findViewById<TextView>(R.id.apkstate)

        state.text = getString(R.string.down_state_doing, "BCU_Android_$version.apk")

        progress.isIndeterminate = false
        progress.max = 100

        return withContext(Dispatchers.IO) {
            val path = StaticStore.getExternalPath(this@ApkDownload)+"apk/"

            val filePrefix = "BCU_Android_"

            val apk = ".apk"

            val fullPath = path + filePrefix + version + apk

            val domain = "https://github.com/battlecatsultimate/bcu-assets/blob/master/apk/BCU_Android_"
            val end = "?raw=true"

            val link = domain + version + apk + end

            try {
                val url = URL(link)

                val c = url.openConnection() as HttpURLConnection

                c.requestMethod = "GET"
                c.connect()

                val size = c.contentLength.toLong()

                val output = File(fullPath)

                val paths = File(path)

                if (!paths.exists()) {
                    paths.mkdirs()
                }
                if (!output.exists()) {
                    output.createNewFile()
                }

                val fos = FileOutputStream(output)

                val inputStream = c.inputStream
                val buffer = ByteArray(1024)

                var len: Int
                var total = 0.toLong()

                while (inputStream.read(buffer).also { len = it } != -1) {
                    total += len.toLong()

                    progress.progress = (total * 100 / size).toInt()

                    fos.write(buffer, 0, len)
                }

                c.disconnect()

                fos.close()
                inputStream.close()

                return@withContext output
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return@withContext File("")
        }
    }
}