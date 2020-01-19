package com.mandarin.bcu.androidutil.unit.adapters;

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

public class UnitListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] name;
    private final ArrayList<Integer> locate;

    public UnitListAdapter(Activity context, String[] name, ArrayList<Integer> location) {
        super(context, R.layout.listlayout, name);

        this.context = context;
        this.name = name;
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
        holder.image.setImageBitmap(StaticStore.MakeIcon(getContext(), (Bitmap) StaticStore.units.get(locate.get(position)).forms[0].anim.uni.getImg().bimg(), 48f));

        return row;
    }
}
