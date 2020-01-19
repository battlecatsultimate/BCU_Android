package com.mandarin.bcu.androidutil.lineup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Environment;
import android.view.View;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import common.battle.BasisSet;
import common.battle.LineUp;
import common.util.unit.Form;

public class LineUpView extends View {
    /**
     * Bitmap list of unit icons
     **/
    private List<Bitmap> units = new ArrayList<>();
    /**
     * Bitmap of empty icon
     **/
    private final Bitmap empty;
    /**
     * Paint for whole lineup view
     **/
    private Paint p = new Paint();
    /**
     * Paint for whole icons
     **/
    private Paint icon = new Paint();
    /**
     * Paint for whole floating icons
     **/
    private Paint floating = new Paint();
    /**
     * Float value of 8 dpi
     **/
    private float f;
    /**
     * Bitmap from delete icon drawable
     **/
    private Bitmap bd;
    /**
     * Bitmap from replace icon drawable
     **/
    private Bitmap replace;
    /**
     * Positions of each units
     **/
    float[][][] position = new float[2][5][2];

    /**
     * This boolean decides if floating image can be drawn or not
     **/
    public boolean drawFloating = false;
    /**
     * Bitmap about floating unit icon
     **/
    public Bitmap floatB;
    /**
     * This boolean decides whether view calls onDraw
     **/
    public boolean touched = false;
    /**
     * Position where user firstly touched
     **/
    public int[] prePosit = new int[2];
    /**
     * Last position of lineup
     **/
    public int lastPosit = 1;

    private boolean drawOnce = true;

    /**
     * X coordinate where user is touching
     **/
    public float x = -1;
    /**
     * Y coordinate where user is touching
     **/
    public float y = -1;

    /**
     * Width/Height of unit icons
     **/
    float bw = 128f;

    /**
     * Replacing area's form data
     **/
    public Form repform = null;

    public LineUpView(Context context) {
        super(context);

        String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/page/uni.png";

        File f = new File(path);

        if (!f.exists()) {
            empty = StaticStore.empty(context, 10, 10);
        } else {
            empty = BitmapFactory.decodeFile(path);
        }

        this.f = StaticStore.dptopx(8f, context);
        this.bd = StaticStore.getResizebp(StaticStore.getBitmapFromVector(context, R.drawable.ic_delete_forever_black_24dp), bw * 2 / 3, bw * 2 / 3);
        this.replace = StaticStore.getResizebp(StaticStore.getBitmapFromVector(context, R.drawable.ic_autorenew_black_24dp), bw * 2 / 5, bw * 2 / 5);

        p.setFilterBitmap(true);
        p.setAntiAlias(true);
        p.setColor(StaticStore.getAttributeColor(context, R.attr.ButtonPrimary));

        floating.setFilterBitmap(true);
        floating.setAntiAlias(true);
        floating.setAlpha(255 / 2);

        for (int i = 0; i < position.length; i++) {
            for (int j = 0; j < position[i].length; j++) {
                position[i][j][0] = bw * i;
                position[i][j][1] = bw * i;
            }
        }

        for (int i = 0; i < 15; i++)
            units.add(empty);
    }

    @Override
    public void onDraw(Canvas c) {
        getPosition();

        DrawUnits(c);

        DrawDeleteBox(c);

        DrawReplaceBox(c);

        if (drawFloating)
            DrawFloatingImage(c);

        if (touched) {
            invalidate();
        }
    }

    /**
     * If floatB is true, draws floating unit image
     **/
    public void DrawFloatingImage(Canvas c) {
        if (floatB != empty)
            c.drawBitmap(StaticStore.getResizebp(floatB, bw * 1.5f, bw * 1.5f), x - (bw * 1.5f / 2), y - bw * 1.5f / 2, floating);
    }

    /**
     * Decides unit icons position using its width and height
     **/
    public void getPosition() {
        float w = getWidth();
        float h = getHeight();

        if (w != 0 && h != 0) {
            bw = w / 5.0f;

            for (int i = 0; i < position.length; i++) {
                for (int j = 0; j < position[i].length; j++) {
                    position[i][j][0] = bw * j;
                    position[i][j][1] = bw * i;
                }
            }
        }
    }

    /**
     * Remove unit from lineup and itself using position
     **/
    public void RemoveUnit(int[] posit) {
        if (posit[0] * 5 + posit[1] >= StaticStore.currentForms.size() || posit[0] * 5 + posit[1] < 0)
            if (posit[0] != 100)
                return;

        if (posit[0] != 100) {
            if (posit[0] * 5 + posit[1] < StaticStore.currentForms.size()) {
                StaticStore.currentForms.remove(posit[0] * 5 + posit[1]);
                units.remove(posit[0] * 5 + posit[1]);
                units.add(empty);
            }

            lastPosit--;

            toFormArray();
        } else {
            repform = null;
        }
    }

