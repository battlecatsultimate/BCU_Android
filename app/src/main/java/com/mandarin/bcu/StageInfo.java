package com.mandarin.bcu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;

public class StageInfo extends AppCompatActivity {

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

        setContentView(R.layout.activity_stage_info);
        ImageButton back = findViewById(R.id.stgbck);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Spinner stageset = findViewById(R.id.stgspin);
        String [] setstg = getResources().getStringArray(R.array.set_stg);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.spinneradapter,setstg)
        {
            @NonNull
            public View getView(int position, View converView, @NonNull ViewGroup parent) {
                View v = super.getView(position,converView,parent);

                ((TextView)v).setTextColor(ContextCompat.getColor(StageInfo.this,R.color.TextPrimary));
                int eight = StaticStore.dptopx(8f,StageInfo.this);
                v.setPadding(eight,eight,eight,eight);

                return v;
            }

            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position,convertView,parent);

                ((TextView)v).setTextColor(ContextCompat.getColor(StageInfo.this,R.color.TextPrimary));

                return v;
            }
        };

        stageset.setAdapter(adapter);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences("configuration",Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase,shared.getInt("Language",0)));
    }
}
