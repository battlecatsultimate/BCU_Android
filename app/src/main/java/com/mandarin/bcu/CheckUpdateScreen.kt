package com.mandarin.bcu

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.AssetDownloadService
import com.mandarin.bcu.androidutil.io.AssetException
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.pack.PackConflict
import com.mandarin.bcu.androidutil.supports.AnimatorConst
import com.mandarin.bcu.androidutil.supports.CustomAnimator
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.io.assets.AssetLoader
import common.io.assets.UpdateCheck
import common.pack.UserProfile
import common.system.fake.ImageBuilder
import common.util.Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException
import java.util.Locale
import javax.net.ssl.SSLHandshakeException

open class CheckUpdateScreen : AppCompatActivity() {
    private val langFolder = arrayOf("en/", "jp/", "kr/", "zh/", "fr/", "it/", "es/", "de/", "th/")

    private val broadcastReceiver = ServiceBroadCastReceiver()
    private val serviceConnector = AssetDownloaderConnector()

    private lateinit var communicator: Messenger

    private var config = false
    private var bound = false

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed = shared.edit()

        Definer.initializeConfiguration(shared, this)

        if (!shared.getBoolean("theme", false)) {
            setTheme(R.style.AppTheme_night)
        } else {
            setTheme(R.style.AppTheme_day)
        }

        LeakCanaryManager.initCanary(shared, application)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        Thread.setDefaultUncaughtExceptionHandler(ErrorLogWriter(StaticStore.getExternalLog(this), shared.getBoolean("upload", false) || shared.getBoolean("ask_upload", true)))

        setContentView(R.layout.activity_check_update_screen)

        lifecycleScope.launch {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 786)

            if (MainActivity.isRunning)
                finish()

            val prog = findViewById<ProgressBar>(R.id.prog)
            val retry = findViewById<Button>(R.id.retry)
            val state = findViewById<TextView>(R.id.status)

            retry.visibility = View.GONE
            prog.visibility = View.VISIBLE

            prog.isIndeterminate = true

            state.setText(R.string.main_check_apk)

            var close = false

            withContext(Dispatchers.IO) {
                deleter(File(StaticStore.getExternalPath(this@CheckUpdateScreen) + "apk/"))

                val result = intent

                if (result.extras != null) {
                    val extra = result.extras

                    config = extra!!.getBoolean("Config")
                }

                ImageBuilder.builder = BMBuilder()

                StaticStore.checkFolders(
                    StaticStore.getExternalPath(this@CheckUpdateScreen),
                    StaticStore.getExternalLog(this@CheckUpdateScreen),
                    StaticStore.getExternalPack(this@CheckUpdateScreen)
                )
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q && checkOldFiles() && !shared.getBoolean("Announce_0.13.0", false)) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

                suspendCancellableCoroutine {
                    val builder = AlertDialog.Builder(this@CheckUpdateScreen)
                    val inflater = LayoutInflater.from(this@CheckUpdateScreen)

                    val v = inflater.inflate(R.layout.announce_dialog, null)

                    builder.setView(v)

                    val confirm = v.findViewById<Button>(R.id.anncomfirm)

                    val dialog = builder.create()

                    dialog.setCancelable(false)

                    v.post {
                        val w = (resources.displayMetrics.widthPixels * 0.75).toInt()

                        dialog.window?.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT)
                    }

                    dialog.setOnDismissListener {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    }

                    confirm.setOnClickListener(object : SingleClick() {
                        override fun onSingleClick(v: View?) {
                            ed.putBoolean("Announce_0.13.0", true)
                            ed.apply()

                            dialog.dismiss()

                            it.resume(0) { _ -> }
                        }
                    })

