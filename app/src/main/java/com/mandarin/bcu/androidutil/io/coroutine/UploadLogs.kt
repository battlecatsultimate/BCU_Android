package com.mandarin.bcu.androidutil.io.coroutine

import android.app.Activity
import android.os.Environment
import android.util.Log
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.drive.DriveUtil
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference

class UploadLogs(activity: Activity) : CoroutineTask<String>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    private var total = 0
    private var succeed = 0
    private var failed = 0

    override fun prepare() {
    }

    override fun finish() {
    }

    override fun doSomething() {
        val activity = weakReference.get() ?: return
        val path = Environment.getDataDirectory().absolutePath + "/data/com.mandarin.bcu/upload/"
        val upload = File(path)
        val files = upload.listFiles()
        total = upload.listFiles()?.size ?: 0

        for (i in 0 until total) {
            val f = files?.get(i) ?: return

            publishProgress("0", (i + 1).toString())
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
        publishProgress("1")
    }

    override fun progressUpdate(vararg data: String) {
        val activity = weakReference.get() ?: return

        if (data[0] == "0") {
            val str = activity.getString(R.string.err_send_log).replace("-", data[1]).replace("_", total.toString())
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