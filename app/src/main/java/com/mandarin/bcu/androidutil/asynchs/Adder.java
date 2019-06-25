package com.mandarin.bcu.androidutil.asynchs;

import android.app.Activity;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.Objects;

import common.system.MultiLangCont;
import common.system.files.VFile;
import common.util.pack.Pack;
import common.util.unit.Form;

public class Adder extends AsyncTask<Void, Integer, Void> {
    private final int unitnumber;
    private final long INTERVAL = 1000;
    private final Context context;

    public Adder(int unitnumber, Context context) {
        this.unitnumber = unitnumber;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        ListView list = ((Activity)context).findViewById(R.id.unitinflist);

        list.setVisibility(View.GONE);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        new Definer().define(context);

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

                StaticStore.bitmaps[i] = StaticStore.getResizeb((Bitmap)Objects.requireNonNull(VFile.getFile(shortPath)).getData().getImg().bimg(),context,48f);

            }
        }

        publishProgress(0);

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        ListView list = ((Activity)context).findViewById(R.id.unitinflist);
        ArrayList<Integer> locate = new ArrayList<>();
        for(int i = 0; i < unitnumber;i++) {
            locate.add(i);
        }
        UnitListAdapter adap = new UnitListAdapter((Activity)context,StaticStore.names,StaticStore.bitmaps,locate);
        list.setAdapter(adap);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(context,showName(locate.get(position)),Toast.LENGTH_SHORT).show();
                list.setClickable(false);

                return false;
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(SystemClock.elapsedRealtime() - StaticStore.unitinflistClick < INTERVAL)
                    return;

                StaticStore.unitinflistClick = SystemClock.elapsedRealtime();

                Intent result = new Intent(context, UnitInfo.class);
                result.putExtra("ID",locate.get(position));
                context.startActivity(result);
            }
        });
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        ListView list = ((Activity)context).findViewById(R.id.unitinflist);
        ProgressBar prog = ((Activity)context).findViewById(R.id.unitinfprog);
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
