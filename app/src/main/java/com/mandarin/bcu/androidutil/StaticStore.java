package com.mandarin.bcu.androidutil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.SystemClock;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.mandarin.bcu.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import common.CommonStatic;
import common.battle.BasisLU;
import common.battle.BasisSet;
import common.battle.Treasure;
import common.io.OutStream;
import common.system.MultiLangCont;
import common.system.fake.FakeImage;
import common.util.anim.ImgCut;
import common.util.stage.MapColc;
import common.util.unit.Combo;
import common.util.unit.Enemy;
import common.util.unit.Form;
import common.util.unit.Unit;

public class StaticStore {
    /**
     * System/IO variables
     **/
    public static String VER = "0.11.7";
    public static String ERR_FILE_ID = "1F60YLwsJ_zrJOh0IczUuf-Q1QyJftWzK";
    public static final String[] LIBREQ = {"000001", "000002", "000003", "080602", "080603", "080604", "080605", "080700", "080705", "080706", "080800", "080801", "080802",
            "080900", "080901", "080902", "081000", "081001", "081005", "081006", "090000", "090001", "090100", "090101", "090102", "090103", "090104"};
    public static final String[] OPTREQS = {"080504"};
    public static final String[] lang = {"", "en", "zh", "ko", "ja", "ru", "de", "fr", "nl", "es"};
    public static final String[] langfile = {"EnemyName.txt", "StageName.txt", "UnitName.txt", "UnitExplanation.txt", "EnemyExplanation.txt", "CatFruitExplanation.txt", "RewardName.txt", "ComboName.txt", "MedalName.txt", "MedalExplanation.txt"};
    public static final String LOGPATH = Environment.getExternalStorageDirectory().getPath() + "/BCU/logs/";
    public static final String CONFIG = "configuration";
    public static final long INTERVAL = 1000;
    public static final long INFO_INTERVAL = 350;
    public static int bgread = 0;
    public static int unitlang = 1;
    public static int enemeylang = 1;
    public static int stagelang = 1;
    public static int maplang = 1;
    public static int medallang = 1;
    public static boolean effread = false;
    public static boolean soulread = false;
    public static boolean nycread = false;
    public static boolean resread = false;
    public static boolean musicread = false;
    public static boolean dialogisShwed = false;
    public static Toast toast = null;

    public static int root = 0;

    /**
     * Image/Text variables
     **/

    public static Treasure t = null;
    public static FakeImage[] img15 = null;
    public static Bitmap[] icons = null;
    public static Bitmap[] picons = null;
    public static Bitmap[] fruit = null;
    public static String[] addition = null;

    public static int[] anumber = {203, 204, 206, 202, 205, 200, 209, 227, 218, 227, 227, 227, 227, 260, 258, 227, 227, 110, 227, 227, 122, 114};
    public static int[] pnumber = {207, 197, 198, 201, 208, 195, 264, 266, 227, 196, 199, 227, 227, 216, 214, 215, 210, 213, 262, 116, 227, 227, 227, 227, 227, 227, 227, 227, 227, 229, 231, 227, 49, 45, 47, 51, 43, 53, 109, 227};
    public static String[] afiles = {"", "", "", "", "", "", "", "MovingX.png", "", "SnipeX.png", "TimeX.png", "Ghost.png", "PoisonX.png", "", "", "", "ThemeX.png",
            "", "SealX.png", "BossWaveX.png", "", ""};
    public static String[] pfiles = {"", "", "", "", "", "", "", "", "Curse.png", "", "", "Burrow.png", "Revive.png", "", "", "", "", "", "", "", "Snipe.png", "Time.png", "Seal.png"
            , "Summon.png", "Moving.png", "Theme.png", "Poison.png", "BossWave.png", "CritX.png", "", "", "BCPoison.png"};

