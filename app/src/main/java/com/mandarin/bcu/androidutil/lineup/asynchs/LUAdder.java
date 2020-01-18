package com.mandarin.bcu.androidutil.lineup.asynchs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mandarin.bcu.R;
import com.mandarin.bcu.SearchFilter;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import common.battle.BasisLU;
import common.battle.BasisSet;
import common.io.InStream;
import common.io.OutStream;
import common.system.MultiLangCont;
import common.util.pack.Pack;

public class LUAdder extends AsyncTask<Void, Integer, Void> {
    private final WeakReference<Activity> weakReference;
    private final FragmentManager manager;

    private int prePosit;
    private boolean initialized = false;

    private LUTab tab;

    private final int[] ids = {R.string.lineup_list, R.string.lineup_unit, R.string.lineup_castle, R.string.lineup_treasure, R.string.lineup_construction, R.string.lineup_combo};
    private String[] names = new String[ids.length];

    public LUAdder(Activity activity, FragmentManager manager) {
        this.weakReference = new WeakReference<>(activity);
        this.manager = manager;
    }

    @Override
    protected void onPreExecute() {
        StaticStore.LULoading = true;
        Activity activity = weakReference.get();

        if (activity == null) return;

        TabLayout tabLayout = activity.findViewById(R.id.lineuptab);
        MeasureViewPager measureViewPager = activity.findViewById(R.id.lineuppager);
        LineUpView line = activity.findViewById(R.id.lineupView);
        TableRow row = activity.findViewById(R.id.lineupsetrow);
        TextInputEditText schname = activity.findViewById(R.id.animschname);
        TextInputLayout layout = activity.findViewById(R.id.animschnamel);

        View view = activity.findViewById(R.id.view);

        for (int i = 0; i < ids.length; i++)
            names[i] = activity.getString(ids[i]);

        if (view == null)
            setDisappear(tabLayout, measureViewPager, line, row, schname, layout);
        else
            setDisappear(tabLayout, measureViewPager, line, row, view, schname, layout);
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
                            ErrorLogWriter.WriteLog(e);
                        }
                    } catch (Exception e) {
                        ErrorLogWriter.WriteLog(e);
                    }
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

        if (activity == null) return;


        if (result[0] == 1) {

            SharedPreferences shared = activity.getSharedPreferences("configuration",Context.MODE_PRIVATE);

            int setn = shared.getInt("equip_set",0);
            int lun = shared.getInt("equip_lu",0);

            ProgressBar prog = activity.findViewById(R.id.lineupprog);
            TextView st = activity.findViewById(R.id.lineupst);
            MeasureViewPager pager = activity.findViewById(R.id.lineuppager);
            TabLayout tabs = activity.findViewById(R.id.lineuptab);
            FloatingActionButton bck = activity.findViewById(R.id.lineupbck);
            FloatingActionButton sch = activity.findViewById(R.id.linesch);
            LineUpView line = activity.findViewById(R.id.lineupView);
            FloatingActionButton option = activity.findViewById(R.id.lineupsetting);

            PopupMenu popupMenu = new PopupMenu(activity, option);
            Menu menu = popupMenu.getMenu();
            popupMenu.getMenuInflater().inflate(R.menu.lineup_menu, menu);

            setDisappear(prog, st);

            bck.setOnClickListener(v -> {
                save();
                StaticStore.setline[0] = 0;
                StaticStore.setline[1] = 0;
                StaticStore.filterReset();
                StaticStore.set = null;
                StaticStore.lu = null;
                StaticStore.combos.clear();
                StaticStore.lineunitname = null;
                activity.finish();
            });

            sch.setOnClickListener(v -> {
                Intent intent = new Intent(activity, SearchFilter.class);
                activity.startActivity(intent);
            });

            tab = new LUTab(manager, line);

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

            Spinner setspin = activity.findViewById(R.id.setspin);
            Spinner luspin = activity.findViewById(R.id.luspin);

            List<String> setname = new ArrayList<>();

            for (int i = 0; i < StaticStore.sets.size(); i++)
                setname.add(StaticStore.sets.get(i).name);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.spinneradapter, setname);

            setspin.setAdapter(adapter);

            setspin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!initialized) return;

                    BasisSet.current = StaticStore.sets.get(position);

                    SharedPreferences preferences = activity.getSharedPreferences("configuration", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();

                    editor.putInt("equip_set",position);

                    editor.apply();

                    StaticStore.setline[0] = position;
                    StaticStore.setline[1] = 0;

                    List<String> luname = new ArrayList<>();

                    for (int i = 0; i < BasisSet.current.lb.size(); i++) {
                        luname.add(BasisSet.current.lb.get(i).name);
                    }

                    ArrayAdapter<String> adapter1 = new ArrayAdapter<>(activity, R.layout.spinneradapter, luname);

                    luspin.setAdapter(adapter1);

                    StaticStore.updateForm = true;
                    StaticStore.updateTreasure = true;
                    StaticStore.updateConst = true;
                    StaticStore.updateCastle = true;

                    if (position == 0) {
                        menu.getItem(5).getSubMenu().getItem(0).setEnabled(false);
                        menu.getItem(3).getSubMenu().getItem(0).setEnabled(false);
                    } else {
                        menu.getItem(5).getSubMenu().getItem(0).setEnabled(true);
                        menu.getItem(3).getSubMenu().getItem(0).setEnabled(true);
                    }

                    if (!menu.getItem(5).getSubMenu().getItem(0).isEnabled() && !menu.getItem(5).getSubMenu().getItem(1).isEnabled()) {
                        menu.getItem(5).setEnabled(false);
                    } else {
                        menu.getItem(5).setEnabled(true);
                    }

                    line.invalidate();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            List<String> LUname = new ArrayList<>();

            for (int i = 0; i < BasisSet.current.lb.size(); i++) {
                LUname.add(BasisSet.current.lb.get(i).name);
            }

            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(activity, R.layout.spinneradapter, LUname);

            luspin.setAdapter(adapter1);

            luspin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!initialized) return;

                    BasisSet.current.sele = BasisSet.current.lb.get(position);

                    SharedPreferences preferences = activity.getSharedPreferences("configuration",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();

                    editor.putInt("equip_lu",position);
                    editor.apply();

                    StaticStore.setline[1] = position;

                    line.ChangeFroms(BasisSet.current.sele.lu);

                    StaticStore.updateForm = true;

                    if (BasisSet.current.lb.size() == 1) {
                        menu.getItem(5).getSubMenu().getItem(1).setEnabled(false);
                    } else {
                        menu.getItem(5).getSubMenu().getItem(1).setEnabled(true);
                    }

                    if (!menu.getItem(5).getSubMenu().getItem(0).isEnabled() && !menu.getItem(5).getSubMenu().getItem(1).isEnabled()) {
                        menu.getItem(5).setEnabled(false);
                    } else {
                        menu.getItem(5).setEnabled(true);
                    }

                    line.invalidate();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (StaticStore.set == null && StaticStore.lu == null) {
                menu.getItem(2).setEnabled(false);
                menu.getItem(2).getSubMenu().getItem(0).setEnabled(false);
                menu.getItem(2).getSubMenu().getItem(1).setEnabled(false);
            }

            TextInputEditText schname = activity.findViewById(R.id.animschname);

            schname.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    StaticStore.lineunitname = s.toString();
                    StaticStore.updateList = true;
                }
            });

            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.lineup_create_set:
                        Dialog dialog = new Dialog(activity);

                        dialog.setContentView(R.layout.create_setlu_dialog);

                        EditText edit = dialog.findViewById(R.id.setluedit);
                        Button setdone = dialog.findViewById(R.id.setludone);
                        Button cancel = dialog.findViewById(R.id.setlucancel);

                        edit.setHint("set " + BasisSet.list.size());

                        int[] rgb = StaticStore.getRGB(StaticStore.getAttributeColor(activity, R.attr.TextPrimary));

                        edit.setHintTextColor(Color.argb(255 / 2, rgb[0], rgb[1], rgb[2]));

                        setdone.setOnClickListener(v -> {
                            if (edit.getText().toString().isEmpty())
                                new BasisSet();
                            else {
                                new BasisSet();
                                BasisSet.list.get(BasisSet.list.size() - 1).name = edit.getText().toString();
                            }

                            List<String> names = new ArrayList<>();

                            for (int i = 0; i < BasisSet.list.size(); i++)
                                names.add(BasisSet.list.get(i).name);

                            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(activity, R.layout.spinneradapter, names);

                            setspin.setAdapter(adapter2);

                            setspin.setSelection(setspin.getCount() - 1);

                            LUTab tab = new LUTab(manager, line);
                            StaticStore.updateForm = true;

                            pager.setAdapter(tab);
                            pager.setOffscreenPageLimit(5);
                            pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
                            tabs.setupWithViewPager(pager);

                            Objects.requireNonNull(tabs.getTabAt(prePosit)).select();

                            save();

                            dialog.dismiss();
                        });

                        cancel.setOnClickListener(v -> dialog.dismiss());

                        dialog.show();

                        return true;
                    case R.id.lineup_create_lineup:
                        dialog = new Dialog(activity);

                        dialog.setContentView(R.layout.create_setlu_dialog);

                        edit = dialog.findViewById(R.id.setluedit);
                        setdone = dialog.findViewById(R.id.setludone);
                        cancel = dialog.findViewById(R.id.setlucancel);

                        edit.setHint("lineup " + BasisSet.current.lb.size());

                        rgb = StaticStore.getRGB(StaticStore.getAttributeColor(activity, R.attr.TextPrimary));

                        edit.setHintTextColor(Color.argb(255 / 2, rgb[0], rgb[1], rgb[2]));

                        setdone.setOnClickListener(v -> {
                            if (edit.getText().toString().isEmpty())
                                BasisSet.current.add();
                            else {
                                BasisSet.current.add();
                                BasisSet.current.lb.get(BasisSet.current.lb.size() - 1).name = edit.getText().toString();
                            }

                            List<String> names = new ArrayList<>();

                            for (int i = 0; i < BasisSet.current.lb.size(); i++)
                                names.add(BasisSet.current.lb.get(i).name);

                            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(activity, R.layout.spinneradapter, names);

                            luspin.setAdapter(adapter2);

                            luspin.setSelection(luspin.getCount() - 1);

                            LUTab tab = new LUTab(manager, line);
                            StaticStore.updateForm = true;

                            pager.setAdapter(tab);
                            pager.setOffscreenPageLimit(5);
                            pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
                            tabs.setupWithViewPager(pager);

                            Objects.requireNonNull(tabs.getTabAt(prePosit)).select();

                            save();

                            dialog.dismiss();
                        });

                        cancel.setOnClickListener(v -> dialog.dismiss());

                        dialog.show();

                        return true;
                    case R.id.lineup_copy_set:
                        StaticStore.set = BasisSet.current.copy();
                        BasisSet.list.remove(BasisSet.list.size() - 1);

                        Toast.makeText(activity, R.string.lineup_set_copied, Toast.LENGTH_SHORT).show();
                        menu.getItem(2).setEnabled(true);
                        menu.getItem(2).getSubMenu().getItem(0).setEnabled(true);

                        return true;
                    case R.id.lineup_copy_lineup:
                        StaticStore.lu = BasisSet.current.sele.copy();

                        Toast.makeText(activity, R.string.lineup_lu_copied, Toast.LENGTH_SHORT).show();
                        menu.getItem(2).setEnabled(true);
                        menu.getItem(2).getSubMenu().getItem(1).setEnabled(true);

                        return true;
                    case R.id.lineup_paste_set:
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(R.string.lineup_pasting_set);
                        builder.setMessage(R.string.lineup_paste_set_msg);
                        builder.setPositiveButton(R.string.main_file_ok, (dialog1, which) -> {
                            String name = BasisSet.current.name;

                            BasisSet.list.remove(setspin.getSelectedItemPosition());
                            BasisSet.list.add(setspin.getSelectedItemPosition(), StaticStore.set);

                            BasisSet.current = BasisSet.list.get(setspin.getSelectedItemPosition());

                            BasisSet.current.name = name;

                            line.UpdateLineUp();

                            List<String> LUname1 = new ArrayList<>();

                            for (int i = 0; i < BasisSet.current.lb.size(); i++) {
                                LUname1.add(BasisSet.current.lb.get(i).name);
                            }

                            ArrayAdapter<String> adapter11 = new ArrayAdapter<>(activity, R.layout.spinneradapter, LUname1);

                            luspin.setAdapter(adapter11);

                            Toast.makeText(activity, R.string.lineup_paste_set_done, Toast.LENGTH_SHORT).show();
                        });

                        builder.setNegativeButton(R.string.main_file_cancel, (dialog12, which) -> {

                        });

                        builder.show();

                        return true;
                    case R.id.lineup_paste_lineup:
                        builder = new AlertDialog.Builder(activity);
                        builder.setTitle(R.string.lineup_pasting_lu);
                        builder.setMessage(R.string.lineup_paste_lu_msg);
                        builder.setPositiveButton(R.string.main_file_ok, (dialog13, which) -> {
                            String name = BasisSet.current.sele.name;

                            BasisSet.current.lb.remove(luspin.getSelectedItemPosition());
                            BasisSet.current.lb.add(luspin.getSelectedItemPosition(), StaticStore.lu);
                            BasisSet.current.sele = BasisSet.current.lb.get(luspin.getSelectedItemPosition());

                            BasisSet.current.sele.name = name;

                            line.UpdateLineUp();

                            Toast.makeText(activity, R.string.lineup_paste_lu_done, Toast.LENGTH_SHORT).show();
                        });

                        builder.setNegativeButton(R.string.main_file_cancel, (dialog14, which) -> {

                        });

                        builder.show();

                        return true;
                    case R.id.lineup_rename_set:
                        dialog = new Dialog(activity);

                        dialog.setContentView(R.layout.create_setlu_dialog);

                        edit = dialog.findViewById(R.id.setluedit);
                        setdone = dialog.findViewById(R.id.setludone);
                        cancel = dialog.findViewById(R.id.setlucancel);
                        TextView setluname = dialog.findViewById(R.id.setluname);

                        setluname.setText(R.string.lineup_renaming_set);

                        edit.setHint(BasisSet.current.name);

                        rgb = StaticStore.getRGB(StaticStore.getAttributeColor(activity, R.attr.TextPrimary));

                        edit.setHintTextColor(Color.argb(255 / 2, rgb[0], rgb[1], rgb[2]));

                        setdone.setOnClickListener(v -> {
                            if (!edit.getText().toString().isEmpty()) {
                                BasisSet.current.name = edit.getText().toString();

                                List<String> setname1 = new ArrayList<>();

                                for (int i = 0; i < StaticStore.sets.size(); i++)
                                    setname1.add(StaticStore.sets.get(i).name);

                                ArrayAdapter<String> adapter22 = new ArrayAdapter<>(activity, R.layout.spinneradapter, setname1);

                                int pos = setspin.getSelectedItemPosition();

                                setspin.setAdapter(adapter22);

                                setspin.setSelection(pos);

                                save();
                            }

                            dialog.dismiss();
                        });

                        cancel.setOnClickListener(v -> dialog.dismiss());

                        dialog.show();

                        return true;
                    case R.id.lineup_rename_lineup:
                        dialog = new Dialog(activity);

                        dialog.setContentView(R.layout.create_setlu_dialog);

                        edit = dialog.findViewById(R.id.setluedit);
                        setdone = dialog.findViewById(R.id.setludone);
                        cancel = dialog.findViewById(R.id.setlucancel);
                        setluname = dialog.findViewById(R.id.setluname);

                        setluname.setText(R.string.lineup_renaming_lu);

                        edit.setHint(BasisSet.current.sele.name);

                        rgb = StaticStore.getRGB(StaticStore.getAttributeColor(activity, R.attr.TextPrimary));

                        edit.setHintTextColor(Color.argb(255 / 2, rgb[0], rgb[1], rgb[2]));

                        setdone.setOnClickListener(v -> {
                            if (!edit.getText().toString().isEmpty()) {
                                BasisSet.current.sele.name = edit.getText().toString();

                                List<String> LUname1 = new ArrayList<>();

                                for (int i = 0; i < BasisSet.current.lb.size(); i++) {
                                    LUname1.add(BasisSet.current.lb.get(i).name);
                                }

                                ArrayAdapter<String> adapter11 = new ArrayAdapter<>(activity, R.layout.spinneradapter, LUname1);

                                luspin.setAdapter(adapter11);

                                luspin.setSelection(BasisSet.current.lb.size() - 1);

                                save();
                            }

                            dialog.dismiss();
                        });

                        cancel.setOnClickListener(v -> dialog.dismiss());

                        dialog.show();

                        return true;
                    case R.id.lineup_clone_set:
                        String origin = BasisSet.current.name;

                        BasisSet.current.copy();

                        BasisSet.list.get(BasisSet.list.size() - 1).name = origin + "`";

                        List<String> setname1 = new ArrayList<>();

                        for (int i = 0; i < StaticStore.sets.size(); i++)
                            setname1.add(StaticStore.sets.get(i).name);

                        ArrayAdapter<String> adapter22 = new ArrayAdapter<>(activity, R.layout.spinneradapter, setname1);

                        setspin.setAdapter(adapter22);

                        setspin.setSelection(BasisSet.list.size() - 1);

                        save();

                        Toast.makeText(activity, R.string.lineup_cloned_set, Toast.LENGTH_SHORT).show();

                        return true;
                    case R.id.lineup_clone_lineup:
                        BasisLU lu = BasisSet.current.sele.copy();

                        lu.name = BasisSet.current.sele.name + "`";

                        BasisSet.current.lb.add(lu);

                        List<String> LUname1 = new ArrayList<>();

                        for (int i = 0; i < BasisSet.current.lb.size(); i++) {
                            LUname1.add(BasisSet.current.lb.get(i).name);
                        }

                        ArrayAdapter<String> adapter11 = new ArrayAdapter<>(activity, R.layout.spinneradapter, LUname1);

                        luspin.setAdapter(adapter11);

                        luspin.setSelection(BasisSet.current.lb.size() - 1);

                        save();

                        Toast.makeText(activity, R.string.lineup_cloned_lineup, Toast.LENGTH_SHORT).show();

                        return true;
                    case R.id.lineup_remove_set:
                        builder = new AlertDialog.Builder(activity);
                        builder.setTitle(R.string.lineup_removing_set);
                        builder.setMessage(R.string.lineup_remove_set_msg);
                        builder.setPositiveButton(R.string.main_file_ok, (dialog15, which) -> {
                            BasisSet.list.remove(setspin.getSelectedItemPosition());

                            int pos = setspin.getSelectedItemPosition();

                            List<String> setname2 = new ArrayList<>();

                            for (int i = 0; i < StaticStore.sets.size(); i++)
                                setname2.add(StaticStore.sets.get(i).name);

                            ArrayAdapter<String> adapter23 = new ArrayAdapter<>(activity, R.layout.spinneradapter, setname2);

                            setspin.setAdapter(adapter23);

                            if (pos >= BasisSet.list.size())
                                setspin.setSelection(BasisSet.list.size() - 1);
                            else
                                setspin.setSelection(pos);
                        });

                        builder.setNegativeButton(R.string.main_file_cancel, (dialog16, which) -> {

                        });

                        builder.show();

                        return true;
                    case R.id.lineup_remove_lineup:
                        builder = new AlertDialog.Builder(activity);
                        builder.setTitle(R.string.lineup_removing_lu);
                        builder.setMessage(R.string.lineup_remove_lu_msg);
                        builder.setPositiveButton(R.string.main_file_ok, (dialog17, which) -> {
                            BasisSet.current.lb.remove(luspin.getSelectedItemPosition());

                            int pos = luspin.getSelectedItemPosition();

                            List<String> LUname2 = new ArrayList<>();

                            for (int i = 0; i < BasisSet.current.lb.size(); i++) {
                                LUname2.add(BasisSet.current.lb.get(i).name);
                            }

                            ArrayAdapter<String> adapter12 = new ArrayAdapter<>(activity, R.layout.spinneradapter, LUname2);

                            luspin.setAdapter(adapter12);

                            if (pos >= BasisSet.current.lb.size())
                                luspin.setSelection(BasisSet.current.lb.size() - 1);
                            else
                                luspin.setSelection(pos);
                        });

                        builder.setNegativeButton(R.string.main_file_cancel, (dialog18, which) -> {

                        });

                        builder.show();

                        return true;
                }

                return false;
            });

            option.setOnClickListener(v -> popupMenu.show());

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

            initialized = true;

            setspin.setSelection(setn);
            luspin.setSelection(lun);


        } else if (result[0] == 0) {
            TextView st = activity.findViewById(R.id.lineupst);

            st.setText(R.string.lineup_reading);
        } else {
            Toast.makeText(activity, result[0], Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        if (activity == null) return;

        TabLayout tabLayout = activity.findViewById(R.id.lineuptab);
        MeasureViewPager measureViewPager = activity.findViewById(R.id.lineuppager);
        LineUpView line = activity.findViewById(R.id.lineupView);
        TableRow row = activity.findViewById(R.id.lineupsetrow);
        TextInputEditText schname = activity.findViewById(R.id.animschname);
        TextInputLayout layout = activity.findViewById(R.id.animschnamel);

        View view = activity.findViewById(R.id.view);

        if (view == null)
            setAppear(tabLayout, measureViewPager, line, row, schname, layout);
        else
            setAppear(tabLayout, measureViewPager, line, row, view, schname, layout);
    }

    private void setDisappear(View... view) {
        for (View v : view)
            v.setVisibility(View.GONE);
    }

    private void setAppear(View... view) {
        for (View v : view)
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

    private class LUTab extends FragmentStatePagerAdapter {
        private LineUpView lineup;

        LUTab(FragmentManager fm, LineUpView line) {
            super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.lineup = line;
        }

        @NonNull
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

            return new LUUnitSetting();
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return names[position];
        }
    }

    private void save() {
        String Path = Environment.getExternalStorageDirectory().getPath() + "/BCU/user/basis.v";
        String Direct = Environment.getExternalStorageDirectory().getPath() + "/BCU/user/";

        File g = new File(Direct);

        if (!g.exists())
            g.mkdirs();

        File f = new File(Path);

        try {
            if (!f.exists())
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
