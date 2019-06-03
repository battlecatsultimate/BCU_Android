package com.mandarin.bcu;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.asynchs.CheckUpdates;
import com.mandarin.bcu.main.MainBCU;
import com.mandarin.bcu.util.system.android.BMBuilder;
import com.mandarin.bcu.util.system.fake.ImageBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    private final String [] LIB_REQUIRED = {"000001","000002","000003", "080602", "080603"};
    private String path;
    private ArrayList<String> fileneed = new ArrayList<>();
    private ArrayList<String> filenum = new ArrayList<>();

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainBCU.CheckMem(this);

        path = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU";

        ImageBuilder.builder = new BMBuilder();

        Button animbtn = findViewById(R.id.anvibtn);
        Button stagebtn = findViewById(R.id.stgbtn);
        animbtn.setVisibility(View.GONE);
        stagebtn.setVisibility(View.GONE);
        TextView checkstate = findViewById(R.id.mainstup);
        ProgressBar mainprog = findViewById(R.id.mainprogup);

        animbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this,R.drawable.ic_kasa_jizo), null, null, null);
        stagebtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this,R.drawable.ic_castle),null,null,null);

        animbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationview();
            }
        });
        stagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stageinfoview();
            }
        });

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager.getActiveNetworkInfo() != null) {
            boolean lang = false;
            CheckUpdates checkUpdates = new CheckUpdates(animbtn, stagebtn,path, lang, checkstate, mainprog,fileneed,filenum,MainActivity.this);
            checkUpdates.execute();
        } else {
            if(cando()) {
                com.mandarin.bcu.decode.ZipLib.init();
                com.mandarin.bcu.decode.ZipLib.read();
                mainprog.setVisibility(View.GONE);
                checkstate.setVisibility(View.GONE);
                stagebtn.setVisibility(View.VISIBLE);
                animbtn.setVisibility(View.VISIBLE);
            } else {
                mainprog.setVisibility(View.GONE);
                checkstate.setText(R.string.main_internet_no);
                Toast.makeText(this, "You need internet connection to run this application!", Toast.LENGTH_SHORT).show();
            }
        }

    }
    protected boolean cando() {
        String infopath = path + "/files/info/";
        String filename = "info_android.ini";

        File f = new File(infopath,filename);

        if(f.exists()) {
            try {
                String line;

                FileInputStream fis = new FileInputStream(f);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);

                ArrayList<String> lines = new ArrayList<>();

                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }

                try {
                    Set<String> libs = new TreeSet<>(Arrays.asList(lines.get(2).split("=")[1].split(",")));

                    for(String s : LIB_REQUIRED)
                        if(!libs.contains(s))
                            return false;

                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    protected void animationview() {
        Intent intent = new Intent(this, AnimationViewer.class);
        startActivity(intent);
    }

    protected void stageinfoview()
    {
        Intent intent = new Intent(this,StageInfo.class);
        startActivity(intent);
    }


}
