package com.mandarin.bcu.androidutil;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LeakingThread extends Thread {
    private static Object obj = new Object();
    public static Thread th = new LeakingThread();

    public static List<View> views = new ArrayList<>();

    public LeakingThread() {
        setName("FUCK FUCK FUCK");
        start();
    }

    @Override
    public void run() {
        synchronized (obj) {
            try {
                obj.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
