package com.mandarin.bcu.androidutil.io.asynchs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.mandarin.bcu.DownloadScreen;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.StaticStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;

public class CheckUpdates extends AsyncTask<Void, Integer, Void> {
    private final String path;
    private final boolean cando;

    private final WeakReference<Activity> weakReference;

    private boolean lang;
    private boolean music;
    private String[] lan = {"/en/", "/jp/", "/kr/", "/zh/"};
    private String source;
    private ArrayList<String> fileneed;
    private ArrayList<String> filenum;
    private boolean contin = true;
    private boolean config = false;

    private JSONObject ans;

    CheckUpdates(String path, boolean lang, ArrayList<String> fileneed, ArrayList<String> filenum, Activity context, boolean cando) {
        this.path = path;
        this.lang = lang;
        this.fileneed = fileneed;
        this.filenum = filenum;
        this.weakReference = new WeakReference<>(context);
        this.cando = cando;

        source = path + "/lang";
    }

    CheckUpdates(String path, boolean lang, ArrayList<String> fileneed, ArrayList<String> filenum, Activity context, boolean cando, boolean config) {
        this.path = path;
        this.lang = lang;
        this.fileneed = fileneed;
        this.filenum = filenum;
        this.weakReference = new WeakReference<>(context);
        this.cando = cando;
        this.config = config;

        source = path + "/lang";
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        if (activity == null) return;

        TextView checkstate = activity.findViewById(R.id.mainstup);

        if (checkstate != null)
            checkstate.setText(R.string.main_check_up);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        if(activity == null) return null;

        File output;
        try {
            if(!config) {
                String assetlink = "https://raw.githubusercontent.com/battlecatsultimate/bcu-page/master/api/getUpdate.json";
                URL asseturl = new URL(assetlink);

                InputStream is = asseturl.openStream();
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                String result = readAll(new BufferedReader(isr));
                ans = new JSONObject(result);
                is.close();
            }

            String difffile = "Difficulty.txt";
            File diff = new File(source + "/", difffile);

            if (!diff.exists()) lang = true;

            for (String s1 : lan) {
                if (lang) continue;

                for (String s : StaticStore.langfile) {
                    if (lang) continue;

                    File f = new File(source + s1, s);

                    if (!f.exists()) {
                        lang = true;
                    }
                }
            }

            SharedPreferences shared = activity.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE);

            if ((!shared.getBoolean("Skip_Text", false) || config) && !lang) {
                String url = "https://raw.githubusercontent.com/battlecatsultimate/bcu-resources/master/resources/lang";
                String durl = url + "/" + difffile;
                URL dlink = new URL(durl);
                HttpURLConnection dc = (HttpURLConnection) dlink.openConnection();
                dc.setRequestMethod("GET");
                dc.connect();

                InputStream durlis = dc.getInputStream();

                byte[] dbuf = new byte[1024];
                int dlen;
                int dsize = 0;
                while ((dlen = durlis.read(dbuf)) != -1) {
                    dsize += dlen;
                }

                output = new File(source + "/", difffile);

                if (output.exists()) {
                    if (output.length() != dsize) {
                        lang = true;
                    }
                } else {
                    lang = true;
                }

                dc.disconnect();
                durlis.close();

                for (String s1 : lan) {
                    if (lang) continue;

                    for (String s : StaticStore.langfile) {
                        if (lang) continue;

                        String langurl = url + s1 + s;
                        URL link = new URL(langurl);
                        HttpURLConnection c = (HttpURLConnection) link.openConnection();
                        c.setRequestMethod("GET");
                        c.connect();

                        InputStream urlis = c.getInputStream();

                        byte[] buf = new byte[1024];
                        int len1;
                        int size = 0;
                        while ((len1 = urlis.read(buf)) != -1) {
                            size += len1;
                        }


                        output = new File(source + s1, s);

                        if (output.exists()) {
                            if (output.length() != size) {
                                lang = true;
                                break;
                            }
                        } else {
                            lang = true;
                            break;
                        }

                        c.disconnect();
                        durlis.close();
                    }

                    if (lang) {
                        break;
                    }
                }
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
            contin = false;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            contin = false;
        } catch (IOException e) {
            e.printStackTrace();
            contin = false;
        } catch (JSONException e) {
            e.printStackTrace();
            contin = false;
        }


        publishProgress(1);

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Activity activity = weakReference.get();

        if (activity == null) return;

        TextView checkstate = activity.findViewById(R.id.mainstup);

        if (values[0] == 1) {
            if (checkstate != null)
                checkstate.setText(R.string.main_check_file);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (contin || cando) {
            Activity activity = weakReference.get();

            if (activity == null) return;

            ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager.getActiveNetworkInfo() != null)
                checkFiles(ans);

            if (fileneed.isEmpty() && filenum.isEmpty()) {
                new AddPathes(activity, config).execute();
            }
        } else {
            new CheckUpdates(path, lang, fileneed, filenum, weakReference.get(), false).execute();
        }
    }

