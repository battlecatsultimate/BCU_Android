package com.mandarin.bcu.androidutil.stage.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mandarin.bcu.R;

import common.util.stage.Stage;

public class EnemyListRecycle extends RecyclerView.Adapter<EnemyListRecycle.ViewHolder> {
    private final Activity activity;
    private final Stage st;
    private int multi;

    public EnemyListRecycle(Activity activity, Stage st) {
        this.activity = activity;
        this.st = st;
    }

    public EnemyListRecycle(Activity activity, Stage st, int multi) {
        this.activity = activity;
        this.st = st;
        this.multi = multi;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView listView;
        Button frse;

        ViewHolder(@NonNull View row) {
            super(row);

            listView = row.findViewById(R.id.stginfoenemlist);
            frse = row.findViewById(R.id.stginfoenfrse);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(activity).inflate(R.layout.stage_enemy_layout,viewGroup,false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        SharedPreferences shared = activity.getSharedPreferences("configuration", Context.MODE_PRIVATE);

        viewHolder.listView.setLayoutManager(new LinearLayoutManager(activity));
        ViewCompat.setNestedScrollingEnabled(viewHolder.listView,false);

        StEnListRecycle listAdapter = new StEnListRecycle(activity,st,multi,shared.getBoolean("frame",true));

        viewHolder.listView.setAdapter(listAdapter);

        if(!shared.getBoolean("frame",true))
            viewHolder.frse.setText(activity.getString(R.string.config_seconds));

        viewHolder.frse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.frse.getText().toString().equals(activity.getString(R.string.config_frames))) {
                    StEnListRecycle listAdapter = new StEnListRecycle(activity,st,multi,false);

                    viewHolder.listView.setAdapter(listAdapter);

                    viewHolder.frse.setText(activity.getString(R.string.config_seconds));
                } else {
                    StEnListRecycle listAdapter = new StEnListRecycle(activity,st,multi,true);

                    viewHolder.listView.setAdapter(listAdapter);

                    viewHolder.frse.setText(activity.getString(R.string.config_frames));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
