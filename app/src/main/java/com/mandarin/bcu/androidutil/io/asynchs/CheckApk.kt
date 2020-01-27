package com.mandarin.bcu.androidutil.io.asynchs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.mandarin.bcu.ApkDownload
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

class CheckApk : AsyncTask<Void?, String?, Void?> {
    private val weakReference: WeakReference<Activity?>
    private var thisver: String? = null
    private var cando: Boolean
    private var path: String
    private var lang: Boolean
    private val fileneed = ArrayList<String>()
    private val filenum = ArrayList<String>()
    private var contin = true
    private var config = false

    constructor(path: String, lang: Boolean, context: Activity?, cando: Boolean) {
        weakReference = WeakReference(context)
        this.path = path
        this.lang = lang
        this.cando = cando
    }

    constructor(path: String, lang: Boolean, context: Activity?, cando: Boolean, config: Boolean) {
        weakReference = WeakReference(context)
        this.path = path
        this.lang = lang
        this.cando = cando
        this.config = config
    }

    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        try {
            val packageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            thisver = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val prog = activity.findViewById<ProgressBar>(R.id.mainprogup)
        val retry = activity.findViewById<Button>(R.id.checkupretry)
        retry.visibility = View.GONE
        prog.visibility = View.VISIBLE
        val state = activity.findViewById<TextView>(R.id.mainstup)
        state.setText(R.string.main_check_apk)
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        try {
            val apklink = "https://raw.githubusercontent.com/battlecatsultimate/bcu-page/master/api/getUpdate.json"
            val apkurl = URL(apklink)
            val `in` = apkurl.openStream()
            val isr = InputStreamReader(`in`, StandardCharsets.UTF_8)
            val sb = StringBuilder()
            var cp: Int
            while (isr.read().also { cp = it } != -1) {
                sb.append(cp.toChar())
            }
            val result = sb.toString()
            val ans = JSONObject(result)
            `in`.close()
            val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
            val thatver: String
            thatver = if (shared.getBoolean("apktest", false)) {
                ans.getString("android_test")
            } else {
                ans.getString("android_ver")
            }
            publishProgress(thatver)
        } catch (e: JSONException) {
            e.printStackTrace()
            contin = false
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            contin = false
        } catch (e: IOException) {
            e.printStackTrace()
            contin = false
        }
        return null
    }

    override fun onProgressUpdate(vararg values: String?) {
        goToApk(values[0] ?: StaticStore.VER)
    }

    override fun onPostExecute(results: Void?) {
        val activity = weakReference.get() ?: return
        if (!contin) {
            if (cando) if (!config) CheckUpdates(path, lang, fileneed, filenum, activity, true).execute() else CheckUpdates(path, lang, fileneed, filenum, activity, true, true).execute() else {
                val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                if (connectivityManager.activeNetworkInfo == null) {
                    val checkup = activity.findViewById<Button>(R.id.checkupretry)
                    val prog = activity.findViewById<ProgressBar>(R.id.mainprogup)
                    val mainstup = activity.findViewById<TextView>(R.id.mainstup)
                    checkup.setOnClickListener { if (connectivityManager.activeNetworkInfo != null) CheckApk(path, lang, weakReference.get(), cando).execute() else StaticStore.showShortMessage(activity, R.string.needconnect) }
                    prog.visibility = View.GONE
                    mainstup.setText(R.string.main_internet_no)
                } else {
                    CheckApk(path, lang, weakReference.get(), cando).execute()
                }
            }
        }
    }

    private fun goToApk(ver: String) {
        val activity = weakReference.get()
        val thisnum = thisver!!.split(".").toTypedArray()
        val thatnum = ver.split(".").toTypedArray()
        val update = check(thisnum, thatnum)
        if (update) {
            val apkdon = AlertDialog.Builder(activity)
            apkdon.setCancelable(false)
            apkdon.setTitle(R.string.apk_down_title)
            val content = activity!!.getString(R.string.apk_down_content) + ver
            apkdon.setMessage(content)
            apkdon.setPositiveButton(R.string.main_file_ok) { _, _ ->
                val result = Intent(activity, ApkDownload::class.java)
                result.putExtra("ver", ver)
                activity.startActivity(result)
                activity.finish()
            }
            apkdon.setNegativeButton(R.string.main_file_cancel) { _, _ -> if (!config) CheckUpdates(path, lang, fileneed, filenum, activity, cando).execute() else CheckUpdates(path, lang, fileneed, filenum, activity, cando, true).execute() }
            val apkdown = apkdon.create()
            apkdown.show()
        } else {
            if (!config) CheckUpdates(path, lang, fileneed, filenum, activity, cando).execute() else CheckUpdates(path, lang, fileneed, filenum, activity, cando, true).execute()
        }
    }

    fun check(thisnum: Array<String>, thatnum: Array<String>): Boolean {
        var update = false
        val these = intArrayOf(thisnum[0].toInt(), thisnum[1].toInt(), thisnum[2].toInt())
        val those = intArrayOf(thatnum[0].toInt(), thatnum[1].toInt(), thatnum[2].toInt())
        if (these[0] < those[0]) update = true else if (these[0] == those[0] && these[1] < those[1]) update = true else if (these[0] == those[0] && these[1] == those[1] && these[2] < those[2]) update = true
        return update
    }
}