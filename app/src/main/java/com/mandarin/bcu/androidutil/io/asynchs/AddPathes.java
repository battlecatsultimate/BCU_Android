package com.mandarin.bcu.androidutil.io.asynchs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.TextView;

import com.mandarin.bcu.MainActivity;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.io.DefineItf;

import java.lang.ref.WeakReference;

public class AddPathes extends AsyncTask<Void, Integer, Void> {
    private final WeakReference<Activity> weakReference;
    private final boolean config;

    AddPathes(Activity activity, boolean config) {
        this.weakReference = new WeakReference<>(activity);
        this.config = config;
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        if (activity == null) return;

        TextView checkstate = activity.findViewById(R.id.mainstup);

        if (checkstate != null)
            checkstate.setText(R.string.main_file_read);
    }


    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        if (activity == null) return null;

        SharedPreferences shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);
        com.mandarin.bcu.decode.ZipLib.init();
        com.mandarin.bcu.decode.ZipLib.read();

        StaticStore.getUnitnumber();
        StaticStore.getEnemynumber();
        StaticStore.root = 1;

        new DefineItf().init();

        StaticStore.getLang(shared.getInt("Language", 0));

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        if (activity == null) return;

        if (!MainActivity.isRunning) {
            Intent intent = new Intent(activity, MainActivity.class);
            intent.putExtra("config",config);
            activity.startActivity(intent);
            activity.finish();
        }
    }
}
