package com.mandarin.bcu.androidutil.supports;

import android.graphics.Point;
import android.graphics.Rect;

import common.system.P;

public strictfp class PP extends P {

    public PP(float d, float e) {
        super(d, e);
    }

    public PP(Point p) {
        super(p.x, p.y);
    }

    @Override
    public PP copy() {
        return new PP(x, y);
    }

    @Override
    public PP divide(P p) {
        x /= p.x;
        y /= p.y;
        return this;
    }

    @Override
    public PP sf(P p) {
        return new PP(p.x - x, p.y - y);
    }

    @Override
    public PP times(float d) {
        x *= d;
        y *= d;
        return this;
    }

    @Override
    public PP times(float hf, float vf) {
        x *= hf;
        y *= vf;
        return this;
    }

    @Override
    public PP times(P p) {
        x *= p.x;
        y *= p.y;
        return this;
    }

    public Point toPoint() {
        return new Point((int) x, (int) y);
    }

    public Rect toRectangle(int w, int h) {
        return new Rect((int) x, (int) y, w, h);
    }

    public Rect toRectangle(P p) {
        return new Rect((int) x, (int) y, (int) p.x, (int) p.y);
    }

}
