package com.mandarin.bcu.util.page;

import android.graphics.Point;

import com.mandarin.bcu.util.PP;

import common.CommonStatic.BattleConst;
import common.battle.BattleField;
import common.battle.StageBasis;
import common.battle.entity.Entity;
import common.system.P;
import common.system.SymCoord;
import common.system.VImg;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.system.fake.FakeTransform;
import common.util.Data;
import common.util.ImgCore;
import common.util.Res;
import common.util.pack.NyCastle;
import common.util.stage.Castles;
import common.util.unit.Form;

public interface BattleBox {

    static class BBPainter implements BattleConst {

        private static final double exp = 0.9, sprite = 0.8;
        private static final int road_h = 156; // in p
        private static final int off = 200;
        private static final int DEP = 4;
        private static final int bar = 8, wave = 28, castw = 128, casth = 256;
        private static final int c0y = -130, c1y = -130, c2y = -258;
        private static final int[] cany = new int[]{-134, -134, -134, -250, -250, -134, -134};
        private static final int[] canx = new int[]{0, 0, 0, 64, 64, 0, 0};

        public static void drawNyCast(FakeGraphics gra, int y, int x, double siz, int[] inf) {
            FakeImage bimg = NyCastle.main[2][inf[2]].getImg();
            int bw = bimg.getWidth();
            int bh = bimg.getHeight();
            int cy = (int) (y + c0y * siz);
            gra.drawImage(bimg, x, cy, (int) (bw * siz), (int) (bh * siz));
            bimg = NyCastle.main[0][inf[0]].getImg();
            bw = bimg.getWidth();
            bh = bimg.getHeight();
            cy = (int) (y + c2y * siz);
            gra.drawImage(bimg, x, cy, (int) (bw * siz), (int) (bh * siz));
            bimg = NyCastle.main[1][inf[1]].getImg();
            bw = bimg.getWidth();
            bh = bimg.getHeight();
            cy = (int) (y + c1y * siz);
            gra.drawImage(bimg, x, cy, (int) (bw * siz), (int) (bh * siz));
        }

        public final BattleField bf;

        public int pt = -1;

        protected final OuterBox page;
        protected final BattleBox box;

        public double siz;
        protected double corr, unir; // siz = pix/p;

        private StageBasis sb;
        private int maxW, maxH, minH; // in p
        public int pos;
        private int midh, prew, preh; // in pix

        private P mouse; // in pix
        private P p = new P(0, 0);
        private PP pp = new PP(0, 0);
        private SymCoord sc = new SymCoord(null, 0, 0, 0, 0);

        public BBPainter(OuterBox bip, BattleField bas, BattleBox bb) {
            page = bip;
            bf = bas;
            box = bb;
            maxW = (int) (bas.sb.st.len * ratio + off * 2);
            maxH = 510 * 3;
            minH = 510;
        }

        public void click(Point p, int button) {
        }

        public void draw(FakeGraphics g) {
            int w = box.getWidth();
            int h = box.getHeight();
            sb = bf.sb;
            if (prew != w || preh != h) {
                clear();
                prew = w;
                preh = h;
            }
            regulate();

            ImgCore.set(g);
            setP(box.getWidth(), box.getHeight());
            sb.bg.draw(g, p, pos, midh, siz);
            drawCastle(g);
            drawEntity(g);
            drawBtm(g);
            drawTop(g);
            sb = null;
        }

        public double getX(double x) {
            return (x * ratio + off) * siz + pos;
        }

        public void regulate() {
            int w = box.getWidth();
            int h = box.getHeight();
            if (siz * minH > h * bar / 10)
                siz = 1.0 * h * bar / 10 / minH;
            if (siz * maxH < h)
                siz = 1.0 * h / maxH;
            if (siz * maxW < w)
                siz = 1.0 * w / maxW;
            if (pos > 0)
                pos = 0;
            if (maxW * siz + pos < w)
                pos = (int) (w - maxW * siz);
            midh = h * bar / 10;
            if (midh > siz * minH * 2)
                midh = (int) (siz * minH * 2);

        }

