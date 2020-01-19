package com.mandarin.bcu.androidutil.enemy.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import java.util.ArrayList;

public class EnemyListAdapter extends ArrayAdapter<String> {
    private final String[] name;
    private final ArrayList<Integer> location;

    public EnemyListAdapter(Activity activity, String[] name, ArrayList<Integer> location) {
        super(activity, R.layout.listlayout, name);
        this.name = name;
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

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            row = inflater.inflate(R.layout.listlayout, parent, false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            row = view;
            holder = (ViewHolder) row.getTag();
        }

        holder.title.setText(name[position]);
        if (StaticStore.enemies.get(location.get(position)).anim.edi.getImg() != null)
            holder.img.setImageBitmap(StaticStore.getResizeb((Bitmap) StaticStore.enemies.get(location.get(position)).anim.edi.getImg().bimg(), getContext(), 85f, 32f));
        else
            holder.img.setImageBitmap(StaticStore.empty(getContext(), 85f, 32f));
        holder.img.setPadding(StaticStore.dptopx(8f, getContext()), StaticStore.dptopx(12f, getContext()), 0, StaticStore.dptopx(12f, getContext()));

        return row;
    }
}
