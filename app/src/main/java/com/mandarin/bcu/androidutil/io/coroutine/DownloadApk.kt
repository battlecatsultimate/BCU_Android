package com.mandarin.bcu.androidutil.io.coroutine

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.FileProvider
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class DownloadApk(context: Activity, private val ver: String, private val url: String, private val path: String, private val realpath: String) : CoroutineTask<String>() {
    private val weakReference: WeakReference<Activity> = WeakReference(context)
    private var output: File? = null

    override fun prepare() {
        val activity = weakReference.get() ?: return
        val state = activity.findViewById<TextView>(R.id.apkstate)
        state?.setText(R.string.down_wait)
    }

    override fun doSomething() {
        try {
            val realurl = URL(url)
            val c = realurl.openConnection() as HttpURLConnection
            c.requestMethod = "GET"
            c.connect()
            val size = c.contentLength.toLong()
            output = File(realpath)

            val pathes = File(path)

            if (!pathes.exists()) {
                pathes.mkdirs()
            }
            if (!output!!.exists()) {
                output!!.createNewFile()
            }

            val fos = FileOutputStream(output)
            val `is` = c.inputStream
            val buffer = ByteArray(1024)
            var len1: Int
            var total = 0.toLong()
            while (`is`.read(buffer).also { len1 = it } != -1) {
                total += len1.toLong()
                val progress = (total * 100 / size).toInt()
                publishProgress(progress.toString())
                fos.write(buffer, 0, len1)
            }
            c.disconnect()
            fos.close()
            `is`.close()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            output = null
        } catch (e: IOException) {
            e.printStackTrace()
            output = null
        }
    }

    override fun progressUpdate(vararg data: String) {
        val activity = weakReference.get() ?: return

        val prog = activity.findViewById<ProgressBar>(R.id.apkprog)
        val state = activity.findViewById<TextView>(R.id.apkstate)
        val name = activity.getString(R.string.down_state_doing) + "BCU_Android_" + ver + ".apk"
        if (state != null) state.text = name
        if (prog != null) {
            if (prog.isIndeterminate) {
                prog.isIndeterminate = false
            }
            prog.progress = data[0].toInt()
        }

    }

    override fun finish() {
        val activity = weakReference.get() ?: return
        if (output == null) {
            val retry = activity.findViewById<Button>(R.id.apkretry)
            if (retry != null) retry.visibility = View.VISIBLE
        } else {
            val install = File(realpath)
            val apkuri = FileProvider.getUriForFile(activity, "com.mandarin.bcu.provider", install)

            val intent = Intent(Intent.ACTION_VIEW).setDataAndType(apkuri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            activity.startActivity(intent)
            activity.finish()
        }
    }
}