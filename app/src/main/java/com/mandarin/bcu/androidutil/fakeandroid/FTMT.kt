package com.mandarin.bcu.androidutil.fakeandroid

import android.graphics.Matrix
import common.system.fake.FakeTransform

class FTMT internal constructor(m: Matrix?) : FakeTransform {
    private val m = Matrix()
    override fun getAT(): Any {
        return m
    }

    fun updateMatrix(m: Matrix?) {
        this.m.reset()
        this.m.set(m)
    }

    fun setMatrix(m: Matrix) {
        m.set(this.m)
    }

    init {
        this.m.set(m)
    }
}