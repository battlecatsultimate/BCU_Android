package com.mandarin.bcu.androidutil.adapters;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.getStrings;
import com.mandarin.bcu.util.Interpret;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import common.battle.BasisSet;
import common.battle.Treasure;
import common.battle.data.MaskUnit;
import common.system.MultiLangCont;
import common.util.unit.Form;

public class UnitinfPager extends Fragment {
    View view;
    
    public static UnitinfPager newInstance(int form, int id, String [] names) {
        UnitinfPager pager = new UnitinfPager();
        Bundle bundle = new Bundle();
        bundle.putInt("Form",form);
        bundle.putInt("ID",id);
        bundle.putStringArray("Names",names);
        pager.setArguments(bundle);
        
        return pager;
    }
    
    int form;
    int id;
    String [] names;

    private int fs = 0;
    private getStrings s;
    private String [][] fragment = {{"Immune to "},{""}};
    private int[][] states = new int[][] {
            new int[] {android.R.attr.state_enabled}
    };

    private int[] color;

    private boolean talents = false;

    private int[] pcoinlev;

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
    CheckBox unittalen;
    TableRow npresetrow;
    Button npreset;
    TableRow nprow;
    Activity activity;
    int [] ids = {R.id.talent0,R.id.talent1,R.id.talent2,R.id.talent3,R.id.talent4};
    Spinner [] pcoins = new Spinner[ids.length];
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
        view = inflater.inflate(R.layout.unit_table,container,false);
        activity =  getActivity();
        assert activity != null;
        s = new getStrings(activity);
        s.getTalList();
        color = new int[] {
                getAttributeColor(activity,R.attr.TextPrimary)
        };
        init(view);
        assert getArguments() != null;
        form = getArguments().getInt("Form");
        id = getArguments().getInt("ID");
        names = getArguments().getStringArray("Names");
        unitabil.setFocusableInTouchMode(false);
        unitabil.setFocusable(false);
        unitabil.setNestedScrollingEnabled(false);

        TextInputLayout cdlev = activity.findViewById(R.id.cdlev);
        TextInputLayout cdtrea = activity.findViewById(R.id.cdtrea);
        TextInputLayout atktrea = activity.findViewById(R.id.atktrea);
        TextInputLayout healtrea = activity.findViewById(R.id.healtrea);

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

        SharedPreferences shared = activity.getSharedPreferences("configuration", Context.MODE_PRIVATE);

        if(shared.getBoolean("frame",true)) {
            fs = 0;
            frse.setText(activity.getString(R.string.unit_info_fr));
        } else {
            fs = 1;
            frse.setText(activity.getString(R.string.unit_info_sec));
        }

        Treasure t = BasisSet.current.t();
        Form f = StaticStore.units.get(id).forms[form];

        if(f.getPCoin()==null) {
            unittalen.setVisibility(View.GONE);
            npreset.setVisibility(View.GONE);
            nprow.setVisibility(View.GONE);
            pcoinlev = null;
        } else {
            int [] max = f.getPCoin().max;
            pcoinlev = new int[max.length];
            pcoinlev[0] = 0;

            for(int j =0;j<pcoins.length;j++) {
                List<Integer> plev = new ArrayList<>();
                for(int k=0;k<max[j+1]+1;k++)
                    plev.add(k);
                ArrayAdapter<Integer> adapter = new ArrayAdapter<>(activity,R.layout.spinneradapter,plev);
                pcoins[j].setAdapter(adapter);
                pcoins[j].setSelection(getIndex(pcoins[j],max[j+1]));

                pcoinlev[j+1] = max[j+1];
            }
        }

        List<String> ability = Interpret.getAbi(f.du,fragment,StaticStore.addition,0);
        List<Integer> abilityicon = Interpret.getAbiid(f.du);

