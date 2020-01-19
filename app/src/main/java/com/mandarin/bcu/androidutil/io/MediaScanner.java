package com.mandarin.bcu.androidutil.io;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;

public class MediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    private MediaScannerConnection connection;
    private File file;

    public MediaScanner(Context context, File f) {
        this.file = f;
        connection = new MediaScannerConnection(context, this);
        connection.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        connection.scanFile(file.getAbsolutePath(), null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        connection.disconnect();
    }
}
