package com.mandarin.bcu.androidutil.io.asynchs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.widget.TextView
import com.mandarin.bcu.MainActivity
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.decode.ZipLib
import java.lang.ref.WeakReference

class AddPathes internal constructor(activity: Activity, private val config: Boolean) : AsyncTask<Void?, Int?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val checkstate = activity.findViewById<TextView>(R.id.mainstup)
        checkstate?.setText(R.string.main_file_read)
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        ZipLib.init()
        ZipLib.read()
        StaticStore.getUnitnumber()
        StaticStore.getEnemynumber()
        StaticStore.root = 1
        DefineItf().init()
        StaticStore.getLang(shared.getInt("Language", 0))
        return null
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get() ?: return
        if (!MainActivity.isRunning) {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra("config", config)
            activity.startActivity(intent)
            activity.finish()
        }
    }

}