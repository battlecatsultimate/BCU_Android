package com.mandarin.bcu.androidutil.asynchs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mandarin.bcu.BuildConfig;
import com.mandarin.bcu.R;
import com.mandarin.bcu.util.Interpret;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadApk extends AsyncTask<Void,Integer,Void> {
    private final Context context;
    private final String ver;
    private final String url;
    private final String path;
    private final String realpath;
    private File output = null;

    public DownloadApk(Context context,String ver,String url,String path,String realpath) {
        this.context = context;
        this.ver = ver;
        this.url = url;
        this.path = path;
        this.realpath =realpath;
    }

    @Override
    protected void onPreExecute() {
        TextView state = ((Activity)context).findViewById(R.id.apkstate);
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
            Long total = (long)0;

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
        ProgressBar prog = ((Activity)context).findViewById(R.id.apkprog);
        TextView state = ((Activity)context).findViewById(R.id.apkstate);
        String name = context.getString(R.string.down_state_doing)+"BCU_Android_"+ver+".apk";
        state.setText(name);

        if(prog.isIndeterminate()) {
            prog.setIndeterminate(false);
        }

        prog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Void result) {
        if(output == null) {
            Button retry = ((Activity)context).findViewById(R.id.apkretry);
            retry.setVisibility(View.VISIBLE);
        } else {
            File install = new File(realpath);
            Uri apkuri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID+".fileprovider",install);
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(apkuri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
            ((Activity)context).finish();
        }
    }
}
