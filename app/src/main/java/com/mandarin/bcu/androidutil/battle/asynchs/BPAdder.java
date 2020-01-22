package com.mandarin.bcu.androidutil.battle.asynchs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mandarin.bcu.BattleSimulation;
import com.mandarin.bcu.LineUpScreen;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.io.ErrorLogWriter;
import com.mandarin.bcu.androidutil.lineup.LineUpView;
import com.mandarin.bcu.androidutil.unit.Definer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import common.battle.BasisSet;
import common.io.InStream;
import common.system.MultiLangCont;
import common.util.pack.Pack;
import common.util.stage.MapColc;
import common.util.stage.Stage;
import common.util.stage.StageMap;

public class BPAdder extends AsyncTask<Void, Integer, Void> {
    private final WeakReference<Activity> weakReference;
    private final int mapcode, stid, posit;

    private int selection = 0;

    private int item = 0;

    public BPAdder(Activity activity, int mapcode, int stid, int posit) {
        this.weakReference = new WeakReference<>(activity);
        this.mapcode = mapcode;
        this.stid = stid;
        this.posit = posit;
    }

    public BPAdder(Activity activity, int mapcode, int stid, int posit, int seleciton) {
        this.weakReference = new WeakReference<>(activity);
        this.mapcode = mapcode;
        this.stid = stid;
        this.posit = posit;
        this.selection = seleciton;
    }

    @Override
    public void onPreExecute() {
        Activity activity = weakReference.get();

        if (activity == null) return;

        TextView setname = activity.findViewById(R.id.lineupname);
        Spinner star = activity.findViewById(R.id.battlestar);
        Button equip = activity.findViewById(R.id.battleequip);
        CheckBox sniper = activity.findViewById(R.id.battlesniper);
        CheckBox rich = activity.findViewById(R.id.battlerich);
        Button start = activity.findViewById(R.id.battlestart);
        LinearLayout layout = activity.findViewById(R.id.preparelineup);
        TextView stname = activity.findViewById(R.id.battlestgname);

        View v = activity.findViewById(R.id.view);

        setDisappear(setname, star, equip, sniper, rich, start, layout, stname);

        if (v != null)
            setDisappear(v);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        if (activity == null) return null;

        new Definer().define(activity);

        if (StaticStore.LUnames == null) {
            StaticStore.LUnames = new String[StaticStore.unitnumber];

            for (int i = 0; i < StaticStore.LUnames.length; i++) {
                StaticStore.LUnames[i] = withID(i, MultiLangCont.FNAME.getCont(Pack.def.us.ulist.get(i).forms[0]));
            }
        }

        publishProgress(0);

        if (!StaticStore.LUread) {

            String Path = Environment.getExternalStorageDirectory().getPath() + "/BCU/user/basis.v";

            File f = new File(Path);

            SharedPreferences preferences = activity.getSharedPreferences(StaticStore.CONFIG,Context.MODE_PRIVATE);

            if (f.exists()) {
                if (f.length() != 0) {
                    byte[] buff = new byte[(int) f.length()];

                    try {
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));

                        bis.read(buff, 0, buff.length);
                        bis.close();

                        InStream is = InStream.getIns(buff);

                        try {
                            BasisSet.read(is);
                        } catch (Exception e) {
                            publishProgress(R.string.lineup_file_err);
                            BasisSet.list.clear();
                            new BasisSet();
                            ErrorLogWriter.WriteLog(e,preferences.getBoolean("upload",false)||preferences.getBoolean("ask_upload",true));
                        }
                    } catch (Exception e) {
                        ErrorLogWriter.WriteLog(e,preferences.getBoolean("upload",false)||preferences.getBoolean("ask_upload",true));
                    }
                }
            }

