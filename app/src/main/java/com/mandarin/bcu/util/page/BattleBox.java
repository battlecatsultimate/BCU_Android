package com.mandarin.bcu.util.page;

import android.graphics.Point;

import com.mandarin.bcu.androidutil.fakeandroid.CVGraphics;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import common.CommonStatic;
import common.CommonStatic.BCAuxAssets;
import common.CommonStatic.BattleConst;
import common.battle.BattleField;
import common.battle.StageBasis;
import common.battle.attack.ContAb;
import common.battle.attack.ContWaveAb;
import common.battle.entity.EAnimCont;
import common.battle.entity.ECastle;
import common.battle.entity.EEnemy;
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
        private static final DecimalFormat df;
        private static final double bar = 8;
        protected static final int BOTTOM_GAP = 48;

        static {
            NumberFormat nf = NumberFormat.getInstance(Locale.US);
            df = (DecimalFormat) nf;
            df.applyPattern("#.##");
        }

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
        @SuppressWarnings("FieldCanBeLocal")
        private final int minH = 510;
        @SuppressWarnings("FieldCanBeLocal")
        private final int maxH = 510 * 3;
        public int pos, midh, prew, preh; // in pix

        private double minSiz = -1;
        private double maxSiz = -1;

        private double groundHeight = -1;

        public int dpi = -1;
        public double cutout = 0;

        private final BCAuxAssets aux = CommonStatic.getBCAssets();

        private final ArrayList<ContAb> efList = new ArrayList<>();

        public SymCoord sym = new SymCoord(null, 0, 0, 0, 0);
        public P p = new P(0, 0);

        public double preSiz = 1.0;
        public int prePos = 0;

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
                siz = preSiz;
                pos = prePos;
            }

            regulate();

            if(sb.s_stop == 0 && ((CVGraphics)g).neg) {
                //Set p0 to 1 to change negative effect
                g.setComposite(CVGraphics.POSITIVE, 1, 0);
            } else if(sb.s_stop > 0) {
                g.setComposite(CVGraphics.GRAY, 0, 0);
            }

            double y = maxH * siz - midh;
            double midY = groundHeight / minSiz;

            if(CommonStatic.getConfig().drawBGEffect)
                sb.registerBattleDimension(midY, h / minSiz);

            if(CommonStatic.getConfig().twoRow)
                midY += (h * 0.75 / 10.0);

            ImgCore.set(g);

            sb.bg.draw(g, setP(box.getWidth(), box.getHeight()), pos, midh, siz, (int) (groundHeight + (CommonStatic.getConfig().twoRow ? (h * 0.75 / 10.0) : 0)));

            if(CommonStatic.getConfig().drawBGEffect && sb.bgEffect != null) {
                sb.bgEffect.preDraw(g, setP(pos, y), siz, midY);
            }

            drawCastle(g);

            if(sb.cannon == sb.maxCannon && sb.canon.id == 0) {
                drawCannonRange(g);
            }

            drawEntity(g);

            if(CommonStatic.getConfig().drawBGEffect && sb.bgEffect != null) {
                sb.bgEffect.postDraw(g, setP(pos, y), siz, midY);
            }

            if(sb.bg.overlay != null) {
                drawBGOverlay(g, midY);
            }

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

        private P setP(double x, double y) {
            p.x = x;
            p.y = y;

            return p;
        }

        public double getX(double x) {
            return (x * ratio + off) * siz + pos;
        }

        public void calculateSiz(int w, int h) {
            minSiz = 0;
            maxSiz = Double.MAX_VALUE;

            minSiz = getReulatedSiz(minSiz, w, h);
            maxSiz = getReulatedSiz(maxSiz, w, h);

            groundHeight = (h * 2 / 10.0) * (1 - minSiz / maxSiz);
        }

        private double getReulatedSiz(double siz, int w, int h) {
            if (siz * minH > h)
                siz = 1.0 * h / minH;
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

            if(CommonStatic.getConfig().twoRow) {
                midh -= h * 0.75 / 10.0;
            }
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
            int time = sb.s_stop == 0 ? (sb.time / 5) % 2 : 1;
            int mtype = sb.money < sb.upgradeCost ? 0 : time == 0 ? 1 : 2;

            if (sb.work_lv == 8)
                mtype = 2;

            FakeImage left = aux.battle[0][mtype].getImg();

            int ctype = sb.cannon == sb.maxCannon && time == 0 ? 1 : 0;

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

            g.drawImage(left, cutout - BOTTOM_GAP * hr, h - ih, iw, ih);

            iw = (int) (hr * right.getWidth());
            ih = (int) (hr * right.getHeight());

            g.drawImage(right, w - iw - cutout + BOTTOM_GAP * hr, h - ih, iw, ih);

            setSym(g, hr,  hr * 5 + cutout, h-hr*5, 2);

            Res.getCost(sb.getUpgradeCost(), mtype > 0, sym);

            setSym(g, hr, hr * 5 + cutout, h-hr*130, 0);

            Res.getWorkerLv(sb.work_lv, mtype > 0, sym);

            int hi = h;

            double marg = 0;

            if (ctype == 0)
                for (int i = 0; i < 10 * sb.cannon / sb.maxCannon; i++) {
                    FakeImage img = aux.battle[1][2 + i].getImg();
                    iw = (int) (hr * img.getWidth());
                    ih = (int) (hr * img.getHeight());
                    marg += hr * img.getHeight() - ih;
                    if (marg > 0.5) {
                        marg--;
                        ih++;
                    }
                    hi -= ih;
                    g.drawImage(img, w - iw - cutout + BOTTOM_GAP * hr, hi, iw, ih);
                }
            if(sb.cannon == sb.maxCannon) {
                FakeImage fire = aux.battle[1][getFireLang()+ctype].getImg();

                int fw = (int) (hr * fire.getWidth());
                int fh = (int) (hr * fire.getHeight());

                double fir;

                if(cutout != 0) {
                    fir = w - fw - 4 * hr - cutout;
                } else {
                    fir = w - fw - 4 * hr;
                }

                g.drawImage(fire, fir, h - fh - 4 * hr, fw, fh);
            }
            //Decide lineup icon's size, 0.675 is guessed value by comparing BC and BCU
            hr = avah * 0.675 / aux.slot[0].getImg().getHeight();
            //Make lineup won't cover cannon button and money upgrade button
            hr = Math.min(hr, (box.getWidth()-iw*2.0-cutout*2)/aux.slot[0].getImg().getWidth()/5.9);
            double term = hr * aux.slot[0].getImg().getWidth() * 0.2;

            if(CommonStatic.getConfig().twoRow) {
                double termh = hr * aux.slot[0].getImg().getHeight() * 0.1;

                drawLineupWithTwoRows(g, w, h, hr, term, termh);
            } else {
                if(sb.isOneLineup) {
                    drawLineup(g, w, h, hr, term, false, 0);
                } else {
                    drawLineup(g, w, h, hr, term, true, 1-sb.frontLineup);
                    drawLineup(g, w, h, hr, term, false, sb.frontLineup);
                }
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

        private void drawLineupWithTwoRows(FakeGraphics g, int w, int h, double hr, double term, double termh) {
            int iw;
            int ih;
            int imw;
            int imh;

            for (int i = 0; i < 2; i++) {
                for(int j = 0; j < 5; j++) {
                    Form f = sb.b.lu.fs[i][j];

                    FakeImage img = f == null ? aux.slot[0].getImg() : f.anim.getUni().getImg();

                    iw = (int) (hr * img.getWidth());
                    ih = (int) (hr * img.getHeight());

                    imw = iw;
                    imh = ih;

                    if(sb.selectedUnit[0] != -1 && sb.selectedUnit[0] == i && sb.selectedUnit[1] == j) {
                        switch (sb.buttonDelay) {
                            case 3:
                                imw *= 0.95;
                                imh *= 0.95;
                                break;
                            case 4:
                                imw *= 1.05;
                                imh *= 1.05;
                        }
                    }

                    int x = (w - iw * 5) / 2 + iw * (j % 5) + (int) (term * ((j % 5) - 2));
                    int y = (int) (h - (2 - i) * (ih + termh));

                    g.drawImage(img, x - (imw - iw) / 2.0, y - (imh - ih) / 2.0, imw, imh);

                    if(f == null)
                        continue;

                    int pri = sb.elu.price[i][j];

                    if (pri == -1)
                        g.colRect(x, y, iw, ih, 255, 0, 0, 100);

                    int cool = sb.elu.cool[i][j];

                    boolean b = pri > sb.money || cool > 0;

                    if (b)
                        g.colRect((int) (x - (imw - iw) / 2.0), (int) (y - (imh - ih) / 2.0), imw, imh, 0, 0, 0, 100);

                    if (sb.locks[i][j])
                        g.colRect((int) (x - (imw - iw) / 2.0), (int) (y - (imh - ih) / 2.0), imw, imh, 0, 255, 0, 100);

                    if (cool > 0) {
                        int dw = (int) (hr * 10);
                        int dh = (int) (hr * 12);

                        double cd = 1.0 * cool / sb.elu.maxC[i][j];

                        int xw = (int) (cd * (iw - dw * 2));

                        g.colRect(x + iw - dw - xw, y + ih - dh * 2, xw, dh, 0, 0, 0, 255);

                        g.colRect(x + dw, y + ih - dh * 2, iw - dw * 2 - xw, dh, 100, 212, 255, 255);
                    } else {
                        setSym(g, hr, x + iw, y + ih, 3);

                        Res.getCost(pri / 100, !b, sym);
                    }
                }
            }
        }

        private void drawLineup(FakeGraphics g, int w, int h, double hr, double term, boolean isBehind, int index) {
            int iw;
            int ih;
            int imw;
            int imh;

            for (int i = 0; i < 5; i++) {
                Form f = sb.b.lu.fs[index][i];

                FakeImage img = f == null ? aux.slot[0].getImg() : f.anim.getUni().getImg();

                iw = (int) (hr * img.getWidth());
                ih = (int) (hr * img.getHeight());

                imw = iw;
                imh = ih;

                if(sb.selectedUnit[0] != -1 && sb.selectedUnit[0] == index && sb.selectedUnit[1] == i) {
                    switch (sb.buttonDelay) {
                        case 3:
                            imw *= 0.95;
                            imh *= 0.95;
                            break;
                        case 4:
                            imw *= 1.05;
                            imh *= 1.05;
                    }
                }

                int x = (w - iw * 5) / 2 + iw * (i % 5) + (int) (term * ((i % 5) - 2) + (index == 0 ? 0 : (term / 2)));
                int y = h - ih - (isBehind ? 0 : (int) (ih * 0.1));

                //Check if lineup is changing
                if(sb.changeFrame != -1) {
                    if(sb.changeFrame >= sb.changeDivision) {
                        double dis = isBehind ? ih * 0.5 : sb.goingUp ? ih * 0.4 : ih * 0.6;

                        y += (dis / sb.changeDivision) * (sb.changeDivision * 2 - sb.changeFrame) * (isBehind ? 1 : -1) * (sb.goingUp ? 1 : -1);
                    } else {
                        double dis = isBehind ? ih * 0.5 : sb.goingUp ? ih * 0.6 : ih * 0.4;

                        y +=  (dis - (dis / sb.changeDivision) * (sb.changeDivision - sb.changeFrame)) * (isBehind ? -1 : 1) * (sb.goingUp ? 1 : -1);
                    }
                }

                g.drawImage(img, x - (imw - iw) / 2.0, y - (imh - ih) / 2.0, imw, imh);

                if (f == null)
                    continue;

                int pri = sb.elu.price[index][i % 5];

                if (pri == -1)
                    g.colRect(x, y, iw, ih, 255, 0, 0, 100);

                int cool = sb.elu.cool[index][i % 5];

                boolean b = isBehind || pri > sb.money || cool > 0;

                if (b)
                    g.colRect((int) (x - (imw - iw) / 2.0), (int) (y - (imh - ih) / 2.0), imw, imh, 0, 0, 0, 100);

                if (sb.locks[index][i % 5])
                    g.colRect((int) (x - (imw - iw) / 2.0), (int) (y - (imh - ih) / 2.0), imw, imh, 0, 255, 0, 100);

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

                        Res.getCost(pri / 100, !b, sym);
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

            rang = Math.max(rang, sb.ebase.pos * ratio - off / 2.0);

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
            int posx = (int) ((sb.ebase.pos * ratio + off) * siz + pos);

            double shake = 0.0;

            if(sb.ebase.health <= 0 || (drawCast ? ((EEnemy) sb.ebase).hit : ((ECastle) sb.ebase).hit) > 0) {
                shake = (2 + (sb.time % 2 * -4)) * siz;
            }

            if (!drawCast) {
                Identifier<CastleImg> cind = sb.st.castle;

                VImg cast = Identifier.getOr(cind, CastleImg.class).img;

                FakeImage bimg = cast.getImg();

                int bw = (int) (bimg.getWidth() * siz);
                int bh = (int) (bimg.getHeight() * siz);

                gra.drawImage(bimg, posx - bw + shake, posy - bh, bw, bh);
            } else {
                if(sb.s_stop > 0 && (sb.ebase.getAbi() & Data.AB_TIMEI) != 0)
                    gra.setComposite(CVGraphics.POSITIVE, 0, 0);

                ((Entity) sb.ebase).anim.draw(gra,  setP(posx + shake, posy), siz * sprite);

                if(sb.ebase.health > 0)
                    ((Entity) sb.ebase).anim.drawEff(gra, p, siz * sprite);

                if(((CVGraphics) gra).neg)
                    gra.setComposite(FakeGraphics.GRAY, 0, 0);
            }

            gra.setTransform(at);

            shake = 0.0;

            if(sb.ubase.health <= 0 || ((ECastle)sb.ubase).hit > 0) {
                shake = (2 + (sb.time % 2 * -4)) * siz;
            }

            posx -= castw * siz / 2;
            posy -= casth * siz;

            setSym(gra, siz, posx, posy, 0);

            Res.getBase(sb.ebase, sym, bf.sb.st.trail);

            posx = (int) (((sb.st.len - 800) * ratio + off) * siz + pos);

            drawNyCast(gra, (int) (midh - road_h * siz), (int) (posx + shake), siz, sb.nyc);

            posx += castw * siz / 2;

            setSym(gra, siz, posx, posy, 1);

            Res.getBase(sb.ubase, sym, false);

            gra.delete(at);
        }

        @SuppressWarnings("UseBulkOperation")
        private void drawEntity(FakeGraphics gra) {
            for(int i = 0; i < sb.lw.size(); i++) {
                efList.add(sb.lw.get(i));
            }

            FakeTransform at = gra.getTransform();

            double psiz = siz * sprite;

            CommonStatic.getConfig().battle = true;

            for (int j = 0; j < sb.le.size(); j++)
                if ((sb.s_stop == 0 || (sb.le.get(j).getAbi() & Data.AB_TIMEI) == 0)) {
                    int dep = sb.le.get(j).layer * DEP;

                    while(efList.size() > 0) {
                        ContAb wc = efList.get(0);

                        if(wc.layer + 1 <= sb.le.get(j).layer) {
                            drawEff(gra, wc, at, psiz);

                            efList.remove(0);
                        } else
                            break;
                    }

                    gra.setTransform(at);

                    double p = getX(sb.le.get(j).pos);
                    double y = midh - (road_h - dep) * siz;

                    sb.le.get(j).anim.draw(gra, setP(p, y), psiz);

                    gra.setTransform(at);

                    sb.le.get(j).anim.drawEff(gra, setP(p, y), siz);
                }

            for(int i = 0; i < sb.le.size(); i++) {
                Entity e = sb.le.get(i);

                if(e.anim.smoke != null && !e.anim.smoke.done()) {
                    gra.setTransform(at);

                    double sx = getX(e.anim.smokeX);
                    double sy = midh - (road_h - e.anim.smokeLayer * DEP + 75.0) * siz;

                    e.anim.smoke.draw(gra, setP(sx, sy), psiz * 1.2);
                }
            }

            if(sb.ebase instanceof Entity) {
                if(sb.s_stop == 0 || (sb.ebase.getAbi() & Data.AB_TIMEI) > 0) {
                    if(((Entity) sb.ebase).anim.smoke != null && !((Entity) sb.ebase).anim.smoke.done()) {
                        gra.setTransform(at);

                        double sx = getX(((Entity) sb.ebase).anim.smokeX);
                        double sy = midh - (road_h - ((Entity) sb.ebase).anim.smokeLayer * DEP + 100.0) * siz;

                        ((Entity) sb.ebase).anim.smoke.draw(gra, setP(sx, sy), psiz * 1.2);
                    }
                }
            } else if(sb.ebase instanceof ECastle) {
                if(sb.s_stop == 0 && ((ECastle) sb.ebase).smoke != null && !((ECastle) sb.ebase).smoke.done()) {
                    gra.setTransform(at);

                    double sx = getX(((ECastle) sb.ebase).smokeX);
                    double sy = midh - (road_h - ((ECastle) sb.ebase).smokeLayer * DEP + 100.0) * siz;

                    ((ECastle) sb.ebase).smoke.draw(gra, setP(sx, sy), psiz * 1.2);
                }
            }

            if(sb.ubase instanceof ECastle) {
                if(sb.s_stop == 0 && ((ECastle) sb.ubase).smoke != null && !((ECastle) sb.ubase).smoke.done()) {
                    gra.setTransform(at);

                    double sx = getX(((ECastle) sb.ubase).smokeX);
                    double sy = midh - (road_h - ((ECastle) sb.ubase).smokeLayer * DEP + 100.0) * siz;

                    ((ECastle) sb.ubase).smoke.draw(gra, setP(sx, sy), psiz * 1.2);
                }
            }

            while(efList.size() > 0) {
                drawEff(gra, efList.get(0), at, psiz);

                efList.remove(0);
            }

            for (int j = 0; j < sb.lea.size(); j++) {
                gra.setTransform(at);
                double p = getX(sb.lea.get(j).pos);
                double y = midh - (road_h - DEP * sb.lea.get(j).layer) * siz;

                if (sb.lea.get(j) instanceof WaprCont) {
                    double dx = ((WaprCont) sb.lea.get(j)).dire == -1 ? -27 * siz : -24 * siz;
                    sb.lea.get(j).draw(gra, setP(p+dx, y-24*siz), psiz);
                } else {
                    sb.lea.get(j).draw(gra, setP(p, y), psiz);
                }
            }

            if(sb.ebase.health <= 0) {
                for(int i = 0; i < sb.ebaseSmoke.size(); i++) {
                    EAnimCont eac = sb.ebaseSmoke.get(i);

                    gra.setTransform(at);
                    double p = getX(eac.pos);
                    double y = midh - (road_h - DEP * eac.layer) * siz;

                    eac.draw(gra, setP(p, y), psiz);
                }
            }

            if(sb.ubase.health <= 0) {
                for(int i = 0; i < sb.ubaseSmoke.size(); i++) {
                    EAnimCont eac = sb.ubaseSmoke.get(i);

                    gra.setTransform(at);
                    double p = getX(eac.pos);
                    double y = midh - (road_h - DEP * eac.layer) * siz;

                    eac.draw(gra, setP(p, y), psiz);
                }
            }

            gra.setTransform(at);
            int can = cany[sb.canon.id];
            int disp = canx[sb.canon.id];

            sb.canon.drawBase(gra, setP(getX(sb.ubase.pos) + disp * siz, midh + (can - road_h) * siz), psiz);

            gra.setTransform(at);

            sb.canon.drawAtk(gra, setP(getX(sb.canon.pos), midh - road_h * siz), psiz);

            gra.setTransform(at);

            if (sb.sniper != null && sb.sniper.enabled) {
                sb.sniper.drawBase(gra, setP(getX(sb.sniper.getPos()), midh-road_h*siz), psiz);

                gra.setTransform(at);
            }

            if (sb.s_stop > 0) {
                for (int j = 0; j < sb.le.size(); j++)
                    if ((sb.le.get(j).getAbi() & Data.AB_TIMEI) > 0) {
                        int dep = sb.le.get(j).layer * DEP;

                        if(((CVGraphics)gra).neg) {
                            gra.setComposite(CVGraphics.POSITIVE, 0, 0);
                        }

                        gra.setTransform(at);

                        double p = getX(sb.le.get(j).pos);
                        double y = midh - (road_h - dep) * siz;

                        sb.le.get(j).anim.draw(gra, setP(p, y), psiz);

                        gra.setTransform(at);

                        sb.le.get(j).anim.drawEff(gra, setP(p, y), siz);

                        if(((CVGraphics)gra).neg) {
                            gra.setComposite(FakeGraphics.GRAY, 0, 0);
                        }
                    }
            }

            gra.setTransform(at);
            gra.delete(at);

            CommonStatic.getConfig().battle = false;
        }

        private void drawEff(FakeGraphics g, ContAb wc, FakeTransform at, double pSiz) {
            int dep = wc.layer * DEP;

            g.setTransform(at);

            double p = (wc.pos * ratio + off) * siz + pos;

            if(wc instanceof ContWaveAb)
                p -= wave * siz;

            double y = midh - (road_h - dep) * siz;

            wc.draw(g, setP(p, y), pSiz);
        }

        private void drawTop(FakeGraphics g) {
            g.setComposite(CVGraphics.POSITIVE, 0, 0);

            int w = box.getWidth();
            double ratio = dpi / 42.0;
            setSym(g, ratio, w-aux.num[0][0].getImg().getWidth()*0.2 - cutout, aux.num[0][0].getImg().getHeight()*0.2, 1);
            P p = Res.getMoney(sb.getMoney(), sb.getMaxMoney(), sym);
            int ih = (int) p.y + (int) (aux.num[0][0].getImg().getHeight()*0.2);
            int n = 0;
            FakeImage bimg = aux.battle[2][1].getImg();
            int cw = (int) (bimg.getWidth() * ratio);
            if ((sb.conf[0] & 2) > 0 && sb.sniper != null) {
                bimg = aux.battle[2][sb.sniper.enabled ? 2 : 4].getImg();
                g.drawImage(bimg, w - cw - cutout, ih, dpi, dpi);
                n++;
            }
            bimg = aux.battle[2][1].getImg();
            if ((sb.conf[0] & 1) > 0) {
                g.drawImage(bimg, w - cw * (n + 1) - cutout, ih, dpi, dpi);
                n++;
            }
            bimg = aux.battle[2][page.getSpeed() > 0 ? 0 : 3].getImg();
            for (int i = 0; i < Math.abs(page.getSpeed()); i++)
                g.drawImage(bimg, w - cw * (i + 1 + n) - cutout, ih, dpi, dpi);

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

            if(timeLeft < 0 || min < 0) {
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
        }

        protected synchronized void drawBGOverlay(FakeGraphics gra, double midY) {
            if(sb.bg.overlay == null)
                return;

            gra.gradRectAlpha(pos, - (int) (maxH * siz - midh - midY * siz), (int) ((sb.st.len * ratio + 400) * siz), (int) ((1530 + midY) * siz), pos, 0, sb.bg.overlayAlpha, sb.bg.overlay[1], pos, (int) (1530 * siz - maxH * siz + midh + midY * siz), sb.bg.overlayAlpha, sb.bg.overlay[0]);
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
