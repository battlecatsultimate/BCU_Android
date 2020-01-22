package com.mandarin.bcu.androidutil.lineup.adapters;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import common.battle.BasisSet;
import common.battle.Treasure;
import common.io.OutStream;

public class LUTreasureSetting extends Fragment {

    public static LUTreasureSetting newInstance() {
        return new LUTreasureSetting();
    }

    private boolean CanbeEdited = true;
    private boolean Initialized = false;
    private boolean destroyed = false;

    private int[] techid = {R.id.cdlev, R.id.aclev, R.id.basehlev, R.id.worklev, R.id.walletlev, R.id.rechargelev};
    private int[] eocid = {R.id.atktrea, R.id.healtrea, R.id.cdtrea, R.id.actrea, R.id.worktrea, R.id.wallettrea};
    private int[] eocitfid = {R.id.rechargetrea, R.id.canatktrea, R.id.basehtrea};
    private int[] itfid = {R.id.redfrtrea, R.id.floatfrtrea, R.id.blacktrea, R.id.angelfrtrea};
    private int[] cotcid = {R.id.metalfrtrea, R.id.zombiefrtrea, R.id.alienfrtrea};
    private int[] maskid = {R.id.masktrea, R.id.mask2trea, R.id.mask3trea};

    private int[] limitvals = {30, 30, 10, 300, 600, 300, 300, 600, 1500};
    private int[] limitmins = {1, 1, 1, 0, 0, 0, 0, 0, 0};
    private int[] limitvalss = {30, 300, 600, 300, 300, 100};
    private int[] limitminss = {1, 0, 0, 0, 0, 0};
    private String[] helpers = {"1~30 lv", "1~30 lv", "1~10 lv", "0~300 %", "0~600 %", "0~300 %", "0~300 %", "0~600 %", "0~1500 %"};
    private String[] helperss = {"1~30 lv", "0~300 %", "0~600 %", "0~300 %", "0~300 %", "0~100 %"};

    private int[] techeid = {R.id.cdlevt, R.id.aclevt, R.id.basehlevt, R.id.worklevt, R.id.walletlevt, R.id.rechargelevt};
    private int[] eoceid = {R.id.atktreat, R.id.healtreat, R.id.cdtreat, R.id.actreat, R.id.worktreat, R.id.wallettreat};
    private int[] eocitfeid = {R.id.rechargetreat, R.id.canatktreat, R.id.basehtreat};
    private int[] itfeid = {R.id.redfrtreat, R.id.floatfrtreat, R.id.blacktreat, R.id.angelfrtreat};
    private int[] cotceid = {R.id.metalfrtreat, R.id.zombiefrtreat, R.id.alienfrtreat};
    private int[] maskeid = {R.id.masktreat, R.id.mask2treat, R.id.mask3treat};

    private int[] expandid = {R.id.techexpand, R.id.eocexpand, R.id.eocitfexpand, R.id.itfexpand, R.id.cotcexpand};

    private int[] layoutid = {R.id.techlayout, R.id.eoclayout, R.id.eocitflayout, R.id.itffruitlayout, R.id.cotclayout};

    private int[][] states = new int[][]{
            new int[]{android.R.attr.state_enabled}
    };

    private int [] tilid = {R.id.techlev, R.id.canatklev, R.id.canrangelev, R.id.eoctrea, R.id.eocitftrea, R.id.itffruittrea, R.id.cotctrea, R.id.itfcrytrea, R.id.cotccrytrea};
    private int [][] tilsid = {techid,eocid,eocitfid,itfid,cotcid,maskid};

    private Handler handler = new Handler();

