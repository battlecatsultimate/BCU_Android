package com.mandarin.bcu;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mandarin.bcu.androidutil.DynamicExplanation;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.UnitinfRecycle;
import com.mandarin.bcu.androidutil.getStrings;

import java.util.ArrayList;

public class UnitInfo extends AppCompatActivity {
    private getStrings s = new getStrings(this);
    private ArrayList<String> names = new ArrayList<>();
    private int[] nformid = {R.string.unit_info_first,R.string.unit_info_second,R.string.unit_info_third};
    private String[] nform = new String[nformid.length];
    private ImageButton treasure;
    private ConstraintLayout mainLayout;
    private ConstraintLayout treasuretab;
    private boolean isOpen = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences shared = getSharedPreferences("configuration",MODE_PRIVATE);
        SharedPreferences.Editor ed;
        if(!shared.contains("initial")) {
            ed = shared.edit();
            ed.putBoolean("initial",true);
            ed.putBoolean("theme",true);
            ed.apply();
        } else {
            if(!shared.getBoolean("theme",false)) {
                setTheme(R.style.AppTheme_night);
            } else {
                setTheme(R.style.AppTheme_day);
            }
        }

        setContentView(R.layout.activity_unit_info);

        ScrollView scrollView = findViewById(R.id.unitinfscroll);
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scrollView.setFocusable(true);
        scrollView.setFocusableInTouchMode(true);

        for(int i=0;i<nformid.length;i++)
            nform[i] = getString(nformid[i]);

        TextView unittitle = findViewById(R.id.unitinfrarname);

        ImageButton back = findViewById(R.id.unitinfback);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.unitinfrec);
        recyclerView.requestFocusFromTouch();

        Intent result = getIntent();
        Bundle extra = result.getExtras();

        if(extra != null) {
            int id = extra.getInt("ID");
            unittitle.setText(s.getTitle(StaticStore.units.get(id).forms[0]));
            for(int i = 0; i<StaticStore.units.get(id).forms.length; i++)
                names.add(StaticStore.units.get(id).forms[i].name);

            UnitinfRecycle unitinfRecycle = new UnitinfRecycle(this,names,StaticStore.units.get(id).forms, id);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(unitinfRecycle);
            ViewCompat.setNestedScrollingEnabled(recyclerView,false);

            TabLayout tabs = findViewById(R.id.unitinfexplain);

            for(int i=0;i<StaticStore.units.get(id).forms.length;i++) {
                tabs.addTab(tabs.newTab().setText(nform[i]));
            }

            ExplanationTab explain = new ExplanationTab(getSupportFragmentManager(),tabs.getTabCount(),id,nform);

            ViewPager viewPager = findViewById(R.id.unitinfpager);
            viewPager.setAdapter(explain);
            viewPager.setOffscreenPageLimit(1);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
            tabs.setupWithViewPager(viewPager);

            treasure = findViewById(R.id.imageButton);
            mainLayout = findViewById(R.id.unitinfomain);
            treasuretab = findViewById(R.id.treasurelayout);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

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
                        View view = UnitInfo.this.getCurrentFocus();
                        if(view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
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
        }
    }

    protected class ExplanationTab extends FragmentStatePagerAdapter {
        int number;
        int id;
        String[] title;

        ExplanationTab(FragmentManager fm, int number, int id,String[] title) {
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

    @Override
    public void onBackPressed() {
        if(isOpen) {
            treasure.performClick();
        } else {
            super.onBackPressed();
        }
    }

}