    private String readAll(Reader rd) {
        try {
            StringBuilder sb = new StringBuilder();
            int chara;
            while ((chara = rd.read()) != -1) {
                sb.append((char) chara);
            }

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void checkFiles(JSONObject asset) {
        Activity activity = weakReference.get();

        if (activity == null) return;

        try {
            ArrayList<String> libmap = new ArrayList<>();

            if (asset == null) return;

            JSONArray ja = asset.getJSONArray("android");

            int mus = asset.getInt("music");

            String musicPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/com.mandarin.bcu/music/";

            ArrayList<String> musics = new ArrayList<>();

            for(int i = 0; i < mus; i++) {
                if(music) continue;

                String name = number(i)+".ogg";

                File mf = new File(musicPath,name);

                if(!mf.exists()) {
                    music = true;

                    for(int j = i; j < mus; j++) {
                        musics.add(number(j)+".ogg");
                    }
                }
            }

            System.out.println(musics);

            for (int i = 0; i < ja.length(); i++) {
                libmap.add(ja.getString(i));
            }

            System.out.println(libmap.toString());

            AlertDialog.Builder donloader = new AlertDialog.Builder(activity);
            final Intent intent = new Intent(activity, DownloadScreen.class);
            donloader.setTitle(R.string.main_file_need);
            donloader.setMessage(R.string.main_file_up);
            donloader.setPositiveButton(R.string.main_file_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (lang && !fileneed.contains("Language")) {
                        fileneed.add("Language");
                        filenum.add(String.valueOf(filenum.size()));
                    }

                    if(music && !fileneed.contains("Music")) {
                        fileneed.add("Music");
                        filenum.add("Music");
                    }

                    System.out.println(fileneed.toString());
                    intent.putExtra("fileneed", fileneed);
                    intent.putExtra("filenum", filenum);
                    intent.putExtra("music",musics);
                    activity.startActivity(intent);
                    activity.finish();
                }
            });

            donloader.setNegativeButton(R.string.main_file_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!cando || lang || music)
                        activity.finish();
                    else
                        new AddPathes(activity,config).execute();
                }
            });

            donloader.setCancelable(false);

            try {
                Set<String> libs = com.mandarin.bcu.io.Reader.getInfo(path);

                if (libs != null && libs.isEmpty()) {
                    for (int i = 0; i < libmap.size(); i++) {
                        fileneed.add(libmap.get(i));
                        filenum.add(String.valueOf(i));
                    }
                    AlertDialog downloader = donloader.create();
                    downloader.show();
                } else {
                    for (int i = 0; i < libmap.size(); i++) {
                        if (!(libs != null && libs.contains(libmap.get(i)))) {
                            fileneed.add(libmap.get(i));
                            filenum.add(String.valueOf(i));
                        }
                    }

                    if (!filenum.isEmpty()) {
                        donloader.setTitle(R.string.main_file_x);
                        AlertDialog downloader = donloader.create();
                        downloader.show();
                    } else if (lang) {
                        fileneed.add("Language");
                        filenum.add(String.valueOf(filenum.size()));
                        donloader.setTitle(R.string.main_file_x);
                        AlertDialog downloader = donloader.create();
                        downloader.show();
                    } else if (music) {
                        fileneed.add("Music");
                        filenum.add("Music");
                        donloader.setTitle(R.string.main_file_x);
                        AlertDialog downloader = donloader.create();
                        downloader.show();
                    }
                }
            } catch (Exception e) {
                for (int i = 0; i < libmap.size(); i++) {
                    fileneed.add(libmap.get(i));
                    filenum.add(String.valueOf(i));
                }
                donloader.setTitle(R.string.main_info_corr);
                donloader.setMessage(R.string.main_info_cont);
                AlertDialog downloader = donloader.create();
                downloader.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String number(int n) {
        if (0 <= n && n < 10) {
            return "00" + n;
        } else if (10 <= n && n <= 99) {
            return "0" + n;
        } else {
            return String.valueOf(n);
        }
    }
}