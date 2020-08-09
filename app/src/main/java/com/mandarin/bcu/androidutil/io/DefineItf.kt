package com.mandarin.bcu.androidutil.io

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.pack.AACLoader
import com.mandarin.bcu.androidutil.pack.AImageReader
import com.mandarin.bcu.androidutil.pack.AMusicLoader
import common.CommonStatic
import common.CommonStatic.Itf
import common.io.InStream
import common.io.OutStream
import common.system.VImg
import common.system.files.VFile
import common.util.anim.AnimCI
import java.io.*
import java.util.*
import java.util.function.Function

class DefineItf : Itf {
    companion object {
        var dir: String = ""
        val packPath = ArrayList<String>()

        fun check(c: Context) {
            if(dir == "") {
                dir = StaticStore.getExternalPath(c)
            }
        }
    }

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

    override fun loadAnim(ins: InStream?, r: CommonStatic.ImgReader?): AnimCI.AnimLoader {
        var name = ""

        return if(r != null) {
            name = (r as AImageReader).name

            if(r.isNull) {
                AACLoader(ins, dir, name)
            } else {
                AACLoader(ins, r)
            }
        } else {
            AACLoader(ins, dir, name)
        }
    }

    override fun delete(file: File) {
        if (file.isDirectory) {
            val lit = file.listFiles() ?: return

            for (g in lit)
                delete(g)
        } else {
            file.delete()
        }
    }

    override fun exit(save: Boolean) {}

    override fun prog(str: String) {}

    override fun getMusicLength(f: File?): Long {
        f ?: return -1

        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(f.absolutePath)

        return mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
    }

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

    override fun route(path: String?): File {
        val realPath = path?.replace("./",dir) ?: ""

        return File(realPath)
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
        return func.apply(ArrayDeque())
    }

    override fun writeErrorLog(e: java.lang.Exception) {
        ErrorLogWriter.writeDriveLog(e)
    }

    override fun redefine(class1: Class<*>?) {}
    override fun setSE(ind: Int) {
        SoundHandler.setSE(ind)
    }

    override fun getMusicReader(pid: Int, mid: Int): CommonStatic.ImgReader? {
        return AMusicLoader(pid, mid)
    }

    override fun getReader(f: File?): CommonStatic.ImgReader? {
        val path = f?.absolutePath ?: ""

        println(f?.name)

        return when {

            path.endsWith(".bcupack") -> {
                packPath.add(f?.absolutePath ?: "")
                AImageReader(f?.name?.replace(".bcupack", "")?.replace(".bcudata", "")
                        ?: "", true)
            }

            path.endsWith(".bcudata") -> {
                AImageReader(f?.name?.replace(".bcupack", "")?.replace(".bcudata", "")
                        ?: "", false)
            }
            else -> {
                null
            }
        }
    }

    override fun writeBytes(os: OutStream, path: String): Boolean {
        os.terminate()

        val f = if(path.startsWith(".")) {
            CommonStatic.def.route(path)
        } else {
            File(path)
        }

        val dir = f.parentFile

        if(dir != null) {
            if(!dir.exists()) {
                if(!dir.mkdirs()) {
                    Log.e("ItfWriteBytes","Failed to create directory "+dir.absolutePath)
                    return false
                }
            }
        }

        if(!f.exists()) {
            if(!f.createNewFile()) {
                Log.e("ItfWriteBytes","Faile to create file "+f.absolutePath)
                return false
            }
        }

        var done = false

        try {
            val fos = FileOutputStream(f)
            os.flush(fos)
            fos.close()
            done = true
        } catch (e: IOException) {
            e.printStackTrace()
            return done
        }

        return done
    }

    fun init(c: Context) {
        dir = StaticStore.getExternalPath(c)

        CommonStatic.def = this
    }
}