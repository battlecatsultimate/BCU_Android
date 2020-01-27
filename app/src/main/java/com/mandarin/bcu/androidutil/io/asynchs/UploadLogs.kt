package com.mandarin.bcu.androidutil.io.asynchs

import android.app.Activity
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.drive.DriveUtil
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference

class UploadLogs(activity: Activity) : AsyncTask<Void?, Int?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    private var total = 0
    private var succeed = 0
    private var failed = 0
    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        val path = Environment.getDataDirectory().absolutePath + "/data/com.mandarin.bcu/upload/"
        val upload = File(path)
        val files = upload.listFiles()
        total = upload.listFiles().size
        for (i in 0 until total) {
            val f = files[i]
            publishProgress(0, i + 1)
            try {
                if (safeCheck(f)) {
                    val `in` = activity.resources.openRawResource(R.raw.service_key)
                    val good = DriveUtil.upload(f, `in`)
                    if (good) {
                        f.delete()
                        succeed++
                    } else {
                        Log.e("uploadFailed", "Uploading " + f.name + " to server failed")
                        failed++
                    }
                } else {
                    f.delete()
                    failed++
                }
            } catch (e: IOException) {
                e.printStackTrace()
                failed++
            }
        }
        publishProgress(1)
        return null
    }

    override fun onProgressUpdate(vararg result: Int?) {
        val activity = weakReference.get() ?: return
        if (result[0] == 0) {
            val str = activity.getString(R.string.err_send_log).replace("-", result[1].toString()).replace("_", total.toString())
            StaticStore.showShortMessage(activity, str)
        } else {
            val str = activity.getString(R.string.err_send_result).replace("-", succeed.toString()).replace("_", failed.toString())
            StaticStore.showShortMessage(activity, str)
        }
    }

    private fun safeCheck(f: File): Boolean {
        val name = f.name
        if (!name.endsWith("txt")) return false
        val size = f.length()
        val mb = size / 1024 / 1024
        return mb <= 10
    }

}