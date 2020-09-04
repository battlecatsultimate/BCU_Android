package com.mandarin.bcu.androidutil.io.asynchs

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.mandarin.bcu.ApkDownload
import com.mandarin.bcu.CheckUpdateScreen.Companion.mustShow
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import common.CommonStatic
import common.io.assets.UpdateCheck
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class UpdateCheckDownload(ac: Activity, private val fromConfig: Boolean, private val retry: Boolean, private val notifyManager: NotificationManager, private val notifyBuilder: NotificationCompat.Builder, private val reformatRequired: Boolean) : AsyncTask<Void, String, Void>() {
    companion object {
        private const val APK = "apk"
        private const val UPDATE = "update"
        private const val TEXT = "text"
        private const val PROG = "progress"
        const val NOTIF = "Download_Notif"
    }

    private val w = WeakReference(ac)

    private var pause = false
    private var stopper = Object()

    private val langFolder = arrayOf("en/", "jp/", "kr/", "zh/")

    private var canGo = true

    private var dontGo = false

    override fun onPreExecute() {
        val ac = w.get() ?: return

        val prog = ac.findViewById<ProgressBar>(R.id.prog)
        val retry = ac.findViewById<Button>(R.id.retry)

        retry.visibility = View.GONE
        prog.visibility = View.VISIBLE

        prog.isIndeterminate = true

        val state = ac.findViewById<TextView>(R.id.status)

        state.setText(R.string.main_check_apk)
    }

    override fun doInBackground(vararg params: Void?): Void? {
        val ac = w.get() ?: return null

        val shared = ac.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val langShared = ac.getSharedPreferences(StaticStore.LANG, Context.MODE_PRIVATE)

        //Check Apk Update, skip apk checking if users are retrying to download assets
        if(!retry) {
            try {
                val apkLink = "https://raw.githubusercontent.com/battlecatsultimate/bcu-page/master/api/getUpdate.json"
                val apkURL = URL(apkLink)

                val ins = apkURL.openStream()

                val isr = InputStreamReader(ins, StandardCharsets.UTF_8)

                val sb = StringBuilder()

                var len: Int

                while(isr.read().also { len = it } != -1) {
                    sb.append(len.toChar())
                }

                val result = sb.toString()

                val ans = JSONObject(result)

                ins.close()

                val thatVersion = if(shared.getBoolean("apktest", false)) {
                    ans.getString("android_test")
                } else {
                    ans.getString("android_ver")
                }

                publishProgress(thatVersion, APK)

                pause = true

                synchronized(stopper) {
                    while(pause) {
                        stopper.wait()
                    }
                }

                if(dontGo)
                    return null
            } catch (e: Exception) {
                e.printStackTrace()

                ErrorLogWriter.writeLog(e, StaticStore.upload, ac)

                canGo = false
            }
        }

        try {
            publishProgress(ac.getString(R.string.main_check_up), TEXT)

            val updateJson = UpdateCheck.checkUpdate()

            val assetJson = getAssetJson()

            val assetList = UpdateCheck.checkAsset(updateJson, "android")

            val musicList = UpdateCheck.checkMusic(assetJson.getInt("music"))

            val langFiles = ArrayList<String>()

            langFiles.add("Difficulty.txt")

            CommonStatic.getConfig().localLangMap["Difficulty.txt"] = langShared.getString("Difficulty.txt", "")

            for(lang in langFolder) {
                for(l in StaticStore.langfile) {
                    langFiles.add("$lang$l")

                    CommonStatic.getConfig().localLangMap["$lang$l"] = langShared.getString("$lang$l", "")
                }
            }

            val langList = UpdateCheck.checkLang(langFiles.toTypedArray()).get()

            if(!retry && (assetList.isNotEmpty() || musicList.isNotEmpty() || langList.isNotEmpty())) {
                val file = File(StaticStore.getExternalAsset(ac)+"assets/")

                val msg = if(!file.exists())
                    ac.getString(R.string.main_file_need)
                else if(assetList.isNotEmpty() && musicList.isEmpty() && langList.isEmpty())
                    ac.getString(R.string.main_file_asset)
                else if(assetList.isEmpty() && musicList.isNotEmpty() && langList.isEmpty())
                    ac.getString(R.string.main_file_music)
                else if(assetList.isEmpty() && musicList.isEmpty() && langList.isNotEmpty())
                    ac.getString(R.string.main_file_text)
                else
                    ac.getString(R.string.main_file_x)

                val canContinue = if(assetList.isNotEmpty() || musicList.isNotEmpty() || langNeed)
                    "False"
                else
                    "True"

                publishProgress(msg, UPDATE, canContinue)

                pause = true

                synchronized(stopper) {
                    while(pause) {
                        stopper.wait()
                    }
                }

                if(dontGo)
                    return null
            }

            if(assetList.isNotEmpty()) {
                mustShow = true

                for(asset in assetList) {
                    publishProgress(ac.getString(R.string.down_state_doing)+asset.target.name, TEXT)

                    notifyBuilder.setContentTitle(ac.getString(R.string.main_notif_down))
                    notifyBuilder.setContentText(asset.target.name).setOngoing(true)

                    notifyManager.notify(NOTIF, R.id.downloadnotification, notifyBuilder.build())

                    asset.run(this::updateText)
                }
            }

            if(musicList.isNotEmpty()) {
                mustShow = true

                for(music in musicList) {
                    publishProgress(ac.getString(R.string.down_state_music)+music.target.name, TEXT)

                    notifyBuilder.setContentTitle(ac.getString(R.string.main_notif_music))
                    notifyBuilder.setContentText(music.target.name).setOngoing(true)

                    notifyManager.notify(NOTIF, R.id.downloadnotification, notifyBuilder.build())

                    music.run(this::updateText)
                }
            }

            if(langList.isNotEmpty()) {
                mustShow = true

                val editor = langShared.edit()

                for(lang in langList) {
                    val fileName = if(langFolder.contains((lang.target.parentFile?.name ?: "")+"/"))
                        (lang.target.parentFile?.name ?: "") + "/" + lang.target.name
                    else
                        lang.target.name

                    publishProgress(ac.getString(R.string.down_state_doing)+fileName, TEXT)

                    notifyBuilder.setContentTitle(ac.getString(R.string.main_notif_down))
                    notifyBuilder.setContentText(fileName).setOngoing(true)

                    notifyManager.notify(NOTIF, R.id.downloadnotification, notifyBuilder.build())

                    lang.run(this::updateText)

                    editor.putString(fileName, CommonStatic.getConfig().localLangMap[fileName])

                    editor.apply()
                }

                println(CommonStatic.getConfig().localLangMap)
            }
        } catch (e: Exception) {
            e.printStackTrace()

            CommonStatic.getConfig().localLangMap.clear()

            canGo = false
        }

        return null
    }

    override fun onProgressUpdate(vararg values: String?) {
        val array = Array(values.size) {
            values[it] ?: return
        }

        if(array.size < 2) {
            return
        }

        val ac = w.get() ?: return

        when(array[1]) {
            APK -> {
                goToApkDownloadScreen(ac, array[0])
            }
            UPDATE -> {
                showUpdateNotice(ac, array[2] == "True", array[0], reformatRequired)
            }
            TEXT -> {
                val state = ac.findViewById<TextView>(R.id.status)

                state.text = array[0]
            }
            PROG -> {
                val prog = ac.findViewById<ProgressBar>(R.id.prog)

                prog.max = 10000
                prog.isIndeterminate = false

                prog.progress = array[0].toInt()
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        val ac = w.get() ?: return

        if(dontGo)
            return

        if(canGo) {
            if(mustShow) {
                notifyBuilder.setContentText(null).setOngoing(false).setContentIntent(null).setProgress(0, 0, false).setContentTitle(ac.getString(R.string.down_state_ok))

                notifyManager.notify(NOTIF, R.id.downloadnotification, notifyBuilder.build())
            }

            AddPathes(ac, fromConfig).execute()
        } else {
            val retry = ac.findViewById<Button>(R.id.retry)
            val prog = ac.findViewById<ProgressBar>(R.id.prog)
            val state = ac.findViewById<TextView>(R.id.status)

            retry.visibility = View.VISIBLE
            prog.isIndeterminate = false
            prog.progress = 0

            notifyBuilder.setOngoing(false)
                    .setContentText(ac.getString(R.string.down_state_no)).setAutoCancel(true)

            notifyManager.notify(NOTIF, R.id.downloadnotification, notifyBuilder.build())

            state.setText(R.string.down_state_no)

            val connectivityManager = ac.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            retry.setOnClickListener {
                if (connectivityManager.activeNetwork != null) {
                    retry.visibility = View.GONE
                    prog.isIndeterminate = true
                    val checkApk = UpdateCheckDownload(ac, fromConfig, true, notifyManager, notifyBuilder, reformatRequired)
                    checkApk.execute()
                } else {
                    StaticStore.showShortMessage(ac, R.string.needconnect)
                }
            }
        }
    }

    private fun goToApkDownloadScreen(ac: Activity, thatVersion: String) {
        val thisVersion = ac.packageManager.getPackageInfo(ac.packageName, 0).versionName

        val thisNum = thisVersion.split(".")
        val thatNum = thatVersion.split(".")

        val update = check(thisNum, thatNum)

        if(update) {
            val apkDialog = AlertDialog.Builder(ac)

            apkDialog.setCancelable(false)

            apkDialog.setTitle(R.string.apk_down_title)

            val content = ac.getString(R.string.apk_down_content) + thatVersion

            apkDialog.setMessage(content)
            apkDialog.setPositiveButton(R.string.main_file_ok) { _, _ ->
                val intent = Intent(ac, ApkDownload::class.java)
                dontGo = true

                intent.putExtra("ver", thatVersion)

                ac.startActivity(intent)
                ac.finish()

                synchronized(stopper) {
                    pause = false
                    stopper.notifyAll()
                }
            }

            apkDialog.setNegativeButton(R.string.main_file_cancel) { _, _ ->
                synchronized(stopper) {
                    pause = false
                    stopper.notifyAll()
                }
            }

            apkDialog.show()
        } else {
            synchronized(stopper) {
                pause = false
                stopper.notifyAll()
            }
        }
    }

    private fun showUpdateNotice(ac: Activity, canContinue: Boolean, title: String, reformatRequired: Boolean) {
        val updateDialog = AlertDialog.Builder(ac)

        updateDialog.setTitle(title)
        updateDialog.setMessage(R.string.main_file_up)

        updateDialog.setPositiveButton(R.string.main_file_ok) { _, _ ->
            pause = false

            mustShow = true

            synchronized(stopper) {
                stopper.notifyAll()
            }
        }

        updateDialog.setNegativeButton(R.string.main_file_cancel) { _, _ ->
            dontGo = true

            if(canContinue) {
                if(reformatRequired) {
                    ReviveOldFiles(ac, fromConfig).execute()
                } else {
                    AddPathes(ac, fromConfig).execute()
                }
            } else {
                ac.finish()
            }

            pause = false

            synchronized(stopper) {
                stopper.notifyAll()
            }
        }

        updateDialog.show()
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

    private fun updateText(prog: Double) {
        w.get() ?: return

        publishProgress((prog * 10000.0).toInt().toString(), PROG)

        notifyBuilder.setProgress(10000, (prog * 10000).toInt(), false)
        notifyManager.notify(NOTIF, R.id.downloadnotification, notifyBuilder.build())
    }

    private fun getAssetJson() : JSONObject {
        val url = URL("https://raw.githubusercontent.com/battlecatsultimate/bcu-page/master/api/getUpdate.json")

        val connection = url.openConnection() as HttpURLConnection

        val bfr = BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8))

        val sb = StringBuilder()

        var len: Int

        while(bfr.read().also { len = it } != -1) {
            sb.append(len.toChar())
        }

        return JSONObject(sb.toString())
    }

    private val langNeed: Boolean
        get() {
            val ac = w.get() ?: return true

            var f = File(StaticStore.getExternalAsset(ac)+"lang/Difficulty.txt")

            if(!f.exists())
                return true

            for(lang in langFolder) {
                for(l in StaticStore.langfile) {
                    f = File(StaticStore.getExternalAsset(ac)+"lang$lang$l")

                    if(!f.exists())
                        return true
                }
            }

            return false
        }
}