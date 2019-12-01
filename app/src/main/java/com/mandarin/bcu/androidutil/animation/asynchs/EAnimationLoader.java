package com.mandarin.bcu.androidutil.animation.asynchs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.SingleClick;
import com.mandarin.bcu.androidutil.animation.AnimationCView;
import com.mandarin.bcu.androidutil.enemy.EDefiner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import common.system.MultiLangCont;
import common.system.files.VFile;

public class EAnimationLoader extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakReference;
    private final int id;
    private final int [] animS = {R.string.anim_move,R.string.anim_wait,R.string.anim_atk,R.string.anim_kb,R.string.anim_burrow,R.string.anim_under,R.string.anim_burrowup};
    
    public EAnimationLoader(Activity activity, int id) {
        this.weakReference = new WeakReference<>(activity);
        this.id = id;
    }
    
    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        if(activity == null) return;

        Spinner anims = activity.findViewById(R.id.animselect);
        Spinner forms = activity.findViewById(R.id.formselect);
        ImageButton setting = activity.findViewById(R.id.imgvieweroption);
        TableRow player = activity.findViewById(R.id.palyrow);
        SeekBar controller = activity.findViewById(R.id.animframeseek);
        TextView frame = activity.findViewById(R.id.animframe);
        TextView fps = activity.findViewById(R.id.imgviewerfps);
        TextView gif = activity.findViewById(R.id.imgviewergiffr);
        LinearLayout cViewlayout = activity.findViewById(R.id.imgviewerln);
        ImageView img = activity.findViewById(R.id.imgviewerimg);
        TextView loadst = activity.findViewById(R.id.imgviewerst);

        loadst.setText(R.string.stg_info_enem);

        setDisappear(anims,forms,setting,player,controller,frame,fps,gif,cViewlayout,img);
    }
    
    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();
        
        if(activity == null) return null;

        new EDefiner().define(activity);

        publishProgress(0);

        if(StaticStore.enames == null) {
            StaticStore.enames = new String[StaticStore.emnumber];

            for(int i = 0;i<StaticStore.emnumber;i++) {
                StaticStore.enames[i] = withID(i, MultiLangCont.ENAME.getCont(StaticStore.enemies.get(i)));
            }
        }

        publishProgress(1);

        if(StaticStore.ebitmaps == null) {
            StaticStore.ebitmaps = new Bitmap[StaticStore.emnumber];

            for(int i = 0;i < StaticStore.emnumber;i++) {
                String shortPath = "./org/enemy/"+number(i)+"/edi_"+number(i)+".png";

                try {
                    float ratio = 32f/32f;
                    StaticStore.ebitmaps[i] = StaticStore.getResizeb((Bitmap) Objects.requireNonNull(VFile.getFile(shortPath)).getData().getImg().bimg(), activity, 85f*ratio, 32f*ratio);
                } catch(NullPointerException e) {
                    float ratio = 32f/32f;
                    StaticStore.ebitmaps[i] = StaticStore.empty(activity, 85f*ratio, 32f*ratio);
                }
            }
        }
        
        publishProgress(2);
        
        return null;
    }
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onProgressUpdate(Integer... result) {
        Activity activity = weakReference.get();
        
        if(activity == null) return;
        
        TextView st = activity.findViewById(R.id.imgviewerst);
        
        switch (result[0]) {
            case 0:
                st.setText(R.string.stg_info_enemname);
                break;
            case 1:
                st.setText(R.string.stg_info_enemimg);
                break;
            case 2:
                Spinner anims = activity.findViewById(R.id.animselect);
                SeekBar controller = activity.findViewById(R.id.animframeseek);
                TextView frame = activity.findViewById(R.id.animframe);
                TextView fps = activity.findViewById(R.id.imgviewerfps);
                TextView gif = activity.findViewById(R.id.imgviewergiffr);
                FloatingActionButton[] buttons = {activity.findViewById(R.id.animbackward),activity.findViewById(R.id.animplay),activity.findViewById(R.id.animforward)};
                LinearLayout cViewlayout = activity.findViewById(R.id.imgviewerln);
                ImageButton option = activity.findViewById(R.id.imgvieweroption);

                SharedPreferences shared = activity.getSharedPreferences("configuration", Context.MODE_PRIVATE);

                AnimationCView cView = new AnimationCView(activity,id,0,!shared.getBoolean("theme",false),shared.getBoolean("Axis",true),frame,controller,fps,gif);
                cView.siz = (float)StaticStore.dptopx(1f,activity)/1.25f;

                ScaleGestureDetector detector = new ScaleGestureDetector(activity,new ScaleListener(cView));

                cView.setOnTouchListener(new View.OnTouchListener() {
                    int preid = -1;
                    float preX;
                    float preY;
                    
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        detector.onTouchEvent(event);

                        if(preid == -1)
                            preid = event.getPointerId(0);

                        int id = event.getPointerId(0);

                        float x = event.getX();
                        float y = event.getY();

                        if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            if (event.getPointerCount() == 1 && id == preid) {
                                float dx = x - preX;
                                float dy = y - preY;

                                cView.x += dx;
                                cView.y += dy;
                            }
                        }

                        preX = x;
                        preY = y;

                        preid = id;

                        return true;
                    }
                });

                List<String> name = new ArrayList<>();

                for(int i = 0; i < StaticStore.enemies.get(id).anim.anims.length; i++) {
                    name.add(activity.getString(animS[i]));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,R.layout.spinneradapter,name);
                
                anims.setAdapter(adapter);

                cView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                cViewlayout.addView(cView);

                anims.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(StaticStore.animposition != position) {
                            StaticStore.animposition = position;
                            cView.anim.changeAnim(position);
                            controller.setMax(cView.anim.len());
                            controller.setProgress(0);
                            StaticStore.frame = 0;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                buttons[1].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        frame.setTextColor(getAttributeColor(activity,R.attr.TextPrimary));

                        if(StaticStore.play) {
                            buttons[1].setImageDrawable(activity.getDrawable(R.drawable.ic_pause_black_24dp));
                            buttons[0].show();
                            buttons[2].show();
                            controller.setEnabled(true);
                        } else {
                            buttons[1].setImageDrawable(activity.getDrawable(R.drawable.ic_play_arrow_black_24dp));
                            buttons[0].hide();
                            buttons[2].hide();
                            controller.setEnabled(false);
                        }

                        StaticStore.play = !StaticStore.play;
                    }
                });

                buttons[0].setOnClickListener(new View.OnClickListener() {
                    Toast toast;
                    
                    @Override
                    public void onClick(View v) {
                        if(StaticStore.frame > 0) {
                            StaticStore.frame--;
                            cView.anim.setTime(StaticStore.frame);
                        } else {
                            frame.setTextColor(Color.rgb(227, 66, 66));

                            toast = Toast.makeText(activity,R.string.anim_warn_frame,Toast.LENGTH_SHORT);

                            if(toast.getView().isShown())
                                toast.show();
                        }
                    }
                });

                buttons[2].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StaticStore.frame++;
                        cView.anim.setTime(StaticStore.frame);
                        frame.setTextColor(getAttributeColor(activity,R.attr.TextPrimary));
                    }
                });

                controller.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar controller, int progress, boolean fromUser) {
                        if(fromUser) {
                            StaticStore.frame = progress;
                            cView.anim.setTime(StaticStore.frame);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar controller) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar controller) {

                    }
                });
                
                frame.setText(activity.getString(R.string.anim_frame).replace("-",""+StaticStore.frame));
                controller.setProgress(StaticStore.frame);
                anims.setSelection(StaticStore.animposition);
                cView.anim.changeAnim(StaticStore.animposition);
                cView.anim.setTime(StaticStore.frame);
                controller.setMax(cView.anim.len());

                PopupMenu popup = new PopupMenu(activity,option);
                Menu menu = popup.getMenu();
                popup.getMenuInflater().inflate(R.menu.animation_menu,menu);

                if(StaticStore.enableGIF)
                    popup.getMenu().getItem(3).setTitle(R.string.anim_option_gifstop);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()) {
                            case R.id.anim_option_reset:
                                cView.x = 0;
                                cView.y = 0;
                                cView.siz = StaticStore.dptopx(1f,activity)/1.25f;

                                return true;
                            case R.id.anim_option_png:
                                Bitmap b = Bitmap.createBitmap(cView.getWidth(),cView.getHeight(), Bitmap.Config.ARGB_8888);
                                Canvas c = new Canvas(b);
                                Paint p = new Paint();

                                if(!shared.getBoolean("theme",false))
                                    p.setColor(Color.argb(255,54,54,54));
                                else
                                    p.setColor(Color.argb(255,255,255,255));

                                c.drawRect(0,0,b.getWidth(),b.getHeight(),p);
                                cView.draw(c);

                                String path = Environment.getExternalStorageDirectory().getPath()+"/BCU/img/";

                                File g = new File(path);

                                if(!g.exists())
                                    g.mkdirs();

                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
                                Date date = new Date();

                                String name = dateFormat.format(date)+"-E-"+id+".png";

                                File f = new File(path,name);

                                try {
                                    if(!f.exists())
                                        f.createNewFile();

                                    FileOutputStream fos = new FileOutputStream(f);

                                    b.compress(Bitmap.CompressFormat.PNG,100,fos);

                                    fos.close();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                return true;
                            case R.id.anim_option_pngtr:
                                b = Bitmap.createBitmap(cView.getWidth(),cView.getHeight(), Bitmap.Config.ARGB_8888);
                                c = new Canvas(b);
                                p = new Paint();

                                if(!shared.getBoolean("theme",false))
                                    p.setColor(Color.argb(255,54,54,54));
                                else
                                    p.setColor(Color.argb(255,255,255,255));

                                cView.trans = true;
                                cView.draw(c);
                                cView.trans = false;

                                path = Environment.getExternalStorageDirectory().getPath()+"/BCU/img/";

                                g = new File(path);

                                if(!g.exists())
                                    g.mkdirs();

                                dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
                                date = new Date();

                                name = dateFormat.format(date)+"-E-Trans-"+id+".png";

                                f = new File(path,name);

                                try {
                                    if(!f.exists())
                                        f.createNewFile();

                                    FileOutputStream fos = new FileOutputStream(f);

                                    b.compress(Bitmap.CompressFormat.PNG,100,fos);

                                    fos.close();

                                    Toast.makeText(activity,activity.getString(R.string.anim_png_success).replace("-","/BCU/img/"+name),Toast.LENGTH_SHORT).show();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    Toast.makeText(activity,R.string.anim_png_fail,Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(activity,R.string.anim_png_fail,Toast.LENGTH_SHORT).show();
                                }

                                return true;
                            case R.id.anim_option_gif:
                                if(!StaticStore.gifisSaving) {
                                    if (!StaticStore.enableGIF) {
                                        gif.setVisibility(View.VISIBLE);
                                        item.setTitle(R.string.anim_option_gifstop);
                                    } else {
                                        item.setTitle(R.string.anim_option_gifstart);
                                        cView.StartAsync(activity);
                                        StaticStore.gifisSaving = true;
                                    }

                                    StaticStore.enableGIF = !StaticStore.enableGIF;
                                } else {
                                    Toast.makeText(activity,R.string.gif_saving,Toast.LENGTH_SHORT).show();
                                }

                                return true;
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

                ImageButton bck = activity.findViewById(R.id.imgviewerbck);

                bck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!StaticStore.gifisSaving) {
                            StaticStore.play = true;
                            StaticStore.frame = 0;
                            StaticStore.animposition = 0;
                            StaticStore.formposition = 0;
                            StaticStore.enableGIF = false;
                            StaticStore.gifFrame = 0;
                            StaticStore.frames.clear();
                            activity.finish();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle(R.string.anim_gif_warn);
                            builder.setMessage(R.string.anim_gif_recording);
                            builder.setPositiveButton(R.string.main_file_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    StaticStore.play = true;
                                    StaticStore.frame = 0;
                                    StaticStore.animposition = 0;
                                    StaticStore.formposition = 0;
                                    StaticStore.keepDoing = false;
                                    activity.finish();
                                }
                            });
                            builder.setNegativeButton(R.string.main_file_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
        }
    }
    
    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();
        
        if(activity == null) return;

        ProgressBar prog = activity.findViewById(R.id.imgviewerprog);
        TextView st = activity.findViewById(R.id.imgviewerst);

        setDisappear(prog,st);

        Spinner anims = activity.findViewById(R.id.animselect);
        ImageButton setting = activity.findViewById(R.id.imgvieweroption);
        TableRow player = activity.findViewById(R.id.palyrow);
        SeekBar controller = activity.findViewById(R.id.animframeseek);
        TextView frame = activity.findViewById(R.id.animframe);
        TextView fps = activity.findViewById(R.id.imgviewerfps);
        TextView gif = activity.findViewById(R.id.imgviewergiffr);
        LinearLayout cViewlayout = activity.findViewById(R.id.imgviewerln);
        FloatingActionButton [] buttons = {activity.findViewById(R.id.animbackward),activity.findViewById(R.id.animplay),activity.findViewById(R.id.animforward)};

        SharedPreferences shared = activity.getSharedPreferences("configuration",Context.MODE_PRIVATE);

        setAppear(anims,setting,player,controller,frame,fps,cViewlayout);

        if(StaticStore.enableGIF || StaticStore.gifisSaving)
            gif.setVisibility(View.VISIBLE);
        else
            gif.setVisibility(View.GONE);

        if(StaticStore.play) {
            buttons[0].hide();
            buttons[2].hide();
            controller.setEnabled(false);
        } else {
            buttons[1].setImageDrawable(activity.getDrawable(R.drawable.ic_pause_black_24dp));
        }

        if(!shared.getBoolean("FPS",true))
            fps.setVisibility(View.GONE);
    }

    private String number(int num) {
        if (0 <= num && num < 10) {
            return "00" + num;
        } else if (10 <= num && num <= 99) {
            return "0" + num;
        } else {
            return ""+num;
        }
    }

    private String withID(int id, String name) {
        String result;
        String names = name;

        if(names == null)
            names = "";

        if(names.equals("")) {
            result = number(id);
        } else {
            result = number(id)+" - "+names;
        }

        return result;
    }

    private void setDisappear(View... views) {
        for(View v : views) {
            v.setVisibility(View.GONE);
        }
    }

    private void setAppear(View... views) {
        for(View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private final AnimationCView cView;

        ScaleListener(AnimationCView view) {
            this.cView = view;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            cView.siz *= detector.getScaleFactor();

            return true;
        }
    }

    private static int getAttributeColor(Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = ContextCompat.getColor(context,colorRes);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return color;
    }
}
