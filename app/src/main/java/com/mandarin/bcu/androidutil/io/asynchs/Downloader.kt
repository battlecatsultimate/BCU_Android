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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import kotlin.math.abs

class Downloader(private val fileneed: ArrayList<String>, music: ArrayList<String>, context: Activity) : AsyncTask<Void?, Int?, Void?>() {
    private var size = 0
    private val sizes: MutableMap<String, Long> = HashMap()
    private val remover = ArrayList<Boolean>()
    private val lan = arrayOf("/en/", "/jp/", "/kr/", "/zh/")
    private val difffile = "Difficulty.txt"
    private val musics: ArrayList<String> = music
    private val weakActivity: WeakReference<Activity> = WeakReference(context)
    private var purifyneed = ArrayList<String>()
    private var output: File? = null

    override fun onPreExecute() {
        super.onPreExecute()
        val activity = weakActivity.get() ?: return
        val prog = activity.findViewById<ProgressBar>(R.id.downprog)
        val state = activity.findViewById<TextView>(R.id.downstate)
        prog.isIndeterminate = true
        state.setText(R.string.down_check_file)
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val a = weakActivity.get() ?: return null

        var url: String
        var link: URL
        var connection: HttpURLConnection
        val urls = "https://github.com/battlecatsultimate/bcu-assets/blob/master/assets/"
        val raw = "?raw=true"

        val assetPath = StaticStore.getExternalAsset(a)+"assets/"

        for (i in fileneed.indices) {
            try {
                val f = File(assetPath, fileneed[i] + ".asset.bcuzip")
                if (!f.exists()) continue
                url = urls + fileneed[i] + ".asset.bcuzip" + raw
                link = URL(url)
                connection = link.openConnection() as HttpURLConnection
                size = connection.contentLength
                sizes[fileneed[i]] = size.toLong()
                connection.disconnect()
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        purify(sizes)

        val number: Int = if (purifyneed.contains("Language") && purifyneed.contains("Music")) {
            purifyneed.size - 2
        } else if (purifyneed.contains("Language") || purifyneed.contains("Music")) {
            purifyneed.size - 1
        } else {
            purifyneed.size
        }

        var total: Long

        for (i in 0 until number) {
            try {
                url = urls + purifyneed[i] + ".asset.bcuzip" + raw

                link = URL(url)

                connection = link.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                size = connection.contentLength

                publishProgress(-1,size)

                connection.responseCode

                output = File(assetPath, purifyneed[i] + ".asset.bcuzip")

                val pathes = File(assetPath)

                if (!pathes.exists()) {
                    pathes.mkdirs()
                }
                if (!output!!.exists()) {
                    output!!.createNewFile()
                }

                val fos = FileOutputStream(output)

                val `is` = connection.inputStream

                val buffer = ByteArray(1024)

                var len1: Int

                total = 0

                while (`is`.read(buffer).also { len1 = it } != -1) {
                    total += abs(len1).toLong()

                    val progress = total.toInt()

                    publishProgress(progress, i)
                    fos.write(buffer, 0, len1)
                }

                connection.disconnect()
            } catch (e: MalformedURLException) {
                ErrorLogWriter.writeLog(e, StaticStore.upload, a)

                output = null
            } catch (e: IOException) {
                ErrorLogWriter.writeLog(e, StaticStore.upload, a)

                output = null
            }
            if (output != null) {
                remover.add(false)
            }
        }

        println(musics)

        if (purifyneed.contains("Music")) {
            val musicPath = StaticStore.getExternalAsset(a) + "music/"

            if (musics.isNotEmpty()) {
                try {
                    for (i in musics.indices) {
                        val murl = "https://github.com/battlecatsultimate/bcu-resources/raw/master/resources/music/"
                        val mfile = musics[i]

                        link = URL(murl + mfile)

                        connection = link.openConnection() as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.connect()
                        connection.responseCode

                        size = connection.contentLength

                        publishProgress(-1,size)

                        output = File(musicPath, mfile)

                        val mf = output!!.parentFile ?: return null

                        if (!mf.exists()) {
                            mf.mkdirs()
                        }

                        if (!output!!.exists()) {
                            output!!.createNewFile()
                        }

                        val fos = FileOutputStream(output)
                        val `is` = connection.inputStream
                        val buffer = ByteArray(1024)
                        var len1: Int
                        total = 0
                        while (`is`.read(buffer).also { len1 = it } != -1) {
                            total += len1.toLong()
                            var progress = 0
                            if (size != 0) progress = total.toInt()
                            publishProgress(progress, 50, i)
                            fos.write(buffer, 0, len1)
                        }
                        connection.disconnect()
                        fos.close()
                        `is`.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            purifyneed.remove("Music")
            fileneed.remove("Music")
        }
        if (purifyneed.contains("Language")) {
            val langPath = StaticStore.getExternalAsset(a) + "lang/"

            try {
                val lurl = "https://raw.githubusercontent.com/battlecatsultimate/bcu-resources/master/resources/lang"
                val durl = "$lurl/$difffile"
                link = URL(durl)
                connection = link.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                connection.responseCode
                output = File(langPath, difffile)
                val dpath = output!!.parentFile ?: return null
                if (!dpath.exists()) {
                    dpath.mkdirs()
                }
                if (!output!!.exists()) {
                    output!!.createNewFile()
                }
                val dfos = FileOutputStream(output)
                val dis = connection.inputStream
                var buffer = ByteArray(1024)
                var len1: Int
                total = 0
                while (dis.read(buffer).also { len1 = it } != -1) {
                    total += len1.toLong()
                    var progress = 0
                    if (size != 0) progress = (total * 100 / size).toInt()
                    publishProgress(progress, 100)
                    dfos.write(buffer, 0, len1)
                }
                connection.disconnect()
                dis.close()
                dfos.close()

                for (s1 in lan) {
                    for (s in StaticStore.langfile) {
                        val langurl = lurl + s1 + s + raw
                        link = URL(langurl)
                        connection = link.openConnection() as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.connect()
                        connection.responseCode
                        output = File(langPath + s1, s)
                        val pathes = File(langPath + s1)
                        if (!pathes.exists()) {
                            pathes.mkdirs()
                        }
                        if (!output!!.exists()) {
                            output!!.createNewFile()
                        }
                        val fos = FileOutputStream(output)
                        val `is` = connection.inputStream
                        buffer = ByteArray(1024)
                        total = 0
                        while (`is`.read(buffer).also { len1 = it } > 0) {
                            total += len1.toLong()
                            var progress = 0
                            if (size != 0) progress = (total * 100 / size).toInt()
                            publishProgress(progress, 100)
                            fos.write(buffer, 0, len1)
                        }
                        connection.disconnect()
                        fos.close()
                        `is`.close()
                    }
                    purifyneed.remove("Language")
                    fileneed.remove("Language")
                    StaticStore.unitlang = 1
                    StaticStore.enemeylang = 1
                    StaticStore.stagelang = 1
                    StaticStore.maplang = 1
                }
            } catch (e: IOException) {
                output = null
                e.printStackTrace()
            } finally {
                if (output != null) {
                    fileneed.remove("Language")
                    purifyneed.remove("Language")
                }
            }
        }
        return null
    }

   override fun onProgressUpdate(vararg values: Int?) {
        val activity = weakActivity.get() ?: return
        val prog = activity.findViewById<ProgressBar>(R.id.downprog)
        val state = activity.findViewById<TextView>(R.id.downstate)

        if (prog.isIndeterminate) {
            prog.isIndeterminate = false
        } else if(values[1] == 100) {
            prog.isIndeterminate = true
        }

        if(values[0] == -1) {
            prog.max = values[1] ?: 100
            return
        }

        prog.progress = values[0] ?: 0
        if (values[1] != 100 && values[1] != 50) {
            val t = activity.getString(R.string.down_state_doing) + purifyneed[values[1] ?: 0]
            state.text = t
        } else if (values[1] == 50) {
            val t = activity.getString(R.string.down_state_music) + musics[values[2] ?: 0]
            state.text = t
        } else {
            val t = activity.getString(R.string.down_state_doing) + "Language Files"
            state.text = t
        }
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakActivity.get() ?: return
        val prog = activity.findViewById<ProgressBar>(R.id.downprog)
        val state = activity.findViewById<TextView>(R.id.downstate)
        val retry = activity.findViewById<Button>(R.id.retry)
        val results = ArrayList<String>()
        if (remover.size < purifyneed.size) {
            for (i in 0 until purifyneed.size - remover.size) {
                remover.add(true)
            }
        }
        for (i in remover.indices) {
            if (remover[i]) {
                results.add(fileneed[i])
            }
        }
        purifyneed = results

        if (prog.isIndeterminate) {
            prog.isIndeterminate = false
            prog.progress = 100
        }

        if (purifyneed.isNotEmpty() || output == null) {
            if (purifyneed.isEmpty()) {
                state.setText(R.string.down_state_ok)

                val intent = Intent(activity, CheckUpdateScreen::class.java)
                StaticStore.clear()
                activity.startActivity(intent)
                activity.finish()
            } else {
                state.setText(R.string.down_state_no)
                retry.visibility = View.VISIBLE
            }
        } else {
            state.setText(R.string.down_state_ok)

            val intent = Intent(activity, CheckUpdateScreen::class.java)
            StaticStore.clear()
            activity.startActivity(intent)
            activity.finish()
        }
    }

    private fun purify(size: Map<String, Long>?) {
        val activity = weakActivity.get() ?: return

        val assetPath = StaticStore.getExternalAsset(activity)

        if (size == null || size.isEmpty()) {
            purifyneed.addAll(fileneed)
            return
        }

        purifyneed = ArrayList()

        val result = ArrayList<Int>()

        for (i in fileneed.indices) {
            val f = File(assetPath, fileneed[i] + ".zip")
            if (f.exists()) {
                if (f.length() != size[fileneed[i]]) {
                    result.add(i)
                }
            } else {
                result.add(i)
            }
        }
        if (result.isNotEmpty()) {
            for (i in result.indices) {
                purifyneed.add(fileneed[result[i]])
            }
        }
    }

}