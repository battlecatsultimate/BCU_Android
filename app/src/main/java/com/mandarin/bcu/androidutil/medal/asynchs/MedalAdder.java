package com.mandarin.bcu.androidutil.medal.asynchs;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.Display;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.io.ErrorLogWriter;
import com.mandarin.bcu.androidutil.medal.MDefiner;
import com.mandarin.bcu.androidutil.medal.adapters.MedalListAdapter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MedalAdder extends AsyncTask<Void, Integer, Void> {
    private WeakReference<Activity> weakReference;

    public MedalAdder(Activity activity) {
        weakReference = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        if (activity == null) return;

        ListView listView = activity.findViewById(R.id.medallist);

        setDisappear(listView);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        if (activity == null) return null;

        new MDefiner().define();

        publishProgress(0);

        if (StaticStore.medals == null) {
            StaticStore.medals = new ArrayList<>();

            for (int i = 0; i < StaticStore.medalnumber; i++) {
                String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/page/medal/";

                String name = "medal_" + number(i) + ".png";

                File f = new File(path, name);

                if (!f.exists()) {
                    continue;
                }

                try {
                    Bitmap b = BitmapFactory.decodeFile(path + name);

                    StaticStore.medals.add(b);
                } catch (Exception e) {
                    ErrorLogWriter.WriteLog(e);
                }
            }
        }

        publishProgress(1);

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Activity activity = weakReference.get();

        if (activity == null) return;

        TextView mlistst = activity.findViewById(R.id.medalloadt);

        switch (values[0]) {
            case 0:
                mlistst.setText(R.string.medal_reading_icon);
                break;
            case 1:
                mlistst.setText(R.string.medal_loading_data);

                ListView medallist = activity.findViewById(R.id.medallist);

                Display display = activity.getWindowManager().getDefaultDisplay();
                Point p = new Point();

                display.getSize(p);

                int width = p.x;

                float wh;

                if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    wh = 72f;
                else
                    wh = 90f;

                int num = (width - StaticStore.dptopx(16f, activity)) / StaticStore.dptopx(wh, activity);
                int line = StaticStore.medalnumber / num;

                if (StaticStore.medalnumber % num != 0)
                    line++;

                String[] lines = new String[line];

                MedalListAdapter adapter = new MedalListAdapter(activity, num, width, wh, lines);

                medallist.setAdapter(adapter);
                medallist.setClickable(false);

                break;
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        if (activity == null) return;

        ListView listView = activity.findViewById(R.id.medallist);
        ProgressBar prog = activity.findViewById(R.id.medalprog);
        TextView medalt = activity.findViewById(R.id.medalloadt);

        setAppear(listView);
        setDisappear(prog, medalt);
    }

    private void setDisappear(View... views) {
        for (View v : views) {
            v.setVisibility(View.GONE);
        }
    }

    private void setAppear(View... views) {
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

    private String number(int num) {
        if (0 <= num && num < 10) {
            return "00" + num;
        } else if (10 <= num && num <= 99) {
            return "0" + num;
        } else {
            return String.valueOf(num);
        }
    }
}