    public static int[] colorid = {R.string.sch_wh, R.string.sch_red, R.string.sch_fl, R.string.sch_bla, R.string.sch_me, R.string.sch_an, R.string.sch_al, R.string.sch_zo, R.string.sch_re, R.string.esch_eva, R.string.esch_witch};
    public static int[] starid = {R.string.unit_info_starred, R.string.unit_info_god1, R.string.unit_info_god2, R.string.unit_info_god3};
    public static int[] procid = {R.string.sch_abi_kb, R.string.sch_abi_fr, R.string.sch_abi_sl, R.string.sch_abi_cr, R.string.sch_abi_wv, R.string.sch_abi_we, R.string.sch_abi_bb, R.string.sch_abi_wa, R.string.abi_cu,
            R.string.sch_abi_str, R.string.sch_abi_su, R.string.abi_bu, R.string.abi_rev, R.string.sch_abi_ik, R.string.sch_abi_if, R.string.sch_abi_is, R.string.sch_abi_iwv, R.string.sch_abi_iw, R.string.sch_abi_iwa,
            R.string.sch_abi_ic, R.string.abi_snk, R.string.abi_stt, R.string.abi_seal, R.string.abi_sum, R.string.abi_mvatk, R.string.abi_thch, R.string.abi_poi, R.string.abi_boswv
            , R.string.abi_imcri, R.string.sch_abi_sb, R.string.sch_abi_iv, R.string.sch_abi_poi, R.string.talen_kb, R.string.talen_fr, R.string.talen_sl, R.string.talen_wv, R.string.talen_we, R.string.talen_warp,
            R.string.talen_cu};
    public static int[] abiid = {R.string.sch_abi_st, R.string.sch_abi_re, R.string.sch_abi_md, R.string.sch_abi_ao, R.string.sch_abi_em, R.string.sch_abi_bd, R.string.sch_abi_me, R.string.abi_imvatk, R.string.sch_abi_ws,
            R.string.abi_isnk, R.string.abi_istt, R.string.abi_gh, R.string.abi_ipoi, R.string.sch_abi_zk, R.string.sch_abi_wk, R.string.abi_sui, R.string.abi_ithch, R.string.sch_abi_eva,
            R.string.abi_iseal, R.string.abi_iboswv, R.string.sch_abi_it, R.string.sch_abi_id};
    public static int[] textid = {R.string.unit_info_text0, R.string.unit_info_text1, R.string.unit_info_text2, R.string.unit_info_text3, R.string.unit_info_text4, R.string.unit_info_text5, R.string.unit_info_text6, R.string.unit_info_text7,
            R.string.def_unit_info_text8, R.string.unit_info_text9, R.string.unit_info_text10, R.string.def_unit_info_text11, R.string.def_unit_info_text12, R.string.unit_info_text13,
            R.string.unit_info_text14, R.string.unit_info_text15, R.string.unit_info_text16, R.string.unit_info_text17, R.string.unit_info_text18};

    /**
     * Variables for Unit
     **/
    public static List<Unit> units = null;
    public static int unitnumber;
    public static String[] names = null;
    public static long unitinflistClick = SystemClock.elapsedRealtime();
    public static boolean UisOpen = false;

    public static int unittabposition = 0;
    public static boolean unitinfreset = false;

    /**
     * Variables for Enemy
     **/
    public static List<Enemy> enemies = null;
    public static String[] enames = null;
    public static int emnumber;
    public static long enemyinflistClick = SystemClock.elapsedRealtime();
    public static boolean EisOpen = false;

    /**
     * Variables for Map/Stage
     **/
    public static Map<Integer, MapColc> map = null;
    public static String[][] mapnames = null;
    public static final int[] MAPCODE = {0, 1, 2, 3, 4, 6, 7, 11, 12, 13, 14};
    public static Bitmap[] eicons = null;
    public static long maplistClick = SystemClock.elapsedRealtime();
    public static long stglistClick = SystemClock.elapsedRealtime();
    public static long infoClick = SystemClock.elapsedRealtime();
    public static Bitmap treasure = null;
    public static boolean[] infoOpened = null;
    public static int stageSpinner = -1;
    public static int bgnumber = 0;
    public static long bglistClick = SystemClock.elapsedRealtime();

    /**
     * Variables for Medal
     **/
    public static int medalnumber = 0;
    public static List<Bitmap> medals = null;
    public static MultiLangCont<Integer, String> MEDNAME = new MultiLangCont<>();
    public static MultiLangCont<Integer, String> MEDEXP = new MultiLangCont<>();

    /**
     * Variables for Music
     */
    public static List<String> musicnames = new ArrayList<>();

    /**
     * Variables for Animation
     **/
    public static boolean play = true;
    public static int frame = 0;
    public static int formposition = 0;
    public static int animposition = 0;
    public static int gifFrame = 0;
    public static boolean gifisSaving = false;
    public static boolean enableGIF = false;
    public static boolean keepDoing = true;
    public static ArrayList<Bitmap> frames = new ArrayList<>();

