package com.mandarin.bcu.androidutil.fakeandroid

import common.CommonStatic.FakeKey

class AndroidKeys : FakeKey {
    override fun pressed(i: Int, j: Int): Boolean {
        return false
    }

    override fun remove(i: Int, j: Int) {}
}