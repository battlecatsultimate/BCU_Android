package com.mandarin.bcu.androidutil;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.util.Interpret;
import com.mandarin.bcu.util.Res;
import com.mandarin.bcu.util.basis.BasisSet;
import com.mandarin.bcu.util.basis.Treasure;
import com.mandarin.bcu.util.unit.EForm;
import com.mandarin.bcu.util.unit.Form;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UnitinfRecycle extends RecyclerView.Adapter<UnitinfRecycle.ViewHolder> {
    private Activity context;
    private final ArrayList<String> names;
    private final Form[] forms;
    private final int id;
    private int fs = 0;
    private getStrings s;
    private String [][] fragment = {{"Immune to "},{""}};


    public UnitinfRecycle(Activity context, ArrayList<String> names, Form[] forms, int id) {
        this.context = context;
        this.names = names;
        this.forms = forms;
        this.id = id;
        s = new getStrings(this.context);

        if(StaticStore.addition == null) {
            int[] addid = {R.string.unit_info_strong, R.string.unit_info_resis, R.string.unit_info_masdam, R.string.unit_info_exmon, R.string.unit_info_atkbs, R.string.unit_info_wkill, R.string.unit_info_evakill, R.string.unit_info_insres, R.string.unit_info_insmas};
            StaticStore.addition = new String[addid.length];
            for (int i = 0; i < addid.length; i++)
                StaticStore.addition[i] = context.getString(addid[i]);
        }

        if(StaticStore.icons == null) {
            int[] abiconid = {R.mipmap.ic_strong, R.mipmap.ic_resist, R.mipmap.ic_md, R.mipmap.ic_target, R.mipmap.ic_em, R.mipmap.ic_cb, R.mipmap.ic_met, R.mipmap.ic_white, R.mipmap.ic_imws, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_zk,
                    R.mipmap.ic_imwk, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_eva, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_inr, R.mipmap.ic_ind};
            StaticStore.icons = new Drawable[abiconid.length];
            for (int i = 0; i < abiconid.length; i++)
                StaticStore.icons[i] = context.getDrawable(abiconid[i]);
        }

        if(StaticStore.picons == null) {
            int[] proiconid = {R.mipmap.ic_kb, R.mipmap.ic_freeze, R.mipmap.ic_slow, R.mipmap.ic_critical, R.mipmap.ic_wv, R.mipmap.ic_weaken, R.mipmap.ic_bb, R.mipmap.ic_wa, R.mipmap.ic_white, R.mipmap.ic_stren, R.mipmap.ic_survive, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_imkb,
                    R.mipmap.ic_imfr, R.mipmap.ic_imsl, R.mipmap.ic_imwv, R.mipmap.ic_imwe, R.mipmap.ic_imwa, R.mipmap.ic_imcu, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white, R.mipmap.ic_white};
            StaticStore.picons = new Drawable[proiconid.length];

            for (int i = 0; i < proiconid.length; i++)
                StaticStore.picons[i] = context.getDrawable(proiconid[i]);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(context).inflate(R.layout.unit_table,viewGroup,false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Treasure t = BasisSet.current.t();
        Form f = forms[viewHolder.getAdapterPosition()];
        EForm ef = new EForm(f,f.unit.getPrefLvs());
        List<String> ability = Interpret.getAbi(f.du,fragment,StaticStore.addition,0);
        List<Integer> abilityicon = Interpret.getAbiid(f.du);

        String language = Locale.getDefault().getLanguage();
        List<String> proc = new ArrayList<>();
        if(language == "ko")
            proc = Interpret.getProc(f.du,1);
        else
            proc = Interpret.getProc(f.du,0);
        List<Integer> procicon = Interpret.getProcid(f.du);

        viewHolder.uniticon.setImageBitmap(f.anim.uni.getIcon());
        viewHolder.unitname.setText(names.get(i));
        viewHolder.unitid.setText(s.getID(f,viewHolder,number(id)));
        viewHolder.unithp.setText(s.getHP(f,t,f.unit.getPrefLv()));
        viewHolder.unithb.setText(s.getHB(f));
        viewHolder.unitatk.setText(s.getTotAtk(f,t,f.unit.getPrefLv()));
        viewHolder.unittrait.setText(s.getTrait(ef));
        viewHolder.unitcost.setText(s.getCost(f));
        viewHolder.unitsimu.setText(s.getSimu(f));
        viewHolder.unitspd.setText(s.getSpd(f));
        viewHolder.unitcd.setText(s.getCD(f,t,0));
        viewHolder.unitrang.setText(s.getRange(f));
        viewHolder.unitpreatk.setText(s.getPre(f,0));
        viewHolder.unitpost.setText(s.getPost(f,0));
        viewHolder.unittba.setText(s.getTBA(f,0));
        viewHolder.unitatkt.setText(s.getAtkTime(f,0));
        viewHolder.unitabilt.setText(s.getAbilT(f));

        if(ability.size()>0 || proc.size() > 0) {
            viewHolder.none.setVisibility(View.GONE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            viewHolder.unitabil.setLayoutManager(linearLayoutManager);
            AdapterAbil adapterAbil = new AdapterAbil(ability, proc,abilityicon,procicon);
            viewHolder.unitabil.setAdapter(adapterAbil);
            ViewCompat.setNestedScrollingEnabled(viewHolder.unitabil, false);
        } else {
            viewHolder.unitabil.setVisibility(View.GONE);
        }

        Listeners(viewHolder);
    }

    private void Listeners(ViewHolder viewHolder) {
        Treasure t = BasisSet.current.t();
        Form f = forms[viewHolder.getAdapterPosition()];

        List<Integer> levels = new ArrayList<>();
        for(int j =1;j < f.unit.max+1;j++)
            levels.add(j);

        ArrayList<Integer> levelsp = new ArrayList<>();
        for(int j=0;j<f.unit.maxp+1;j++)
            levelsp.add(j);

        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item,levels);
        ArrayAdapter<Integer> arrayAdapterp = new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item,levelsp);

        viewHolder.unitlevel.setAdapter(arrayAdapter);
        viewHolder.unitlevel.setSelection(getIndex(viewHolder.unitlevel,f.unit.max));
        viewHolder.unitlevelp.setAdapter(arrayAdapterp);
        viewHolder.unitlevelp.setSelection(getIndex(viewHolder.unitlevelp,f.unit.getPrefLv()-f.unit.max));

        if(levelsp.size() == 1) {
            viewHolder.unitlevelp.setVisibility(View.GONE);
            viewHolder.unitplus.setVisibility(View.GONE);
        }

        viewHolder.frse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fs == 0) {
                    viewHolder.unitcd.setText(s.getCD(f,t,1));
                    viewHolder.unitpreatk.setText(s.getPre(f,1));
                    viewHolder.unitpost.setText(s.getPost(f,1));
                    viewHolder.unittba.setText(s.getTBA(f,1));
                    viewHolder.unitatkt.setText(s.getAtkTime(f,1));
                    viewHolder.frse.setText(context.getString(R.string.unit_info_sec));

                    fs = 1;
                } else {
                    viewHolder.unitcd.setText(s.getCD(f,t,0));
                    viewHolder.unitpreatk.setText(s.getPre(f,0));
                    viewHolder.unitpost.setText(s.getPost(f,0));
                    viewHolder.unittba.setText(s.getTBA(f,0));
                    viewHolder.unitatkt.setText(s.getAtkTime(f,0));
                    viewHolder.frse.setText(context.getString(R.string.unit_info_fr));

                    fs = 0;
                }
            }
        });

        viewHolder.unitcdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.unitcd.getText().toString().endsWith("f"))
                    viewHolder.unitcd.setText(s.getCD(f,t,1));
                else
                    viewHolder.unitcd.setText(s.getCD(f,t,0));
            }
        });

        viewHolder.unitpreatkb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.unitpreatk.getText().toString().endsWith("f"))
                    viewHolder.unitpreatk.setText(s.getPre(f,1));
                else
                    viewHolder.unitpreatk.setText(s.getPre(f,0));
            }
        });

        viewHolder.unitpostb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.unitpost.getText().toString().endsWith("f"))
                    viewHolder.unitpost.setText(s.getPost(f,1));
                else
                    viewHolder.unitpost.setText(s.getPost(f,0));
            }
        });

        viewHolder.unittbab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.unittba.getText().toString().endsWith("f"))
                    viewHolder.unittba.setText(s.getTBA(f,1));
                else
                    viewHolder.unittba.setText(s.getTBA(f,0));
            }
        });

        viewHolder.unitatkb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int level = (int)viewHolder.unitlevel.getSelectedItem();
                int levelp = (int)viewHolder.unitlevelp.getSelectedItem();
                if(viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk))) {
                    viewHolder.unitatkb.setText(context.getString(R.string.unit_info_dps));
                    viewHolder.unitatk.setText(s.getDPS(f,t,level+levelp));
                } else {
                    viewHolder.unitatkb.setText(context.getString(R.string.unit_info_atk));
                    viewHolder.unitatk.setText(s.getAtk(f,t,level+levelp));
                }
            }
        });

        viewHolder.unitatktb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.unitatkt.getText().toString().endsWith("f"))
                    viewHolder.unitatkt.setText(s.getAtkTime(f,1));
                else
                    viewHolder.unitatkt.setText(s.getAtkTime(f,0));
            }
        });

        viewHolder.unitlevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int level = (int)viewHolder.unitlevel.getSelectedItem();
                int levelp = (int)viewHolder.unitlevelp.getSelectedItem();
                viewHolder.unithp.setText(s.getHP(f,t,level+levelp));

                if(f.du.rawAtkData().length > 1) {
                    if (viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk)))
                        viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp));
                    else
                        viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp));
                } else {
                    if (viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk)))
                        viewHolder.unitatk.setText(s.getTotAtk(f, t, level + levelp));
                    else
                        viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        viewHolder.unitlevelp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int level = (int)viewHolder.unitlevel.getSelectedItem();
                int levelp = (int)viewHolder.unitlevelp.getSelectedItem();
                viewHolder.unithp.setText(s.getHP(f,t,level+levelp));
                if(f.du.rawAtkData().length > 1) {
                    if (viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk)))
                        viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp));
                    else
                        viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp));
                } else {
                    if (viewHolder.unitatkb.getText().equals(context.getString(R.string.unit_info_atk)))
                        viewHolder.unitatk.setText(s.getAtk(f, t, level + levelp));
                    else
                        viewHolder.unitatk.setText(s.getDPS(f, t, level + levelp));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private int getIndex(Spinner spinner, int lev) {
        int index = 0;

        for(int i = 0; i< spinner.getCount();i++)
            if (lev == (int)spinner.getItemAtPosition(i))
                index = i;

        return index;
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        Button frse;
        TextView unitname;
        TextView unitid;
        TextView unithp;
        TextView unithb;
        Spinner unitlevel;
        Spinner unitlevelp;
        TextView unitplus;
        ImageView uniticon;
        Button unitatkb;
        TextView unitatk;
        TextView unittrait;
        TextView unitcost;
        TextView unitsimu;
        TextView unitspd;
        Button unitcdb;
        TextView unitcd;
        TextView unitrang;
        Button unitpreatkb;
        TextView unitpreatk;
        Button unitpostb;
        TextView unitpost;
        Button unittbab;
        TextView unittba;
        Button unitatktb;
        TextView unitatkt;
        TextView unitabilt;
        TextView none;
        RecyclerView unitabil;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            frse = itemView.findViewById(R.id.unitinffrse);
            unitname = itemView.findViewById(R.id.unitinfname);
            unitid = itemView.findViewById(R.id.unitinfidr);
            uniticon = itemView.findViewById(R.id.unitinficon);
            unithp = itemView.findViewById(R.id.unitinfhpr);
            unithb = itemView.findViewById(R.id.unitinfhbr);
            unitlevel = itemView.findViewById(R.id.unitinflevr);
            unitlevelp = itemView.findViewById(R.id.unitinflevpr);
            unitplus = itemView.findViewById(R.id.unitinfplus);
            unitplus.setText(" + ");
            unitatkb = itemView.findViewById(R.id.unitinfatk);
            unitatk = itemView.findViewById(R.id.unitinfatkr);
            unittrait = itemView.findViewById(R.id.unitinftraitr);
            unitcost = itemView.findViewById(R.id.unitinfcostr);
            unitsimu = itemView.findViewById(R.id.unitinfsimur);
            unitspd = itemView.findViewById(R.id.unitinfspdr);
            unitcdb = itemView.findViewById(R.id.unitinfcd);
            unitcd = itemView.findViewById(R.id.unitinfcdr);
            unitrang = itemView.findViewById(R.id.unitinfrangr);
            unitpreatkb = itemView.findViewById(R.id.unitinfpreatk);
            unitpreatk = itemView.findViewById(R.id.unitinfpreatkr);
            unitpostb = itemView.findViewById(R.id.unitinfpost);
            unitpost = itemView.findViewById(R.id.unitinfpostr);
            unittbab = itemView.findViewById(R.id.unitinftba);
            unittba = itemView.findViewById(R.id.unitinftbar);
            unitatktb = itemView.findViewById(R.id.unitinfatktime);
            unitatkt = itemView.findViewById(R.id.unitinfatktimer);
            unitabilt = itemView.findViewById(R.id.unitinfabiltr);
            none = itemView.findViewById(R.id.unitabilnone);
            unitabil = itemView.findViewById(R.id.unitinfabilr);
            unitabil.requestFocusFromTouch();
        }
    }

    private String number(int num) {
        if (0 <= num && num < 10) {
            return "00" + num;
        } else if (10 <= num && num <= 99) {
            return "0" + num;
        } else {
            return String.valueOf(num);
        }
    }

    class AdapterAbil extends RecyclerView.Adapter<AdapterAbil.ViewHolder> {
        private List<String> ability;
        private List<String> procs;
        private List<Integer> abilicon;
        private List<Integer> procicon;

        AdapterAbil(List<String> ability,List<String> procs, List<Integer> abilicon, List<Integer> procicon) {
            this.ability = ability;
            this.procs = procs;
            this.abilicon = abilicon;
            this.procicon = procicon;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View row = LayoutInflater.from(context).inflate(R.layout.ability_layout,viewGroup,false);

            return new ViewHolder(row);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            if(viewHolder.getAdapterPosition() < ability.size()) {
                viewHolder.abiltext.setText(ability.get(viewHolder.getAdapterPosition()));
                if (abilicon.get(viewHolder.getAdapterPosition()) != 15 && abilicon.get(viewHolder.getAdapterPosition()) != 19) {
                    Bitmap resized = getResize(StaticStore.icons[abilicon.get(viewHolder.getAdapterPosition())]);
                    viewHolder.abilicon.setImageBitmap(resized);
                } else {
                    viewHolder.abilicon.setImageBitmap(empty());
                }
            } else {
                int location = viewHolder.getAdapterPosition()-ability.size();
                viewHolder.abiltext.setText(procs.get(location));
                Bitmap resized = getResize(StaticStore.picons[procicon.get(location)]);
                viewHolder.abilicon.setImageBitmap(resized);
            }
        }

        @Override
        public int getItemCount() {
            return ability.size()+procs.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView abilicon;
            TextView abiltext;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                abilicon = itemView.findViewById(R.id.abilicon);
                abiltext = itemView.findViewById(R.id.ability);
            }
        }

        private Bitmap getResize(Drawable drawable) {
            float dp = 32f;
            Resources r = context.getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,r.getDisplayMetrics());
            Bitmap b = ((BitmapDrawable)drawable).getBitmap();
            return Bitmap.createScaledBitmap(b,(int)px,(int)px,false);
        }

        private Bitmap empty() {
            float dp =32f;
            Resources r = context.getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,r.getDisplayMetrics());
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            return Bitmap.createBitmap((int)px,(int)px,conf);
        }

    }
}
