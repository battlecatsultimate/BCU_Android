package com.mandarin.bcu.androidutil.stage.adapters;

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
import java.util.List;

import common.util.stage.MapColc;
import common.util.stage.SCDef;
import common.util.stage.Stage;
import common.util.stage.StageMap;

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

        List<Integer> ids = getid(st.data);

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

    private List<Integer> getid(SCDef stage) {
        List<int []> result = new ArrayList<>();
        int [][] data = reverse(stage.datas);

        for (int[] datas : data) {
            if (result.isEmpty()) {
                result.add(datas);
                continue;
            }

            int id = datas[SCDef.E];

            if (haveSame(id, result)) {
                result.add(datas);
            }
        }

        List<Integer> ids = new ArrayList<>();

        for(int [] datas : result) {
            ids.add(datas[SCDef.E]);
        }

        return ids;
    }

    private boolean haveSame(int id, List<int []> result) {
        if(id == 19 || id == 20 || id == 21) return false;

        for(int [] data : result) {
            if(id == data[SCDef.E])
                return false;
        }

        return true;
    }

    private int[][] reverse(int [][] data) {
        int [][] result = new int[data.length][];

        for(int i = 0; i < data.length; i++) {
            result[i] = data[data.length-1-i];
        }

        return result;
    }
}
