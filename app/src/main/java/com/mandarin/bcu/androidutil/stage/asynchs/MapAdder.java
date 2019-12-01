package com.mandarin.bcu.androidutil.stage.asynchs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.R;
import com.mandarin.bcu.StageList;
import com.mandarin.bcu.androidutil.enemy.EDefiner;
import com.mandarin.bcu.androidutil.stage.MapDefiner;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.stage.adapters.MapListAdapter;
import com.mandarin.bcu.androidutil.unit.Definer;

import java.lang.ref.WeakReference;
import java.util.Objects;

import common.system.MultiLangCont;
import common.system.files.VFile;
import common.util.stage.CharaGroup;
import common.util.stage.Limit;

public class MapAdder extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakReference;

    public MapAdder(Activity activity) {
        weakReference = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        if(activity == null) return;

        ListView maplist = activity.findViewById(R.id.maplist);

        maplist.setVisibility(View.GONE);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        if(activity == null) return null;

        new Definer().define(activity);

        publishProgress(0);

        new MapDefiner().define(activity);

        publishProgress(1);

        new EDefiner().define(activity);

        publishProgress(2);

        if(StaticStore.ebitmaps == null) {
            StaticStore.ebitmaps = new Bitmap[StaticStore.emnumber];

            for(int i = 0; i < StaticStore.emnumber; i++) {
                String shortPath = "./org/enemy/"+number(i)+"/edi_"+number(i)+".png";

                try {
                    float ratio = 32f/32f;
                    StaticStore.ebitmaps[i] = StaticStore.getResizeb((Bitmap) Objects.requireNonNull(VFile.getFile(shortPath)).getData().getImg().bimg(),activity,85f*ratio,32f*ratio);
                } catch (NullPointerException e) {
                    StaticStore.ebitmaps[i] = StaticStore.empty(activity,85f,32f);
                }
            }
        }

        publishProgress(3);

        if(StaticStore.enames == null) {
            StaticStore.enames = new String[StaticStore.emnumber];

            for(int i = 0; i < StaticStore.emnumber; i++) {
                StaticStore.enames[i] = withID(i, MultiLangCont.ENAME.getCont(StaticStore.enemies.get(i)));
            }
        }

        publishProgress(4);

        if(StaticStore.eicons == null) {
            StaticStore.eicons = new Bitmap[StaticStore.emnumber];

            for(int i = 0; i < StaticStore.emnumber; i++) {
                String shortPath = "./org/enemy/"+number(i)+"/enemy_icon_"+number(i)+".png";

                try{
                    float ratio = 32f/32f;
                    StaticStore.eicons[i] = StaticStore.getResizeb((Bitmap) Objects.requireNonNull(VFile.getFile(shortPath)).getData().getImg().bimg(),activity,36f*ratio);
                } catch(Exception e) {
                    float ratio = 32f/32f;
                    StaticStore.eicons[i] = StaticStore.empty(activity,18f*ratio,18f*ratio);
                }
            }
        }

        publishProgress(5);

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... results) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        TextView mapst = activity.findViewById(R.id.mapst);

        switch (results[0]) {
            case 0:
                mapst.setText(R.string.stg_info_stgd);
                break;
            case 1:
                mapst.setText(activity.getString(R.string.stg_info_enem));
                break;
            case 2:
                mapst.setText(activity.getString(R.string.stg_info_enemimg));
                break;
            case 3:
                mapst.setText(activity.getString(R.string.stg_info_enemname));
                break;
            case 4:
                mapst.setText(R.string.stg_list_enemic);
                break;
            case 5:
                mapst.setText(activity.getString(R.string.stg_info_stgs));

                Spinner stageset = activity.findViewById(R.id.stgspin);

                ListView maplist = activity.findViewById(R.id.maplist);

                stageset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        MapListAdapter mapListAdapter = new MapListAdapter(activity,StaticStore.mapnames[position],StaticStore.MAPCODE[position]);

                        maplist.setAdapter(mapListAdapter);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                MapListAdapter mapListAdapter = new MapListAdapter(activity,StaticStore.mapnames[stageset.getSelectedItemPosition()],StaticStore.MAPCODE[stageset.getSelectedItemPosition()]);
                maplist.setAdapter(mapListAdapter);

                maplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (SystemClock.elapsedRealtime() - StaticStore.maplistClick < StaticStore.INTERVAL)
                            return;

                        StaticStore.maplistClick = SystemClock.elapsedRealtime();

                        Intent intent = new Intent(activity, StageList.class);
                        intent.putExtra("mapcode",StaticStore.MAPCODE[stageset.getSelectedItemPosition()]);
                        intent.putExtra("stid",position);

                        activity.startActivity(intent);
                    }
                });
        }
    }

    @Override
    protected void onPostExecute(Void results) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        ListView maplist = activity.findViewById(R.id.maplist);
        TextView mapst = activity.findViewById(R.id.mapst);
        ProgressBar mapprog = activity.findViewById(R.id.mapprog);

        maplist.setVisibility(View.VISIBLE);
        mapst.setVisibility(View.GONE);
        mapprog.setVisibility(View.GONE);
    }

    private String number(int num) {
        if(0 <= num && num < 10)
            return "00"+num;
        else if(10 <= num && num < 100)
            return "0"+num;
        else
            return ""+num;
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
