package com.mandarin.bcu.androidutil.battle;

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
import common.battle.attack.ContAb;
import common.battle.attack.ContWaveAb;
import common.battle.data.DataEnemy;
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
import common.util.anim.AnimU;
import common.util.pack.EffAnim;
import common.util.stage.CastleImg;
import common.util.unit.Form;

public interface BattleBox {

    class BBPainter implements BattleConst {

        private static final float sprite = 0.8f;
        private static final int road_h = 156; // in p
        private static final int off = 200;
        private static final int DEP = 4;
        private static final int wave = 28, castw = 128, casth = 256;
        private static final int c0y = -130, c1y = -130, c2y = -258;
        private static final int[] cany = new int[] { -134, -134, -134, -250, -250, -134, -134, -134 };
        private static final int[] canx = new int[] { 0, 0, 0, 64, 64, 0, 0, 0 };
        private static final DecimalFormat df;
        private static final float bar = 8;
        protected static final int BOTTOM_GAP = 48;

        static {
            NumberFormat nf = NumberFormat.getInstance(Locale.US);
            df = (DecimalFormat) nf;
            df.applyPattern("#.##");
        }

        public static void drawNyCast(FakeGraphics gra, int y, int x, float siz, int[] inf) {
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

        public float corr, unir; // siz = pix/p;
        
        private final int maxW;
        @SuppressWarnings("FieldCanBeLocal")
        private final int minH = 510;
        @SuppressWarnings("FieldCanBeLocal")
        private final int maxH = 510 * 3;
        public int midh, prew, preh; // in pix

        private float minSiz = -1;
        private float maxSiz = -1;

        private float groundHeight = -1;

        public int dpi = -1;
        public int stmImageOffset = -1;
        public int stmImageYOffset = 0;
        public float cutout = 0;

        private final BCAuxAssets aux = CommonStatic.getBCAssets();

        private final ArrayList<ContAb> efList = new ArrayList<>();

        public SymCoord sym = new SymCoord(null, 0, 0, 0, 0);
        public P p = new P(0, 0);

        public float preSiz = 1f;
        public int prePos = 0;

        public FakeImage stageImage = null;

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

            if (prew != w || preh != h) {
                clear();
                prew = w;
                preh = h;
                bf.sb.siz = preSiz;
                bf.sb.pos = prePos;
            }

            regulate();

            if(bf.sb.s_stop == 0 && ((CVGraphics)g).neg) {
                //Set p0 to 1 to change negative effect
                g.setComposite(CVGraphics.POSITIVE, 1, 0);
            } else if(bf.sb.s_stop > 0) {
                g.setComposite(CVGraphics.GRAY, 0, 0);
            }

            float y = maxH * bf.sb.siz - midh;
            float midY = groundHeight / minSiz;

            if(CommonStatic.getConfig().drawBGEffect)
                bf.sb.registerBattleDimension(midY, h / minSiz);

            if(CommonStatic.getConfig().twoRow)
                midY += (h * 0.75f / 10f);

            ImgCore.set(g);

            bf.sb.bg.draw(g, setP(box.getWidth(), box.getHeight()), bf.sb.pos, midh, bf.sb.siz, (int) Math.ceil(groundHeight + (CommonStatic.getConfig().twoRow ? (h * 0.75 / 10.0) : 0) - bf.sb.shakeOffset));

            if(CommonStatic.getConfig().drawBGEffect && bf.sb.bgEffect != null) {
                bf.sb.bgEffect.preDraw(g, setP(bf.sb.pos, y), bf.sb.siz, midY);
            }

            drawCastle(g);

            if(bf.sb.cannon == bf.sb.maxCannon && bf.sb.canon.id == 0) {
                drawCannonRange(g);
            }

            drawEntity(g);

            drawCastleHealthIndicator(g);

            if(CommonStatic.getConfig().drawBGEffect && bf.sb.bgEffect != null) {
                bf.sb.bgEffect.postDraw(g, setP(bf.sb.pos, y), bf.sb.siz, midY);
            }

            if(bf.sb.bg.overlay != null) {
                drawBGOverlay(g, midY);
            }

            drawBtm(g);
            drawTop(g);

            if(bf.sb.st.timeLimit != 0) {
                drawTime(g);
            }

            if(CommonStatic.getConfig().stageName && stageImage != null)
                g.drawImage(stageImage, stmImageOffset, stmImageYOffset);
        }

