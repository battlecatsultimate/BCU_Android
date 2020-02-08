package com.mandarin.bcu.androidutil.io.asynchs

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.mandarin.bcu.CheckUpdateScreen
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.io.Reader
import java.io.*
import java.lang.ref.WeakReference
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal class Unzipper(private val path: String, private val fileneed: ArrayList<String>, private val extracting: String, context: Activity, private val upload: Boolean) : AsyncTask<Void?, Int?, Void?>() {
    private val destin: String = path + "files/"
    private var contin = true
    private val weakReference: WeakReference<Activity> = WeakReference(context)
    override fun doInBackground(vararg voids: Void?): Void? {
        val j: Int = if (fileneed.contains("Language")) fileneed.size - 1 else fileneed.size
        for (i in 0 until j) {
            try {
                val source = path + fileneed[i] + ".zip"
                val `is`: InputStream = FileInputStream(source)
                val zis = ZipInputStream(BufferedInputStream(`is`))
                var ze: ZipEntry?
                val buffer = ByteArray(1024)
                var count: Int
                while (zis.nextEntry.also { ze = it } != null) {
                    if(ze == null) break
                    val filenam = ze?.name
                    val f = File(destin + filenam)
                    if (ze?.isDirectory == true) {
                        if (!f.exists()) f.mkdirs()
                        continue
                    }
                    val dir = File(f.parent)
                    if (!dir.exists()) dir.mkdirs()
                    if (!f.exists()) f.createNewFile()
                    val fout = FileOutputStream(f)
                    while (zis.read(buffer).also { count = it } != -1) {
                        publishProgress(i)
                        fout.write(buffer, 0, count)
                    }
                    fout.close()
                    zis.closeEntry()
                }
                zis.close()
            } catch (e: IOException) {
                ErrorLogWriter.writeLog(e, upload)
                contin = false
            }
        }
        return null
    }

    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val prog = activity.findViewById<ProgressBar>(R.id.downprog)
        val state = activity.findViewById<TextView>(R.id.downstate)
        prog.isIndeterminate = true
        state.setText(R.string.down_zip_ex)
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val activity = weakReference.get() ?: return
        val state = activity.findViewById<TextView>(R.id.downstate)
        val t = extracting + fileneed[values[0] ?: 0]
        state.text = t
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get() ?: return
        if (contin) {
            infowirter()
            for (s in fileneed) {
                val f = File(path, "$s.zip")
                if (f.exists()) {
                    f.delete()
                }
            }
            val intent = Intent(activity, CheckUpdateScreen::class.java)
            StaticStore.clear()
            activity.startActivity(intent)
            activity.finish()
        } else {
            val retry = activity.findViewById<Button>(R.id.retry)
            val checkstate = activity.findViewById<TextView>(R.id.downstate)
            val prog = activity.findViewById<ProgressBar>(R.id.downprog)
            checkstate.setText(R.string.unzip_fail)
            prog.isIndeterminate = false
            retry.visibility = View.VISIBLE
        }
    }

    private fun infowirter() {
        val pathes = path + "files/info/"
        val filename = "info_android.ini"
        val f = File(pathes, filename)
        try {
            val libs = infolibbuild()
            val fos = FileOutputStream(f, false)
            fos.write(libs.toByteArray())
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun infolibbuild(): String {
        val pathes = path + "files/info/"
        val filename = "info_android.ini"
        var original: MutableSet<String?>?
        val result = StringBuilder()
        val abort = StringBuilder()
        for (i in fileneed.indices) {
            if (i != fileneed.size - 1) {
                abort.append(fileneed[i]).append(",")
            } else {
                abort.append(fileneed[i])
            }
        }
        abort.insert(0, "file_version = 00040510\nnumber_of_libs = " + fileneed.size + "\nlib=")
        val f = File(pathes, filename)
        return if (f.exists()) {
            try {
                original = Reader.getInfo(path)
                original?.let { println(it) }
                if (original == null) {
                    original = TreeSet()
                }
                original.addAll(fileneed)
                val combined = ArrayList(original)
                for (i in combined.indices) {
                    if (i != combined.size - 1) {
                        result.append(combined[i]).append(",")
                    } else {
                        result.append(combined[i])
                    }
                }
                result.insert(0, "file_version = 00040510\nnumber_of_libs = " + combined.size + "\nlib=")
                result.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                abort.toString()
            }
        } else {
            abort.toString()
        }
    }

}