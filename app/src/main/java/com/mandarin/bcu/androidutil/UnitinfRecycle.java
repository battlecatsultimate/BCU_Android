package com.mandarin.bcu.androidutil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.util.Interpret;
import com.mandarin.bcu.util.basis.BasisSet;
import com.mandarin.bcu.util.basis.Treasure;
import com.mandarin.bcu.util.unit.EForm;
import com.mandarin.bcu.util.unit.Form;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class UnitinfRecycle extends RecyclerView.Adapter<UnitinfRecycle.ViewHolder> {
    private Activity context;
    private final ArrayList<String> names;
    private final Form[] forms;
    private final int id;
    private int fs = 0;
    private getStrings s;
    private String [][] fragment = {{"Immune to "},{""}};
    private int[][] states = new int[][] {
            new int[] {android.R.attr.state_enabled}
    };

    private int[] color;


    public UnitinfRecycle(Activity context, ArrayList<String> names, Form[] forms, int id) {
        this.context = context;
        this.names = names;
        this.forms = forms;
        this.id = id;
        s = new getStrings(this.context);
        color = new int[] {
                getAttributeColor(context,R.attr.TextPrimary)
        };

        if(StaticStore.addition == null) {
            int[] addid = {R.string.unit_info_strong, R.string.unit_info_resis, R.string.unit_info_masdam, R.string.unit_info_exmon, R.string.unit_info_atkbs, R.string.unit_info_wkill, R.string.unit_info_evakill, R.string.unit_info_insres, R.string.unit_info_insmas};
            StaticStore.addition = new String[addid.length];
            for (int i = 0; i < addid.length; i++)
                StaticStore.addition[i] = context.getString(addid[i]);
        }

        if(StaticStore.icons == null) {
            int[] abiconid = {R.mipmap.ic_strong, R.mipmap.ic_resist, R.mipmap.ic_md, R.mipmap.ic_target, R.mipmap.ic_em, R.mipmap.ic_cb, R.mipmap.ic_met, R.mipmap.ic_white, R.mipmap.ic_imws, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_zk,
                    R.mipmap.ic_imwk, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_eva, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_inr, R.mipmap.ic_ind};
            StaticStore.icons = new Drawable[abiconid.length];
            for (int i = 0; i < abiconid.length; i++)
                StaticStore.icons[i] = context.getDrawable(abiconid[i]);
        }

        if(StaticStore.picons == null) {
            int[] proiconid = {R.mipmap.ic_kb, R.mipmap.ic_freeze, R.mipmap.ic_slow, R.mipmap.ic_critical, R.mipmap.ic_wv, R.mipmap.ic_weaken, R.mipmap.ic_bb, R.mipmap.ic_wa, R.mipmap.ic_white, R.mipmap.ic_stren, R.mipmap.ic_survive, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_imkb,
                    R.mipmap.ic_imfr, R.mipmap.ic_imsl, R.mipmap.ic_imwv, R.mipmap.ic_imwe, R.mipmap.ic_imwa, R.mipmap.ic_imcu, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white};
            StaticStore.picons = new Drawable[proiconid.length];

            for (int i = 0; i < proiconid.length; i++)
                StaticStore.picons[i] = context.getDrawable(proiconid[i]);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(context).inflate(R.layout.unit_table,viewGroup,false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        TextInputLayout cdlev;
        TextInputLayout cdtrea;
        TextInputLayout atktrea;
        TextInputLayout healtrea;


        cdlev = context.findViewById(R.id.cdlev);
        cdtrea = context.findViewById(R.id.cdtrea);
        atktrea = context.findViewById(R.id.atktrea);
        healtrea = context.findViewById(R.id.healtrea);

        cdlev.setCounterEnabled(true);
        cdlev.setCounterMaxLength(2);

        cdtrea.setCounterEnabled(true);
        cdtrea.setCounterMaxLength(3);

        atktrea.setCounterEnabled(true);
        atktrea.setCounterMaxLength(3);

        healtrea.setCounterEnabled(true);
        healtrea.setCounterMaxLength(3);

        cdlev.setHelperTextColor(new ColorStateList(states,color));
        cdtrea.setHelperTextColor(new ColorStateList(states,color));
        atktrea.setHelperTextColor(new ColorStateList(states,color));
        healtrea.setHelperTextColor(new ColorStateList(states,color));

        SharedPreferences shared = context.getSharedPreferences("configuration", Context.MODE_PRIVATE);

        if(shared.getBoolean("frame",true)) {
            fs = 0;
            viewHolder.frse.setText(context.getString(R.string.unit_info_fr));
        } else {
            fs = 1;
            viewHolder.frse.setText(context.getString(R.string.unit_info_sec));
        }

        Treasure t = BasisSet.current.t();
        Form f = forms[viewHolder.getAdapterPosition()];
        EForm ef = new EForm(f,f.unit.getPrefLvs());
        List<String> ability = Interpret.getAbi(f.du,fragment,StaticStore.addition,0);
        List<Integer> abilityicon = Interpret.getAbiid(f.du);

        TextInputEditText cdlevt = context.findViewById(R.id.cdlevt);
        TextInputEditText cdtreat = context.findViewById(R.id.cdtreat);
        TextInputEditText atktreat = context.findViewById(R.id.atktreat);
        TextInputEditText healtreat = context.findViewById(R.id.healtreat);

        cdlevt.setText(String.valueOf(t.tech[0]));
        cdtreat.setText(String.valueOf(t.trea[2]));
        atktreat.setText(String.valueOf(t.trea[0]));
        healtreat.setText(String.valueOf(t.trea[1]));


        String language = Locale.getDefault().getLanguage();
        List<String> proc;
        if(language.equals("ko")) {
            proc = Interpret.getProc(f.du,1,fs);
        } else {
            proc = Interpret.getProc(f.du,0,fs);
        }
        List<Integer> procicon = Interpret.getProcid(f.du);

        viewHolder.uniticon.setImageBitmap(StaticStore.getResizeb(f.anim.uni.getIcon(),context,48));
        viewHolder.unitname.setText(names.get(i));
        viewHolder.unitid.setText(s.getID(f,viewHolder,number(id)));
        viewHolder.unithp.setText(s.getHP(f,t,f.unit.getPrefLv()));
        viewHolder.unithb.setText(s.getHB(f));
        viewHolder.unitatk.setText(s.getTotAtk(f,t,f.unit.getPrefLv()));
        viewHolder.unittrait.setText(s.getTrait(ef));
        viewHolder.unitcost.setText(s.getCost(f));
        viewHolder.unitsimu.setText(s.getSimu(f));
        viewHolder.unitspd.setText(s.getSpd(f));
        viewHolder.unitcd.setText(s.getCD(f, t, fs));
        viewHolder.unitrang.setText(s.getRange(f));
        viewHolder.unitpreatk.setText(s.getPre(f, fs));
        viewHolder.unitpost.setText(s.getPost(f, fs));
        viewHolder.unittba.setText(s.getTBA(f, fs));
        viewHolder.unitatkt.setText(s.getAtkTime(f, fs));
        viewHolder.unitabilt.setText(s.getAbilT(f));

        if(ability.size()>0 || proc.size() > 0) {
            viewHolder.none.setVisibility(View.GONE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            viewHolder.unitabil.setLayoutManager(linearLayoutManager);
            AdapterAbil adapterAbil = new AdapterAbil(ability, proc,abilityicon,procicon);
            viewHolder.unitabil.setAdapter(adapterAbil);
            ViewCompat.setNestedScrollingEnabled(viewHolder.unitabil, false);
        } else {
            viewHolder.unitabil.setVisibility(View.GONE);
        }

        Listeners(viewHolder);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void Listeners(ViewHolder viewHolder) {
        TextInputLayout cdlev = context.findViewById(R.id.cdlev);
        TextInputLayout cdtrea = context.findViewById(R.id.cdtrea);
        TextInputLayout atktrea = context.findViewById(R.id.atktrea);
        TextInputLayout healtrea = context.findViewById(R.id.healtrea);
        TextInputEditText cdlevt = context.findViewById(R.id.cdlevt);
        TextInputEditText cdtreat = context.findViewById(R.id.cdtreat);
        TextInputEditText atktreat = context.findViewById(R.id.atktreat);
        TextInputEditText healtreat = context.findViewById(R.id.healtreat);
        Button reset = context.findViewById(R.id.treasurereset);

        Treasure t = BasisSet.current.t();
        Form f = forms[viewHolder.getAdapterPosition()];

        List<Integer> levels = new ArrayList<>();
        for(int j =1;j < f.unit.max+1;j++)
            levels.add(j);

        ArrayList<Integer> levelsp = new ArrayList<>();
        for(int j=0;j<f.unit.maxp+1;j++)
            levelsp.add(j);

        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<>(context, R.layout.spinneradapter, levels);
        ArrayAdapter<Integer> arrayAdapterp = new ArrayAdapter<>(context, R.layout.spinneradapter, levelsp);

        int currentlev;

        SharedPreferences shared = context.getSharedPreferences("configuration", Context.MODE_PRIVATE);

        if(shared.getInt("default_level",50) > f.unit.max)
            currentlev = f.unit.max;
        else
            if(f.unit.rarity != 0)
                currentlev = shared.getInt("default_level",50);
            else
                currentlev = f.unit.max;

        viewHolder.unitlevel.setAdapter(arrayAdapter);
        viewHolder.unitlevel.setSelection(getIndex(viewHolder.unitlevel,currentlev));
        viewHolder.unitlevelp.setAdapter(arrayAdapterp);

        if(f.unit.getPrefLv()-f.unit.max < 0) {
            viewHolder.unitlevelp.setSelection(getIndex(viewHolder.unitlevelp,0));
        } else {
            viewHolder.unitlevelp.setSelection(getIndex(viewHolder.unitlevelp, f.unit.getPrefLv() - f.unit.max));
        }

        if(levelsp.size() == 1) {
            viewHolder.unitlevelp.setVisibility(View.GONE);
            viewHolder.unitplus.setVisibility(View.GONE);
        }

        viewHolder.frse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fs == 0) {
                    fs = 1;
                    viewHolder.unitcd.setText(s.getCD(f,t,fs));
                    viewHolder.unitpreatk.setText(s.getPre(f,fs));
                    viewHolder.unitpost.setText(s.getPost(f,fs));
                    viewHolder.unittba.setText(s.getTBA(f,fs));
                    viewHolder.unitatkt.setText(s.getAtkTime(f,fs));
                    viewHolder.frse.setText(context.getString(R.string.unit_info_sec));

                    if(viewHolder.unitabil.getVisibility() != View.GONE) {

                        List<String> ability = Interpret.getAbi(f.du, fragment, StaticStore.addition, 0);
                        List<Integer> abilityicon = Interpret.getAbiid(f.du);

                        String language = Locale.getDefault().getLanguage();
                        List<String> proc;
                        if (language.equals("ko"))
                            proc = Interpret.getProc(f.du, 1, fs);
                        else
                            proc = Interpret.getProc(f.du, 0, fs);
                        List<Integer> procicon = Interpret.getProcid(f.du);

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        viewHolder.unitabil.setLayoutManager(linearLayoutManager);
                        AdapterAbil adapterAbil = new AdapterAbil(ability, proc,abilityicon,procicon);
                        viewHolder.unitabil.setAdapter(adapterAbil);
                        ViewCompat.setNestedScrollingEnabled(viewHolder.unitabil, false);
                    }
                } else {
                    fs = 0;
                    viewHolder.unitcd.setText(s.getCD(f,t,fs));
                    viewHolder.unitpreatk.setText(s.getPre(f,fs));
                    viewHolder.unitpost.setText(s.getPost(f,fs));
                    viewHolder.unittba.setText(s.getTBA(f,fs));
                    viewHolder.unitatkt.setText(s.getAtkTime(f,fs));
                    viewHolder.frse.setText(context.getString(R.string.unit_info_fr));

                    if(viewHolder.unitabil.getVisibility() != View.GONE) {

                        List<String> ability = Interpret.getAbi(f.du, fragment, StaticStore.addition, 0);
                        List<Integer> abilityicon = Interpret.getAbiid(f.du);

                        String language = Locale.getDefault().getLanguage();
                        List<String> proc;
                        if (language.equals("ko"))
                            proc = Interpret.getProc(f.du, 1, fs);
                        else
                            proc = Interpret.getProc(f.du, 0, fs);
                        List<Integer> procicon = Interpret.getProcid(f.du);

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        viewHolder.unitabil.setLayoutManager(linearLayoutManager);
                        AdapterAbil adapterAbil = new AdapterAbil(ability, proc,abilityicon,procicon);
                        viewHolder.unitabil.setAdapter(adapterAbil);
                        ViewCompat.setNestedScrollingEnabled(viewHolder.unitabil, false);
                    }
                }
            }
        });

        viewHolder.unitcdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.unitcd.getText().toString().endsWith("f"))
                    viewHolder.unitcd.setText(s.getCD(f,t,1));
                else
                    viewHolder.unitcd.setText(s.getCD(f,t,0));
            }
        });

        viewHolder.unitpreatkb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.unitpreatk.getText().toString().endsWith("f"))
                    viewHolder.unitpreatk.setText(s.getPre(f,1));
                else
                    viewHolder.unitpreatk.setText(s.getPre(f,0));
            }
        });

        viewHolder.unitpostb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.unitpost.getText().toString().endsWith("f"))
                    viewHolder.unitpost.setText(s.getPost(f,1));
                else
                    viewHolder.unitpost.setText(s.getPost(f,0));
            }
        });

        viewHolder.unittbab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.unittba.getText().toString().endsWith("f"))
                    viewHolder.unittba.setText(s.getTBA(f,1));
                else
                    viewHolder.unittba.setText(s.getTBA(f,0));
            }
        });

        viewHolder.unitatkb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int level = (int)viewHolder.unitlevel.getSelectedItem();
                int levelp = (int)viewHolder.unitlevelp.getSelectedItem();
                if(viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk))) {
                    viewHolder.unitatkb.setText(context.getString(R.string.unit_info_dps));
                    viewHolder.unitatk.setText(s.getDPS(f,t,level+levelp));
                } else {
                    viewHolder.unitatkb.setText(context.getString(R.string.unit_info_atk));
                    viewHolder.unitatk.setText(s.getAtk(f,t,level+levelp));
                }
            }
        });

        viewHolder.unitatktb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.unitatkt.getText().toString().endsWith("f"))
                    viewHolder.unitatkt.setText(s.getAtkTime(f,1));
                else
                    viewHolder.unitatkt.setText(s.getAtkTime(f,0));
            }
        });

        viewHolder.unitlevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int level = (int)viewHolder.unitlevel.getSelectedItem();
                int levelp = (int)viewHolder.unitlevelp.getSelectedItem();
                viewHolder.unithp.setText(s.getHP(f,t,level+levelp));

                if(f.du.rawAtkData().length > 1) {
                    if (viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk)))
                        viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp));
                    else
                        viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp));
                } else {
                    if (viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk)))
                        viewHolder.unitatk.setText(s.getTotAtk(f, t, level + levelp));
                    else
                        viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        viewHolder.unitlevelp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int level = (int)viewHolder.unitlevel.getSelectedItem();
                int levelp = (int)viewHolder.unitlevelp.getSelectedItem();
                viewHolder.unithp.setText(s.getHP(f,t,level+levelp));
                if(f.du.rawAtkData().length > 1) {
                    if (viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk)))
                        viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp));
                    else
                        viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp));
                } else {
                    if (viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk)))
                        viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp));
                    else
                        viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cdlevt.setSelection(Objects.requireNonNull(cdlevt.getText()).length());
        cdtreat.setSelection(Objects.requireNonNull(cdtreat.getText()).length());
        atktreat.setSelection(Objects.requireNonNull(atktreat.getText()).length());
        healtreat.setSelection(Objects.requireNonNull(healtreat.getText()).length());

        cdlevt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) > 30 || Integer.parseInt(s.toString()) <= 0) {
                        if(cdlev.isHelperTextEnabled()) {
                            cdlev.setHelperTextEnabled(false);
                            cdlev.setErrorEnabled(true);
                            cdlev.setError(context.getString(R.string.treasure_invalid));
                        }
                    } else {
                        if(cdlev.isErrorEnabled()) {
                            cdlev.setError(null);
                            cdlev.setErrorEnabled(false);
                            cdlev.setHelperTextEnabled(true);
                            cdlev.setHelperTextColor(new ColorStateList(states,color));
                            cdlev.setHelperText("1~30");
                        }
                    }
                } else {
                    if(cdlev.isErrorEnabled()) {
                        cdlev.setError(null);
                        cdlev.setErrorEnabled(false);
                        cdlev.setHelperTextEnabled(true);
                        cdlev.setHelperTextColor(new ColorStateList(states,color));
                        cdlev.setHelperText("1~30");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if(!text.toString().isEmpty()) {
                    if (Integer.parseInt(text.toString()) <= 30 && Integer.parseInt(text.toString()) > 0) {
                        int lev = Integer.parseInt(text.toString());

                        t.tech[0] = lev;

                        if (viewHolder.unitcd.getText().toString().endsWith("s")) {
                            viewHolder.unitcd.setText(s.getCD(f, t, 1));
                        } else {
                            viewHolder.unitcd.setText(s.getCD(f, t, 0));
                        }
                    }
                } else {
                    t.tech[0] = 1;
                    if(viewHolder.unitcd.getText().toString().endsWith("s")) {
                        viewHolder.unitcd.setText(s.getCD(f,t,1));
                    } else {
                        viewHolder.unitcd.setText(s.getCD(f,t,0));
                    }
                }
            }
        });

        cdtreat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) > 300) {
                        if(cdtrea.isHelperTextEnabled()) {
                            cdtrea.setHelperTextEnabled(false);
                            cdtrea.setErrorEnabled(true);
                            cdtrea.setError(context.getString(R.string.treasure_invalid));
                        }
                    } else {
                        if(cdtrea.isErrorEnabled()) {
                            cdtrea.setError(null);
                            cdtrea.setErrorEnabled(false);
                            cdtrea.setHelperTextEnabled(true);
                            cdtrea.setHelperTextColor(new ColorStateList(states,color));
                            cdtrea.setHelperText("0~300");
                        }
                    }
                } else {
                    if(cdtrea.isErrorEnabled()) {
                        cdtrea.setError(null);
                        cdtrea.setErrorEnabled(false);
                        cdtrea.setHelperTextEnabled(true);
                        cdtrea.setHelperTextColor(new ColorStateList(states,color));
                        cdtrea.setHelperText("0~300");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if(!text.toString().isEmpty()) {
                    if (Integer.parseInt(text.toString()) <= 300) {
                        int trea = Integer.parseInt(text.toString());

                        t.trea[2] = trea;

                        if (viewHolder.unitcd.getText().toString().endsWith("s")) {
                            viewHolder.unitcd.setText(s.getCD(f, t, 1));
                        } else {
                            viewHolder.unitcd.setText(s.getCD(f, t, 0));
                        }
                    }
                } else {
                    t.trea[2] = 0;
                    if(viewHolder.unitcd.getText().toString().endsWith("s")) {
                        viewHolder.unitcd.setText(s.getCD(f,t,1));
                    } else {
                        viewHolder.unitcd.setText(s.getCD(f,t,0));
                    }
                }
            }
        });

        atktreat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) > 300) {
                        if(atktrea.isHelperTextEnabled()) {
                            atktrea.setHelperTextEnabled(false);
                            atktrea.setErrorEnabled(true);
                            atktrea.setError(context.getString(R.string.treasure_invalid));
                        }
                    } else {
                        if(atktrea.isErrorEnabled()) {
                            atktrea.setError(null);
                            atktrea.setErrorEnabled(false);
                            atktrea.setHelperTextEnabled(true);
                            atktrea.setHelperTextColor(new ColorStateList(states,color));
                            atktrea.setHelperText("0~300");
                        }
                    }
                } else {
                    if(atktrea.isErrorEnabled()) {
                        atktrea.setError(null);
                        atktrea.setErrorEnabled(false);
                        atktrea.setHelperTextEnabled(true);
                        atktrea.setHelperTextColor(new ColorStateList(states,color));
                        atktrea.setHelperText("0~300");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if(!text.toString().isEmpty()) {
                    if (Integer.parseInt(text.toString()) <= 300) {
                        int trea = Integer.parseInt(text.toString());

                        t.trea[0] = trea;
                        int level = (int) viewHolder.unitlevel.getSelectedItem();
                        int levelp = (int) viewHolder.unitlevelp.getSelectedItem();

                        if (viewHolder.unitatkb.getText().toString().equals(context.getString(R.string.unit_info_dps))) {
                            viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp));
                        } else {
                            viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp));
                        }
                    }
                } else {
                    t.trea[0] = 0;
                    int level = (int)viewHolder.unitlevel.getSelectedItem();
                    int levelp = (int)viewHolder.unitlevelp.getSelectedItem();

                    if(viewHolder.unitatkb.getText().toString().equals(context.getString(R.string.unit_info_dps))) {
                        viewHolder.unitatk.setText(s.getDPS(f,t,level+levelp));
                    } else {
                        viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp));
                    }
                }
            }
        });

        healtreat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) > 300) {
                        if(healtrea.isHelperTextEnabled()) {
                            healtrea.setHelperTextEnabled(false);
                            healtrea.setErrorEnabled(true);
                            healtrea.setError(context.getString(R.string.treasure_invalid));
                        }
                    } else {
                        if(healtrea.isErrorEnabled()) {
                            healtrea.setError(null);
                            healtrea.setErrorEnabled(false);
                            healtrea.setHelperTextEnabled(true);
                            healtrea.setHelperTextColor(new ColorStateList(states,color));
                            healtrea.setHelperText("0~300");
                        }
                    }
                } else {
                    if(healtrea.isErrorEnabled()) {
                        healtrea.setError(null);
                        healtrea.setErrorEnabled(false);
                        healtrea.setHelperTextEnabled(true);
                        healtrea.setHelperTextColor(new ColorStateList(states,color));
                        healtrea.setHelperText("0~300");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if(!text.toString().isEmpty()) {
                    if (Integer.parseInt(text.toString()) <= 300) {
                        int trea = Integer.parseInt(text.toString());

                        t.trea[1] = trea;
                        int level = (int) viewHolder.unitlevel.getSelectedItem();
                        int levelp = (int) viewHolder.unitlevelp.getSelectedItem();

                        viewHolder.unithp.setText(s.getHP(f, t, level + levelp));
                    }
                } else {
                    t.trea[1] = 0;
                    int level = (int)viewHolder.unitlevel.getSelectedItem();
                    int levelp = (int)viewHolder.unitlevelp.getSelectedItem();

                    viewHolder.unithp.setText(s.getHP(f,t,level+levelp));
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t.tech[0] = 30;
                t.trea[0] = 300;
                t.trea[1] = 300;
                t.trea[2] = 300;

                cdlevt.setText(String.valueOf(t.tech[0]));
                cdtreat.setText(String.valueOf(t.trea[0]));
                atktreat.setText(String.valueOf(t.trea[1]));
                healtreat.setText(String.valueOf(t.trea[2]));

                int level = (int)viewHolder.unitlevel.getSelectedItem();
                int levelp = (int)viewHolder.unitlevelp.getSelectedItem();

                if(viewHolder.unitcd.getText().toString().endsWith("s")) {
                    viewHolder.unitcd.setText(s.getCD(f,t,1));
                } else {
                    viewHolder.unitcd.setText(s.getCD(f,t,0));
                }

                if(viewHolder.unitatkb.getText().toString().equals(context.getString(R.string.unit_info_dps))) {
                    viewHolder.unitatk.setText(s.getDPS(f,t,level+levelp));
                } else {
                    viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp));
                }

                viewHolder.unithp.setText(s.getHP(f,t,level+levelp));
            }
        });
    }

    private int getIndex(Spinner spinner, int lev) {
        int index = 0;

        for(int i = 0; i< spinner.getCount();i++)
            if (lev == (int)spinner.getItemAtPosition(i))
                index = i;

        return index;
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        Button frse;
        TextView unitname;
        TextView unitid;
        TextView unithp;
        TextView unithb;
        Spinner unitlevel;
        Spinner unitlevelp;
        TextView unitplus;
        ImageView uniticon;
        Button unitatkb;
        TextView unitatk;
        TextView unittrait;
        TextView unitcost;
        TextView unitsimu;
        TextView unitspd;
        Button unitcdb;
        TextView unitcd;
        TextView unitrang;
        Button unitpreatkb;
        TextView unitpreatk;
        Button unitpostb;
        TextView unitpost;
        Button unittbab;
        TextView unittba;
        Button unitatktb;
        TextView unitatkt;
        TextView unitabilt;
        TextView none;
        RecyclerView unitabil;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            frse = itemView.findViewById(R.id.unitinffrse);
            unitname = itemView.findViewById(R.id.unitinfname);
            unitid = itemView.findViewById(R.id.unitinfidr);
            uniticon = itemView.findViewById(R.id.unitinficon);
            unithp = itemView.findViewById(R.id.unitinfhpr);
            unithb = itemView.findViewById(R.id.unitinfhbr);
            unitlevel = itemView.findViewById(R.id.unitinflevr);
            unitlevelp = itemView.findViewById(R.id.unitinflevpr);
            unitplus = itemView.findViewById(R.id.unitinfplus);
            unitplus.setText(" + ");
            unitatkb = itemView.findViewById(R.id.unitinfatk);
            unitatk = itemView.findViewById(R.id.unitinfatkr);
            unittrait = itemView.findViewById(R.id.unitinftraitr);
            unitcost = itemView.findViewById(R.id.unitinfcostr);
            unitsimu = itemView.findViewById(R.id.unitinfsimur);
            unitspd = itemView.findViewById(R.id.unitinfspdr);
            unitcdb = itemView.findViewById(R.id.unitinfcd);
            unitcd = itemView.findViewById(R.id.unitinfcdr);
            unitrang = itemView.findViewById(R.id.unitinfrangr);
            unitpreatkb = itemView.findViewById(R.id.unitinfpreatk);
            unitpreatk = itemView.findViewById(R.id.unitinfpreatkr);
            unitpostb = itemView.findViewById(R.id.unitinfpost);
            unitpost = itemView.findViewById(R.id.unitinfpostr);
            unittbab = itemView.findViewById(R.id.unitinftba);
            unittba = itemView.findViewById(R.id.unitinftbar);
            unitatktb = itemView.findViewById(R.id.unitinfatktime);
            unitatkt = itemView.findViewById(R.id.unitinfatktimer);
            unitabilt = itemView.findViewById(R.id.unitinfabiltr);
            none = itemView.findViewById(R.id.unitabilnone);
            unitabil = itemView.findViewById(R.id.unitinfabilr);
            unitabil.requestFocusFromTouch();
        }
    }

    private String number(int num) {
        if (0 <= num && num < 10) {
            return "00" + num;
        } else if (10 <= num && num <= 99) {
            return "0" + num;
        } else {
            return String.valueOf(num);
        }
    }

    class AdapterAbil extends RecyclerView.Adapter<AdapterAbil.ViewHolder> {
        private List<String> ability;
        private List<String> procs;
        private List<Integer> abilicon;
        private List<Integer> procicon;

        AdapterAbil(List<String> ability,List<String> procs, List<Integer> abilicon, List<Integer> procicon) {
            this.ability = ability;
            this.procs = procs;
            this.abilicon = abilicon;
            this.procicon = procicon;
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
                    Bitmap resized = StaticStore.getResize(StaticStore.icons[abilicon.get(viewHolder.getAdapterPosition())],context);
                    viewHolder.abilicon.setImageBitmap(resized);
                } else {
                    viewHolder.abilicon.setImageBitmap(empty());
                }
            } else {
                int location = viewHolder.getAdapterPosition()-ability.size();
                viewHolder.abiltext.setText(procs.get(location));
                Bitmap resized = StaticStore.getResize(StaticStore.picons[procicon.get(location)],context);
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