        private void setSym(FakeGraphics g, float r, float x, float y, int t) {
            sym.g = g;
            sym.r = r;
            sym.x = x;
            sym.y = y;
            sym.type = t;
        }

        private P setP(float x, float y) {
            p.x = x;
            p.y = y;

            return p;
        }

        public float getX(float x) {
            return (x * ratio + off) * bf.sb.siz + bf.sb.pos;
        }

        public void calculateSiz(int w, int h) {
            minSiz = 0;
            maxSiz = Float.MAX_VALUE;

            minSiz = getReulatedSiz(minSiz, w, h);
            maxSiz = getReulatedSiz(maxSiz, w, h);

            groundHeight = (h * 2 / 10f) * (1 - minSiz / maxSiz);
        }

        private float getReulatedSiz(float size, int w, int h) {
            if (size * minH > h)
                size = 1f * h / minH;
            if (size * maxH < h)
                size = 1f * h / maxH;
            if (size * maxW < w)
                size = 1f * w / maxW;

            return size;
        }

        public void regulate() {
            int w = box.getWidth();
            int h = box.getHeight();

            if (bf.sb.siz < minSiz)
                bf.sb.siz = minSiz;

            if (bf.sb.siz >= maxSiz)
                bf.sb.siz = maxSiz;

            if (bf.sb.pos > 0)
                bf.sb.pos = 0;

            if (maxW * bf.sb.siz + bf.sb.pos < w)
                bf.sb.pos = (int) (w - maxW * bf.sb.siz);

            midh = h + (int) (groundHeight * (bf.sb.siz - maxSiz) / (maxSiz - minSiz));

            if(CommonStatic.getConfig().twoRow) {
                midh = (int) (midh - h * 0.75f / 10f);
            }

            midh = (int) (midh + bf.sb.shakeOffset * dpi / 32f);
        }

        public void reset() {
            pt = bf.sb.time;
            box.reset();
        }

        private void clear() {
            pt = -1;
            bf.sb.siz = 0;
            bf.sb.pos = 0;
            midh = 0;
        }