                    dialog.show()
                }
            }

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

            if (internetConnected) {
                try {
                    withContext(Dispatchers.IO) {
                        val updateJson = UpdateCheck.checkUpdate()

                        val langShared = getSharedPreferences(StaticStore.LANG, Context.MODE_PRIVATE)
                        val musicShared = getSharedPreferences(StaticStore.MUSIC, Context.MODE_PRIVATE)

                        //Check APK
                        val apk : UpdateCheck.UpdateJson.ApkJson? = if(updateJson.apk_update != null)
                            checkApk(updateJson.apk_update)
                        else
                            null

                        if(apk != null) {
                            withContext(Dispatchers.Main) {
                                suspendCancellableCoroutine {
                                    val apkDialog = android.app.AlertDialog.Builder(this@CheckUpdateScreen)

                                    apkDialog.setCancelable(false)

                                    apkDialog.setTitle(R.string.apk_down_title)

                                    val content = getString(R.string.apk_down_content) + apk.ver

                                    apkDialog.setMessage(content)
                                    apkDialog.setPositiveButton(R.string.main_file_ok) { _, _ ->
                                        val intent = Intent(this@CheckUpdateScreen, ApkDownload::class.java)

                                        intent.putExtra("ver", apk.ver)

                                        close = true

                                        startActivity(intent)
                                        finish()

                                        it.resume(0) { _ -> }
                                    }

                                    apkDialog.setNegativeButton(R.string.main_file_cancel) { _, _ ->
                                        it.resume(0) { _ -> }
                                    }

                                    apkDialog.show()
                                }
                            }
                        }

                        if (close)
                            return@withContext

                        withContext(Dispatchers.Main) {
                            state.setText(R.string.main_check_up)
                        }

                        //Check Assets
                        val langFiles = ArrayList<String>()

                        langFiles.add("Difficulty.txt")

                        CommonStatic.getConfig().localLangMap["Difficulty.txt"] = langShared.getString("Difficulty.txt", "")

                        for(lang in langFolder) {
                            for(l in StaticStore.langfile) {
                                langFiles.add("$lang$l")

                                CommonStatic.getConfig().localLangMap["$lang$l"] = langShared.getString("$lang$l", "")
                            }
                        }

                        //Read Music File
                        val musicCount = updateJson?.music ?: 0

                        for(i in 0 until musicCount) {
                            CommonStatic.getConfig().localMusicMap[i] = musicShared.getString(Data.trio(i), "")
                        }

                        val assetList = try {
                            UpdateCheck.checkAsset(updateJson, "android")
                        } catch (_: Exception) {
                            ArrayList()
                        }

                        val langList = try {
                            UpdateCheck.checkLang(langFiles.toTypedArray()).get()
                        } catch (_: Exception) {
                            ArrayList()
                        }

                        val musicList = try {
                            UpdateCheck.checkMusic(musicCount).get()
                        } catch (_: Exception) {
                            ArrayList()
                        }

                        if(assetList.isNotEmpty() || musicList.isNotEmpty() || langList.isNotEmpty()) {
                            val file = File(StaticStore.getExternalAsset(this@CheckUpdateScreen)+"assets/")

                            val msg = if(!file.exists())
                                getString(R.string.main_file_need)
                            else if(assetList.isNotEmpty() && musicList.isEmpty() && langList.isEmpty())
                                getString(R.string.main_file_asset)
                            else if(assetList.isEmpty() && musicList.isNotEmpty() && langList.isEmpty())
                                getString(R.string.main_file_music)
                            else if(assetList.isEmpty() && musicList.isEmpty() && langList.isNotEmpty())
                                getString(R.string.main_file_text)
                            else
                                getString(R.string.main_file_x)

                            val canContinue = !(assetList.isNotEmpty() || musicList.isNotEmpty() || langNeed)

                            retry.setOnClickListener(object: SingleClick() {
                                override fun onSingleClick(v: View?) {
                                    lifecycleScope.launch {
                                        StaticStore.setDisappear(retry)

                                        val serviceIntent = Intent(this@CheckUpdateScreen, AssetDownloadService::class.java)

                                        startService(serviceIntent)

                                        bound = bindService(serviceIntent, serviceConnector, 0)
                                    }
                                }
                            })

                            withContext(Dispatchers.Main) {
                                suspendCancellableCoroutine {
                                    val updateDialog = AlertDialog.Builder(this@CheckUpdateScreen)

                                    updateDialog.setTitle(msg)
                                    updateDialog.setMessage(R.string.main_file_up)

                                    updateDialog.setPositiveButton(R.string.main_file_ok) { _, _ ->
                                        val serviceIntent = Intent(this@CheckUpdateScreen, AssetDownloadService::class.java)

                                        broadcastReceiver.attachListener { intent ->
                                            if (intent.action == AssetDownloadService.SUCCESS) {
                                                if (!it.isActive || it.isCancelled)
                                                    return@attachListener

                                                it.resume(0) { _ -> }
                                            }
                                        }

                                        startService(serviceIntent)

                                        bound = bindService(serviceIntent, serviceConnector, 0)
                                    }

                                    updateDialog.setNegativeButton(R.string.main_file_cancel) { _, _ ->
                                        if(!canContinue) {
                                            close = true

                                            finish()
                                        }

                                        it.resume(0) { _ -> }
                                    }

                                    updateDialog.show()
                                }
                            }
                        }
                    }
                } catch (e: SocketException) {
                    suspendCancellableCoroutine<Int> {
                        val internetDialog = android.app.AlertDialog.Builder(this@CheckUpdateScreen)

                        internetDialog.setCancelable(false)

                        internetDialog.setTitle(R.string.main_timeout_dialog_title)

                        internetDialog.setMessage(R.string.main_timeout_dialog_content)

                        internetDialog.setPositiveButton(R.string.main_file_ok) { _, _ ->
                            close = true

                            startActivity(intent)
                            finish()

                            it.resume(0) { _ -> }
                        }

                        internetDialog.show()
                    }
                } catch (e: ConnectException) {
                    suspendCancellableCoroutine {
                        val internetDialog = android.app.AlertDialog.Builder(this@CheckUpdateScreen)

                        internetDialog.setCancelable(false)

                        internetDialog.setTitle(R.string.main_timeout_dialog_title)

                        internetDialog.setMessage(R.string.main_timeout_dialog_content)

                        internetDialog.setPositiveButton(R.string.main_file_ok) { _, _ ->
                            close = true

                            startActivity(intent)
                            finish()

                            it.resume(0) { _ -> }
                        }

                        internetDialog.show()
                    }
                } catch (e: UnknownHostException) {
                    suspendCancellableCoroutine {
                        val internetDialog = android.app.AlertDialog.Builder(this@CheckUpdateScreen)

                        internetDialog.setCancelable(false)

                        internetDialog.setTitle(R.string.main_timeout_dialog_title)

                        internetDialog.setMessage(R.string.main_timeout_dialog_content)

                        internetDialog.setPositiveButton(R.string.main_file_ok) { _, _ ->
                            close = true

                            startActivity(intent)
                            finish()

                            it.resume(0) { _ -> }
                        }

                        internetDialog.show()
                    }
                } catch (e: SSLHandshakeException) {
                    suspendCancellableCoroutine {
                        val internetDialog = android.app.AlertDialog.Builder(this@CheckUpdateScreen)

                        internetDialog.setCancelable(false)

                        internetDialog.setTitle(R.string.main_terminated_dialog_title)

                        internetDialog.setMessage(R.string.main_terminated_dialog_content)

                        internetDialog.setPositiveButton(R.string.main_file_ok) { _, _ ->
                            close = true

                            startActivity(intent)
                            finish()

                            it.resume(0) { _ -> }
                        }

                        internetDialog.show()
                    }
                }

                if (close) {
                    return@launch
                }
            } else if (!hasAllAsset || langNeed) {
                prog.isIndeterminate = false
                prog.max = 1
                prog.progress = 0

                state.setText(R.string.main_internet_no)

                suspendCancellableCoroutine {
                    val internetDialog = android.app.AlertDialog.Builder(this@CheckUpdateScreen)

                    internetDialog.setCancelable(false)

                    internetDialog.setTitle(R.string.main_internet_dialog_title)

                    internetDialog.setMessage(R.string.main_internet_dialog_content)

                    internetDialog.setPositiveButton(R.string.main_file_ok) { _, _ ->
                        close = true

                        startActivity(intent)
                        finish()

                        it.resume(0) { _ -> }
                    }

                    internetDialog.show()
                }
            }

            if (close)
                return@launch

            if (bound) {
                unbindService(serviceConnector)
                bound = false

                stopService(Intent(this@CheckUpdateScreen, AssetDownloadService::class.java))
            }

            loadFile()
        }
    }

    private fun deleter(f: File) {
        if (f.isDirectory) {
            val lit = f.listFiles() ?: return

            for (g in lit)
                deleter(g)

            f.delete()
        } else
            f.delete()
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

        val filter = IntentFilter().apply {
            addAction(AssetDownloadService.SUCCESS)
            addAction(AssetDownloadService.PROGRESS_PROGRESSION)
            addAction(AssetDownloadService.PROGRESS_TEXT)
            addAction(AssetDownloadService.FAILED)
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter)

        val serviceIntent = Intent(this@CheckUpdateScreen, AssetDownloadService::class.java)

        bound = bindService(serviceIntent, serviceConnector, 0)

        if (bound && this::communicator.isInitialized) {
            communicator.send(Message().apply {
                what = AssetDownloadService.WAKE_UP
            })
        }

        super.onResume()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)

        if (bound) {
            unbindService(serviceConnector)
            bound = false
        }

        super.onPause()
    }

    private fun checkOldFiles() : Boolean {
        val names:List<String> = listOf("apk","lang","music")

        var result = false

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val datapath = StaticStore.getExternalPath(this).replace("files/","")

            val f = File(datapath)

            val lit = f.listFiles() ?: return false

            for(fs in lit) {
                if(fs.name != "files" && names.contains(fs.name)) {
                    result = true

                    deleter(fs)
                }
            }
        }

        return result
    }

    private fun check(thisNum: List<String>, thatNum: List<String>) : Boolean {
        var update = false

        val these = Array(thisNum.size) {
            thisNum[it].toInt()
        }

        val those = Array(thatNum.size) {
            thatNum[it].toInt()
        }

        if(these[0] < those[0])
            update = true
        else if(these[0] == those[0] && these[1] < those[1])
            update = true
        else if(these[0] == those[0] && these[1] == those[1] && these[2] < those[2])
            update = true

        return update
    }

    private val langNeed: Boolean
        get() {
            var f = File(StaticStore.getExternalAsset(this)+"lang/Difficulty.txt")

            if(!f.exists())
                return true

            for(lang in langFolder) {
                for(l in StaticStore.langfile) {
                    f = File(StaticStore.getExternalAsset(this)+"lang/$lang$l")

                    if(!f.exists())
                        return true
                }
            }

            return false
        }

    private val hasAllAsset : Boolean
        get() {
            val assets = AssetLoader.previewAssets()
            val require = UserProfile.getPool("required_asset", String::class.java)

            if(assets == null)
                return false

            for(asset in require) {
                if(!assets.contains("asset_$asset")) {
                    return false
                }
            }

            return true
        }

    private fun checkApk(apks: Array<UpdateCheck.UpdateJson.ApkJson>) : UpdateCheck.UpdateJson.ApkJson? {
        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        val ver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)).versionName.replace(Regex("b_.+?\$"), "")
        } else {
            packageManager.getPackageInfo(packageName, 0).versionName.replace(Regex("b_.+?\$"), "")
        }
        val allowTest = shared.getBoolean("apktest", false)

        for(apk in apks.reversedArray()) {
            if(!apk.isTest || allowTest) {
                if(check(ver.split("."), apk.ver.split(".")))
                    return apk
            }
        }

        return null
    }

    private fun progressSmoothly(p: Double) {
        val progression = findViewById<ProgressBar>(R.id.prog)

        CustomAnimator(500, AnimatorConst.Accelerator.DECELERATE, progression.progress.toFloat(), (p * 10000).toFloat()) { pg ->
            progression.progress = pg.toInt()
        }.start()
    }

    private fun loadFile() {
        lifecycleScope.launch {
            val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

            val prog = findViewById<ProgressBar>(R.id.prog)
            val state = findViewById<TextView>(R.id.status)

            prog.isIndeterminate = false
            prog.max = 10000

            state.setText(R.string.main_file_read)

            withContext(Dispatchers.IO) {
                DefineItf().init(this@CheckUpdateScreen)

                UserProfile.profile()

                try {
                    Definer.define(this@CheckUpdateScreen, { p -> runOnUiThread { if (p * 10000 != -1.0) prog.progress = (p * 10000).toInt() else prog.isIndeterminate = true } }, { t -> runOnUiThread { state.text = StaticStore.getLoadingText(this@CheckUpdateScreen, t) }})
                } catch (assetError: AssetException) {
                    val intent = Intent(this@CheckUpdateScreen, ErrorScreen::class.java)

                    intent.putExtra("reasonPhrase", getString(R.string.err_reason_asset))
                    intent.putExtra("solution", getString(R.string.err_solution_asset))
                    intent.putExtra("errorCode", StaticStore.ERR_ASSET)

                    startActivity(intent)
                    finish()

                    return@withContext
                }

                StaticStore.getLang(shared.getInt("Language", 0))

                StaticStore.init = true
            }

            if(PackConflict.conflicts.isEmpty()) {
                if (!MainActivity.isRunning) {
                    val intent = Intent(this@CheckUpdateScreen, MainActivity::class.java)
                    intent.putExtra("config", config)
                    startActivity(intent)
                    finish()
                }
            } else {
                val intent = Intent(this@CheckUpdateScreen, PackConflictSolve::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    inner class AssetDownloaderConnector : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            p1 ?: return

            communicator = Messenger(p1)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
        }
    }

    inner class ServiceBroadCastReceiver : BroadcastReceiver() {
        private lateinit var listener: (Intent) -> Unit

        override fun onReceive(p0: Context?, p1: Intent?) {
            p1 ?: return

            if (this::listener.isInitialized) {
                listener.invoke(p1)
            }

            val progression = findViewById<ProgressBar>(R.id.prog)
            val retry = findViewById<Button>(R.id.retry)
            val state = findViewById<TextView>(R.id.status)

            when(p1.action) {
                AssetDownloadService.FAILED -> {
                    val message = p1.getStringExtra("content") ?: ""

                    state.text = message

                    progression.isIndeterminate = false
                    progression.progress = 0
                    progression.isEnabled = false

                    StaticStore.setAppear(retry)

                    if (bound) {
                        unbindService(serviceConnector)
                        bound = false

                        stopService(Intent(this@CheckUpdateScreen, AssetDownloadService::class.java))
                    }
                }
                AssetDownloadService.PROGRESS_TEXT -> {
                    val message = p1.getStringExtra("content") ?: ""

                    state.text = message
                }
                AssetDownloadService.PROGRESS_PROGRESSION -> {
                    val value = p1.getDoubleExtra("value", 0.0)

                    if (value == -1.0) {
                        progression.isIndeterminate = true
                    } else {
                        progression.isIndeterminate = false
                        progression.max = 10000
                        progressSmoothly(p1.getDoubleExtra("value", 0.0))
                        progression.isEnabled = true
                    }
                }
            }
        }

        fun attachListener(listener: (Intent) -> Unit) {
            this.listener = listener
        }
    }
}


