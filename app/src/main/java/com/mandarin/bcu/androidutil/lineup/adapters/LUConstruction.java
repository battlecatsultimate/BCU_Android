package com.mandarin.bcu.androidutil.lineup.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import common.battle.BasisSet;
import common.battle.Treasure;
import common.io.OutStream;

public class LUConstruction extends Fragment {

    private boolean initialized = false;
    private boolean editable = true;
    private boolean destroyed = false;

    private int[] layoutid = {R.id.castlelev, R.id.slowlev, R.id.walllev, R.id.stoplev, R.id.waterlev, R.id.zombielev, R.id.breakerlev};

    private int[] textid = {R.id.castlelevt, R.id.slowlevt, R.id.walllevt, R.id.stoplevt, R.id.waterlevt, R.id.zombielevt, R.id.breakerlevt};

    public static LUConstruction newInstance() {
        return new LUConstruction();
    }

    private int[][] states = new int[][]{
            new int[]{android.R.attr.state_enabled}
    };

    private int[] color;

    private Handler handler = new Handler();
    private Runnable runnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup group, @Nullable Bundle bundle) {
        View view = inflater.inflate(R.layout.lineup_construction, group, false);

        if (getContext() == null) return view;

        color = new int[]{
                getAttributeColor(getContext(), R.attr.TextPrimary)
        };

        Listeners(view);

        runnable = new Runnable() {
            @Override
            public void run() {
                if (StaticStore.updateConst) {
                    initialized = false;

                    TextInputEditText[] texts = new TextInputEditText[7];

                    TextInputEditText text = view.findViewById(R.id.constlevt);

                    if (ValuesAllSame())
                        text.setText(String.valueOf(BasisSet.current.t().bslv[0]));

                    int[] vals = BasisSet.current.t().bslv;

                    for (int i = 0; i < vals.length; i++) {
                        texts[i] = view.findViewById(textid[i]);
                        texts[i].setText(String.valueOf(vals[i]));
                    }

                    initialized = true;

                    StaticStore.updateConst = false;
                }

                if (!destroyed)
                    handler.postDelayed(this, 50);
            }
        };

        handler.postDelayed(runnable, 50);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private void Listeners(View view) {
        TextInputLayout[] constructions = new TextInputLayout[7];
        TextInputLayout construction = view.findViewById(R.id.constlev);
        TextInputEditText text = view.findViewById(R.id.constlevt);
        TextInputEditText[] texts = new TextInputEditText[7];

        for (int i = 0; i < layoutid.length; i++) {
            constructions[i] = view.findViewById(layoutid[i]);
            texts[i] = view.findViewById(textid[i]);
        }

        construction.setHelperTextColor(new ColorStateList(states, color));
        SetListenerforTextInputLayouts(constructions);

        if (ValuesAllSame())
            text.setText(String.valueOf(BasisSet.current.t().bslv[0]));

        int[] vals = BasisSet.current.t().bslv;

        for (int i = 0; i < vals.length; i++) {
            texts[i].setText(String.valueOf(vals[i]));
        }

        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) > 20 || Integer.parseInt(s.toString()) < 1) {
                        if (construction.isHelperTextEnabled()) {
                            construction.setHelperTextEnabled(false);
                            construction.setErrorEnabled(true);
                            construction.setError(getContext().getString(R.string.treasure_invalid));
                        }
                    } else {
                        if (construction.isErrorEnabled()) {
                            construction.setError(null);
                            construction.setErrorEnabled(false);
                            construction.setHelperTextEnabled(true);
                            construction.setHelperTextColor(new ColorStateList(states, color));
                            construction.setHelperText("1~20 Lv.");
                        }
                    }
                } else {
                    if (construction.isErrorEnabled()) {
                        construction.setError(null);
                        construction.setErrorEnabled(false);
                        construction.setHelperTextEnabled(true);
                        construction.setHelperTextColor(new ColorStateList(states, color));
                        construction.setHelperText("1~20 Lv.");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    Treasure t = BasisSet.current.t();

                    editable = false;

                    if (Integer.parseInt(s.toString()) <= 20 && Integer.parseInt(s.toString()) >= 1) {
                        int val = Integer.parseInt(s.toString());

                        for (int i = 0; i < texts.length; i++) {
                            t.bslv[i] = val;
                            texts[i].setText(String.valueOf(val));
                        }
                    } else {
                        for (int i = 0; i < texts.length; i++) {
                            t.bslv[i] = 20;
                            texts[i].setText(String.valueOf(20));
                        }
                    }

                    editable = true;
                }
            }
        });

        SetListenerforTextInputEditTexts(construction,text,constructions,texts);

        initialized = true;
    }

    private void SetListenerforTextInputLayouts(TextInputLayout[] layouts) {
        for (TextInputLayout t : layouts) {
            t.setHelperTextColor(new ColorStateList(states, color));
        }
    }

    private void SetListenerforTextInputEditTexts(TextInputLayout construction, TextInputEditText text, TextInputLayout[] constructions, TextInputEditText[] texts) {
        if (getContext() == null) return;

        for (int i = 0; i < texts.length; i++) {
            final int ii = i;

            texts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!s.toString().isEmpty()) {
                        if (Integer.parseInt(s.toString()) > 20 || Integer.parseInt(s.toString()) < 1) {
                            if (constructions[ii].isHelperTextEnabled()) {
                                constructions[ii].setHelperTextEnabled(false);
                                constructions[ii].setErrorEnabled(true);
                                constructions[ii].setError(getContext().getString(R.string.treasure_invalid));
                            }
                        } else {
                            if (constructions[ii].isErrorEnabled()) {
                                constructions[ii].setError(null);
                                constructions[ii].setErrorEnabled(false);
                                constructions[ii].setHelperTextEnabled(true);
                                constructions[ii].setHelperTextColor(new ColorStateList(states, color));
                                constructions[ii].setHelperText("1~20 Lv.");
                            }
                        }
                    } else {
                        if (constructions[ii].isErrorEnabled()) {
                            constructions[ii].setError(null);
                            constructions[ii].setErrorEnabled(false);
                            constructions[ii].setHelperTextEnabled(true);
                            constructions[ii].setHelperTextColor(new ColorStateList(states, color));
                            constructions[ii].setHelperText("1~20 Lv.");
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!initialized) return;

                    if (!s.toString().isEmpty()) {
                        Treasure t = BasisSet.current.t();

                        if (editable && Integer.parseInt(s.toString()) <= 20 && Integer.parseInt(s.toString()) >= 1) {
                            int val = Integer.parseInt(s.toString());

                            t.bslv[ii] = val;

                            text.setText("");
                            construction.setHelperTextEnabled(true);
                            construction.setHelperText("1~20 Lv.");
                        }

                        save();
                    }
                }
            });
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

    private void save() {
        String Path = Environment.getExternalStorageDirectory().getPath() + "/BCU/user/basis.v";
        String Direct = Environment.getExternalStorageDirectory().getPath() + "/BCU/user/";

        File g = new File(Direct);

        if (!g.exists())
            g.mkdirs();

        File f = new File(Path);

        try {
            if (!f.exists())
                f.createNewFile();

            OutputStream os = new FileOutputStream(f);

            OutStream out = BasisSet.writeAll();

            out.flush(os);

            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean ValuesAllSame() {
        int[] bases = BasisSet.current.t().bslv;

        if (bases == null) return false;

        int check = bases[0];

        for (int i = 1; i < bases.length; i++) {
            if (check != bases[i])
                return false;
        }

        return true;
    }
}
