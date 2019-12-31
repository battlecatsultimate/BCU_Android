package com.mandarin.bcu.androidutil.lineup.adapters;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.FilterEntity;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.lineup.LineUpView;
import com.mandarin.bcu.androidutil.unit.adapters.UnitListAdapter;

import java.util.ArrayList;
import java.util.List;

import common.battle.BasisSet;
import common.util.unit.Form;
import common.util.unit.Unit;

public class LUUnitList extends Fragment {
    View view;
    LineUpView line;

    public static LUUnitList newInstance(String [] names, LineUpView line) {
        LUUnitList ulist = new LUUnitList();
        Bundle bundle = new Bundle();
        bundle.putStringArray("Names",names);
        ulist.setArguments(bundle);
        ulist.setLineUp(line);

        return ulist;
    }
    private boolean destroyed = false;
    private ArrayList<Integer> numbers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup group, @Nullable Bundle bundle) {
        view = inflater.inflate(R.layout.lineup_unit_list,group,false);

        if(getArguments() == null) return view;

        FilterEntity entity = new FilterEntity(StaticStore.unitnumber);
        numbers = entity.setFilter();
        ArrayList<String> names = new ArrayList<>();

        for(int i : numbers) {
            names.add(StaticStore.LUnames[i]);
        }

        UnitListAdapter adapter = new UnitListAdapter(getActivity(),names.toArray(new String[0]),StaticStore.bitmaps,numbers);

        ListView ulist = view.findViewById(R.id.lineupunitlist);

        ulist.setAdapter(adapter);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(StaticStore.updateList) {
                    FilterEntity entity = new FilterEntity(StaticStore.unitnumber);
                    numbers.clear();
                    numbers = entity.setFilter();
                    ArrayList<String> names = new ArrayList<>();

                    for(int i : numbers) {
                        names.add(StaticStore.LUnames[i]);
                    }

                    UnitListAdapter adapter = new UnitListAdapter(getActivity(),names.toArray(new String[0]),StaticStore.bitmaps,numbers);

                    ulist.setAdapter(adapter);

                    StaticStore.updateList = false;
                }

                if(!destroyed)
                    handler.postDelayed(this,50);
            }
        };

        handler.postDelayed(runnable,50);

        ulist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Form f = StaticStore.units.get(numbers.get(position)).forms[StaticStore.units.get(numbers.get(position)).forms.length-1];

                if(alreadyExist(f)) return;

                int [] posit = StaticStore.getPossiblePosition(BasisSet.current.sele.lu.fs);

                if(posit[0] != 100)
                    BasisSet.current.sele.lu.fs[posit[0]][posit[1]] = f;
                else
                    line.repform = f;

                line.UpdateLineUp();
                line.toFormArray();
            }
        });

        return view;
    }

    private boolean alreadyExist(Form form) {
        Unit u = form.unit;

        for(int i = 0; i < BasisSet.current.sele.lu.fs.length; i++) {
            for(int j = 0; j < BasisSet.current.sele.lu.fs[i].length; j++) {
                if(BasisSet.current.sele.lu.fs[i][j] == null) {
                    if(line.repform == null) return false;

                    return u.equals(line.repform.unit);
                }

                Unit u2 = BasisSet.current.sele.lu.fs[i][j].unit;

                if(u.equals(u2))
                    return true;
            }
        }

        return false;
    }

    @Override
    public void onDestroy() {
        destroyed = !destroyed;
        super.onDestroy();
    }

    public void setLineUp(LineUpView line) {
        this.line = line;
    }
}
