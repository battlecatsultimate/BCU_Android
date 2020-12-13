package com.mandarin.bcu.util.page;

import android.graphics.Point;

import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics;

import java.text.DecimalFormat;

import common.CommonStatic;
import common.CommonStatic.BCAuxAssets;
import common.CommonStatic.BattleConst;
import common.battle.BattleField;
import common.battle.StageBasis;
import common.battle.entity.Entity;
import common.battle.entity.WaprCont;
import common.pack.Identifier;
import common.system.P;
import common.system.SymCoord;
import common.system.VImg;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.system.fake.FakeTransform;
import common.util.Data;
import common.util.ImgCore;
import common.util.Res;
import common.util.stage.CastleImg;
import common.util.unit.Form;

public interface BattleBox {

    class BBPainter implements BattleConst {

        private static final double sprite = 0.8;
        private static final int road_h = 156; // in p
        private static final int off = 200;
        private static final int DEP = 4;
        private static final int wave = 28, castw = 128, casth = 256;
        private static final int c0y = -130, c1y = -130, c2y = -258;
        private static final int[] cany = new int[] { -134, -134, -134, -250, -250, -134, -134, -134 };
        private static final int[] canx = new int[] { 0, 0, 0, 64, 64, 0, 0, 0 };
        private static final DecimalFormat df = new DecimalFormat("00.00");
        private static final double bar = 8;

        public static void drawNyCast(FakeGraphics gra, int y, int x, double siz, int[] inf) {
            BCAuxAssets aux = CommonStatic.getBCAssets();
            FakeImage bimg = aux.main[2][inf[2]].getImg();
            int bw = bimg.getWidth();
            int bh = bimg.getHeight();
            int cy = (int) (y + c0y * siz);
            gra.drawImage(bimg, x, cy, (int) (bw * siz), (int) (bh * siz));
            bimg = aux.main[0][inf[0]].getImg();
            bw = bimg.getWidth();
            bh = bimg.getHeight();
            cy = (int) (y + c2y * siz);
            gra.drawImage(bimg, x, cy, (int) (bw * siz), (int) (bh * siz));
            bimg = aux.main[1][inf[1]].getImg();
            bw = bimg.getWidth();
            bh = bimg.getHeight();
            cy = (int) (y + c1y * siz);
            gra.drawImage(bimg, x, cy, (int) (bw * siz), (int) (bh * siz));
        }

        public final BattleField bf;

        public int pt = -1;

        protected final OuterBox page;
        protected final BattleBox box;

        public double siz, corr, unir; // siz = pix/p;

        private StageBasis sb;
        private final int maxW;
        public int pos, midh, prew, preh; // in pix

        private double minSiz = -1;
        private double maxSiz = -1;

        private double groundHeight = -1;

        public boolean up = false;

        public int dpi = -1;

        private final BCAuxAssets aux = CommonStatic.getBCAssets();

        public SymCoord sym = new SymCoord(null, 0, 0, 0, 0);
        public P p = new P(0, 0);

        public BBPainter(OuterBox bip, BattleField bas, BattleBox bb) {
            page = bip;
            bf = bas;
            box = bb;
            maxW = (int) (bas.sb.st.len * ratio + off * 2);
        }

        public void click(Point p, int button) {
        }

        public void draw(FakeGraphics g) {
            int w = box.getWidth();
            int h = box.getHeight();

            calculateSiz(w, h);

            sb = bf.sb;
            if (prew != w || preh != h) {
                clear();
                prew = w;
                preh = h;
            }
            regulate();

            if(sb.s_stop == 0 && ((CVGraphics)g).neg) {
                //Set p0 to 1 to change negative effect
                g.setComposite(CVGraphics.POSITIVE, 1, 0);
            }

            ImgCore.set(g);
            setP(box.getWidth(), box.getHeight());
            sb.bg.draw(g, p, pos, midh, siz);
            drawCastle(g);
            if(sb.can == sb.max_can && sb.canon.id == 0) {
                drawCannonRange(g);
            }
            drawEntity(g);
            drawBtm(g);
            drawTop(g);
            if(bf.sb.st.timeLimit != 0) {
                drawTime(g);
            }
            sb = null;
        }