        public void reset() {
            pt = bf.sb.time;
            box.reset();
        }

        private void adjust(int w, int s) {
            pos += w;
            siz *= Math.pow(exp, s);
        }

        private void setP(double x, double y) {
            p.x = x;
            p.y = y;
        }

        private void setPP(double x, double y) {
            pp.x = x;
            pp.y = y;
        }

        private void setSym(FakeGraphics g, double r, double x, double y, int t) {
            sc.g = g;
            sc.r = r;
            sc.x = x;
            sc.y = y;
            sc.type = t;
        }

        private void setPP(Point p) {
            pp.x = p.x;
            pp.y = p.y;
        }

        private void clear() {
            pt = -1;
            siz = 0;
            pos = 0;
            midh = 0;
        }

        private synchronized void drag(Point p) {
            if (mouse != null) {
                setPP(p);
                adjust((int) (pp.x - mouse.x), 0);
                mouse.setTo(pp);
                reset();
            }
        }

        private void drawBtm(FakeGraphics g) {
            int w = box.getWidth();
            int h = box.getHeight();
            int cw = 0;
            int time = (sb.time / 5) % 2;
            int mtype = sb.mon < sb.next_lv ? 0 : time == 0 ? 1 : 2;
            if (sb.work_lv == 8)
                mtype = 2;
            FakeImage left = Res.battle[0][mtype].getImg();
            int ctype = sb.can == sb.max_can && time == 0 ? 1 : 0;
            FakeImage right = Res.battle[1][ctype].getImg();
            cw += left.getWidth();
            cw += right.getWidth();
            cw += Res.slot[0].getImg().getWidth() * 5;
            double r = 1.0 * w / cw;
            double avah = (h * (10 - bar) / 10f);
            double hr = avah / left.getHeight();
            corr = hr = Math.min(r, hr);
            int ih = (int) (hr * left.getHeight());
            int iw = (int) (hr * left.getWidth());
            g.drawImage(left, 0, h - ih, iw, ih);
            iw = (int) (hr * right.getWidth());
            ih = (int) (hr * right.getHeight());
            g.drawImage(right, w - iw, h - ih, iw, ih);
            setSym(g, hr, hr * 5, h - hr * 5, 2);
            Res.getCost(sb.next_lv, mtype > 0, sc);
            setSym(g, hr, hr * 5, h - hr * 130, 0);
            Res.getWorkerLv(sb.work_lv, mtype > 0, sc);
            int hi = h;
            double marg = 0;
            if (ctype == 0)
                for (int i = 0; i < 10 * sb.can / sb.max_can; i++) {
                    FakeImage img = Res.battle[1][2 + i].getImg();
                    iw = (int) (hr * img.getWidth());
                    ih = (int) (hr * img.getHeight());
                    marg += hr * img.getHeight() - ih;
                    if (marg > 0.5) {
                        marg--;
                        ih++;
                    }
                    hi -= ih;
                    g.drawImage(img, w - iw, hi, iw, ih);
                }
            hr = avah * 1.2 / 2 / Res.slot[0].getImg().getHeight();
            hr = Math.min(r, hr);
            for (int i = 0; i < 10; i++) {
                Form f = sb.b.lu.fs[i / 5][i % 5];

                FakeImage img = f == null ? Res.slot[0].getImg() : f.anim.uni.getImg();

                iw = (int) (hr * img.getWidth());
                ih = (int) (hr * img.getHeight());
                int x = (w - iw * 5) / 2 + iw * (i % 5);
                int y = h - ih * (2 - i / 5);
                g.drawImage(img, x, y, iw, ih);
                if (f == null)
                    continue;
                int pri = sb.elu.price[i / 5][i % 5];
                if (pri == -1)
                    g.colRect(x, y, iw, ih, 255, 0, 0, 100);
                int cool = sb.elu.cool[i / 5][i % 5];
                boolean b = pri > sb.mon || cool > 0;
                if (b)
                    g.colRect(x, y, iw, ih, 0, 0, 0, 100);
                if (sb.locks[i / 5][i % 5])
                    g.colRect(x, y, iw, ih, 0, 255, 0, 100);
                if (cool > 0) {
                    int dw = (int) (hr * 10);
                    int dh = (int) (hr * 12);
                    double cd = 1.0 * cool / sb.elu.maxC[i / 5][i % 5];
                    int xw = (int) (cd * (iw - dw * 2));
                    g.colRect(x + iw - dw - xw, y + ih - dh * 2, xw, dh, 0, 0, 0, 255);
                    g.colRect(x + dw, y + ih - dh * 2, iw - dw * 2 - xw, dh, 100, 212, 255, 255);
                } else {
                    setSym(g, hr, x += iw, y += ih, 3);
                    Res.getCost(pri, !b, sc);
                }
            }
            unir = hr;
        }

