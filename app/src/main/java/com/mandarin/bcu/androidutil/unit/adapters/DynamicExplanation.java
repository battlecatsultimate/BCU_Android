package com.mandarin.bcu.androidutil.unit.adapters;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import java.util.Objects;

import common.system.MultiLangCont;

public class DynamicExplanation extends Fragment {
    View view;

    public static DynamicExplanation newInstance(int val, int id,String [] titles) {
        DynamicExplanation explanation = new DynamicExplanation();
        Bundle bundle = new Bundle();
        bundle.putInt("Number",val);
        bundle.putInt("ID",id);
        bundle.putStringArray("Title",titles);
        explanation.setArguments(bundle);
        return explanation;
    }

    int val;
    int id;
    String[] titles;
    TextView unitname;
    TextView[] explains = new TextView[3];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
        view = inflater.inflate(R.layout.unit_info_tab,container,false);
        val = getArguments().getInt("Number",0);
        id = getArguments().getInt("ID",0);
        titles = getArguments().getStringArray("Title");
        String[] explanation = MultiLangCont.FEXP.getCont(StaticStore.units.get(id).forms[val]);
        if(explanation == null) {
            explanation = new String[]{"","",""};
        }

        unitname = view.findViewById(R.id.unitexname);
        int[] lineid = {R.id.unitex0,R.id.unitex1,R.id.unitex2};
        for(int i=0;i<lineid.length;i++)
            explains[i] = view.findViewById(lineid[i]);

        explains[2].setPadding(0,0,0,StaticStore.dptopx(24f, Objects.requireNonNull(getActivity())));

        String name = MultiLangCont.FNAME.getCont(StaticStore.units.get(id).forms[val]);

        if(name == null)
            name = "";

        unitname.setText(name);

        for(int i=0;i < explains.length;i++) {
            if(i >= explanation.length) {
                explains[i].setText("");
            } else {
                if(explanation[i] != null)
                    explains[i].setText(explanation[i]);
            }
        }

        return view;
    }
}
