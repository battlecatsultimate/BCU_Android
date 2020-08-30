package com.mandarin.bcu.androidutil.io.asynchs

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.widget.ProgressBar
import android.widget.TextView
import com.mandarin.bcu.MainActivity
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.pack.asynchs.PackExtract
import common.io.assets.AssetLoader
import common.pack.UserProfile
import common.system.files.VFile
import java.lang.ref.WeakReference

class AddPathes internal constructor(activity: Activity, private val config: Boolean) : AsyncTask<Void, String, Void>() {
    companion object {
        private const val TEXT = "text"
    }

    private val weakReference: WeakReference<Activity> = WeakReference(activity)

    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val checkstate = activity.findViewById<TextView>(R.id.updatestate)
        val prog = activity.findViewById<ProgressBar>(R.id.updateprog)

        checkstate.setText(R.string.main_file_read)
        prog.isIndeterminate = true
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        AssetLoader.merge()

        DefineItf().init(activity)

        UserProfile.profile()

        AssetLoader.load(this::updateProgress)

        printFileList(VFile.getBCFileTree())

        UserProfile.getBCData().load(this::updateText, this::updateProgress)

        Definer.define(activity)

        StaticStore.getLang(shared.getInt("Language", 0))

        StaticStore.init = true

        return null
    }

    override fun onProgressUpdate(vararg values: String?) {
        val ac = weakReference.get() ?: return

        val array = Array(values.size) {
            values[it] ?: return
        }

        when(array[1]) {
            TEXT -> {
                val st = ac.findViewById<TextView>(R.id.updatestate)

                st.text = array[0]
            }
        }
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get() ?: return

        if(!MainActivity.isRunning) {
            PackExtract(activity, config).execute()
        }
    }

    private fun updateText(info: String) {
        weakReference.get() ?: return

        publishProgress(info, TEXT)
    }

    private fun updateProgress(progress: Double) {
        val ac = weakReference.get() ?: return

        val prog = ac.findViewById<ProgressBar>(R.id.updateprog)

        prog.isIndeterminate = false

        prog.max = 10000

        prog.progress = (progress*10000).toInt()
    }

    private fun printFileList(v: VFile) {
        val lit = v.list()

        if(lit == null) {
            if(v.path.contains("029.imgcut") && v.path.contains("page")) {
                println(v.data.readLine())
            }
            return
        }

        for(vf in lit) {
            printFileList(vf)
        }
    }
}