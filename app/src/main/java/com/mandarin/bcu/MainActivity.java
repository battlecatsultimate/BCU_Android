package com.mandarin.bcu;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler;
import com.mandarin.bcu.androidutil.io.ErrorLogWriter;
import com.mandarin.bcu.androidutil.io.asynchs.UploadLogs;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static boolean isRunning = false;

    private boolean sendcheck = false;
    private boolean notshowcheck = false;

    private boolean send = false;
    private boolean show = false;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences shared = getSharedPreferences(StaticStore.CONFIG, MODE_PRIVATE);
        SharedPreferences.Editor ed = shared.edit();
        if (!shared.contains("initial")) {
            ed.putBoolean("initial", true);
            ed.putBoolean("theme", true);
            ed.putBoolean("frame", true);
            ed.putBoolean("apktest", false);
            ed.putInt("default_level", 50);
            ed.putInt("Language", 0);
            ed.apply();
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_night);
            } else {
                setTheme(R.style.AppTheme_day);
            }
        }

        if (shared.getInt("Orientation", 0) == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else if (shared.getInt("Orientation", 0) == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        else if (shared.getInt("Orientation", 0) == 0)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        SharedPreferences preferences = getSharedPreferences(StaticStore.CONFIG, MODE_PRIVATE);

        Thread.setDefaultUncaughtExceptionHandler(new ErrorLogWriter(StaticStore.LOGPATH, preferences.getBoolean("upload", false) || preferences.getBoolean("ask_upload", true)));

        setContentView(R.layout.activity_main);

        SoundHandler.musicPlay = preferences.getBoolean("music",true);
        SoundHandler.mu_vol = StaticStore.getVolumScaler(preferences.getInt("mu_vol",99));
        SoundHandler.sePlay = preferences.getBoolean("SE",true);
        SoundHandler.se_vol = StaticStore.getVolumScaler((int)(preferences.getInt("se_vol",99)*0.85));

        Intent result = getIntent();

        boolean conf = false;

        Bundle bundle = result.getExtras();

        if (bundle != null)
            conf = bundle.getBoolean("Config");

        String upath = Environment.getDataDirectory().getAbsolutePath() + "/data/com.mandarin.bcu/upload/";

        File upload = new File(upath);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (upload.exists() && upload.listFiles().length > 0 && connectivityManager.getActiveNetworkInfo() != null) {
            if (preferences.getBoolean("ask_upload", true) && (!StaticStore.dialogisShwed && !conf)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = LayoutInflater.from(this);
                View v = inflater.inflate(R.layout.error_dialog, null);

                builder.setView(v);

                Button yes = v.findViewById(R.id.errorupload);
                Button no = v.findViewById(R.id.errorno);
                RadioGroup group = v.findViewById(R.id.radio);
                RadioButton donotshow = v.findViewById(R.id.radionotshow);
                RadioButton always = v.findViewById(R.id.radiosend);

                always.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (sendcheck && send) {
                            group.clearCheck();

                            SharedPreferences.Editor editor = preferences.edit();

                            editor.putBoolean("upload", false);
                            editor.putBoolean("ask_upload", true);

                            editor.apply();

                            sendcheck = false;
                            notshowcheck = false;

                            send = false;
                            show = false;
                        } else if (sendcheck || send) {
                            send = true;
                        }
                    }
                });

                donotshow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (notshowcheck && show) {
                            group.clearCheck();

                            SharedPreferences.Editor editor = preferences.edit();

                            editor.putBoolean("upload", false);
                            editor.putBoolean("ask_upload", true);

                            editor.apply();

                            sendcheck = false;
                            notshowcheck = false;

                            send = false;
                            show = false;
                        } else if (notshowcheck || show) {
                            show = true;
                        }
                    }
                });

                group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (checkedId == donotshow.getId()) {
                            SharedPreferences.Editor editor = preferences.edit();

                            editor.putBoolean("upload", false);
                            editor.putBoolean("ask_upload", false);

                            editor.apply();

                            notshowcheck = true;
                            sendcheck = false;

                            send = false;
                            show = false;
                        } else {
                            SharedPreferences.Editor editor = preferences.edit();

                            editor.putBoolean("upload", true);
                            editor.putBoolean("ask_upload", false);

                            editor.apply();

                            notshowcheck = false;
                            sendcheck = true;

                            send = false;
                            show = false;
                        }
                    }
                });

                AlertDialog dialog = builder.create();

                dialog.setCancelable(true);

                dialog.show();

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleter(upload);
                        dialog.dismiss();
                    }
                });

                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new UploadLogs(MainActivity.this).execute();
                        Toast.makeText(MainActivity.this, R.string.main_err_start, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (shared.getInt("Orientation", 0) == 1)
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                        else if (shared.getInt("Orientation", 0) == 2)
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                        else if (shared.getInt("Orientation", 0) == 0)
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

                        StaticStore.dialogisShwed = true;
                    }
                });
            } else if (preferences.getBoolean("upload", false)) {
                Toast.makeText(this, R.string.main_err_upload, Toast.LENGTH_SHORT).show();
                new UploadLogs(this).execute();
            }
        }

        isRunning = true;

        Button animbtn = findViewById(R.id.anvibtn);
        Button stagebtn = findViewById(R.id.stgbtn);
        Button emlistbtn = findViewById(R.id.eninfbtn);
        Button basisbtn = findViewById(R.id.basisbtn);
        Button medalbtn = findViewById(R.id.medalbtn);
        Button bgbtn = findViewById(R.id.bgbtn);

        FloatingActionButton config = findViewById(R.id.mainconfig);

        animbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this, R.drawable.ic_kasa_jizo), null, null, null);
        stagebtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this, R.drawable.ic_castle), null, null, null);
        emlistbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this, R.drawable.ic_enemy), null, null, null);
        emlistbtn.setCompoundDrawablePadding(StaticStore.dptopx(16f, this));
        basisbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this, R.drawable.ic_basis), null, null, null);
        medalbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this, R.drawable.ic_medal), null, null, null);
        bgbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this, R.drawable.ic_bg), null, null, null);
        bgbtn.setCompoundDrawablePadding(StaticStore.dptopx(16f, this));

        animbtn.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                animationview();
            }
        });

        stagebtn.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                stageinfoview();
            }
        });

        config.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                gotoconfig();
            }
        });

        emlistbtn.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                gotoenemyinf();
            }
        });

        basisbtn.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(MainActivity.this, LineUpScreen.class);
                startActivity(intent);
            }
        });

        medalbtn.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(MainActivity.this, MedalList.class);
                startActivity(intent);
            }
        });

        bgbtn.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(MainActivity.this, BackgroundList.class);
                startActivity(intent);
            }
        });
    }

    protected void animationview() {
        Intent intent = new Intent(this, AnimationViewer.class);
        startActivity(intent);
    }

    protected void stageinfoview() {
        Intent intent = new Intent(this, MapList.class);
        startActivity(intent);
    }

    protected void gotoconfig() {
        Intent intent = new Intent(this, ConfigScreen.class);
        startActivity(intent);
        finish();
    }

    protected void gotoenemyinf() {
        Intent intent = new Intent(this, EnemyList.class);
        startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase, shared.getInt("Language", 0)));
    }

    @Override
    public void onBackPressed() {
        isRunning = false;
        StaticStore.dialogisShwed = false;
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        StaticStore.dialogisShwed = false;
        super.onDestroy();
    }

    private void deleter(File f) {
        if (f.isDirectory())
            for (File g : f.listFiles())
                deleter(g);
        else
            f.delete();
    }
}
