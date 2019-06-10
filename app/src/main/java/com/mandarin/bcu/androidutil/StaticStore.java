package com.mandarin.bcu.androidutil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import com.mandarin.bcu.util.basis.Treasure;
import com.mandarin.bcu.util.unit.Unit;

import java.util.List;

public class StaticStore {
    public static Bitmap[] bitmaps = null;
    public static String[] names = null;
    public static List<Unit> units = null;
    public static Treasure t = null;
    public static Drawable[] icons = null;
    public static Drawable[] picons = null;
    public static String [] addition = null;

    public static Bitmap getResize(Drawable drawable, Context context) {
        float dp = 32f;
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,r.getDisplayMetrics());
        Bitmap b = ((BitmapDrawable)drawable).getBitmap();
        return Bitmap.createScaledBitmap(b,(int)px,(int)px,false);
    }

    public static Bitmap getResizeb(Bitmap b, Context context, float dp) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,r.getDisplayMetrics());
        return Bitmap.createScaledBitmap(b,(int)px,(int)px,false);
    }
}
