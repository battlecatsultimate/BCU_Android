package com.mandarin.bcu.androidutil.unit.asynchs;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.unit.adapters.DynamicExplanation;
import com.mandarin.bcu.androidutil.unit.adapters.DynamicFruit;
import com.mandarin.bcu.androidutil.unit.adapters.UnitinfPager;
import com.mandarin.bcu.androidutil.unit.adapters.UnitinfRecycle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

import common.system.MultiLangCont;

public class UInfoLoader extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakActivity;
    private final int id;
    private ArrayList<String> names = new ArrayList<>();
    private final FragmentManager fm;
    private int[] nformid = {R.string.unit_info_first,R.string.unit_info_second,R.string.unit_info_third};
    private String[] nform = new String[nformid.length];
    private TableTab table;
    private ExplanationTab explain;
    private UnitinfRecycle unitinfRecycle;

    public UInfoLoader(int id,Activity activity,FragmentManager fm) {
        this.id = id;
        this.weakActivity = new WeakReference<>(activity);
        this.fm = fm;

        for(int i=0;i<nformid.length;i++)
            nform[i] = weakActivity.get().getString(nformid[i]);
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakActivity.get();

        if(activity == null) return;

        TextView fruittext = activity.findViewById(R.id.cfinftext);
        ViewPager fruitpage = activity.findViewById(R.id.catfruitpager);
        Button anim = activity.findViewById(R.id.animanim);

        if(StaticStore.units.get(id).info.evo == null) {
            fruitpage.setVisibility(View.GONE);
            fruittext.setVisibility(View.GONE);
            anim.setVisibility(View.GONE);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakActivity.get();

        if(activity == null) return null;

        SharedPreferences shared = activity.getSharedPreferences("configuration",Context.MODE_PRIVATE);

        for(int i = 0; i<StaticStore.units.get(id).forms.length; i++) {
            String name = MultiLangCont.FNAME.getCont(StaticStore.units.get(id).forms[i]);

            if(name == null)
                name = "";

            names.add(name);
        }

        TabLayout tabs = activity.findViewById(R.id.unitinfexplain);

        for(int i=0;i<StaticStore.units.get(id).forms.length;i++) {
            tabs.addTab(tabs.newTab().setText(nform[i]));
        }

        if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(shared.getBoolean("Lay_Land",false)) {
                table = new TableTab(fm,tabs.getTabCount(),id,nform);
                explain = new ExplanationTab(fm,tabs.getTabCount(),id,nform);
            } else {
                unitinfRecycle = new UnitinfRecycle(activity,names,StaticStore.units.get(id).forms, id);
                explain = new ExplanationTab(fm,tabs.getTabCount(),id,nform);
            }
        } else {
            if(shared.getBoolean("Lay_Port",true)) {
                table = new TableTab(fm,tabs.getTabCount(),id,nform);
                explain = new ExplanationTab(fm,tabs.getTabCount(),id,nform);
            } else {
                unitinfRecycle = new UnitinfRecycle(activity,names,StaticStore.units.get(id).forms, id);
                explain = new ExplanationTab(fm,tabs.getTabCount(),id,nform);
            }
        }

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
                if(!StaticStore.UisOpen) {
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
                    StaticStore.UisOpen = true;
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
                    StaticStore.UisOpen = false;
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

        ViewPager fruitpage = activity.findViewById(R.id.catfruitpager);

        if(StaticStore.units.get(id).info.evo != null){
            fruitpage.setAdapter(new DynamicFruit(activity,id));
            fruitpage.setOffscreenPageLimit(1);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... results) {
        Activity activity = weakActivity.get();

        if(activity == null) return;

        TabLayout tabs = activity.findViewById(R.id.unitinfexplain);

        SharedPreferences shared = activity.getSharedPreferences("configuration",Context.MODE_PRIVATE);

        if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(shared.getBoolean("Lay_Land",false)) {
                setUinfo(activity,tabs);
            } else {
                setUinfoR(activity,tabs);
            }
        } else {
            if(shared.getBoolean("Lay_Port",true)) {
                setUinfo(activity,tabs);
            } else {
                setUinfoR(activity,tabs);
            }
        }

    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakActivity.get();

        if(activity == null) return;

        ScrollView scrollView = activity.findViewById(R.id.unitinfscroll);
        scrollView.setVisibility(View.VISIBLE);

        ConstraintLayout treasuretab = activity.findViewById(R.id.treasurelayout);
        treasuretab.setVisibility(View.VISIBLE);

        ProgressBar prog = activity.findViewById(R.id.unitinfprog);
        prog.setVisibility(View.GONE);

        Button anim = activity.findViewById(R.id.animanim);
        anim.setVisibility(View.VISIBLE);

        TabLayout tabs = activity.findViewById(R.id.unitinfexplain);
        Objects.requireNonNull(tabs.getTabAt(StaticStore.unittabposition)).select();
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

    protected class TableTab extends FragmentPagerAdapter {
        int form;
        int id;
        String [] names;

        TableTab(FragmentManager fm, int form, int id,String [] names) {
            super(fm);
            this.form = form;
            this.id = id;
            this.names = names;
        }

        @Override
        public Fragment getItem(int i) {
            return UnitinfPager.newInstance(i,id,names);
        }

        @Override
        public int getCount() {
            return form;
        }

        @Override
        public CharSequence getPageTitle(int position) {return names[position];}
    }

    private void setUinfo(Activity activity, TabLayout tabs) {
        ViewPager tablePager = activity.findViewById(R.id.unitinftable);
        tablePager.setAdapter(table);
        tablePager.setOffscreenPageLimit(2);
        tablePager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.setupWithViewPager(tablePager);

        ViewPager viewPager = activity.findViewById(R.id.unitinfpager);
        viewPager.setAdapter(explain);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                tablePager.setCurrentItem(tab.getPosition());
                StaticStore.unittabposition = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if(StaticStore.units.get(id).info.evo == null)
            viewPager.setPadding(0,0,0,StaticStore.dptopx(24f,activity));

        View view = activity.findViewById(R.id.view);
        View view2 = activity.findViewById(R.id.view2);
        TextView exp = activity.findViewById(R.id.unitinfexp);

        if(MultiLangCont.FEXP.getCont(StaticStore.units.get(id).forms[0]) == null) {
            viewPager.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
            view2.setVisibility(View.GONE);
            exp.setVisibility(View.GONE);
        }
    }

    private void setUinfoR(Activity activity, TabLayout tabs) {
        RecyclerView recyclerView = activity.findViewById(R.id.unitinfrec);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(unitinfRecycle);
        ViewCompat.setNestedScrollingEnabled(recyclerView,false);

        ViewPager viewPager = activity.findViewById(R.id.unitinfpager);
        viewPager.setAdapter(explain);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.setupWithViewPager(viewPager);

        if(StaticStore.units.get(id).info.evo == null)
            viewPager.setPadding(0,0,0,StaticStore.dptopx(24f,activity));

        View view = activity.findViewById(R.id.view);
        View view2 = activity.findViewById(R.id.view2);
        TextView exp = activity.findViewById(R.id.unitinfexp);

        if(MultiLangCont.FEXP.getCont(StaticStore.units.get(id).forms[0]) == null) {
            viewPager.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
            view2.setVisibility(View.GONE);
            tabs.setVisibility(View.GONE);
            exp.setVisibility(View.GONE);
        }

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                StaticStore.unittabposition = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}
