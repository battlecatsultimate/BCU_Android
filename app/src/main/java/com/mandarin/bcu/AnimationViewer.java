package com.mandarin.bcu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.util.Interpret;
import com.mandarin.bcu.util.pack.Pack;
import com.mandarin.bcu.util.system.android.BMBuilder;
import com.mandarin.bcu.util.system.fake.ImageBuilder;
import com.mandarin.bcu.util.system.files.VFile;
import com.mandarin.bcu.util.unit.Form;
import com.mandarin.bcu.util.unit.Unit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class AnimationViewer extends AppCompatActivity {

    protected ImageButton search;
    private ListView list;
    private ProgressBar prog;
    private String unitpath;
    private int unitnumber;
    static final int REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation_viewer);

        ImageBuilder builder = new BMBuilder();

        unitpath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/unit/";

        File f = new File(unitpath);
        unitnumber = f.listFiles().length;

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
            try {
                if(StaticStore.units==null)
                    Unit.readData();

                if(StaticStore.units == null)
                    StaticStore.units = Pack.def.us.ulist.getList();
            } catch (IOException e) {
                e.printStackTrace();
            }

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

                    StaticStore.bitmaps[i] = VFile.getFile(shortPath).getData().getImg().bimg();

                }
            }

            publishProgress(0);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            ArrayList<Integer> locate = new ArrayList<>();
            for(int i = 0; i < unitnumber;i++) {
                locate.add(i);
            }
            Adapters adap = new Adapters(AnimationViewer.this,StaticStore.names,StaticStore.bitmaps,locate);
            list.setAdapter(adap);
            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(AnimationViewer.this,showName(locate.get(position)),Toast.LENGTH_SHORT).show();

                    return false;
                }
            });
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            list.setVisibility(View.VISIBLE);
            prog.setVisibility(View.GONE);
        }
    }

    protected String showName(int location) {
        ArrayList<String> names = new ArrayList<>();

        for(Form f : StaticStore.units.get(location).forms) {
            names.add(f.name);
        }

        String result = withID(location,names.get(0));

        for(int i = 1; i < names.size();i++) {
            result += " - " + names.get(i);
        }

        return result;
    }

    protected String withID(int id, String name) {
        String result;

        if(name == "") {
            result = number(id);
        } else {
            result = number(id)+" - "+name;
        }

        return result;
    }

    protected void gotoFilter() {
        Intent intent = new Intent(this, SearchFilter.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    class Adapters extends ArrayAdapter<String> {
        private final Activity context;
        private final String[] name;
        private final Bitmap[] img;
        private final ArrayList<Integer> locate;

        Adapters(Activity context, String[] name, Bitmap[] img,ArrayList<Integer> location) {
            super(context, R.layout.listlayout, name);

            this.context = context;
            this.name = name;
            this.img = img;
            this.locate = location;
        }

        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View row = inflater.inflate(R.layout.listlayout, null, true);

            TextView title = row.findViewById(R.id.unitname);
            ImageView image = row.findViewById(R.id.uniticon);

            title.setText(name[position]);
            image.setImageBitmap(img[locate.get(position)]);

            return row;
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ArrayList<String> rarity;
        ArrayList<String> attack;
        ArrayList<String> target;
        ArrayList<ArrayList<Integer>> ability;
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
            ability = (ArrayList<ArrayList<Integer>>) extra.getSerializable("ability");
            System.out.println(ability);
            atksimu = extra.getBoolean("atksimu");
            atkorand = extra.getBoolean("atkorand");
            tgorand = extra.getBoolean("tgorand");
            aborand = extra.getBoolean("aborand");

            FilterUnit filterUnit = new FilterUnit(rarity,attack,target,ability,atksimu,atkorand,tgorand,aborand,empty,unitnumber);
            ArrayList<Integer> newNumber = filterUnit.setFilter();
            ArrayList<String> newName = new ArrayList<>();

            for(int i : newNumber) {
                newName.add(StaticStore.names[i]);
            }

            Adapters adapters = new Adapters(this,newName.toArray(new String[newName.size()]),StaticStore.bitmaps,newNumber);
            list.setAdapter(adapters);
            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(AnimationViewer.this,showName(newNumber.get(position)),Toast.LENGTH_SHORT).show();

                    return false;
                }
            });



            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AnimationViewer.this,SearchFilter.class);

                    intent.putExtra("empty",empty);
                    intent.putExtra("tgorand",tgorand);
                    intent.putExtra("atksimu",atksimu);
                    intent.putExtra("aborand",aborand);
                    intent.putExtra("atkorand",atkorand);
                    intent.putExtra("target",target);
                    intent.putExtra("attack",attack);
                    intent.putExtra("rare",rarity);
                    intent.putExtra("ability",ability);
                    setResult(Activity.RESULT_OK,intent);
                    startActivityForResult(intent,REQUEST_CODE);
                }
            });
        }
    }
}
