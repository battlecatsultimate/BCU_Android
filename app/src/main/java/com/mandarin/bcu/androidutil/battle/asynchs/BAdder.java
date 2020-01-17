package com.mandarin.bcu.androidutil.battle.asynchs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.battle.BDefinder;
import com.mandarin.bcu.androidutil.battle.BattleView;
import com.mandarin.bcu.androidutil.enemy.EDefiner;
import com.mandarin.bcu.androidutil.stage.MapDefiner;
import com.mandarin.bcu.androidutil.unit.Definer;
import com.mandarin.bcu.util.page.BBCtrl;

import java.lang.ref.WeakReference;

public class BAdder extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakReference;
    private float x,y;

    public BAdder(Activity activity) {
        this.weakReference = new WeakReference<>(activity);
    }

    @Override
    public void onPreExecute() {
        Activity activity = weakReference.get();

        if(activity == null) return;

        BattleView battleView = activity.findViewById(R.id.battleView);

        setDisappear(battleView);
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
                loadt.setText(R.string.battle_prepare);

                BattleView battleView = activity.findViewById(R.id.battleView);

                ScaleGestureDetector detector = new ScaleGestureDetector(activity,new ScaleListener(battleView));

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

                        return true;
                    }
                });

                battleView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        battleView.getPainter().click(new Point((int) x, (int) y), BBCtrl.ACTION_LONG);

                        return false;
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

        setAppear(battleView);

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

            return true;
        }
    }
}
