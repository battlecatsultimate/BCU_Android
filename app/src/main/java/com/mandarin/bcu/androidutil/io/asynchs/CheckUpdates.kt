package com.mandarin.bcu.androidutil.io.asynchs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Environment
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.mandarin.bcu.DownloadScreen
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.io.Reader
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

class CheckUpdates : AsyncTask<Void?, Int?, Void?> {
    private val path: String
    private val cando: Boolean
    private val weakReference: WeakReference<Activity?>
    private var lang: Boolean
    private var music = false
    private val lan = arrayOf("/en/", "/jp/", "/kr/", "/zh/")
    private var source: String
    private var fileneed: ArrayList<String>
    private var filenum: ArrayList<String>
    private var contin = true
    private var config = false
    private var ans: JSONObject? = null

    internal constructor(path: String, lang: Boolean, fileneed: ArrayList<String>, filenum: ArrayList<String>, context: Activity?, cando: Boolean) {
        this.path = path
        this.lang = lang
        this.fileneed = fileneed
        this.filenum = filenum
        weakReference = WeakReference(context)
        this.cando = cando
        source = "$path/lang"
    }

    internal constructor(path: String, lang: Boolean, fileneed: ArrayList<String>, filenum: ArrayList<String>, context: Activity?, cando: Boolean, config: Boolean) {
        this.path = path
        this.lang = lang
        this.fileneed = fileneed
        this.filenum = filenum
        weakReference = WeakReference(context)
        this.cando = cando
        this.config = config
        source = "$path/lang"
    }

    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        val checkstate = activity.findViewById<TextView>(R.id.mainstup)
        checkstate?.setText(R.string.main_check_up)
    }

    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        var output: File
        try {
            val assetlink = "https://raw.githubusercontent.com/battlecatsultimate/bcu-page/master/api/getUpdate.json"
            val asseturl = URL(assetlink)
            val `is` = asseturl.openStream()
            val isr = InputStreamReader(`is`, StandardCharsets.UTF_8)
            val result = readAll(BufferedReader(isr))
            ans = JSONObject(result)
            `is`.close()

            val difffile = "Difficulty.txt"
            val diff = File("$source/", difffile)
            if (!diff.exists()) lang = true
            for (s1 in lan) {
                if (lang) continue
                for (s in StaticStore.langfile) {
                    if (lang) continue
                    val f = File(source + s1, s)
                    if (!f.exists()) {
                        lang = true
                    }
                }
            }
            val shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
            if ((!shared.getBoolean("Skip_Text", false) || config) && !lang) {
                val url = "https://raw.githubusercontent.com/battlecatsultimate/bcu-resources/master/resources/lang"
                val durl = "$url/$difffile"
                val dlink = URL(durl)
                val dc = dlink.openConnection() as HttpURLConnection
                dc.requestMethod = "GET"
                dc.connect()
                val durlis = dc.inputStream
                val dbuf = ByteArray(1024)
                var dlen: Int
                var dsize = 0
                while (durlis.read(dbuf).also { dlen = it } != -1) {
                    dsize += dlen
                }
                output = File("$source/", difffile)
                if (output.exists()) {
                    if (output.length() != dsize.toLong()) {
                        lang = true
                    }
                } else {
                    lang = true
                }
                dc.disconnect()
                durlis.close()
                for (s1 in lan) {
                    if (lang) continue
                    for (s in StaticStore.langfile) {
                        if (lang) continue
                        val langurl = url + s1 + s
                        val link = URL(langurl)
                        val c = link.openConnection() as HttpURLConnection
                        c.requestMethod = "GET"
                        c.connect()
                        val urlis = c.inputStream
                        val buf = ByteArray(1024)
                        var len1: Int
                        var size = 0
                        while (urlis.read(buf).also { len1 = it } != -1) {
                            size += len1
                        }
                        output = File(source + s1, s)
                        if (output.exists()) {
                            if (output.length() != size.toLong()) {
                                lang = true
                                break
                            }
                        } else {
                            lang = true
                            break
                        }
                        c.disconnect()
                        durlis.close()
                    }
                    if (lang) {
                        break
                    }
                }
            }
        } catch (e: ProtocolException) {
            e.printStackTrace()
            contin = false
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            contin = false
        } catch (e: IOException) {
            e.printStackTrace()
            contin = false
        } catch (e: JSONException) {
            e.printStackTrace()
            contin = false
        }
        publishProgress(1)
        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val activity = weakReference.get() ?: return
        val checkstate = activity.findViewById<TextView>(R.id.mainstup)
        if (values[0] == 1) {
            checkstate?.setText(R.string.main_check_file)
        }
    }

    override fun onPostExecute(result: Void?) {
        if (contin || cando) {
            val activity = weakReference.get() ?: return
            val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager.activeNetworkInfo != null) checkFiles(ans)
            if (fileneed.isEmpty() && filenum.isEmpty()) {
                AddPathes(activity, config).execute()
            }
        } else {
            CheckUpdates(path, lang, fileneed, filenum, weakReference.get(), false).execute()
        }
    }

    private fun readAll(rd: java.io.Reader): String? {
        return try {
            val sb = StringBuilder()
            var chara: Int
            while (rd.read().also { chara = it } != -1) {
                sb.append(chara.toChar())
            }
            sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun checkFiles(asset: JSONObject?) {
        val activity = weakReference.get() ?: return
        try {
            val libmap = ArrayList<String>()
            if (asset == null) return
            val ja = asset.getJSONArray("android")
            val mus = asset.getInt("music")
            val musicPath = Environment.getExternalStorageDirectory().absolutePath + "/Android/data/com.mandarin.bcu/music/"
            val musics = ArrayList<String>()
            for (i in 0 until mus) {
                if (music) continue
                val name = number(i) + ".ogg"
                val mf = File(musicPath, name)
                if (!mf.exists()) {
                    music = true
                    for (j in i until mus) {
                        musics.add(number(j) + ".ogg")
                    }
                }
            }
            println(musics)
            for (i in 0 until ja.length()) {
                libmap.add(ja.getString(i))
            }
            println(libmap.toString())
            val donloader = AlertDialog.Builder(activity)
            val intent = Intent(activity, DownloadScreen::class.java)
            donloader.setTitle(R.string.main_file_need)
            donloader.setMessage(R.string.main_file_up)
            donloader.setPositiveButton(R.string.main_file_ok) { _, _ ->
                if (lang && !fileneed.contains("Language")) {
                    fileneed.add("Language")
                    filenum.add(filenum.size.toString())
                }
                if (music && !fileneed.contains("Music")) {
                    fileneed.add("Music")
                    filenum.add("Music")
                }
                println(fileneed.toString())
                intent.putExtra("fileneed", fileneed)
                intent.putExtra("filenum", filenum)
                intent.putExtra("music", musics)
                activity.startActivity(intent)
                activity.finish()
            }
            donloader.setNegativeButton(R.string.main_file_cancel) { _, _ -> if (!cando || lang || music) activity.finish() else AddPathes(activity, config).execute() }
            donloader.setCancelable(false)
            try {
                val libs = Reader.getInfo(path)
                if (libs != null && libs.isEmpty()) {
                    for (i in libmap.indices) {
                        fileneed.add(libmap[i])
                        filenum.add(i.toString())
                    }
                    val downloader = donloader.create()
                    downloader.show()
                } else {
                    for (i in libmap.indices) {
                        if (!(libs != null && libs.contains(libmap[i]))) {
                            fileneed.add(libmap[i])
                            filenum.add(i.toString())
                        }
                    }
                    when {
                        filenum.isNotEmpty() -> {
                            donloader.setTitle(R.string.main_file_x)
                            val downloader = donloader.create()
                            downloader.show()
                        }
                        lang -> {
                            fileneed.add("Language")
                            filenum.add(filenum.size.toString())
                            donloader.setTitle(R.string.main_file_x)
                            val downloader = donloader.create()
                            downloader.show()
                        }
                        music -> {
                            fileneed.add("Music")
                            filenum.add("Music")
                            donloader.setTitle(R.string.main_file_x)
                            val downloader = donloader.create()
                            downloader.show()
                        }
                    }
                }
            } catch (e: Exception) {
                var i = 0
                while (i < libmap.size) {
                    fileneed.add(libmap[i])
                    filenum.add(i.toString())
                    i++
                }
                donloader.setTitle(R.string.main_info_corr)
                donloader.setMessage(R.string.main_info_cont)
                val downloader = donloader.create()
                downloader.show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun number(n: Int): String {
        return when (n) {
            in 0..9 -> {
                "00$n"
            }
            in 10..99 -> {
                "0$n"
            }
            else -> {
                n.toString()
            }
        }
    }
}