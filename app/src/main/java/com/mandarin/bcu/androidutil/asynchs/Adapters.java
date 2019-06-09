package com.mandarin.bcu.androidutil.asynchs;

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

import java.util.ArrayList;

public class Adapters extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] name;
    private final Bitmap[] img;
    private final ArrayList<Integer> locate;

    public Adapters(Activity context, String[] name, Bitmap[] img, ArrayList<Integer> location) {
        super(context, R.layout.listlayout, name);

        this.context = context;
        this.name = name;
        this.img = img;
        this.locate = location;
    }

    private static class ViewHolder {
        TextView title;
        ImageView image;

        private ViewHolder(View row) {
            title = row.findViewById(R.id.unitname);
            image = row.findViewById(R.id.uniticon);
        }
    }

    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        View row;
        ViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            row = inflater.inflate(R.layout.listlayout, parent, false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            row = view;
            holder = (ViewHolder) row.getTag();
        }

        holder.title.setText(name[position]);
        holder.image.setImageBitmap(img[locate.get(position)]);

        return row;
    }
}