        private void setSym(FakeGraphics g, double r, double x, double y, int t) {
            sym.g = g;
            sym.r = r;
            sym.x = x;
            sym.y = y;
            sym.type = t;
        }

        private void setP(double x, double y) {
            p.x = x;
            p.y = y;
        }

        public double getX(double x) {
            return (x * ratio + off) * siz + pos;
        }

        public void calculateSiz(int w, int h) {
            minSiz = 0;
            maxSiz = Double.MAX_VALUE;

            minSiz = getReulatedSiz(minSiz, w, h);
            maxSiz = getReulatedSiz(maxSiz, w, h);

            groundHeight = (h * 2 / 10.0) * (maxSiz - minSiz);
        }

        private double getReulatedSiz(double siz, int w, int h) {
            // in p
            int minH = 510;
            if (siz * minH > h)
                siz = 1.0 * h / minH;
            int maxH = 510 * 3;
            if (siz * maxH < h)
                siz = 1.0 * h / maxH;
            if (siz * maxW < w)
                siz = 1.0 * w / maxW;

            return siz;
        }

        public void regulate() {
            int w = box.getWidth();
            int h = box.getHeight();
            if (siz < minSiz)
                siz = minSiz;
            if (siz >= maxSiz)
                siz = maxSiz;
            if (pos > 0)
                pos = 0;
            if (maxW * siz + pos < w)
                pos = (int) (w - maxW * siz);
            midh = h + (int) (groundHeight * (siz - maxSiz) / (maxSiz - minSiz));

        }

        public void reset() {
            pt = bf.sb.time;
            box.reset();
        }

        private void clear() {
            pt = -1;
            siz = 0;
            pos = 0;
            midh = 0;
        }