        TextInputEditText cdlevt = activity.findViewById(R.id.cdlevt);
        TextInputEditText cdtreat = activity.findViewById(R.id.cdtreat);
        TextInputEditText atktreat = activity.findViewById(R.id.atktreat);
        TextInputEditText healtreat = activity.findViewById(R.id.healtreat);

        cdlevt.setText(String.valueOf(t.tech[0]));
        cdtreat.setText(String.valueOf(t.trea[2]));
        atktreat.setText(String.valueOf(t.trea[0]));
        healtreat.setText(String.valueOf(t.trea[1]));

        String language = StaticStore.lang[shared.getInt("Language",0)];
        if(language.equals("")) {
            language = Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
        }
        List<String> proc;
        if(language.equals("ko")) {
            proc = Interpret.getProc(f.du,1,fs);
        } else {
            proc = Interpret.getProc(f.du,0,fs);
        }
        List<Integer> procicon = Interpret.getProcid(f.du);

        String name = MultiLangCont.FNAME.getCont(f);

        if(name == null)
            name = "";

        uniticon.setImageBitmap(StaticStore.getResizeb((Bitmap)f.anim.uni.getImg().bimg(),activity,48));
        unitname.setText(name);
        unitid.setText(s.getID(form,number(id)));
        unithp.setText(s.getHP(f,t,f.unit.getPrefLv(),false,pcoinlev));
        unithb.setText(s.getHB(f,false,pcoinlev));
        unitatk.setText(s.getTotAtk(f,t,f.unit.getPrefLv(),false,pcoinlev));
        unittrait.setText(s.getTrait(f,false,pcoinlev));
        unitcost.setText(s.getCost(f,false,pcoinlev));
        unitsimu.setText(s.getSimu(f));
        unitspd.setText(s.getSpd(f,false,pcoinlev));
        unitcd.setText(s.getCD(f, t, fs,false,pcoinlev));
        unitrang.setText(s.getRange(f));
        unitpreatk.setText(s.getPre(f, fs));
        unitpost.setText(s.getPost(f, fs));
        unittba.setText(s.getTBA(f, fs));
        unitatkt.setText(s.getAtkTime(f, fs));
        unitabilt.setText(s.getAbilT(f));

