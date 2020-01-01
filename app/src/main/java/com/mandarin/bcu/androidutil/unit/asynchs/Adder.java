package com.mandarin.bcu.androidutil.unit.asynchs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.R;
import com.mandarin.bcu.UnitInfo;
import com.mandarin.bcu.androidutil.FilterEntity;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.unit.Definer;
import com.mandarin.bcu.androidutil.unit.adapters.UnitListAdapter;

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

        if(activity == null) return;

        ListView list = activity.findViewById(R.id.unitinflist);
        FloatingActionButton search = activity.findViewById(R.id.animsch);

        list.setVisibility(View.GONE);
        search.hide();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        if(activity == null) return null;

        new Definer().define(activity);

        publishProgress(0);

        if (StaticStore.names == null) {
            StaticStore.names = new String[StaticStore.unitnumber];

            for (int i = 0; i < StaticStore.names.length; i++) {
                StaticStore.names[i] = withID(i, MultiLangCont.FNAME.getCont(Pack.def.us.ulist.get(i).forms[0]));
            }
        }

        publishProgress(1);

        if (StaticStore.bitmaps == null) {
            StaticStore.bitmaps = new Bitmap[StaticStore.unitnumber];


            for (int i = 0; i < unitnumber; i++) {
                String shortPath = "./org/unit/" + number(i) + "/f/uni" + number(i) + "_f00.png";

                Bitmap b = (Bitmap) Objects.requireNonNull(VFile.getFile(shortPath)).getData().getImg().bimg();

                if(b.getWidth() == b.getHeight())
                    StaticStore.bitmaps[i] = StaticStore.getResizeb(b, activity, 48f);
                else
                    StaticStore.bitmaps[i] = StaticStore.MakeIcon(activity,b,48f);

            }
        }

        publishProgress(2);

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        TextView ulistst = activity.findViewById(R.id.unitinfst);

        switch(values[0]) {
            case 0:
                ulistst.setText(R.string.unit_list_unitname);
                break;
            case 1:
                ulistst.setText(R.string.unit_list_unitic);
                break;
            case 2:
                ListView list = activity.findViewById(R.id.unitinflist);
                FilterEntity filterEntity = new FilterEntity(unitnumber);
                ArrayList<Integer> numbers = filterEntity.setFilter();
                ArrayList<String> names = new ArrayList<>();

                for (int i : numbers) {
                    names.add(StaticStore.names[i]);
                }
                UnitListAdapter adap = new UnitListAdapter(activity, names.toArray(new String[0]), StaticStore.bitmaps, numbers);
                list.setAdapter(adap);
                list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(activity, showName(numbers.get(position)), Toast.LENGTH_SHORT).show();
                        list.setClickable(false);

                        return true;
                    }
                });
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (SystemClock.elapsedRealtime() - StaticStore.unitinflistClick < StaticStore.INTERVAL)
                            return;

                        StaticStore.unitinflistClick = SystemClock.elapsedRealtime();

                        Intent result = new Intent(activity, UnitInfo.class);
                        result.putExtra("ID", numbers.get(position));
                        activity.startActivity(result);
                    }
                });
                break;
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        super.onPostExecute(result);

        if(activity == null) return;

        ListView list = activity.findViewById(R.id.unitinflist);
        ProgressBar prog = activity.findViewById(R.id.unitinfprog);
        TextView ulistst = activity.findViewById(R.id.unitinfst);
        FloatingActionButton search = activity.findViewById(R.id.animsch);
        list.setVisibility(View.VISIBLE);
        prog.setVisibility(View.GONE);
        ulistst.setVisibility(View.GONE);
        search.show();
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
            String name = MultiLangCont.FNAME.getCont(f);
            if(name == null)
                name = "";

            names.add(name);
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