        private void drawBtm(FakeGraphics g) {
            g.setComposite(CVGraphics.POSITIVE, 0, 0);
            int w = box.getWidth();
            int h = box.getHeight();
            int cw = 0;
            int time = (sb.time / 5) % 2;
            int mtype = sb.mon < sb.next_lv ? 0 : time == 0 ? 1 : 2;
            if (sb.work_lv == 8)
                mtype = 2;
            FakeImage left = aux.battle[0][mtype].getImg();
            int ctype = sb.can == sb.max_can && time == 0 ? 1 : 0;
            FakeImage right = aux.battle[1][ctype].getImg();
            cw += left.getWidth();
            cw += right.getWidth();
            cw += aux.slot[0].getImg().getWidth() * 5;
            double r = 1.0 * w / cw;
            double avah = h * (10 - bar) / 10.0;
            double hr = avah / left.getHeight();
            corr = hr = Math.min(r, hr);
            int ih = (int) (hr * left.getHeight());
            int iw = (int) (hr * left.getWidth());
            g.drawImage(left, 0, h - ih, iw, ih);
            iw = (int) (hr * right.getWidth());
            ih = (int) (hr * right.getHeight());
            g.drawImage(right, w - iw, h - ih, iw, ih);
            setSym(g, hr,  hr*5, h-hr*5, 2);
            Res.getCost(sb.next_lv, mtype > 0, sym);
            setSym(g, hr, hr * 5, h-hr*130, 0);
            Res.getWorkerLv(sb.work_lv, mtype > 0, sym);
            int hi = h;
            double marg = 0;
            if (ctype == 0)
                for (int i = 0; i < 10 * sb.can / sb.max_can; i++) {
                    FakeImage img = aux.battle[1][2 + i].getImg();
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
            if(sb.can == sb.max_can) {
                FakeImage fire = aux.battle[1][getFireLang()+ctype].getImg();

                int fw = (int) (hr * fire.getWidth());
                int fh = (int) (hr * fire.getHeight());

                g.drawImage(fire, w - fw - 4 * hr, h - fh - 4 * hr, fw, fh);
            }
            //Decide lineup icon's size, 0.675 is guessed value by comparing BC and BCU
            hr = avah * 0.675 / aux.slot[0].getImg().getHeight();
            //Make lineup won't cover cannon button and money upgrade button
            hr = Math.min(hr, (box.getWidth()-iw*2.0)/aux.slot[0].getImg().getWidth()/5.9);
            double term = hr * aux.slot[0].getImg().getWidth() * 0.2;

            if(sb.isOneLineup) {
                drawLineup(g, w, h, hr, term, false, 0);
            } else {
                drawLineup(g, w, h, hr, term, true, 1-sb.frontLineup);
                drawLineup(g, w, h, hr, term, false, sb.frontLineup);
            }

            unir = hr;
        }

        private int getFireLang() {
            switch (CommonStatic.getConfig().lang) {
                case 1:
                    return 18;
                case 2:
                    return 16;
                case 3:
                    return 12;
                default:
                    return 14;
            }
        }

        private void drawLineup(FakeGraphics g, int w, int h, double hr, double term, boolean isBehind, int index) {
            int iw;
            int ih;
            for (int i = 0; i < 5; i++) {
                Form f = sb.b.lu.fs[index][i];
                FakeImage img = f == null ? aux.slot[0].getImg() : f.anim.getUni().getImg();
                iw = (int) (hr * img.getWidth());
                ih = (int) (hr * img.getHeight());
                int x = (w - iw * 5) / 2 + iw * (i % 5) + (int) (term * ((i % 5) - 2) + (index == 0 ? 0 : (term / 2)));
                int y = h - ih - (isBehind ? 0 : (int) (ih * 0.1));

                //Check if lineup is changing
                if(sb.changeFrame != -1) {
                    if(sb.changeFrame >= sb.changeDivision) {
                        double dis = isBehind ? ih * 0.5 : up ? ih * 0.4 : ih * 0.6;

                        y += (dis / sb.changeDivision) * (sb.changeDivision * 2 - sb.changeFrame) * (isBehind ? 1 : -1) * (up ? 1 : -1);
                    } else {
                        double dis = isBehind ? ih * 0.5 : up ? ih * 0.6 : ih * 0.4;

                        y +=  (dis - (dis / sb.changeDivision) * (sb.changeDivision - sb.changeFrame)) * (isBehind ? -1 : 1) * (up ? 1 : -1);
                    }
                }

                g.drawImage(img, x, y, iw, ih);
                if (f == null)
                    continue;
                int pri = sb.elu.price[index][i % 5];
                if (pri == -1)
                    g.colRect(x, y, iw, ih, 255, 0, 0, 100);
                int cool = sb.elu.cool[index][i % 5];
                boolean b = isBehind || pri > sb.mon || cool > 0;
                if (b)
                    g.colRect(x, y, iw, ih, 0, 0, 0, 100);
                if (sb.locks[index][i % 5])
                    g.colRect(x, y, iw, ih, 0, 255, 0, 100);
                if(!isBehind) {
                    if (cool > 0) {
                        int dw = (int) (hr * 10);
                        int dh = (int) (hr * 12);
                        double cd = 1.0 * cool / sb.elu.maxC[index][i % 5];
                        int xw = (int) (cd * (iw - dw * 2));
                        g.colRect(x + iw - dw - xw, y + ih - dh * 2, xw, dh, 0, 0, 0, 255);
                        g.colRect(x + dw, y + ih - dh * 2, iw - dw * 2 - xw, dh, 100, 212, 255, 255);
                    } else {
                        setSym(g, hr, x + iw, y + ih, 3);
                        Res.getCost(pri, !b, sym);
                    }
                }
            }

            if(((CVGraphics)g).neg) {
                g.setComposite(FakeGraphics.GRAY, 0, 0);
            }
        }

        private void drawCannonRange(FakeGraphics g) {
            FakeImage range = aux.battle[1][20].getImg();
            FakeImage cann = aux.battle[1][21].getImg();

            double rang = sb.ubase.pos + 100 + 56 * 4;

            for(int i = 0; i < sb.b.t().tech[Data.LV_CRG]+2; i++) {
                rang -= 405;
            }

            rang = getX(rang);

            double rw = range.getWidth() * 0.75 * siz;
            double rh = range.getHeight()  * 0.85 * siz;

            //102 is guessed value, making range indicator on ground
            g.drawImage(range, rang, midh - rh - 102 * siz, rw, rh);

            int rtime = (int) (sb.time / 1.5) % 4;

            double canw = cann.getWidth() * 0.75 * siz;
            double canh = cann.getHeight() * 0.75 * siz;

            g.drawImage(cann, rang + rw / 2.0 - canw / 2.0, midh - canh - rh - 102 * siz - Math.abs(rtime - 2) * 8 * siz, canw, canh);
        }

        private void drawCastle(FakeGraphics gra) {
            FakeTransform at = gra.getTransform();
            boolean drawCast = sb.ebase instanceof Entity;
            int posy = (int) (midh - road_h * siz);
            int posx = (int) ((800 * ratio + off) * siz + pos);
            if (!drawCast) {
                Identifier<CastleImg> cind = sb.st.castle;
                VImg cast = Identifier.getOr(cind, CastleImg.class).img;
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
            Res.getBase(sb.ebase, sym, bf.sb.st.trail);
            posx = (int) (((sb.st.len - 800) * ratio + off) * siz + pos);
            drawNyCast(gra, (int) (midh - road_h * siz), posx, siz, sb.nyc);
            posx += castw * siz / 2;
            setSym(gra, siz, posx, posy, 1);
            Res.getBase(sb.ubase, sym, false);
            gra.delete(at);
        }

        private void drawEntity(FakeGraphics gra) {
            int w = box.getWidth();
            int h = box.getHeight();
            FakeTransform at = gra.getTransform();
            double psiz = siz * sprite;
            CommonStatic.getConfig().battle = true;
            for (int i = 0; i < 10; i++) {
                int dep = i * DEP;
                for (int j = 0; j < sb.le.size(); j++)
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
                for (int j = 0; j < sb.lw.size(); j++)
                    if (sb.lw.get(j).layer == i) {
                        gra.setTransform(at);
                        double p = (sb.lw.get(j).pos * ratio + off - wave) * siz + pos;
                        double y = midh - (road_h - DEP * sb.lw.get(j).layer) * siz;
                        setP(p, y);
                        sb.lw.get(j).draw(gra, this.p, psiz);
                    }
                for (int j = 0; j < sb.lea.size(); j++)
                    if (sb.lea.get(j).layer == i) {
                        gra.setTransform(at);
                        double p = getX(sb.lea.get(j).pos);
                        double y = midh - (road_h - DEP * sb.lea.get(j).layer) * siz;

                        if (sb.lea.get(j) instanceof WaprCont) {
                            double dx = ((WaprCont) sb.lea.get(j)).dire == -1 ? -27 * siz : -24 * siz;
                            setP(p+dx, y-24*siz);
                        } else {
                            setP(p, y);
                        }

                        sb.lea.get(j).draw(gra, this.p, psiz);

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
                setP(sb.sniper.getPos(), midh-road_h*siz);
                sb.sniper.drawBase(gra, p, psiz);
                gra.setTransform(at);
            }

            if (sb.s_stop > 0) {
                gra.setComposite(FakeGraphics.GRAY, 0, 0);
                gra.fillRect(0, 0, w, h);
                for (int i = 0; i < 10; i++) {
                    int dep = i * DEP;
                    for (int j = 0; j < sb.le.size(); j++)
                        if (sb.le.get(j).layer == i && (sb.le.get(j).getAbi() & Data.AB_TIMEI) > 0) {
                            if(((CVGraphics)gra).neg) {
                                gra.setComposite(CVGraphics.POSITIVE, 0, 0);
                            }
                            gra.setTransform(at);
                            double p = getX(sb.le.get(j).pos);
                            double y = midh - (road_h - dep) * siz;
                            setP(p, y);
                            sb.le.get(j).anim.draw(gra, this.p, psiz);
                            gra.setTransform(at);
                            setP(p, y);
                            sb.le.get(j).anim.drawEff(gra, this.p, siz);

                            if(((CVGraphics)gra).neg) {
                                gra.setComposite(FakeGraphics.GRAY, 0, 0);
                            }
                        }
                }
            }
            gra.setTransform(at);
            gra.delete(at);
            CommonStatic.getConfig().battle = false;
        }

        private void drawTop(FakeGraphics g) {
            g.setComposite(CVGraphics.POSITIVE, 0, 0);
            int w = box.getWidth();
            double ratio = dpi / 42.0;
            setSym(g, ratio, w-aux.num[0][0].getImg().getWidth()*0.2, aux.num[0][0].getImg().getHeight()*0.2, 1);
            P p = Res.getMoney((int) sb.mon, sb.max_mon, sym);
            int ih = (int) p.y + (int) (aux.num[0][0].getImg().getHeight()*0.2);
            int n = 0;
            FakeImage bimg = aux.battle[2][1].getImg();
            int cw = (int) (bimg.getWidth() * ratio);
            if ((sb.conf[0] & 2) > 0 && sb.sniper != null) {
                bimg = aux.battle[2][sb.sniper.enabled ? 2 : 4].getImg();
                g.drawImage(bimg, w - cw, ih, dpi, dpi);
                n++;
            }
            bimg = aux.battle[2][1].getImg();
            if ((sb.conf[0] & 1) > 0) {
                g.drawImage(bimg, w - cw * (n + 1), ih, dpi, dpi);
                n++;
            }
            bimg = aux.battle[2][page.getSpeed() > 0 ? 0 : 3].getImg();
            for (int i = 0; i < Math.abs(page.getSpeed()); i++)
                g.drawImage(bimg, w - cw * (i + 1 + n), ih, dpi, dpi);

            if(((CVGraphics)g).neg) {
                g.setComposite(FakeGraphics.GRAY, 0, 0);
            }
        }

        private void drawTime(FakeGraphics g) {
            setP(dpi * 16.0 / 32.0, dpi * 60.0 / 32.0);
            double ratio = (double) dpi / aux.timer[0].getImg().getHeight();

            double timeLeft = bf.sb.st.timeLimit * 60.0 - bf.sb.time / 30.0;

            int min = (int) timeLeft / 60;

            timeLeft -= min * 60.0;

            FakeImage separator = aux.timer[10].getImg();
            FakeImage zero = aux.timer[0].getImg();

            if(timeLeft < 0) {
                for(int i = 0; i < 3; i ++) {
                    g.drawImage(zero, p.x, p.y, zero.getWidth() * ratio, zero.getHeight() * ratio);
                    p.x += zero.getWidth() * ratio;
                    g.drawImage(zero, p.x, p.y, zero.getWidth() * ratio, zero.getHeight() * ratio);
                    p.x += zero.getWidth() * ratio;
                    if(i != 2) {
                        g.drawImage(separator, p.x, p.y, separator.getWidth() * ratio, separator.getHeight() * ratio);
                        p.x += separator.getWidth() * ratio;
                    }
                }

                return;
            }

            if(min < 10) {
                FakeImage m = aux.timer[min].getImg();

                g.drawImage(zero, p.x, p.y, zero.getWidth() * ratio, zero.getHeight() * ratio);
                p.x += zero.getWidth() * ratio;

                g.drawImage(m, p.x, p.y, m.getWidth()*ratio, m.getHeight()*ratio);
                p.x += m.getWidth() * ratio;
            }

            g.drawImage(separator, p.x, p.y, separator.getWidth() * ratio, separator.getHeight() * ratio);
            p.x += separator.getWidth() * ratio;

            FakeImage m;

            String time = df.format(timeLeft);

            for(int i = 0; i < time.length(); i++) {
                if((time.charAt(i)) == '.') {
                    g.drawImage(separator, p.x, p.y, separator.getWidth() * ratio, separator.getHeight() * ratio);
                    p.x += separator.getWidth() * ratio;
                } else {
                    m = aux.timer[Character.getNumericValue(time.charAt(i))].getImg();

                    g.drawImage(m, p.x, p.y, m.getWidth()*ratio, m.getHeight()*ratio);
                    p.x += m.getWidth() * ratio;
                }
            }

            P.delete(p);
        }
    }

    interface OuterBox extends RetFunc {

        int getSpeed();

    }

    default void click(Point p, int button) {
        getPainter().click(p, button);
    }

    int getHeight();

    BBPainter getPainter();

    int getWidth();

    void paint();

    void reset();

}
