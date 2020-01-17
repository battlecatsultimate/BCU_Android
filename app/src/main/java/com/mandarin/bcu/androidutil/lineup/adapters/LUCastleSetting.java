package com.mandarin.bcu.androidutil.lineup.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import common.battle.BasisSet;

public class LUCastleSetting extends Fragment {
    View view;

    public static LUCastleSetting newInstance() {
        return new LUCastleSetting();
    }

    private boolean destroyed = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup group, @Nullable Bundle bundle) {
        view = inflater.inflate(R.layout.lineup_castle_set,group,false);

        ImageView castle = view.findViewById(R.id.lineupcastle);

        drawCastle(castle);

        Button[] buttons = {view.findViewById(R.id.lineupchcannon),view.findViewById(R.id.lineupchlabel),view.findViewById(R.id.lineupchbase)};

        for(int i = 0; i < buttons.length; i++) {
            final int ii = i;

            buttons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setNyb(ii,castle);
                }
            });
        }

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(StaticStore.updateCastle) {
                    drawCastle(castle);

                    StaticStore.updateCastle = false;
                }

                if(!destroyed)
                    handler.postDelayed(this,50);
            }
        };

        handler.postDelayed(runnable,50);

        return view;
    }

    private void setNyb(int index, ImageView img) {
        if(index >= 3) return;

        if(BasisSet.current.sele.nyc[index] == 6)
            BasisSet.current.sele.nyc[index] = 0;
        else
            BasisSet.current.sele.nyc[index]++;

        drawCastle(img);
    }

    private void drawCastle(ImageView img) {
        int [] data = BasisSet.current.sele.nyc;

        if(data == null)
            data = new int [] {0,0,0};

        Bitmap result = Bitmap.createBitmap(128,256, Bitmap.Config.ARGB_8888);

        String Path = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU/files/org/castle/";

        String cannon = Path + "000/nyankoCastle_000_0"+data[0]+".png";
        String label = Path + "002/nyankoCastle_002_0"+data[1]+".png";
        String base = Path + "003/nyankoCastle_003_0"+data[2]+".png";

        Bitmap cb = BitmapFactory.decodeFile(cannon);
        Bitmap lb = BitmapFactory.decodeFile(label);
        Bitmap bb = BitmapFactory.decodeFile(base);

        Canvas c = new Canvas(result);
        Paint p = new Paint();

        c.drawBitmap(bb,0,125,p);
        c.drawBitmap(cb,0,0,p);
        c.drawBitmap(lb,0,128,p);

        img.setImageBitmap(result);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyed = true;
    }
}
