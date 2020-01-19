package com.mandarin.bcu.androidutil.lineup.adapters;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.lineup.LineUpView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import common.battle.BasisSet;
import common.system.MultiLangCont;
import common.util.unit.Combo;

public class LUCatCombo extends Fragment {
    private View view;

    public static LUCatCombo newInstance(LineUpView line) {
        LUCatCombo combo = new LUCatCombo();
        combo.SetVariables(line);

        return combo;
    }

    private int posit = -1;

    private LineUpView line;

    private int[] schid = {R.string.combo_str, R.string.combo_abil, R.string.combo_bscan, R.string.combo_mon, R.string.combo_env};
    private int[][] locateid = {{R.string.combo_atk, R.string.combo_hp, R.string.combo_spd}, {R.string.combo_strag, R.string.combo_md, R.string.combo_res, R.string.combo_kbdis, R.string.combo_sl, R.string.combo_st, R.string.combo_wea, R.string.combo_inc, R.string.combo_wit, R.string.combo_eva, R.string.combo_crit}, {R.string.combo_caninch, R.string.combo_canatk, R.string.combo_canchtime, R.string.combo_bsh}, {R.string.combo_initmon, R.string.combo_work, R.string.combo_wal}, {R.string.combo_cd, R.string.combo_ac, R.string.combo_xp}};
    private int[][] locater = {{0, 1, 2}, {14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24}, {3, 6, 7, 10}, {5, 4, 9}, {11, 12, 13}};
    private String[] sch = new String[schid.length];

    private ComboListAdapter comboListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup group, @Nullable Bundle bundle) {
        view = inflater.inflate(R.layout.lineup_cat_combo, group, false);

        if (getContext() == null) return view;

        if (StaticStore.combos != null)
            StaticStore.combos.clear();
        else
            StaticStore.combos = new ArrayList<>();

        for (int i = 0; i < Combo.combos.length; i++) {
            StaticStore.combos.addAll(Arrays.asList(Combo.combos[i]));
        }

        String[] names = new String[StaticStore.combos.size()];

        for (int i = 0; i < StaticStore.combos.size(); i++) {
            names[i] = MultiLangCont.COMNAME.getCont(StaticStore.combos.get(i).name);
        }

        ListView combolist = view.findViewById(R.id.combolist);
        ListView schlist = view.findViewById(R.id.comschlist1);
        ListView schlist1 = view.findViewById(R.id.comschlist2);
        Button use = view.findViewById(R.id.combouse);

        for (int i = 0; i < schid.length; i++) {
            sch[i] = getContext().getString(schid[i]);
        }

        List<String> subsch = new ArrayList<>();
        List<Integer> locates = new ArrayList<>();

        for (int[] i : locater) {
            for (int j : i) {
                locates.add(j);
            }
        }

        for (int i = 0; i < locater.length; i++) {
            for (int j = 0; j < locater[i].length; j++) {
                subsch.add(getContext().getString(locateid[i][j]));
            }
        }

        ComboSchListAdapter adapter = new ComboSchListAdapter(getActivity(), sch, schlist1, combolist, comboListAdapter);
        ComboSubSchListAdapter adapter1 = new ComboSubSchListAdapter(getActivity(), subsch, combolist, locates, comboListAdapter);

        schlist.setAdapter(adapter);
        schlist1.setAdapter(adapter1);

        comboListAdapter = new ComboListAdapter(getActivity(), names);

        combolist.setAdapter(comboListAdapter);

        combolist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getContext() == null) return;

                Combo c = StaticStore.combos.get(position);

                posit = position;

                if (!BasisSet.current.sele.lu.contains(c)) {
                    use.setClickable(true);

                    if (BasisSet.current.sele.lu.willRem(c)) {
                        use.setTextColor(Color.rgb(229, 57, 53));
                        use.setText(R.string.combo_rep);
                    } else {
                        use.setTextColor(StaticStore.getAttributeColor(getContext(), R.attr.TextPrimary));
                        use.setText(R.string.combo_use);
                    }
                } else {
                    use.setTextColor(StaticStore.getAttributeColor(getContext(), R.attr.TextPrimary));
                    use.setText(R.string.combo_using);
                    use.setClickable(false);
                }
            }
        });

        use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() == null) return;

                if (posit < 0) return;

                Combo c = StaticStore.combos.get(posit);

                BasisSet.current.sele.lu.set(c.units);

                line.UpdateLineUp();

                use.setText(R.string.combo_using);
                use.setTextColor(StaticStore.getAttributeColor(getContext(), R.attr.TextPrimary));
                use.setClickable(false);

                comboListAdapter.notifyDataSetChanged();

                line.invalidate();
            }
        });

        return view;
    }

    private void SetVariables(LineUpView line) {
        this.line = line;
    }
}
