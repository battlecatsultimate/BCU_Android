package com.mandarin.bcu.androidutil.stage.asynchs;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.stage.adapters.EnemyListRecycle;
import com.mandarin.bcu.androidutil.stage.adapters.StageRecycle;

import java.lang.ref.WeakReference;

import common.system.MultiLangCont;
import common.util.pack.Background;
import common.util.stage.MapColc;
import common.util.stage.Stage;
import common.util.stage.StageMap;

public class StageAdder extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakReference;
    private final int mapcode,stid,posit;

    public StageAdder(Activity activity, int mapcode, int stid, int posit) {
        this.weakReference = new WeakReference<>(activity);
        this.mapcode = mapcode;
        this.stid = stid;
        this.posit = posit;
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        if(activity == null) return;

        ScrollView scrollView = activity.findViewById(R.id.stginfoscroll);

        scrollView.setVisibility(View.GONE);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if(StaticStore.treasure == null)
            StaticStore.readTreasureIcon();

        publishProgress(0);

        if(StaticStore.bgread == 0) {
            Background.read();
            StaticStore.bgread = 1;
        }

        publishProgress(1);

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... results) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        TextView st = activity.findViewById(R.id.stginfost);

        switch (results[0]) {
            case 0:
                st.setText(R.string.stg_info_loadbg);
            case 1:
                st.setText(R.string.stg_info_loadfilt);

                TextView title = activity.findViewById(R.id.stginfoname);

                MapColc mc = MapColc.MAPS.get(mapcode);

                if(mc == null) return;

                if(stid >= mc.maps.length || stid < 0) return;

                StageMap stm = mc.maps[stid];

                if(stm == null) return;

                if(posit >= stm.list.size() || posit < 0) return;

                Stage stage = stm.list.get(posit);

                title.setText(MultiLangCont.STNAME.getCont(stage));

                ScrollView stgscroll = activity.findViewById(R.id.stginfoscroll);
                stgscroll.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
                stgscroll.setFocusable(false);
                stgscroll.setFocusableInTouchMode(true);

                RecyclerView stgrec = activity.findViewById(R.id.stginforec);
                stgrec.setLayoutManager(new LinearLayoutManager(activity));
                ViewCompat.setNestedScrollingEnabled(stgrec,false);

                StageRecycle stageRecycle = new StageRecycle(activity,mapcode,stid,posit);

                stgrec.setAdapter(stageRecycle);

                RecyclerView stgen = activity.findViewById(R.id.stginfoenrec);
                stgen.setLayoutManager(new LinearLayoutManager(activity));
                ViewCompat.setNestedScrollingEnabled(stgen,false);

                EnemyListRecycle enemyListRecycle = new EnemyListRecycle(activity,stage);

                stgen.setAdapter(enemyListRecycle);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        ScrollView scrollView = activity.findViewById(R.id.stginfoscroll);
        ProgressBar prog = activity.findViewById(R.id.stginfoprog);
        TextView st = activity.findViewById(R.id.stginfost);

        scrollView.setVisibility(View.VISIBLE);
        prog.setVisibility(View.GONE);
        st.setVisibility(View.GONE);
    }
}
