package com.mandarin.bcu.androidutil.stage.adapters;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import common.system.MultiLangCont;
import common.util.stage.Stage;

public class DropRecycle extends RecyclerView.Adapter<DropRecycle.ViewHolder> {
    private final Stage st;
    private final Activity activity;

    public DropRecycle(Stage st, Activity activity) {
        this.st = st;
        this.activity = activity;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView chance;
        TextView item;
        TextView amount;

        ViewHolder(@NonNull View row) {
            super(row);

            chance = row.findViewById(R.id.dropchance);
            item = row.findViewById(R.id.dropitem);
            amount = row.findViewById(R.id.dropamount);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(activity).inflate(R.layout.drop_info_layout,viewGroup,false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        int [] data = st.info.drop[i];

        String chance = data[0] +" %";

        viewHolder.chance.setText(chance);

        String reward = MultiLangCont.RWNAME.getCont(data[1]);

        if(reward == null)
            reward = String.valueOf(data[1]);

        if(i == 0) {
            if(data[0] != 100) {
                BitmapDrawable bd = new BitmapDrawable(activity.getResources(), StaticStore.getResizeb(StaticStore.treasure, activity, 24f));
                bd.setFilterBitmap(true);
                bd.setAntiAlias(true);
                viewHolder.item.setCompoundDrawablesWithIntrinsicBounds(null, null, bd, null);
            }

            if(st.info.rand == 1 || data[1] >= 1000) {
                reward += activity.getString(R.string.stg_info_once);
                viewHolder.item.setText(reward);
            } else {
                viewHolder.item.setText(reward);
            }
        } else {
            viewHolder.item.setText(reward);
        }

        viewHolder.amount.setText(String.valueOf(data[2]));
    }

    @Override
    public int getItemCount() {
        return st.info.drop.length;
    }
}
