package com.mandarin.bcu.androidutil.battle.asynchs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.MediaPrepare;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.battle.BDefinder;
import com.mandarin.bcu.androidutil.battle.BattleView;
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler;
import com.mandarin.bcu.androidutil.enemy.EDefiner;
import com.mandarin.bcu.androidutil.fakeandroid.AndroidKeys;
import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics;
import com.mandarin.bcu.androidutil.stage.MapDefiner;
import com.mandarin.bcu.androidutil.unit.Definer;
import com.mandarin.bcu.util.page.BBCtrl;

import java.lang.ref.WeakReference;

import common.battle.BasisSet;
import common.battle.SBCtrl;
import common.system.P;
import common.util.stage.MapColc;
import common.util.stage.Stage;
import common.util.stage.StageMap;

public class BAdder extends AsyncTask<Void, Integer, Void> {
    private final WeakReference<Activity> weakReference;
    private final int mapcode, stid, stage, star, item;
    private float x, y;

    public BAdder(Activity activity, int mapcode, int stid, int stage, int star, int itme) {
        this.weakReference = new WeakReference<>(activity);
        this.mapcode = mapcode;
        this.stid = stid;
        this.stage = stage;
        this.star = star;
        this.item = itme;
    }

    @Override
    public void onPreExecute() {
        Activity activity = weakReference.get();

        if (activity == null) return;

        FloatingActionButton fab = activity.findViewById(R.id.battlepause);
        FloatingActionButton fast = activity.findViewById(R.id.battlefast);
        FloatingActionButton slow = activity.findViewById(R.id.battleslow);

        fab.hide();
        fast.hide();
        slow.hide();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        if (activity == null) return null;

        new Definer().define(activity);

        publishProgress(0);

        new EDefiner().define(activity);

        publishProgress(1);

        new MapDefiner().define(activity);

        publishProgress(2);

        new BDefinder().define();

        publishProgress(3);

        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onProgressUpdate(Integer... result) {
        Activity activity = weakReference.get();

        if (activity == null) return;

        TextView loadt = activity.findViewById(R.id.battleloadt);

        switch (result[0]) {
            case 0:
                loadt.setText(R.string.stg_info_enem);
                break;
            case 1:
                loadt.setText(R.string.stg_list_stl);
                break;
            case 2:
                loadt.setText(R.string.battle_loading);
                break;
            case 3:
                LinearLayout layout = activity.findViewById(R.id.battlelayout);

                MapColc mc = StaticStore.map.get(mapcode);

                if (mc == null)
                    return;

                StageMap stm = mc.maps[stid];

                if (stm == null)
                    return;

                Stage stg = stm.list.get(stage);

                if (stg == null)
                    return;

                SBCtrl ctrl = new SBCtrl(new AndroidKeys(), stg, star, BasisSet.current.sele, new int[]{item}, 0L);

                SharedPreferences shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);

                boolean axis = shared.getBoolean("Axis", true);

                BattleView view = new BattleView(activity, ctrl, 1, axis);
                view.initialized = false;
                view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                view.setId(R.id.battleView);
                view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                layout.addView(view);

                loadt.setText(R.string.battle_prepare);

                BattleView battleView = activity.findViewById(R.id.battleView);

                ScaleGestureDetector detector = new ScaleGestureDetector(activity, new ScaleListener(battleView));

                FloatingActionButton actionButton = activity.findViewById(R.id.battlepause);
                FloatingActionButton play = activity.findViewById(R.id.battleplay);
                FloatingActionButton skipframe = activity.findViewById(R.id.battlenextframe);
                FloatingActionButton fast = activity.findViewById(R.id.battlefast);
                FloatingActionButton slow = activity.findViewById(R.id.battleslow);

                skipframe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        battleView.painter.bf.update();
                        battleView.invalidate();
                    }
                });

                actionButton.setExpanded(false);

                actionButton.setOnClickListener(new SingleClick() {
                    @Override
                    public void onSingleClick(View v) {
                        actionButton.setExpanded(true);
                        battleView.paused = true;
                        fast.hide();
                        slow.hide();
                    }
                });

                play.setOnClickListener(new SingleClick() {
                    @Override
                    public void onSingleClick(View v) {
                        actionButton.setExpanded(false);
                        battleView.paused = false;
                        fast.show();
                        slow.show();
                    }
                });

                battleView.setOnTouchListener(new View.OnTouchListener() {
                    int preid = -1;
                    int preX;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        detector.onTouchEvent(event);

                        if (preid == -1)
                            preid = event.getPointerId(0);

                        int id = event.getPointerId(0);

                        int x2 = (int) event.getX();

                        int action = event.getAction();

                        if (action == MotionEvent.ACTION_DOWN) {
                            x = event.getX();
                            y = event.getY();
                        } else if (action == MotionEvent.ACTION_UP) {
                            if(battleView.painter.bf.sb.ubase.health > 0 && battleView.painter.bf.sb.ebase.health > 0) {
                                battleView.getPainter().click(new Point((int) event.getX(), (int) event.getY()), action);
                            }
                        } else if (action == MotionEvent.ACTION_MOVE) {
                            if (event.getPointerCount() == 1 && id == preid) {

                                battleView.painter.pos += x2 - preX;

                                if(battleView.paused) {
                                    battleView.invalidate();
                                }
                            }
                        }

                        preX = x2;

                        preid = id;

                        return false;
                    }
                });

                battleView.setLongClickable(true);

                battleView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if(battleView.painter.bf.sb.ubase.health > 0 && battleView.painter.bf.sb.ebase.health > 0) {
                            battleView.getPainter().click(new Point((int) x, (int) y), BBCtrl.ACTION_LONG);
                        }

                        return true;
                    }
                });

                fast.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (battleView.spd < (battleView.painter instanceof BBCtrl ? 5 : 7)) {
                            battleView.spd++;
                            SoundHandler.speed++;

                            battleView.painter.reset();
                        }
                    }
                });

                slow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (battleView.spd > -7) {
                            battleView.spd--;
                            SoundHandler.speed--;

                            battleView.painter.reset();
                        }
                    }
                });

                Button exitbattle = activity.findViewById(R.id.battleexit);

                exitbattle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (battleView.painter.bf.sb.ebase.health > 0 && battleView.painter.bf.sb.ubase.health > 0 && shared.getBoolean("show", true)) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(activity, R.style.AlertDialog);

                            LayoutInflater inflater = LayoutInflater.from(activity);

                            View layouts = inflater.inflate(R.layout.do_not_show_dialog, null);

                            CheckBox donotshow = layouts.findViewById(R.id.donotshowcheck);
                            Button cancel = layouts.findViewById(R.id.battlecancel);
                            Button exit = layouts.findViewById(R.id.battledexit);

                            alert.setView(layouts);

                            donotshow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked) {
                                        SharedPreferences.Editor editor = shared.edit();

                                        editor.putBoolean("show", false);

                                        editor.apply();
                                    } else {
                                        SharedPreferences.Editor editor = shared.edit();

                                        editor.putBoolean("show", true);

                                        editor.apply();
                                    }
                                }
                            });

                            AlertDialog dialog = alert.create();

                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.cancel();
                                }
                            });

                            exit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    P.stack.clear();
                                    CVGraphics.clear();
                                    dialog.dismiss();

                                    SoundHandler.ResetHandler();

                                    if(SoundHandler.MUSIC != null) {
                                        if(SoundHandler.MUSIC.isInitialized()) {
                                            if(SoundHandler.MUSIC.isRunning()) {
                                                SoundHandler.MUSIC.stop();
                                                SoundHandler.MUSIC.release();
                                                SoundHandler.MUSIC = null;
                                            } else {
                                                SoundHandler.MUSIC.release();
                                                SoundHandler.MUSIC = null;
                                            }
                                        }
                                    }

                                    activity.finish();
                                }
                            });

                            dialog.show();
                        } else {
                            P.stack.clear();
                            CVGraphics.clear();

                            SoundHandler.ResetHandler();

                            if(SoundHandler.MUSIC != null) {
                                if(SoundHandler.MUSIC.isInitialized()) {
                                    if(SoundHandler.MUSIC.isRunning()) {
                                        SoundHandler.MUSIC.stop();
                                        SoundHandler.MUSIC.release();
                                        SoundHandler.MUSIC = null;
                                    } else {
                                        SoundHandler.MUSIC.release();
                                        SoundHandler.MUSIC = null;
                                    }
                                }
                            }
                            activity.finish();
                        }
                    }
                });

                Switch mus = activity.findViewById(R.id.switchmus);

                SeekBar musvol = activity.findViewById(R.id.seekmus);

                mus.setChecked(shared.getBoolean("music",true));
                musvol.setEnabled(shared.getBoolean("music",true));

                musvol.setMax(99);

                mus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mus.setClickable(false);

                        mus.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mus.setClickable(true);
                            }
                        },1000);

                        if(isChecked) {
                            SharedPreferences.Editor editor = shared.edit();
                            editor.putBoolean("music",true);
                            editor.apply();
                            SoundHandler.musicPlay = true;
                            musvol.setEnabled(true);

                            if(SoundHandler.MUSIC != null) {
                                if (!SoundHandler.MUSIC.isPlaying() && SoundHandler.MUSIC.isInitialized()) {
                                    SoundHandler.MUSIC.start();
                                }
                            }
                        } else {
                            SharedPreferences.Editor editor = shared.edit();
                            editor.putBoolean("music",false);
                            editor.apply();
                            SoundHandler.musicPlay = false;
                            musvol.setEnabled(false);

                            if(SoundHandler.MUSIC != null) {
                                if (SoundHandler.MUSIC.isInitialized() && SoundHandler.MUSIC.isRunning()) {
                                    if (SoundHandler.MUSIC.isPlaying())
                                        SoundHandler.MUSIC.pause();
                                }
                            }
                        }
                    }
                });
                musvol.setProgress(shared.getInt("mus_vol",99));

                musvol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser) {
                            if(progress >= 100 || progress < 0) return;

                            SharedPreferences.Editor editor = shared.edit();
                            editor.putInt("mus_vol",progress);
                            editor.apply();

                            float log1 = (float)(1-(Math.log(100-progress)/Math.log(100)));

                            SoundHandler.MUSIC.setVolume(log1,log1);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                Switch switchse = activity.findViewById(R.id.switchse);
                SeekBar seekse = activity.findViewById(R.id.seekse);

                switchse.setChecked(shared.getBoolean("SE",true));
                seekse.setEnabled(shared.getBoolean("SE",true));

                seekse.setMax(99);
                seekse.setProgress(shared.getInt("se_vol",99));

                switchse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        switchse.setClickable(false);

                        switchse.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                switchse.setClickable(true);
                            }
                        },1000);

                        if(isChecked) {
                            SharedPreferences.Editor editor = shared.edit();
                            editor.putBoolean("SE",true);
                            editor.apply();
                            SoundHandler.se_vol = StaticStore.getVolumScaler((int)(shared.getInt("se_vol",99)*0.85));
                            seekse.setEnabled(true);
                        } else {
                            SharedPreferences.Editor editor = shared.edit();
                            editor.putBoolean("SE",false);
                            editor.apply();
                            SoundHandler.se_vol = 0;
                            seekse.setEnabled(false);
                        }
                    }
                });

                seekse.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser) {
                            if(progress >= 100 || progress < 0) return;

                            SharedPreferences.Editor editor = shared.edit();
                            editor.putInt("se_vol",progress);
                            editor.apply();

                            SoundHandler.se_vol = StaticStore.getVolumScaler((int)(progress*0.85));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                break;
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        if (activity == null) return;

        BattleView battleView = activity.findViewById(R.id.battleView);
        ProgressBar prog = activity.findViewById(R.id.battleprog);
        TextView loadt = activity.findViewById(R.id.battleloadt);
        FloatingActionButton fab = activity.findViewById(R.id.battlepause);
        FloatingActionButton fast = activity.findViewById(R.id.battlefast);
        FloatingActionButton slow = activity.findViewById(R.id.battleslow);

        setAppear(battleView);

        fab.show();
        fast.show();
        slow.show();

        ((ViewManager) prog.getParent()).removeView(prog);
        ((ViewManager) loadt.getParent()).removeView(loadt);

        battleView.initialized = true;
        if(SoundHandler.MUSIC != null)
            if(SoundHandler.MUSIC.isInitialized() && !SoundHandler.MUSIC.isRunning()) {
                SoundHandler.MUSIC.prepareAsync();
                SoundHandler.MUSIC.setOnPreparedListener(new MediaPrepare() {
                    @Override
                    public void PrePare(MediaPlayer mp) {
                        if(SoundHandler.musicPlay)
                            SoundHandler.MUSIC.start();
                    }
                });
            }

    }

    private void setDisappear(View... views) {
        for (View v : views)
            v.setVisibility(View.GONE);
    }

    private void setAppear(View... views) {
        for (View v : views)
            v.setVisibility(View.VISIBLE);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private final BattleView cView;

        ScaleListener(BattleView view) {
            this.cView = view;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            cView.painter.siz *= detector.getScaleFactor();

            if (cView.paused) {
                cView.invalidate();
            }

            return true;
        }
    }
}
