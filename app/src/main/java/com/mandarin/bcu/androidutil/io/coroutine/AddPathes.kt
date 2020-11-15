package com.mandarin.bcu.androidutil.io.coroutine

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.ProgressBar
import android.widget.TextView
import com.mandarin.bcu.MainActivity
import com.mandarin.bcu.PackConflictSolve
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.pack.PackConflict
import com.mandarin.bcu.androidutil.supports.CoroutineTask
import common.io.assets.AssetLoader
import common.pack.UserProfile
import java.lang.ref.WeakReference

class AddPathes internal constructor(activity: Activity, private val config: Boolean) : CoroutineTask<String>() {

    private val weakReference: WeakReference<Activity> = WeakReference(activity)

    override fun prepare() {
        val activity = weakReference.get() ?: return
        val checkstate = activity.findViewById<TextView>(R.id.status)
        val prog = activity.findViewById<ProgressBar>(R.id.prog)

        checkstate.setText(R.string.main_file_read)
        prog.isIndeterminate = true
    }

    override fun doSomething() {
        val activity = weakReference.get() ?: return
        val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        publishProgress(StaticStore.TEXT, activity.getString(R.string.main_file_merge))

        AssetLoader.merge()

        publishProgress(StaticStore.TEXT, activity.getString(R.string.main_file_read))

        DefineItf().init(activity)

        UserProfile.profile()

        Definer.define(activity, this::updateProgress, this::updateText)

        StaticStore.getLang(shared.getInt("Language", 0))

        StaticStore.init = true
    }

    override fun progressUpdate(vararg data: String) {
        val ac = weakReference.get() ?: return

        when(data[0]) {
            StaticStore.TEXT -> {
                val st = ac.findViewById<TextView>(R.id.status)

                st.text = data[1]
            }
            StaticStore.PROG -> {
                val prog = ac.findViewById<ProgressBar>(R.id.prog)

                if(data[1].toInt() == -1) {
                    prog.isIndeterminate = true

                    return
                }

                prog.isIndeterminate = false
                prog.max = 10000
                prog.progress = data[1].toInt()
            }
        }
    }

    override fun finish() {
        val activity = weakReference.get() ?: return

        StaticStore.filterEntityList = BooleanArray(UserProfile.getAllPacks().size)

        if(PackConflict.conflicts.isEmpty()) {
            if (!MainActivity.isRunning) {
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

    private fun updateText(info: String) {
        val ac = weakReference.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProgress(progress: Double) {
        weakReference.get() ?: return

        publishProgress(StaticStore.PROG, (progress*10000).toInt().toString())
    }
}