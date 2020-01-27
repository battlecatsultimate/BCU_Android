package com.mandarin.bcu.androidutil.io

import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import java.io.File

class MediaScanner(context: Context?, private val file: File) : MediaScannerConnectionClient {
    private val connection: MediaScannerConnection = MediaScannerConnection(context, this)
    override fun onMediaScannerConnected() {
        connection.scanFile(file.absolutePath, null)
    }

    override fun onScanCompleted(path: String, uri: Uri) {
        connection.disconnect()
    }

    init {
        connection.connect()
    }
}