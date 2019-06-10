package com.mandarin.bcu.androidutil.asynchs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mandarin.bcu.R;
import com.mandarin.bcu.UnitInfo;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.util.Interpret;
import com.mandarin.bcu.util.basis.BasisSet;
import com.mandarin.bcu.util.basis.Combo;
import com.mandarin.bcu.util.pack.Pack;
import com.mandarin.bcu.util.system.files.VFile;
import com.mandarin.bcu.util.unit.Form;
import com.mandarin.bcu.util.unit.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Adder extends AsyncTask<Void, Integer, Void> {
    private final int unitnumber;
    private final Context context;

    public Adder(int unitnumber, Context context) {
        this.unitnumber = unitnumber;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        ListView list = ((Activity)context).findViewById(R.id.unitinflist);

        list.setVisibility(View.GONE);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        new Definer().define(context);

        if(StaticStore.names == null) {
            StaticStore.names = new String[unitnumber];

            for(int i = 0; i < StaticStore.names.length;i++) {
                StaticStore.names[i] = withID(i,Pack.def.us.ulist.getList().get(i).forms[0].name);
            }
        }

        if(StaticStore.bitmaps == null) {
            StaticStore.bitmaps = new Bitmap[unitnumber];


            for (int i = 0; i < unitnumber; i++) {
                String shortPath = "./org/unit/"+ number(i) + "/f/uni" + number(i) + "_f00.png";

                StaticStore.bitmaps[i] = StaticStore.getResizeb(Objects.requireNonNull(VFile.getFile(shortPath)).getData().getImg().bimg(),context,48f);

            }
        }

        publishProgress(0);

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        ListView list = ((Activity)context).findViewById(R.id.unitinflist);
        ArrayList<Integer> locate = new ArrayList<>();
        for(int i = 0; i < unitnumber;i++) {
            locate.add(i);
        }
        Adapters adap = new Adapters((Activity)context,StaticStore.names,StaticStore.bitmaps,locate);
        list.setAdapter(adap);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(context,showName(locate.get(position)),Toast.LENGTH_SHORT).show();

                return false;
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent result = new Intent(context, UnitInfo.class);
                result.putExtra("ID",locate.get(position));
                context.startActivity(result);
            }
        });
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        ListView list = ((Activity)context).findViewById(R.id.unitinflist);
        ProgressBar prog = ((Activity)context).findViewById(R.id.unitinfprog);
        list.setVisibility(View.VISIBLE);
        prog.setVisibility(View.GONE);
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

    private String showName(int location) {
        ArrayList<String> names = new ArrayList<>();

        for(Form f : StaticStore.units.get(location).forms) {
            names.add(f.name);
        }

        StringBuilder result = new StringBuilder(withID(location, names.get(0)));

        for(int i = 1; i < names.size();i++) {
            result.append(" - ").append(names.get(i));
        }

        return result.toString();
    }

    private String withID(int id, String name) {
        String result;

        if(name.equals("")) {
            result = number(id);
        } else {
            result = number(id)+" - "+name;
        }

        return result;
    }
}

class Definer {
    private int [] colorid = {R.string.sch_wh,R.string.sch_red,R.string.sch_fl,R.string.sch_bla,R.string.sch_me,R.string.sch_an,R.string.sch_al,R.string.sch_zo,R.string.sch_re};
    private int [] starid = {R.string.unit_info_starred,R.string.unit_info_god1,R.string.unit_info_god2,R.string.unit_info_god3};
    private String [] starstring = new String[5];
    private String [] colorstring = new String[colorid.length];
    private int [] procid = {R.string.sch_abi_kb,R.string.sch_abi_fr,R.string.sch_abi_sl,R.string.sch_abi_cr,R.string.sch_abi_wv,R.string.sch_abi_we,R.string.sch_abi_bb,R.string.sch_abi_wa,R.string.sch_abi_cr,
            R.string.sch_abi_str,R.string.sch_abi_su,R.string.abi_bo,R.string.abi_rev,R.string.sch_abi_ik,R.string.sch_abi_if,R.string.sch_abi_is,R.string.sch_abi_iwv,R.string.sch_abi_iw,R.string.sch_abi_iwa,
            R.string.sch_abi_ic,R.string.abi_snk,R.string.abi_stt,R.string.abi_seal,R.string.abi_sum,R.string.abi_mvatk,R.string.abi_thch,R.string.abi_poi,R.string.abi_boswv};
    private String [] proc = new String[procid.length];
    private int [] abiid = {R.string.sch_abi_st,R.string.sch_abi_re,R.string.sch_abi_md,R.string.sch_abi_ao,R.string.sch_abi_em,R.string.sch_abi_bd,R.string.sch_abi_me,R.string.abi_imvatk,R.string.sch_abi_ws,
            R.string.abi_isnk,R.string.abi_istt,R.string.abi_gh,R.string.abi_ipoi,R.string.sch_abi_zk,R.string.sch_abi_wk,R.string.abi_sui,R.string.abi_ithch,R.string.sch_abi_eva,
            R.string.abi_iseal,R.string.abi_iboswv,R.string.sch_abi_it,R.string.sch_abi_id};
    private String [] abi = new String[abiid.length];
    private int [] textid = {R.string.unit_info_text0,R.string.unit_info_text1,R.string.unit_info_text2,R.string.unit_info_text3,R.string.unit_info_text4,R.string.unit_info_text5,R.string.unit_info_text6,R.string.unit_info_text7,
            R.string.def_unit_info_text8,R.string.unit_info_text9,R.string.unit_info_text10,R.string.def_unit_info_text11};
    private String [] textstring = new String[textid.length];

    void define(Context context) {
        try {
            if(StaticStore.units==null) {
                Unit.readData();
                StaticStore.units = Pack.def.us.ulist.getList();

                for(int i = 0;i<colorid.length;i++) {
                    colorstring[i] = context.getString(colorid[i]);
                }

                starstring[0] = "";

                for(int i = 0;i<starid.length;i++)
                    starstring[i+1] = context.getString(starid[i]);

                for(int i =0;i<procid.length;i++)
                    proc[i] = context.getString(procid[i]);

                for(int i=0;i<abiid.length;i++)
                    abi[i] = context.getString(abiid[i]);

                for(int i=0;i<textid.length;i++)
                    textstring[i] = context.getString(textid[i]);

                Interpret.TRAIT = colorstring;
                Interpret.STAR = starstring;
                Interpret.PROC = proc;
                Interpret.ABIS = abi;
                Interpret.TEXT = textstring;
            }

            if(StaticStore.t == null) {
                Combo.readFile();
                StaticStore.t = BasisSet.current.t();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
