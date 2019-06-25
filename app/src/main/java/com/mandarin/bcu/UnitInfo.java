package com.mandarin.bcu;

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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mandarin.bcu.androidutil.DynamicExplanation;
import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.asynchs.UInfoLoader;
import com.mandarin.bcu.androidutil.getStrings;

import java.util.ArrayList;

public class UnitInfo extends AppCompatActivity {
    private ArrayList<String> names = new ArrayList<>();
    private int[] nformid = {R.string.unit_info_first,R.string.unit_info_second,R.string.unit_info_third};
    private String[] nform = new String[nformid.length];
    private boolean isOpen = false;
    private ImageButton treasure;

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
        scrollView.setVisibility(View.GONE);

        ConstraintLayout treasuretab = findViewById(R.id.treasurelayout);
        treasuretab.setVisibility(View.GONE);

        for(int i=0;i<nformid.length;i++)
            nform[i] = getString(nformid[i]);

        TextView unittitle = findViewById(R.id.unitinfrarname);

        ImageButton back = findViewById(R.id.unitinfback);
        treasure = findViewById(R.id.treabutton);

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
            getStrings s = new getStrings(this);
            unittitle.setText(s.getTitle(StaticStore.units.get(id).forms[0]));


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

            new UInfoLoader(id,this,isOpen).execute();
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

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences("configuration",Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase,shared.getInt("Language",0)));
    }

}
