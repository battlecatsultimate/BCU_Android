package com.mandarin.bcu.androidutil.io

import android.app.Activity
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.CommonStatic
import common.io.PackLoader
import common.io.assets.AssetLoader
import common.pack.Context
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import common.util.Data
import common.util.stage.Music
import java.io.File
import java.io.InputStream
import java.lang.Exception
import java.lang.ref.WeakReference

class AContext : Context {
    companion object {
        fun check() {
            if(CommonStatic.ctx == null) {
                CommonStatic.ctx = AContext()
            }
        }
    }

    private val lang = arrayOf("en/", "zh/", "kr/", "jp/", "ru/", "fr/")

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

    override fun getWorkspaceFile(relativePath: String): File {
        val wac = c ?: return File("")

        val a = wac.get() ?: return File("")

        return File(relativePath.replace("./",StaticStore.getExternalWorkspace(a)))
    }

    override fun getAssetFile(string: String): File {
        val wac = c ?: return File("")

        val a = wac.get() ?: return File("")

        return File(string.replace("./",StaticStore.getExternalAsset(a)))
    }

    override fun initProfile() {
        AssetLoader.load()

        UserProfile.getBCData().load(this::printer)
    }

    override fun getPackFolder(): File {
        val w = c ?: return File("")

        val ac = w.get() ?: return File("")

        return File(StaticStore.getExternalPack(ac))
    }

    override fun getUserFile(string: String): File {
        val wac = c ?: return File("")

        val a = wac.get() ?: return File("")

        return File(string.replace("./",StaticStore.getExternalUser(a)))
    }

    override fun confirmDelete(): Boolean {
        TODO("Not yet implemented")
    }

    override fun printErr(t: Context.ErrType?, str: String?) {
        TODO("Not yet implemented")
    }

    override fun getLangFile(file: String): InputStream? {
        val wac = c ?: return null

        val a = wac.get() ?: return null

        return a.resources.openRawResource(R.raw.proc)
    }

    override fun preload(desc: PackLoader.ZipDesc.FileDesc): Boolean {
        return true
    }

    fun getMusicFile(m: Music) : File {
        val wac = c ?: return File("")
        val a = wac.get() ?: return File("")

        return if(m.id.pack == Identifier.DEF) {
            File(StaticStore.getExternalAsset(a)+"music/"+ Data.trio(m.id.id)+".ogg")
        } else {
            File(StaticStore.dataPath+"music/"+m.id.pack+"-"+Data.trio(m.id.id)+".ogg")
        }
    }

    fun printer(msg: String) {
        println(msg)
    }
}