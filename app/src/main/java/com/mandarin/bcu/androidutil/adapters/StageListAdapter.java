package com.mandarin.bcu.androidutil.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import common.util.stage.MapColc;
import common.util.stage.Stage;
import common.util.stage.StageMap;
import common.util.unit.Enemy;

public class StageListAdapter extends ArrayAdapter<String> {
    private final Activity activity;
    private final String[] stages;

    private final int mapcode;
    private final int stid;

    public StageListAdapter(Activity activity, String [] stages, int mapcode, int stid) {
        super(activity, R.layout.stage_list_layout,stages);

        this.activity = activity;
        this.stages = stages;
        this.mapcode = mapcode;
        this.stid = stid;
    }

    private static class ViewHolder {
        TextView name;
        FlexboxLayout icons;
        List<ImageView> images = new ArrayList<>();

        private ViewHolder(View row) {
            name = row.findViewById(R.id.stagename);
            icons = row.findViewById(R.id.enemicon);
        }
    }

    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        View row;
        ViewHolder holder;

        if(view == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            row = inflater.inflate(R.layout.stage_list_layout,parent,false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            row = view;
            holder = (ViewHolder)row.getTag();
        }

        holder.name.setText(stages[position]);

        holder.images.clear();
        holder.icons.removeAllViews();

        MapColc mc = StaticStore.map.get(mapcode);

        if(mc == null) return row;

        StageMap stm = mc.maps[stid];

        Stage st = stm.list.get(position);

        Set<Enemy> enemies = st.data.getAllEnemy();

        List<Integer> ids = new ArrayList<>();

        for(Enemy e : enemies) {
            if(!ids.contains(e.id) && e.id != 21)
                ids.add(e.id);
        }

        Collections.sort(ids);

        ImageView[] icons = new ImageView[ids.size()];

        for (int i = 0; i < ids.size(); i++) {
            icons[i] = new ImageView(activity);
            icons[i].setLayoutParams(new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            icons[i].setImageBitmap(StaticStore.eicons[ids.get(i)]);
            icons[i].setPadding(StaticStore.dptopx(12f,activity),StaticStore.dptopx(4f,activity),0,StaticStore.dptopx(4f,activity));


            holder.icons.addView(icons[i]);
            holder.images.add(icons[i]);
        }

        return row;
    }
}
