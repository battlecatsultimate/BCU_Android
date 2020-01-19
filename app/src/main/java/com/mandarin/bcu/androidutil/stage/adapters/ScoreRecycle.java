package com.mandarin.bcu.androidutil.stage.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mandarin.bcu.R;

import common.system.MultiLangCont;
import common.util.stage.Stage;

public class ScoreRecycle extends RecyclerView.Adapter<ScoreRecycle.ViewHolder> {
    private final Stage st;
    private final Activity activity;

    ScoreRecycle(Stage st, Activity activity) {
        this.st = st;
        this.activity = activity;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView score;
        TextView item;
        TextView amount;

        ViewHolder(@NonNull View row) {
            super(row);

            score = row.findViewById(R.id.dropchance);
            item = row.findViewById(R.id.dropitem);
            amount = row.findViewById(R.id.dropamount);
        }
    }

    @NonNull
    @Override
    public ScoreRecycle.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(activity).inflate(R.layout.drop_info_layout, viewGroup, false);

        return new ScoreRecycle.ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreRecycle.ViewHolder viewHolder, int i) {
        int[] data = st.info.time[i];

        viewHolder.score.setText(String.valueOf(data[0]));

        String reward = MultiLangCont.RWNAME.getCont(data[1]);

        if (reward == null)
            reward = String.valueOf(data[1]);

        viewHolder.item.setText(reward);
        viewHolder.amount.setText(String.valueOf(data[2]));
    }

    @Override
    public int getItemCount() {
        return st.info.time.length;
    }
}
