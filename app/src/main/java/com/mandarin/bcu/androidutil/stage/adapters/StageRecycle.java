package com.mandarin.bcu.androidutil.stage.adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
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

import com.mandarin.bcu.ImageViewer;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.getStrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import common.battle.Treasure;
import common.util.stage.Limit;
import common.util.stage.MapColc;
import common.util.stage.Stage;
import common.util.stage.StageMap;

public class StageRecycle extends RecyclerView.Adapter<StageRecycle.ViewHolder> {
    private Activity activity;
    private final int mapcode;
    private final int stid;
    private final int posit;
    private final getStrings s;
    private final int [] CASTLES = {45,44,43,42,41,40,39,38,37,36,35,34,32,31,30,29,28,27,26,25,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1,0,46,47,45,47,47,45,45};

    private List<Integer> wc = Arrays.asList(3,4,5,10,12);
    private List<Integer> ec = Arrays.asList(0,1,2,9);
    private List<Integer> sc = Arrays.asList(6,7,8);

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
        TextView limitNone;
        RecyclerView limitrec;
        NestedScrollView limitscroll;

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
            limitNone = itemView.findViewById(R.id.stginfononer);
            limitrec = itemView.findViewById(R.id.stginfolimitrec);
            limitscroll = itemView.findViewById(R.id.limitscroll);
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
        Treasure t = StaticStore.t;

        MapColc mc = StaticStore.map.get(mapcode);

        if (mc == null) return;

        if (stid >= mc.maps.length || stid < 0) return;

        StageMap stm = mc.maps[stid];

        if (stm == null) return;

        if (posit >= stm.list.size() || posit < 0) return;

        Stage st = stm.list.get(posit);

        viewHolder.id.setText(s.getID(mapcode, stid, posit));

        List<String> stars = new ArrayList<>();

        for (int k = 0; k < stm.stars.length; k++) {
            String s = k + 1 + " (" + stm.stars[k] + " %)";
            stars.add(s);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, R.layout.spinneradapter, stars);

        viewHolder.star.setAdapter(arrayAdapter);

        viewHolder.star.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RecyclerView enrec = activity.findViewById(R.id.stginfoenrec);
                enrec.setLayoutManager(new LinearLayoutManager(activity));
                ViewCompat.setNestedScrollingEnabled(enrec, false);

                EnemyListRecycle listRecycle = new EnemyListRecycle(activity, st, stm.stars[position]);

                enrec.setAdapter(listRecycle);

                StaticStore.stageSpinner = position;

                Limit l = st.getLim(position);

