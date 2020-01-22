package com.mandarin.bcu.androidutil;

import android.app.Application;
import android.os.StrictMode;

public class LeakingCheck extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        enable();
    }

    private void enable() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectAll().penaltyLog().penaltyDeath().build());
    }
}