        private void drawBtm(FakeGraphics g) {
            g.setComposite(CVGraphics.POSITIVE, 0, 0);

            int w = box.getWidth();
            int h = box.getHeight();
            int cw = 0;
            int time = bf.sb.s_stop == 0 ? (bf.sb.time / 5) % 2 : 1;
            int mtype = bf.sb.money < bf.sb.upgradeCost ? 0 : time == 0 ? 1 : 2;

            if (bf.sb.work_lv >= 8)
                mtype = 2;

            FakeImage left = aux.battle[0][mtype].getImg();

            int ctype = bf.sb.cannon == bf.sb.maxCannon && time == 0 ? 1 : 0;

            FakeImage right = aux.battle[1][ctype].getImg();

            cw += left.getWidth();
            cw += right.getWidth();
            cw += aux.slot[0].getImg().getWidth() * 5;

            float r = 1f * w / cw;
            float avah = h * (10 - bar) / 10f;
            float hr = avah / left.getHeight();

            corr = hr = Math.min(r, hr);

            int ih = (int) (hr * left.getHeight());
            int iw = (int) (hr * left.getWidth());

            g.drawImage(left, cutout - BOTTOM_GAP * hr, h - ih, iw, ih);

            iw = (int) (hr * right.getWidth());
            ih = (int) (hr * right.getHeight());

            g.drawImage(right, w - iw - cutout + BOTTOM_GAP * hr, h - ih, iw, ih);

            setSym(g, hr,  hr * 5 + cutout, h-hr*5, 2);

            Res.getCost(bf.sb.getUpgradeCost(), mtype > 0, sym);

            setSym(g, hr, hr * 5 + cutout, h-hr*130, 0);

            Res.getWorkerLv(bf.sb.work_lv, mtype > 0, sym);

            int hi = h;

            float marg = 0;

            if (ctype == 0)
                for (int i = 0; i < 10 * bf.sb.cannon / bf.sb.maxCannon; i++) {
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
            if(bf.sb.cannon == bf.sb.maxCannon) {
                FakeImage fire = aux.battle[1][getFireLang()+ctype].getImg();

                int fw = (int) (hr * fire.getWidth());
                int fh = (int) (hr * fire.getHeight());

                float fir;

                if(cutout != 0) {
                    fir = w - fw - 4 * hr - cutout;
                } else {
                    fir = w - fw - 4 * hr;
                }

                g.drawImage(fire, fir, h - fh - 4 * hr, fw, fh);
            }
            //Decide lineup icon's size, 0.675 is guessed value by comparing BC and BCU
            hr = avah * 0.675f / aux.slot[0].getImg().getHeight();
            //Make lineup won't cover cannon button and money upgrade button
            hr = Math.min(hr, (box.getWidth()-iw*2f-cutout*2)/aux.slot[0].getImg().getWidth()/5.9f);
            float term = hr * aux.slot[0].getImg().getWidth() * 0.2f;

            if(CommonStatic.getConfig().twoRow) {
                float termh = hr * aux.slot[0].getImg().getHeight() * 0.1f;

                drawLineupWithTwoRows(g, w, h, hr, term, termh);
            } else {
                if(bf.sb.isOneLineup) {
                    drawLineup(g, w, h, hr, term, false, 0);
                } else {
                    drawLineup(g, w, h, hr, term, true, 1-bf.sb.frontLineup);
                    drawLineup(g, w, h, hr, term, false, bf.sb.frontLineup);
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

        private void drawLineupWithTwoRows(FakeGraphics g, int w, int h, float hr, float term, float termh) {
            int iw;
            int ih;
            int imw;
            int imh;

            for (int i = 0; i < 2; i++) {
                for(int j = 0; j < 5; j++) {
                    Form f = bf.sb.b.lu.fs[i][j];

                    FakeImage img = f == null ? aux.slot[0].getImg() : f.anim.getUni().getImg();

                    iw = (int) (hr * img.getWidth());
                    ih = (int) (hr * img.getHeight());

                    imw = iw;
                    imh = ih;

                    if(bf.sb.selectedUnit[0] != -1 && bf.sb.selectedUnit[0] == i && bf.sb.selectedUnit[1] == j) {
                        switch (bf.sb.buttonDelay) {
                            case 3:
                                imw = (int) (imw * 0.95f);
                                imh = (int) (imh * 0.95f) ;
                                break;
                            case 4:
                                imw = (int) (imw * 1.05f);
                                imh = (int) (imh * 1.05f);
                        }
                    }

                    int x = (w - iw * 5) / 2 + iw * (j % 5) + (int) (term * ((j % 5) - 2));
                    int y = (int) (h - (2 - i) * (ih + termh));

                    g.drawImage(img, x - (imw - iw) / 2f, y - (imh - ih) / 2f, imw, imh);

                    if(f == null)
                        continue;

                    int pri = bf.sb.elu.price[i][j];

                    if (pri == -1)
                        g.colRect(x, y, iw, ih, 255, 0, 0, 100);

                    int cool = bf.sb.elu.cool[i][j];

                    boolean b = pri > bf.sb.money || cool > 0;

                    if (b)
                        g.colRect((int) (x - (imw - iw) / 2.0), (int) (y - (imh - ih) / 2.0), imw, imh, 0, 0, 0, 100);

                    if (bf.sb.locks[i][j])
                        g.colRect((int) (x - (imw - iw) / 2.0), (int) (y - (imh - ih) / 2.0), imw, imh, 0, 255, 0, 100);

                    if (cool > 0) {
                        int dw = (int) (hr * 10);
                        int dh = (int) (hr * 12);

                        float cd = 1f * cool / bf.sb.elu.maxC[i][j];

                        int xw = (int) (cd * (iw - dw * 2));

                        g.colRect(x + iw - dw - xw, y + ih - dh * 2, xw, dh, 0, 0, 0, 255);

                        g.colRect(x + dw, y + ih - dh * 2, iw - dw * 2 - xw, dh, 100, 212, 255, 255);
                    } else {
                        setSym(g, hr, x + iw * 1.05f, y + ih * 1.05f, 3);

                        Res.getCost(pri / 100, !b, sym);
                    }
                }
            }
        }

        private void drawLineup(FakeGraphics g, int w, int h, float hr, float term, boolean isBehind, int index) {
            int iw;
            int ih;
            int imw;
            int imh;

            for (int i = 0; i < 5; i++) {
                Form f = bf.sb.b.lu.fs[index][i];

                FakeImage img = f == null ? aux.slot[0].getImg() : f.anim.getUni().getImg();

                iw = (int) (hr * img.getWidth());
                ih = (int) (hr * img.getHeight());

                imw = iw;
                imh = ih;

                if(bf.sb.selectedUnit[0] != -1 && bf.sb.selectedUnit[0] == index && bf.sb.selectedUnit[1] == i) {
                    switch (bf.sb.buttonDelay) {
                        case 3:
                            imw = (int) (imw * 0.95f);
                            imh = (int) (imh * 0.95f) ;
                            break;
                        case 4:
                            imw = (int) (imw * 1.05f);
                            imh = (int) (imh * 1.05f);
                    }
                }

                int x = (w - iw * 5) / 2 + iw * (i % 5) + (int) (term * ((i % 5) - 2) + (index == 0 ? 0 : (term / 2)));
                int y = getLineupY(h, isBehind, ih);

                g.drawImage(img, x - (imw - iw) / 2f, y - (imh - ih) / 2f, imw, imh);

                if (f == null)
                    continue;

                int pri = bf.sb.elu.price[index][i % 5];

                if (pri == -1)
                    g.colRect(x, y, iw, ih, 255, 0, 0, 100);

                int cool = bf.sb.elu.cool[index][i % 5];

                boolean b = isBehind || pri > bf.sb.money || cool > 0;

                if (b)
                    g.colRect((int) (x - (imw - iw) / 2.0), (int) (y - (imh - ih) / 2.0), imw, imh, 0, 0, 0, 100);

                if (bf.sb.locks[index][i % 5])
                    g.colRect((int) (x - (imw - iw) / 2.0), (int) (y - (imh - ih) / 2.0), imw, imh, 0, 255, 0, 100);

                if(!isBehind) {
                    if (cool > 0) {
                        int dw = (int) (hr * 10);
                        int dh = (int) (hr * 12);

                        float cd = 1f * cool / bf.sb.elu.maxC[index][i % 5];

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

        private int getLineupY(int h, boolean isBehind, int ih) {
            int y = h - ih - (isBehind ? 0 : (int) (ih * 0.1));

            //Check if lineup is changing
            if(bf.sb.changeFrame != -1) {
                if(bf.sb.changeFrame >= bf.sb.changeDivision) {
                    float dis = isBehind ? ih * 0.5f : bf.sb.goingUp ? ih * 0.4f : ih * 0.6f;

                    y = (int) (y + (dis / bf.sb.changeDivision) * (bf.sb.changeDivision * 2 - bf.sb.changeFrame) * (isBehind ? 1 : -1) * (bf.sb.goingUp ? 1 : -1));
                } else {
                    float dis = isBehind ? ih * 0.5f : bf.sb.goingUp ? ih * 0.6f : ih * 0.4f;

                    y = (int) (y + (dis - (dis / bf.sb.changeDivision) * (bf.sb.changeDivision - bf.sb.changeFrame)) * (isBehind ? -1 : 1) * (bf.sb.goingUp ? 1 : -1));
                }
            }
            
            return y;
        }

        private void drawCannonRange(FakeGraphics g) {
            FakeImage range = aux.battle[1][20].getImg();
            FakeImage cann = aux.battle[1][21].getImg();

            float rang = bf.sb.ubase.pos + 100 + 56 * 4;

            for(int i = 0; i < bf.sb.b.t().tech[Data.LV_CRG]+2; i++) {
                rang -= 405;
            }

            rang = Math.max(rang, bf.sb.ebase.pos * ratio - off / 2f);

            rang = getX(rang);

            float rw = range.getWidth() * 0.75f * bf.sb.siz;
            float rh = range.getHeight()  * 0.85f * bf.sb.siz;

            //102 is guessed value, making range indicator on ground
            g.drawImage(range, rang, midh - rh - 102 * bf.sb.siz, rw, rh);

            int rtime = (int) (bf.sb.time / 1.5) % 4;

            float canw = cann.getWidth() * 0.75f * bf.sb.siz;
            float canh = cann.getHeight() * 0.75f * bf.sb.siz;

            g.drawImage(cann, rang + rw / 2f - canw / 2f, midh - canh - rh - 102 * bf.sb.siz - Math.abs(rtime - 2) * 8 * bf.sb.siz, canw, canh);
        }

        private void drawCastle(FakeGraphics gra) {
            FakeTransform at = gra.getTransform();

            boolean drawCast = bf.sb.ebase instanceof Entity;

            int posy = (int) (midh - road_h * bf.sb.siz);
            int posx = (int) ((bf.sb.ebase.pos * ratio + off) * bf.sb.siz + bf.sb.pos);

            float shake = 0f;

            if(bf.sb.ebase.health <= 0 || (drawCast ? ((EEnemy) bf.sb.ebase).hit : ((ECastle) bf.sb.ebase).hit) > 0) {
                shake = (2 + (bf.sb.time % 2 * -4)) * bf.sb.siz;
            }

            if (!drawCast) {
                Identifier<CastleImg> cind = bf.sb.st.castle;

                VImg cast = Identifier.getOr(cind, CastleImg.class).img;

                FakeImage bimg = cast.getImg();

                int bw = (int) (bimg.getWidth() * bf.sb.siz);
                int bh = (int) (bimg.getHeight() * bf.sb.siz);

                gra.drawImage(bimg, posx - bw + shake, posy - bh, bw, bh);
            } else {
                if(bf.sb.s_stop > 0 && (bf.sb.ebase.getAbi() & Data.AB_TIMEI) != 0)
                    gra.setComposite(CVGraphics.POSITIVE, 0, 0);

                posx = (int) getX(bf.sb.ebase.pos);

                ((Entity) bf.sb.ebase).anim.draw(gra,  setP(posx + shake, posy), bf.sb.siz * sprite);

                if(bf.sb.ebase.health > 0)
                    ((Entity) bf.sb.ebase).anim.drawEff(gra, p, bf.sb.siz * sprite);

                if(((CVGraphics) gra).neg)
                    gra.setComposite(FakeGraphics.GRAY, 0, 0);
            }

            gra.setTransform(at);

            posx = (int) (((bf.sb.st.len - 800) * ratio + off) * bf.sb.siz + bf.sb.pos);

            shake = 0f;

            if(bf.sb.ubase.health <= 0 || ((ECastle)bf.sb.ubase).hit > 0) {
                shake = (2 + (bf.sb.time % 2 * -4)) * bf.sb.siz;
            }

            drawNyCast(gra, (int) (midh - road_h * bf.sb.siz), (int) (posx + shake), bf.sb.siz, bf.sb.nyc);

            gra.delete(at);
        }

        private void drawCastleHealthIndicator(FakeGraphics gra) {
            int posy = (int) (midh - road_h * bf.sb.siz);
            int posx = (int) ((bf.sb.ebase.pos * ratio + off) * bf.sb.siz + bf.sb.pos);

            if (bf.sb.ebase instanceof Entity && ((Entity) bf.sb.ebase).data instanceof DataEnemy) {
                posx = (int) (posx - castw * bf.sb.siz / 2);

                AnimU<?> anim = ((Entity) bf.sb.ebase).data.getPack().anim;

                if(anim != null && anim.mamodel.confs.length > 1) {
                    posx = (int) (posx + anim.mamodel.confs[1][2] * 2.5f * anim.mamodel.parts[0][8] / anim.mamodel.ints[0] * bf.sb.siz * ratio);
                    posy = (int) (posy + anim.mamodel.confs[1][3] * 2.5f * anim.mamodel.parts[0][9] / anim.mamodel.ints[0] * bf.sb.siz * ratio);
                }
            } else {
                posx = (int) (posx - castw * bf.sb.siz * 1.15f);
                posy = (int) (posy - (casth * bf.sb.siz * 0.95f + aux.num[5][0].getImg().getHeight() * bf.sb.siz));
            }

            setSym(gra, bf.sb.siz * 0.8f, posx, posy, 0);

            Res.getBase(bf.sb.ebase, sym, bf.sb.st.trail);

            posy = (int) (midh - road_h * bf.sb.siz - casth * bf.sb.siz - aux.num[5][0].getImg().getHeight() * bf.sb.siz);
            posx = (int) (((bf.sb.st.len - 800) * ratio + off) * bf.sb.siz + bf.sb.pos);

            setSym(gra, bf.sb.siz * 0.8f, posx, posy, 0);

            Res.getBase(bf.sb.ubase, sym, false);
        }

        @SuppressWarnings("UseBulkOperation")
        private void drawEntity(FakeGraphics gra) {
            for(int i = 0; i < bf.sb.lw.size(); i++) {
                efList.add(bf.sb.lw.get(i));
            }

            FakeTransform at = gra.getTransform();

            float psiz = bf.sb.siz * sprite;

            CommonStatic.getConfig().battle = true;

            for (int i = 0; i < bf.sb.le.size(); i++) {
                if(bf.sb.le.get(i).dead)
                    continue;

                if ((bf.sb.s_stop == 0 || (bf.sb.le.get(i).getAbi() & Data.AB_TIMEI) == 0)) {
                    int dep = bf.sb.le.get(i).layer * DEP;

                    while (!efList.isEmpty()) {
                        ContAb wc = efList.get(0);

                        if (wc.layer + 1 <= bf.sb.le.get(i).layer) {
                            drawEff(gra, wc, at, psiz);

                            efList.remove(0);
                        } else
                            break;
                    }

                    gra.setTransform(at);

                    float p = getX(bf.sb.le.get(i).pos);
                    float y = midh - (road_h - dep) * bf.sb.siz;

                    bf.sb.le.get(i).anim.draw(gra, setP(p, y), psiz);

                    gra.setTransform(at);

                    if (bf.sb.le.get(i).anim.corpse == null || bf.sb.le.get(i).anim.corpse.type == EffAnim.ZombieEff.BACK) {
                        bf.sb.le.get(i).anim.drawEff(gra, setP(p, y), bf.sb.siz);
                    }
                }
            }

            for(int i = 0; i < bf.sb.le.size(); i++) {
                Entity e = bf.sb.le.get(i);

                if(e.dead)
                    continue;

                if(e.anim.smoke != null && !e.anim.smoke.done()) {
                    gra.setTransform(at);

                    float sx = getX(e.anim.smokeX);
                    float sy = midh - (road_h - e.anim.smokeLayer * DEP + 75f) * bf.sb.siz;

                    e.anim.smoke.draw(gra, setP(sx, sy), psiz * 1.2f);
                }
            }

            if(bf.sb.ebase instanceof Entity) {
                if(bf.sb.s_stop == 0 || (bf.sb.ebase.getAbi() & Data.AB_TIMEI) > 0) {
                    if(((Entity) bf.sb.ebase).anim.smoke != null && !((Entity) bf.sb.ebase).anim.smoke.done()) {
                        gra.setTransform(at);

                        float sx = getX(((Entity) bf.sb.ebase).anim.smokeX);
                        float sy = midh - (road_h - ((Entity) bf.sb.ebase).anim.smokeLayer * DEP + 100f) * bf.sb.siz;

                        ((Entity) bf.sb.ebase).anim.smoke.draw(gra, setP(sx, sy), psiz * 1.2f);
                    }
                }
            } else if(bf.sb.ebase instanceof ECastle) {
                if(bf.sb.s_stop == 0 && ((ECastle) bf.sb.ebase).smoke != null && !((ECastle) bf.sb.ebase).smoke.done()) {
                    gra.setTransform(at);

                    float sx = getX(((ECastle) bf.sb.ebase).smokeX);
                    float sy = midh - (road_h - ((ECastle) bf.sb.ebase).smokeLayer * DEP + 100f) * bf.sb.siz;

                    ((ECastle) bf.sb.ebase).smoke.draw(gra, setP(sx, sy), psiz * 1.2f);
                }
            }

            if(bf.sb.ubase instanceof ECastle) {
                if(bf.sb.s_stop == 0 && ((ECastle) bf.sb.ubase).smoke != null && !((ECastle) bf.sb.ubase).smoke.done()) {
                    gra.setTransform(at);

                    float sx = getX(((ECastle) bf.sb.ubase).smokeX);
                    float sy = midh - (road_h - ((ECastle) bf.sb.ubase).smokeLayer * DEP + 100f) * bf.sb.siz;

                    ((ECastle) bf.sb.ubase).smoke.draw(gra, setP(sx, sy), psiz * 1.2f);
                }
            }

            while(!efList.isEmpty()) {
                drawEff(gra, efList.get(0), at, psiz);

                efList.remove(0);
            }

            for (int j = 0; j < bf.sb.lea.size(); j++) {
                gra.setTransform(at);
                float p = getX(bf.sb.lea.get(j).pos);
                float y = midh - (road_h - DEP * bf.sb.lea.get(j).layer) * bf.sb.siz;

                if (bf.sb.lea.get(j) instanceof WaprCont) {
                    float dx = ((WaprCont) bf.sb.lea.get(j)).dire == -1 ? -27 * bf.sb.siz : -24 * bf.sb.siz;
                    bf.sb.lea.get(j).draw(gra, setP(p+dx, y-24*bf.sb.siz), psiz);
                } else {
                    bf.sb.lea.get(j).draw(gra, setP(p, y), psiz);
                }
            }

            if(bf.sb.ebase.health <= 0) {
                for(int i = 0; i < bf.sb.ebaseSmoke.size(); i++) {
                    EAnimCont eac = bf.sb.ebaseSmoke.get(i);

                    gra.setTransform(at);
                    float p = getX(eac.pos);
                    float y = midh - (road_h - DEP * eac.layer) * bf.sb.siz;

                    eac.draw(gra, setP(p, y), psiz);
                }
            }

            if(bf.sb.ubase.health <= 0) {
                for(int i = 0; i < bf.sb.ubaseSmoke.size(); i++) {
                    EAnimCont eac = bf.sb.ubaseSmoke.get(i);

                    gra.setTransform(at);
                    float p = getX(eac.pos);
                    float y = midh - (road_h - DEP * eac.layer) * bf.sb.siz;

                    eac.draw(gra, setP(p, y), psiz);
                }
            }

            gra.setTransform(at);
            int can = cany[bf.sb.canon.id];
            int disp = canx[bf.sb.canon.id];

            bf.sb.canon.drawBase(gra, setP(getX(bf.sb.ubase.pos) + disp * bf.sb.siz, midh + (can - road_h) * bf.sb.siz), psiz);

            gra.setTransform(at);

            bf.sb.canon.drawAtk(gra, setP(getX(bf.sb.canon.pos), midh - road_h * bf.sb.siz), psiz);

            gra.setTransform(at);

            if (bf.sb.sniper != null && bf.sb.sniper.enabled) {
                bf.sb.sniper.drawBase(gra, setP(getX(bf.sb.sniper.getPos()), midh-road_h*bf.sb.siz), psiz);

                gra.setTransform(at);
            }

            if (bf.sb.s_stop > 0) {
                if((bf.sb.ebase.getAbi() * Data.AB_TIMEI) != 0) {
                    if(((CVGraphics)gra).neg) {
                        gra.setComposite(CVGraphics.POSITIVE, 0, 0);
                    }

                    float shake = 0f;

                    if(bf.sb.ebase.health <= 0 || (bf.sb.ebase instanceof ECastle && ((ECastle) bf.sb.ebase).hit > 0) || (bf.sb.ebase instanceof EEnemy && ((EEnemy) bf.sb.ebase).hit > 0)) {
                        shake = (2 + (bf.sb.time % 2 * -4)) * bf.sb.siz;
                    }

                    if (bf.sb.ebase instanceof Entity) {
                        int posx = (int) getX(bf.sb.ebase.pos);
                        int posy = (int) (midh - road_h * bf.sb.siz);

                        ((Entity) bf.sb.ebase).anim.draw(gra, setP(posx + shake, posy), bf.sb.siz * sprite);

                        if(((Entity) bf.sb.ebase).anim.smoke != null) {
                            ((Entity) bf.sb.ebase).anim.smoke.draw(gra, setP(posx + shake, posy), bf.sb.siz * sprite);
                        }

                        if (bf.sb.ebase.health > 0)
                            ((Entity) bf.sb.ebase).anim.drawEff(gra, setP(posx + shake, posy), bf.sb.siz * sprite);
                    }

                    if(((CVGraphics)gra).neg) {
                        gra.setComposite(FakeGraphics.GRAY, 0, 0);
                    }
                }

                for (int i = 0; i < bf.sb.le.size(); i++) {
                    if(bf.sb.le.get(i).dead)
                        continue;
                    
                    if ((bf.sb.le.get(i).getAbi() & Data.AB_TIMEI) > 0) {
                        int dep = bf.sb.le.get(i).layer * DEP;

                        if (((CVGraphics) gra).neg) {
                            gra.setComposite(CVGraphics.POSITIVE, 0, 0);
                        }

                        gra.setTransform(at);

                        float p = getX(bf.sb.le.get(i).pos);
                        float y = midh - (road_h - dep) * bf.sb.siz;

                        bf.sb.le.get(i).anim.draw(gra, setP(p, y), psiz);

                        gra.setTransform(at);

                        if (bf.sb.le.get(i).anim.corpse == null || bf.sb.le.get(i).anim.corpse.type == EffAnim.ZombieEff.BACK) {
                            bf.sb.le.get(i).anim.drawEff(gra, setP(p, y), bf.sb.siz);
                        }

                        if (((CVGraphics) gra).neg) {
                            gra.setComposite(FakeGraphics.GRAY, 0, 0);
                        }
                    }
                }
            }

            gra.setTransform(at);
            gra.delete(at);

            CommonStatic.getConfig().battle = false;
        }

        private void drawEff(FakeGraphics g, ContAb wc, FakeTransform at, float pSiz) {
            int dep = wc.layer * DEP;

            g.setTransform(at);

            float p = (wc.pos * ratio + off) * bf.sb.siz + bf.sb.pos;

            if(wc instanceof ContWaveAb)
                p -= wave * bf.sb.siz;

            float y = midh - (road_h - dep) * bf.sb.siz;

            wc.draw(g, setP(p, y), pSiz);
        }

        private void drawTop(FakeGraphics g) {
            g.setComposite(CVGraphics.POSITIVE, 0, 0);

            int w = box.getWidth();

            float ratio = dpi / 42f;

            setSym(g, ratio, w-aux.num[0][0].getImg().getWidth()*0.2f - cutout, aux.num[0][0].getImg().getHeight()*0.2f, 1);

            P p = Res.getMoney(bf.sb.getMoney(), bf.sb.getMaxMoney(), sym);

            int ih = (int) p.y + (int) (aux.num[0][0].getImg().getHeight() * 0.2 * ratio);
            int n = 0;

            FakeImage bimg = aux.battle[2][1].getImg();

            int cw = (int) (bimg.getWidth() * ratio * 1.025);

            if ((bf.sb.conf[0] & 2) > 0 && bf.sb.sniper != null) {
                bimg = aux.battle[2][bf.sb.sniper.enabled ? 2 : 4].getImg();
                g.drawImage(bimg, w - cw - cutout, ih, dpi, dpi);
                n++;
            }

            bimg = aux.battle[2][1].getImg();

            if ((bf.sb.conf[0] & 1) > 0) {
                g.drawImage(bimg, w - cw * (n + 1) - cutout, ih, dpi, dpi);
                n++;
            }

            if(page.getSpeed() > 0) {
                if(page.getSpeed() != 1) {
                    bimg = aux.battle[2][page.getSpeed() + 3].getImg();

                    float speedRatio = bimg.getHeight() * 1f / aux.battle[2][0].getImg().getHeight();
                    float iconRatio = dpi * 1f / aux.battle[2][0].getImg().getHeight();

                    float offset = 11 * iconRatio;

                    g.drawImage(bimg, w - cw * (1 + n) - offset - cutout, ih - offset, dpi * speedRatio, dpi * speedRatio);
                } else {
                    bimg = aux.battle[2][0].getImg();

                    g.drawImage(bimg, w - cw * (1 + n) - cutout, ih, dpi, dpi);
                }
            } else {
                bimg = aux.battle[2][3].getImg();

                for (int i = 0; i < Math.abs(page.getSpeed()); i++)
                    g.drawImage(bimg, w - cw * (i + 1 + n) - cutout, ih, dpi, dpi);
            }

            if(((CVGraphics)g).neg) {
                g.setComposite(FakeGraphics.GRAY, 0, 0);
            }
        }

        private void drawTime(FakeGraphics g) {
            setP(dpi * 16f / 32f, dpi * 60f / 32f);

            float ratio = (float) dpi / aux.timer[0].getImg().getHeight();

            float timeLeft = bf.sb.st.timeLimit * 60f - bf.sb.time / 30f;

            int min = (int) timeLeft / 60;

            timeLeft = (int) (timeLeft - min * 60f);

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

        protected synchronized void drawBGOverlay(FakeGraphics gra, float midY) {
            if(bf.sb.bg.overlay == null)
                return;

            gra.gradRectAlpha(bf.sb.pos, - (int) (maxH * bf.sb.siz - midh - midY * bf.sb.siz), (int) ((bf.sb.st.len * ratio + 400) * bf.sb.siz), (int) ((1530 + midY) * bf.sb.siz), bf.sb.pos, 0, bf.sb.bg.overlayAlpha, bf.sb.bg.overlay[1], bf.sb.pos, (int) (1530 * bf.sb.siz - maxH * bf.sb.siz + midh + midY * bf.sb.siz), bf.sb.bg.overlayAlpha, bf.sb.bg.overlay[0]);
        }
    }

    interface OuterBox extends RetFunc {

        int getSpeed();

    }

    int getHeight();

    BBPainter getPainter();

    int getWidth();

    void paint();

    void reset();

}
