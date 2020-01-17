package com.mandarin.bcu.androidutil.stage.adapters;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mandarin.bcu.R;

import common.util.stage.MapColc;

public class MapListAdapter extends ArrayAdapter<String> {
    private final Activity activity;
    private final String[] maps;
    private final int mapcode;

    public MapListAdapter(Activity activity, String [] maps, int mapcode) {
        super(activity, R.layout.map_list_layout,maps);

        this.activity = activity;
        this.maps = maps;
        this.mapcode = mapcode;
    }

    private static class ViewHolder {
        TextView name;
        TextView count;

        private ViewHolder(View row) {
            name = row.findViewById(R.id.map_list_name);
            count = row.findViewById(R.id.map_list_coutns);
        }
    }

    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        View row;
        ViewHolder holder;

        MapColc mc = MapColc.MAPS.get(mapcode);

        if(view == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            row = inflater.inflate(R.layout.map_list_layout,parent,false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            row = view;
            holder = (ViewHolder) row.getTag();
        }

        holder.name.setText(withID(position,maps[position]));

        String numbers;

        if(mc != null)
            if(mc.maps[position].list.size() == 1)
                numbers = mc.maps[position].list.size() + activity.getString(R.string.map_list_stage);
            else
                numbers = mc.maps[position].list.size() + activity.getString(R.string.map_list_stages);
        else
            numbers = 0 + activity.getString(R.string.map_list_stages);

        holder.count.setText(numbers);

        return  row;
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
