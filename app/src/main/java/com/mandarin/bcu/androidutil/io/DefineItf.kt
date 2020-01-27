package com.mandarin.bcu.androidutil.io

import android.graphics.BitmapFactory
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import common.CommonStatic
import common.CommonStatic.Itf
import common.io.InStream
import common.io.OutStream
import common.system.VImg
import common.system.files.VFile
import java.io.*
import java.util.*
import java.util.function.Function

class DefineItf : Itf {
    override fun check(f: File) {
        try {
            if (f.isFile) {
                val g = File(f.absolutePath)
                if (!g.exists()) g.mkdirs()
                if (!f.exists()) f.createNewFile()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun delete(file: File) {
        if (file.isDirectory) for (g in file.listFiles()) delete(g) else file.delete()
    }

    override fun exit(save: Boolean) {}
    override fun prog(str: String) {}
    override fun readBytes(fi: File): InStream? {
        try {
            val bytes = ByteArray(fi.length().toInt())
            val bis = BufferedInputStream(FileInputStream(fi))
            bis.read(bytes, 0, bytes.size)
            bis.close()
            return InStream.getIns(bytes)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun readReal(fi: File): VImg {
        return VImg(BitmapFactory.decodeFile(fi.absolutePath))
    }

    override fun <T> readSave(path: String, func: Function<Queue<String>, T>): T {
        val f = VFile.getFile(path)
        val qs = f.data!!.readLine()
        if (qs != null) try {
            val t: T? = func.apply(qs)
            if (t != null) return t
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return func.apply(ArrayDeque<String>())
    }

    override fun redefine(class1: Class<*>?) {}
    override fun setSE(ind: Int) {
        SoundHandler.setSE(ind)
    }

    override fun writeBytes(os: OutStream, path: String): Boolean {
        return true
    }

    fun init() {
        CommonStatic.def = this
    }
}