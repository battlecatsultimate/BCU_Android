package com.mandarin.bcu.androidutil.stage.adapters;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.mandarin.bcu.EnemyInfo;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.getStrings;

import common.util.stage.SCDef;
import common.util.stage.Stage;
import common.util.unit.Enemy;

public class StEnListRecycle extends  RecyclerView.Adapter<StEnListRecycle.ViewHolder>{
    private final Activity activity;
    private final Stage st;

    private boolean frse = true;
    private int multi = 100;

    public StEnListRecycle(Activity activity, Stage st) {
        this.activity = activity;
        this.st = st;

        if(StaticStore.infoOpened == null) {
            StaticStore.infoOpened = new boolean[st.data.datas.length];

            for (int i = 0; i < st.data.datas.length; i++) {
                StaticStore.infoOpened[i] = false;
            }
        } else if(StaticStore.infoOpened.length < st.data.datas.length) {
            StaticStore.infoOpened = new boolean[st.data.datas.length];

            for (int i = 0; i < st.data.datas.length; i++) {
                StaticStore.infoOpened[i] = false;
            }
        }
    }

    public StEnListRecycle(Activity activity, Stage st, int multi, boolean frse) {
        this.activity = activity;
        this.st = st;
        this.multi = multi;
        this.frse = frse;

        if(StaticStore.infoOpened == null) {
            StaticStore.infoOpened = new boolean[st.data.datas.length];

            for (int i = 0; i < st.data.datas.length; i++) {
                StaticStore.infoOpened[i] = false;
            }
        } else if(StaticStore.infoOpened.length < st.data.datas.length) {
            StaticStore.infoOpened = new boolean[st.data.datas.length];

            for (int i = 0; i < st.data.datas.length; i++) {
                StaticStore.infoOpened[i] = false;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(activity).inflate(R.layout.stage_enemy_list_layout,viewGroup,false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        getStrings s = new getStrings(activity);
        int[][] data = reverse(st.data.datas);

        viewHolder.expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - StaticStore.infoClick < StaticStore.INFO_INTERVAL)
                    return;

                StaticStore.infoClick = SystemClock.elapsedRealtime();

                if(viewHolder.moreinfo.getHeight() == 0) {
                    viewHolder.moreinfo.measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

                    int height = viewHolder.moreinfo.getMeasuredHeight();

                    ValueAnimator anim = ValueAnimator.ofInt(0, height);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int val = (Integer) animation.getAnimatedValue();
                            ViewGroup.LayoutParams layout = viewHolder.moreinfo.getLayoutParams();
                            layout.height = val;
                            viewHolder.moreinfo.setLayoutParams(layout);
                        }
                    });
                    anim.setDuration(300);
                    anim.setInterpolator(new DecelerateInterpolator());
                    anim.start();

                    viewHolder.expand.setImageDrawable(activity.getDrawable(R.drawable.ic_expand_more_black_24dp));
                    StaticStore.infoOpened[viewHolder.getAdapterPosition()] = true;
                } else {
                    viewHolder.moreinfo.measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

                    int height = viewHolder.moreinfo.getMeasuredHeight();

                    ValueAnimator anim = ValueAnimator.ofInt(height, 0);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int val = (Integer) animation.getAnimatedValue();
                            ViewGroup.LayoutParams layout = viewHolder.moreinfo.getLayoutParams();
                            layout.height = val;
                            viewHolder.moreinfo.setLayoutParams(layout);
                        }
                    });
                    anim.setDuration(300);
                    anim.setInterpolator(new DecelerateInterpolator());
                    anim.start();

                    viewHolder.expand.setImageDrawable(activity.getDrawable(R.drawable.ic_expand_less_black_24dp));
                    StaticStore.infoOpened[viewHolder.getAdapterPosition()] = false;
                }
            }
        });

        if(StaticStore.infoOpened[viewHolder.getAdapterPosition()]) {
            viewHolder.moreinfo.measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            ViewGroup.LayoutParams layout = viewHolder.moreinfo.getLayoutParams();
            layout.height = viewHolder.moreinfo.getMeasuredHeight();
            viewHolder.moreinfo.setLayoutParams(layout);

            viewHolder.expand.setImageDrawable(activity.getDrawable(R.drawable.ic_expand_more_black_24dp));
        }

        viewHolder.icon.setImageBitmap(StaticStore.ebitmaps[data[viewHolder.getAdapterPosition()][SCDef.E]]);
        viewHolder.number.setText(s.getNumber(data[viewHolder.getAdapterPosition()]));

        viewHolder.info.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                Enemy en = StaticStore.enemies.get(data[viewHolder.getAdapterPosition()][SCDef.E]);

                Intent intent = new Intent(activity, EnemyInfo.class);
                intent.putExtra("ID",en.id);
                intent.putExtra("Multiply",(int)((float)data[viewHolder.getAdapterPosition()][SCDef.M]*(float)multi/(float)100));

                activity.startActivity(intent);
            }
        });

        viewHolder.multiply.setText(s.getMultiply(data[viewHolder.getAdapterPosition()],multi));
        viewHolder.bh.setText(s.getBaseHealth(data[viewHolder.getAdapterPosition()]));

        if(data[viewHolder.getAdapterPosition()][SCDef.B] == 0)
            viewHolder.isboss.setText(activity.getString(R.string.unit_info_false));
        else
            viewHolder.isboss.setText(activity.getString(R.string.unit_info_true));

        viewHolder.layer.setText(s.getLayer(data[viewHolder.getAdapterPosition()]));

        viewHolder.startb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.start.getText().toString().endsWith("f"))
                    viewHolder.start.setText(s.getStart(data[viewHolder.getAdapterPosition()],false));
                else
                    viewHolder.start.setText(s.getStart(data[viewHolder.getAdapterPosition()],true));
            }
        });

        viewHolder.start.setText(s.getStart(data[viewHolder.getAdapterPosition()],frse));

        viewHolder.respawnb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.respawn.getText().toString().endsWith("f"))
                    viewHolder.respawn.setText(s.getRespawn(data[viewHolder.getAdapterPosition()],false));
                else
                    viewHolder.respawn.setText(s.getRespawn(data[viewHolder.getAdapterPosition()],true));
            }
        });

        viewHolder.respawn.setText(s.getRespawn(data[viewHolder.getAdapterPosition()],frse));
    }

    @Override
    public int getItemCount() {
        return st.data.datas.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton expand;
        ImageView icon;
        TextView multiply;
        TextView number;
        ImageButton info;
        TextView bh;
        TextView isboss;
        TextView layer;
        Button startb;
        TextView start;
        Button respawnb;
        TextView respawn;

        TableLayout moreinfo;

        ViewHolder(@NonNull View row) {
            super(row);

            expand = row.findViewById(R.id.stgenlistexp);
            icon = row.findViewById(R.id.stgenlisticon);
            multiply = row.findViewById(R.id.stgenlistmultir);
            number = row.findViewById(R.id.stgenlistnumr);
            info = row.findViewById(R.id.stgenlistinfo);
            bh = row.findViewById(R.id.enemlistbhr);
            isboss = row.findViewById(R.id.enemlistibr);
            layer = row.findViewById(R.id.enemlistlayr);
            startb = row.findViewById(R.id.enemlistst);
            start = row.findViewById(R.id.enemliststr);
            respawnb = row.findViewById(R.id.enemlistres);
            respawn = row.findViewById(R.id.enemlistresr);
            moreinfo = row.findViewById(R.id.stgenlistmi);
        }
    }

    private int[][] reverse(int [][] data) {
        int [][] result = new int[data.length][];

        for(int i = 0; i < data.length; i++) {
            result[i] = data[data.length-1-i];
        }

        return result;
    }

}