package com.mandarin.bcu.androidutil.io.asynchs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.core.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mandarin.bcu.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadApk extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakReference;
    private final String ver;
    private final String url;
    private final String path;
    private final String realpath;
    private File output = null;

    public DownloadApk(Activity context,String ver,String url,String path,String realpath) {
        this.weakReference = new WeakReference<>(context);
        this.ver = ver;
        this.url = url;
        this.path = path;
        this.realpath =realpath;
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        if(activity == null) return;

        TextView state = activity.findViewById(R.id.apkstate);

        if(state != null)
            state.setText(R.string.down_wait);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL realurl = new URL(url);
            HttpURLConnection c = (HttpURLConnection)realurl.openConnection();
            c.setRequestMethod("GET");
            c.connect();

            Long size = (long)c.getContentLength();

            output = new File(realpath);
            File pathes = new File(path);

            if(!pathes.exists()) {
                pathes.mkdirs();
            }

            if(!output.exists()) {
                output.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(output);
            InputStream is = c.getInputStream();

            byte [] buffer = new byte[1024];
            int len1;
            long total = (long)0;

            while((len1 = is.read(buffer))!= -1) {
                total += len1;
                int progress = (int)(total*100/size);
                publishProgress(progress);
                fos.write(buffer,0,len1);
            }

            c.disconnect();
            fos.close();
            is.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            output = null;
        } catch (IOException e) {
            e.printStackTrace();
            output = null;
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        ProgressBar prog = activity.findViewById(R.id.apkprog);
        TextView state = activity.findViewById(R.id.apkstate);
        String name = activity.getString(R.string.down_state_doing)+"BCU_Android_"+ver+".apk";

        if(state != null)
            state.setText(name);

        if(prog != null) {
            if (prog.isIndeterminate()) {
                prog.setIndeterminate(false);
            }

            prog.setProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        if(output == null) {
            Button retry = activity.findViewById(R.id.apkretry);

            if(retry != null)
                retry.setVisibility(View.VISIBLE);
        } else {
            File install = new File(realpath);
            Uri apkuri = FileProvider.getUriForFile(activity, "com.mandarin.bcu.provider",install);
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(apkuri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(intent);
            activity.finish();
        }
    }
}
