package com.mandarin.bcu.androidutil.asynchs;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
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
import com.mandarin.bcu.UnitInfo;
import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.adapters.DynamicExplanation;
import com.mandarin.bcu.androidutil.adapters.DynamicFruit;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.UnitinfRecycle;
import com.mandarin.bcu.androidutil.getStrings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import common.system.MultiLangCont;

public class UInfoLoader extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakActivity;
    private final int id;
    private ArrayList<String> names = new ArrayList<>();
    private boolean isOpen;
    private final FragmentManager fm;
    private int[] nformid = {R.string.unit_info_first,R.string.unit_info_second,R.string.unit_info_third};
    private String[] nform = new String[nformid.length];

    public UInfoLoader(int id,Activity activity,boolean isOpen,FragmentManager fm) {
     this.id = id;
     this.weakActivity = new WeakReference<>(activity);
     this.isOpen = isOpen;
     this.fm = fm;

    for(int i=0;i<nformid.length;i++)
        nform[i] = weakActivity.get().getString(nformid[i]);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakActivity.get();


        RecyclerView recyclerView = activity.findViewById(R.id.unitinfrec);

        getStrings s = new getStrings(activity);
        for(int i = 0; i<StaticStore.units.get(id).forms.length; i++) {
            String name = MultiLangCont.FNAME.getCont(StaticStore.units.get(id).forms[i]);

            if(name == null)
                name = "";

            names.add(name);
        }

        UnitinfRecycle unitinfRecycle = new UnitinfRecycle(activity,names,StaticStore.units.get(id).forms, id);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(unitinfRecycle);
        ViewCompat.setNestedScrollingEnabled(recyclerView,false);

        publishProgress(0);

        ImageButton treasure = activity.findViewById(R.id.treabutton);
        ConstraintLayout mainLayout = activity.findViewById(R.id.unitinfomain);
        ConstraintLayout treasuretab = activity.findViewById(R.id.treasurelayout);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        AnimatorSet set = new AnimatorSet();

        treasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isOpen) {
                    ValueAnimator slider = ValueAnimator.ofInt(0,treasuretab.getWidth()).setDuration(300);
                    slider.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            treasuretab.setTranslationX(-(int)animation.getAnimatedValue());
                            treasuretab.requestLayout();
                        }
                    });

                    set.play(slider);
                    set.setInterpolator(new DecelerateInterpolator());
                    set.start();
                    isOpen = true;
                } else {
                    View view = activity.getCurrentFocus();
                    if(view != null) {
                        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                        treasuretab.clearFocus();
                    }
                    ValueAnimator slider = ValueAnimator.ofInt(treasuretab.getWidth(),0).setDuration(300);
                    slider.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            treasuretab.setTranslationX(-(int)animation.getAnimatedValue());
                            treasuretab.requestLayout();
                        }
                    });
                    set.play(slider);
                    set.setInterpolator(new AccelerateInterpolator());
                    set.start();
                    isOpen = false;
                }
            }
        });

        treasuretab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mainLayout.setClickable(false);
                return true;
            }
        });

        TextView fruittext = activity.findViewById(R.id.cfinftext);
        ViewPager fruitpage = activity.findViewById(R.id.catfruitpager);

        if(StaticStore.units.get(id).info.evo == null) {
            fruitpage.setVisibility(View.GONE);
            fruittext.setVisibility(View.GONE);
        } else {
            fruitpage.setAdapter(new DynamicFruit(activity,id));
            fruitpage.setOffscreenPageLimit(1);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... results) {
        Activity activity = weakActivity.get();

        TabLayout tabs = activity.findViewById(R.id.unitinfexplain);

        for(int i=0;i<StaticStore.units.get(id).forms.length;i++) {
            tabs.addTab(tabs.newTab().setText(nform[i]));
        }

        ExplanationTab explain = new ExplanationTab(fm,tabs.getTabCount(),id,nform);

        ViewPager viewPager = activity.findViewById(R.id.unitinfpager);
        viewPager.setAdapter(explain);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.setupWithViewPager(viewPager);

        if(StaticStore.units.get(id).info.evo == null)
            viewPager.setPadding(0,0,0,StaticStore.dptopx(24f,activity));
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakActivity.get();

        ScrollView scrollView = activity.findViewById(R.id.unitinfscroll);
        scrollView.setVisibility(View.VISIBLE);

        ConstraintLayout treasuretab = activity.findViewById(R.id.treasurelayout);
        treasuretab.setVisibility(View.VISIBLE);

        ProgressBar prog = activity.findViewById(R.id.unitinfprog);
        prog.setVisibility(View.GONE);
    }

    protected class ExplanationTab extends FragmentStatePagerAdapter {
        int number;
        int id;
        String[] title;

        ExplanationTab(FragmentManager fm, int number, int id, String[] title) {
            super(fm);
            this.number = number;
            this.id = id;
            this.title = title;
        }

        @Override
        public Fragment getItem(int i) {
            return DynamicExplanation.newInstance(i,id,title);
        }

        @Override
        public int getCount() {
            return number;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }
}