        private void drawCastle(FakeGraphics gra) {
            FakeTransform at = gra.getTransform();
            boolean drawCast = sb.ebase instanceof Entity;
            int posy = (int) (midh - road_h * siz);
            int posx = (int) ((800 * ratio + off) * siz + pos);
            if (!drawCast) {
                int cind = sb.st.getCastle();
                if (cind == -1)
                    cind = 0;
                VImg cast = Castles.getCastle(cind);
                FakeImage bimg = cast.getImg();
                int bw = (int) (bimg.getWidth() * siz);
                int bh = (int) (bimg.getHeight() * siz);
                gra.drawImage(bimg, posx - bw, posy - bh, bw, bh);
            } else {
                setP(posx, posy);
                ((Entity) sb.ebase).anim.draw(gra, p, siz * sprite);
            }
            gra.setTransform(at);
            posx -= castw * siz / 2;
            posy -= casth * siz;
            setSym(gra, siz, posx, posy, 0);
            Res.getBase(sb.ebase, sc);
            posx = (int) (((sb.st.len - 800) * ratio + off) * siz + pos);
            drawNyCast(gra, (int) (midh - road_h * siz), posx, siz, sb.nyc);
            posx += castw * siz / 2;
            setSym(gra, siz, posx, posy, 1);
            Res.getBase(sb.ubase, sc);
            gra.delete(at);
        }

