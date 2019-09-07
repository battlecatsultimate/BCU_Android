package com.mandarin.bcu.androidutil.stage.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.getStrings;

import java.util.ArrayList;
import java.util.List;

import common.util.stage.MapColc;
import common.util.stage.Stage;
import common.util.stage.StageMap;

public class StageRecycle extends RecyclerView.Adapter<StageRecycle.ViewHolder> {
    private Activity activity;
    private final int mapcode;
    private final int stid;
    private final int posit;
    private final getStrings s;

    public StageRecycle(Activity activity,int mapcode, int stid, int posit) {
        this.activity = activity;
        this.mapcode = mapcode;
        this.stid = stid;
        this.posit = posit;
        this.s = new getStrings(activity);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView id;
        Spinner star;
        TextView energy;
        TextView xp;
        TextView health;
        TextView difficulty;
        TextView continueable;
        TextView length;
        TextView maxenemy;
        TextView music;
        TextView castleperc;
        TextView music2;
        Button background;
        Button castle;
        TextView droptitle;
        RecyclerView drop;
        TableRow droprow;
        NestedScrollView dropscroll;
        RecyclerView score;
        TableRow scorerow;
        NestedScrollView scorescroll;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            id = itemView.findViewById(R.id.stginfoidr);
            star = itemView.findViewById(R.id.stginfostarr);
            energy = itemView.findViewById(R.id.stginfoengr);
            xp = itemView.findViewById(R.id.stginfoxpr);
            health = itemView.findViewById(R.id.stginfobhr);
            difficulty = itemView.findViewById(R.id.stginfodifr);
            continueable = itemView.findViewById(R.id.stginfocontinr);
            length = itemView.findViewById(R.id.stginfolenr);
            maxenemy = itemView.findViewById(R.id.stginfomaxenr);
            music = itemView.findViewById(R.id.stginfomusicr);
            castleperc = itemView.findViewById(R.id.stginfomusic2);
            music2 = itemView.findViewById(R.id.stginfomusic2r);
            background = itemView.findViewById(R.id.stginfobgr);
            castle = itemView.findViewById(R.id.stginfoctr);
            droptitle = itemView.findViewById(R.id.stginfodrop);
            drop = itemView.findViewById(R.id.droprec);
            droprow = itemView.findViewById(R.id.drop);
            dropscroll = itemView.findViewById(R.id.dropscroll);
            score = itemView.findViewById(R.id.scorerec);
            scorerow = itemView.findViewById(R.id.score);
            scorescroll = itemView.findViewById(R.id.scorescroll);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(activity).inflate(R.layout.stage_info_layout,viewGroup,false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        MapColc mc = StaticStore.map.get(mapcode);

        if(mc == null) return;

        if(stid >= mc.maps.length || stid < 0) return;

        StageMap stm = mc.maps[stid];

        if(stm == null) return;

        if(posit >= stm.list.size() || posit < 0) return;

        Stage st = stm.list.get(posit);

        viewHolder.id.setText(s.getID(mapcode,stid,posit));

        List<String> stars = new ArrayList<>();

        for(int k = 0; k < stm.stars.length; k++) {
            String s = k+1 + " (" + stm.stars[k]+" %)";
            stars.add(s);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity,R.layout.spinneradapter,stars);

        viewHolder.star.setAdapter(arrayAdapter);

        viewHolder.star.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RecyclerView enrec = activity.findViewById(R.id.stginfoenrec);
                enrec.setLayoutManager(new LinearLayoutManager(activity));
                ViewCompat.setNestedScrollingEnabled(enrec,false);

                EnemyListRecycle listRecycle = new EnemyListRecycle(activity,st,stm.stars[position]);

                enrec.setAdapter(listRecycle);

                StaticStore.stageSpinner = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if(StaticStore.stageSpinner != -1) {
            viewHolder.star.setSelection(StaticStore.stageSpinner);
        }

        viewHolder.xp.setText(String.valueOf(st.info.xp));
        viewHolder.energy.setText(String.valueOf(st.info.energy));
        viewHolder.health.setText(String.valueOf(st.health));
        viewHolder.difficulty.setText(s.getDifficulty(st.info.diff));
        viewHolder.continueable.setText(st.non_con?activity.getString(R.string.stg_info_impo):activity.getString(R.string.stg_info_poss));
        viewHolder.length.setText(String.valueOf(st.len));
        viewHolder.maxenemy.setText(String.valueOf(st.max));
        viewHolder.music.setText(String.valueOf(st.mus0));
        viewHolder.castleperc.setText(viewHolder.castleperc.getText().toString().replace("??",String.valueOf(st.mush)));
        viewHolder.music2.setText(String.valueOf(st.mus1));
        viewHolder.background.setText(String.valueOf(st.bg));
        viewHolder.castle.setText(String.valueOf(st.castle));

        if(st.info.drop.length > 0) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            viewHolder.drop.setLayoutManager(linearLayoutManager);
            DropRecycle dropRecycle = new DropRecycle(st,activity);
            viewHolder.drop.setAdapter(dropRecycle);
            ViewCompat.setNestedScrollingEnabled(viewHolder.drop,false);
        } else {
            viewHolder.droprow.setVisibility(View.GONE);
            viewHolder.dropscroll.setVisibility(View.GONE);
        }

        if(st.info.time.length > 0) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            viewHolder.score.setLayoutManager(linearLayoutManager);
            ScoreRecycle scoreRecycle = new ScoreRecycle(st,activity);
            viewHolder.score.setAdapter(scoreRecycle);
            ViewCompat.setNestedScrollingEnabled(viewHolder.score,false);
        } else {
            viewHolder.scorerow.setVisibility(View.GONE);
            viewHolder.scorescroll.setVisibility(View.GONE);
        }

        if(st.info.drop.length <= 0 && st.info.time.length <= 0) {
            viewHolder.droptitle.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