        if(ability.size()>0 || proc.size() > 0) {
            none.setVisibility(View.GONE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            unitabil.setLayoutManager(linearLayoutManager);
            AdapterAbil adapterAbil = new AdapterAbil(ability, proc,abilityicon,procicon,activity);
            unitabil.setAdapter(adapterAbil);
            ViewCompat.setNestedScrollingEnabled(unitabil, false);
        } else {
            unitabil.setVisibility(View.GONE);
        }
        
        Listeners();
        
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void Listeners() {
        TextInputLayout cdlev = Objects.requireNonNull(activity).findViewById(R.id.cdlev);
        TextInputLayout cdtrea = activity.findViewById(R.id.cdtrea);
        TextInputLayout atktrea = activity.findViewById(R.id.atktrea);
        TextInputLayout healtrea = activity.findViewById(R.id.healtrea);
        TextInputEditText cdlevt = activity.findViewById(R.id.cdlevt);
        TextInputEditText cdtreat = activity.findViewById(R.id.cdtreat);
        TextInputEditText atktreat = activity.findViewById(R.id.atktreat);
        TextInputEditText healtreat = activity.findViewById(R.id.healtreat);
        Button reset = activity.findViewById(R.id.treasurereset);

        Treasure t = BasisSet.current.t();
        Form f = StaticStore.units.get(id).forms[form];

        List<Integer> levels = new ArrayList<>();
        for(int j =1;j < f.unit.max+1;j++)
            levels.add(j);

        ArrayList<Integer> levelsp = new ArrayList<>();
        for(int j=0;j<f.unit.maxp+1;j++)
            levelsp.add(j);

        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<>(activity, R.layout.spinneradapter, levels);
        ArrayAdapter<Integer> arrayAdapterp = new ArrayAdapter<>(activity, R.layout.spinneradapter, levelsp);

        int currentlev;

        SharedPreferences shared = activity.getSharedPreferences("configuration", Context.MODE_PRIVATE);

        if(shared.getInt("default_level",50) > f.unit.max)
            currentlev = f.unit.max;
        else
        if(f.unit.rarity != 0)
            currentlev = shared.getInt("default_level",50);
        else
            currentlev = f.unit.max;

        unitlevel.setAdapter(arrayAdapter);
        unitlevel.setSelection(getIndex(unitlevel,currentlev));
        unitlevelp.setAdapter(arrayAdapterp);

        if(f.unit.getPrefLv()-f.unit.max < 0) {
            unitlevelp.setSelection(getIndex(unitlevelp,0));
        } else {
            unitlevelp.setSelection(getIndex(unitlevelp, f.unit.getPrefLv() - f.unit.max));
        }

        if(levelsp.size() == 1) {
            unitlevelp.setVisibility(View.GONE);
            unitplus.setVisibility(View.GONE);
        }

        frse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fs == 0) {
                    fs = 1;
                    unitcd.setText(s.getCD(f,t,fs,talents,pcoinlev));
                    unitpreatk.setText(s.getPre(f,fs));
                    unitpost.setText(s.getPost(f,fs));
                    unittba.setText(s.getTBA(f,fs));
                    unitatkt.setText(s.getAtkTime(f,fs));
                    frse.setText(activity.getString(R.string.unit_info_sec));

                    if(unitabil.getVisibility() != View.GONE) {
                        MaskUnit du = f.du;
                        if(f.getPCoin() != null)
                            du = talents?f.getPCoin().improve(pcoinlev):f.du;

                        List<String> ability = Interpret.getAbi(du, fragment, StaticStore.addition, 0);
                        List<Integer> abilityicon = Interpret.getAbiid(du);

                        String language = Locale.getDefault().getLanguage();
                        List<String> proc;
                        if (language.equals("ko"))
                            proc = Interpret.getProc(du, 1, fs);
                        else
                            proc = Interpret.getProc(du, 0, fs);
                        List<Integer> procicon = Interpret.getProcid(du);

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
                        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        unitabil.setLayoutManager(linearLayoutManager);
                        AdapterAbil adapterAbil = new AdapterAbil(ability, proc,abilityicon,procicon,activity);
                        unitabil.setAdapter(adapterAbil);
                        ViewCompat.setNestedScrollingEnabled(unitabil, false);
                    }
                } else {
                    fs = 0;
                    unitcd.setText(s.getCD(f,t,fs,talents,pcoinlev));
                    unitpreatk.setText(s.getPre(f,fs));
                    unitpost.setText(s.getPost(f,fs));
                    unittba.setText(s.getTBA(f,fs));
                    unitatkt.setText(s.getAtkTime(f,fs));
                    frse.setText(activity.getString(R.string.unit_info_fr));

                    if(unitabil.getVisibility() != View.GONE) {
                        MaskUnit du = f.du;
                        if(f.getPCoin() != null)
                            du = talents?f.getPCoin().improve(pcoinlev):f.du;

                        List<String> ability = Interpret.getAbi(du, fragment, StaticStore.addition, 0);
                        List<Integer> abilityicon = Interpret.getAbiid(du);

                        String language = Locale.getDefault().getLanguage();
                        List<String> proc;

                        if (language.equals("ko"))
                            proc = Interpret.getProc(du, 1, fs);
                        else
                            proc = Interpret.getProc(du, 0, fs);

                        List<Integer> procicon = Interpret.getProcid(du);

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
                        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        unitabil.setLayoutManager(linearLayoutManager);
                        AdapterAbil adapterAbil = new AdapterAbil(ability, proc,abilityicon,procicon,activity);
                        unitabil.setAdapter(adapterAbil);
                        ViewCompat.setNestedScrollingEnabled(unitabil, false);
                    }
                }
            }
        });

        unitcdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(unitcd.getText().toString().endsWith("f"))
                    unitcd.setText(s.getCD(f,t,1,talents,pcoinlev));
                else
                    unitcd.setText(s.getCD(f,t,0,talents,pcoinlev));
            }
        });

        unitpreatkb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(unitpreatk.getText().toString().endsWith("f"))
                    unitpreatk.setText(s.getPre(f,1));
                else
                    unitpreatk.setText(s.getPre(f,0));
            }
        });

        unitpostb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(unitpost.getText().toString().endsWith("f"))
                    unitpost.setText(s.getPost(f,1));
                else
                    unitpost.setText(s.getPost(f,0));
            }
        });

        unittbab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(unittba.getText().toString().endsWith("f"))
                    unittba.setText(s.getTBA(f,1));
                else
                    unittba.setText(s.getTBA(f,0));
            }
        });

        unitatkb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int level = (int)unitlevel.getSelectedItem();
                int levelp = (int)unitlevelp.getSelectedItem();
                if(unitatkb.getText().equals(activity.getString(R.string.unit_info_atk))) {
                    unitatkb.setText(activity.getString(R.string.unit_info_dps));
                    unitatk.setText(s.getDPS(f,t,level+levelp,talents,pcoinlev));
                } else {
                    unitatkb.setText(activity.getString(R.string.unit_info_atk));
                    unitatk.setText(s.getAtk(f,t,level+levelp,talents,pcoinlev));
                }
            }
        });

        unitatktb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(unitatkt.getText().toString().endsWith("f"))
                    unitatkt.setText(s.getAtkTime(f,1));
                else
                    unitatkt.setText(s.getAtkTime(f,0));
            }
        });

        unitlevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int level = (int)unitlevel.getSelectedItem();
                int levelp = (int)unitlevelp.getSelectedItem();
                unithp.setText(s.getHP(f,t,level+levelp,talents,pcoinlev));

                if(f.du.rawAtkData().length > 1) {
                    if (unitatkb.getText().equals(activity.getString(R.string.unit_info_atk)))
                        unitatk.setText(s.getAtk(f, t, level + levelp,talents,pcoinlev));
                    else
                        unitatk.setText(s.getDPS(f, t, level + levelp,talents,pcoinlev));
                } else {
                    if (unitatkb.getText().equals(activity.getString(R.string.unit_info_atk)))
                        unitatk.setText(s.getTotAtk(f, t, level + levelp,talents,pcoinlev));
                    else
                        unitatk.setText(s.getDPS(f, t, level + levelp,talents,pcoinlev));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        unitlevelp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int level = (int)unitlevel.getSelectedItem();
                int levelp = (int)unitlevelp.getSelectedItem();
                unithp.setText(s.getHP(f,t,level+levelp,talents,pcoinlev));
                if(f.du.rawAtkData().length > 1) {
                    if (unitatkb.getText().equals(activity.getString(R.string.unit_info_atk)))
                        unitatk.setText(s.getAtk(f, t, level + levelp,talents,pcoinlev));
                    else
                        unitatk.setText(s.getDPS(f, t, level + levelp,talents,pcoinlev));
                } else {
                    if (unitatkb.getText().equals(activity.getString(R.string.unit_info_atk)))
                        unitatk.setText(s.getAtk(f, t, level + levelp,talents,pcoinlev));
                    else
                        unitatk.setText(s.getDPS(f, t, level + levelp,talents,pcoinlev));
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
                            cdlev.setError(activity.getString(R.string.treasure_invalid));
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

                        if (unitcd.getText().toString().endsWith("s")) {
                            unitcd.setText(s.getCD(f, t, 1,talents,pcoinlev));
                        } else {
                            unitcd.setText(s.getCD(f, t, 0,talents,pcoinlev));
                        }
                    }
                } else {
                    t.tech[0] = 1;
                    if(unitcd.getText().toString().endsWith("s")) {
                        unitcd.setText(s.getCD(f,t,1,talents,pcoinlev));
                    } else {
                        unitcd.setText(s.getCD(f,t,0,talents,pcoinlev));
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
                            cdtrea.setError(activity.getString(R.string.treasure_invalid));
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

                        if (unitcd.getText().toString().endsWith("s")) {
                            unitcd.setText(s.getCD(f, t, 1,talents,pcoinlev));
                        } else {
                            unitcd.setText(s.getCD(f, t, 0,talents,pcoinlev));
                        }
                    }
                } else {
                    t.trea[2] = 0;
                    if(unitcd.getText().toString().endsWith("s")) {
                        unitcd.setText(s.getCD(f,t,1,talents,pcoinlev));
                    } else {
                        unitcd.setText(s.getCD(f,t,0,talents,pcoinlev));
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
                            atktrea.setError(activity.getString(R.string.treasure_invalid));
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
                        int level = (int) unitlevel.getSelectedItem();
                        int levelp = (int) unitlevelp.getSelectedItem();

                        if (unitatkb.getText().toString().equals(activity.getString(R.string.unit_info_dps))) {
                            unitatk.setText(s.getDPS(f, t, level + levelp,talents,pcoinlev));
                        } else {
                            unitatk.setText(s.getAtk(f, t, level + levelp,talents,pcoinlev));
                        }
                    }
                } else {
                    t.trea[0] = 0;
                    int level = (int)unitlevel.getSelectedItem();
                    int levelp = (int)unitlevelp.getSelectedItem();

                    if(unitatkb.getText().toString().equals(activity.getString(R.string.unit_info_dps))) {
                        unitatk.setText(s.getDPS(f,t,level+levelp,talents,pcoinlev));
                    } else {
                        unitatk.setText(s.getAtk(f, t, level + levelp,talents,pcoinlev));
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
                            healtrea.setError(activity.getString(R.string.treasure_invalid));
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
                        int level = (int) unitlevel.getSelectedItem();
                        int levelp = (int) unitlevelp.getSelectedItem();

                        unithp.setText(s.getHP(f, t, level + levelp,talents,pcoinlev));
                    }
                } else {
                    t.trea[1] = 0;
                    int level = (int)unitlevel.getSelectedItem();
                    int levelp = (int)unitlevelp.getSelectedItem();

                    unithp.setText(s.getHP(f,t,level+levelp,talents,pcoinlev));
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

                int level = (int)unitlevel.getSelectedItem();
                int levelp = (int)unitlevelp.getSelectedItem();

                if(unitcd.getText().toString().endsWith("s")) {
                    unitcd.setText(s.getCD(f,t,1,talents,pcoinlev));
                } else {
                    unitcd.setText(s.getCD(f,t,0,talents,pcoinlev));
                }

                if(unitatkb.getText().toString().equals(activity.getString(R.string.unit_info_dps))) {
                    unitatk.setText(s.getDPS(f,t,level+levelp,talents,pcoinlev));
                } else {
                    unitatk.setText(s.getAtk(f, t, level + levelp,talents,pcoinlev));
                }

                unithp.setText(s.getHP(f,t,level+levelp,talents,pcoinlev));
            }
        });

        unittalen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                talents = true;
                validate(f,t);
                if(isChecked) {
                    ValueAnimator anim = ValueAnimator.ofInt(0,StaticStore.dptopx(100f,activity));
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int val = (Integer) animation.getAnimatedValue();
                            ViewGroup.LayoutParams layout = npresetrow.getLayoutParams();
                            layout.width = val;
                            npresetrow.setLayoutParams(layout);
                        }
                    });
                    anim.setDuration(300);
                    anim.setInterpolator(new DecelerateInterpolator());
                    anim.start();

                    ValueAnimator anim2 = ValueAnimator.ofInt(0,StaticStore.dptopx(48f,activity));
                    anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)nprow.getLayoutParams();
                            params.height = (int) (Integer) animation.getAnimatedValue();
                            nprow.setLayoutParams(params);
                        }
                    });
                    anim2.setDuration(300);
                    anim2.setInterpolator(new DecelerateInterpolator());
                    anim2.start();

                    ValueAnimator anim3 = ValueAnimator.ofInt(0,StaticStore.dptopx(16f,activity));
                    anim3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)nprow.getLayoutParams();
                            params.topMargin = (int) animation.getAnimatedValue();
                            nprow.setLayoutParams(params);
                        }
                    });
                    anim3.setDuration(300);
                    anim3.setInterpolator(new DecelerateInterpolator());
                    anim3.start();
                } else {
                    talents = false;
                    validate(f,t);
                    ValueAnimator anim = ValueAnimator.ofInt(StaticStore.dptopx(100f,activity),0);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int val = (Integer) animation.getAnimatedValue();
                            ViewGroup.LayoutParams layout = npresetrow.getLayoutParams();
                            layout.width = val;
                            npresetrow.setLayoutParams(layout);
                        }
                    });
                    anim.setDuration(300);
                    anim.setInterpolator(new DecelerateInterpolator());
                    anim.start();

                    ValueAnimator anim2 = ValueAnimator.ofInt(StaticStore.dptopx(48f,activity),0);
                    anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)nprow.getLayoutParams();
                            params.height = (int) animation.getAnimatedValue();
                            nprow.setLayoutParams(params);
                        }
                    });
                    anim2.setDuration(300);
                    anim2.setInterpolator(new DecelerateInterpolator());
                    anim2.start();

                    ValueAnimator anim3 = ValueAnimator.ofInt(StaticStore.dptopx(16f,activity),0);
                    anim3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)nprow.getLayoutParams();
                            params.topMargin = (int) animation.getAnimatedValue();
                            nprow.setLayoutParams(params);
                        }
                    });
                    anim3.setDuration(300);
                    anim3.setInterpolator(new DecelerateInterpolator());
                    anim3.start();
                }
            }
        });

        for(int i = 0;i<pcoins.length;i++) {
            final int finals = i;
            pcoins[i].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    pcoinlev[finals+1] = (int)pcoins[finals].getSelectedItem();
                    validate(f,t);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            pcoins[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    pcoins[finals].setClickable(false);
                    Toast.makeText(activity,s.getTalentName(finals,f),Toast.LENGTH_SHORT).show();

                    return true;
                }
            });
        }

        npreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i =0;i<pcoins.length;i++) {
                    pcoins[i].setSelection(getIndex(pcoins[i],f.getPCoin().max[i+1]));
                    pcoinlev[i+1] = f.getPCoin().max[i+1];
                }

                validate(f,t);
            }
        });
    }

    private void validate(Form f, Treasure t) {
        int level = (int)unitlevel.getSelectedItem();
        int levelp = (int)unitlevelp.getSelectedItem();
        unithp.setText(s.getHP(f,t,level+levelp,talents,pcoinlev));
        unithb.setText(s.getHB(f,talents,pcoinlev));
        if(unitatk.getText().toString().equals("DPS"))
            unitatk.setText(s.getDPS(f,t,level+levelp,talents,pcoinlev));
        else
            unitatk.setText(s.getAtk(f,t,level+levelp,talents,pcoinlev));
        unitcost.setText(s.getCost(f,talents,pcoinlev));
        if(unitcd.getText().toString().endsWith("s"))
            unitcd.setText(s.getCD(f,t,1,talents,pcoinlev));
        else
            unitcd.setText(s.getCD(f,t,0,talents,pcoinlev));
        unittrait.setText(s.getTrait(f,talents,pcoinlev));
        unitspd.setText(s.getSpd(f,talents,pcoinlev));

        MaskUnit du;

        if(f.getPCoin() != null)
            du = talents?f.getPCoin().improve(pcoinlev):f.du;
        else
            du = f.du;

        List<String> abil = Interpret.getAbi(du,fragment,StaticStore.addition,0);

        SharedPreferences shared = activity.getSharedPreferences("configuration",Context.MODE_PRIVATE);

        String language = StaticStore.lang[shared.getInt("Language",0)];
        if(language.equals("")) {
            language = Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
        }
        List<String> proc;
        if(language.equals("ko")) {
            proc = Interpret.getProc(du,1,fs);
        } else {
            proc = Interpret.getProc(du,0,fs);
        }

        List<Integer> abilityicon = Interpret.getAbiid(du);
        List<Integer> procicon = Interpret.getProcid(du);

        if(abil.size()>0 || proc.size() > 0) {
            none.setVisibility(View.GONE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            unitabil.setLayoutManager(linearLayoutManager);
            AdapterAbil adapterAbil = new AdapterAbil(abil, proc,abilityicon,procicon,activity);
            unitabil.setAdapter(adapterAbil);
            ViewCompat.setNestedScrollingEnabled(unitabil, false);
        } else {
            unitabil.setVisibility(View.GONE);
        }
    }
    
    private void init(View view) {
        frse = view.findViewById(R.id.unitinffrse);
        unitname = view.findViewById(R.id.unitinfname);
        unitid = view.findViewById(R.id.unitinfidr);
        uniticon = view.findViewById(R.id.unitinficon);
        unithp = view.findViewById(R.id.unitinfhpr);
        unithb = view.findViewById(R.id.unitinfhbr);
        unitlevel = view.findViewById(R.id.unitinflevr);
        unitlevelp = view.findViewById(R.id.unitinflevpr);
        unitplus = view.findViewById(R.id.unitinfplus);
        unitplus.setText(" + ");
        unitatkb = view.findViewById(R.id.unitinfatk);
        unitatk = view.findViewById(R.id.unitinfatkr);
        unittrait = view.findViewById(R.id.unitinftraitr);
        unitcost = view.findViewById(R.id.unitinfcostr);
        unitsimu = view.findViewById(R.id.unitinfsimur);
        unitspd = view.findViewById(R.id.unitinfspdr);
        unitcdb = view.findViewById(R.id.unitinfcd);
        unitcd = view.findViewById(R.id.unitinfcdr);
        unitrang = view.findViewById(R.id.unitinfrangr);
        unitpreatkb = view.findViewById(R.id.unitinfpreatk);
        unitpreatk = view.findViewById(R.id.unitinfpreatkr);
        unitpostb = view.findViewById(R.id.unitinfpost);
        unitpost = view.findViewById(R.id.unitinfpostr);
        unittbab = view.findViewById(R.id.unitinftba);
        unittba = view.findViewById(R.id.unitinftbar);
        unitatktb = view.findViewById(R.id.unitinfatktime);
        unitatkt = view.findViewById(R.id.unitinfatktimer);
        unitabilt = view.findViewById(R.id.unitinfabiltr);
        none = view.findViewById(R.id.unitabilnone);
        unitabil = view.findViewById(R.id.unitinfabilr);
        unitabil.requestFocusFromTouch();
        unittalen = view.findViewById(R.id.unitinftalen);
        npreset = view.findViewById(R.id.unitinftalreset);
        npresetrow = view.findViewById(R.id.talresetrow);
        nprow = view.findViewById(R.id.talenrow);
        for(int i = 0;i<ids.length;i++)
            pcoins[i] = view.findViewById(ids[i]);
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

    private int getIndex(Spinner spinner, int lev) {
        int index = 0;

        for(int i = 0; i< spinner.getCount();i++)
            if (lev == (int)spinner.getItemAtPosition(i))
                index = i;

        return index;
    }
}
