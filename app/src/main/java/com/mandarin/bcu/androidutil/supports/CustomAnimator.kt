package com.mandarin.bcu.androidutil.supports

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import java.util.function.Consumer

class CustomAnimator(duration: Int, animator: AnimatorConst.Accelerator, from: Float, to: Float, target: Consumer<Float>) : ValueAnimator() {
    init {
        setFloatValues(from, to)
        addUpdateListener { animation ->
            val v = animation.animatedValue as Float

            target.accept(v)
        }

        this.duration = duration.toLong()

        interpolator = when(animator) {
            AnimatorConst.Accelerator.DECELERATE -> DecelerateInterpolator()
            AnimatorConst.Accelerator.ACCELERATE -> AccelerateInterpolator()
            AnimatorConst.Accelerator.ACCELDECEL -> AccelerateDecelerateInterpolator()
        }
    }
}