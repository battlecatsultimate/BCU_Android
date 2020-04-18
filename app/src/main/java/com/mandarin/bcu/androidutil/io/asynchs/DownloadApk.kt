package com.mandarin.bcu.androidutil.io.asynchs

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.mandarin.bcu.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URI
import java.net.URL

class DownloadApk(context: Activity, private val ver: String, private val url: String, private val path: String, private val realpath: String) : AsyncTask<Void?, Int?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(context)
    private var output: File? = null
    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val state = activity.findViewById<TextView>(R.id.apkstate)
        state?.setText(R.string.down_wait)
    }

    override fun doInBackground(vararg voids: Void?): Void? {
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
                publishProgress(progress)
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
        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val activity = weakReference.get() ?: return
        val prog = activity.findViewById<ProgressBar>(R.id.apkprog)
        val state = activity.findViewById<TextView>(R.id.apkstate)
        val name = activity.getString(R.string.down_state_doing) + "BCU_Android_" + ver + ".apk"
        if (state != null) state.text = name
        if (prog != null) {
            if (prog.isIndeterminate) {
                prog.isIndeterminate = false
            }
            prog.progress = values[0] ?: 0
        }
    }

    override fun onPostExecute(result: Void?) {
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