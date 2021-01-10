package com.mandarin.bcu.androidutil.supports

import android.content.Context
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class AutoMarquee : AppCompatTextView {
    constructor(context: Context) : super(context) {
        isSingleLine = true
        ellipsize = TextUtils.TruncateAt.MARQUEE
        marqueeRepeatLimit = -1
        setHorizontallyScrolling(true)
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        isSingleLine = true
        ellipsize = TextUtils.TruncateAt.MARQUEE
        marqueeRepeatLimit = -1
        setHorizontallyScrolling(true)
    }

    constructor(context: Context, attributeSet: AttributeSet?, i: Int) : super(context, attributeSet, i) {
        isSingleLine = true
        ellipsize = TextUtils.TruncateAt.MARQUEE
        marqueeRepeatLimit = -1
        setHorizontallyScrolling(true)
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        if (focused)
            super.onFocusChanged(true, direction, previouslyFocusedRect)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        if (hasWindowFocus)
            super.onWindowFocusChanged(true)
    }

    override fun isFocused(): Boolean {
        return if(needFocus) {
            super.isFocused()
        } else {
            true
        }
    }

    var needFocus = false
}