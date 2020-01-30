package com.mandarin.bcu.androidutil.adapters

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.mandarin.bcu.androidutil.adapters.AnimatorConst.ACCELDECEL
import com.mandarin.bcu.androidutil.adapters.AnimatorConst.ACCELERATE
import com.mandarin.bcu.androidutil.adapters.AnimatorConst.DECELERATE
import com.mandarin.bcu.androidutil.adapters.AnimatorConst.HEIGHT
import com.mandarin.bcu.androidutil.adapters.AnimatorConst.WIDTH

class ScaleAnimator(target: View, mode: Int, duration: Int, animator: Int, from: Int, to: Int) : ValueAnimator() {

    init {
        setIntValues(from,to)
        addUpdateListener { animation ->
            val v = animation.animatedValue as Int
            val layout = target.layoutParams

            when(mode) {
                HEIGHT -> layout.height = v
                WIDTH -> layout.width = v
            }

            target.layoutParams = layout
        }
        this.duration = duration.toLong()

        when(animator) {
            DECELERATE -> interpolator = DecelerateInterpolator()
            ACCELERATE -> interpolator = AccelerateInterpolator()
            ACCELDECEL -> interpolator = AccelerateDecelerateInterpolator()
        }
    }
}