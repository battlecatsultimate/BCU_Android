package com.mandarin.bcu.androidutil.io.drive;

import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.mandarin.bcu.androidutil.StaticStore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class DriveUtil {
    public static boolean Upload(java.io.File file, InputStream in) throws IOException {
        GoogleCredential credential = GoogleCredential.fromStream(in).createScoped(Collections.singletonList(DriveScopes.DRIVE));

        HttpTransport transport = new NetHttpTransport();
        JsonFactory factory = JacksonFactory.getDefaultInstance();
        Drive service = new com.google.api.services.drive.Drive.Builder(transport,factory,credential).setApplicationName("BCU").build();

        Log.i("uploadDrive","-*****- Uploading Log File to Drive -> File Name : "+file.getName()+" | File Size : "+file.length()+" -*****-");

        File target = new File();

        FileContent con = new FileContent("text/txt",file);

        target.setName(file.getName()).setMimeType(con.getType())
                .setParents(Collections.singletonList(StaticStore.ERR_FILE_ID));

        service.files().create(target,con).execute();

        return true;
    }
}
