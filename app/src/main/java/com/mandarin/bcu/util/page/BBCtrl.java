package com.mandarin.bcu.util.page;

import android.graphics.Point;
import android.view.MotionEvent;

import com.mandarin.bcu.util.PP;

import common.battle.SBCtrl;
import common.system.P;
import common.system.fake.FakeImage;
import common.util.Res;
import common.util.unit.Form;

public class BBCtrl extends BattleBox.BBPainter {
    private SBCtrl ctrl;
    public static final int ACTION_LONG = 100;

    private final float dpi;

    public BBCtrl(BattleBox.OuterBox bip, SBCtrl bas, BattleBox bb, float dpi) {
        super(bip, bas, bb);
        ctrl = bas;
        this.dpi = dpi;
    }

    @Override
    public void click(Point p, int action) {
        if (action == MotionEvent.ACTION_UP) {
            int w = box.getWidth();
            int h = box.getHeight();
            double hr = unir;

            for (int i = 0; i < 10; i++) {
                Form f = ctrl.sb.b.lu.fs[i / 5][i % 5];

                FakeImage img = f == null ? Res.slot[0].getImg() : f.anim.uni.getImg();

                int iw = (int) (hr * img.getWidth());
                int ih = (int) (hr * img.getHeight());
                int x = (w - iw * 5) / 2 + iw * (i % 5);
                int y = h - ih * (2 - i / 5);

                if (!new PP(p).out(new P(x, y), new P(x + iw, y + ih), 0))
                    ctrl.action.add(i);
            }

            hr = corr;
            FakeImage left = Res.battle[0][0].getImg();
            FakeImage right = Res.battle[1][0].getImg();

            float ratio = dpi/58f;

            int ih = (int) (hr * left.getHeight()*ratio);
            int iw = (int) (hr * left.getWidth()*ratio);
            if (!new PP(p).out(new P(0, h - ih), new P(iw, h), 0))
                ctrl.action.add(-1);

            iw = (int) (hr * right.getWidth()*ratio);
            ih = (int) (hr * right.getHeight()*ratio);
            if (!new PP(p).out(new P(w - iw, h - ih), new P(w, h), 0))
                ctrl.action.add(-2);

            if ((ctrl.sb.conf[0] & 2) > 0) {
                FakeImage bimg = Res.battle[2][1].getImg();
                int cw = (int)(bimg.getWidth()*ratio);
                int ch = (int)(bimg.getHeight()*ratio);
                int mh = (int)(Res.num[0][0].getImg().getHeight()*ratio);
                if (!new PP(p).out(new P(w - cw, mh), new P(w, mh + ch), 0))
                    ctrl.action.add(-3);
            }

            reset();
        } else if (action == ACTION_LONG) {
            int w = box.getWidth();
            int h = box.getHeight();
            double hr = unir;

            for (int i = 0; i < 10; i++) {
                Form f = ctrl.sb.b.lu.fs[i / 5][i % 5];

                FakeImage img = f == null ? Res.slot[0].getImg() : f.anim.uni.getImg();

                int iw = (int) (hr * img.getWidth());
                int ih = (int) (hr * img.getHeight());
                int x = (w - iw * 5) / 2 + iw * (i % 5);
                int y = h - ih * (2 - i / 5);

                if (!new PP(p).out(new P(x, y), new P(x + iw, y + ih), 0))
                    ctrl.action.add(i);

                ctrl.action.add(10);
            }

            reset();
        }
    }
}
