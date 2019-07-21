package com.mandarin.bcu.androidutil.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
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
import java.util.Objects;

import common.battle.BasisSet;
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
    private int[][] states = new int[][] {
            new int[] {android.R.attr.state_enabled}
    };
    private int[] color;

    public EnemyRecycle(Activity activity,int id) {
        this.activity = activity;
        this.id = id;
        s = new getStrings(activity);
        color = new int[] {
                getAttributeColor(activity,R.attr.TextPrimary)
        };
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

        TextInputLayout aclev = activity.findViewById(R.id.aclev);
        TextInputLayout actrea = activity.findViewById(R.id.actrea);
        TextInputLayout itfcry = activity.findViewById(R.id.itfcrytrea);
        TextInputLayout cotccry = activity.findViewById(R.id.cotccrytrea);
        TextInputLayout[] godmask = {activity.findViewById(R.id.godmask),activity.findViewById(R.id.godmask1),activity.findViewById(R.id.godmask2)};
        TextInputEditText aclevt = activity.findViewById(R.id.aclevt);
        TextInputEditText actreat = activity.findViewById(R.id.actreat);
        TextInputEditText itfcryt = activity.findViewById(R.id.itfcrytreat);
        TextInputEditText cotccryt = activity.findViewById(R.id.cotccrytreat);
        TextInputEditText[] godmaskt = {activity.findViewById(R.id.godmaskt),activity.findViewById(R.id.godmaskt1),activity.findViewById(R.id.godmaskt2)};

        aclev.setCounterEnabled(true);
        aclev.setCounterMaxLength(2);
        aclev.setHelperTextColor(new ColorStateList(states,color));

        actrea.setCounterEnabled(true);
        actrea.setCounterMaxLength(3);
        actrea.setHelperTextColor(new ColorStateList(states,color));

        itfcry.setCounterEnabled(true);
        itfcry.setCounterMaxLength(3);
        itfcry.setHelperTextColor(new ColorStateList(states,color));

        cotccry.setCounterEnabled(true);
        cotccry.setCounterMaxLength(4);
        cotccry.setHelperTextColor(new ColorStateList(states,color));

        for(TextInputLayout til : godmask) {
            til.setCounterEnabled(true);
            til.setCounterMaxLength(3);
            til.setHelperTextColor(new ColorStateList(states,color));
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

        aclevt.setText(String.valueOf(t.tech[1]));
        actreat.setText(String.valueOf(t.trea[3]));
        itfcryt.setText(String.valueOf(t.alien));
        cotccryt.setText(String.valueOf(t.star));
        for(int j = 0;j < godmaskt.length;j++)
            godmaskt[j].setText(String.valueOf(t.gods[j]));

        Listeners(viewHolder);
    }

    private void Listeners(ViewHolder viewHolder) {
        Enemy em = StaticStore.enemies.get(id);

        if(activity == null) return;

        Treasure t = BasisSet.current.t();

        TextInputLayout aclev = activity.findViewById(R.id.aclev);
        TextInputLayout actrea = activity.findViewById(R.id.actrea);
        TextInputLayout itfcry = activity.findViewById(R.id.itfcrytrea);
        TextInputLayout cotccry = activity.findViewById(R.id.cotccrytrea);
        TextInputLayout[] godmask = {activity.findViewById(R.id.godmask),activity.findViewById(R.id.godmask1),activity.findViewById(R.id.godmask2)};
        TextInputEditText aclevt = activity.findViewById(R.id.aclevt);
        TextInputEditText actreat = activity.findViewById(R.id.actreat);
        TextInputEditText itfcryt = activity.findViewById(R.id.itfcrytreat);
        TextInputEditText cotccryt = activity.findViewById(R.id.cotccrytreat);
        TextInputEditText[] godmaskt = {activity.findViewById(R.id.godmaskt),activity.findViewById(R.id.godmaskt1),activity.findViewById(R.id.godmaskt2)};

        Button reset = activity.findViewById(R.id.enemtreareset);

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

        aclevt.setSelection(Objects.requireNonNull(aclevt.getText()).length());
        actreat.setSelection(Objects.requireNonNull(actreat.getText()).length());
        itfcryt.setSelection(Objects.requireNonNull(itfcryt.getText()).length());
        cotccryt.setSelection(Objects.requireNonNull(cotccryt.getText()).length());
        for(TextInputEditText tiet : godmaskt)
            tiet.setSelection(Objects.requireNonNull(tiet.getText()).length());

        aclevt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) > 30 || Integer.parseInt(s.toString()) <= 0) {
                        if(aclev.isHelperTextEnabled()) {
                            aclev.setHelperTextEnabled(false);
                            aclev.setErrorEnabled(true);
                            aclev.setError(activity.getString(R.string.treasure_invalid));
                        }
                    } else {
                        if(aclev.isErrorEnabled()) {
                            aclev.setError(null);
                            aclev.setErrorEnabled(false);
                            aclev.setHelperTextEnabled(true);
                            aclev.setHelperTextColor(new ColorStateList(states,color));
                            aclev.setHelperText("1~30");
                        }
                    }
                } else {
                    if(aclev.isErrorEnabled()) {
                        aclev.setError(null);
                        aclev.setErrorEnabled(false);
                        aclev.setHelperTextEnabled(true);
                        aclev.setHelperTextColor(new ColorStateList(states,color));
                        aclev.setHelperText("1~30");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if(!text.toString().isEmpty()) {
                    if(Integer.parseInt(text.toString()) <= 30 && Integer.parseInt(text.toString()) > 0) {
                        int lev = Integer.parseInt(text.toString());

                        t.tech[1] = lev;

                        viewHolder.enemdrop.setText(s.getDrop(em,t));
                    }
                } else {
                    t.tech[1] = 1;

                    viewHolder.enemdrop.setText(s.getDrop(em,t));
                }
            }
        });

        actreat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) > 300) {
                        if(actrea.isHelperTextEnabled()) {
                            actrea.setHelperTextEnabled(false);
                            actrea.setErrorEnabled(true);
                            actrea.setError(activity.getString(R.string.treasure_invalid));
                        }
                    } else {
                        if(actrea.isErrorEnabled()) {
                            actrea.setError(null);
                            actrea.setErrorEnabled(false);
                            actrea.setHelperTextEnabled(true);
                            actrea.setHelperTextColor(new ColorStateList(states,color));
                            actrea.setHelperText("0~300");
                        }
                    }
                } else {
                    if(actrea.isErrorEnabled()) {
                        actrea.setError(null);
                        actrea.setErrorEnabled(false);
                        actrea.setHelperTextEnabled(true);
                        actrea.setHelperTextColor(new ColorStateList(states,color));
                        actrea.setHelperText("0~300");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if(!text.toString().isEmpty()) {
                    if (Integer.parseInt(text.toString()) <= 300) {
                        int trea = Integer.parseInt(text.toString());

                        t.trea[3] = trea;

                        viewHolder.enemdrop.setText(s.getDrop(em,t));
                    }
                } else {
                    t.trea[3] = 0;

                    viewHolder.enemdrop.setText(s.getDrop(em,t));
                }
            }
        });

        itfcryt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) > 600) {
                        if(itfcry.isHelperTextEnabled()) {
                            itfcry.setHelperTextEnabled(false);
                            itfcry.setErrorEnabled(true);
                            itfcry.setError(activity.getString(R.string.treasure_invalid));
                        }
                    } else {
                        if(itfcry.isErrorEnabled()) {
                            itfcry.setError(null);
                            itfcry.setErrorEnabled(false);
                            itfcry.setHelperTextEnabled(true);
                            itfcry.setHelperTextColor(new ColorStateList(states,color));
                            itfcry.setHelperText("0~600");
                        }
                    }
                } else {
                    if(itfcry.isErrorEnabled()) {
                        itfcry.setError(null);
                        itfcry.setErrorEnabled(false);
                        itfcry.setHelperTextEnabled(true);
                        itfcry.setHelperTextColor(new ColorStateList(states,color));
                        itfcry.setHelperText("0~600");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if(!text.toString().isEmpty()) {
                    if (Integer.parseInt(text.toString()) <= 600) {

                        t.alien = Integer.parseInt(text.toString());

                        viewHolder.enemhp.setText(s.getHP(em,multi));

                        if(viewHolder.enematkb.getText().toString().equals(activity.getString(R.string.unit_info_dps))) {
                            viewHolder.enematk.setText(s.getDPS(em,multi));
                        } else {
                            viewHolder.enematk.setText(s.getAtk(em,multi));
                        }
                    }
                } else {
                    t.alien = 0;

                    viewHolder.enemhp.setText(s.getHP(em,multi));

                    if(viewHolder.enematkb.getText().toString().equals(activity.getString(R.string.unit_info_dps))) {
                        viewHolder.enematk.setText(s.getDPS(em,multi));
                    } else {
                        viewHolder.enematk.setText(s.getAtk(em,multi));
                    }
                }
            }
        });

        cotccryt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) > 1500) {
                        if(cotccry.isHelperTextEnabled()) {
                            cotccry.setHelperTextEnabled(false);
                            cotccry.setErrorEnabled(true);
                            cotccry.setError(activity.getString(R.string.treasure_invalid));
                        }
                    } else {
                        if(cotccry.isErrorEnabled()) {
                            cotccry.setError(null);
                            cotccry.setErrorEnabled(false);
                            cotccry.setHelperTextEnabled(true);
                            cotccry.setHelperTextColor(new ColorStateList(states,color));
                            cotccry.setHelperText("0~1500");
                        }
                    }
                } else {
                    if(cotccry.isErrorEnabled()) {
                        cotccry.setError(null);
                        cotccry.setErrorEnabled(false);
                        cotccry.setHelperTextEnabled(true);
                        cotccry.setHelperTextColor(new ColorStateList(states,color));
                        cotccry.setHelperText("0~1500");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if(!text.toString().isEmpty()) {
                    if (Integer.parseInt(text.toString()) <= 1500) {

                        t.star = Integer.parseInt(text.toString());

                        viewHolder.enemhp.setText(s.getHP(em,multi));

                        if(viewHolder.enematkb.getText().toString().equals(activity.getString(R.string.unit_info_dps))) {
                            viewHolder.enematk.setText(s.getDPS(em,multi));
                        } else {
                            viewHolder.enematk.setText(s.getAtk(em,multi));
                        }
                    }
                } else {
                    t.star = 0;

                    viewHolder.enemhp.setText(s.getHP(em,multi));

                    if(viewHolder.enematkb.getText().toString().equals(activity.getString(R.string.unit_info_dps))) {
                        viewHolder.enematk.setText(s.getDPS(em,multi));
                    } else {
                        viewHolder.enematk.setText(s.getAtk(em,multi));
                    }
                }
            }
        });

        for(int i = 0;i< godmaskt.length;i++) {
            final int finall = i;
            godmaskt[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(!s.toString().isEmpty()) {
                        if (Integer.parseInt(s.toString()) > 100) {
                            if(godmask[finall].isHelperTextEnabled()) {
                                godmask[finall].setHelperTextEnabled(false);
                                godmask[finall].setErrorEnabled(true);
                                godmask[finall].setError(activity.getString(R.string.treasure_invalid));
                            }
                        } else {
                            if(godmask[finall].isErrorEnabled()) {
                                godmask[finall].setError(null);
                                godmask[finall].setErrorEnabled(false);
                                godmask[finall].setHelperTextEnabled(true);
                                godmask[finall].setHelperTextColor(new ColorStateList(states,color));
                                godmask[finall].setHelperText("0~100");
                            }
                        }
                    } else {
                        if(godmask[finall].isErrorEnabled()) {
                            godmask[finall].setError(null);
                            godmask[finall].setErrorEnabled(false);
                            godmask[finall].setHelperTextEnabled(true);
                            godmask[finall].setHelperTextColor(new ColorStateList(states,color));
                            godmask[finall].setHelperText("0~100");
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable text) {
                    if(!text.toString().isEmpty()) {
                        if (Integer.parseInt(text.toString()) <= 100) {

                            t.gods[finall] = Integer.parseInt(text.toString());

                            viewHolder.enemhp.setText(s.getHP(em,multi));

                            if(viewHolder.enematkb.getText().toString().equals(activity.getString(R.string.unit_info_dps))) {
                                viewHolder.enematk.setText(s.getDPS(em,multi));
                            } else {
                                viewHolder.enematk.setText(s.getAtk(em,multi));
                            }
                        }
                    } else {
                        t.gods[finall] = 0;

                        viewHolder.enemhp.setText(s.getHP(em,multi));

                        if(viewHolder.enematkb.getText().toString().equals(activity.getString(R.string.unit_info_dps))) {
                            viewHolder.enematk.setText(s.getDPS(em,multi));
                        } else {
                            viewHolder.enematk.setText(s.getAtk(em,multi));
                        }
                    }
                }
            });
        }

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t.tech[1] = 30;
                t.trea[3] = 300;
                t.alien = 600;
                t.star = 1500;
                for(int i = 0; i < t.gods.length;i++)
                    t.gods[i] = 100;

                aclevt.setText(String.valueOf(t.tech[1]));
                actreat.setText(String.valueOf(t.trea[3]));
                itfcryt.setText(String.valueOf(t.alien));
                cotccryt.setText(String.valueOf(t.star));
                for(int i = 0;i < t.gods.length;i++)
                    godmaskt[i].setText(String.valueOf(t.gods[i]));

                viewHolder.enemhp.setText(s.getHP(em,multi));

                if(viewHolder.enematkb.getText().toString().equals(activity.getString(R.string.unit_info_dps))) {
                    viewHolder.enematk.setText(s.getDPS(em,multi));
                } else {
                    viewHolder.enematk.setText(s.getAtk(em,multi));
                }

                viewHolder.enemdrop.setText(s.getDrop(em,t));
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

    private static int getAttributeColor(Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = ContextCompat.getColor(context,colorRes);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return color;
    }
}
