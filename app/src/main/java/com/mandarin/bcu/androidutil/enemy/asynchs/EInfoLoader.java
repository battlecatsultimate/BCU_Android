package com.mandarin.bcu.androidutil.enemy.asynchs;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.enemy.adapters.DynamicEmExplanation;
import com.mandarin.bcu.androidutil.enemy.adapters.EnemyRecycle;

import java.lang.ref.WeakReference;

import common.system.MultiLangCont;

public class EInfoLoader extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakReference;
    private final int id;
    private int multi = -1;

    public EInfoLoader(Activity activity,int id) {
        this.weakReference = new WeakReference<>(activity);
        this.id = id;
    }

    public EInfoLoader(Activity activity, int id, int multi) {
        this.weakReference = new WeakReference<>(activity);
        this.id = id;
        this.multi = multi;
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        if(activity == null) return;

        if(MultiLangCont.EEXP.getCont(StaticStore.enemies.get(id)) == null) {
            View view1 = activity.findViewById(R.id.enemviewtop);
            View view2 = activity.findViewById(R.id.enemviewbot);
            ViewPager viewPager = activity.findViewById(R.id.eneminfexp);
            TextView exptext = activity.findViewById(R.id.eneminfexptx);

            if(view1 != null) {
                view1.setVisibility(View.GONE);
                view2.setVisibility(View.GONE);
                viewPager.setVisibility(View.GONE);
                exptext.setVisibility(View.GONE);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        if(activity == null) return  null;

        RecyclerView recyclerView = activity.findViewById(R.id.eneminftable);

        if(recyclerView != null) {

            EnemyRecycle enemyRecycle;

            if(multi != -1)
                enemyRecycle = new EnemyRecycle(activity,id,multi);
            else
                enemyRecycle = new EnemyRecycle(activity, id);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.setAdapter(enemyRecycle);
            ViewCompat.setNestedScrollingEnabled(recyclerView, false);

            DynamicEmExplanation explain = new DynamicEmExplanation(activity, id);

            ViewPager viewPager = activity.findViewById(R.id.eneminfexp);
            viewPager.setAdapter(explain);
            viewPager.setOffscreenPageLimit(1);
        }

        ImageButton treasure = activity.findViewById(R.id.enemtreasure);
        ConstraintLayout main = activity.findViewById(R.id.enemmainlayout);
        ConstraintLayout treasurelay = activity.findViewById(R.id.enemtreasuretab);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        AnimatorSet set = new AnimatorSet();

        treasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!StaticStore.EisOpen) {
                    ValueAnimator slider = ValueAnimator.ofInt(0,treasurelay.getWidth()).setDuration(300);
                    slider.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            treasurelay.setTranslationX(-(int)animation.getAnimatedValue());
                            treasurelay.requestLayout();
                        }
                    });

                    set.play(slider);
                    set.setInterpolator(new DecelerateInterpolator());
                    set.start();
                    StaticStore.EisOpen = true;
                } else {
                    View view = activity.getCurrentFocus();

                    if(view != null) {
                        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        treasurelay.clearFocus();
                    }

                    ValueAnimator slider = ValueAnimator.ofInt(treasurelay.getWidth(),0).setDuration(300);
                    slider.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            treasurelay.setTranslationX(-(int)animation.getAnimatedValue());
                            treasurelay.requestLayout();
                        }
                    });

                    set.play(slider);
                    set.setInterpolator(new AccelerateInterpolator());
                    set.start();
                    StaticStore.EisOpen = false;
                }
            }
        });

        treasurelay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                main.setClickable(false);
                return true;
            }
        });

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        if(activity == null) return;

        ImageButton back = activity.findViewById(R.id.eneminfbck);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticStore.EisOpen = false;
                activity.finish();
            }
        });

        ScrollView scrollView = activity.findViewById(R.id.eneminfscroll);
        ProgressBar prog = activity.findViewById(R.id.eneminfprog);

        scrollView.setVisibility(View.VISIBLE);
        prog.setVisibility(View.GONE);
    }
}
