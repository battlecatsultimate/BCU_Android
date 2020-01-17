package com.mandarin.bcu.androidutil.stage.adapters;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.getStrings;

import common.util.stage.Limit;

public class LimitRecycle extends RecyclerView.Adapter<LimitRecycle.ViewHolder>{
    private final Activity activity;
    private String [] limits;

    public LimitRecycle(Activity activity, Limit l) {
        this.activity = activity;

        getStrings s = new getStrings(activity);

        limits = s.getLimit(l);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView limit;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            limit = itemView.findViewById(R.id.limitst);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(activity).inflate(R.layout.stg_limit_layout,viewGroup,false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.limit.setText(limits[viewHolder.getAdapterPosition()]);
    }

    @Override
    public int getItemCount() {
        return limits.length;
    }
}
