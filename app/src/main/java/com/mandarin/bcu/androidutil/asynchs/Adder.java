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
import com.mandarin.bcu.androidutil.FilterEntity;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.UnitListAdapter;

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

    private boolean empty;
    private boolean tgorand;
    private boolean atksimu;
    private boolean aborand;
    private boolean atkorand;
    private boolean talents;
    private ArrayList<String> target;
    private ArrayList<String> attack;
    private ArrayList<String> rarity;
    private ArrayList<ArrayList<Integer>> ability;

    public Adder(int unitnumber, Activity context,boolean empty, boolean tgorand, boolean atksimu, boolean aborand, boolean atkorand, boolean talents
    ,ArrayList<String> target, ArrayList<String> attack,ArrayList<String> rarity, ArrayList<ArrayList<Integer>> ability) {
        this.unitnumber = unitnumber;
        this.weakReference = new WeakReference<>(context);
        this.empty = empty;
        this.tgorand = tgorand;
        this.atksimu = atksimu;
        this.aborand = aborand;
        this.atkorand = atkorand;
        this.talents = talents;
        this.target = target;
        this.attack = attack;
        this.rarity = rarity;
        this.ability = ability;
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
        FilterEntity filterEntity = new FilterEntity(rarity,attack,target,ability,atksimu,atkorand,tgorand,aborand,empty,unitnumber,talents);
        ArrayList<Integer> numbers = filterEntity.setFilter();
        ArrayList<String> names = new ArrayList<>();

        for(int i : numbers) {
            names.add(StaticStore.names[i]);
        }
        UnitListAdapter adap = new UnitListAdapter(activity,names.toArray(new String[0]),StaticStore.bitmaps,numbers);
        list.setAdapter(adap);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(activity,showName(numbers.get(position)),Toast.LENGTH_SHORT).show();
                list.setClickable(false);

                return true;
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(SystemClock.elapsedRealtime() - StaticStore.unitinflistClick < StaticStore.INTERVAL)
                    return;

                StaticStore.unitinflistClick = SystemClock.elapsedRealtime();

                Intent result = new Intent(activity, UnitInfo.class);
                result.putExtra("ID",numbers.get(position));
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
