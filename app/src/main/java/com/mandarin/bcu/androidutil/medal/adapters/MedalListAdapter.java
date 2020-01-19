package com.mandarin.bcu.androidutil.medal.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MedalListAdapter extends ArrayAdapter<String> {
    private final Activity activity;
    private int num;
    private int height;

    private float imgwh;

    public MedalListAdapter(Activity activity, int num, int width, float imgwh, String[] lines) {
        super(activity, R.layout.medal_layout, lines);
        this.activity = activity;
        this.num = num;
        this.imgwh = imgwh;

        height = width / num - StaticStore.dptopx(4f, activity);
    }

    private static class ViewHolder {
        LinearLayout layout;
        List<ImageView> icons;

        private ViewHolder(View view) {
            layout = view.findViewById(R.id.medallinear);
            icons = new ArrayList<>();
        }
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup group) {
        View row;
        ViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            row = inflater.inflate(R.layout.medal_layout, group, false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            row = view;
            holder = (ViewHolder) row.getTag();
        }

        int posit = num * position;

        holder.layout.removeAllViews();
        holder.icons.clear();

        for (int j = 0; j < num; j++) {

            if (posit + j < StaticStore.medals.size()) {
                final int jj = j;
                Bitmap b = StaticStore.getResizeb(StaticStore.medals.get(posit + j), activity, imgwh);

                ImageButton icon = new ImageButton(activity);
                icon.setBackground(activity.getDrawable(R.drawable.image_button_circular));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                params.height = height;
                params.setMarginStart(StaticStore.dptopx(2f, activity));
                params.setMarginEnd(StaticStore.dptopx(2f, activity));
                icon.setLayoutParams(params);

                icon.setImageBitmap(b);

                icon.setOnClickListener(new SingleClick() {
                    @Override
                    public void onSingleClick(View v) {
                        Dialog dialog = new Dialog(activity);

                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.layout_medal_desc);
                        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        View v1 = dialog.getWindow().getDecorView();
                        v1.setBackground(activity.getDrawable(R.drawable.dialog_box));

                        ImageView icon = dialog.findViewById(R.id.medalimg);
                        TextView name = dialog.findViewById(R.id.medalname);
                        TextView desc = dialog.findViewById(R.id.medaldesc);

                        icon.setImageBitmap(StaticStore.getResizeb(StaticStore.medals.get(posit + jj), activity, imgwh));
                        name.setText(StaticStore.MEDNAME.getCont(posit + jj));
                        desc.setText(StaticStore.MEDEXP.getCont(posit + jj));

                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(dialog.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                        dialog.getWindow().setAttributes(lp);

                        dialog.show();
                    }
                });

                holder.layout.addView(icon);
                holder.icons.add(icon);
            } else {
                Bitmap b = StaticStore.empty(activity, imgwh, imgwh);

                ImageView icon = new ImageView(activity);
                icon.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
                icon.setImageBitmap(b);

                holder.layout.addView(icon);
                holder.icons.add(icon);
            }
        }

        return row;
    }
}
