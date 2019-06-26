package com.mandarin.bcu.androidutil.asynchs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.TextView;

import com.mandarin.bcu.ApkDownload;
import com.mandarin.bcu.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class CheckApk extends AsyncTask<Void,String,Void> {
    private final WeakReference<Activity> weakReference;
    private String thisver;
    private boolean cando;
    private String path;
    private boolean lang;
    private ArrayList<String> fileneed;
    private ArrayList<String> filenum;

    public CheckApk(String path, boolean lang, ArrayList<String> fileneed, ArrayList<String> filenum, Activity context, boolean cando) {
        this.weakReference = new WeakReference<>(context);
        this.path = path;
        this.lang = lang;
        this.fileneed = fileneed;
        this.filenum = filenum;
        this.cando = cando;
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();
        try{
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(),0);
            thisver = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView state = activity.findViewById(R.id.mainstup);
        state.setText(R.string.main_check_apk);

    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();
        try{
            JSONObject update = new JSONObject();
            String apklink = "http://battlecatsultimate.cf/api/java/getupdate.php";
            update.put("bcuver",thisver);
            URL apkurl = new URL(apklink);
            HttpURLConnection apkcon = (HttpURLConnection)apkurl.openConnection();
            apkcon.setDoInput(true);
            apkcon.setDoOutput(true);
            apkcon.setRequestMethod("POST");
            apkcon.connect();
            OutputStream os = apkcon.getOutputStream();
            os.write(update.toString().getBytes(StandardCharsets.UTF_8));
            os.close();

            InputStream in = apkcon.getInputStream();
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            int cp;
            while((cp = isr.read())!=-1) {
                sb.append((char)cp);
            }
            String result = sb.toString();
            JSONObject ans = new JSONObject(result);
            in.close();
            apkcon.disconnect();

            SharedPreferences shared = activity.getSharedPreferences("configuration",MODE_PRIVATE);
            String thatver;

            if(shared.getBoolean("apktest",false)) {
                thatver = ans.getString("android_test");
            } else {
                thatver = ans.getString("android_ver");
            }

            publishProgress(thatver);


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        goToApk(values[0]);
    }

    @Override
    protected void onPostExecute(Void results) {
    }

    private void goToApk(String ver) {
        Activity activity = weakReference.get();
        boolean update = false;

        String[] thisnum = thisver.split("\\.");
        String[] thatnum = ver.split("\\.");

        for(int i = 0;i < 3;i++) {
            int num1 = Integer.valueOf(thisnum[i]);
            int num2 = Integer.valueOf(thatnum[i]);

            if(num1 < num2) {
                update = true;
            }
        }

        if(update) {
            AlertDialog.Builder apkdon = new AlertDialog.Builder(activity);
            apkdon.setCancelable(false);
            apkdon.setTitle(R.string.apk_down_title);
            String content = activity.getString(R.string.apk_down_content)+ver;
            apkdon.setMessage(content);
            apkdon.setPositiveButton(R.string.main_file_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent result = new Intent(activity, ApkDownload.class);
                    result.putExtra("ver",ver);
                    activity.startActivity(result);
                    activity.finish();
                }
            });
            apkdon.setNegativeButton(R.string.main_file_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new CheckUpdates(path,lang,fileneed,filenum,activity,cando).execute();
                }
            });

            AlertDialog apkdown = apkdon.create();
            apkdown.show();
        } else {
            new CheckUpdates(path,lang,fileneed,filenum,activity,cando).execute();
        }
    }
}
