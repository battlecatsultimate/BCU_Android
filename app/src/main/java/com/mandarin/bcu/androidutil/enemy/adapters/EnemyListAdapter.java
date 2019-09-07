package com.mandarin.bcu.androidutil.enemy.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import java.util.ArrayList;

public class EnemyListAdapter extends ArrayAdapter<String> {
    private final Activity activity;
    private final String[] name;
    private final Bitmap[] img;
    private final ArrayList<Integer> location;

    public EnemyListAdapter(Activity activity, String [] name, Bitmap[] img, ArrayList<Integer> location) {
        super(activity, R.layout.listlayout,name);

        this.activity = activity;
        this.name = name;
        this.img = img;
        this.location = location;
    }

    private static class ViewHolder {
        TextView title;
        ImageView img;

        private ViewHolder(View row) {
            title = row.findViewById(R.id.unitname);
            img = row.findViewById(R.id.uniticon);
        }
    }

    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        View row;
        ViewHolder holder;

        if(view == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            row = inflater.inflate(R.layout.listlayout,parent,false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            row = view;
            holder = (ViewHolder) row.getTag();
        }

        holder.title.setText(name[position]);
        holder.img.setImageBitmap(img[location.get(position)]);
        holder.img.setPadding(StaticStore.dptopx(8f,getContext()), StaticStore.dptopx(12f,getContext()),0,StaticStore.dptopx(12f,getContext()));

        return row;
    }
}
