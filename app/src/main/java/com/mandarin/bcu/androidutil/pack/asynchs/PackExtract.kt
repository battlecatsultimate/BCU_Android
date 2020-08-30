package com.mandarin.bcu.androidutil.pack.asynchs

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import com.mandarin.bcu.MainActivity
import com.mandarin.bcu.PackConflictSolve
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.pack.PackConflict
import common.pack.UserProfile
import java.lang.ref.WeakReference

class PackExtract(ac: Activity, private val config: Boolean) : AsyncTask<Void, String, Void>() {
    private val pack = "1"
    private val image = "2"
    private val castle = "3"
    private val bg = "4"
    private val packext = "5"

    private val errInvlaid = "100"

    private var paused = false
    private var destroy = false

    private var stopper = Object()

    val a = WeakReference(ac)

    override fun doInBackground(vararg params: Void?): Void? {
        return null
    }

    override fun onProgressUpdate(vararg values: String?) {

    }

    override fun onPostExecute(result: Void?) {
        val activity = a.get() ?: return

        StaticStore.filterEntityList = BooleanArray(UserProfile.getAllPacks().size)

        if(PackConflict.conflicts.isEmpty()) {
            if (!MainActivity.isRunning && !destroy) {
                val intent = Intent(activity, MainActivity::class.java)
                intent.putExtra("config", config)
                activity.startActivity(intent)
                activity.finish()
            }
        } else {
            val intent = Intent(activity, PackConflictSolve::class.java)
            activity.startActivity(intent)
            activity.finish()
        }
    }
}