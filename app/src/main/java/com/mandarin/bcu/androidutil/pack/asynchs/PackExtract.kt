package com.mandarin.bcu.androidutil.pack.asynchs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import com.mandarin.bcu.MainActivity
import com.mandarin.bcu.PackConflictSolve
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.fakeandroid.FIBM
import com.mandarin.bcu.androidutil.pack.AImageWriter
import com.mandarin.bcu.androidutil.io.DefferedLoader
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.pack.PackConflict
import common.pack.UserProfile
import common.system.fake.FakeImage
import common.util.Data
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.ref.WeakReference
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.ArrayList

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