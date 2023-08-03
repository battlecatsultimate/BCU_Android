package com.mandarin.bcu.androidutil.supports

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

class TranslationAnimator(target: View, axis: AnimatorConst.Axis, duration: Int, animator: AnimatorConst.Accelerator, from: Float, to: Float) : ValueAnimator() {
    init {
        setFloatValues(from, to)
        addUpdateListener { animation ->
            val v = animation.animatedValue as Float

            when(axis) {
                AnimatorConst.Axis.X -> target.translationX = -v
                AnimatorConst.Axis.Y -> target.translationY = -v
            }

            target.requestLayout()
        }

        this.duration = duration.toLong()

        interpolator = when(animator) {
            AnimatorConst.Accelerator.DECELERATE -> DecelerateInterpolator()
            AnimatorConst.Accelerator.ACCELERATE -> AccelerateInterpolator()
            AnimatorConst.Accelerator.ACCELDECEL -> AccelerateDecelerateInterpolator()
        }
    }
}