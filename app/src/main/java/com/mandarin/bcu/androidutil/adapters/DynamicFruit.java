package com.mandarin.bcu.androidutil.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import java.util.Arrays;
import java.util.List;

public class DynamicFruit extends PagerAdapter {
    private  Activity activity;
    private int id;
    private ImageView[] fruits = new ImageView[6];
    private TextView[] fruittext = new TextView[6];
    private TextView[] cfdesc = new TextView[3];
    private List<Integer> ids = Arrays.asList(30,31,32,33,34,35,36,37,38,39,40,41,42);

    private int[] imgid = {R.id.fruit1,R.id.fruit2,R.id.fruit3,R.id.fruit4,R.id.fruit5,R.id.xp};
    private int[] txid = {R.id.fruittext1,R.id.fruittext2,R.id.fruittext3,R.id.fruittext4,R.id.fruittext5,R.id.xptext};
    int[] cfdeid = {R.id.cfinf1,R.id.cfinf2,R.id.cfinf3};
    int[] cftooltip = {R.string.fruit1,R.string.fruit2,R.string.fruit3,R.string.fruit4,R.string.fruit5,R.string.fruit6,R.string.fruit7,R.string.fruit8,R.string.fruit9,R.string.fruit10,R.string.fruit11,R.string.fruit12,R.string.fruit13};

    public DynamicFruit(Activity activity, int id) {
        this.activity = activity;
        this.id = id;
    }

    @Override
    public Object instantiateItem(@NonNull ViewGroup group, int position) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        ViewGroup layout = (ViewGroup)inflater.inflate(R.layout.fruit_table,group,false);

        for(int i = 0; i < fruits.length;i++) {
            fruits[i] = layout.findViewById(imgid[i]);
            fruittext[i] = layout.findViewById(txid[i]);
        }

        for(int i = 0; i < cfdesc.length;i++) {
            cfdesc[i] = layout.findViewById(cfdeid[i]);
        }

        int[][] evo = StaticStore.units.get(id).info.evo;

        fruits[5].setImageBitmap(StaticStore.getResizeb(StaticStore.fruit[13],activity,48f));
        fruittext[5].setText(String.valueOf(evo[0][0]));

        for(int i =0;i<fruits.length-1;i++) {
            final int finall = i;
            fruits[i].setImageBitmap(StaticStore.getResizeb(StaticStore.fruit[ids.indexOf(evo[i+1][0])],activity,48f));
            fruits[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(activity,activity.getString(cftooltip[ids.indexOf(evo[finall+1][0])]),Toast.LENGTH_SHORT).show();

                    return true;
                }
            });

            fruittext[i].setText(String.valueOf(evo[i+1][1]));
        }

        String [] lines = StaticStore.units.get(id).info.getExplanation();

        for(int i = 0;i<cfdesc.length;i++) {
            if(i >= lines.length) {
                cfdesc[i].setVisibility(View.GONE);
                cfdesc[i].setPadding(0,0,0,0);
            } else {
                if(i == lines.length-1 && i != cfdesc.length-1)
                    cfdesc[i].setPadding(0,0,0,StaticStore.dptopx(24f,activity));

                cfdesc[i].setText(lines[i]);
            }
        }

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
