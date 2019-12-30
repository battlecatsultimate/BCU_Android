package com.mandarin.bcu.androidutil.lineup.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import common.system.MultiLangCont;
import common.util.unit.Combo;

public class ComboSubSchListAdapter extends ArrayAdapter<String> {
    private List<String> sch;
    private boolean [] combocat;
    private ListView combolist;
    private Activity activity;
    private ComboListAdapter comboListAdapter;

    private List<String> comid = new ArrayList<>();
    private List<Integer> defcom;

    ComboSubSchListAdapter(Activity activity, List<String> sch, ListView combolist, List<Integer> defcom, ComboListAdapter comboListAdapter) {
        super(activity, R.layout.spinneradapter,sch);
        this.sch = sch;
        this.combolist = combolist;
        this.activity = activity;
        this.defcom = defcom;
        this.comboListAdapter = comboListAdapter;

        combocat = new boolean[sch.size()];
    }

    private static class ViewHolder {
        TextView category;

        private ViewHolder(View view) {
            category = view.findViewById(R.id.spinnertext);
        }
    }

    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        View row;
        ViewHolder holder;

        if(view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            row = inflater.inflate(R.layout.spinneradapter,parent,false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            row = view;
            holder = (ViewHolder) row.getTag();
        }

        holder.category.setText(sch.get(position));

        holder.category.setBackgroundColor(combocat[position] ?StaticStore.getAttributeColor(getContext(),R.attr.SelectionPrimary):getContext().getColor(android.R.color.transparent));

        holder.category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                combocat[position] = !combocat[position];
                holder.category.setBackgroundColor(combocat[position] ?StaticStore.getAttributeColor(getContext(),R.attr.SelectionPrimary):getContext().getColor(android.R.color.transparent));

                if(combocat[position]) {
                    comid.add(String.valueOf(defcom.get(position)));
                } else {
                    comid.remove(String.valueOf(defcom.get(position)));
                }

                StaticStore.combos.clear();

                if(comid.isEmpty()) {
                    for(int i = 0; i < defcom.size(); i++) {
                        StaticStore.combos.addAll(Arrays.asList(Combo.combos[defcom.get(i)]));
                    }

                    String [] names = new String[StaticStore.combos.size()];

                    for(int i = 0; i < StaticStore.combos.size(); i++) {
                        names[i] = MultiLangCont.COMNAME.getCont(StaticStore.combos.get(i).name);
                    }

                    comboListAdapter = new ComboListAdapter(activity,names);

                    combolist.setAdapter(comboListAdapter);
                } else {

                    for(int i = 0; i < comid.size(); i++) {
                        StaticStore.combos.addAll(Arrays.asList(Combo.combos[Integer.parseInt(comid.get(i))]));
                    }

                    String [] names = new String[StaticStore.combos.size()];

                    for(int i = 0; i < StaticStore.combos.size(); i++) {
                        names[i] = MultiLangCont.COMNAME.getCont(StaticStore.combos.get(i).name);
                    }

                    comboListAdapter = new ComboListAdapter(activity,names);

                    combolist.setAdapter(comboListAdapter);
                }
            }
        });

        return row;
    }
}
