package com.mandarin.bcu.androidutil.medal.asynchs

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.AsyncTask
import android.os.Environment
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.medal.MDefiner
import com.mandarin.bcu.androidutil.medal.adapters.MedalListAdapter
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

class MedalAdder(activity: Activity) : AsyncTask<Void?, Int?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val listView = activity.findViewById<ListView>(R.id.medallist)
        setDisappear(listView)
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        MDefiner().define(activity)
        publishProgress(0)
        if (StaticStore.medals == null) {
            StaticStore.medals = ArrayList()
            val preferences = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
            for (i in 0 until StaticStore.medalnumber) {
                val path = StaticStore.getExternalPath(activity)+"org/page/medal/"
                val name = "medal_" + number(i) + ".png"
                val f = File(path, name)
                if (!f.exists()) {
                    continue
                }
                try {
                    val b = BitmapFactory.decodeFile(path + name)
                    StaticStore.medals.add(b)
                } catch (e: Exception) {
                    ErrorLogWriter.writeLog(e, preferences.getBoolean("upload", false) || preferences.getBoolean("ask_upload", true), activity)
                }
            }
        }
        publishProgress(1)
        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val activity = weakReference.get() ?: return
        val mlistst = activity.findViewById<TextView>(R.id.medalloadt)
        when (values[0]) {
            0 -> mlistst.setText(R.string.medal_reading_icon)
            1 -> {
                mlistst.setText(R.string.medal_loading_data)
                val medallist = activity.findViewById<ListView>(R.id.medallist)
                val display = activity.windowManager.defaultDisplay
                val p = Point()
                display.getSize(p)
                val width = p.x
                val wh: Float = if (activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 72f else 90f
                val num = (width - StaticStore.dptopx(16f, activity)) / StaticStore.dptopx(wh, activity)
                var line = StaticStore.medalnumber / num
                if (StaticStore.medalnumber % num != 0) line++
                val lines = arrayOfNulls<String>(line)
                val adapter = MedalListAdapter(activity, num, width, wh, lines)
                medallist.adapter = adapter
                medallist.isClickable = false
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get() ?: return
        val listView = activity.findViewById<ListView>(R.id.medallist)
        val prog = activity.findViewById<ProgressBar>(R.id.medalprog)
        val medalt = activity.findViewById<TextView>(R.id.medalloadt)
        setAppear(listView)
        setDisappear(prog, medalt)
    }

    private fun setDisappear(vararg views: View) {
        for (v in views) {
            v.visibility = View.GONE
        }
    }

    private fun setAppear(vararg views: View) {
        for (v in views) {
            v.visibility = View.VISIBLE
        }
    }

    private fun number(num: Int): String {
        return when (num) {
            in 0..9 -> {
                "00$num"
            }
            in 10..99 -> {
                "0$num"
            }
            else -> {
                num.toString()
            }
        }
    }

}