package com.mandarin.bcu.androidutil;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.util.Interpret;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import common.battle.BasisSet;
import common.battle.Treasure;
import common.battle.data.MaskAtk;
import common.battle.data.MaskEnemy;
import common.battle.data.MaskUnit;
import common.system.MultiLangCont;
import common.system.P;
import common.util.stage.SCDef;
import common.util.unit.Enemy;
import common.util.unit.Form;

public class getStrings {
    private final Context c;
    private String[] abilID = {"1","2","3","8","10","11","13","14","15","16","17","18","19","20","21","22","25","26","27","29","30","31","32","37","38","39","40"};
    private int[] talID = {R.string.sch_abi_we,R.string.sch_abi_fr,R.string.sch_abi_sl,R.string.sch_abi_kb,R.string.sch_abi_str,R.string.sch_abi_su,R.string.sch_abi_cr,
            R.string.sch_abi_zk,R.string.sch_abi_bb,R.string.sch_abi_em,R.string.sch_abi_wv,R.string.talen_we,R.string.talen_fr,R.string.talen_sl,R.string.talen_kb
            ,R.string.talen_wv,R.string.unit_info_cost,R.string.unit_info_cd,R.string.unit_info_spd,R.string.sch_abi_ic,R.string.talen_cu,
            R.string.unit_info_atk,R.string.unit_info_hp,R.string.sch_an,R.string.sch_al,R.string.sch_zo,R.string.sch_re};
    private String [] talTool = new String[talID.length];
    private String [] mapcolcid = {"N","S","C","CH","E","T","V","R","M","A","B"};
    private List<String> mapcodes = Arrays.asList("0","1","2","3","4","6","7","11","12","13","14");
    private int [] diffid = {R.string.stg_info_easy,R.string.stg_info_norm,R.string.stg_info_hard,R.string.stg_info_vete,R.string.stg_info_expe,R.string.stg_info_insa,R.string.stg_info_dead,R.string.stg_info_merc};
    
    public getStrings(Context context) {
        c = context;
        getTalList();
    }

    public void getTalList() {
        for(int i = 0;i<talTool.length;i++)
            talTool[i] = c.getString(talID[i]);
    }
    
