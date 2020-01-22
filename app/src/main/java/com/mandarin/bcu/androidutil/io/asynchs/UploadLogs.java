package com.mandarin.bcu.androidutil.io.asynchs;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.io.drive.DriveUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class UploadLogs extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakReference;

    public UploadLogs(Activity activity) {
        this.weakReference = new WeakReference<>(activity);
    }

    private int total;
    private int succeed = 0;
    private int failed = 0;

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        if(activity == null) return null;

        String path = Environment.getDataDirectory().getAbsolutePath()+"/data/com.mandarin.bcu/upload/";

        File upload = new File(path);

        File[] files = upload.listFiles();

        total = upload.listFiles().length;

        for(int i = 0; i < total; i++) {
            File f = files[i];

            publishProgress(0,i+1);

            try {
                if(safeCheck(f)) {
                    InputStream in = activity.getResources().openRawResource(R.raw.service_key);

                    boolean good = DriveUtil.Upload(f,in);

                    if (good) {
                        f.delete();
                        succeed++;
                    } else {
                        Log.e("uploadFailed", "Uploading " + f.getName() + " to server failed");
                        failed++;
                    }
                } else {
                    f.delete();
                    failed++;
                }
            } catch (IOException e) {
                e.printStackTrace();
                failed++;
            }
        }

        publishProgress(1);

        return null;
    }

    @Override
    public void onProgressUpdate(Integer... result) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        if(result[0] == 0) {
            String str = activity.getString(R.string.err_send_log).replace("-",String.valueOf(result[1])).replace("_",String.valueOf(total));
            Toast.makeText(activity,str,Toast.LENGTH_SHORT).show();
        } else {
            String str = activity.getString(R.string.err_send_result).replace("-",String.valueOf(succeed)).replace("_",String.valueOf(failed));
            Toast.makeText(activity,str,Toast.LENGTH_SHORT).show();
        }
    }

    private boolean safeCheck(File f) {
        String name = f.getName();

        if(!name.endsWith("txt")) return false;

        long size = f.length();

        long mb = size/1024/1024;

        return mb <= 10;
    }
}
