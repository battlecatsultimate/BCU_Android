package com.mandarin.bcu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.androidutil.Revalidater;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.animation.asynchs.EAnimationLoader;
import com.mandarin.bcu.androidutil.animation.asynchs.UAnimationLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;

import common.system.fake.FakeImage;
import common.system.files.VFile;
import common.util.anim.ImgCut;

public class ImageViewer extends AppCompatActivity {
    private final int BG = 0;
    private final int CASTLE = 1;
    private final int ANIMU = 2;
    private final int ANIME = 3;

    private String path  = null;
    private int img = -1;
    private int bgnum = -1;

    private int id = -1;
    private int form = -1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences shared = getSharedPreferences("configuration",MODE_PRIVATE);
        SharedPreferences.Editor ed;
        if(!shared.contains("initial")) {
            ed = shared.edit();
            ed.putBoolean("initial",true);
            ed.putBoolean("theme",true);
            ed.apply();
        } else {
            if(!shared.getBoolean("theme",false)) {
                setTheme(R.style.AppTheme_night);
            } else {
                setTheme(R.style.AppTheme_day);
            }
        }

        if(shared.getInt("Orientation",0) == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else if(shared.getInt("Orientation",0) == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        else if(shared.getInt("Orientation",0) == 0)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        setContentView(R.layout.activity_image_viewer);

        Intent result = getIntent();

        if(result.getExtras() != null) {
            Bundle extra = result.getExtras();

            path = extra.getString("Path");
            img = extra.getInt("Img");
            bgnum = extra.getInt("BGNum");

            id = extra.getInt("ID");
            form = extra.getInt("Form");
        }

        ImageButton bck = findViewById(R.id.imgviewerbck);

        TableRow row = findViewById(R.id.palyrow);
        SeekBar seekBar = findViewById(R.id.animframeseek);
        TextView frame = findViewById(R.id.animframe);
        TextView fpsind = findViewById(R.id.imgviewerfps);
        TextView gif = findViewById(R.id.imgviewergiffr);
        ProgressBar prog = findViewById(R.id.imgviewerprog);

        bck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticStore.play = true;
                StaticStore.frame = 0;
                StaticStore.animposition = 0;
                StaticStore.formposition = 0;
                finish();
            }
        });

        Spinner anims = findViewById(R.id.animselect);

        ImageButton option = findViewById(R.id.imgvieweroption);

        switch(img) {
            case BG:
                row.setVisibility(View.GONE);
                seekBar.setVisibility(View.GONE);
                frame.setVisibility(View.GONE);
                anims.setVisibility(View.GONE);
                fpsind.setVisibility(View.GONE);
                gif.setVisibility(View.GONE);
                prog.setVisibility(View.GONE);

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                int width = size.x;
                int height = size.y;

                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

                paint.setFilterBitmap(true);
                paint.setAntiAlias(true);
                paint.setDither(true);

                Bitmap b = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(b);

                if(getImgcut() == 1 || getImgcut() == 8) {
                    Bitmap b1 = getImg(b.getHeight(),2);
                    Bitmap b2 = getImg2(b.getHeight(),2);

                    int h = b1.getHeight();
                    int w = b1.getWidth();

                    for (int i = 0; i < 1 + width / w; i++) {
                        canvas.drawBitmap(b2, w * i, height - 2 * h, paint);
                        canvas.drawBitmap(b1, w * i, height - h, paint);
                    }
                } else {
                    Bitmap b1 = getImg(b.getHeight(),2);

                    int h = b1.getHeight();
                    int w = b1.getWidth();

                    for (int i = 0; i < 1 + width / w; i++) {
                        canvas.drawBitmap(b1, w * i, height - h, paint);
                    }

                    Shader shader = new LinearGradient(0,0,0,height-h,getSkyUpper(),getSkyBelow(), Shader.TileMode.CLAMP);
                    paint.setShader(shader);
                    canvas.drawRect(new RectF(0,0,width,height-h),paint);
                }

                ImageView img = findViewById(R.id.imgviewerimg);

                img.setImageBitmap(b);

                PopupMenu popup = new PopupMenu(this,option);
                Menu menu = popup.getMenu();
                popup.getMenuInflater().inflate(R.menu.bg_menu,menu);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.anim_option_png) {
                            String path = Environment.getExternalStorageDirectory().getPath()+"/BCU/img/";

                            File f = new File(path);

                            if(!f.exists())
                                f.mkdirs();

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
                            Date date = new Date();

                            String name = dateFormat.format(date)+"-BG-"+bgnum+".png";

                            File g = new File(path,name);

                            try {
                                if(!g.exists())
                                    g.createNewFile();

                                FileOutputStream fos = new FileOutputStream(g);

                                b.compress(Bitmap.CompressFormat.PNG,100,fos);

                                fos.close();

                                Toast.makeText(ImageViewer.this,getString(R.string.anim_png_success).replace("-","/BCU/img/"+name),Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();

                                Toast.makeText(ImageViewer.this,R.string.anim_png_fail,Toast.LENGTH_SHORT).show();
                            }
                        }

                        return false;
                    }
                });

                option.setOnClickListener(new SingleClick() {
                    @Override
                    public void onSingleClick(View v) {
                        popup.show();
                    }
                });

                break;
            case CASTLE:
                anims.setVisibility(View.GONE);
                row.setVisibility(View.GONE);
                seekBar.setVisibility(View.GONE);
                frame.setVisibility(View.GONE);
                option.setVisibility(View.GONE);
                fpsind.setVisibility(View.GONE);
                gif.setVisibility(View.GONE);
                prog.setVisibility(View.GONE);

                Bitmap b2 = (Bitmap) Objects.requireNonNull(VFile.getFile(path)).getData().getImg().bimg();

                BitmapDrawable bd = new BitmapDrawable(getResources(),b2);

                bd.setFilterBitmap(true);
                bd.setAntiAlias(true);

                img = findViewById(R.id.imgviewerimg);

                ConstraintLayout constraintLayout = findViewById(R.id.imglayout);
                Toolbar toolbar = findViewById(R.id.toolbar7);

                img.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                ConstraintSet set = new ConstraintSet();

                set.clone(constraintLayout);
                set.connect(img.getId(),ConstraintSet.TOP,toolbar.getId(),ConstraintSet.BOTTOM,4);
                set.connect(img.getId(),ConstraintSet.BOTTOM,constraintLayout.getId(),ConstraintSet.BOTTOM,4);
                set.connect(img.getId(),ConstraintSet.LEFT,constraintLayout.getId(),ConstraintSet.LEFT,4);
                set.connect(img.getId(),ConstraintSet.RIGHT,constraintLayout.getId(),ConstraintSet.RIGHT,4);
                set.applyTo(constraintLayout);

                Bitmap castle = StaticStore.getResizeb(bd.getBitmap(),this,bd.getBitmap().getWidth(),bd.getBitmap().getHeight());

                img.setImageBitmap(castle);

                break;
            case ANIMU:
                new UAnimationLoader(this,id,form).execute();

                break;
            case ANIME:
                new EAnimationLoader(this,id).execute();

                break;
        }
    }

    private String [] getData() {
        String datapath = "./org/battle/bg/bg.csv";
        Queue<String> qs = Objects.requireNonNull(VFile.getFile(datapath)).getData().readLine();

        for(String s : qs) {
            String [] data = s.trim().split(",");

            try {
                if (Integer.parseInt(data[0]) == bgnum)
                    return data;
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    @NonNull
    private Bitmap getImg(int height, float param) {
        String[] data = getData();

        if (data == null) return StaticStore.empty(this,100f,100f);

        String imgPath;

        if(Integer.parseInt(data[13]) == 8)
            imgPath = "./org/battle/bg/bg0"+1+".imgcut";
        else
            imgPath = "./org/battle/bg/bg0"+data[13]+".imgcut";

        ImgCut img = ImgCut.newIns(imgPath);

        File f = new File(path);

        try {
            FakeImage png = FakeImage.read(f);
            FakeImage[] imgs = img.cut(png);
            Bitmap b = (Bitmap)imgs[0].bimg();

            float ratio = (height/param)/b.getHeight();

            return StaticStore.getResizebp(b,this,ratio*b.getWidth(),ratio*b.getHeight());
        } catch (IOException e) {
            e.printStackTrace();

            return StaticStore.empty(this,100f,100f);
        }
    }

    @NonNull
    private Bitmap getImg2(int height, float param) {
        String[] data = getData();

        if (data == null) return StaticStore.empty(this,100f,100f);

        String imgPath;

        if(Integer.parseInt(data[13]) == 8)
            imgPath = "./org/battle/bg/bg0"+1+".imgcut";
        else
            imgPath = "./org/battle/bg/bg0"+data[13]+".imgcut";

        ImgCut img = ImgCut.newIns(imgPath);

        File f = new File(path);

        try {
            FakeImage png = FakeImage.read(f);
            FakeImage[] imgs = img.cut(png);
            Bitmap b = (Bitmap)imgs[20].bimg();

            float ratio = (height/param)/b.getHeight();

            return StaticStore.getResizebp(b,this,ratio*b.getWidth(),ratio*b.getHeight());
        } catch (IOException e) {
            e.printStackTrace();

            return StaticStore.empty(this,100f,100f);
        }
    }

    private int getImgcut() {
        String [] data = getData();

        if(data == null) return -1;

        return Integer.parseInt(data[13]);
    }

    private int getSkyUpper() {
        String [] data = getData();

        if(data == null) return 0;

        int R = Integer.parseInt(data[1]);
        int G = Integer.parseInt(data[2]);
        int B = Integer.parseInt(data[3]);

        return Color.rgb(R,G,B);
    }

    private int getSkyBelow() {
        String [] data = getData();

        if(data == null) return 0;

        int R = Integer.parseInt(data[4]);
        int G = Integer.parseInt(data[5]);
        int B = Integer.parseInt(data[6]);

        return Color.rgb(R,G,B);
    }

    private int getGroundUpper() {
        String [] data = getData();

        if(data == null) return 0;

        int R = Integer.parseInt(data[7]);
        int G = Integer.parseInt(data[8]);
        int B = Integer.parseInt(data[9]);

        return Color.rgb(R,G,B);
    }

    private int getGroundBelow() {
        String [] data = getData();

        if(data == null) return 0;

        int R = Integer.parseInt(data[10]);
        int G = Integer.parseInt(data[11]);
        int B = Integer.parseInt(data[12]);

        return Color.rgb(R,G,B);
    }

    @Override
    public void onBackPressed() {
        ImageButton bck = findViewById(R.id.imgviewerbck);

        bck.performClick();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences shared = newBase.getSharedPreferences("configuration",Context.MODE_PRIVATE);
        super.attachBaseContext(Revalidater.LangChange(newBase,shared.getInt("Language",0)));
    }
}
