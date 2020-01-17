package com.mandarin.bcu.androidutil.lineup.adapters;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import common.battle.BasisSet;
import common.battle.Treasure;
import common.io.OutStream;

public class LUTreasureSetting extends Fragment {
    View view;

    public static LUTreasureSetting newInstance() {
        return new LUTreasureSetting();
    }

    boolean CanbeEdited = true;
    boolean Initialized = false;
    private boolean destroyed = false;

    TextInputLayout tech;
    TextInputLayout [] techs = new TextInputLayout[6];
    int [] techid = {R.id.cdlev,R.id.aclev,R.id.basehlev,R.id.worklev,R.id.walletlev,R.id.rechargelev};
    TextInputLayout canatk;
    TextInputLayout canrange;
    TextInputLayout eoc;
    TextInputLayout [] eocs = new TextInputLayout[6];
    int [] eocid = {R.id.atktrea,R.id.healtrea,R.id.cdtrea,R.id.actrea,R.id.worktrea,R.id.wallettrea};
    TextInputLayout eocitf;
    TextInputLayout [] eocitfs = new TextInputLayout[3];
    int [] eocitfid = {R.id.rechargetrea,R.id.canatktrea,R.id.basehtrea};
    TextInputLayout itf;
    TextInputLayout [] itfs = new TextInputLayout[4];
    int [] itfid = {R.id.redfrtrea,R.id.floatfrtrea,R.id.blacktrea,R.id.angelfrtrea};
    TextInputLayout cotc;
    TextInputLayout [] cotcs = new TextInputLayout[3];
    int [] cotcid = {R.id.metalfrtrea,R.id.zombiefrtrea,R.id.alienfrtrea};
    TextInputLayout itfcry;
    TextInputLayout cotccry;
    TextInputLayout [] masks = new TextInputLayout[3];
    int [] maskid = {R.id.masktrea,R.id.mask2trea,R.id.mask3trea};

    TextInputLayout [] tils = {tech,canatk,canrange,eoc,eocitf,itf,cotc,itfcry,cotccry};
    TextInputLayout [][] tilss = {techs,eocs,eocitfs,itfs,cotcs,masks};
    int [] limitvals = {30,30,10,300,600,300,300,600,1500};
    int [] limitmins = {1,1,1,0,0,0,0,0,0};
    int [] limitvalss = {30,300,600,300,300,100};
    int [] limitminss = {1,0,0,0,0,0};
    String [] helpers = {"1~30 lv","1~30 lv","1~10 lv","0~300 %","0~600 %","0~300 %","0~300 %","0~600 %","0~1500 %"};
    String [] helperss = {"1~30 lv", "0~300 %","0~600 %","0~300 %", "0~300 %", "0~100 %"};

    TextInputEditText teche;
    TextInputEditText [] teches = new TextInputEditText[6];
    int [] techeid = {R.id.cdlevt,R.id.aclevt,R.id.basehlevt,R.id.worklevt,R.id.walletlevt,R.id.rechargelevt};
    TextInputEditText canatke;
    TextInputEditText canrangee;
    TextInputEditText eoce;
    TextInputEditText [] eoces = new TextInputEditText[6];
    int [] eoceid = {R.id.atktreat,R.id.healtreat,R.id.cdtreat,R.id.actreat,R.id.worktreat,R.id.wallettreat};
    TextInputEditText eocitfe;
    TextInputEditText [] eocitfes = new TextInputEditText[3];
    int [] eocitfeid = {R.id.rechargetreat,R.id.canatktreat,R.id.basehtreat};
    TextInputEditText itfe;
    TextInputEditText [] itfes = new TextInputEditText[4];
    int [] itfeid = {R.id.redfrtreat,R.id.floatfrtreat,R.id.blacktreat,R.id.angelfrtreat};
    TextInputEditText cotce;
    TextInputEditText [] cotces = new TextInputEditText[3];
    int [] cotceid = {R.id.metalfrtreat,R.id.zombiefrtreat,R.id.alienfrtreat};
    TextInputEditText itfcrye;
    TextInputEditText cotccrye;
    TextInputEditText [] maskes = new TextInputEditText[3];
    int [] maskeid = {R.id.masktreat,R.id.mask2treat,R.id.mask3treat};

    FloatingActionButton[] expands = new FloatingActionButton[5];
    int [] expandid = {R.id.techexpand,R.id.eocexpand,R.id.eocitfexpand,R.id.itfexpand,R.id.cotcexpand};

    LinearLayout[] layouts = new LinearLayout[5];
    int [] layoutid = {R.id.techlayout,R.id.eoclayout,R.id.eocitflayout,R.id.itffruitlayout,R.id.cotclayout};

