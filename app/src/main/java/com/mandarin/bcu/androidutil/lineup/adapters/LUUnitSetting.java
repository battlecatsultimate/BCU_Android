package com.mandarin.bcu.androidutil.lineup.adapters;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.R;
import com.mandarin.bcu.UnitInfo;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.getStrings;
import com.mandarin.bcu.androidutil.lineup.LineUpView;

import java.util.ArrayList;
import java.util.List;

import common.battle.BasisSet;
import common.util.unit.Form;

public class LUUnitSetting extends Fragment {
    View view;
    LineUpView line;
    private int [] pcoin;
    private int [] zeros = {0, 0, 0, 0, 0, 0};

    private int fid = 0;

    Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(StaticStore.updateForm) {
                Update();
                StaticStore.updateForm = false;
            }

            handler.postDelayed(this,50);
        }
    };

    public Form f;

    public static LUUnitSetting newInstance(LineUpView line) {
        LUUnitSetting unitSetting = new LUUnitSetting();
        unitSetting.setVariable(line);

        return unitSetting;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup group, @Nullable Bundle bundle) {
        view = inflater.inflate(R.layout.lineup_unit_set,group,false);

        handler.postDelayed(runnable,50);

        return view;
    }

    private void Update() {
        Spinner[] spinners = {view.findViewById(R.id.lineuplevspin),view.findViewById(R.id.lineuplevpspin)};
        TextView plus = view.findViewById(R.id.lineuplevplus);
        TableRow row = view.findViewById(R.id.lineupunittable);
        TableRow tal = view.findViewById(R.id.lineuppcoin);
        CheckBox t = view.findViewById(R.id.lineuptalent);
        TextView hp = view.findViewById(R.id.lineupunithp);
        TextView atk = view.findViewById(R.id.lineupunitatk);
        Button chform = view.findViewById(R.id.lineupchform);
        TextView levt = view.findViewById(R.id.lineupunitlevt);


        if(StaticStore.position[0] == -1)
            f = null;
        else if(StaticStore.position[0] == 100)
            f = line.repform;
        else {
            if(StaticStore.position[0]*5+StaticStore.position[1] >= StaticStore.currentForms.size())
                f = null;
            else
                f= StaticStore.currentForms.get(StaticStore.position[0]*5+StaticStore.position[1]);
        }

        if(f == null) {
            setDisappear(spinners[0],spinners[1],plus,row,t,tal,chform,levt);
        } else {
            if(getContext() == null) return;

            setAppear(spinners[0],spinners[1],plus,row,t,tal,chform,levt);

            getStrings s = new getStrings(getContext());

            fid = f.fid;

            if(f.unit.maxp == 0)
                setDisappear(spinners[1],plus);

            int [] id = {R.id.lineupp,R.id.lineupp1,R.id.lineupp2,R.id.lineupp3,R.id.lineupp4};

            Spinner [] talents = new Spinner[id.length];

            for(int i = 0; i < id.length; i++) {
                talents[i] = view.findViewById(id[i]);
            }

            if(f.getPCoin() != null) {
                pcoin = BasisSet.current.sele.lu.getLv(f.unit);

                int [] max = f.getPCoin().max;

                for(int i = 1; i < max.length; i++) {
                    final int ii = i-1;

                    List<Integer> list = new ArrayList<>();

                    for(int j = 0; j < max[i]+1; j++)
                        list.add(j);

                    ArrayAdapter<Integer> adapter = new ArrayAdapter<>(getContext(),R.layout.spinneradapter,list);

                    talents[i-1].setAdapter(adapter);

                    talents[i-1].setSelection(getIndex(talents[i-1],pcoin[i]));

                    talents[i-1].setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            talents[ii].setClickable(false);
                            Toast.makeText(getContext(),s.getTalentName(ii,f),Toast.LENGTH_SHORT).show();

                            return true;
                        }
                    });
                }
            } else {
                pcoin = new int[] {0, 0, 0, 0, 0, 0};
                setDisappear(t,tal);
            }

            ArrayList<Integer> levs = new ArrayList<>();
            ArrayList<Integer> levp = new ArrayList<>();

            for(int i = 1; i < f.unit.max+1; i++)
                levs.add(i);

            for(int i = 0; i < f.unit.maxp+1; i++)
                levp.add(i);

            ArrayAdapter<Integer> adapter = new ArrayAdapter<>(getContext(),R.layout.spinneradapter,levs);
            ArrayAdapter<Integer> adapter1 = new ArrayAdapter<>(getContext(),R.layout.spinneradapter,levp);

            spinners[0].setAdapter(adapter);
            spinners[1].setAdapter(adapter1);

            int loadlev = BasisSet.current.sele.lu.getLv(f.unit)[0];

            int loadlevp = 0;

            if(loadlev > f.unit.max) {
                loadlevp = loadlev - f.unit.max;
                loadlev = f.unit.max;
            }

            final int floadlev = loadlev;
            final int floadlevp = loadlevp;

            spinners[0].setSelection(getIndex(spinners[0],floadlev));
            spinners[1].setSelection(getIndex(spinners[1],floadlevp));

            spinners[0].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int lev = (int)spinners[0].getSelectedItem();
                    int levp = (int)spinners[1].getSelectedItem();

                    int [] levs;

                    if(t.isChecked()) {
                        levs = new int[]{lev + levp, pcoin[1], pcoin[2], pcoin[3], pcoin[4], pcoin[5]};
                        hp.setText(s.getHP(f,BasisSet.current.t(),lev+levp,f.getPCoin()!=null&&t.isChecked(),pcoin));
                        atk.setText(s.getAtk(f,BasisSet.current.t(),lev+levp,f.getPCoin()!=null&&t.isChecked(),pcoin));
                    } else {
                        levs = new int[]{lev + levp, 0,0,0,0,0};
                        hp.setText(s.getHP(f,BasisSet.current.t(),lev+levp,f.getPCoin()!=null&&t.isChecked(),zeros));
                        atk.setText(s.getAtk(f,BasisSet.current.t(),lev+levp,f.getPCoin()!=null&&t.isChecked(),zeros));
                    }

                    BasisSet.current.sele.lu.setLv(f.unit,levs);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinners[1].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int lev = (int)spinners[0].getSelectedItem();
                    int levp = (int)spinners[1].getSelectedItem();

                    int [] levs;

                    if(t.isChecked()) {
                        levs = new int[]{lev + levp, pcoin[1], pcoin[2], pcoin[3], pcoin[4], pcoin[5]};
                        hp.setText(s.getHP(f,BasisSet.current.t(),lev+levp,f.getPCoin()!=null&&t.isChecked(),pcoin));
                        atk.setText(s.getAtk(f,BasisSet.current.t(),lev+levp,f.getPCoin()!=null&&t.isChecked(),pcoin));
                    } else {
                        levs = new int[]{lev + levp, 0,0,0,0,0};
                        hp.setText(s.getHP(f,BasisSet.current.t(),lev+levp,f.getPCoin()!=null&&t.isChecked(),zeros));
                        atk.setText(s.getAtk(f,BasisSet.current.t(),lev+levp,f.getPCoin()!=null&&t.isChecked(),zeros));
                    }

                    BasisSet.current.sele.lu.setLv(f.unit,levs);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            for(int i = 0; i < talents.length; i++) {
                final int ii = i;

                talents[i].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        pcoin[ii+1] = position;

                        int lev = (int)spinners[0].getSelectedItem();
                        int levp = (int)spinners[1].getSelectedItem();

                        int [] levs;

                        if(t.isChecked()) {
                            levs = new int[]{lev + levp, pcoin[1], pcoin[2], pcoin[3], pcoin[4], pcoin[5]};
                            hp.setText(s.getHP(f,BasisSet.current.t(),lev+levp,f.getPCoin()!=null&&t.isChecked(),pcoin));
                            atk.setText(s.getAtk(f,BasisSet.current.t(),lev+levp,f.getPCoin()!=null&&t.isChecked(),pcoin));
                        } else {
                            levs = new int[]{lev + levp, zeros[1], zeros[2], zeros[3], zeros[4], zeros[5]};
                            hp.setText(s.getHP(f,BasisSet.current.t(),lev+levp,f.getPCoin()!=null&&t.isChecked(),zeros));
                            atk.setText(s.getAtk(f,BasisSet.current.t(),lev+levp,f.getPCoin()!=null&&t.isChecked(),zeros));
                        }

                        BasisSet.current.sele.lu.setLv(f.unit,levs);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                ImageButton info = view.findViewById(R.id.lineupunitinfo);

                info.setOnClickListener(new SingleClick() {
                    @Override
                    public void onSingleClick(View v) {
                        Intent intent = new Intent(getContext(), UnitInfo.class);
                        intent.putExtra("ID",f.unit.id);
                        getContext().startActivity(intent);
                    }
                });

                hp.setText(s.getHP(f,BasisSet.current.t(),loadlev+loadlevp,f.getPCoin()!=null&&t.isChecked(),pcoin));
                atk.setText(s.getAtk(f,BasisSet.current.t(),loadlev+loadlevp,f.getPCoin()!=null&&t.isChecked(),pcoin));
            }

            t.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        ValueAnimator anim = ValueAnimator.ofInt(0, StaticStore.dptopx(64f, getContext()));
                        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int val = (Integer) animation.getAnimatedValue();
                                ViewGroup.LayoutParams params = tal.getLayoutParams();
                                params.height = val;
                                tal.setLayoutParams(params);
                            }
                        });

                        anim.setDuration(300);
                        anim.setInterpolator(new DecelerateInterpolator());
                        anim.start();

                        int lev = (int) spinners[0].getSelectedItem();
                        int levp = (int) spinners[1].getSelectedItem();

                        int[] levs = new int[]{lev + levp, pcoin[1], pcoin[2], pcoin[3], pcoin[4], pcoin[5]};

                        BasisSet.current.sele.lu.setLv(f.unit, levs);

                        hp.setText(s.getHP(f, BasisSet.current.t(), lev + levp, f.getPCoin() != null && t.isChecked(), pcoin));
                        atk.setText(s.getAtk(f, BasisSet.current.t(), lev + levp, f.getPCoin() != null && t.isChecked(), pcoin));

                    } else {
                        ValueAnimator anim = ValueAnimator.ofInt(StaticStore.dptopx(64f, getContext()), 0);
                        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int val = (Integer) animation.getAnimatedValue();
                                ViewGroup.LayoutParams params = tal.getLayoutParams();
                                params.height = val;
                                tal.setLayoutParams(params);
                            }
                        });

                        anim.setDuration(300);
                        anim.setInterpolator(new DecelerateInterpolator());
                        anim.start();

                        int lev = (int) spinners[0].getSelectedItem();
                        int levp = (int) spinners[1].getSelectedItem();

                        int[] levs = new int[]{lev + levp, zeros[1], zeros[2], zeros[3], zeros[4], zeros[5]};

                        BasisSet.current.sele.lu.setLv(f.unit, levs);

                        hp.setText(s.getHP(f, BasisSet.current.t(), lev + levp, f.getPCoin() != null && t.isChecked(), zeros));
                        atk.setText(s.getAtk(f, BasisSet.current.t(), lev + levp, f.getPCoin() != null && t.isChecked(), zeros));
                    }
                }
            });

            if(f.getPCoin() != null) {
                if(pcoin[1] == 0 && pcoin[2] == 0 && pcoin[3] == 0 && pcoin[4] == 0 && pcoin[5] == 0) {
                    t.setChecked(false);
                    ViewGroup.LayoutParams params = tal.getLayoutParams();
                    params.height = 0;
                    tal.setLayoutParams(params);
                } else {
                    t.setChecked(true);
                }
            }

            chform.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fid++;

                    if(StaticStore.position[0] != 100)
                        BasisSet.current.sele.lu.fs[StaticStore.position[0]][StaticStore.position[1]] = f.unit.forms[fid%f.unit.forms.length];
                    else
                        line.repform = f.unit.forms[fid%f.unit.forms.length];

                    f = f.unit.forms[fid%f.unit.forms.length];

                    line.UpdateLineUp();
                    line.toFormArray();

                    int lev = (int) spinners[0].getSelectedItem();
                    int levp = (int) spinners[1].getSelectedItem();

                    hp.setText(s.getHP(f,BasisSet.current.t(),lev+levp,f.getPCoin()!=null&&t.isChecked(),pcoin));
                    atk.setText(s.getAtk(f,BasisSet.current.t(),lev+levp,f.getPCoin()!=null&&t.isChecked(),pcoin));

                    if(f.getPCoin() == null) {
                        setDisappear(t,tal);
                        pcoin = new int[] {0, 0, 0, 0, 0, 0};
                    } else {
                        setAppear(t, tal);

                        pcoin = BasisSet.current.sele.lu.getLv(f.unit);

                        int [] max = f.getPCoin().max;

                        for(int i = 1; i < max.length; i++) {
                            final int ii = i - 1;

                            List<Integer> list = new ArrayList<>();

                            for (int j = 0; j < max[i] + 1; j++)
                                list.add(j);

                            ArrayAdapter<Integer> adapter = new ArrayAdapter<>(getContext(), R.layout.spinneradapter, list);

                            talents[i - 1].setAdapter(adapter);

                            talents[i - 1].setSelection(getIndex(talents[i - 1], pcoin[i]));

                            talents[i - 1].setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    talents[ii].setClickable(false);
                                    Toast.makeText(getContext(), s.getTalentName(ii, f), Toast.LENGTH_SHORT).show();

                                    return true;
                                }
                            });
                        }

                        if(pcoin[1] == 0 && pcoin[2] == 0 && pcoin[3] == 0 && pcoin[4] == 0 && pcoin[5] == 0) {
                            t.setChecked(false);
                            ViewGroup.LayoutParams params = tal.getLayoutParams();
                            params.height = 0;
                            tal.setLayoutParams(params);
                        } else {
                            t.setChecked(true);
                        }
                    }
                }
            });
        }
    }

    private void setDisappear(View... views) {
        for(View v : views)
            v.setVisibility(View.GONE);
    }

    private void setAppear(View... views) {
        for(View v : views)
            v.setVisibility(View.VISIBLE);
    }

    private void setVariable(LineUpView line) {
        this.line = line;
    }

    private int getIndex(Spinner spinner, int lev) {
        int index = 0;

        for(int i = 0; i< spinner.getCount();i++)
            if (lev == (int)spinner.getItemAtPosition(i))
                index = i;

        return index;
    }
}