    public String getTitle(Form f) {
        StringBuilder result = new StringBuilder();

        String name = MultiLangCont.FNAME.getCont(f);

        if(name == null)
            name = "";

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

    public String getAtkTime(Enemy em, int frse) {
        if(frse == 0)
            return em.de.getItv()+" f";
        else
            return new DecimalFormat("#.##").format((double)em.de.getItv()/30)+" s";
    }

    public String getAbilT(Form f) {
        int [][] atkdat = f.du.rawAtkData();

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
    }

    public String getAbilT(Enemy em) {
        int [][] atks = em.de.rawAtkData();

        StringBuilder result = new StringBuilder();

        for(int i =0;i<atks.length;i++) {
            if(i < atks.length-1) {
                if(atks[i][2] == 1)
                    result.append(c.getString(R.string.unit_info_true)).append(" / ");
                else
                    result.append(c.getString(R.string.unit_info_false)).append(" / ");
            } else {
                if(atks[i][2] == 1)
                    result.append(c.getString(R.string.unit_info_true));
                else
                    result.append(c.getString(R.string.unit_info_false));
            }
        }

        return result.toString();
    }

    public String getPost(Form f, int frse) {
        if(frse == 0)
            return f.du.getPost()+" f";
        else
            return new DecimalFormat("#.##").format((double)f.du.getPost()/30)+" s";
    }

    public String getPost(Enemy em, int frse) {
        if(frse == 0)
            return em.de.getPost()+" f";
        else
            return new DecimalFormat("#.##").format((double)em.de.getPost()/30)+" s";
    }

    public String getTBA(Form f,int frse) {
        if(frse == 0)
            return f.du.getTBA()+" f";
        else
            return new DecimalFormat("#.##").format((double)f.du.getTBA()/30)+" s";
    }

    public String getTBA(Enemy em,int frse) {
        if(frse == 0)
            return em.de.getTBA()+" f";
        else
            return new DecimalFormat("#.##").format((double)em.de.getTBA()/30)+" s";
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

    public String getPre(Enemy em,int frse) {
        int[][] atkdat = em.de.rawAtkData();

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

    public String getID(RecyclerView.ViewHolder viewHolder, String id) {
        return id+"-"+viewHolder.getAdapterPosition();
    }

    public String getID(int form, String id) {
        return id+"-"+ form;
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

    public String getRange(Enemy em) {
        int tb = em.de.getRange();
        MaskAtk ma = em.de.getRepAtk();
        int lds = ma.getShortPoint();
        int ldr = ma.getLongPoint()-ma.getShortPoint();

        int start = Math.min(lds,lds+ldr);
        int end = Math.max(lds,lds+ldr);


        if(lds > 0)
            return tb+" / "+start+" ~ "+end;
        else
            return String.valueOf(tb);
    }

    public String getCD(Form f, Treasure t, int frse,boolean talent, int[] lvs) {
        MaskUnit du;
        if(lvs != null && f.getPCoin() != null)
            du = talent?f.getPCoin().improve(lvs):f.du;
        else
            du = f.du;

        if(frse == 0)
            return t.getFinRes(du.getRespawn()) +" f";
        else
            return new DecimalFormat("#.##").format((double) t.getFinRes(du.getRespawn())/30) +" s";
    }

    public String getAtk(Form f,Treasure t,int lev,boolean talent,int [] lvs) {
        MaskUnit du;
        if(lvs != null && f.getPCoin() != null)
            du = talent?f.getPCoin().improve(lvs):f.du;
        else
            du = f.du;

        if(du.rawAtkData().length > 1)
            return getTotAtk(f, t, lev,talent,lvs) +" "+getAtks(f,t,lev,talent,lvs);
        else
            return getTotAtk(f,t,lev,talent,lvs);
    }

    public String getAtk(Enemy em,int multi) {
        if(em.de.rawAtkData().length > 1)
            return getTotAtk(em,multi)+" "+getAtks(em,multi);
        else
            return getTotAtk(em,multi);
    }

    public String getSpd(Form f, boolean talent,int [] lvs) {
        MaskUnit du;
        if(lvs != null && f.getPCoin() != null)
            du = talent?f.getPCoin().improve(lvs):f.du;
        else
            du = f.du;

        return String.valueOf(du.getSpeed());
    }

    public String getSpd(Enemy em) {
        return String.valueOf(em.de.getSpeed());
    }

    public String getBarrier(Enemy em) {
        if(em.de.getShield() == 0)
            return c.getString(R.string.unit_info_t_none);
        else
            return String.valueOf(em.de.getShield());
    }

    public String getHB(Form f,boolean talent,int [] lvs) {
        MaskUnit du;
        if(lvs != null && f.getPCoin() != null)
            du = talent?f.getPCoin().improve(lvs):f.du;
        else
            du = f.du;

        return String.valueOf(du.getHb());
    }

    public String getHB(Enemy em) {
        return String.valueOf(em.de.getHb());
    }

    public String getHP(Form f, Treasure t, int lev,boolean talent,int [] lvs) {
        MaskUnit du;
        if(lvs != null && f.getPCoin() != null)
            du = talent?f.getPCoin().improve(lvs):f.du;
        else
            du = f.du;

        return String.valueOf((int)(du.getHp()*t.getDefMulti()*f.unit.lv.getMult(lev)));
    }

    public String getHP(Enemy em, int multi) {
        return String.valueOf((int)(em.de.multi(BasisSet.current)*em.de.getHp()*multi/100));
    }

    public String getTotAtk(Form f,Treasure t,int lev, boolean talent, int [] lvs) {
        MaskUnit du;
        if(lvs != null && f.getPCoin() != null)
            du = talent?f.getPCoin().improve(lvs):f.du;
        else
            du = f.du;

        return String.valueOf((int)(du.allAtk()*t.getAtkMulti()*f.unit.lv.getMult(lev)));
    }

    public String getTotAtk(Enemy em, int multi) {
        return String.valueOf((int)(em.de.multi(BasisSet.current)*em.de.allAtk()*multi/100));
    }

    public String getDPS(Form f,Treasure t,int lev,boolean talent, int[] lvs) {
        return String.valueOf(new DecimalFormat("#.##").format(Double.parseDouble(getTotAtk(f,t,lev,talent,lvs))/((double)f.du.getItv()/30)));
    }

    public String getDPS(Enemy em, int multi) {
        return String.valueOf(new DecimalFormat("#.##").format(Double.parseDouble(getTotAtk(em,multi))/((double)em.de.getItv()/30)));
    }

    public String getTrait(Form ef,boolean talent, int [] lvs) {
        MaskUnit du;
        if(lvs != null && ef.getPCoin() != null)
            du = talent?ef.getPCoin().improve(lvs):ef.du;
        else
            du = ef.du;

        StringBuilder allcolor = new StringBuilder();
        StringBuilder alltrait = new StringBuilder();

        for(int i = 0; i< Interpret.TRAIT.length; i++) {
            if (i != 0)
                allcolor.append(Interpret.TRAIT[i]).append(", ");
            alltrait.append(Interpret.TRAIT[i]).append(", ");
        }

        String result;

        result = Interpret.getTrait(du.getType(), 0);

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

    public String getTrait(Enemy em) {
        MaskEnemy de = em.de;

        StringBuilder allcolor = new StringBuilder();
        StringBuilder alltrait = new StringBuilder();

        for(int i = 0; i< Interpret.TRAIT.length; i++) {
            if (i != 0)
                allcolor.append(Interpret.TRAIT[i]).append(", ");
            alltrait.append(Interpret.TRAIT[i]).append(", ");
        }

        String result;
        int star = de.getStar();

        result = Interpret.getTrait(de.getType(), star);

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

    public String getCost(Form f,boolean talent,int [] lvs) {
        MaskUnit du;
        if(lvs != null && f.getPCoin() != null)
            du = talent?f.getPCoin().improve(lvs):f.du;
        else
            du = f.du;

        return String.valueOf((int)(du.getPrice()*1.5));
    }

    public String getDrop(Enemy em,Treasure t) {
        return String.valueOf((int)(em.de.getDrop()*t.getDropMulti()));
    }

    public String getAtks(Form f,Treasure t, int lev,boolean talent, int[] lvs) {
        MaskUnit du;
        if(lvs != null && f.getPCoin() != null)
            du = talent?f.getPCoin().improve(lvs):f.du;
        else
            du = f.du;

        int[][] atks = du.rawAtkData();

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

    public String getAtks(Enemy em, int multi) {
        int[][] atks = em.de.rawAtkData();

        ArrayList<Integer> damages = new ArrayList<>();

        for(int[] atk : atks) {
            damages.add((int)(atk[0]*em.de.multi(BasisSet.current)*multi/100));
        }

        StringBuilder result = new StringBuilder("(");

        for(int i = 0;i<damages.size();i++) {
            if(i < damages.size()-1)
                result.append("").append(damages.get(i)).append((", "));
            else
                result.append("").append(damages.get(i)).append(")");
        }

        return result.toString();
    }

    public String getSimu(Form f) {
        if(Interpret.isType(f.du,1))
            return c.getString(R.string.sch_atk_ra);
        else
            return c.getString(R.string.sch_atk_si);
    }

    public String getSimu(Enemy em) {
        if(Interpret.isType(em.de,1))
            return c.getString(R.string.sch_atk_ra);
        else
            return c.getString(R.string.sch_atk_si);
    }

    public String getTalentName(int index,Form f) {
        String ans = "";

        int [][] info = f.getPCoin().info;
        List<String> abil = Arrays.asList(abilID);

        List<String> trait = Arrays.asList("37","38","39","40");
        List<String> basic = Arrays.asList("25","26","27","31","32");

        if(trait.contains(String.valueOf(info[index][0])))
            ans = c.getString(R.string.talen_trait) + talTool[abil.indexOf(String.valueOf(info[index][0]))];
        else if(basic.contains(String.valueOf(info[index][0])))
            ans = talTool[abil.indexOf(String.valueOf(info[index][0]))];
        else
            ans = c.getString(R.string.talen_abil) + talTool[abil.indexOf(String.valueOf(info[index][0]))];


        return ans;
    }

    public String number(int num) {
        if (0 <= num && num < 10) {
            return "00" + num;
        } else if (10 <= num && num <= 99) {
            return "0" + num;
        } else {
            return String.valueOf(num);
        }
    }

    public String getID(int mapcode,int stid, int posit) {
        int p = mapcodes.indexOf(String.valueOf(mapcode));

        if(p == -1 || p >= mapcolcid.length) {
            return mapcode +"-"+stid+"-"+posit;
        } else {
            return mapcolcid[p]+"-"+stid+"-"+posit;
        }
    }

    public String getDifficulty(int diff) {
        if(diff >= diffid.length || diff < 0) {
            if(diff == -1)
                return c.getString(R.string.unit_info_t_none);
            else
                return String.valueOf(diff);
        } else {
            return c.getString(diffid[diff]);
        }
    }

    public String getLayer(int [] data) {
        if(data[SCDef.L0] == data[SCDef.L1])
            return ""+data[SCDef.L0];
        else
            return data[SCDef.L0] + " ~ " + data[SCDef.L1];
    }

    public String getRespawn(int [] data, boolean frse) {
        if(data[SCDef.R0] == data[SCDef.R1])
            if(frse)
                return data[SCDef.R0]+" f";
            else
                return new DecimalFormat("#.##").format((float)data[SCDef.R0]/30) + " s";
        else
            if(frse)
                return data[SCDef.R0] + " f ~ " + data[SCDef.R1]+" f";
            else
                return new DecimalFormat("#.##").format((float)data[SCDef.R0]/30) +" s ~ "+ new DecimalFormat("#.##").format((float)data[SCDef.R1]/30) + " s";
    }

    public String getBaseHealth(int [] data) {
        return data[SCDef.C0] + " %";
    }

    public String getMultiply(int [] data, int multi) {
        return (int)((float)data[SCDef.M]*(float)multi/(float)100) + " %";
    }

    public String getNumber(int [] data) {
        if(data[SCDef.N] == 0)
            return c.getString(R.string.infinity);
        else
            return String.valueOf(data[SCDef.N]);
    }

    public String getStart(int [] data, boolean frse) {
        if(frse)
            return data[SCDef.S0]+" f";
        else
            return new DecimalFormat("#.##").format((float)data[SCDef.S0]/30) + " s";
    }
}
