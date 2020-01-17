package com.mandarin.bcu.androidutil.stage.asynchs;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.StageInfo;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.stage.adapters.StageListAdapter;

import java.lang.ref.WeakReference;

import common.system.MultiLangCont;
import common.util.stage.MapColc;
import common.util.stage.StageMap;

public class StageLoader extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakReference;
    private final int mapcode;
    private final int stid;

    public StageLoader(Activity activity, int mapcode, int stid) {
        this.weakReference = new WeakReference<>(activity);
        this.mapcode = mapcode;
        this.stid = stid;
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        if(activity == null) return;

        ListView stglist = activity.findViewById(R.id.stglist);

        stglist.setVisibility(View.GONE);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        if(activity == null) return null;

        MapColc mc = StaticStore.map.get(mapcode);

        if(mc == null) return null;

        publishProgress(0);

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... result) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        MapColc mc = StaticStore.map.get(mapcode);

        if(mc == null) return;

        StageMap stm = mc.maps[stid];

        String [] stages = new String[stm.list.size()];

        for(int i = 0; i < stages.length; i++) {
            stages[i] = MultiLangCont.STNAME.getCont(stm.list.get(i));
        }

        StageListAdapter stageListAdapter = new StageListAdapter(activity,stages,mapcode,stid);
        ListView stglist = activity.findViewById(R.id.stglist);

        stglist.setAdapter(stageListAdapter);

        stglist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (SystemClock.elapsedRealtime() - StaticStore.stglistClick < StaticStore.INTERVAL)
                    return;

                StaticStore.stglistClick = SystemClock.elapsedRealtime();

                Intent intent = new Intent(activity, StageInfo.class);
                intent.putExtra("mapcode",mapcode);
                intent.putExtra("stid",stid);
                intent.putExtra("posit",position);

                activity.startActivity(intent);
            }
        });

        FloatingActionButton bck = activity.findViewById(R.id.stglistbck);
        bck.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                activity.finish();
            }
        });
    }

    @Override
    protected void onPostExecute(Void results) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        ListView stglist = activity.findViewById(R.id.stglist);
        ProgressBar stgprog = activity.findViewById(R.id.stglistprog);
        TextView stgst = activity.findViewById(R.id.stglistst);

        stglist.setVisibility(View.VISIBLE);
        stgprog.setVisibility(View.GONE);
        stgst.setVisibility(View.GONE);
    }
}
