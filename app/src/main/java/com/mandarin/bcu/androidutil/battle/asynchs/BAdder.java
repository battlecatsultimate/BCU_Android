package com.mandarin.bcu.androidutil.battle.asynchs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.battle.BDefinder;
import com.mandarin.bcu.androidutil.battle.BattleView;
import com.mandarin.bcu.androidutil.enemy.EDefiner;
import com.mandarin.bcu.androidutil.fakeandroid.AndroidKeys;
import com.mandarin.bcu.androidutil.stage.MapDefiner;
import com.mandarin.bcu.androidutil.unit.Definer;
import com.mandarin.bcu.util.page.BBCtrl;

import java.lang.ref.WeakReference;

import common.battle.BasisSet;
import common.battle.SBCtrl;
import common.util.stage.MapColc;
import common.util.stage.Stage;
import common.util.stage.StageMap;

public class BAdder extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakReference;
    private final int mapcode,stid,stage,star,item;
    private float x,y;

    public BAdder(Activity activity,int mapcode, int stid, int stage, int star, int itme) {
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

        if(activity == null) return;

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

        if(activity == null) return null;

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

        if(activity == null) return;

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

                if(mc == null)
                    return;

                StageMap stm = mc.maps[stid];

                if(stm == null)
                    return;

                Stage stg = stm.list.get(stage);

                if(stg == null)
                    return;

                SBCtrl ctrl = new SBCtrl(new AndroidKeys(),stg,star, BasisSet.current.sele,new int [] {item},0L);

                SharedPreferences shared = activity.getSharedPreferences("configuration", Context.MODE_PRIVATE);

                boolean axis = shared.getBoolean("Axis",true);

                BattleView view = new BattleView(activity,ctrl,1,axis);
                view.initialized = false;
                view.setLayerType(View.LAYER_TYPE_HARDWARE,null);
                view.setId(R.id.battleView);
                view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                layout.addView(view);

                loadt.setText(R.string.battle_prepare);

                BattleView battleView = activity.findViewById(R.id.battleView);

                ScaleGestureDetector detector = new ScaleGestureDetector(activity,new ScaleListener(battleView));

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

                        if(preid == -1)
                            preid = event.getPointerId(0);

                        int id = event.getPointerId(0);

                        int x2 = (int) event.getX();

                        int action = event.getAction();

                        if(action == MotionEvent.ACTION_DOWN) {
                            x = event.getX();
                            y = event.getY();
                        } else if(action == MotionEvent.ACTION_UP) {
                            battleView.getPainter().click(new Point((int)event.getX(),(int)event.getY()),action);
                            System.out.println("Up : "+"("+event.getX()+","+event.getY()+")");
                        } else if(action == MotionEvent.ACTION_MOVE) {
                            if(event.getPointerCount() == 1 && id == preid) {

                                battleView.painter.pos += x2-preX;
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
                        battleView.getPainter().click(new Point((int) x, (int) y), BBCtrl.ACTION_LONG);

                        return true;
                    }
                });

                fast.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(battleView.spd < (battleView.painter instanceof BBCtrl ? 5 : 7)) {
                            battleView.spd++;

                            if(battleView.spd < 0 && battleView.upd >= Math.pow(2,-battleView.spd)) {
                                battleView.painter.bf.update();
                                battleView.upd = 0;
                            }

                            battleView.painter.reset();
                        }
                    }
                });

                slow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(battleView.spd > -5) {
                            battleView.spd--;

                            if(battleView.spd < 0 && battleView.upd >= Math.pow(2,-battleView.spd)) {
                                battleView.painter.bf.update();
                                battleView.upd = 0;
                            }

                            battleView.painter.reset();
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

        ((ViewManager)prog.getParent()).removeView(prog);
        ((ViewManager)loadt.getParent()).removeView(loadt);

        battleView.initialized = true;
    }

    private void setDisappear(View... views) {
        for(View v : views)
            v.setVisibility(View.GONE);
    }

    private void setAppear(View... views) {
        for(View v : views)
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

            if(cView.paused) {
                cView.invalidate();
            }

            return true;
        }
    }
}