    /**
     * Draws deleting area
     **/
    private void DrawDeleteBox(Canvas c) {
        c.drawRoundRect(new RectF(bw + f, bw * 2 + f, bw * 5 - f, bw * 3 - f), f, f, p);
        c.drawBitmap(bd, bw + bw * 4 / 2 - (float) bd.getWidth() / 2, bw * 2.5f - (float) bd.getHeight() / 2, icon);
    }

    /**
     * Draws replacing area using repform
     **/
    private void DrawReplaceBox(Canvas c) {
        if (repform == null) {
            c.drawBitmap(StaticStore.getResizebp(empty, bw, bw), 0, bw * 2, p);
            c.drawBitmap(replace, bw / 2 - (float) replace.getWidth() / 2, bw * 2.5f - (float) replace.getHeight() / 2, icon);
        } else {
            Bitmap icon = (Bitmap) repform.anim.uni.getImg().bimg();

            if (icon.getWidth() != icon.getHeight())
                icon = StaticStore.MakeIcon(getContext(), icon, 48f);

            c.drawBitmap(StaticStore.getResizebp(icon, bw, bw), 0, bw * 2, p);
        }
    }

    /**
     * Draws unit icons using units
     **/
    private void DrawUnits(Canvas c) {
        int k = 0;

        for (float[][] floats : position) {
            for (float[] aFloat : floats) {
                if (k >= units.size())
                    c.drawBitmap(StaticStore.getResizebp(empty, bw, bw), aFloat[0], aFloat[1], p);
                else
                    c.drawBitmap(StaticStore.getResizebp(units.get(k), bw, bw), aFloat[0], aFloat[1], p);

                k += 1;
            }
        }
    }

    public void addUnit(Form f) {
        if (units.size() < 15) {
            changeUnitImage(lastPosit, (Bitmap) Objects.requireNonNull(f.udi).getImg().bimg());
            lastPosit += 1;
        } else {
            units.set(units.size() - 1, (Bitmap) Objects.requireNonNull(f.udi).bimg.bimg());
        }
    }

    /**
     * Changes 2 units' position using 2 ints. from is first unit's position, and to is second unit's position
     **/
    public void changeUnitPosition(int from, int to) {
        if (from < 0 || from >= units.size() || to < 0 || to >= units.size())
            return;

        Bitmap b = units.get(from);
        Bitmap b2 = units.get(to);

        if (b == empty) return;

        Form f = StaticStore.currentForms.get(from);

        if (b2 != empty) {
            units.remove(from);
            StaticStore.currentForms.remove(from);
            units.add(to, b);
            StaticStore.currentForms.add(to, f);
        } else {
            units.remove(from);
            StaticStore.currentForms.remove(from);
            units.add(lastPosit - 1, b);
            StaticStore.currentForms.add(lastPosit - 1, f);
        }

        toFormArray();
    }

    /**
     * Replaces specific unit and unit which is in replacing area
     **/
    public void ReplaceUnit(int from, int to) {
        Form f = null;
        Form f2 = null;
        Bitmap b = null;
        Bitmap b2 = null;

        boolean mode = true;

        if (from == 600 && (to < 0 || to >= units.size()))
            return;

        if (to == 600 && (from < 0 || from >= units.size()))
            return;

        if (from == 600) {
            if (repform != null)
                b = (Bitmap) repform.anim.uni.getImg().bimg();
            b2 = units.get(to);

            f = repform;
            if (b2 != empty)
                f2 = StaticStore.currentForms.get(to);

            mode = false;
        } else {
            b = units.get(from);

            if (repform != null)
                b2 = (Bitmap) repform.anim.uni.getImg().bimg();

            if (b != empty)
                f = StaticStore.currentForms.get(from);
            f2 = repform;
        }

        if (b != null) {
            if (b.getHeight() != b.getHeight())
                b = StaticStore.MakeIcon(getContext(), b, 48f);
        }

        if (b2 != null) {
            if (b2.getHeight() != b2.getWidth())
                b2 = StaticStore.MakeIcon(getContext(), b2, 48f);
        }

        if (f == null && f2 == null)
            return;

        if (mode) {
            if (f == null) return;

            if (f2 == null) {
                units.remove(from);
                units.add(empty);
                StaticStore.currentForms.remove(from);
                StaticStore.currentForms.add(null);

                lastPosit--;

                repform = f;
            } else {
                units.remove(from);
                StaticStore.currentForms.remove(from);
                units.add(from, b2);
                StaticStore.currentForms.add(from, f2);

                repform = f;
            }
        } else {
            if (f == null) return;

            if (f2 == null) {
                units.remove(to);
                units.add(lastPosit, b);
                StaticStore.currentForms.remove(to);
                StaticStore.currentForms.add(lastPosit, f);

                lastPosit++;

                repform = null;
            } else {
                units.remove(to);
                StaticStore.currentForms.remove(to);
                units.add(to, b);
                StaticStore.currentForms.add(to, f);

                repform = f2;
            }
        }

        toFormArray();
    }

