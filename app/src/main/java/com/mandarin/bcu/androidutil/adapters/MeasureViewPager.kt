package com.mandarin.bcu.androidutil.adapters

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager

class MeasureViewPager : ViewPager {
    constructor(context: Context) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}

    override fun onMeasure(width: Int, height: Int) {
        var h = height
        val mode = MeasureSpec.getMode(h)
        if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
            super.onMeasure(width, h)
            var he = 0
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.measure(width, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                val h1 = child.measuredHeight
                if (h1 > he) he = h1
            }
            h = MeasureSpec.makeMeasureSpec(he, MeasureSpec.EXACTLY)
        }
        super.onMeasure(width, h)
    }
}