            StaticStore.LUread = true;
        }

        StaticStore.sets = BasisSet.list;

        SharedPreferences preferences = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);

        int set = preferences.getInt("equip_set", 0);
        int lu = preferences.getInt("equip_lu", 0);

        if(set >= BasisSet.list.size()) set = BasisSet.list.size()-1;

        BasisSet.current = StaticStore.sets.get(set);

        if(lu >= BasisSet.current.lb.size()) lu = BasisSet.current.lb.size()-1;

        BasisSet.current.sele = BasisSet.current.lb.get(lu);

        publishProgress(1);

        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onProgressUpdate(Integer... results) {
        Activity activity = weakReference.get();

        if (activity == null) return;

        TextView loadt = activity.findViewById(R.id.preparet);

        switch (results[0]) {
            case 0:
                loadt.setText(R.string.lineup_reading);
                break;
            case 1:
                LineUpView line = activity.findViewById(R.id.lineupView);
                TextView setname = activity.findViewById(R.id.lineupname);
                Spinner star = activity.findViewById(R.id.battlestar);
                Button equip = activity.findViewById(R.id.battleequip);
                CheckBox sniper = activity.findViewById(R.id.battlesniper);
                CheckBox rich = activity.findViewById(R.id.battlerich);
                Button start = activity.findViewById(R.id.battlestart);
                TextView stname = activity.findViewById(R.id.battlestgname);

                line.UpdateLineUp();

                setname.setText(getSetLUName());

                MapColc mc = MapColc.MAPS.get(mapcode);

                if (mc == null) return;

                if (stid >= mc.maps.length) return;

                StageMap stm = mc.maps[stid];

                if (stm == null) return;

                if (posit >= stm.list.size()) return;

                Stage st = stm.list.get(posit);

                stname.setText(MultiLangCont.STNAME.getCont(st));

                ArrayList<String> stars = new ArrayList<>();

                for (int i = 0; i < stm.stars.length; i++) {
                    String s = (i + 1) + " (" + stm.stars[i] + " %)";
                    stars.add(s);
                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, R.layout.spinneradapter, stars);

                star.setAdapter(arrayAdapter);

                if (selection < stars.size() && selection >= 0)
                    star.setSelection(selection);

                equip.setOnClickListener(new SingleClick() {
                    @Override
                    public void onSingleClick(View v) {
                        Intent intent = new Intent(activity, LineUpScreen.class);
                        activity.startActivityForResult(intent, 0);
                    }
                });

                sniper.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            item += 2;
                        } else {
                            item -= 2;
                        }

                        System.out.println(item);
                    }
                });

                rich.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            item += 1;
                        } else {
                            item -= 1;
                        }

                        System.out.println(item);
                    }
                });

                start.setOnClickListener(new SingleClick() {
                    @Override
                    public void onSingleClick(View v) {
                        Intent intent = new Intent(activity, BattleSimulation.class);
                        intent.putExtra("mapcode", mapcode);
                        intent.putExtra("stid", stid);
                        intent.putExtra("stage", posit);
                        intent.putExtra("star", star.getSelectedItemPosition());
                        intent.putExtra("item", item);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                });

                line.setOnTouchListener((v, event) -> {
                    int[] posit;

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            line.x = event.getX();
                            line.y = event.getY();

                            line.touched = true;
                            line.invalidate();

                            if (!line.drawFloating) {
                                posit = line.getTouchedUnit(event.getX(), event.getY());

                                if (posit != null) {
                                    line.prePosit = posit;
                                }
                            }

                            break;
                        case MotionEvent.ACTION_MOVE:
                            line.x = event.getX();
                            line.y = event.getY();

                            if (!line.drawFloating) {
                                line.floatB = line.getUnitImage(line.prePosit[0], line.prePosit[1]);
                            }

                            line.drawFloating = true;

                            break;
                        case MotionEvent.ACTION_UP:
                            line.CheckChange();

                            int[] deleted = line.getTouchedUnit(event.getX(), event.getY());

                            if (deleted != null) {
                                if (deleted[0] == -100) {
                                    StaticStore.position = new int[]{-1, -1};
                                    StaticStore.updateForm = true;
                                } else {
                                    StaticStore.position = deleted;
                                    StaticStore.updateForm = true;
                                }
                            }

                            line.drawFloating = false;

                            line.touched = false;

                            break;
                    }

                    return true;
                });

                FloatingActionButton bck = activity.findViewById(R.id.battlebck);

                bck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.finish();
                    }
                });

                break;
            default:
                Toast.makeText(activity, results[0], Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        if (activity == null) return;

        LineUpView line = activity.findViewById(R.id.lineupView);
        TextView setname = activity.findViewById(R.id.lineupname);
        Spinner star = activity.findViewById(R.id.battlestar);
        Button equip = activity.findViewById(R.id.battleequip);
        CheckBox sniper = activity.findViewById(R.id.battlesniper);
        CheckBox rich = activity.findViewById(R.id.battlerich);
        Button start = activity.findViewById(R.id.battlestart);
        LinearLayout layout = activity.findViewById(R.id.preparelineup);
        TextView stname = activity.findViewById(R.id.battlestgname);

        ProgressBar prog = activity.findViewById(R.id.prepareprog);
        TextView t = activity.findViewById(R.id.preparet);

        setAppear(line, setname, star, equip, sniper, rich, start, layout, stname);
        setDisappear(prog, t);

        View v = activity.findViewById(R.id.view);

        if (v != null)
            setAppear(v);
    }

    private String getSetLUName() {
        return BasisSet.current.name + " - " + BasisSet.current.sele.name;
    }

    private void setDisappear(View... views) {
        for (View v : views)
            v.setVisibility(View.GONE);
    }

    private void setAppear(View... views) {
        for (View v : views)
            v.setVisibility(View.VISIBLE);
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

    private String withID(int id, String name) {
        String result;
        String names = name;

        if (names == null)
            names = "";

        if (names.equals("")) {
            result = number(id);
        } else {
            result = number(id) + " - " + names;
        }

        return result;
    }
}
