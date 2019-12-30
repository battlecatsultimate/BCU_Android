package com.mandarin.bcu.androidutil.lineup.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import common.battle.BasisSet;
import common.system.MultiLangCont;
import common.util.unit.Combo;

public class ComboListAdapter extends ArrayAdapter<String> {

    ComboListAdapter(Activity activity, String[] names) {
        super(activity, R.layout.combo_list_layout,names);
    }

    private static class ViewHolder {
        TextView comboname;
        TextView combodesc;
        TextView comboocc;
        LinearLayout comimglayout;
        List<ImageView> icons = new ArrayList<>();

        private ViewHolder(View view) {
            comboname = view.findViewById(R.id.comboname);
            combodesc = view.findViewById(R.id.combodesc);
            comboocc = view.findViewById(R.id.comboocc);
            comimglayout = view.findViewById(R.id.iconlayout);
        }
    }

    private int [] comnames = {R.string.combo_atk,R.string.combo_hp,R.string.combo_spd,R.string.combo_caninch,R.string.combo_work,R.string.combo_initmon,R.string.combo_canatk,R.string.combo_canchtime,0,R.string.combo_wal,R.string.combo_bsh,R.string.combo_cd,R.string.combo_ac,R.string.combo_xp,R.string.combo_strag,R.string.combo_md,R.string.combo_res,R.string.combo_kbdis,R.string.combo_sl,R.string.combo_st,R.string.combo_wea,R.string.combo_inc,R.string.combo_wit,R.string.combo_eva,R.string.combo_crit};

    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup group) {
        View row;
        ViewHolder holder;

        if(view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            row = inflater.inflate(R.layout.combo_list_layout,group,false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            row = view;
            holder = (ViewHolder) row.getTag();
        }

        holder.comboname.setText(MultiLangCont.COMNAME.getCont(StaticStore.combos.get(position).name));

        String occ = getContext().getString(R.string.combo_occu)+" : "+BasisSet.current.sele.lu.occupance(StaticStore.combos.get(position));

        holder.comboocc.setText(occ);

        holder.combodesc.setText(getDescription(StaticStore.combos.get(position)));

        holder.comimglayout.removeAllViews();
        holder.icons.clear();

        for(int i = 0; i < 5; i++) {
            if(StaticStore.combos.get(position).units.length <= i) {
                ImageView icon = new ImageView(getContext());
                icon.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1.0f));
                icon.setImageBitmap(StaticStore.empty(getContext(),24f,24f));
                icon.setBackground(getContext().getDrawable(R.drawable.cell_shape));

                holder.comimglayout.addView(icon);
                holder.icons.add(icon);
            } else {
                ImageView icon = new ImageView(getContext());
                icon.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1.0f));
                icon.setImageBitmap((Bitmap)StaticStore.units.get(StaticStore.combos.get(position).units[i][0]).forms[StaticStore.combos.get(position).units[i][1]].anim.uni.getImg().bimg());
                icon.setBackground(getContext().getDrawable(R.drawable.cell_shape));

                holder.comimglayout.addView(icon);
                holder.icons.add(icon);
            }
        }

        return row;
    }

    private String getDescription(Combo c) {
        int type = c.type;
        String multi = "";

        switch (type) {
            case 0:
                multi = " ( +"+(10 + 5*c.lv)+"% )";
                break;
            case 1:
                multi = " ( +"+(10 + 10*c.lv)+"% )";
                break;
            case 2:
                multi = " ( +"+(10 + 5*c.lv)+"% )";
                break;
            case 3:
                multi = " ( +"+(20 + 20*c.lv)+"% )";
                break;
            case 4:
                multi = " ( + Lv. "+(1+c.lv)+" )";
                break;
            case 5:
                if(c.lv == 0)
                    multi = " ( +"+300+" )";
                else if(c.lv == 1)
                    multi = " ( +"+500+" )";
                else
                    multi = " ( +"+1000+" )";

                break;
            case 6:
                multi = " ( +"+(20 + 30*c.lv)+"% )";
                break;
            case 7:
                multi = " ( -"+(150+150*c.lv)+"f / -"+(5+5*c.lv)+"s )";
                break;
            case 9:
                multi = " ( +"+(10 + 10*c.lv)+"% )";
                break;
            case 10:
                multi = " ( +"+(20 + 30*c.lv)+"% )";
                break;
            case 11:
                multi = " ( -"+(26+26*c.lv)+"f / -"+new DecimalFormat("#.##").format((double)(26+26*c.lv)/30)+"s )";
                break;
            case 12:
                multi = " ( +"+(10 + 10*c.lv)+"% )";
                break;
            case 13:
                multi = " ( +"+(10 + 10*c.lv)+"% )";
                break;
            case 14:
                multi = " ( +"+(10 + 10*c.lv)+"% )";
                break;
            case 15:
                multi = " ( +"+(10 + 10*c.lv)+"% )";
                break;
            case 16:
                multi = " ( +"+(10 + 10*c.lv)+"% )";
                break;
            case 17:
                multi = " ( +"+(10 + 10*c.lv)+"% )";
                break;
            case 18:
                multi = " ( +"+(10 + 10*c.lv)+"% )";
                break;
            case 19:
                multi = " ( +"+(10 + 10*c.lv)+"% )";
                break;
            case 20:
                multi = " ( +"+(10 + 10*c.lv)+"% )";
                break;
            case 21:
                multi = " ( +"+(10 + 10*c.lv)+"% )";
                break;
            case 22:
                multi = " ( +"+(20 + 10*c.lv)+"% )";
                break;
            case 23:
                multi = " ( +"+(100 + 100*c.lv)+"% )";
                break;
            case 24:
                multi = " ( +"+(100 + 100*c.lv)+"% )";
                break;
            case 25:
                multi = " ( +"+(2 + 2*c.lv)+"% )";
                break;
        }

        return getContext().getString(comnames[c.type]) + " Lv. " + c.lv + multi;
    }
}