    /**
     * Checks whether user tried to changed lineup
     **/
    public void CheckChange() {
        int[] posit = getTouchedUnit(x, y);

        if (posit == null)
            return;

        if (posit[0] == -100 && posit[1] == -100) {
            RemoveUnit(prePosit);
        } else if (posit[0] == 100 || prePosit[0] == 100) {
            ReplaceUnit(prePosit[0] * 5 + prePosit[1], posit[0] * 5 + posit[1]);
        } else {
            changeUnitPosition(5 * prePosit[0] + prePosit[1], 5 * posit[0] + posit[1]);
        }

        BasisSet.current.sele.lu.renew();

        StaticStore.SaveLineUp();
    }

    /**
     * Changes unit icon
     **/
    public void changeUnitImage(int position, Bitmap newb) {
        units.set(position, newb);
    }

    /**
     * Clears all unit icons
     **/
    public void clearAllUnit() {
        units.clear();

        for (int i = 0; i < 15; i++)
            units.add(empty);
    }

    /**
     * Gets unit's icon using position vlaues
     **/
    public Bitmap getUnitImage(int x, int y) {
        if (x == 100)
            if (repform != null) {
                Bitmap b = (Bitmap) repform.anim.uni.getImg().bimg();

                if (b.getHeight() != b.getWidth())
                    b = StaticStore.MakeIcon(getContext(), b, 48f);

                return b;
            } else
                return empty;

        if (5 * x + y >= units.size() || 5 * x + y < 0)
            return empty;
        else
            return units.get(5 * x + y);
    }

    /**
     * Gets unit which user touched
     **/
    public int[] getTouchedUnit(float x, float y) {
        for (int i = 0; i < position.length; i++) {
            for (int j = 0; j < position[i].length; j++) {
                if (x - position[i][j][0] >= 0 && y - position[i][j][1] > 0 && x - bw - position[i][j][0] < 0 && y - bw - position[i][j][1] < 0)
                    return new int[]{i, j};
            }
        }

        if (y > bw * 2 && y <= bw * 3 && x >= bw)
            return new int[]{-100, -100};

        if (y > bw * 2 && y <= bw * 3 && x <= bw)
            return new int[]{100, 100};

        return null;
    }

    /**
     * Gets form data from array of forms in Lineup and converts it to list of forms in StaticStore
     **/
    public void toFormList() {
        Form[][] forms = BasisSet.current.sele.lu.fs;

        StaticStore.currentForms = new ArrayList<>();

        for (Form[] form : forms) {
            StaticStore.currentForms.addAll(Arrays.asList(form));
        }
    }

    /**
     * Gets form data from list of forms in StaticStore and converts it to array of forms in Lineup
     **/
    public void toFormArray() {
        for (int i = 0; i < BasisSet.current.sele.lu.fs.length; i++) {
            for (int j = 0; j < BasisSet.current.sele.lu.fs[i].length; j++) {
                if (i * 5 + j < StaticStore.currentForms.size())
                    BasisSet.current.sele.lu.fs[i][j] = StaticStore.currentForms.get(i * 5 + j);
                else
                    BasisSet.current.sele.lu.fs[i][j] = null;
            }
        }
    }

    /**
     * Update Lineup view using form data in Lineup
     **/
    public void UpdateLineUp() {
        clearAllUnit();
        toFormList();

        lastPosit = 0;

        for (int i = 0; i < BasisSet.current.sele.lu.fs.length; i++) {
            for (int j = 0; j < BasisSet.current.sele.lu.fs[i].length; j++) {
                Form f = BasisSet.current.sele.lu.fs[i][j];

                if (f != null) {
                    if (repform != null) {
                        if (f.unit == repform.unit) {
                            repform = null;
                            StaticStore.position = new int[]{-1, -1};
                            StaticStore.updateForm = true;
                        }
                    }

                    Bitmap b = (Bitmap) f.anim.uni.getImg().bimg();

                    if (b.getWidth() != b.getHeight())
                        b = StaticStore.MakeIcon(getContext(), b, 48f);

                    changeUnitImage(i * 5 + j, b);

                    lastPosit += 1;
                }
            }
        }

        BasisSet.current.sele.lu.renew();
    }

    /**
     * Change specific unit's form
     **/
    public void ChangeFroms(LineUp lu) {
        for (int i = 0; i < lu.fs.length; i++) {
            if (lu.fs[i].length >= 0)
                System.arraycopy(lu.fs[i], 0, BasisSet.current.sele.lu.fs[i], 0, lu.fs[i].length);
        }

        UpdateLineUp();
    }
}
