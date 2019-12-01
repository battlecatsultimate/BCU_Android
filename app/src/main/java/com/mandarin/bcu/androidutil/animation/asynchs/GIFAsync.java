package com.mandarin.bcu.androidutil.animation.asynchs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.AnimatedGifEncoder;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.animation.AnimationCView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GIFAsync extends AsyncTask<Void,Void,Void> {
    private final WeakReference<AnimationCView> weakReference;
    private final WeakReference<Activity> activityWeakReference;
    private int id = -1;
    private int form = -1;

    public boolean keepDoing = true;

    private boolean done = false;

    public GIFAsync(AnimationCView cView, Activity activity, int id, int form) {
        this.weakReference = new WeakReference<>(cView);
        this.activityWeakReference = new WeakReference<>(activity);
        this.id = id;
        this.form = form;
    }

    public GIFAsync(AnimationCView cView, Activity activity, int id) {
        this.weakReference = new WeakReference<>(cView);
        this.activityWeakReference = new WeakReference<>(activity);
        this.id = id;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity c = activityWeakReference.get();
        AnimationCView cView = weakReference.get();

        if(c == null || cView == null) return null;

        c.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        byte [] buffer = generateGIF();

        if(buffer == null) {
            StaticStore.frames.clear();
            StaticStore.gifFrame = 0;
            return null;
        }

        String path = Environment.getExternalStorageDirectory().getPath()+"/BCU/gif/";
        File f = new File(path);

        if(!f.exists())
            f.mkdirs();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date date = new Date();

        String name;

        if(id != -1) {
            if (form != -1) {
                name = dateFormat.format(date)+"-U-"+id+"-"+form + ".gif";
            } else {
                name = dateFormat.format(date)+"-E-"+id + ".gif";
            }
        } else {
            name = dateFormat.format(date)+".gif";
        }

        File g = new File(path,name);

        try {
            if(!g.exists())
                g.createNewFile();

            FileOutputStream fos = new FileOutputStream(g);

            fos.write(buffer);
            fos.close();

            done = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        StaticStore.frames.clear();

        return null;
    }

    @Override
    public void onPostExecute(Void result) {
        Activity c = activityWeakReference.get();
        AnimationCView cView = weakReference.get();

        if(c == null || cView == null) return;

        SharedPreferences shared = c.getSharedPreferences("configuration", Context.MODE_PRIVATE);

        if(shared.getInt("Orientation",0) == 1)
            c.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else if(shared.getInt("Orientation",0) == 2)
            c.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        else if(shared.getInt("Orientation",0) == 0)
            c.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        cView.gif.setVisibility(View.GONE);

        String path = Environment.getExternalStorageDirectory().getPath()+"/BCU/gif/";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date date = new Date();

        String name = dateFormat.format(date)+".gif";

        if(!keepDoing) {
            File f = new File(path,name);

            if(f.exists())
                f.delete();
        }

        if(done && keepDoing)
            Toast.makeText(c,c.getText(R.string.anim_png_success).toString().replace("-",path+name),Toast.LENGTH_SHORT).show();
        else if(!keepDoing)
            Toast.makeText(c,R.string.anim_gif_cancel,Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(c,R.string.anim_png_fail,Toast.LENGTH_SHORT).show();

        StaticStore.gifisSaving = false;
    }

    private byte[] generateGIF() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.setFrameRate(30);
        encoder.start(bos);
        for (Bitmap bitmap : StaticStore.frames) {
            if(keepDoing) {
                encoder.addFrame(bitmap);
                StaticStore.gifFrame--;
            } else {
                return null;
            }
        }
        encoder.finish();
        return bos.toByteArray();
    }
}