    /**
     * Variables for LineUp
     **/
    public static String[] LUnames = null;
    public static List<BasisSet> sets = null;
    public static boolean LULoading = false;
    public static boolean LUread = false;
    public static int LUtabPosition = 0;
    public static List<Form> currentForms = null;
    public static int[] position = {-1, -1};
    public static List<Combo> combos = new ArrayList<>();
    public static boolean updateList = false;
    public static boolean updateForm = true;
    public static boolean updateTreasure = false;
    public static boolean updateConst = false;
    public static boolean updateCastle = false;
    public static BasisSet set = null;
    public static BasisLU lu = null;
    public static String lineunitname = null;

    /**
     * Search Filter Variables
     **/

    public static ArrayList<String> tg = new ArrayList<>();
    public static ArrayList<String> rare = new ArrayList<>();
    public static ArrayList<ArrayList<Integer>> ability = new ArrayList<>();
    public static ArrayList<String> attack = new ArrayList<>();
    public static boolean tgorand = true;
    public static boolean atksimu = true;
    public static boolean aborand = true;
    public static boolean atkorand = true;
    public static boolean empty = true;
    public static boolean talents = false;
    public static boolean starred = false;

    /**
     * Resets all values stored in StaticStore.
     * It will also reset whole data of BCU.
     */
    public static void clear() {
        bgread = 0;
        unitlang = 1;
        enemeylang = 1;
        stagelang = 1;
        maplang = 1;
        medallang = 1;

        root = 0;

        t = null;
        img15 = null;
        icons = null;
        picons = null;
        fruit = null;
        addition = null;

        units = null;
        unitnumber = 0;
        names = null;
        unitinflistClick = SystemClock.elapsedRealtime();
        UisOpen = false;

        unittabposition = 0;
        unitinfreset = false;

        enemies = null;
        enames = null;
        emnumber = 0;
        enemyinflistClick = SystemClock.elapsedRealtime();
        EisOpen = false;

        medalnumber = 0;
        medals = null;
        MEDNAME.clear();
        MEDEXP.clear();

        musicnames.clear();

        map = null;
        mapnames = null;
        eicons = null;
        maplistClick = SystemClock.elapsedRealtime();
        stglistClick = SystemClock.elapsedRealtime();
        infoClick = SystemClock.elapsedRealtime();
        treasure = null;
        infoOpened = null;
        stageSpinner = -1;
        bgnumber = 0;
        bglistClick = SystemClock.elapsedRealtime();

        LUnames = null;
        sets = null;
        LULoading = false;
        LUread = false;
        LUtabPosition = 0;
        currentForms = null;
        updateForm = true;
        position = new int[]{-1, -1};
        combos = null;
        updateList = false;
        set = null;
        lu = null;
        lineunitname = null;

        play = true;
        frame = 0;
        formposition = 0;
        animposition = 0;

        CommonStatic.clearData();
    }

    /**
     * Gets number of units from file
     */
    public static void getUnitnumber() {
        String unitpath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/unit/";

        File f = new File(unitpath);
        unitnumber = f.listFiles().length;
    }

    /**
     * Gets number of enemies from file
     */
    public static void getEnemynumber() {
        String empath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/enemy/";

        File f = new File(empath);
        emnumber = f.listFiles().length;
    }

