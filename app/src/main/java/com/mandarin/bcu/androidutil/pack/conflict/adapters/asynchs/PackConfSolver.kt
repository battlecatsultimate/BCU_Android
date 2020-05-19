package com.mandarin.bcu.androidutil.pack.conflict.adapters.asynchs

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import com.mandarin.bcu.MainActivity
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.pack.PackConflict
import java.lang.ref.WeakReference

class PackConfSolver(c: Activity) : AsyncTask<Void, String, Void>() {
    private val weak = WeakReference(c)

    private var canDo = false

    override fun onPreExecute() {
        val ac = weak.get() ?: return

        val solve = ac.findViewById<Button>(R.id.packconfsolve)
        val prog = ac.findViewById<ProgressBar>(R.id.packconfprog)

        solve.visibility = View.GONE
        prog.visibility = View.GONE
    }

    override fun doInBackground(vararg params: Void?): Void? {
        val ac = weak.get() ?: return null

        val c = check()

        if(c.isNotEmpty()) {
            publishProgress(ac.getString(R.string.pack_conf_solve_solve).replace("_", c))
            return null
        }

        for(pc in PackConflict.conflicts) {
            pc.solve(ac)

            if(!pc.isSolved) {
                val err = when(pc.err) {
                    PackConflict.ERR_ACTION -> "ERR_ACTION"
                    PackConflict.ERR_FILE -> "ERR_FILE"
                    PackConflict.ERR_INDEX -> "ERR_INDEX"
                    else -> "UNKNOWN"
                }

                Log.e(err, pc.msg)
            }
        }

        PackConflict.conflicts.clear()

        canDo = true

        return null
    }

    override fun onProgressUpdate(vararg values: String?) {
        val ac = weak.get() ?: return

        StaticStore.showShortMessage(ac, values[0])
    }

    override fun onPostExecute(result: Void?) {
        val ac = weak.get() ?: return

        if(canDo) {
            val intent = Intent(ac, MainActivity::class.java)

            ac.startActivity(intent)
            ac.finish()
        } else {
            val solve = ac.findViewById<Button>(R.id.packconfsolve)
            val prog = ac.findViewById<ProgressBar>(R.id.packconfprog)

            solve.visibility = View.VISIBLE
            prog.visibility = View.GONE
        }
    }

    private fun check() : String {
        var res = 0

        for(p in PackConflict.conflicts.indices) {
            val pc = PackConflict.conflicts[p]

            if(pc.confPack.isEmpty()) {
                continue
            }

            if(pc.id != PackConflict.ID_CORRUPTED && pc.isSolvable && pc.action == PackConflict.ACTION_NONE) {
                res++
            }
        }

        if(res == 0)
            return ""

        return if(StaticStore.isEnglish()) {
            if(res == 1) {
                "$res conflict"
            } else {
                "$res conflicts"
            }
        } else {
            res.toString()
        }
    }
}