        private void drawEntity(FakeGraphics gra) {
            int w = box.getWidth();
            int h = box.getHeight();
            FakeTransform at = gra.getTransform();
            double psiz = siz * sprite;
            ImgCore.battle = true;
            for (int i = 0; i < 10; i++) {
                int dep = i * DEP;
                for (int j = 0; j < sb.le.size(); j++) {
                    if (sb.le.get(j).layer == i && (sb.s_stop == 0 || (sb.le.get(j).getAbi() & Data.AB_TIMEI) == 0)) {
                        gra.setTransform(at);
                        double p = getX(sb.le.get(j).pos);
                        double y = midh - (road_h - dep) * siz;
                        setP(p, y);
                        sb.le.get(j).anim.draw(gra, this.p, psiz);
                        gra.setTransform(at);
                        setP(p, y);
                        sb.le.get(j).anim.drawEff(gra, this.p, siz);
                    }
                }
                for (int j = 0; j < sb.lw.size(); j++) {
                    if (sb.lw.get(j).layer == i) {
                        gra.setTransform(at);
                        double p = (sb.lw.get(j).pos * ratio + off - wave) * siz + pos;
                        double y = midh - (road_h - DEP * sb.lw.get(j).layer) * siz;
                        setP(p, y);
                        sb.lw.get(j).draw(gra, this.p, psiz);
                    }
                }
                for (int j = 0; j < sb.lea.size(); j++) {
                    if (sb.lea.get(j).layer == i) {
                        gra.setTransform(at);
                        double p = getX(sb.lea.get(j).pos);
                        double y = midh - (road_h - DEP * sb.lea.get(j).layer) * siz;
                        setP(p, y);
                        sb.lea.get(j).draw(gra, this.p, psiz);
                    }
                }
            }

            gra.setTransform(at);
            int can = cany[sb.canon.id];
            int disp = canx[sb.canon.id];
            setP(getX(sb.ubase.pos) + disp * siz, midh + (can - road_h) * siz);
            sb.canon.drawBase(gra, p, psiz);
            gra.setTransform(at);
            setP(getX(sb.canon.pos), midh - road_h * siz);
            sb.canon.drawAtk(gra, p, psiz);
            gra.setTransform(at);
            if (sb.sniper != null && sb.sniper.enabled) {
                setP(getX(sb.sniper.getPos()), midh - road_h * siz);
                sb.sniper.drawBase(gra, p, psiz);
                gra.setTransform(at);
            }

            if (sb.s_stop > 0) {
                gra.setComposite(FakeGraphics.GRAY, 0, 0);
                gra.fillRect(0, 0, w, h);
                for (int i = 0; i < 10; i++) {
                    int dep = i * DEP;
                    for (int j = 0; j < sb.le.size(); j++) {
                        if (sb.le.get(j).layer == i && (sb.le.get(j).getAbi() & Data.AB_TIMEI) > 0) {
                            gra.setTransform(at);
                            double p = getX(sb.le.get(j).pos);
                            double y = midh - (road_h - dep) * siz;
                            setP(p, y);
                            sb.le.get(j).anim.draw(gra, this.p, psiz);
                            gra.setTransform(at);
                            setP(p, y);
                            sb.le.get(j).anim.drawEff(gra, this.p, siz);
                        }
                    }
                }
            }
            gra.setTransform(at);
            gra.delete(at);
            ImgCore.battle = false;
        }

        private void drawTop(FakeGraphics g) {
            int w = box.getWidth();
            setSym(g, 1.5, w, 0, 1);
            P p = Res.getMoney((int) sb.mon, sb.max_mon, sc);
            int ih = (int) p.y;
            int n = 0;
            FakeImage bimg = Res.battle[2][1].getImg();
            int cw = bimg.getWidth();
            if ((sb.conf[0] & 2) > 0) {
                bimg = Res.battle[2][sb.sniper.enabled ? 2 : 4].getImg();
                g.drawImage(bimg, w - cw, ih);
                n++;
            }
            bimg = Res.battle[2][1].getImg();
            if ((sb.conf[0] & 1) > 0) {
                g.drawImage(bimg, w - cw * (n + 1), ih);
                n++;
            }
            bimg = Res.battle[2][page.getSpeed() > 0 ? 0 : 3].getImg();
            for (int i = 0; i < Math.abs(page.getSpeed()); i++)
                g.drawImage(bimg, w - cw * (i + 1 + n), ih);
        }

        private synchronized void press(Point p) {
            mouse = new PP(p);
        }

        private synchronized void release(Point p) {
            mouse = null;
        }

        private synchronized void wheeled(Point p, int ind) {
            int w = box.getWidth();
            int h = box.getHeight();
            double psiz = siz * Math.pow(exp, ind);
            if (psiz * minH > h * bar / 10 || psiz * maxH < h || psiz * maxW < w)
                return;
            int dif = -(int) ((p.x - pos) * (Math.pow(exp, ind) - 1));
            adjust(dif, ind);
            reset();
        }

    }

    static interface OuterBox extends RetFunc {

        public int getSpeed();

    }

    public default void click(Point p, int button) {
        getPainter().click(p, button);
    }

    public default void drag(Point p) {
        getPainter().drag(p);
    }

    public int getHeight();

    public BBPainter getPainter();

    public int getWidth();

    public void paint();

    public default void press(Point p) {
        getPainter().press(p);
    }

    public default void release(Point p) {
        getPainter().release(p);
    }

    public void reset();

    public default void wheeled(Point p, int ind) {
        getPainter().wheeled(p, ind);
    }

}
