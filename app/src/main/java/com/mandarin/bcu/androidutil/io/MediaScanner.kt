package com.mandarin.bcu.androidutil.io

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object MediaScanner {
    const val ERRR_WRONG_SDK = "Wrong_SDK_INT"

    fun putImage(c: Context, b: Bitmap, name: String) : String {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            putImageQ(c,b,name)
        } else {
            putImageP(c, b, name)
        }
    }

    private fun putImageQ(c: Context, b: Bitmap, name: String) : String {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Log.e("Bitmap Extracting", "MediaScanner.putImageQ requires API level Q or higher!")
            return ERRR_WRONG_SDK
        }

        val path = Environment.DIRECTORY_PICTURES + "/BCU Image"

        val contents = ContentValues().apply {
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME, "$name.png")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, path)
        }

        val resolver = c.contentResolver

        val contenturi = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(contenturi, contents) ?: throw IOException("Failed to create new MediaStore record")

        val stream = resolver.openOutputStream(uri) ?: throw IOException("Failed to get output stream from uri")

        if(!b.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
            throw IOException("Failed to save bitmap")
        }

        stream.close()

        return "$path/$name.png"
    }

    @Suppress("DEPRECATION")
    private fun putImageP(c: Context, b: Bitmap, name: String) : String {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.e("Bitmap Extracting", "MediaScanner.putImageP requires API level P or lower!")
            return ERRR_WRONG_SDK
        }

        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()+"/BCU Image"

        val g = File(dir)

        if(!g.exists())
            g.mkdirs()

        val f = File(dir, "$name.png")

        if(!f.exists())
            f.createNewFile()

        val fos = FileOutputStream(f)

        b.compress(Bitmap.CompressFormat.PNG, 100, fos)

        val uri = Uri.fromFile(f)
        val mediaIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaIntent.data = uri
        c.sendBroadcast(mediaIntent)

        return f.absolutePath
    }

    fun writeGIF(c: Context, buffer: ByteArray, name: String) : String {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeGIFQ(c,buffer, name)
        } else {
            writeGIFP(c, buffer, name)
        }
    }

    private fun writeGIFQ(c: Context, buffer: ByteArray, name: String) : String {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Log.e("Bitmap Extracting", "MediaScanner.putImageQ requires API level Q or higher!")
            return ERRR_WRONG_SDK
        }

        val relativePath = Environment.DIRECTORY_PICTURES + "/BCU Image"

        val contents = ContentValues().apply {
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME, "$name.gif")
            put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/gif")
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }

        val resolver = c.contentResolver

        val contenturi = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(contenturi, contents) ?: throw IOException("Failed to create new MediaStore")

        val stream = resolver.openOutputStream(uri) ?: throw IOException("Failed to get output stream from uri")

        stream.write(buffer)

        stream.close()

        return "$relativePath/$name.gif"
    }

    @Suppress("DEPRECATION")
    private fun writeGIFP(c: Context, buffer: ByteArray, name: String) : String {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.e("Bitmap Extracting", "MediaScanner.putImageP requires API level P or lower!")
            return ERRR_WRONG_SDK
        }

        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()+"/BCU Image"

        val d = File(dir)

        if(!d.exists())
            d.mkdirs()

        val g = File(dir, "$name.gif")

        if (!g.exists())
            g.createNewFile()

        val fos = FileOutputStream(g)

        fos.write(buffer)
        fos.close()

        val uri = Uri.fromFile(g)
        val mediaIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaIntent.data = uri
        c.sendBroadcast(mediaIntent)

        return g.absolutePath
    }
}