package com.mandarin.bcu.androidutil.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.getStrings;
import com.mandarin.bcu.util.Interpret;

import java.util.List;

import common.battle.Treasure;
import common.system.MultiLangCont;
import common.util.unit.Enemy;

public class EnemyRecycle extends RecyclerView.Adapter<EnemyRecycle.ViewHolder> {
    private final int id;
    private String [][] fragment = {{"Immune to "},{""}};
    private Activity activity;
    private int fs = 0;
    private int multi = 100;
    private getStrings s;

    public EnemyRecycle(Activity activity,int id) {
        this.activity = activity;
        this.id = id;
        s = new getStrings(activity);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(activity).inflate(R.layout.enemy_table,viewGroup,false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Enemy em = StaticStore.enemies.get(id);
        Treasure t = StaticStore.t;

        SharedPreferences shared = activity.getSharedPreferences("configuration", Context.MODE_PRIVATE);

        if(shared.getBoolean("frame",true)) {
            fs = 0;
            viewHolder.frse.setText(activity.getString(R.string.unit_info_fr));
        } else {
            fs = 1;
            viewHolder.frse.setText(activity.getString(R.string.unit_info_sec));
        }

        viewHolder.name.setText(MultiLangCont.ENAME.getCont(em));
        viewHolder.enemid.setText(s.number(id));
        float ratio = 32f/32f;
        viewHolder.enemicon.setImageBitmap(StaticStore.getResizeb(StaticStore.ebitmaps[id],activity,85f*ratio,32f*ratio));
        viewHolder.enemhp.setText(s.getHP(em,multi));
        viewHolder.enemhb.setText(s.getHB(em));
        viewHolder.enemmulti.setText(String.valueOf(multi));
        viewHolder.enematk.setText(s.getAtk(em,multi));
        viewHolder.enematktime.setText(s.getAtkTime(em,fs));
        viewHolder.enemabilt.setText(s.getAbilT(em));
        viewHolder.enempre.setText(s.getPre(em,fs));
        viewHolder.enempost.setText(s.getPost(em,fs));
        viewHolder.enemtba.setText(s.getTBA(em,fs));
        viewHolder.enemtrait.setText(s.getTrait(em));
        viewHolder.enematkt.setText(s.getSimu(em));
        viewHolder.enemdrop.setText(s.getDrop(em,t));
        viewHolder.enemrange.setText(s.getRange(em));
        viewHolder.enembarrier.setText(s.getBarrier(em));
        viewHolder.enemspd.setText(s.getSpd(em));

        String language = StaticStore.lang[shared.getInt("Language",0)];
        if(language.equals("")) {
            language = Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
        }
        List<String> proc;
        if(language.equals("ko")) {
            proc = Interpret.getProc(em.de,1,fs,activity);
        } else {
            proc = Interpret.getProc(em.de,0,fs,activity);
        }
        List<Integer> procicon = Interpret.getProcid(em.de);

        List<String>ability = Interpret.getAbi(em.de,fragment,StaticStore.addition,0);
        List<Integer>abilityicon = Interpret.getAbiid(em.de);

        if(ability.size()>0 || proc.size()>0) {
            viewHolder.none.setVisibility(View.GONE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            viewHolder.emabil.setLayoutManager(linearLayoutManager);
            AdapterAbil adapterAbil = new AdapterAbil(ability,proc,abilityicon,procicon,activity);
            viewHolder.emabil.setAdapter(adapterAbil);
            ViewCompat.setNestedScrollingEnabled(viewHolder.emabil,false);
        } else {
            viewHolder.emabil.setVisibility(View.GONE);
        }

        Listeners(viewHolder);
    }

    private void Listeners(ViewHolder viewHolder) {
        Enemy em = StaticStore.enemies.get(id);
        viewHolder.enemmulti.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(viewHolder.enemmulti.getText().toString().equals("")) {
                    multi = 100;
                    multiply(viewHolder,em);
                } else {
                    if(Double.parseDouble(viewHolder.enemmulti.getText().toString()) > Integer.MAX_VALUE)
                        multi = Integer.MAX_VALUE;
                    else
                        multi = Integer.valueOf(viewHolder.enemmulti.getText().toString());
                    multiply(viewHolder,em);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        viewHolder.frse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fs == 0) {
                    fs = 1;
                    retime(viewHolder,em);
                    viewHolder.frse.setText(activity.getString(R.string.unit_info_sec));
                } else {
                    fs = 0;
                    retime(viewHolder,em);
                    viewHolder.frse.setText(activity.getString(R.string.unit_info_fr));
                }
            }
        });

