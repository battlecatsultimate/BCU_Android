package com.mandarin.bcu.androidutil;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.util.Interpret;
import com.mandarin.bcu.util.basis.Treasure;
import com.mandarin.bcu.util.entity.data.MaskAtk;
import com.mandarin.bcu.util.unit.EForm;
import com.mandarin.bcu.util.unit.Form;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class getStrings {
    private final Context c;
    
    public getStrings(Context context) {
        c = context;
    }
    
    public String getTitle(Form f) {
        StringBuilder result = new StringBuilder();

        String name = f.name;
        String rarity;

        switch (f.unit.rarity) {
            case 0:
                rarity = c.getString(R.string.sch_rare_ba);
                break;
            case 1:
                rarity = c.getString(R.string.sch_rare_ex);
                break;
            case 2:
                rarity = c.getString(R.string.sch_rare_ra);
                break;
            case 3:
                rarity = c.getString(R.string.sch_rare_sr);
                break;
            case 4:
                rarity = c.getString(R.string.sch_rare_ur);
                break;
            case 5:
                rarity = c.getString(R.string.sch_rare_lr);
                break;
            default:
                rarity = "Unknown";
                break;
        }

        return result.append(rarity).append(" - ").append(name).toString();

    }

    public String getAtkTime(Form f,int frse) {
        if(frse == 0)
            return f.du.getItv()+" f";
        else
            return new DecimalFormat("#.##").format((double)f.du.getItv()/30)+" s";
    }

    public String getAbilT(Form f) {
        int [][] atkdat = f.du.rawAtkData();

        if(atkdat.length > 1) {
            StringBuilder result = new StringBuilder();

            for(int i =0;i<atkdat.length;i++) {
                if(i != atkdat.length-1) {
                    if(atkdat[i][2] == 1)
                        result.append(c.getString(R.string.unit_info_true)).append(" / ");
                    else
                        result.append(c.getString(R.string.unit_info_false)).append(" / ");
                } else {
                    if(atkdat[i][2] == 1)
                        result.append(c.getString(R.string.unit_info_true));
                    else
                        result.append(c.getString(R.string.unit_info_false));
                }
            }

            return result.toString();
        } else {
            if(atkdat[0][2] == 1)
                return c.getString(R.string.unit_info_true);
            else
                return c.getString(R.string.unit_info_false);
        }
    }

    public String getPost(Form f, int frse) {
        if(frse == 0)
            return f.du.getPost()+" f";
        else
            return new DecimalFormat("#.##").format((double)f.du.getPost()/30)+" s";
    }

    public String getTBA(Form f,int frse) {
        if(frse == 0)
            return f.du.getTBA()+" f";
        else
            return new DecimalFormat("#.##").format((double)f.du.getTBA()/30)+" s";
    }

    public String getPre(Form f,int frse) {
        int[][] atkdat = f.du.rawAtkData();

        if (frse == 0) {
            if (atkdat.length > 1) {
                StringBuilder result = new StringBuilder();

                for (int i = 0; i < atkdat.length; i++) {
                    if (i != atkdat.length - 1)
                        result.append(atkdat[i][1]).append(" f / ");
                    else
                        result.append(atkdat[i][1]).append(" f");
                }

                return result.toString();
            } else
                return atkdat[0][1] + " f";
        } else {
            if (atkdat.length > 1) {
                StringBuilder result = new StringBuilder();

                for (int i = 0; i < atkdat.length; i++) {
                    if (i != atkdat.length - 1)
                        result.append(new DecimalFormat("#.##").format((double)atkdat[i][1]/30)).append(" s / ");
                    else
                        result.append(new DecimalFormat("#.##").format((double)atkdat[i][1]/30)).append(" s");
                }

                return result.toString();
            } else
                return new DecimalFormat("#.##").format((double)atkdat[0][1]/30) + " s";
        }
    }

    public String getID(Form f, RecyclerView.ViewHolder viewHolder,String id) {
        return id+"-"+viewHolder.getAdapterPosition();
    }

    public String getRange(Form f) {
        int tb = f.du.getRange();
        MaskAtk ma = f.du.getRepAtk();
        int lds = ma.getShortPoint();
        int ldr = ma.getLongPoint()-ma.getShortPoint();

        int start = Math.min(lds,lds+ldr);
        int end = Math.max(lds,lds+ldr);


        if(lds > 0)
            return tb+" / "+start+" ~ "+end;
        else
            return String.valueOf(tb);
    }

    public String getCD(Form f, Treasure t, int frse) {
        if(frse == 0)
            return t.getFinRes(f.du.getRespawn()) +" f";
        else
            return new DecimalFormat("#.##").format((double) t.getFinRes(f.du.getRespawn())/30) +" s";
    }

    public String getAtk(Form f,Treasure t,int lev) {
        if(f.du.rawAtkData().length > 1)
            return getTotAtk(f, t, lev) +" "+getAtks(f,t,lev);
        else
            return getTotAtk(f,t,lev);
    }

    public String getSpd(Form f) {
        return String.valueOf(f.du.getSpeed());
    }

    public String getHB(Form f) {
        return String.valueOf(f.du.getHb());
    }

    public String getHP(Form f, Treasure t, int lev) {
        return String.valueOf((int)(f.du.getHp()*t.getDefMulti()*f.unit.lv.getMult(lev)));
    }

    public String getTotAtk(Form f,Treasure t,int lev) {
        return String.valueOf((int)(f.du.allAtk()*t.getAtkMulti()*f.unit.lv.getMult(lev)));
    }

    public String getDPS(Form f,Treasure t,int lev) {
        return String.valueOf(new DecimalFormat("#.##").format(Double.parseDouble(getTotAtk(f,t,lev))/((double)f.du.getItv()/30)));
    }

    public String getTrait(EForm ef) {
        StringBuilder allcolor = new StringBuilder();
        StringBuilder alltrait = new StringBuilder();

        for(int i = 0; i< Interpret.TRAIT.length; i++) {
            if (i != 0)
                allcolor.append(Interpret.TRAIT[i]).append(", ");
            alltrait.append(Interpret.TRAIT[i]).append(", ");
        }

        String result;

        result = Interpret.getTrait(ef.du.getType(), 0);

        if(result.equals(""))
            result = c.getString(R.string.unit_info_t_none);

        if(result.equals(allcolor.toString()))
            result = c.getString(R.string.unit_info_t_allc);

        if(result.equals(alltrait.toString()))
            result = c.getString(R.string.unit_info_t_allt);

        if(result.endsWith(", "))
            result = result.substring(0,result.length()-2);

        return result;
    }

    public String getCost(Form f) {
        return String.valueOf((int)(f.du.getPrice()*1.5));
    }

    public String getAtks(Form f,Treasure t, int lev) {
        int[][] atks = f.du.rawAtkData();

        ArrayList<Integer> damges = new ArrayList<>();

        for (int[] atk : atks) {
            damges.add((int) (atk[0] * t.getAtkMulti() * f.unit.lv.getMult(lev)));
        }

        StringBuilder result = new StringBuilder("(");

        for(int i=0;i<damges.size();i++) {
            if(i < damges.size()-1)
                result.append("").append(damges.get(i)).append(", ");
            else
                result.append("").append(damges.get(i)).append(")");
        }

        return result.toString();
    }

    public String getSimu(Form f) {
        if(Interpret.isType(f.du,1))
            return c.getString(R.string.sch_atk_ra);
        else
            return c.getString(R.string.sch_atk_si);
    }
}
