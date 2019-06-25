package com.mandarin.bcu.androidutil.asynchs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mandarin.bcu.R;
import com.mandarin.bcu.UnitInfo;
import com.mandarin.bcu.androidutil.Definer;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.UnitListAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

import common.system.MultiLangCont;
import common.system.files.VFile;
import common.util.pack.Pack;
import common.util.unit.Form;

public class Adder extends AsyncTask<Void, Integer, Void> {
    private final int unitnumber;
    private final WeakReference<Activity> weakReference;

    public Adder(int unitnumber, Activity context) {
        this.unitnumber = unitnumber;
        this.weakReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();
        ListView list = activity.findViewById(R.id.unitinflist);

        list.setVisibility(View.GONE);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        new Definer().define(activity);

        if(StaticStore.names == null) {
            StaticStore.names = new String[unitnumber];

            for(int i = 0; i < StaticStore.names.length;i++) {
                StaticStore.names[i] = withID(i, MultiLangCont.FNAME.getCont(Pack.def.us.ulist.get(i).forms[0]));
            }
        }

        if(StaticStore.bitmaps == null) {
            StaticStore.bitmaps = new Bitmap[unitnumber];


            for (int i = 0; i < unitnumber; i++) {
                String shortPath = "./org/unit/"+ number(i) + "/f/uni" + number(i) + "_f00.png";

                StaticStore.bitmaps[i] = StaticStore.getResizeb((Bitmap)Objects.requireNonNull(VFile.getFile(shortPath)).getData().getImg().bimg(),activity,48f);

            }
        }

        publishProgress(0);

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Activity activity = weakReference.get();

        ListView list = activity.findViewById(R.id.unitinflist);
        ArrayList<Integer> locate = new ArrayList<>();
        for(int i = 0; i < unitnumber;i++) {
            locate.add(i);
        }
        UnitListAdapter adap = new UnitListAdapter(activity,StaticStore.names,StaticStore.bitmaps,locate);
        list.setAdapter(adap);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(activity,showName(locate.get(position)),Toast.LENGTH_SHORT).show();
                list.setClickable(false);

                return false;
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(SystemClock.elapsedRealtime() - StaticStore.unitinflistClick < StaticStore.INTERVAL)
                    return;

                StaticStore.unitinflistClick = SystemClock.elapsedRealtime();

                Intent result = new Intent(activity, UnitInfo.class);
                result.putExtra("ID",locate.get(position));
                activity.startActivity(result);
            }
        });
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        super.onPostExecute(result);
        ListView list = activity.findViewById(R.id.unitinflist);
        ProgressBar prog = activity.findViewById(R.id.unitinfprog);
        list.setVisibility(View.VISIBLE);
        prog.setVisibility(View.GONE);
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

    private String showName(int location) {
        ArrayList<String> names = new ArrayList<>();

        for(Form f : StaticStore.units.get(location).forms) {
            names.add(f.name);
        }

        StringBuilder result = new StringBuilder(withID(location, names.get(0)));

        for(int i = 1; i < names.size();i++) {
            result.append(" - ").append(names.get(i));
        }

        return result.toString();
    }

    private String withID(int id, String name) {
        String result;
        String names = name;

        if(names == null)
            names = "";

        if(names.equals("")) {
            result = number(id);
        } else {
            result = number(id)+" - "+names;
        }

        return result;
    }
}