    public static Bitmap getResize(Drawable drawable, Context context, float dp) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        return Bitmap.createScaledBitmap(b, (int) px, (int) px, false);
    }

    public static Bitmap getResizeb(Bitmap b, Context context, float dp) {
        if (b == null) return empty(context, dp, dp);

        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        BitmapDrawable bd = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(b, (int) px, (int) px, true));
        bd.setFilterBitmap(true);
        bd.setAntiAlias(true);
        return bd.getBitmap();
    }

    /**
     * Gets resized Bitmap.
     *
     * @param b       Source Bitmap.
     * @param context Used when converting dpi value to pixel value.
     * @param w       Width of generated Bitmap. Must be dpi value.
     * @param h       Height of generated Bitmap. Must be dpi value.
     * @return Returns resized Bitmap using specified dpi value.
     */
    public static Bitmap getResizeb(Bitmap b, Context context, float w, float h) {
        if (b == null) return empty(context, w, h);

        Resources r = context.getResources();
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, r.getDisplayMetrics());
        float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, r.getDisplayMetrics());
        BitmapDrawable bd = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(b, (int) width, (int) height, true));
        bd.setFilterBitmap(true);
        bd.setAntiAlias(true);
        return bd.getBitmap();
    }

    /**
     * Generates empty Bitmap.
     *
     * @param context Used when converting dpi value to pixel value.
     * @param w       Width of generated Bitmap. Must be dpi value.
     * @param h       Height of generated Bitmap. Must be dpi value.
     * @return Returns empty Bitmap using specified dpi value.
     */
    public static Bitmap empty(Context context, float w, float h) {
        Resources r = context.getResources();
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, r.getDisplayMetrics());
        float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, r.getDisplayMetrics());
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        return Bitmap.createBitmap((int) width, (int) height, conf);
    }

    /**
     * Generates empty Bitmap.
     *
     * @param w Width of generated Bitmap. Must be pixel value.
     * @param h Height of generated Bitmap. Must be pixel value.
     * @return Returns empty Bitmap using specified pixel value.
     */
    public static Bitmap empty(int w, int h) {
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    }

    /**
     * Gets resized bitmap using antialiasing.
     *
     * @param b       Source Bitmap.
     * @param context Used when initializing BitmapDrawable.
     * @param w       Width of generated Bitmap. Must be pixel value.
     * @param h       Height of generated Bitmap. Must be pixel value.
     * @return Returns resized bitmap using antialiasing.
     */
    public static Bitmap getResizebp(Bitmap b, Context context, float w, float h) {
        BitmapDrawable bd = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(b, (int) w, (int) h, true));
        bd.setFilterBitmap(true);
        bd.setAntiAlias(true);
        return bd.getBitmap();
    }

    /**
     * Gets resized bitmap.
     *
     * @param b Source Bitmap.
     * @param w Width of generated Bitmap. Must be pixel value.
     * @param h Height of generated Bitmap. Must be pixel value.
     * @return Returns resized bitmap using specified width and height.
     */
    public static Bitmap getResizebp(Bitmap b, float w, float h) {
        Matrix matrix = new Matrix();

        if (w < 0 || h < 0) {
            if (w < 0 && h < 0) {
                matrix.setScale(-1, -1);
            } else if (w < 0) {
                matrix.setScale(-1, 1);
            } else if (h < 0) {
                matrix.setScale(1, -1);
            }
        }

        Bitmap reversed = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);

        reversed = Bitmap.createScaledBitmap(reversed, (int) Math.abs(w), (int) Math.abs(h), true);

        return reversed;
    }

    public static int dptopx(float dp, Context context) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    /**
     * Saves img15 as cut state by img015.imgcut.
     */
    public static void readImg() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/page/img015.png";
        String imgcut = "./org/page/img015.imgcut";
        File f = new File(path);
        ImgCut img = ImgCut.newIns(imgcut);
        try {
            FakeImage png = FakeImage.read(f);
            img15 = img.cut(png);
        } catch (IOException e) {
            e.printStackTrace();
            img15 = null;
        }
    }

    /**
     * Reads Treasure Radar icon.
     */
    public static void readTreasureIcon() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/page/img002.png";
        String imgcut = "./org/page/img002.imgcut";
        File f = new File(path);
        ImgCut img = ImgCut.newIns(imgcut);

        try {
            FakeImage png = FakeImage.read(f);
            FakeImage[] imgs = img.cut(png);
            treasure = (Bitmap) imgs[28].bimg();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Decides CommonStatic.lang value.
     *
     * @param lan Code of language refers to StaticStore.lang.
     *            0 is Auto.
     */
    public static void getLang(int lan) {
        String language;

        if (lan == 0) {
            language = Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
            CommonStatic.Lang.lang = Arrays.asList(lang).indexOf(language) - 1;
            System.out.println("Auto Set : " + language);
        } else {
            System.out.println(lang[lan]);
            CommonStatic.Lang.lang = lan - 1;
        }
        System.out.println(CommonStatic.Lang.lang);

        if (CommonStatic.Lang.lang >= 4 || CommonStatic.Lang.lang < 0)
            CommonStatic.Lang.lang = 0;
    }

    /**
     * Resets entity filter data.
     * Must be called when exiting Entity list.
     */
    public static void filterReset() {
        tg = new ArrayList<>();
        rare = new ArrayList<>();
        ability = new ArrayList<>();
        attack = new ArrayList<>();
        tgorand = true;
        atksimu = true;
        aborand = true;
        atkorand = true;
        empty = true;
        talents = false;
        starred = false;
    }

    /**
     * Gets possible position in specific lineup.
     *
     * @param f Arrays of forms in Lineup.
     * @return Returns first empty position in Lineup.
     * If Lineup is full, it will return position of replacing area.
     */
    public static int[] getPossiblePosition(Form[][] f) {
        for (int i = 0; i < f.length; i++) {
            for (int j = 0; j < f[i].length; j++) {
                if (f[i][j] == null)
                    return new int[]{i, j};
            }
        }

        return new int[]{100, 100};
    }

    /**
     * Get Color value using Attr ID.
     *
     * @param context     Decides TypedValue using Theme from Context.
     * @param attributeId ID of color from Attr. Format must be color.
     * @return Gets real ID of color considering Theme.
     * It will return Color value as Hex.
     */
    public static int getAttributeColor(Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = ContextCompat.getColor(context, colorRes);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return color;
    }


    /**
     * Generate Bitmap from Vector Asset.
     * Icon's tint color is ?attr/TextPrimary.
     *
     * @param context Get drawable and set tint color to it.
     * @param vectid  Id from Vector Asset. Use "R.drawable._ID_".
     * @return Returns created Bitmap using Vector Asset.
     * If vectid returns null, then it will generate empty icon.
     */
    public static Bitmap getBitmapFromVector(Context context, int vectid) {
        Drawable drawable = context.getDrawable(vectid);

        if (drawable == null) return empty(context, 100, 100);

        drawable.setTint(getAttributeColor(context, R.attr.TextPrimary));

        Bitmap res = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(res);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return res;
    }

    /**
     * Check if specified icon has different width and height.
     * If they are different, it will generate icon which has same width and height.
     * Width and Height's default value is 128 pixels.
     *
     * @param context Using Context to convert dpi value to pixel value.
     * @param b       Source Bitmap.
     * @param wh      This parameter decides width and height of created icon.
     *                It must be dpi value.
     * @return If source has same width and height, it will return source.
     * If not, it will return icon which has same width and height.
     */
    public static Bitmap MakeIcon(Context context, Bitmap b, float wh) {
        if (b == null) return empty(context, 24f, 24f);

        if (b.getHeight() == b.getWidth()) return getResizeb(b, context, wh);

        Bitmap before = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(before);
        Paint p = new Paint();

        c.drawBitmap(b, 64 - (b.getWidth() / 2f), 64 - (b.getHeight() / 2f), p);

        return getResizeb(before, context, wh);
    }

    /**
     * Check if specified icon has different width and height.
     * If they are different, it will generate icon which has same width and height.
     * Width and Height's default value is 128 pixels.
     *
     * @param b  Source Bitmap.
     * @param wh This parameter decides width and height of created icon.
     *           It must be pixel value.
     * @return If source has same width and height, it will return source.
     * If not, it will return icon which has same width and height.
     */
    public static Bitmap MakeIconp(Bitmap b, float wh) {
        if (b == null) return empty(128, 128);

        if (b.getHeight() == b.getWidth()) return getResizebp(b, wh, wh);

        Bitmap before = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(before);
        Paint p = new Paint();

        c.drawBitmap(b, 64 - (b.getWidth() / 2f), 64 - (b.getHeight() / 2f), p);

        if (wh == 128) return before;

        return getResizebp(before, wh, wh);
    }

    /**
     * Saves lineup file.
     */
    public static void SaveLineUp() {
        String Path = Environment.getExternalStorageDirectory().getPath() + "/BCU/user/basis.v";
        String Direct = Environment.getExternalStorageDirectory().getPath() + "/BCU/user/";

        File g = new File(Direct);

        if (!g.exists())
            g.mkdirs();

        File f = new File(Path);

        try {
            if (!f.exists())
                f.createNewFile();

            OutputStream os = new FileOutputStream(f);

            OutStream out = BasisSet.writeAll();

            out.flush(os);

            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get RGB value from specified HEX color value.
     *
     * @param hex Color HEX value which will be converted to RGB values.
     * @return Return as three integer array, first is R, second is G, and third is B.
     */
    public static int[] getRGB(final int hex) {
        int r = (hex & 0xFF0000) >> 16;
        int g = (hex & 0xFF00) >> 8;
        int b = (hex & 0xFF);
        return new int[]{r, g, b};
    }

    /**
     * Get scaled volume value considering log calculation.
     *
     * @param vol This parameter must be 0 ~ 99.
     *            If vol is lower than 0, then it will consider as 0.
     *            If vol is larger than 99, then it will consider as 99.
     * @return Volume is scaled as logarithmically, it will return calculated value.
     */
    public static float getVolumScaler(int vol) {
        if (vol < 0) vol = 0;
        if (vol >= 100) vol = 99;

        return (float) (1 - (Math.log(100 - vol) / Math.log(100)));
    }

    public static void showShortMessage(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            toast.setText(msg);
            toast.show();
        }
    }

    public static void showShortMessage(Context context, int resid) {
        String msg = context.getString(resid);

        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            toast.setText(msg);
            toast.show();
        }
    }
}
