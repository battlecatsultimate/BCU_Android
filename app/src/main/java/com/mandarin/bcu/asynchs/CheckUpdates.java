package com.mandarin.bcu.asynchs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mandarin.bcu.DownloadScreen;
import com.mandarin.bcu.R;
import com.mandarin.bcu.main.MainBCU;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CheckUpdates extends AsyncTask<Void,Integer,Void> {
    private final Button animbtn;
    private final Button stagebtn;
    private final String path;
    private final TextView checkstate;
    private final ProgressBar mainprog;
    private final Context context;

    private boolean lang;
    private String [] lan = {"/en/","/jp/","/kr/","/zh/"};
    private String [] langfile = {"EnemyName.txt","StageName.txt","UnitName.txt"};
    private String source;
    private String assetlink = "http://battlecatsultimate.cf/api/java/getAssets.php";
    private int size;
    private File output = null;
    final private String url = "http://battlecatsultimate.cf/api/resources/lang";
    private ArrayList<String> fileneed;
    private ArrayList<String> filenum;
    private URL link;
    private HttpURLConnection c;

    private JSONObject inp;
    private JSONObject ans;

    public CheckUpdates(Button animbtn, Button stagebtn, String path, boolean lang, TextView checkstate, ProgressBar mainprog, ArrayList<String> fileneed, ArrayList<String> filenum, Context context) {
        this.animbtn = animbtn;
        this.stagebtn = stagebtn;
        this.path = path;
        this.lang = lang;
        this.checkstate = checkstate;
        this.mainprog = mainprog;
        this.fileneed = fileneed;
        this.filenum = filenum;
        this.context = context;

        source = path+"/lang";
    }

    @Override
    protected void onPreExecute() {
        animbtn.setVisibility(View.GONE);
        stagebtn.setVisibility(View.GONE);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            inp = new JSONObject();
            inp.put("bcuver", MainBCU.ver);

            URL asseturl = new URL(assetlink);
            HttpURLConnection connection = (HttpURLConnection) asseturl.openConnection();
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");

            OutputStream os = connection.getOutputStream();
            os.write(inp.toString().getBytes("UTF-8"));
            os.close();

            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is,"UTF-8");
            String result = readAll(new BufferedReader(isr));

            ans = new JSONObject(result);
            is.close();
            connection.disconnect();

            for (String s1 : lan) {
                for (String s : langfile) {
                    String langurl = url + s1 + s;
                    link = new URL(langurl);
                    c = (HttpURLConnection) link.openConnection();
                    c.setRequestMethod("GET");
                    c.connect();

                    InputStream urlis = c.getInputStream();

                    byte[] buf = new byte[1024];
                    int len1;
                    size = 0;
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
                    }

                    c.disconnect();
                }

                if (lang) {
                    break;
                }
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
            output = null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            output = null;
        } catch (IOException e) {
            e.printStackTrace();
            output = null;
        } catch (JSONException e) {
            e.printStackTrace();
            output = null;
        }


        publishProgress(1);

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        System.out.println(lang);
        if (values[0] == 1) {
            checkstate.setText(R.string.main_check_file);
            checkFiles(ans);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if(fileneed.isEmpty() && filenum.isEmpty()) {
            mainprog.setVisibility(View.GONE);
            checkstate.setVisibility(View.GONE);
            stagebtn.setVisibility(View.VISIBLE);
            animbtn.setVisibility(View.VISIBLE);
        }
    }

    protected String readAll(Reader rd) {
        try {
            StringBuilder sb = new StringBuilder();
            int chara;
            while ((chara = rd.read()) != -1) {
                sb.append((char)chara);
            }

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void checkFiles(JSONObject asset) {
        try {
            Map<String, String> libmap = new TreeMap<>();
            JSONArray ja = asset.getJSONArray("assets");

            for(int i=0;i<ja.length();i++) {
                JSONArray ent = ja.getJSONArray(i);
                libmap.put(ent.getString(0),ent.getString(1));
            }

            ArrayList<String> lib = new ArrayList<>(libmap.keySet());

            AlertDialog.Builder donloader = new AlertDialog.Builder(context);
            final Intent intent = new Intent(context, DownloadScreen.class);
            donloader.setTitle(R.string.main_file_need);
            donloader.setMessage(R.string.main_file_up);
            donloader.setPositiveButton(R.string.main_file_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (lang) {
                        fileneed.add("Language");
                        filenum.add(String.valueOf(filenum.size()));
                    }
                    System.out.println(fileneed.toString());
                    intent.putExtra("fileneed", fileneed);
                    intent.putExtra("filenum", filenum);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
            });

            donloader.setNegativeButton(R.string.main_file_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((Activity)context).finish();
                }
            });

            donloader.setCancelable(false);

            try {
                Set<String> libs = com.mandarin.bcu.io.Reader.getInfo(path);

                for(int i = 0; i < lib.size();i++) {
                    if(!libs.contains(lib.get(i))) {
                        fileneed.add(lib.get(i));
                        filenum.add(String.valueOf(i));
                    }
                }

                if (!filenum.isEmpty()) {
                    AlertDialog downloader = donloader.create();
                    downloader.show();
                } else if (lang) {
                    donloader.setTitle(R.string.main_file_x);
                    AlertDialog downloader = donloader.create();
                    downloader.show();
                }
            } catch (Exception e) {
                for (int i = 0; i < lib.size(); i++) {
                    fileneed.add(lib.get(i));
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
}