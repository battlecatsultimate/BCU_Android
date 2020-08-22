package com.mandarin.bcu.androidutil.io

import android.app.Activity
import com.mandarin.bcu.androidutil.StaticStore
import common.io.PackLoader
import common.pack.Context
import java.io.File
import java.lang.Exception
import java.lang.ref.WeakReference

class AContext : Context {
    var c: WeakReference<Activity?>? = null

    fun updateActivity(a: Activity) {
        c = WeakReference(a)
    }

    fun releaseActivity() {
        c = null
    }

    override fun noticeErr(e: Exception?, t: Context.ErrType?, str: String?) {
        TODO("Not yet implemented")
    }

    override fun getWorkspaceFile(relativePath: String?): File {
        TODO("Not yet implemented")
    }

    override fun getAssetFile(string: String?): File {
        TODO("Not yet implemented")
    }

    override fun initProfile() {
        TODO("Not yet implemented")
    }

    override fun getPackFolder(): File {
        val w = c ?: return File("")

        val ac = w.get() ?: return File("")

        return File(StaticStore.getExternalPack(ac))
    }

    override fun confirmDelete(): Boolean {
        TODO("Not yet implemented")
    }

    override fun printErr(t: Context.ErrType?, str: String?) {
        TODO("Not yet implemented")
    }

    override fun getLangFile(file: String?): File {
        TODO("Not yet implemented")
    }

    override fun preload(desc: PackLoader.ZipDesc.FileDesc?): Boolean {
        TODO("Not yet implemented")
    }
}