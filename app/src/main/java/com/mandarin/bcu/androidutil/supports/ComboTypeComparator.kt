package com.mandarin.bcu.androidutil.supports

import common.util.unit.Combo

class ComboTypeComparator : Comparator<Combo> {
    override fun compare(p0: Combo?, p1: Combo?): Int {
        return if(p0 == null || p1 == null) {
            if (p0 == null && p1 != null) {
                -1
            } else if (p0 != null && p1 == null) {
                1
            } else {
                0
            }
        } else {
            if (p0.type == p1.type) {
                p0.lv.compareTo(p1.lv)
            } else {
                0
            }
        }
    }
}