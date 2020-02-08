package com.mandarin.bcu.androidutil.io

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File

object MediaScanner {
    fun scan(c: Context, f: File) {
        val uri = Uri.fromFile(f)
        val mediaIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaIntent.data = uri
        c.sendBroadcast(mediaIntent)
    }
}