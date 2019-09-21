package com.mandarin.bcu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.mandarin.bcu.androidutil.StaticStore;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Queue;

import common.system.fake.FakeImage;
import common.system.files.VFile;
import common.util.anim.ImgCut;
import common.util.stage.Limit;

public class ImageViewer extends AppCompatActivity {
    private final int BG = 0;
    private final int CASTLE = 1;

    private String path  = null;
    private int img = -1;
    private int bgnum = -1;

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
        }

        ImageButton bck = findViewById(R.id.imgviewerbck);

        bck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        switch(img) {
            case BG:

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
                    for (int i = 0; i < 1 + width / getImg(8).getWidth(); i++) {
                        canvas.drawBitmap(getImg2(), getImg(8).getWidth() * i, height - 2 * getImg(8).getHeight(), paint);
                        canvas.drawBitmap(getImg(8), getImg(8).getWidth() * i, height - getImg(8).getHeight(), paint);
                    }

                    Shader shader = new LinearGradient(0,0,0,height-2*getImg(8).getHeight(),getSkyUpper(),getSkyBelow(), Shader.TileMode.CLAMP);
                    paint.setShader(shader);
                    canvas.drawRect(new RectF(0,0,width,height-2*getImg(8).getHeight()),paint);
                } else {
                    for (int i = 0; i < 1 + width / getImg(4).getWidth(); i++) {
                        canvas.drawBitmap(getImg(4), getImg(4).getWidth() * i, height - getImg(4).getHeight(), paint);
                    }

                    Shader shader = new LinearGradient(0,0,0,height-getImg(4).getHeight(),getSkyUpper(),getSkyBelow(), Shader.TileMode.CLAMP);
                    paint.setShader(shader);
                    canvas.drawRect(new RectF(0,0,width,height-getImg(4).getHeight()),paint);
                }

                ImageView img = findViewById(R.id.imgviewerimg);

                img.setImageBitmap(b);

                break;
            case CASTLE:
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
                set.connect(img.getId(),ConstraintSet.TOP,toolbar.getId(),ConstraintSet.BOTTOM,8);
                set.connect(img.getId(),ConstraintSet.BOTTOM,constraintLayout.getId(),ConstraintSet.BOTTOM,8);
                set.connect(img.getId(),ConstraintSet.LEFT,constraintLayout.getId(),ConstraintSet.LEFT,8);
                set.connect(img.getId(),ConstraintSet.RIGHT,constraintLayout.getId(),ConstraintSet.RIGHT,8);
                set.applyTo(constraintLayout);

                Bitmap castle = StaticStore.getResizeb(bd.getBitmap(),this,bd.getBitmap().getWidth(),bd.getBitmap().getHeight());

                img.setImageBitmap(castle);

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
    private Bitmap getImg(float param) {
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
            return StaticStore.getResizeb(b,this,StaticStore.dptopx(b.getWidth()/param,this),StaticStore.dptopx(b.getHeight()/param,this));
        } catch (IOException e) {
            e.printStackTrace();

            return StaticStore.empty(this,100f,100f);
        }
    }

    @NonNull
    private Bitmap getImg2() {
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
            return StaticStore.getResizeb(b,this,StaticStore.dptopx(b.getWidth()/ (float) 8,this),StaticStore.dptopx(b.getHeight()/ (float) 8,this));
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
}
