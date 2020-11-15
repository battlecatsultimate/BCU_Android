package com.mandarin.bcu.androidutil.supports

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

@SuppressLint("Recycle")
class AlphaAnimator(target: View, duration: Int, animator: Int, from: Float, to: Float) : ValueAnimator() {
    init {
        setFloatValues(filter(from),filter(to))
        addUpdateListener { animation ->
            val v = animation.animatedValue as Float
            target.alpha = v
        }
        this.duration = duration.toLong()

        when(animator) {
            AnimatorConst.DECELERATE -> interpolator = DecelerateInterpolator()
            AnimatorConst.ACCELERATE -> interpolator = AccelerateInterpolator()
            AnimatorConst.ACCELDECEL -> interpolator = AccelerateDecelerateInterpolator()
        }
    }

    private fun filter(alpha: Float) : Float {
        var res = alpha

        if(alpha > 1f)
            res = 1f
        else if(alpha < 0f)
            res = 0f

        return res
    }
}