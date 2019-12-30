package com.mandarin.bcu.androidutil.lineup.asynchs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.MeasureViewPager;
import com.mandarin.bcu.androidutil.io.ErrorLogWriter;
import com.mandarin.bcu.androidutil.lineup.LineUpView;
import com.mandarin.bcu.androidutil.lineup.adapters.LUCastleSetting;
import com.mandarin.bcu.androidutil.lineup.adapters.LUCatCombo;
import com.mandarin.bcu.androidutil.lineup.adapters.LUConstruction;
import com.mandarin.bcu.androidutil.lineup.adapters.LUTreasureSetting;
import com.mandarin.bcu.androidutil.lineup.adapters.LUUnitList;
import com.mandarin.bcu.androidutil.lineup.adapters.LUUnitSetting;
import com.mandarin.bcu.androidutil.unit.Definer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import common.battle.BasisSet;
import common.io.InStream;
import common.io.OutStream;
import common.system.MultiLangCont;
import common.system.files.VFile;
import common.util.pack.Pack;

public class LUAdder extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakReference;
    private final FragmentManager manager;

    private int prePosit;
    private boolean TabInitialize = true;

    LUTab tab;

    private final int [] ids = {R.string.lineup_list,R.string.lineup_unit,R.string.lineup_castle,R.string.lineup_treasure,R.string.lineup_construction,R.string.lineup_combo};
    private String [] names = new String[ids.length];

    private ArrayList<Integer> posits = new ArrayList<>();

    public LUAdder(Activity activity, FragmentManager manager) {
        this.weakReference = new WeakReference<>(activity);
        this.manager = manager;

        posits.add(-1);
        posits.add(-1);
    }

    @Override
    protected void onPreExecute() {
        StaticStore.LULoading = true;
        Activity activity = weakReference.get();

        if(activity == null) return;

        TabLayout tabLayout = activity.findViewById(R.id.lineuptab);
        MeasureViewPager measureViewPager = activity.findViewById(R.id.lineuppager);
        LineUpView line = activity.findViewById(R.id.lineupView);
        TableRow row = activity.findViewById(R.id.lineupsetrow);

        for(int i = 0; i < ids.length; i++)
            names[i] = activity.getString(ids[i]);

        setDisappear(tabLayout,measureViewPager,line,row);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        if(activity == null) return null;

        new Definer().define(activity);

        if (StaticStore.LUnames == null) {
            StaticStore.LUnames = new String[StaticStore.unitnumber];

            for (int i = 0; i < StaticStore.LUnames.length; i++) {
                StaticStore.LUnames[i] = withID(i, MultiLangCont.FNAME.getCont(Pack.def.us.ulist.get(i).forms[0]));
            }
        }

        if (StaticStore.bitmaps == null) {
            StaticStore.bitmaps = new Bitmap[StaticStore.unitnumber];


            for (int i = 0; i < StaticStore.unitnumber; i++) {
                String shortPath = "./org/unit/" + number(i) + "/f/uni" + number(i) + "_f00.png";

                Bitmap b = (Bitmap) Objects.requireNonNull(VFile.getFile(shortPath)).getData().getImg().bimg();

                if(b.getWidth() == b.getHeight())
                    StaticStore.bitmaps[i] = StaticStore.getResizeb(b, activity, 48f);
                else
                    StaticStore.bitmaps[i] = StaticStore.MakeIcon(activity,b,48f);
            }
        }

        publishProgress(0);

        if(!StaticStore.LUread) {

            String Path = Environment.getExternalStorageDirectory().getPath() + "/BCU/user/basis.v";

            File f = new File(Path);

            if (f.exists()) {
                byte[] buff = new byte[(int) f.length()];

                try {
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));

                    bis.read(buff, 0, buff.length);
                    bis.close();

                    InStream is = InStream.getIns(buff);

                    try {
                        BasisSet.read(is);
                    } catch (Exception e) {
                        Toast.makeText(activity,R.string.lineup_file_err,Toast.LENGTH_SHORT).show();
                        BasisSet.list.clear();
                        new BasisSet();
                        ErrorLogWriter.WriteLog(e);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            StaticStore.LUread = true;
        }

        StaticStore.sets = BasisSet.list;

        publishProgress(1);

        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onProgressUpdate(Integer... result) {
        Activity activity = weakReference.get();

        if(activity == null) return;


        if(result[0] == 1) {
            ProgressBar prog = activity.findViewById(R.id.lineupprog);
            TextView st = activity.findViewById(R.id.lineupst);
            MeasureViewPager pager = activity.findViewById(R.id.lineuppager);
            TabLayout tabs = activity.findViewById(R.id.lineuptab);
            ImageButton bck = activity.findViewById(R.id.lineupbck);

            setDisappear(prog,st);

            bck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    save();
                    activity.finish();
                }
            });

            LineUpView line = activity.findViewById(R.id.lineupView);

            tab = new LUTab(manager,line,posits);

            line.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int [] posit = new int[2];

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            line.x = event.getX();
                            line.y = event.getY();

                            if(!line.drawFloating) {
                                posit = line.getTouchedUnit(event.getX(), event.getY());

                                if(posit != null) {
                                    line.prePosit = posit;
                                }
                            }

                            break;
                        case MotionEvent.ACTION_MOVE:
                            line.x = event.getX();
                            line.y = event.getY();

                            if(!line.drawFloating) {
                                line.floatB = line.getUnitImage(line.prePosit[0], line.prePosit[1]);
                            }

                            line.drawFloating = true;

                            break;
                        case MotionEvent.ACTION_UP:
                            line.CheckChange();

                            int [] deleted = line.getTouchedUnit(event.getX(),event.getY());

                            if(deleted != null) {
                                if (deleted[0] == -100) {
                                    StaticStore.position = new int[]{-1, -1};
                                    StaticStore.updateForm = true;
                                } else {
                                    StaticStore.position = deleted;
                                    StaticStore.updateForm = true;
                                }
                            }

                            line.drawFloating = false;

                            break;
                    }

                    return  true;
                }
            });

            Spinner setspin = activity.findViewById(R.id.setspin);
            Spinner luspin = activity.findViewById(R.id.luspin);

            List<String> setname = new ArrayList<>();

            for(int i = 0; i < StaticStore.sets.size(); i++)
                setname.add(StaticStore.sets.get(i).name);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,R.layout.spinneradapter,setname);

            setspin.setAdapter(adapter);

            setspin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    BasisSet.current = StaticStore.sets.get(position);

                    List<String> luname = new ArrayList<>();

                    for(int i = 0; i < BasisSet.current.lb.size(); i++) {
                        luname.add(BasisSet.current.lb.get(i).name);
                    }

                    ArrayAdapter<String> adapter1 = new ArrayAdapter<>(activity,R.layout.spinneradapter,luname);

                    luspin.setAdapter(adapter1);

                    posits.clear();
                    posits.add(-1);
                    posits.add(-1);

                    tab = new LUTab(manager,line,posits);
                    StaticStore.updateForm = true;

                    pager.setAdapter(tab);
                    pager.setOffscreenPageLimit(5);
                    pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
                    tabs.setupWithViewPager(pager);

                    Objects.requireNonNull(tabs.getTabAt(prePosit)).select();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            setspin.setSelection(0);

            List<String> LUname = new ArrayList<>();

            for(int i = 0; i < BasisSet.current.lb.size(); i++) {
                LUname.add(BasisSet.current.lb.get(i).name);
            }

            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(activity,R.layout.spinneradapter,LUname);

            luspin.setAdapter(adapter1);

            luspin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    BasisSet.current.sele = BasisSet.current.lb.get(position);

                    line.ChangeFroms(BasisSet.current.sele.lu);

                    posits.clear();
                    posits.add(-1);
                    posits.add(-1);

                    tab = new LUTab(manager,line,posits);
                    StaticStore.updateForm = true;

                    pager.setAdapter(tab);
                    pager.setOffscreenPageLimit(5);
                    pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
                    tabs.setupWithViewPager(pager);

                    Objects.requireNonNull(tabs.getTabAt(prePosit)).select();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            ImageButton option = activity.findViewById(R.id.lineupsetting);

            PopupMenu popupMenu = new PopupMenu(activity,option);
            Menu menu = popupMenu.getMenu();
            popupMenu.getMenuInflater().inflate(R.menu.lineup_menu,menu);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.lineup_create_set:
                            Dialog dialog = new Dialog(activity);

                            dialog.setContentView(R.layout.create_setlu_dialog);

                            EditText edit = dialog.findViewById(R.id.setluedit);
                            Button setdone = dialog.findViewById(R.id.setludone);
                            Button cancel = dialog.findViewById(R.id.setlucancel);

                            edit.setHint("set "+BasisSet.list.size());

                            setdone.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(edit.getText().toString().isEmpty())
                                        new BasisSet();
                                    else {
                                        new BasisSet();
                                        BasisSet.list.get(BasisSet.list.size()-1).name = edit.getText().toString();
                                    }

                                    List<String> names = new ArrayList<>();

                                    for(int i = 0; i < BasisSet.list.size(); i++)
                                        names.add(BasisSet.list.get(i).name);

                                    ArrayAdapter<String> adapter2 = new ArrayAdapter<>(activity,R.layout.spinneradapter,names);

                                    setspin.setAdapter(adapter2);

                                    setspin.setSelection(setspin.getCount()-1);

                                    LUTab tab = new LUTab(manager,line);
                                    StaticStore.updateForm = true;

                                    pager.setAdapter(tab);
                                    pager.setOffscreenPageLimit(5);
                                    pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
                                    tabs.setupWithViewPager(pager);

                                    Objects.requireNonNull(tabs.getTabAt(prePosit)).select();

                                    save();

                                    dialog.dismiss();
                                }
                            });

                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            dialog.show();

                            return true;
                        case R.id.lineup_create_lineup:
                            dialog = new Dialog(activity);

                            dialog.setContentView(R.layout.create_setlu_dialog);

                            edit = dialog.findViewById(R.id.setluedit);
                            setdone = dialog.findViewById(R.id.setludone);
                            cancel = dialog.findViewById(R.id.setlucancel);

                            edit.setHint("lineup "+BasisSet.current.lb.size());

                            setdone.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(edit.getText().toString().isEmpty())
                                        BasisSet.current.add();
                                    else {
                                        BasisSet.current.add();
                                        BasisSet.current.lb.get(BasisSet.current.lb.size()-1).name = edit.getText().toString();
                                    }

                                    List<String> names = new ArrayList<>();

                                    for(int i = 0; i < BasisSet.current.lb.size(); i++)
                                        names.add(BasisSet.current.lb.get(i).name);

                                    ArrayAdapter<String> adapter2 = new ArrayAdapter<>(activity,R.layout.spinneradapter,names);

                                    luspin.setAdapter(adapter2);

                                    luspin.setSelection(luspin.getCount()-1);

                                    LUTab tab = new LUTab(manager,line);
                                    StaticStore.updateForm = true;

                                    pager.setAdapter(tab);
                                    pager.setOffscreenPageLimit(5);
                                    pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
                                    tabs.setupWithViewPager(pager);

                                    Objects.requireNonNull(tabs.getTabAt(prePosit)).select();

                                    save();

                                    dialog.dismiss();
                                }
                            });

                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            dialog.show();

                            return true;
                    }

                    return false;
                }
            });

            option.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMenu.show();
                }
            });

            pager.setAdapter(tab);
            pager.setOffscreenPageLimit(5);
            pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
            tabs.setupWithViewPager(pager);

            tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    pager.setCurrentItem(tab.getPosition());
                    StaticStore.LUtabPosition = tab.getPosition();
                    prePosit = tab.getPosition();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            Objects.requireNonNull(tabs.getTabAt(StaticStore.LUtabPosition)).select();

        } else if(result[0] == 0) {
            TextView st =activity.findViewById(R.id.lineupst);

            st.setText(R.string.lineup_reading);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        TabLayout tabLayout = activity.findViewById(R.id.lineuptab);
        MeasureViewPager measureViewPager = activity.findViewById(R.id.lineuppager);
        LineUpView line = activity.findViewById(R.id.lineupView);
        TableRow row = activity.findViewById(R.id.lineupsetrow);

        setAppear(tabLayout,measureViewPager,line,row);
    }

    private void setDisappear(View... view) {
        for(View v : view)
            v.setVisibility(View.GONE);
    }

    private void setAppear(View... view) {
        for(View v : view)
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

        if(names == null)
            names = "";

        if(names.equals("")) {
            result = number(id);
        } else {
            result = number(id)+" - "+names;
        }

        return result;
    }

    private class LUTab extends FragmentStatePagerAdapter {
        private LineUpView lineup;

        LUTab(FragmentManager fm, LineUpView line) {
            super(fm);
            this.lineup = line;
        }

        LUTab(FragmentManager fm, LineUpView line, ArrayList<Integer> posit) {
            super(fm);
            this.lineup = line;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return LUUnitList.newInstance(StaticStore.LUnames, lineup);
                case 1:
                    return LUUnitSetting.newInstance(lineup);
                case 2:
                    return LUCastleSetting.newInstance();
                case 3:
                    return LUTreasureSetting.newInstance();
                case 4:
                    return LUConstruction.newInstance();
                case 5:
                    return LUCatCombo.newInstance(lineup);
            }

            return null;
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return names[position];
        }
    }

    private void save() {
        String Path = Environment.getExternalStorageDirectory().getPath()+"/BCU/user/basis.v";
        String Direct = Environment.getExternalStorageDirectory().getPath()+"/BCU/user/";

        File g = new File(Direct);

        if(!g.exists())
            g.mkdirs();

        File f = new File(Path);

        try {
            if(!f.exists())
                f.createNewFile();

            OutputStream os = new FileOutputStream(f);

            OutStream out = BasisSet.writeAll();

            out.flush(os);

            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
