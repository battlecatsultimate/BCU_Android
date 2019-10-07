package com.mandarin.bcu.androidutil.fakeandroid;

import android.graphics.Matrix;

import common.system.fake.FakeTransform;

public class FTMT implements FakeTransform {
    private final Matrix m;

    public FTMT(Matrix m) {
        this.m = m;
    }

    @Override
    public Object getAT() {
        return m;
    }
}
