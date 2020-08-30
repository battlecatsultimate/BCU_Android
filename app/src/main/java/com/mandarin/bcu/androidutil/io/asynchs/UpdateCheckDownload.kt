package com.mandarin.bcu.androidutil.io.asynchs

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.mandarin.bcu.ApkDownload
import com.mandarin.bcu.CheckUpdateScreen
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import common.io.assets.UpdateCheck
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class UpdateCheckDownload(ac: Activity, private val fromConfig: Boolean, private val retry: Boolean, private val notifyManager: NotificationManager, private val notifyBuilder: NotificationCompat.Builder) : AsyncTask<Void, String, Void>() {
    companion object {
        private const val APK = "apk"
        private const val UPDATE = "update"
        private const val TEXT = "text"
        const val NOTIF = "Download_Notif"
    }

    private val w = WeakReference(ac)

    private var pause = false
    private var stopper = Object()

    private val langFolder = arrayOf("en/", "jp/", "kr/", "zh/")

    private var canGo = true
    private var mustShow = false

    override fun onPreExecute() {
        val ac = w.get() ?: return

        val prog = ac.findViewById<ProgressBar>(R.id.updateprog)
        val retry = ac.findViewById<Button>(R.id.retry)

        retry.visibility = View.GONE
        prog.visibility = View.VISIBLE

        val state = ac.findViewById<TextView>(R.id.updatestate)

        state.setText(R.string.main_check_apk)
    }

    override fun doInBackground(vararg params: Void?): Void? {
        val ac = w.get() ?: return null

        val shared = ac.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

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

            for(lang in langFolder) {
                for(l in StaticStore.langfile) {
                    langFiles.add("$lang$l")
                }
            }

            println(langFiles.toTypedArray().contentDeepToString())

            val langList = UpdateCheck.checkLang(langFiles.toTypedArray()).get()

            if(!retry && (assetList.isNotEmpty() || musicList.isNotEmpty() || langList.isNotEmpty())) {
                mustShow = true

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

                val canContinue = if(assetList.isNotEmpty())
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
            }

            if(assetList.isNotEmpty()) {
                for(asset in assetList) {
                    publishProgress(ac.getString(R.string.down_state_doing)+asset.target.name, TEXT)

                    notifyBuilder.setContentTitle(ac.getString(R.string.main_notif_down))
                    notifyBuilder.setContentTitle(asset.target.name)

                    notifyManager.notify(NOTIF, R.id.downloadnotification, notifyBuilder.build())

                    asset.run(this::updateText)
                }
            }

            if(musicList.isNotEmpty()) {
                for(music in musicList) {
                    publishProgress(ac.getString(R.string.down_state_music)+music.target.name, TEXT)

                    notifyBuilder.setContentTitle(ac.getString(R.string.main_notif_music))
                    notifyBuilder.setContentTitle(music.target.name)

                    notifyManager.notify(NOTIF, R.id.downloadnotification, notifyBuilder.build())

                    music.run(this::updateText)
                }
            }

            if(langList.isNotEmpty()) {
                for(lang in langList) {
                    publishProgress(ac.getString(R.string.down_state_doing)+lang.target.name, TEXT)

                    notifyBuilder.setContentTitle(ac.getString(R.string.main_notif_down))
                    notifyBuilder.setContentTitle(lang.target.name)

                    notifyManager.notify(NOTIF, R.id.downloadnotification, notifyBuilder.build())

                    lang.run(this::updateText)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

            ErrorLogWriter.writeLog(e, StaticStore.upload, ac)

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
                showUpdateNotice(ac, array[2] == "True", array[0])
            }
            TEXT -> {
                val state = ac.findViewById<TextView>(R.id.updatestate)

                state.text = array[0]
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        val ac = w.get() ?: return

        println("I'm running")

        if(canGo) {
            AddPathes(ac, fromConfig).execute()
        } else {
            val retry = ac.findViewById<Button>(R.id.retry)
            val prog = ac.findViewById<ProgressBar>(R.id.updateprog)
            val state = ac.findViewById<TextView>(R.id.updatestate)

            retry.visibility = View.VISIBLE
            prog.isIndeterminate = false
            prog.progress = 0

            val i = Intent(ac, CheckUpdateScreen.RetryDownload(ac as CheckUpdateScreen)::class.java)
            val p = PendingIntent.getBroadcast(ac, 1, i, PendingIntent.FLAG_UPDATE_CURRENT)

            notifyBuilder.addAction(R.drawable.ic_refresh_black_24dp, "Retry", p)
                    .setContentText(ac.getString(R.string.down_state_no))

            notifyManager.notify(NOTIF, R.id.downloadnotification, notifyBuilder.build())

            state.setText(R.string.down_state_no)

            val connectivityManager = ac.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            retry.setOnClickListener {
                if (connectivityManager.activeNetwork != null) {
                    retry.visibility = View.GONE
                    prog.isIndeterminate = true
                    val checkApk = UpdateCheckDownload(ac, fromConfig, true, notifyManager, notifyBuilder)
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

                intent.putExtra("ver", thatVersion)

                ac.startActivity(intent)
                ac.finish()

                cancel(true)
            }

            val dialog = apkDialog.show()

            apkDialog.setNegativeButton(R.string.main_file_cancel) { _, _ ->
                dialog.dismiss()

                synchronized(stopper) {
                    pause = false
                    stopper.notifyAll()
                }
            }
        } else {
            synchronized(stopper) {
                pause = false
                stopper.notifyAll()
            }
        }
    }

    private fun showUpdateNotice(ac: Activity, canContinue: Boolean, title: String) {
        val updateDialog = AlertDialog.Builder(ac)

        updateDialog.setTitle(title)
        updateDialog.setMessage(R.string.main_file_up)

        updateDialog.setPositiveButton(R.string.main_file_ok) { _, _ ->
            pause = false

            synchronized(stopper) {
                stopper.notifyAll()
            }
        }

        updateDialog.setNegativeButton(R.string.main_file_cancel) { _, _ ->
            if(canContinue) {
                AddPathes(ac, fromConfig).execute()

                cancel(true)
            } else {
                ac.finish()

                cancel(true)
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
        val ac = w.get() ?: return

        val p = ac.findViewById<ProgressBar>(R.id.updateprog)

        p.max = 10000
        p.isIndeterminate = false
        p.progress = (prog * 10000.0).toInt()

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
}