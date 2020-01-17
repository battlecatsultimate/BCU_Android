package com.mandarin.bcu.androidutil.enemy.asynchs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mandarin.bcu.EnemyInfo;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.FilterEntity;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.enemy.EDefiner;
import com.mandarin.bcu.androidutil.enemy.adapters.EnemyListAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import common.system.MultiLangCont;

public class EAdder extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakReference;
    private int enemnumber;
    private ArrayList<Integer> numbers = new ArrayList<>();

    public EAdder(Activity activity,int enemnumber) {
        this.weakReference = new WeakReference<>(activity);
        this.enemnumber = enemnumber;
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        if(activity == null) return;

        ListView listView = activity.findViewById(R.id.enlist);
        listView.setVisibility(View.GONE);
        FloatingActionButton search = activity.findViewById(R.id.enlistsch);
        search.hide();
        EditText schname = activity.findViewById(R.id.enemlistschname);
        schname.setVisibility(View.GONE);

        FloatingActionButton back = activity.findViewById(R.id.enlistbck);
        back.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                StaticStore.filterReset();
                activity.finish();
            }
        });
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        if(activity == null) return null;

        new EDefiner().define(activity);

        publishProgress(0);

        if(StaticStore.enames == null) {
            StaticStore.enames = new String[StaticStore.emnumber];

            for(int i = 0;i<StaticStore.emnumber;i++) {
                StaticStore.enames[i] = withID(i, MultiLangCont.ENAME.getCont(StaticStore.enemies.get(i)));
            }
        }

        publishProgress(2);

        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onProgressUpdate(Integer... results) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        TextView enlistst = activity.findViewById(R.id.enlistst);

        switch(results[0]) {
            case 0:
                enlistst.setText(R.string.stg_info_enemname);
                break;
            case 1:
                enlistst.setText(R.string.stg_info_enemimg);
                break;
            case 2:
                ListView list = activity.findViewById(R.id.enlist);

                FilterEntity filterEntity;

                TextInputEditText schname = activity.findViewById(R.id.enemlistschname);

                if(schname.getText().toString().isEmpty())
                    filterEntity = new FilterEntity(enemnumber);
                else
                    filterEntity = new FilterEntity(enemnumber,schname.getText().toString());

                numbers = filterEntity.EsetFilter();
                ArrayList<String> names = new ArrayList<>();

                for(int i : numbers)
                    names.add(StaticStore.enames[i]);

                EnemyListAdapter enemy = new EnemyListAdapter(activity,names.toArray(new String[0]),numbers);
                list.setAdapter(enemy);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if(SystemClock.elapsedRealtime() - StaticStore.enemyinflistClick < StaticStore.INTERVAL)
                            return;

                        StaticStore.enemyinflistClick = SystemClock.elapsedRealtime();

                        Intent result = new Intent(activity, EnemyInfo.class);
                        result.putExtra("ID",numbers.get(position));
                        activity.startActivity(result);
                    }
                });

                schname.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        FilterEntity filterEntity = new FilterEntity(enemnumber,s.toString());
                        numbers = filterEntity.EsetFilter();
                        ArrayList<String> names = new ArrayList<>();

                        for(int i : numbers)
                            names.add(StaticStore.enames[i]);

                        EnemyListAdapter enemy = new EnemyListAdapter(activity,names.toArray(new String[0]),numbers);
                        list.setAdapter(enemy);

                        if(s.toString().isEmpty()) {
                            schname.setCompoundDrawablesWithIntrinsicBounds(null,null,activity.getDrawable(R.drawable.search),null);
                        } else {
                            schname.setCompoundDrawablesWithIntrinsicBounds(null,null,activity.getDrawable(R.drawable.ic_close_black_24dp),null);
                        }
                    }
                });

                break;
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        super.onPostExecute(result);
        TextView enlistst = activity.findViewById(R.id.enlistst);
        enlistst.setVisibility(View.GONE);
        ListView list = activity.findViewById(R.id.enlist);
        list.setVisibility(View.VISIBLE);
        ProgressBar prog = activity.findViewById(R.id.enlistprog);
        prog.setVisibility(View.GONE);
        FloatingActionButton search = activity.findViewById(R.id.enlistsch);
        search.show();
        EditText schname = activity.findViewById(R.id.enemlistschname);
        schname.setVisibility(View.VISIBLE);
    }

    private String number(int num) {
        if (0 <= num && num < 10) {
            return "00" + num;
        } else if (10 <= num && num <= 99) {
            return "0" + num;
        } else {
            return ""+num;
        }
    }

    private String withID(int id, String name) {
        String result;
        String names = name;

        if(names == null)
            names = "";

        if(names.equals("")) {
            result = number(id);
        } else {
            result = number(id)+" - "+names;
        }

        return result;
    }
}
