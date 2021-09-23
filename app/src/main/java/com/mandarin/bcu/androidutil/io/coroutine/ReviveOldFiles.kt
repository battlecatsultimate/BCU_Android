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
import java.io.File
import java.lang.ref.WeakReference

class ReviveOldFiles(ac: Activity, private val config: Boolean) : CoroutineTask<String>() {
    private val w = WeakReference(ac)

    override fun prepare() {
        val ac = w.get() ?: return

        val st = ac.findViewById<TextView>(R.id.status)

        st.text = ac.getString(R.string.main_rev_check)
    }

    override fun doSomething() {
        val ac = w.get() ?: return

        publishProgress(StaticStore.TEXT, ac.getString(R.string.main_rev_remove))

        var f = File(StaticStore.getExternalPath(ac)+"org/")

        if(f.exists()) {
            StaticStore.deleteFile(f, true)
        }

        f = File(StaticStore.getExternalPath(ac)+"music/")

        if(f.exists()) {
            StaticStore.deleteFile(f, true)
        }

        f = File(StaticStore.getExternalPath(ac)+"lang/")

        if(f.exists()) {
            StaticStore.deleteFile(f, true)
        }

        f = File(StaticStore.getExternalPath(ac)+"info/")

        if(f.exists()) {
            StaticStore.deleteFile(f, true)
        }

        f = File(StaticStore.getExternalPath(ac)+"_MACOSX/")

        if(f.exists()) {
            StaticStore.deleteFile(f, true)
        }

        val shared = ac.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        publishProgress(StaticStore.TEXT, ac.getString(R.string.main_file_merge))

        AssetLoader.merge()

        publishProgress(StaticStore.TEXT, ac.getString(R.string.main_file_read))

        DefineItf().init(ac)

        UserProfile.profile()

        Definer.define(ac, this::updateProgress, this::updateText)

        StaticStore.getLang(shared.getInt("Language", 0))

        StaticStore.init = true

        publishProgress(StaticStore.TEXT, ac.getString(R.string.main_rev_reformat))
    }

    override fun progressUpdate(vararg data: String) {
        val ac = w.get() ?: return

        val st = ac.findViewById<TextView>(R.id.status)

        when(data[0]) {
            StaticStore.TEXT -> {
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
        val ac = w.get() ?: return

        StaticStore.filterEntityList = BooleanArray(UserProfile.getAllPacks().size)

        val shared = ac.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        shared.edit().putBoolean("Reformat0150", false).apply()

        if(PackConflict.conflicts.isEmpty()) {
            if (!MainActivity.isRunning) {
                val intent = Intent(ac, MainActivity::class.java)
                intent.putExtra("config", config)
                ac.startActivity(intent)
                ac.finish()
            }
        } else {
            val intent = Intent(ac, PackConflictSolve::class.java)
            ac.startActivity(intent)
            ac.finish()
        }
    }

    private fun updateText(info: String) {
        val ac = w.get() ?: return

        publishProgress(StaticStore.TEXT, StaticStore.getLoadingText(ac, info))
    }

    private fun updateProgress(progress: Double) {
        w.get() ?: return

        publishProgress(StaticStore.PROG, (progress*10000).toInt().toString())
    }
}