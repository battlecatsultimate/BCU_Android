package com.mandarin.bcu.androidutil.enemy.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import common.system.MultiLangCont;

public class DynamicEmExplanation extends PagerAdapter {
    private Activity activity;
    private int id;
    private int[] txid ={R.id.enemyex0,R.id.enemyex1,R.id.enemyex2,R.id.enemyex3};

    public DynamicEmExplanation(Activity activity, int id) {
        this.activity = activity;
        this.id = id;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup group, int position) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        ViewGroup layout = (ViewGroup)inflater.inflate(R.layout.enemy_explanation,group,false);

        TextView title = layout.findViewById(R.id.enemyexname);
        String name = MultiLangCont.ENAME.getCont(StaticStore.enemies.get(id));

        if(name == null)
            name = "";

        title.setText(name);
        TextView [] exps = new TextView[4];
        for(int i = 0; i < txid.length;i++)
            exps[i] = layout.findViewById(txid[i]);

        String[] explanation = MultiLangCont.EEXP.getCont(StaticStore.enemies.get(id));
        if(explanation == null)
            explanation = new String[]{"","","",""};

        for(int i = 0;i<exps.length;i++) {
            if(i >= explanation.length)
                exps[i].setText("");
            else
                exps[i].setText(explanation[i]);
        }

        exps[3].setPadding(0,0,0,StaticStore.dptopx(24f, activity));

        group.addView(layout);
        return layout;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }
}
