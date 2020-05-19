package com.mandarin.bcu.androidutil.io.asynchs

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.widget.TextView
import com.mandarin.bcu.MainActivity
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.pack.asynchs.PackExtract
import com.mandarin.bcu.androidutil.unit.Definer
import com.mandarin.bcu.decode.ZipLib
import common.util.pack.Background
import java.lang.ref.WeakReference

class AddPathes internal constructor(activity: Activity, private val config: Boolean) : AsyncTask<Void?, Int?, Void?>() {
    private val weakReference: WeakReference<Activity> = WeakReference(activity)
    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val checkstate = activity.findViewById<TextView>(R.id.mainstup)
        checkstate.setText(R.string.main_file_read)
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        ZipLib.init(StaticStore.getExternalPath(activity))
        ZipLib.read(StaticStore.getExternalPath(activity))
        StaticStore.getUnitnumber(activity)
        StaticStore.getEnemynumber(activity)
        StaticStore.root = 1
        DefineItf().init(activity)
        StaticStore.getLang(shared.getInt("Language", 0))

        publishProgress(0)

        if(StaticStore.units == null) {
            Definer().define(activity)
        }

        if(StaticStore.bgread == 0) {
            Background.read()
            StaticStore.bgread = 1
        }
        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val ac = weakReference.get() ?: return

        val st = ac.findViewById<TextView>(R.id.mainstup)

        st.setText(R.string.unit_list_unitload)
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get() ?: return

        if(!MainActivity.isRunning) {
            PackExtract(activity, config).execute()
        }
    }

}