                if(none(l)) {
                    viewHolder.limitNone.setVisibility(View.VISIBLE);
                    viewHolder.limitscroll.setVisibility(View.GONE);
                } else {
                    viewHolder.limitscroll.setVisibility(View.VISIBLE);
                    viewHolder.limitNone.setVisibility(View.GONE);

                    if(posit == l.sid || l.sid == -1) {
                        if(viewHolder.star.getSelectedItemPosition() == l.star || l.star == -1) {
                            viewHolder.limitrec.setLayoutManager(new LinearLayoutManager(activity));
                            ViewCompat.setNestedScrollingEnabled(viewHolder.limitrec,false);
                            LimitRecycle limitRecycle = new LimitRecycle(activity,l);

                            viewHolder.limitrec.setAdapter(limitRecycle);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (StaticStore.stageSpinner != -1) {
            viewHolder.star.setSelection(StaticStore.stageSpinner);
        }

        if(st.info != null) {
            if (mapcode == 0 || mapcode == 13)
                viewHolder.xp.setText(s.getXP(st.info.xp, t, true));
            else
                viewHolder.xp.setText(s.getXP(st.info.xp, t, false));
        } else {
            viewHolder.xp.setText("0");
        }

        if(st.info != null)
            viewHolder.energy.setText(String.valueOf(st.info.energy));
        else
            viewHolder.energy.setText("0");

        viewHolder.health.setText(String.valueOf(st.health));

        if(st.info != null)
            viewHolder.difficulty.setText(s.getDifficulty(st.info.diff));
        else
            viewHolder.difficulty.setText(R.string.unit_info_t_none);

        viewHolder.continueable.setText(st.non_con ? activity.getString(R.string.stg_info_impo) : activity.getString(R.string.stg_info_poss));
        viewHolder.length.setText(String.valueOf(st.len));
        viewHolder.maxenemy.setText(String.valueOf(st.max));
        viewHolder.music.setText(String.valueOf(st.mus0));
        viewHolder.castleperc.setText(viewHolder.castleperc.getText().toString().replace("??", String.valueOf(st.mush)));
        viewHolder.music2.setText(String.valueOf(st.mus1));
        viewHolder.background.setText(String.valueOf(st.bg));

        viewHolder.background.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(activity, ImageViewer.class);
                intent.putExtra("Path", Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.mandarin.BCU/files/org/img/bg/bg" + number(st.bg) + ".png");
                intent.putExtra("Img", 0);
                intent.putExtra("BGNum", st.bg);

                activity.startActivity(intent);
            }
        });

        viewHolder.castle.setText(String.valueOf(st.castle));

        viewHolder.castle.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                if (mapcode == 3 && stid == 11) return;

                if (mapcode == 3) {
                    if (ec.contains(stid)) {
                        String path = "./org/img/ec/ec" + number(CASTLES[posit]) + ".png";

                        Intent intent = new Intent(activity, ImageViewer.class);
                        intent.putExtra("Path", path);
                        intent.putExtra("Img", 1);

                        activity.startActivity(intent);
                    } else if (wc.contains(stid)) {
                        String path = "./org/img/wc/wc" + number(CASTLES[posit]) + ".png";

                        Intent intent = new Intent(activity, ImageViewer.class);
                        intent.putExtra("Path", path);
                        intent.putExtra("Img", 1);

                        activity.startActivity(intent);
                    } else if (sc.contains(stid)) {
                        String path = "./org/img/sc/sc" + number(CASTLES[posit]) + ".png";

                        Intent intent = new Intent(activity, ImageViewer.class);
                        intent.putExtra("Path", path);
                        intent.putExtra("Img", 1);

                        activity.startActivity(intent);
                    }
                } else {
                    String path = "./org/img/rc/rc" + number(st.castle) + ".png";

                    Intent intent = new Intent(activity, ImageViewer.class);
                    intent.putExtra("Path", path);
                    intent.putExtra("Img", 1);

                    activity.startActivity(intent);
                }
            }
        });

        if(st.info != null) {
            if (st.info.drop.length > 0) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                viewHolder.drop.setLayoutManager(linearLayoutManager);
                DropRecycle dropRecycle = new DropRecycle(st, activity);
                viewHolder.drop.setAdapter(dropRecycle);
                ViewCompat.setNestedScrollingEnabled(viewHolder.drop, false);
            } else {
                viewHolder.droprow.setVisibility(View.GONE);
                viewHolder.dropscroll.setVisibility(View.GONE);
            }

            if (st.info.time.length > 0) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                viewHolder.score.setLayoutManager(linearLayoutManager);
                ScoreRecycle scoreRecycle = new ScoreRecycle(st, activity);
                viewHolder.score.setAdapter(scoreRecycle);
                ViewCompat.setNestedScrollingEnabled(viewHolder.score, false);
            } else {
                viewHolder.scorerow.setVisibility(View.GONE);
                viewHolder.scorescroll.setVisibility(View.GONE);
            }

            if (st.info.drop.length <= 0 && st.info.time.length <= 0) {
                viewHolder.droptitle.setVisibility(View.GONE);
            }
        } else {
            viewHolder.droprow.setVisibility(View.GONE);
            viewHolder.dropscroll.setVisibility(View.GONE);
            viewHolder.scorerow.setVisibility(View.GONE);
            viewHolder.scorescroll.setVisibility(View.GONE);
            viewHolder.droptitle.setVisibility(View.GONE);
        }

        Limit l = st.getLim(viewHolder.star.getSelectedItemPosition());

        if(none(l)) {
            viewHolder.limitscroll.setVisibility(View.GONE);
            viewHolder.limitNone.setVisibility(View.VISIBLE);
        } else {
            viewHolder.limitscroll.setVisibility(View.VISIBLE);
            viewHolder.limitNone.setVisibility(View.GONE);

            if(posit == l.sid || l.sid == -1) {
                if(viewHolder.star.getSelectedItemPosition() == l.star || l.star == -1) {
                    viewHolder.limitrec.setLayoutManager(new LinearLayoutManager(activity));
                    ViewCompat.setNestedScrollingEnabled(viewHolder.limitrec,false);
                    LimitRecycle limitRecycle = new LimitRecycle(activity,l);

                    viewHolder.limitrec.setAdapter(limitRecycle);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    private String number(int n) {
        if(0 <= n && n < 10) {
            return "00"+n;
        } else if(10 <= n && n < 99) {
            return "0"+n;
        } else {
            return String.valueOf(n);
        }
    }

    private boolean none(Limit l) {
        if(l == null) return true;

        boolean b0 = l.line == 0;
        boolean b1 = l.min == 0;
        boolean b2 = l.max == 0;
        boolean b3 = l.group == null;
        boolean b4 = l.num == 0;
        boolean b5 = l.rare == 0;

        return b0 && b1 && b2 && b3 && b4 && b5;
    }
}
