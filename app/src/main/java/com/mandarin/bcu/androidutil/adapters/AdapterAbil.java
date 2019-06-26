package com.mandarin.bcu.androidutil.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import java.util.List;

public class AdapterAbil extends RecyclerView.Adapter<AdapterAbil.ViewHolder> {
    private List<String> ability;
    private List<String> procs;
    private List<Integer> abilicon;
    private List<Integer> procicon;
    private Context context;

    AdapterAbil(List<String> ability, List<String> procs, List<Integer> abilicon, List<Integer> procicon, Context context) {
        this.ability = ability;
        this.procs = procs;
        this.abilicon = abilicon;
        this.procicon = procicon;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(context).inflate(R.layout.ability_layout,viewGroup,false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        if(viewHolder.getAdapterPosition() < ability.size()) {
            viewHolder.abiltext.setText(ability.get(viewHolder.getAdapterPosition()));
            if (abilicon.get(viewHolder.getAdapterPosition()) != 15 && abilicon.get(viewHolder.getAdapterPosition()) != 19) {
                Bitmap resized = StaticStore.getResizeb(StaticStore.icons[abilicon.get(viewHolder.getAdapterPosition())],context,28f);
                viewHolder.abilicon.setImageBitmap(resized);
            } else {
                viewHolder.abilicon.setImageBitmap(empty());
            }
        } else {
            int location = viewHolder.getAdapterPosition()-ability.size();
            viewHolder.abiltext.setText(procs.get(location));
            Bitmap resized = StaticStore.getResizeb(StaticStore.picons[procicon.get(location)],context,28f);
            viewHolder.abilicon.setImageBitmap(resized);
        }
    }

    @Override
    public int getItemCount() {
        return ability.size()+procs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView abilicon;
        TextView abiltext;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            abilicon = itemView.findViewById(R.id.abilicon);
            abiltext = itemView.findViewById(R.id.ability);
        }
    }

    private Bitmap empty() {
        float dp =32f;
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,r.getDisplayMetrics());
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        return Bitmap.createBitmap((int)px,(int)px,conf);
    }

}
