package com.mandarin.bcu.util.page;

import android.graphics.Point;
import android.view.MotionEvent;

import com.mandarin.bcu.util.PP;

import common.CommonStatic;
import common.battle.SBCtrl;
import common.system.P;
import common.system.fake.FakeImage;
import common.util.unit.Form;

public class BBCtrl extends BattleBox.BBPainter {
    private final SBCtrl ctrl;
    public static final int ACTION_LONG = 100;
    public static final int ACTION_LINEUP_CHANGE_UP = 10;
    public static final int ACTION_LINEUP_CHANGE_DOWN = 20;

    private final float dpi;
    private final double cutout;

    public BBCtrl(BattleBox.OuterBox bip, SBCtrl bas, BattleBox bb, float dpi, double cutout) {
        super(bip, bas, bb);
        ctrl = bas;
        this.dpi = dpi;
        this.cutout = cutout;
    }

    @Override
    public void click(Point p, int action) {
        CommonStatic.BCAuxAssets aux = CommonStatic.getBCAssets();

        if (action == MotionEvent.ACTION_UP) {
            int w = box.getWidth();
            int h = box.getHeight();
            double hr = unir;
            double term = hr * aux.slot[0].getImg().getWidth() * 0.2;

            if(CommonStatic.getConfig().twoRow) {
                double termh = hr * aux.slot[0].getImg().getHeight() * 0.1;

                for(int i = 0; i < 2; i++) {
                    for(int j = 0; j < 5; j++) {
                        Form f = ctrl.sb.b.lu.fs[i][j];

                        FakeImage img = f == null ? aux.slot[0].getImg() : f.anim.getUni().getImg();

                        int iw = (int) (hr * img.getWidth());
                        int ih = (int) (hr * img.getHeight());

                        int x = (w - iw * 5) / 2 + iw * j + (int) (term * (j -2) + (ctrl.sb.frontLineup == 0 ? 0 : term/2));
                        int y = (int) (h - (2 - i) * (ih + termh));

                        if (!new PP(p).out(new P(x, y), new P(x + iw, y + ih), 0))
                            ctrl.action.add(j + i * 5);
                    }
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    Form f = ctrl.sb.b.lu.fs[ctrl.sb.frontLineup][i];

                    FakeImage img = f == null ? aux.slot[0].getImg() : f.anim.getUni().getImg();

                    int iw = (int) (hr * img.getWidth());
                    int ih = (int) (hr * img.getHeight());

                    int x = (w - iw * 5) / 2 + iw * i + (int) (term * (i -2) + (ctrl.sb.frontLineup == 0 ? 0 : term/2));
                    int y = h - (int) (ih * 1.1);

                    if (!new PP(p).out(new P(x, y), new P(x + iw, y + ih), 0))
                        ctrl.action.add(i+ctrl.sb.frontLineup*5);
                }
            }

            hr = corr;
            FakeImage left = aux.battle[0][0].getImg();
            FakeImage right = aux.battle[1][0].getImg();

            float ratio = dpi/42f;

            int ih = (int) (hr * left.getHeight());
            int iw = (int) (hr * left.getWidth());
            if (!new PP(p).out(new P(cutout - BOTTOM_GAP * hr, h - ih), new P(cutout - BOTTOM_GAP * hr + iw, h), 0))
                ctrl.action.add(-1);

            iw = (int) (hr * right.getWidth());
            ih = (int) (hr * right.getHeight());
            if (!new PP(p).out(new P(w - iw - cutout + BOTTOM_GAP * hr, h - ih), new P(w - cutout + BOTTOM_GAP * hr, h), 0))
                ctrl.action.add(-2);

            if ((ctrl.sb.conf[0] & 2) > 0) {
                FakeImage bimg = CommonStatic.getBCAssets().battle[2][1].getImg();
                int cw = (int)(bimg.getWidth()*ratio);
                int ch = (int)(bimg.getHeight()*ratio);
                int mh = (int)(CommonStatic.getBCAssets().num[0][0].getImg().getHeight()*ratio);
                if (!new PP(p).out(new P(w - cw - cutout, mh), new P(w - cutout, mh + ch), 0))
                    ctrl.action.add(-3);
            }
        } else if (action == ACTION_LONG) {
            int w = box.getWidth();
            int h = box.getHeight();

            double hr = unir;
            double term = hr * aux.slot[0].getImg().getWidth() * 0.2;

            if(CommonStatic.getConfig().twoRow) {
                double termh = hr * aux.slot[0].getImg().getHeight() * 0.1;

                for(int i = 0; i < 2; i++) {
                    for(int j = 0; j < 5; j++) {
                        Form f = ctrl.sb.b.lu.fs[i][j];

                        FakeImage img = f == null ? aux.slot[0].getImg() : f.anim.getUni().getImg();

                        int iw = (int) (hr * img.getWidth());
                        int ih = (int) (hr * img.getHeight());

                        int x = (w - iw * 5) / 2 + iw * j + (int) (term * (j -2) + (ctrl.sb.frontLineup == 0 ? 0 : term/2));
                        int y = (int) (h - (2 - i) * (ih + termh));

                        if (!new PP(p).out(new P(x, y), new P(x + iw, y + ih), 0))
                            ctrl.action.add(j + i * 5);

                        ctrl.action.add(10);
                    }
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    Form f = ctrl.sb.b.lu.fs[ctrl.sb.frontLineup][i];

                    FakeImage img = f == null ? aux.slot[0].getImg() : f.anim.getUni().getImg();

                    int iw = (int) (hr * img.getWidth());
                    int ih = (int) (hr * img.getHeight());

                    int x = (w - iw * 5) / 2 + iw * i + (int) (term * (i -2) + (ctrl.sb.frontLineup == 0 ? 0 : term/2));
                    int y = h - (int) (ih * 1.1);

                    if (!new PP(p).out(new P(x, y), new P(x + iw, y + ih), 0))
                        ctrl.action.add(i+ctrl.sb.frontLineup*5);

                    ctrl.action.add(10);
                }
            }
        }

        reset();
    }

    public void perform(int action) {
        if (action == ACTION_LINEUP_CHANGE_UP) {
            ctrl.action.add(-4);
        } else if(action == ACTION_LINEUP_CHANGE_DOWN) {
            ctrl.action.add(-5);
        }

        reset();
    }
}
