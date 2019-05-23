package com.mandarin.bcu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

public class AnimationViewer extends AppCompatActivity {

    protected ImageButton search;
    private ListView list;
    private ProgressBar prog;
    private String unitpath;
    private int unitnumber;
    static final int REQUEST_CODE = 1;
    private Bitmap[] bitmaps;
    private String[] names;

    private String[] unitrarity;
    private String[][][] unitattack;
    private String[][][] unittarget;
    private String[][][] unitabil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation_viewer);

        unitpath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/unit/";

        File f = new File(unitpath);
        unitnumber = f.listFiles().length;
        names = new String[unitnumber];
        bitmaps = new Bitmap[unitnumber];
        unitrarity = new String[unitnumber];
        unitattack = new String[unitnumber][][];
        unittarget = new String[unitnumber][][];
        unitabil = new String[unitnumber][][];

        ImageButton back = findViewById(R.id.animbck);
        search = findViewById(R.id.animsch);
        prog = findViewById(R.id.unitinfprog);
        list = findViewById(R.id.unitinflist);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoFilter();
            }
        });

        new Adder().execute();
    }

    class Adder extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            list.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String[] prior = chooser();
            getName(prior);

            for (int i = 0; i < unitnumber; i++) {

                File imgfile = new File(unitpath+number(i)+"/f/uni"+number(i)+"_f00.png");
                bitmaps[i] = BitmapFactory.decodeFile(imgfile.getAbsolutePath());

            }

            findrarity();
            findAttack();
            findTarget();
            findAbility();

            publishProgress(0);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Adapters adap = new Adapters(AnimationViewer.this,names,bitmaps);
            list.setAdapter(adap);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            list.setVisibility(View.VISIBLE);
            prog.setVisibility(View.GONE);
        }
    }

    protected void gotoFilter() {
        Intent intent = new Intent(this, SearchFilter.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    class Adapters extends ArrayAdapter<String> {
        private final Activity context;
        private final String[] name;
        private final Bitmap[] img;

        Adapters(Activity context, String[] name, Bitmap[] img) {
            super(context, R.layout.listlayout, name);

            this.context = context;
            this.name = name;
            this.img = img;
        }

        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View row = inflater.inflate(R.layout.listlayout, null, true);

            TextView title = row.findViewById(R.id.unitname);
            ImageView image = row.findViewById(R.id.uniticon);

            title.setText(name[position]);
            image.setImageBitmap(img[position]);

            return row;
        }
    }

    protected String[] chooser() {
        String language = Locale.getDefault().getLanguage();
        String [] priority;
        switch (language) {
            case "en":
                priority = new String[]{"/en/", "/jp/", "/zh/", "/kr/"};
                break;
            case "ja":
                priority = new String[]{"/jp/", "/en/", "/zh/", "/kr/"};
                break;
            case "th":
                priority = new String[]{"/zh/", "/jp/", "/en/", "/kr/"};
                break;
            case "ko":
                priority = new String[]{"/kr/", "/jp/", "/en/", "/zh/"};
                break;
            default:
                priority = new String[]{"/en/", "/jp/", "/zh/", "/kr/"};
                break;
        }

        return priority;
    }

    protected String number(int num) {
        if (0 <= num && num < 10) {
            return "00" + num;
        } else if (10 <= num && num <= 99) {
            return "0" + num;
        } else {
            return String.valueOf(num);
        }
    }

    protected void getName(String [] prior) {
        String [][] uninames = new String[4][];
        String namepath;

        for(int i=0; i<prior.length;i++) {
            namepath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/lang"+prior[i]+"UnitName.txt";

            try{
                File g = new File(namepath);
                FileInputStream fis = new FileInputStream(g);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder str = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    str.append(line).append(System.getProperty("line.separator"));
                }

                String[] lit = str.toString().split("\n");
                uninames[i] = lit;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for(int i=0;i<unitnumber;i++) {
            names[i] = findname(i,uninames[0]);
            if(names[i] == null) {
                names[i] = findname(i,uninames[1]);

                if(names[i] == null) {
                    names[i] = findname(i,uninames[2]);

                    if(names[i] == null) {
                        names[i] = findname(i,uninames[3]);

                        if(names[i] == null) {
                            names[i] = "";
                        }
                    }
                }
            }

            if(names[i].equals("")) {
                names[i] = number(i);
            } else {
                names[i] = number(i) + " - "+names[i];
            }
        }
    }

    protected String findname(int num,String [] names) {
        String name = null;

        if(names.length > num) {
            String [] wait = names[num].split("\t");
            if(wait.length > 1) {
                if(!wait[1].equals("")) {
                    name = wait[1];
                }
            }
        }

        return name;
    }

    protected void findrarity() {
        try {
            String buypath = Environment.getExternalStorageDirectory().getPath() +"/Android/data/com.mandarin.BCU/files/org/data/unitbuy.csv";
            File g = new File(buypath);
            FileInputStream fis = new FileInputStream(g);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder str = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                str.append(line).append(System.getProperty("line.separator"));
            }

            String [] wait = str.toString().split("\n");

            for (int i = 0; i < wait.length; i++) {
                String s = wait[i];
                String[] wait2 = s.split(",");
                unitrarity[i] = wait2[13];
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void findAttack() {
        for(int i =0;i<unitnumber;i++) {
            try {
                String path = unitpath + number(i) +"/unit"+number(i)+".csv";
                File g = new File(path);
                FileInputStream fis = new FileInputStream(g);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder str = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    str.append(line).append(System.getProperty("line.separator"));
                }

                String [] fcs = str.toString().split("\n");

                int checker = 0;

                for (String fc : fcs) {
                    String[] wait = fc.split(",");

                    if (!(wait.length < 10)) {
                        checker++;
                    }
                }

                String [][] attakinfo = new String[checker][];

                for(int j=0;j<attakinfo.length;j++) {
                    String [] wait = fcs[j].split(",");
                    if(wait.length < 10) {
                        continue;
                    }
                    int [] factor = {12,44,45,60};

                    ArrayList<String> result = new ArrayList<>();

                    for (int i1 : factor) {
                        if (i1 < wait.length) {
                            result.add("" + i1 + "|" + wait[i1]);
                        } else {
                            break;
                        }
                    }

                    attakinfo[j] = result.toArray(new String[0]);
                }

                unitattack[i] = attakinfo;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void findTarget() {
        for(int i =0;i<unitnumber;i++) {
            try {
                String path = unitpath + number(i) + "/unit" + number(i) + ".csv";
                File g = new File(path);
                FileInputStream fis = new FileInputStream(g);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder str = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    str.append(line).append(System.getProperty("line.separator"));
                }

                String[] fcs = str.toString().split("\n");

                int checker = 0;

                for (String fc : fcs) {
                    String[] wait = fc.split(",");

                    if (!(wait.length < 10)) {
                        checker++;
                    }
                }

                String [][] targetinfo = new String[checker][];

                for(int j=0;j<targetinfo.length;j++) {
                    targetinfo[j] = fcs[j].split(",");
                }

                unittarget[i] = targetinfo;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void findAbility() {
        for(int i =0;i<unitnumber;i++) {
            try {
                String path = unitpath + number(i) + "/unit" + number(i) + ".csv";
                File g = new File(path);
                FileInputStream fis = new FileInputStream(g);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder str = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    str.append(line).append(System.getProperty("line.separator"));
                }

                String[] fcs = str.toString().split("\n");

                int checker = 0;

                for (String fc : fcs) {
                    String[] wait = fc.split(",");

                    if (!(wait.length < 10)) {
                        checker++;
                    }
                }

                String [][] abilitytinfo = new String[checker][];

                for(int j=0;j<abilitytinfo.length;j++) {
                    abilitytinfo[j] = fcs[j].split(",");
                }

                unitabil[i] = abilitytinfo;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<String> filters;

        ArrayList<String> rarity;
        ArrayList<String> attack;
        ArrayList<String> target;
        ArrayList<String> ability;
        boolean atksimu;
        boolean atkorand;
        boolean tgorand;
        boolean aborand;
        boolean empty;

        if(resultCode == RESULT_OK) {
            assert data != null;
            Bundle extra = data.getExtras();

            assert extra != null;
            empty = extra.getBoolean("empty");
            rarity = extra.getStringArrayList("rare");
            attack = extra.getStringArrayList("attack");
            target = extra.getStringArrayList("target");
            ability = extra.getStringArrayList("ability");
            atksimu = extra.getBoolean("atksimu");
            atkorand = extra.getBoolean("atkorand");
            tgorand = extra.getBoolean("tgorand");
            aborand = extra.getBoolean("aborand");

            FilterUnit filterUnit = new FilterUnit(rarity,attack,target,ability,atksimu,atkorand,tgorand,aborand,empty,unitnumber);

            filters = filterUnit.setRarity(unitrarity);
            filters = filterUnit.setAttack(unitattack,filters);
            filters = filterUnit.setTarget(unittarget,filters);
            filters = filterUnit.setAbility(unitabil,filters);

            String [] newnames = new String[filters.size()];

            for(int i=0;i<filters.size();i++) {
                newnames[i] = names[Integer.parseInt(filters.get(i))];
            }

            FilterAdapters filterAdapters = new FilterAdapters(AnimationViewer.this,newnames,bitmaps,filters);
            list.setAdapter(filterAdapters);
        }
    }

    class FilterAdapters extends ArrayAdapter<String> {
        private final Activity context;
        private final String[] name;
        private final Bitmap[] img;
        private final ArrayList<String> filter;

        FilterAdapters(Activity context, String[] name, Bitmap[] img,ArrayList<String> filter) {
            super(context, R.layout.listlayout, name);

            this.context = context;
            this.name = name;
            this.img = img;
            this.filter = filter;
        }

        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View row = inflater.inflate(R.layout.listlayout, null, true);

            TextView title = row.findViewById(R.id.unitname);
            ImageView image = row.findViewById(R.id.uniticon);

            title.setText(name[position]);
            image.setImageBitmap(img[Integer.parseInt(filter.get(position))]);

            return row;
        }
    }


}