        viewHolder.enematkb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.enematk.getText().toString().equals(s.getAtk(em,multi))) {
                    viewHolder.enematk.setText(s.getDPS(em, multi));
                    viewHolder.enematkb.setText(activity.getString(R.string.unit_info_dps));
                } else {
                    viewHolder.enematk.setText(s.getAtk(em, multi));
                    viewHolder.enematkb.setText(activity.getString(R.string.unit_info_atk));
                }
            }
        });

        viewHolder.enempreb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.enempre.getText().toString().endsWith("f"))
                    viewHolder.enempre.setText(s.getPre(em,1));
                else
                    viewHolder.enempre.setText(s.getPre(em,0));
            }
        });

        viewHolder.enematktimeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.enematktime.getText().toString().endsWith("f"))
                    viewHolder.enematktime.setText(s.getAtkTime(em,1));
                else
                    viewHolder.enematktime.setText(s.getAtkTime(em,0));
            }
        });

        viewHolder.enempostb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.enempost.getText().toString().endsWith("f"))
                    viewHolder.enempost.setText(s.getPost(em,1));
                else
                    viewHolder.enempost.setText(s.getPost(em,0));
            }
        });

        viewHolder.enemtbab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.enemtba.getText().toString().endsWith("f"))
                    viewHolder.enemtba.setText(s.getTBA(em,1));
                else
                    viewHolder.enemtba.setText(s.getTBA(em,0));
            }
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        Button frse;
        TextView enemid;
        ImageView enemicon;
        TextView enemhp;
        TextView enemhb;
        EditText enemmulti;
        Button enematkb;
        TextView enematk;
        Button enematktimeb;
        TextView enematktime;
        TextView enemabilt;
        Button enempreb;
        TextView enempre;
        Button enempostb;
        TextView enempost;
        Button enemtbab;
        TextView enemtba;
        TextView enemtrait;
        TextView enematkt;
        TextView enemdrop;
        TextView enemrange;
        TextView enembarrier;
        TextView enemspd;
        TextView none;
        RecyclerView emabil;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.eneminfname);
            frse = itemView.findViewById(R.id.eneminffrse);
            enemid = itemView.findViewById(R.id.eneminfidr);
            enemicon = itemView.findViewById(R.id.eneminficon);
            enemhp = itemView.findViewById(R.id.eneminfhpr);
            enemhb = itemView.findViewById(R.id.eneminfhbr);
            enemmulti = itemView.findViewById(R.id.eneminfmultir);
            enematkb = itemView.findViewById(R.id.eneminfatk);
            enematk = itemView.findViewById(R.id.eneminfatkr);
            enematktimeb = itemView.findViewById(R.id.eneminfatktime);
            enematktime = itemView.findViewById(R.id.eneminfatktimer);
            enemabilt = itemView.findViewById(R.id.eneminfabiltr);
            enempreb = itemView.findViewById(R.id.eneminfpre);
            enempre = itemView.findViewById(R.id.eneminfprer);
            enempostb = itemView.findViewById(R.id.eneminfpost);
            enempost = itemView.findViewById(R.id.eneminfpostr);
            enemtbab = itemView.findViewById(R.id.eneminftba);
            enemtba = itemView.findViewById(R.id.eneminftbar);
            enemtrait = itemView.findViewById(R.id.eneminftraitr);
            enematkt = itemView.findViewById(R.id.eneminfatktr);
            enemdrop = itemView.findViewById(R.id.eneminfdropr);
            enemrange = itemView.findViewById(R.id.eneminfranger);
            enembarrier = itemView.findViewById(R.id.eneminfbarrierr);
            enemspd = itemView.findViewById(R.id.eneminfspdr);
            none = itemView.findViewById(R.id.eneminfnone);
            emabil = itemView.findViewById(R.id.eneminfabillist);
        }
    }

    private void multiply(ViewHolder viewHolder,Enemy em) {
        viewHolder.enemhp.setText(s.getHP(em,multi));
        viewHolder.enematk.setText(s.getAtk(em,multi));
    }

    private void retime(ViewHolder viewHolder,Enemy em) {
        viewHolder.enematktime.setText(s.getAtkTime(em,fs));
        viewHolder.enempre.setText(s.getPre(em,fs));
        viewHolder.enempost.setText(s.getPost(em,fs));
        viewHolder.enemtba.setText(s.getTBA(em,fs));
        SharedPreferences shared = activity.getSharedPreferences("configuration", Context.MODE_PRIVATE);

        String language = StaticStore.lang[shared.getInt("Language",0)];
        if(language.equals("")) {
            language = Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
        }
        List<String> proc;
        if(language.equals("ko")) {
            proc = Interpret.getProc(em.de,1,fs,activity);
        } else {
            proc = Interpret.getProc(em.de,0,fs,activity);
        }
        List<Integer> procicon = Interpret.getProcid(em.de);

        List<String>ability = Interpret.getAbi(em.de,fragment,StaticStore.addition,0);
        List<Integer>abilityicon = Interpret.getAbiid(em.de);

        if(ability.size()>0 || proc.size()>0) {
            viewHolder.none.setVisibility(View.GONE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            viewHolder.emabil.setLayoutManager(linearLayoutManager);
            AdapterAbil adapterAbil = new AdapterAbil(ability, proc, abilityicon, procicon, activity);
            viewHolder.emabil.setAdapter(adapterAbil);
            ViewCompat.setNestedScrollingEnabled(viewHolder.emabil, false);
        }
    }
}
