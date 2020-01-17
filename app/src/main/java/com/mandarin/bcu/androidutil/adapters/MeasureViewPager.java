package com.mandarin.bcu.androidutil.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class MeasureViewPager extends ViewPager {
    public MeasureViewPager(@NonNull Context context) {
        super(context);
    }

    public MeasureViewPager(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    @Override
    protected void onMeasure(int width, int height) {
        int mode = MeasureSpec.getMode(height);
        if(mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
            super.onMeasure(width,height);
            int he = 0;

            for(int i =0;i<getChildCount();i++) {
                View child = getChildAt(i);
                child.measure(width,MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int h = child.getMeasuredHeight();
                if(h > he)
                    he = h;
            }

            height = MeasureSpec.makeMeasureSpec(he,MeasureSpec.EXACTLY);
        }

        super.onMeasure(width,height);
    }
}
