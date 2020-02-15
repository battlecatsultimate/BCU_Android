package com.mandarin.bcu.androidutil.io

import android.os.Build
import android.os.Environment
import com.mandarin.bcu.androidutil.StaticStore
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class ErrorLogWriter(private val path: String?, private val upload: Boolean) : Thread.UncaughtExceptionHandler {
    private val errors: Thread.UncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    override fun uncaughtException(t: Thread, e: Throwable) {
        if (path != null) {
            writeToFile(e)
        }
        errors.uncaughtException(t, e)
    }

    private fun writeToFile(e: Throwable) {
        try {
            val f = File(path)
            if (!f.exists()) {
                f.mkdirs()
            }
            val fe = File(Environment.getDataDirectory().toString() + "/data/com.mandarin.bcu/upload")
            if (!fe.exists()) fe.mkdirs()
            val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
            val date = Date()
            var name = dateFormat.format(date)
            val stringbuff: Writer = StringWriter()
            val printWriter = PrintWriter(stringbuff)
            e.printStackTrace(printWriter)
            val current = stringbuff.toString()
            printWriter.close()
            if (upload) {
                val dname = name + "_" + Build.MODEL + ".txt"
                val df = File(Environment.getDataDirectory().toString() + "/data/com.mandarin.bcu/upload/", dname)
                val dfileWriter = FileWriter(df)
                dfileWriter.append("VERSION : ").append(StaticStore.VER).append("\r\n")
                dfileWriter.append("MODEL : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL.toString()).append("\r\n")
                dfileWriter.append("IS EMULATOR : ").append((Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK")).toString()).append("\r\n")
                dfileWriter.append("ANDROID_VER : ").append("API ").append(Build.VERSION.SDK_INT.toString()).append(" (").append(Build.VERSION.RELEASE).append(")").append("\r\n").append("\r\n")
                dfileWriter.append(current)
                dfileWriter.flush()
                dfileWriter.close()
            }
            name += ".txt"
            val file = File(path, name)
            if (!file.exists()) f.createNewFile()
            val fileWriter = FileWriter(file)
            fileWriter.append(current)
            fileWriter.flush()
            fileWriter.close()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
    }

    companion object {
        fun writeLog(error: Exception, upload: Boolean) {
            try {
                val path = Environment.getExternalStorageDirectory().path + "/BCU/logs/"
                val f = File(path)
                if (!f.exists()) {
                    f.mkdirs()
                }
                val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                val date = Date()
                val name = dateFormat.format(date) + ".txt"
                var file = File(path, name)
                val stringbuff: Writer = StringWriter()
                val printWriter = PrintWriter(stringbuff)
                error.printStackTrace(printWriter)
                if (!file.exists()) file.createNewFile() else {
                    file = File(path, getExistingFileName(path, name))
                    file.createNewFile()
                }
                if (upload) {
                    val df = File(Environment.getDataDirectory().toString() + "/data/com.mandarin.bcu/upload/", name)
                    if (!df.exists()) df.createNewFile()
                    val dfileWriter = FileWriter(df)
                    dfileWriter.append("VERSION : ").append(StaticStore.VER).append("\r\n")
                    dfileWriter.append("MODEL : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL.toString()).append("\r\n")
                    dfileWriter.append("IS EMULATOR : ").append((Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK")).toString()).append("\r\n")
                    dfileWriter.append("ANDROID_VER : ").append("API ").append(Build.VERSION.SDK_INT.toString()).append(" (").append(Build.VERSION.RELEASE).append(")").append("\r\n").append("\r\n")
                    dfileWriter.append(stringbuff.toString())
                    dfileWriter.flush()
                    dfileWriter.close()
                }
                val fileWriter = FileWriter(file)
                fileWriter.append(stringbuff.toString())
                fileWriter.flush()
                fileWriter.close()
                printWriter.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun getExistingFileName(path: String, name: String): String {
            var decided = false
            var exist = 1
            var nam = "$name-$exist"
            while (!decided) {
                val f = File(path, nam)
                nam = if (!f.exists()) return nam else {
                    exist++
                    "$name-$exist"
                }
                decided = true
            }
            return nam
        }

        @Throws(IOException::class)
        fun upload(file: File): Boolean {
            if (!file.exists()) return false
            val crlf = "\r\n"
            val hyphens = "--"
            val bound = "*****"
            val fileInputStream = FileInputStream(file)
            val u = URL("https://battle-cats-ultimate.000webhostapp.com/api/java/alogio.php")
            val con = u.openConnection() as HttpURLConnection
            con.doInput = true
            con.doOutput = true
            con.useCaches = false
            con.requestMethod = "POST"
            con.setRequestProperty("Connection", "Keep-Alive")
            con.setRequestProperty("ENCTYPE", "multipart/form-data")
            con.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****")
            con.setRequestProperty("uploaded_file", file.name)
            val dos = DataOutputStream(con.outputStream)
            val buffer: ByteArray
            var available: Int
            var read: Int
            var size: Int
            val max = 1024 * 1024
            dos.writeBytes(hyphens + bound + crlf)
            dos.writeBytes("Content-Disposition: form-data; name=\"" + "catFile" + "\";filename=\"" + file.name + "\"" + crlf)
            dos.writeBytes(crlf)
            available = fileInputStream.available()
            size = available.coerceAtMost(max)
            buffer = ByteArray(size)
            read = fileInputStream.read(buffer, 0, size)
            while (read > 0) {
                dos.write(buffer, 0, size)
                available = fileInputStream.available()
                size = available.coerceAtMost(size)
                read = fileInputStream.read(buffer, 0, size)
            }
            dos.writeBytes(crlf)
            dos.writeBytes(hyphens + bound + crlf)
            val response = con.responseCode
            dos.flush()
            dos.close()
            fileInputStream.close()
            con.disconnect()
            return response == 200
        }
    }

}