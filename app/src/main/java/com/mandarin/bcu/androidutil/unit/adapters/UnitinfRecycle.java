package com.mandarin.bcu.androidutil.unit.adapters;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
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

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.AdapterAbil;
import com.mandarin.bcu.androidutil.getStrings;
import com.mandarin.bcu.util.Interpret;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import common.battle.BasisSet;
import common.battle.Treasure;
import common.battle.data.MaskUnit;
import common.util.unit.Form;

public class UnitinfRecycle extends RecyclerView.Adapter<UnitinfRecycle.ViewHolder> {
    private Activity context;
    private final ArrayList<String> names;
    private final Form[] forms;
    private final int id;
    private int fs = 0;
    private getStrings s;
    private String[][] fragment = {{"Immune to "}, {""}};
    private int[][] states = new int[][]{
            new int[]{android.R.attr.state_enabled}
    };

    private int[] color;

    private boolean talents = false;

    private int[] pcoins;


    public UnitinfRecycle(Activity context, ArrayList<String> names, Form[] forms, int id) {
        this.context = context;
        this.names = names;
        this.forms = forms;
        this.id = id;
        s = new getStrings(this.context);
        s.getTalList();
        color = new int[]{
                getAttributeColor(context, R.attr.TextPrimary)
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(context).inflate(R.layout.unit_table, viewGroup, false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        TextInputLayout cdlev = context.findViewById(R.id.cdlev);
        TextInputLayout cdtrea = context.findViewById(R.id.cdtrea);
        TextInputLayout atktrea = context.findViewById(R.id.atktrea);
        TextInputLayout healtrea = context.findViewById(R.id.healtrea);

        cdlev.setCounterEnabled(true);
        cdlev.setCounterMaxLength(2);

        cdtrea.setCounterEnabled(true);
        cdtrea.setCounterMaxLength(3);

        atktrea.setCounterEnabled(true);
        atktrea.setCounterMaxLength(3);

        healtrea.setCounterEnabled(true);
        healtrea.setCounterMaxLength(3);

        cdlev.setHelperTextColor(new ColorStateList(states, color));
        cdtrea.setHelperTextColor(new ColorStateList(states, color));
        atktrea.setHelperTextColor(new ColorStateList(states, color));
        healtrea.setHelperTextColor(new ColorStateList(states, color));

        SharedPreferences shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);

        if (shared.getBoolean("frame", true)) {
            fs = 0;
            viewHolder.frse.setText(context.getString(R.string.unit_info_fr));
        } else {
            fs = 1;
            viewHolder.frse.setText(context.getString(R.string.unit_info_sec));
        }

        Treasure t = BasisSet.current.t();
        Form f = forms[viewHolder.getAdapterPosition()];

        if (f.getPCoin() == null) {
            viewHolder.unittalen.setVisibility(View.GONE);
            viewHolder.npreset.setVisibility(View.GONE);
            viewHolder.nprow.setVisibility(View.GONE);
            pcoins = null;
        } else {
            int[] max = f.getPCoin().max;
            pcoins = new int[max.length];
            pcoins[0] = 0;

            for (int j = 0; j < viewHolder.pcoins.length; j++) {
                List<Integer> plev = new ArrayList<>();
                for (int k = 0; k < max[j + 1] + 1; k++)
                    plev.add(k);
                ArrayAdapter<Integer> adapter = new ArrayAdapter<>(context, R.layout.spinneradapter, plev);
                viewHolder.pcoins[j].setAdapter(adapter);
                viewHolder.pcoins[j].setSelection(getIndex(viewHolder.pcoins[j], max[j + 1]));

                pcoins[j + 1] = max[j + 1];
            }
        }

        List<String> ability = Interpret.getAbi(f.du, fragment, StaticStore.addition, 0);
        List<Integer> abilityicon = Interpret.getAbiid(f.du);

        TextInputEditText cdlevt = context.findViewById(R.id.cdlevt);
        TextInputEditText cdtreat = context.findViewById(R.id.cdtreat);
        TextInputEditText atktreat = context.findViewById(R.id.atktreat);
        TextInputEditText healtreat = context.findViewById(R.id.healtreat);

        cdlevt.setText(String.valueOf(t.tech[0]));
        cdtreat.setText(String.valueOf(t.trea[2]));
        atktreat.setText(String.valueOf(t.trea[0]));
        healtreat.setText(String.valueOf(t.trea[1]));


        String language = StaticStore.lang[shared.getInt("Language", 0)];
        if (language.equals("")) {
            language = Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
        }
        List<String> proc;
        if (language.equals("ko")) {
            proc = Interpret.getProc(f.du, 1, fs);
        } else {
            proc = Interpret.getProc(f.du, 0, fs);
        }
        List<Integer> procicon = Interpret.getProcid(f.du);

        viewHolder.uniticon.setImageBitmap(StaticStore.getResizeb((Bitmap) f.anim.uni.getImg().bimg(), context, 48));
        viewHolder.unitname.setText(names.get(i));
        viewHolder.unitid.setText(s.getID(viewHolder, number(id)));
        viewHolder.unithp.setText(s.getHP(f, t, f.unit.getPrefLv(), false, pcoins));
        viewHolder.unithb.setText(s.getHB(f, false, pcoins));
        viewHolder.unitatk.setText(s.getTotAtk(f, t, f.unit.getPrefLv(), false, pcoins));
        viewHolder.unittrait.setText(s.getTrait(f, false, pcoins));
        viewHolder.unitcost.setText(s.getCost(f, false, pcoins));
        viewHolder.unitsimu.setText(s.getSimu(f));
        viewHolder.unitspd.setText(s.getSpd(f, false, pcoins));
        viewHolder.unitcd.setText(s.getCD(f, t, fs, false, pcoins));
        viewHolder.unitrang.setText(s.getRange(f));
        viewHolder.unitpreatk.setText(s.getPre(f, fs));
        viewHolder.unitpost.setText(s.getPost(f, fs));
        viewHolder.unittba.setText(s.getTBA(f, fs));
        viewHolder.unitatkt.setText(s.getAtkTime(f, fs));
        viewHolder.unitabilt.setText(s.getAbilT(f));

        if (ability.size() > 0 || proc.size() > 0) {
            viewHolder.none.setVisibility(View.GONE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            viewHolder.unitabil.setLayoutManager(linearLayoutManager);
            AdapterAbil adapterAbil = new AdapterAbil(ability, proc, abilityicon, procicon, context);
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

        viewHolder.unitname.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (context == null) return false;

                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText(null, viewHolder.unitname.getText());
                clipboardManager.setPrimaryClip(data);

                Toast.makeText(context, R.string.unit_info_copied, Toast.LENGTH_SHORT).show();

                return true;
            }
        });

        Treasure t = BasisSet.current.t();
        Form f = forms[viewHolder.getAdapterPosition()];

        List<Integer> levels = new ArrayList<>();
        for (int j = 1; j < f.unit.max + 1; j++)
            levels.add(j);

        ArrayList<Integer> levelsp = new ArrayList<>();
        for (int j = 0; j < f.unit.maxp + 1; j++)
            levelsp.add(j);

        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<>(context, R.layout.spinneradapter, levels);
        ArrayAdapter<Integer> arrayAdapterp = new ArrayAdapter<>(context, R.layout.spinneradapter, levelsp);

        int currentlev;

        SharedPreferences shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);

