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

public class ComboSchListAdapter extends ArrayAdapter<String> {
    private String [] sch;
    private boolean [] combocat = new boolean[5];
    private ListView schlst2;
    private ListView combolist;
    private Activity activity;
    private ComboListAdapter comboListAdapter;

    private int [][] locater = {{0,1,2},{14,15,16,17,18,19,20,21,22,23,24},{3,6,7,10},{5,4,9},{11,12,13}};
    private int [][] locateid = {{R.string.combo_atk,R.string.combo_hp,R.string.combo_spd},{R.string.combo_strag,R.string.combo_md,R.string.combo_res,R.string.combo_kbdis,R.string.combo_sl,R.string.combo_st,R.string.combo_wea,R.string.combo_inc,R.string.combo_wit,R.string.combo_eva,R.string.combo_crit},{R.string.combo_caninch,R.string.combo_canatk,R.string.combo_canchtime,R.string.combo_bsh},{R.string.combo_initmon,R.string.combo_work,R.string.combo_wal},{R.string.combo_cd,R.string.combo_ac,R.string.combo_xp}};
    private List<String> comid = new ArrayList<>();

    ComboSchListAdapter(Activity activity, String[] sch, ListView schlst2, ListView combolist, ComboListAdapter comboListAdapter) {
        super(activity, R.layout.spinneradapter,sch);
        this.sch = sch;
        this.schlst2 = schlst2;
        this.combolist = combolist;
        this.activity = activity;
        this.comboListAdapter = comboListAdapter;
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

        holder.category.setText(sch[position]);

        holder.category.setBackgroundColor(combocat[position] ?StaticStore.getAttributeColor(getContext(),R.attr.SelectionPrimary):getContext().getColor(android.R.color.transparent));

        holder.category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                combocat[position] = !combocat[position];
                holder.category.setBackgroundColor(combocat[position] ?StaticStore.getAttributeColor(getContext(),R.attr.SelectionPrimary):getContext().getColor(android.R.color.transparent));

                if(combocat[position]) {
                    comid.add(String.valueOf(position));
                } else {
                    comid.remove(String.valueOf(position));
                }

                StaticStore.combos.clear();

                if(comid.isEmpty()) {
                    List<Integer> locates = new ArrayList<>();

                    for (int[] ints1 : locater) {
                        for (int j : ints1) {
                            locates.add(j);
                        }
                    }

                    for(int i = 0; i < Combo.combos.length; i++) {
                        StaticStore.combos.addAll(Arrays.asList(Combo.combos[i]));
                    }

                    String [] names = new String[StaticStore.combos.size()];
                    List<String> subsch = new ArrayList<>();

                    for (int[] ints : locateid) {
                        for (int anInt : ints) {
                            subsch.add(getContext().getString(anInt));
                        }
                    }

                    for(int i = 0; i < StaticStore.combos.size(); i++) {
                        names[i] = MultiLangCont.COMNAME.getCont(StaticStore.combos.get(i).name);
                    }

                    comboListAdapter = new ComboListAdapter(activity,names);

                    combolist.setAdapter(comboListAdapter);

                    ComboSubSchListAdapter adapter = new ComboSubSchListAdapter(activity,subsch,combolist,locates,comboListAdapter);

                    schlst2.setAdapter(adapter);
                } else {
                    List<Integer> locates = new ArrayList<>();
                    List<String> subsch = new ArrayList<>();

                    for(int i = 0; i < comid.size(); i++) {
                        for(int j : locater[Integer.parseInt(comid.get(i))]) {
                            locates.add(j);
                        }
                    }

                    for(int i = 0; i < comid.size(); i++) {
                        for(int j : locateid[Integer.parseInt(comid.get(i))]) {
                            subsch.add(getContext().getString(j));
                        }
                    }

                    for(int i = 0; i < locates.size(); i++) {
                        StaticStore.combos.addAll(Arrays.asList(Combo.combos[locates.get(i)]));
                    }

                    String [] names = new String[StaticStore.combos.size()];

                    for(int i = 0; i < StaticStore.combos.size(); i++) {
                        names[i] = MultiLangCont.COMNAME.getCont(StaticStore.combos.get(i).name);
                    }

                    comboListAdapter = new ComboListAdapter(activity,names);
                    ComboSubSchListAdapter adapter = new ComboSubSchListAdapter(activity,subsch,combolist,locates,comboListAdapter);

                    combolist.setAdapter(comboListAdapter);
                    schlst2.setAdapter(adapter);
                }
            }
        });

        return row;
    }

    private int getSizeofLocater(int... ints) {
        int sum = 0;

        for(int i = 0; i < ints.length; i++) {
            sum += locater[i].length;
        }

        return sum;
    }
}
