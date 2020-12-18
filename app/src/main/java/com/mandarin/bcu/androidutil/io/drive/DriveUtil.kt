package com.mandarin.bcu.androidutil.io.drive

import android.util.Log
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.mandarin.bcu.androidutil.StaticStore
import java.io.File
import java.io.IOException
import java.io.InputStream

object DriveUtil {
    @Throws(IOException::class)
    fun upload(file: File, `in`: InputStream?): Boolean {
        val credential = GoogleCredentials.fromStream(`in`).createScoped(listOf(DriveScopes.DRIVE))
        val request = HttpCredentialsAdapter(credential)
        val transport: HttpTransport = NetHttpTransport()
        val factory: JsonFactory = JacksonFactory.getDefaultInstance()
        val service = Drive.Builder(transport, factory, request).setApplicationName("BCU").build()
        Log.i("uploadDrive", "-*****- Uploading Log File to Drive -> File Name : " + file.name + " | File Size : " + file.length() + " -*****-")
        val target = com.google.api.services.drive.model.File()
        val con = FileContent("text/txt", file)
        target.setName(file.name).setMimeType(con.type).parents = listOf(StaticStore.ERR_FILE_ID)
        service.files().create(target, con).execute()
        return true
    }
}