        if (shared.getInt("default_level", 50) > f.unit.max)
            currentlev = f.unit.max;
        else if (f.unit.rarity != 0)
            currentlev = shared.getInt("default_level", 50);
        else
            currentlev = f.unit.max;

        viewHolder.unitlevel.setAdapter(arrayAdapter);
        viewHolder.unitlevel.setSelection(getIndex(viewHolder.unitlevel, currentlev));
        viewHolder.unitlevelp.setAdapter(arrayAdapterp);

        if (f.unit.getPrefLv() - f.unit.max < 0) {
            viewHolder.unitlevelp.setSelection(getIndex(viewHolder.unitlevelp, 0));
        } else {
            viewHolder.unitlevelp.setSelection(getIndex(viewHolder.unitlevelp, f.unit.getPrefLv() - f.unit.max));
        }

        if (levelsp.size() == 1) {
            viewHolder.unitlevelp.setVisibility(View.GONE);
            viewHolder.unitplus.setVisibility(View.GONE);
        }

        viewHolder.frse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fs == 0) {
                    fs = 1;
                    viewHolder.unitcd.setText(s.getCD(f, t, fs, talents, pcoins));
                    viewHolder.unitpreatk.setText(s.getPre(f, fs));
                    viewHolder.unitpost.setText(s.getPost(f, fs));
                    viewHolder.unittba.setText(s.getTBA(f, fs));
                    viewHolder.unitatkt.setText(s.getAtkTime(f, fs));
                    viewHolder.frse.setText(context.getString(R.string.unit_info_sec));

                    if (viewHolder.unitabil.getVisibility() != View.GONE) {
                        MaskUnit du = f.du;
                        if (f.getPCoin() != null)
                            du = talents ? f.getPCoin().improve(pcoins) : f.du;

                        List<String> ability = Interpret.getAbi(du, fragment, StaticStore.addition, 0);
                        List<Integer> abilityicon = Interpret.getAbiid(du);

                        String language = Locale.getDefault().getLanguage();
                        List<String> proc;
                        if (language.equals("ko"))
                            proc = Interpret.getProc(du, 1, fs);
                        else
                            proc = Interpret.getProc(du, 0, fs);
                        List<Integer> procicon = Interpret.getProcid(du);

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        viewHolder.unitabil.setLayoutManager(linearLayoutManager);
                        AdapterAbil adapterAbil = new AdapterAbil(ability, proc, abilityicon, procicon, context);
                        viewHolder.unitabil.setAdapter(adapterAbil);
                        ViewCompat.setNestedScrollingEnabled(viewHolder.unitabil, false);
                    }
                } else {
                    fs = 0;
                    viewHolder.unitcd.setText(s.getCD(f, t, fs, talents, pcoins));
                    viewHolder.unitpreatk.setText(s.getPre(f, fs));
                    viewHolder.unitpost.setText(s.getPost(f, fs));
                    viewHolder.unittba.setText(s.getTBA(f, fs));
                    viewHolder.unitatkt.setText(s.getAtkTime(f, fs));
                    viewHolder.frse.setText(context.getString(R.string.unit_info_fr));

                    if (viewHolder.unitabil.getVisibility() != View.GONE) {
                        MaskUnit du = f.du;
                        if (f.getPCoin() != null)
                            du = talents ? f.getPCoin().improve(pcoins) : f.du;

                        List<String> ability = Interpret.getAbi(du, fragment, StaticStore.addition, 0);
                        List<Integer> abilityicon = Interpret.getAbiid(du);

                        String language = Locale.getDefault().getLanguage();
                        List<String> proc;

                        if (language.equals("ko"))
                            proc = Interpret.getProc(du, 1, fs);
                        else
                            proc = Interpret.getProc(du, 0, fs);

                        List<Integer> procicon = Interpret.getProcid(du);

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        viewHolder.unitabil.setLayoutManager(linearLayoutManager);
                        AdapterAbil adapterAbil = new AdapterAbil(ability, proc, abilityicon, procicon, context);
                        viewHolder.unitabil.setAdapter(adapterAbil);
                        ViewCompat.setNestedScrollingEnabled(viewHolder.unitabil, false);
                    }
                }
            }
        });

        viewHolder.unitcdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.unitcd.getText().toString().endsWith("f"))
                    viewHolder.unitcd.setText(s.getCD(f, t, 1, talents, pcoins));
                else
                    viewHolder.unitcd.setText(s.getCD(f, t, 0, talents, pcoins));
            }
        });

        viewHolder.unitpreatkb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.unitpreatk.getText().toString().endsWith("f"))
                    viewHolder.unitpreatk.setText(s.getPre(f, 1));
                else
                    viewHolder.unitpreatk.setText(s.getPre(f, 0));
            }
        });

        viewHolder.unitpostb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.unitpost.getText().toString().endsWith("f"))
                    viewHolder.unitpost.setText(s.getPost(f, 1));
                else
                    viewHolder.unitpost.setText(s.getPost(f, 0));
            }
        });

        viewHolder.unittbab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.unittba.getText().toString().endsWith("f"))
                    viewHolder.unittba.setText(s.getTBA(f, 1));
                else
                    viewHolder.unittba.setText(s.getTBA(f, 0));
            }
        });

        viewHolder.unitatkb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int level = (int) viewHolder.unitlevel.getSelectedItem();
                int levelp = (int) viewHolder.unitlevelp.getSelectedItem();
                if (viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk))) {
                    viewHolder.unitatkb.setText(context.getString(R.string.unit_info_dps));
                    viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp, talents, pcoins));
                } else {
                    viewHolder.unitatkb.setText(context.getString(R.string.unit_info_atk));
                    viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp, talents, pcoins));
                }
            }
        });

        viewHolder.unitatktb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.unitatkt.getText().toString().endsWith("f"))
                    viewHolder.unitatkt.setText(s.getAtkTime(f, 1));
                else
                    viewHolder.unitatkt.setText(s.getAtkTime(f, 0));
            }
        });

        viewHolder.unitlevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int level = (int) viewHolder.unitlevel.getSelectedItem();
                int levelp = (int) viewHolder.unitlevelp.getSelectedItem();
                viewHolder.unithp.setText(s.getHP(f, t, level + levelp, talents, pcoins));

                if (f.du.rawAtkData().length > 1) {
                    if (viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk)))
                        viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp, talents, pcoins));
                    else
                        viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp, talents, pcoins));
                } else {
                    if (viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk)))
                        viewHolder.unitatk.setText(s.getTotAtk(f, t, level + levelp, talents, pcoins));
                    else
                        viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp, talents, pcoins));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        viewHolder.unitlevelp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int level = (int) viewHolder.unitlevel.getSelectedItem();
                int levelp = (int) viewHolder.unitlevelp.getSelectedItem();
                viewHolder.unithp.setText(s.getHP(f, t, level + levelp, talents, pcoins));
                if (f.du.rawAtkData().length > 1) {
                    if (viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk)))
                        viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp, talents, pcoins));
                    else
                        viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp, talents, pcoins));
                } else {
                    if (viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk)))
                        viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp, talents, pcoins));
                    else
                        viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp, talents, pcoins));
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
                if (!s.toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) > 30 || Integer.parseInt(s.toString()) <= 0) {
                        if (cdlev.isHelperTextEnabled()) {
                            cdlev.setHelperTextEnabled(false);
                            cdlev.setErrorEnabled(true);
                            cdlev.setError(context.getString(R.string.treasure_invalid));
                        }
                    } else {
                        if (cdlev.isErrorEnabled()) {
                            cdlev.setError(null);
                            cdlev.setErrorEnabled(false);
                            cdlev.setHelperTextEnabled(true);
                            cdlev.setHelperTextColor(new ColorStateList(states, color));
                            cdlev.setHelperText("1~30");
                        }
                    }
                } else {
                    if (cdlev.isErrorEnabled()) {
                        cdlev.setError(null);
                        cdlev.setErrorEnabled(false);
                        cdlev.setHelperTextEnabled(true);
                        cdlev.setHelperTextColor(new ColorStateList(states, color));
                        cdlev.setHelperText("1~30");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if (!text.toString().isEmpty()) {
                    if (Integer.parseInt(text.toString()) <= 30 && Integer.parseInt(text.toString()) > 0) {
                        int lev = Integer.parseInt(text.toString());

                        t.tech[0] = lev;

                        if (viewHolder.unitcd.getText().toString().endsWith("s")) {
                            viewHolder.unitcd.setText(s.getCD(f, t, 1, talents, pcoins));
                        } else {
                            viewHolder.unitcd.setText(s.getCD(f, t, 0, talents, pcoins));
                        }
                    }
                } else {
                    t.tech[0] = 1;
                    if (viewHolder.unitcd.getText().toString().endsWith("s")) {
                        viewHolder.unitcd.setText(s.getCD(f, t, 1, talents, pcoins));
                    } else {
                        viewHolder.unitcd.setText(s.getCD(f, t, 0, talents, pcoins));
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
                if (!s.toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) > 300) {
                        if (cdtrea.isHelperTextEnabled()) {
                            cdtrea.setHelperTextEnabled(false);
                            cdtrea.setErrorEnabled(true);
                            cdtrea.setError(context.getString(R.string.treasure_invalid));
                        }
                    } else {
                        if (cdtrea.isErrorEnabled()) {
                            cdtrea.setError(null);
                            cdtrea.setErrorEnabled(false);
                            cdtrea.setHelperTextEnabled(true);
                            cdtrea.setHelperTextColor(new ColorStateList(states, color));
                            cdtrea.setHelperText("0~300");
                        }
                    }
                } else {
                    if (cdtrea.isErrorEnabled()) {
                        cdtrea.setError(null);
                        cdtrea.setErrorEnabled(false);
                        cdtrea.setHelperTextEnabled(true);
                        cdtrea.setHelperTextColor(new ColorStateList(states, color));
                        cdtrea.setHelperText("0~300");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if (!text.toString().isEmpty()) {
                    if (Integer.parseInt(text.toString()) <= 300) {
                        int trea = Integer.parseInt(text.toString());

                        t.trea[2] = trea;

                        if (viewHolder.unitcd.getText().toString().endsWith("s")) {
                            viewHolder.unitcd.setText(s.getCD(f, t, 1, talents, pcoins));
                        } else {
                            viewHolder.unitcd.setText(s.getCD(f, t, 0, talents, pcoins));
                        }
                    }
                } else {
                    t.trea[2] = 0;
                    if (viewHolder.unitcd.getText().toString().endsWith("s")) {
                        viewHolder.unitcd.setText(s.getCD(f, t, 1, talents, pcoins));
                    } else {
                        viewHolder.unitcd.setText(s.getCD(f, t, 0, talents, pcoins));
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
                if (!s.toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) > 300) {
                        if (atktrea.isHelperTextEnabled()) {
                            atktrea.setHelperTextEnabled(false);
                            atktrea.setErrorEnabled(true);
                            atktrea.setError(context.getString(R.string.treasure_invalid));
                        }
                    } else {
                        if (atktrea.isErrorEnabled()) {
                            atktrea.setError(null);
                            atktrea.setErrorEnabled(false);
                            atktrea.setHelperTextEnabled(true);
                            atktrea.setHelperTextColor(new ColorStateList(states, color));
                            atktrea.setHelperText("0~300");
                        }
                    }
                } else {
                    if (atktrea.isErrorEnabled()) {
                        atktrea.setError(null);
                        atktrea.setErrorEnabled(false);
                        atktrea.setHelperTextEnabled(true);
                        atktrea.setHelperTextColor(new ColorStateList(states, color));
                        atktrea.setHelperText("0~300");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if (!text.toString().isEmpty()) {
                    if (Integer.parseInt(text.toString()) <= 300) {
                        int trea = Integer.parseInt(text.toString());

                        t.trea[0] = trea;
                        int level = (int) viewHolder.unitlevel.getSelectedItem();
                        int levelp = (int) viewHolder.unitlevelp.getSelectedItem();

                        if (viewHolder.unitatkb.getText().toString().equals(context.getString(R.string.unit_info_dps))) {
                            viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp, talents, pcoins));
                        } else {
                            viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp, talents, pcoins));
                        }
                    }
                } else {
                    t.trea[0] = 0;
                    int level = (int) viewHolder.unitlevel.getSelectedItem();
                    int levelp = (int) viewHolder.unitlevelp.getSelectedItem();

                    if (viewHolder.unitatkb.getText().toString().equals(context.getString(R.string.unit_info_dps))) {
                        viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp, talents, pcoins));
                    } else {
                        viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp, talents, pcoins));
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
                if (!s.toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) > 300) {
                        if (healtrea.isHelperTextEnabled()) {
                            healtrea.setHelperTextEnabled(false);
                            healtrea.setErrorEnabled(true);
                            healtrea.setError(context.getString(R.string.treasure_invalid));
                        }
                    } else {
                        if (healtrea.isErrorEnabled()) {
                            healtrea.setError(null);
                            healtrea.setErrorEnabled(false);
                            healtrea.setHelperTextEnabled(true);
                            healtrea.setHelperTextColor(new ColorStateList(states, color));
                            healtrea.setHelperText("0~300");
                        }
                    }
                } else {
                    if (healtrea.isErrorEnabled()) {
                        healtrea.setError(null);
                        healtrea.setErrorEnabled(false);
                        healtrea.setHelperTextEnabled(true);
                        healtrea.setHelperTextColor(new ColorStateList(states, color));
                        healtrea.setHelperText("0~300");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if (!text.toString().isEmpty()) {
                    if (Integer.parseInt(text.toString()) <= 300) {
                        int trea = Integer.parseInt(text.toString());

                        t.trea[1] = trea;
                        int level = (int) viewHolder.unitlevel.getSelectedItem();
                        int levelp = (int) viewHolder.unitlevelp.getSelectedItem();

                        viewHolder.unithp.setText(s.getHP(f, t, level + levelp, talents, pcoins));
                    }
                } else {
                    t.trea[1] = 0;
                    int level = (int) viewHolder.unitlevel.getSelectedItem();
                    int levelp = (int) viewHolder.unitlevelp.getSelectedItem();

                    viewHolder.unithp.setText(s.getHP(f, t, level + levelp, talents, pcoins));
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

                int level = (int) viewHolder.unitlevel.getSelectedItem();
                int levelp = (int) viewHolder.unitlevelp.getSelectedItem();

                if (viewHolder.unitcd.getText().toString().endsWith("s")) {
                    viewHolder.unitcd.setText(s.getCD(f, t, 1, talents, pcoins));
                } else {
                    viewHolder.unitcd.setText(s.getCD(f, t, 0, talents, pcoins));
                }

                if (viewHolder.unitatkb.getText().toString().equals(context.getString(R.string.unit_info_dps))) {
                    viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp, talents, pcoins));
                } else {
                    viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp, talents, pcoins));
                }

                viewHolder.unithp.setText(s.getHP(f, t, level + levelp, talents, pcoins));
            }
        });

        viewHolder.unittalen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                talents = true;
                validate(viewHolder, f, t);
                if (isChecked) {
                    ValueAnimator anim = ValueAnimator.ofInt(0, StaticStore.dptopx(100f, context));
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int val = (Integer) animation.getAnimatedValue();
                            ViewGroup.LayoutParams layout = viewHolder.npresetrow.getLayoutParams();
                            layout.width = val;
                            viewHolder.npresetrow.setLayoutParams(layout);
                        }
                    });
                    anim.setDuration(300);
                    anim.setInterpolator(new DecelerateInterpolator());
                    anim.start();

                    ValueAnimator anim2;
                    if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        anim2 = ValueAnimator.ofInt(0, StaticStore.dptopx(48f, context));
                    } else {
                        anim2 = ValueAnimator.ofInt(0, StaticStore.dptopx(56f, context));
                    }

                    anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) viewHolder.nprow.getLayoutParams();
                            params.height = (int) (Integer) animation.getAnimatedValue();
                            viewHolder.nprow.setLayoutParams(params);
                        }
                    });
                    anim2.setDuration(300);
                    anim2.setInterpolator(new DecelerateInterpolator());
                    anim2.start();

                    ValueAnimator anim3 = ValueAnimator.ofInt(0, StaticStore.dptopx(16f, context));
                    anim3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) viewHolder.nprow.getLayoutParams();
                            params.topMargin = (int) animation.getAnimatedValue();
                            viewHolder.nprow.setLayoutParams(params);
                        }
                    });
                    anim3.setDuration(300);
                    anim3.setInterpolator(new DecelerateInterpolator());
                    anim3.start();
                } else {
                    talents = false;
                    validate(viewHolder, f, t);
                    ValueAnimator anim = ValueAnimator.ofInt(StaticStore.dptopx(100f, context), 0);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int val = (Integer) animation.getAnimatedValue();
                            ViewGroup.LayoutParams layout = viewHolder.npresetrow.getLayoutParams();
                            layout.width = val;
                            viewHolder.npresetrow.setLayoutParams(layout);
                        }
                    });
                    anim.setDuration(300);
                    anim.setInterpolator(new DecelerateInterpolator());
                    anim.start();

                    ValueAnimator anim2;
                    if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                        anim2 = ValueAnimator.ofInt(StaticStore.dptopx(48f, context), 0);
                    else
                        anim2 = ValueAnimator.ofInt(StaticStore.dptopx(56f, context), 0);
                    anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) viewHolder.nprow.getLayoutParams();
                            params.height = (int) animation.getAnimatedValue();
                            viewHolder.nprow.setLayoutParams(params);
                        }
                    });
                    anim2.setDuration(300);
                    anim2.setInterpolator(new DecelerateInterpolator());
                    anim2.start();

                    ValueAnimator anim3 = ValueAnimator.ofInt(StaticStore.dptopx(16f, context), 0);
                    anim3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) viewHolder.nprow.getLayoutParams();
                            params.topMargin = (int) animation.getAnimatedValue();
                            viewHolder.nprow.setLayoutParams(params);
                        }
                    });
                    anim3.setDuration(300);
                    anim3.setInterpolator(new DecelerateInterpolator());
                    anim3.start();
                }
            }
        });

        for (int i = 0; i < viewHolder.pcoins.length; i++) {
            final int finals = i;
            viewHolder.pcoins[i].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    pcoins[finals + 1] = (int) viewHolder.pcoins[finals].getSelectedItem();
                    validate(viewHolder, f, t);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            viewHolder.pcoins[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    viewHolder.pcoins[finals].setClickable(false);
                    Toast.makeText(context, s.getTalentName(finals, f), Toast.LENGTH_SHORT).show();

                    return true;
                }
            });
        }

        viewHolder.npreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < viewHolder.pcoins.length; i++) {
                    viewHolder.pcoins[i].setSelection(getIndex(viewHolder.pcoins[i], f.getPCoin().max[i + 1]));
                    pcoins[i + 1] = f.getPCoin().max[i + 1];
                }

                validate(viewHolder, f, t);
            }
        });
    }

    private int getIndex(Spinner spinner, int lev) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++)
            if (lev == (int) spinner.getItemAtPosition(i))
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
        CheckBox unittalen;
        TableRow npresetrow;
        Button npreset;
        TableRow nprow;
        int[] ids = {R.id.talent0, R.id.talent1, R.id.talent2, R.id.talent3, R.id.talent4};
        Spinner[] pcoins = new Spinner[ids.length];

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
            unittalen = itemView.findViewById(R.id.unitinftalen);
            npreset = itemView.findViewById(R.id.unitinftalreset);
            npresetrow = itemView.findViewById(R.id.talresetrow);
            nprow = itemView.findViewById(R.id.talenrow);
            for (int i = 0; i < ids.length; i++)
                pcoins[i] = itemView.findViewById(ids[i]);
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

    private static int getAttributeColor(Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = ContextCompat.getColor(context, colorRes);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return color;
    }

    private void validate(ViewHolder viewHolder, Form f, Treasure t) {
        int level = (int) viewHolder.unitlevel.getSelectedItem();
        int levelp = (int) viewHolder.unitlevelp.getSelectedItem();
        viewHolder.unithp.setText(s.getHP(f, t, level + levelp, talents, pcoins));
        viewHolder.unithb.setText(s.getHB(f, talents, pcoins));
        if (viewHolder.unitatkb.getText().toString().equals("DPS"))
            viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp, talents, pcoins));
        else
            viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp, talents, pcoins));
        viewHolder.unitcost.setText(s.getCost(f, talents, pcoins));
        if (viewHolder.unitcd.getText().toString().endsWith("s"))
            viewHolder.unitcd.setText(s.getCD(f, t, 1, talents, pcoins));
        else
            viewHolder.unitcd.setText(s.getCD(f, t, 0, talents, pcoins));
        viewHolder.unittrait.setText(s.getTrait(f, talents, pcoins));
        viewHolder.unitspd.setText(s.getSpd(f, talents, pcoins));

        MaskUnit du;

        if (f.getPCoin() != null)
            du = talents ? f.getPCoin().improve(pcoins) : f.du;
        else
            du = f.du;

        List<String> abil = Interpret.getAbi(du, fragment, StaticStore.addition, 0);

        SharedPreferences shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);

        String language = StaticStore.lang[shared.getInt("Language", 0)];
        if (language.equals("")) {
            language = Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
        }
        List<String> proc;
        if (language.equals("ko")) {
            proc = Interpret.getProc(du, 1, fs);
        } else {
            proc = Interpret.getProc(du, 0, fs);
        }

        List<Integer> abilityicon = Interpret.getAbiid(du);
        List<Integer> procicon = Interpret.getProcid(du);

        if (abil.size() > 0 || proc.size() > 0) {
            viewHolder.none.setVisibility(View.GONE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            viewHolder.unitabil.setLayoutManager(linearLayoutManager);
            AdapterAbil adapterAbil = new AdapterAbil(abil, proc, abilityicon, procicon, context);
            viewHolder.unitabil.setAdapter(adapterAbil);
            ViewCompat.setNestedScrollingEnabled(viewHolder.unitabil, false);
        } else {
            viewHolder.unitabil.setVisibility(View.GONE);
        }
    }
}