    private int[] color;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup group, @Nullable Bundle bundle) {
        View view = inflater.inflate(R.layout.lineup_treasure_set, group, false);

        color = new int[]{
                getAttributeColor(Objects.requireNonNull(getContext()), R.attr.TextPrimary)
        };

        Listeners(view);

        TextInputEditText teche = view.findViewById(R.id.techlevt);
        TextInputEditText canatke = view.findViewById(R.id.canatklevt);
        TextInputEditText canrangee = view.findViewById(R.id.canrangelevt);
        TextInputEditText eoce = view.findViewById(R.id.eoctreat);
        TextInputEditText eocitfe = view.findViewById(R.id.eocitftreat);

        TextInputEditText[] teches = new TextInputEditText[6];

        for (int i = 0; i < teches.length; i++) {
            teches[i] = view.findViewById(techeid[i]);
        }

        TextInputEditText[] eoces = new TextInputEditText[6];

        for (int i = 0; i < eoces.length; i++) {
            eoces[i] = view.findViewById(eoceid[i]);
        }

        TextInputEditText[] eocitfes = new TextInputEditText[3];

        for (int i = 0; i < eocitfes.length; i++) {
            eocitfes[i] = view.findViewById(eocitfeid[i]);
        }

        TextInputEditText itfe = view.findViewById(R.id.itffruittreat);

        TextInputEditText[] itfes = new TextInputEditText[4];

        for (int i = 0; i < itfes.length; i++) {
            itfes[i] = view.findViewById(itfeid[i]);
        }

        TextInputEditText cotce = view.findViewById(R.id.cotctreat);

        TextInputEditText[] cotces = new TextInputEditText[3];

        for (int i = 0; i < cotces.length; i++) {
            cotces[i] = view.findViewById(cotceid[i]);
        }

        TextInputEditText itfcrye = view.findViewById(R.id.itfcrytreat);

        TextInputEditText cotccrye = view.findViewById(R.id.cotccrytreat);

        TextInputEditText[] maskes = new TextInputEditText[3];

        for (int i = 0; i < maskes.length; i++) {
            maskes[i] = view.findViewById(maskeid[i]);
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (StaticStore.updateTreasure) {
                    Treasure t = BasisSet.current.t();

                    Initialized = false;

                    itfcrye.setText(String.valueOf(t.alien));
                    cotccrye.setText(String.valueOf(t.star));

                    for (int i = 0; i < 6; i++) {
                        teches[i].setText(String.valueOf(t.tech[i]));
                    }

                    canatke.setText(String.valueOf(t.tech[6]));
                    canrangee.setText(String.valueOf(t.tech[7]));

                    for (int i = 0; i < 6; i++) {
                        eoces[i].setText(String.valueOf(t.trea[i]));
                    }

                    for (int i = 0; i < eocitfes.length; i++) {
                        eocitfes[i].setText(String.valueOf(t.trea[i + 6]));
                    }

                    for (int i = 0; i < 4; i++) {
                        itfes[i].setText(String.valueOf(t.fruit[i]));
                    }

                    for (int i = 4; i < t.fruit.length; i++) {
                        cotces[i - 4].setText(String.valueOf(t.fruit[i]));
                    }

                    for (int i = 0; i < t.gods.length; i++) {
                        maskes[i].setText(String.valueOf(t.gods[i]));
                    }


                    if (ValuesAllSame(0))
                        teche.setText(String.valueOf(t.tech[0]));

                    if (ValuesAllSame(1))
                        eoce.setText(String.valueOf(t.trea[0]));

                    if (ValuesAllSame(2))
                        eocitfe.setText(String.valueOf(t.trea[6]));

                    if (ValuesAllSame(3))
                        itfe.setText(String.valueOf(t.fruit[0]));

                    if (ValuesAllSame(4))
                        cotce.setText(String.valueOf(t.fruit[4]));

                    Initialized = true;

                    StaticStore.updateTreasure = false;
                }

                if (!destroyed)
                    handler.postDelayed(this, 50);
            }
        };

        handler.postDelayed(runnable, 50);

        return view;
    }

    private void Listeners(View view) {
        Treasure t = BasisSet.current.t();

        TextInputLayout tech = view.findViewById(R.id.techlev);
        TextInputEditText teche = view.findViewById(R.id.techlevt);

        TextInputLayout[] techs = new TextInputLayout[6];
        TextInputEditText[] teches = new TextInputEditText[6];

        for (int i = 0; i < techs.length; i++) {
            techs[i] = view.findViewById(techid[i]);
            teches[i] = view.findViewById(techeid[i]);
        }

        TextInputLayout canatk = view.findViewById(R.id.canatklev);
        TextInputEditText canatke = view.findViewById(R.id.canatklevt);

        TextInputLayout canrange = view.findViewById(R.id.canrangelev);
        TextInputEditText canrangee = view.findViewById(R.id.canrangelevt);

        TextInputLayout eoc = view.findViewById(R.id.eoctrea);
        TextInputEditText eoce = view.findViewById(R.id.eoctreat);

        TextInputLayout eocitf = view.findViewById(R.id.eocitftrea);
        TextInputEditText eocitfe = view.findViewById(R.id.eocitftreat);

        TextInputLayout[] eocs = new TextInputLayout[6];
        TextInputEditText[] eoces = new TextInputEditText[6];

        for (int i = 0; i < eocs.length; i++) {
            eocs[i] = view.findViewById(eocid[i]);
            eoces[i] = view.findViewById(eoceid[i]);
        }

        TextInputLayout itf = view.findViewById(R.id.itffruittrea);
        TextInputEditText itfe = view.findViewById(R.id.itffruittreat);

        TextInputLayout[] eocitfs = new TextInputLayout[3];
        TextInputEditText[] eocitfes = new TextInputEditText[3];

        for (int i = 0; i < eocitfs.length; i++) {
            eocitfs[i] = view.findViewById(eocitfid[i]);
            eocitfes[i] = view.findViewById(eocitfeid[i]);
        }

        TextInputLayout[] itfs = new TextInputLayout[4];
        TextInputEditText[] itfes = new TextInputEditText[4];

        for (int i = 0; i < itfs.length; i++) {
            itfs[i] = view.findViewById(itfid[i]);
            itfes[i] = view.findViewById(itfeid[i]);
        }

        TextInputLayout cotc = view.findViewById(R.id.cotctrea);
        TextInputEditText cotce = view.findViewById(R.id.cotctreat);

        TextInputLayout[] cotcs = new TextInputLayout[3];
        TextInputEditText[] cotces = new TextInputEditText[3];

        for (int i = 0; i < cotcs.length; i++) {
            cotcs[i] = view.findViewById(cotcid[i]);
            cotces[i] = view.findViewById(cotceid[i]);
        }

        TextInputLayout itfcry = view.findViewById(R.id.itfcrytrea);
        TextInputEditText itfcrye = view.findViewById(R.id.itfcrytreat);

        TextInputLayout cotccry = view.findViewById(R.id.cotccrytrea);
        TextInputEditText cotccrye = view.findViewById(R.id.cotccrytreat);

        TextInputLayout[] masks = new TextInputLayout[3];
        TextInputEditText[] maskes = new TextInputEditText[3];

        for (int i = 0; i < masks.length; i++) {
            masks[i] = view.findViewById(maskid[i]);
            maskes[i] = view.findViewById(maskeid[i]);
        }

        FloatingActionButton[] expands = new FloatingActionButton[5];
        LinearLayout[] layouts = new LinearLayout[5];

        for (int i = 0; i < expands.length; i++) {
            expands[i] = view.findViewById(expandid[i]);
            layouts[i] = view.findViewById(layoutid[i]);
        }

        //Listeners for expand image buttons
        for (int i = 0; i < expands.length; i++) {
            final int ii = i;

            expands[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - StaticStore.infoClick < StaticStore.INFO_INTERVAL)
                        return;

                    StaticStore.infoClick = SystemClock.elapsedRealtime();

                    if (layouts[ii].getHeight() == 0) {
                        layouts[ii].measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        int height = layouts[ii].getMeasuredHeight();

                        ValueAnimator anim = ValueAnimator.ofInt(0, height);
                        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int val = (Integer) animation.getAnimatedValue();
                                ViewGroup.LayoutParams params = layouts[ii].getLayoutParams();
                                params.height = val;
                                layouts[ii].setLayoutParams(params);
                            }
                        });

                        anim.setDuration(300);
                        anim.setInterpolator(new DecelerateInterpolator());
                        anim.start();

                        expands[ii].setImageDrawable(Objects.requireNonNull(getContext()).getDrawable(R.drawable.ic_expand_more_black_24dp));
                    } else {
                        layouts[ii].measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        int height = layouts[ii].getMeasuredHeight();

                        ValueAnimator anim = ValueAnimator.ofInt(height, 0);
                        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int val = (Integer) animation.getAnimatedValue();
                                ViewGroup.LayoutParams params = layouts[ii].getLayoutParams();
                                params.height = val;
                                layouts[ii].setLayoutParams(params);
                            }
                        });

                        anim.setDuration(300);
                        anim.setInterpolator(new DecelerateInterpolator());
                        anim.start();

                        expands[ii].setImageDrawable(Objects.requireNonNull(getContext()).getDrawable(R.drawable.ic_expand_less_black_24dp));
                    }
                }
            });
        }

        //Listeners for TextInputLayout

        itfcrye.setText(String.valueOf(t.alien));
        cotccrye.setText(String.valueOf(t.star));

        SetListenerforTextInputLayout(tech, canatk, canrange, eoc, eocitf, itf, cotc, itfcry, cotccry);
        SetListenerforTextInputLayouts(techs, eocs, eocitfs, itfs, cotcs, masks);

        //Listeners for TextInputLayouts

        for (int i = 0; i < 6; i++) {
            teches[i].setText(String.valueOf(t.tech[i]));
        }

        canatke.setText(String.valueOf(t.tech[6]));
        canrangee.setText(String.valueOf(t.tech[7]));

        for (int i = 0; i < 6; i++) {
            eoces[i].setText(String.valueOf(t.trea[i]));
        }

        for (int i = 0; i < eocitfes.length; i++) {
            eocitfes[i].setText(String.valueOf(t.trea[i + 6]));
        }

        for (int i = 0; i < 4; i++) {
            itfes[i].setText(String.valueOf(t.fruit[i]));
        }

        for (int i = 4; i < t.fruit.length; i++) {
            cotces[i - 4].setText(String.valueOf(t.fruit[i]));
        }

        for (int i = 0; i < t.gods.length; i++) {
            maskes[i].setText(String.valueOf(t.gods[i]));
        }


        if (ValuesAllSame(0))
            teche.setText(String.valueOf(t.tech[0]));

        if (ValuesAllSame(1))
            eoce.setText(String.valueOf(t.trea[0]));

        if (ValuesAllSame(2))
            eocitfe.setText(String.valueOf(t.trea[6]));

        if (ValuesAllSame(3))
            itfe.setText(String.valueOf(t.fruit[0]));

        if (ValuesAllSame(4))
            cotce.setText(String.valueOf(t.fruit[4]));

        SetListenerforTextInputEditText(teche, canatke, canrangee, eoce, eocitfe, itfe, cotce, itfcrye, cotccrye);
        SetListenerforTextInptEditTexts(view, teches, eoces, eocitfes, itfes, cotces, maskes);

        Initialized = true;
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

    private boolean ValuesAllSame(int mode) {
        switch (mode) {
            case 0:
                int val = BasisSet.current.t().tech[0];

                for (int i = 1; i < 6; i++) {
                    if (val != BasisSet.current.t().tech[i])
                        return false;
                }

                return true;
            case 1:
                val = BasisSet.current.t().trea[0];

                for (int i = 1; i < 6; i++) {
                    if (val != BasisSet.current.t().trea[i])
                        return false;
                }

                return true;
            case 2:
                val = BasisSet.current.t().trea[6];

                for (int i = 7; i < 9; i++)
                    if (val != BasisSet.current.t().trea[i])
                        return false;

                return true;
            case 3:
                val = BasisSet.current.t().fruit[0];

                for (int i = 1; i < 4; i++)
                    if (val != BasisSet.current.t().fruit[i])
                        return false;

                return true;
            case 4:
                val = BasisSet.current.t().fruit[4];

                for (int i = 5; i < BasisSet.current.t().fruit.length; i++)
                    if (val != BasisSet.current.t().fruit[i])
                        return false;

                return true;
        }

        return false;
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

    private void SetListenerforTextInputLayout(TextInputLayout... texts) {
        for (TextInputLayout t : texts) {
            t.setHelperTextColor(new ColorStateList(states, color));
        }
    }

    private void SetListenerforTextInputLayouts(TextInputLayout[]... texts) {
        for (TextInputLayout[] ts : texts) {
            for (TextInputLayout t : ts) {
                t.setHelperTextColor(new ColorStateList(states, color));
            }
        }
    }

    private void SetListenerforTextInputEditText(View view, TextInputEditText... texts) {
        if (getContext() == null) return;

        TextInputEditText[] teches = new TextInputEditText[6];

        for (int i = 0; i < teches.length; i++) {
            teches[i] = view.findViewById(techeid[i]);
        }

        TextInputEditText[] eoces = new TextInputEditText[6];

        for (int i = 0; i < eoces.length; i++) {
            eoces[i] = view.findViewById(eoceid[i]);
        }

        TextInputEditText[] eocitfes = new TextInputEditText[3];

        for (int i = 0; i < eocitfes.length; i++) {
            eocitfes[i] = view.findViewById(eocitfeid[i]);
        }

        TextInputEditText[] itfes = new TextInputEditText[4];

        for (int i = 0; i < itfes.length; i++) {
            itfes[i] = view.findViewById(itfeid[i]);
        }

        TextInputEditText[] cotces = new TextInputEditText[3];

        for (int i = 0; i < cotces.length; i++) {
            cotces[i] = view.findViewById(cotceid[i]);
        }

        for (int i = 0; i < texts.length; i++) {
            final int ii = i;
            texts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    TextInputLayout tils = view.findViewById(tilid[ii]);

                    if (!s.toString().isEmpty()) {

                        if (Integer.parseInt(s.toString()) > limitvals[ii] || Integer.parseInt(s.toString()) < limitmins[ii]) {
                            if (tils.isHelperTextEnabled()) {
                                tils.setHelperTextEnabled(false);
                                tils.setErrorEnabled(true);
                                tils.setError(Objects.requireNonNull(getContext()).getText(R.string.treasure_invalid));
                            }
                        } else {
                            if (tils.isErrorEnabled()) {
                                tils.setError(null);
                                tils.setErrorEnabled(false);
                                tils.setHelperTextEnabled(true);
                                tils.setHelperTextColor(new ColorStateList(states, color));
                                tils.setHelperText(helpers[ii]);
                            }
                        }
                    } else {
                        if (tils.isErrorEnabled()) {
                            tils.setError(null);
                            tils.setErrorEnabled(false);
                            tils.setHelperTextEnabled(true);
                            tils.setHelperTextColor(new ColorStateList(states, color));
                            tils.setHelperText(helpers[ii]);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!Initialized)
                        return;

                    if (!s.toString().isEmpty()) {
                        Treasure t = BasisSet.current.t();

                        CanbeEdited = false;

                        if (Integer.parseInt(s.toString()) <= limitvals[ii] && Integer.parseInt(s.toString()) >= limitmins[ii]) {
                            int val = Integer.parseInt(s.toString());

                            switch (ii) {
                                case 0:
                                    for (int j = 0; j < 6; j++) {
                                        t.tech[j] = val;
                                        teches[j].setText(String.valueOf(val));
                                    }

                                    break;
                                case 1:
                                    t.tech[6] = val;

                                    break;
                                case 2:
                                    t.tech[7] = val;

                                    break;
                                case 3:
                                    for (int j = 0; j < 6; j++) {
                                        t.trea[j] = val;
                                        eoces[j].setText(String.valueOf(val));
                                    }

                                    break;
                                case 4:
                                    for (int j = 6; j < 9; j++) {
                                        t.trea[j] = val;
                                        eocitfes[j - 6].setText(String.valueOf(val));
                                    }

                                    break;
                                case 5:
                                    for (int j = 0; j < 4; j++) {
                                        t.fruit[j] = val;
                                        itfes[j].setText(String.valueOf(val));
                                    }

                                    break;
                                case 6:
                                    for (int j = 4; j < t.fruit.length; j++) {
                                        t.fruit[j] = val;
                                        cotces[j - 4].setText(String.valueOf(val));
                                    }

                                    break;
                                case 7:
                                    t.alien = val;
                                    break;
                                case 8:
                                    t.star = val;
                                    break;
                            }
                        } else {
                            switch (ii) {
                                case 0:
                                    for (int j = 0; j < 6; j++) {
                                        t.tech[j] = 30;
                                        teches[j].setText(String.valueOf(30));
                                    }

                                    break;
                                case 1:
                                    t.tech[6] = 30;

                                    break;
                                case 2:
                                    t.tech[7] = 10;

                                    break;
                                case 3:
                                    for (int j = 0; j < 6; j++) {
                                        t.trea[j] = 300;
                                        eoces[j].setText(String.valueOf(300));
                                    }

                                    break;
                                case 4:
                                    for (int j = 6; j < 9; j++) {
                                        t.trea[j] = 600;
                                        eocitfes[j - 6].setText(String.valueOf(600));
                                    }

                                    break;
                                case 5:
                                    for (int j = 0; j < 4; j++) {
                                        t.fruit[j] = 300;
                                        itfes[j].setText(String.valueOf(300));
                                    }

                                    break;
                                case 6:
                                    for (int j = 4; j < t.fruit.length; j++) {
                                        t.fruit[j] = 300;
                                        cotces[j - 4].setText(String.valueOf(300));
                                    }

                                    break;
                                case 7:
                                    t.alien = 600;
                                    break;
                                case 8:
                                    t.star = 1500;
                                    break;
                            }
                        }

                        CanbeEdited = true;
                        save();
                    }
                }
            });
        }
    }

    private void SetListenerforTextInptEditTexts(View view, TextInputEditText[]... texts) {
        if (getContext() == null) return;

        TextInputLayout tech = view.findViewById(R.id.techlev);
        TextInputEditText teche = view.findViewById(R.id.techlevt);

        TextInputLayout eoc = view.findViewById(R.id.eoctrea);
        TextInputEditText eoce = view.findViewById(R.id.eoctreat);

        TextInputLayout eocitf = view.findViewById(R.id.eocitftrea);
        TextInputEditText eocitfe = view.findViewById(R.id.eocitftreat);

        TextInputLayout itf = view.findViewById(R.id.itffruittrea);
        TextInputEditText itfe = view.findViewById(R.id.itffruittreat);

        TextInputLayout cotc = view.findViewById(R.id.cotctrea);
        TextInputEditText cotce = view.findViewById(R.id.cotctreat);

        for (int i = 0; i < texts.length; i++) {
            for (int j = 0; j < texts[i].length; j++) {
                final int ii = i;
                final int jj = j;

                texts[i][j].addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!Initialized)
                            return;

                        TextInputLayout tilss = view.findViewById(tilsid[ii][jj]);

                        if (!s.toString().isEmpty()) {
                            if (Integer.parseInt(s.toString()) > limitvalss[ii] || Integer.parseInt(s.toString()) < limitminss[ii]) {
                                if (tilss.isHelperTextEnabled()) {
                                    tilss.setHelperTextEnabled(false);
                                    tilss.setErrorEnabled(true);
                                    tilss.setError(Objects.requireNonNull(getContext()).getText(R.string.treasure_invalid));
                                }
                            } else {
                                if (tilss.isErrorEnabled()) {
                                    tilss.setError(null);
                                    tilss.setErrorEnabled(false);
                                    tilss.setHelperTextEnabled(true);
                                    tilss.setHelperTextColor(new ColorStateList(states, color));
                                    tilss.setHelperText(helperss[ii]);
                                }
                            }
                        } else {
                            if (tilss.isErrorEnabled()) {
                                tilss.setError(null);
                                tilss.setErrorEnabled(false);
                                tilss.setHelperTextEnabled(true);
                                tilss.setHelperTextColor(new ColorStateList(states, color));
                                tilss.setHelperText(helperss[ii]);
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!s.toString().isEmpty()) {
                            Treasure t = BasisSet.current.t();

                            if (CanbeEdited && Integer.parseInt(s.toString()) <= limitvalss[ii] && Integer.parseInt(s.toString()) >= limitminss[ii]) {
                                int val = Integer.parseInt(s.toString());

                                switch (ii) {
                                    case 0:
                                        t.tech[jj] = val;

                                        teche.setText("");
                                        tech.setHelperTextEnabled(true);
                                        tech.setHelperText("1~30 Lv.");
                                        break;
                                    case 1:
                                        t.trea[jj] = val;

                                        eoce.setText(null);
                                        eoc.setHelperTextEnabled(true);
                                        eoc.setHelperText(helperss[ii]);

                                        break;
                                    case 2:
                                        t.trea[jj + 6] = val;

                                        eocitfe.setText(null);
                                        eocitf.setHelperTextEnabled(true);
                                        eocitf.setHelperText(helperss[ii]);

                                        break;
                                    case 3:
                                        t.fruit[jj] = val;

                                        itfe.setText(null);
                                        itf.setHelperTextEnabled(true);
                                        itf.setHelperText(helperss[ii]);

                                        break;
                                    case 4:
                                        t.fruit[jj + 4] = val;

                                        cotce.setText(null);
                                        cotc.setHelperTextEnabled(true);
                                        cotc.setHelperText(helperss[ii]);

                                        break;
                                    case 5:
                                        t.gods[jj] = val;

                                        break;
                                }
                            }

                            save();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyed = true;
    }
}
