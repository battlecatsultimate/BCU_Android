package com.mandarin.bcu.androidutil.adapters;

import android.os.SystemClock;
import android.view.View;

public abstract class SingleClick implements View.OnClickListener {
    private static final long INTERVAL = 1000;

    private long mLastClickTime;

    public abstract void onSingleClick(View v);

    @Override
    public final void onClick(View v) {
        long currentTime = SystemClock.uptimeMillis();
        long elapsed = currentTime - mLastClickTime;
        mLastClickTime = currentTime;

        if (elapsed <= INTERVAL) {
            return;
        }

        onSingleClick(v);
    }
}