    private int[][] states = new int[][] {
            new int[] {android.R.attr.state_enabled}
    };

    private int [] color;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup group, @Nullable Bundle bundle) {
        view = inflater.inflate(R.layout.lineup_treasure_set,group,false);

        color = new int [] {
                getAttributeColor(getContext(),R.attr.TextPrimary)
        };

        Initialize(view);

        Listeners();

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(StaticStore.updateTreasure) {
                    Treasure t = BasisSet.current.t();

                    Initialized = false;

                    itfcrye.setText(String.valueOf(t.alien));
                    cotccrye.setText(String.valueOf(t.star));

                    for(int i = 0; i < 6; i++) {
                        teches[i].setText(String.valueOf(t.tech[i]));
                    }

                    canatke.setText(String.valueOf(t.tech[6]));
                    canrangee.setText(String.valueOf(t.tech[7]));

                    for(int i = 0; i < 6; i++) {
                        eoces[i].setText(String.valueOf(t.trea[i]));
                    }

                    for(int i = 0; i < eocitfes.length; i++) {
                        eocitfes[i].setText(String.valueOf(t.trea[i+6]));
                    }

                    for(int i = 0; i < 4; i++) {
                        itfes[i].setText(String.valueOf(t.fruit[i]));
                    }

                    for(int i = 4; i < t.fruit.length; i++) {
                        cotces[i-4].setText(String.valueOf(t.fruit[i]));
                    }

                    for(int i = 0; i < t.gods.length; i++) {
                        maskes[i].setText(String.valueOf(t.gods[i]));
                    }


                    if(ValuesAllSame(0))
                        teche.setText(String.valueOf(t.tech[0]));

                    if(ValuesAllSame(1))
                        eoce.setText(String.valueOf(t.trea[0]));

                    if(ValuesAllSame(2))
                        eocitfe.setText(String.valueOf(t.trea[6]));

                    if(ValuesAllSame(3))
                        itfe.setText(String.valueOf(t.fruit[0]));

                    if(ValuesAllSame(4))
                        cotce.setText(String.valueOf(t.fruit[4]));

                    Initialized = true;

                    StaticStore.updateTreasure = false;
                }

                if(!destroyed)
                    handler.postDelayed(this,50);
            }
        };

        handler.postDelayed(runnable,50);

        return view;
    }

    /** Initialize view components **/
    private void Initialize(View view) {
        tech = view.findViewById(R.id.techlev);
        canatk = view.findViewById(R.id.canatklev);
        canrange = view.findViewById(R.id.canrangelev);
        eoc = view.findViewById(R.id.eoctrea);
        eocitf = view.findViewById(R.id.eocitftrea);
        itf = view.findViewById(R.id.itffruittrea);
        cotc = view.findViewById(R.id.cotctrea);
        itfcry = view.findViewById(R.id.itfcrytrea);
        cotccry = view.findViewById(R.id.cotccrytrea);

        teche = view.findViewById(R.id.techlevt);
        canatke = view.findViewById(R.id.canatklevt);
        canrangee = view.findViewById(R.id.canrangelevt);
        eoce = view.findViewById(R.id.eoctreat);
        eocitfe = view.findViewById(R.id.eocitftreat);
        itfe = view.findViewById(R.id.itffruittreat);
        cotce = view.findViewById(R.id.cotctreat);
        itfcrye = view.findViewById(R.id.itfcrytreat);
        cotccrye = view.findViewById(R.id.cotccrytreat);

        for(int i = 0; i < expands.length; i++) {
            expands[i] = view.findViewById(expandid[i]);
            layouts[i] = view.findViewById(layoutid[i]);
        }

        for(int i = 0; i < techs.length; i++) {
            techs[i] = view.findViewById(techid[i]);
            teches[i] = view.findViewById(techeid[i]);
        }

        for(int i = 0; i < eocs.length; i++) {
            eocs[i] = view.findViewById(eocid[i]);
            eoces[i] = view.findViewById(eoceid[i]);
        }

        for(int i = 0; i < eocitfs.length; i++) {
            eocitfs[i] = view.findViewById(eocitfid[i]);
            eocitfes[i] = view.findViewById(eocitfeid[i]);
        }

        for(int i = 0; i < itfs.length; i++) {
            itfs[i] = view.findViewById(itfid[i]);
            itfes[i] = view.findViewById(itfeid[i]);
        }

        for(int i = 0; i < cotcs.length; i++) {
            cotcs[i] = view.findViewById(cotcid[i]);
            cotces[i] = view.findViewById(cotceid[i]);
        }

        for(int i = 0; i < masks.length; i++) {
            masks[i] = view.findViewById(maskid[i]);
            maskes[i] = view.findViewById(maskeid[i]);
        }
    }

    private void Listeners() {
        Treasure t = BasisSet.current.t();

        //Listeners for expand image buttons
        for(int i = 0; i < expands.length; i++) {
            final int ii = i;

            expands[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(SystemClock.elapsedRealtime() - StaticStore.infoClick < StaticStore.INFO_INTERVAL)
                        return;

                    StaticStore.infoClick = SystemClock.elapsedRealtime();

                    if(layouts[ii].getHeight() == 0) {
                        layouts[ii].measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

                        int height = layouts[ii].getMeasuredHeight();

                        ValueAnimator anim = ValueAnimator.ofInt(0,height);
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

                        expands[ii].setImageDrawable(getContext().getDrawable(R.drawable.ic_expand_more_black_24dp));
                    } else {
                        layouts[ii].measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

                        int height = layouts[ii].getMeasuredHeight();

                        ValueAnimator anim = ValueAnimator.ofInt(height,0);
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

                        expands[ii].setImageDrawable(getContext().getDrawable(R.drawable.ic_expand_less_black_24dp));
                    }
                }
            });
        }

        //Listeners for TextInputLayout

        itfcrye.setText(String.valueOf(t.alien));
        cotccrye.setText(String.valueOf(t.star));

        SetListenerforTextInputLayout(tech,canatk,canrange,eoc,eocitf,itf,cotc,itfcry,cotccry);
        SetListenerforTextInputLayouts(techs,eocs,eocitfs,itfs,cotcs,masks);

        tils = new TextInputLayout[]{tech,canatk,canrange,eoc,eocitf,itf,cotc,itfcry,cotccry};
        tilss = new TextInputLayout[][]{techs,eocs,eocitfs,itfs,cotcs,masks};

        //Listeners for TextInputLayouts

        for(int i = 0; i < 6; i++) {
            teches[i].setText(String.valueOf(t.tech[i]));
        }

        canatke.setText(String.valueOf(t.tech[6]));
        canrangee.setText(String.valueOf(t.tech[7]));

        for(int i = 0; i < 6; i++) {
            eoces[i].setText(String.valueOf(t.trea[i]));
        }

        for(int i = 0; i < eocitfes.length; i++) {
            eocitfes[i].setText(String.valueOf(t.trea[i+6]));
        }

        for(int i = 0; i < 4; i++) {
            itfes[i].setText(String.valueOf(t.fruit[i]));
        }

        for(int i = 4; i < t.fruit.length; i++) {
            cotces[i-4].setText(String.valueOf(t.fruit[i]));
        }

        for(int i = 0; i < t.gods.length; i++) {
            maskes[i].setText(String.valueOf(t.gods[i]));
        }


        if(ValuesAllSame(0))
            teche.post(new Runnable() {
                @Override
                public void run() {
                    teche.setText(String.valueOf(t.tech[0]));
                }
            });

        if(ValuesAllSame(1))
            eoce.post(new Runnable() {
                @Override
                public void run() {
                    eoce.setText(String.valueOf(t.trea[0]));
                }
            });

        if(ValuesAllSame(2))
            eocitfe.post(new Runnable() {
                @Override
                public void run() {
                    eocitfe.setText(String.valueOf(t.trea[6]));
                }
            });

        if(ValuesAllSame(3))
            itfe.post(new Runnable() {
                @Override
                public void run() {
                    itfe.setText(String.valueOf(t.fruit[0]));
                }
            });

        if(ValuesAllSame(4))
            cotce.post(new Runnable() {
                @Override
                public void run() {
                    cotce.setText(String.valueOf(t.fruit[4]));
                }
            });

        SetListenerforTextInputEditText(teche,canatke,canrangee,eoce,eocitfe,itfe,cotce,itfcrye,cotccrye);
        SetListenerforTextInptEditTexts(teches,eoces,eocitfes,itfes,cotces,maskes);

        Initialized = true;
    }

    private void save() {
        String Path = Environment.getExternalStorageDirectory().getPath()+"/BCU/user/basis.v";
        String Direct = Environment.getExternalStorageDirectory().getPath()+"/BCU/user/";

        File g = new File(Direct);

        if(!g.exists())
            g.mkdirs();

        File f = new File(Path);

        try {
            if(!f.exists())
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

                for(int i = 1; i < 6; i++) {
                    if (val != BasisSet.current.t().tech[i])
                        return false;
                }

                return true;
            case 1:
                val = BasisSet.current.t().trea[0];

                for(int i = 1; i < 6; i++) {
                    if (val != BasisSet.current.t().trea[i])
                        return false;
                }

                return true;
            case 2:
                val = BasisSet.current.t().trea[6];

                for(int i = 7; i < 9; i++)
                    if(val != BasisSet.current.t().trea[i])
                        return false;

                return true;
            case 3:
                val = BasisSet.current.t().fruit[0];

                for(int i = 1; i < 4; i++)
                    if(val != BasisSet.current.t().fruit[i])
                        return false;

                return true;
            case 4:
                val = BasisSet.current.t().fruit[4];

                for(int i = 5; i < BasisSet.current.t().fruit.length; i++)
                    if(val != BasisSet.current.t().fruit[i])
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
            color = ContextCompat.getColor(context,colorRes);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return color;
    }

    private void SetListenerforTextInputLayout(TextInputLayout... texts) {
        for(TextInputLayout t : texts) {
            t.setHelperTextColor(new ColorStateList(states,color));
        }
    }

    private void SetListenerforTextInputLayouts(TextInputLayout[]... texts) {
        for(TextInputLayout[] ts : texts) {
            for(TextInputLayout t : ts) {
                t.setHelperTextColor(new ColorStateList(states,color));
            }
        }
    }

    private void SetListenerforTextInputEditText(TextInputEditText... texts) {
        if(getContext() == null) return;

        for(int i = 0; i < texts.length; i++) {
            final int ii = i;
            texts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(!s.toString().isEmpty()) {
                        if(Integer.parseInt(s.toString()) > limitvals[ii] || Integer.parseInt(s.toString()) < limitmins[ii]) {
                            if(tils[ii].isHelperTextEnabled()) {
                                tils[ii].setHelperTextEnabled(false);
                                tils[ii].setErrorEnabled(true);
                                tils[ii].setError(getContext().getText(R.string.treasure_invalid));
                            }
                        } else {
                            if(tils[ii].isErrorEnabled()) {
                                tils[ii].setError(null);
                                tils[ii].setErrorEnabled(false);
                                tils[ii].setHelperTextEnabled(true);
                                tils[ii].setHelperTextColor(new ColorStateList(states,color));
                                tils[ii].setHelperText(helpers[ii]);
                            }
                        }
                    } else {
                        if(tils[ii].isErrorEnabled()) {
                            tils[ii].setError(null);
                            tils[ii].setErrorEnabled(false);
                            tils[ii].setHelperTextEnabled(true);
                            tils[ii].setHelperTextColor(new ColorStateList(states,color));
                            tils[ii].setHelperText(helpers[ii]);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(!Initialized)
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

    private void SetListenerforTextInptEditTexts(TextInputEditText []... texts) {
        if(getContext() == null) return;

        for(int i = 0; i < texts.length; i++) {
            for(int j = 0; j < texts[i].length; j++) {
                final int ii = i;
                final int jj = j;

                texts[i][j].addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(!Initialized)
                            return;

                        if(!s.toString().isEmpty()) {
                            if(Integer.parseInt(s.toString()) > limitvalss[ii] || Integer.parseInt(s.toString()) < limitminss[ii]) {
                                if (tilss[ii][jj].isHelperTextEnabled()) {
                                    tilss[ii][jj].setHelperTextEnabled(false);
                                    tilss[ii][jj].setErrorEnabled(true);
                                    tilss[ii][jj].setError(getContext().getText(R.string.treasure_invalid));
                                }
                            } else {
                                    if (tilss[ii][jj].isErrorEnabled()) {
                                        tilss[ii][jj].setError(null);
                                        tilss[ii][jj].setErrorEnabled(false);
                                        tilss[ii][jj].setHelperTextEnabled(true);
                                        tilss[ii][jj].setHelperTextColor(new ColorStateList(states, color));
                                        tilss[ii][jj].setHelperText(helperss[ii]);
                                    }
                            }
                        } else {
                            if(tilss[ii][jj].isErrorEnabled()) {
                                tilss[ii][jj].setError(null);
                                tilss[ii][jj].setErrorEnabled(false);
                                tilss[ii][jj].setHelperTextEnabled(true);
                                tilss[ii][jj].setHelperTextColor(new ColorStateList(states,color));
                                tilss[ii][jj].setHelperText(helperss[ii]);
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(!s.toString().isEmpty()) {
                            Treasure t = BasisSet.current.t();

                            if(CanbeEdited && Integer.parseInt(s.toString()) <= limitvalss[ii] && Integer.parseInt(s.toString()) >= limitminss[ii]) {
                                int val = Integer.parseInt(s.toString());

                                switch(ii) {
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
                                        t.trea[jj+6] = val;

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
                                        t.fruit[jj+4] = val;

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
