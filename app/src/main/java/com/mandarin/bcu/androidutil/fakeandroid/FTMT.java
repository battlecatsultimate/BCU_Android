package com.mandarin.bcu.androidutil.fakeandroid;

import android.graphics.Matrix;

import common.system.fake.FakeTransform;

public class FTMT implements FakeTransform {
    private final Matrix m = new Matrix();

    FTMT(Matrix m) {
        this.m.set(m);
    }

    @Override
    public Object getAT() {
        return m;
    }

    void updateMatrix(Matrix m) {
        this.m.set(m);
    }

    public void setMatrix(Matrix m) {
        m.set(this.m);
    }
}
