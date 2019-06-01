package com.mandarin.bcu;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mandarin.bcu.main.MainBCU;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private String path;
    private ArrayList<String> fileneed = new ArrayList<>();
    private ArrayList<String> filenum = new ArrayList<>();
    private boolean lang = false;

    private Button animbtn;
    private Button stagebtn;
    private TextView checkstate;
    private ProgressBar mainprog;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainBCU.CheckMem(this);

        path = Environment.getExternalStorageDirectory().getPath()+"/Android/data/com.mandarin.BCU";

        animbtn = findViewById(R.id.anvibtn);
        stagebtn = findViewById(R.id.stgbtn);
        animbtn.setVisibility(View.GONE);
        stagebtn.setVisibility(View.GONE);
        checkstate = findViewById(R.id.mainstup);
        mainprog = findViewById(R.id.mainprogup);

        animbtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this,R.drawable.ic_kasa_jizo), null, null, null);
        stagebtn.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this,R.drawable.ic_castle),null,null,null);

        animbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationview();
            }
        });
        stagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stageinfoview();
            }
        });

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager.getActiveNetworkInfo() != null) {
            checkUpdates updates = new checkUpdates();
            updates.execute();
        } else {
            if(cando()) {
                mainprog.setVisibility(View.GONE);
                checkstate.setVisibility(View.GONE);
                stagebtn.setVisibility(View.VISIBLE);
                animbtn.setVisibility(View.VISIBLE);
            } else {
                mainprog.setVisibility(View.GONE);
                checkstate.setText(R.string.main_internet_no);
                Toast.makeText(this, "You need internet connection to run this application!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    protected boolean cando() {
        boolean result = false;

        String infopath = path + "/files/info/";
        String filename = "info_android.ini";

        File f = new File(infopath,filename);

        if(f.exists()) {
            try {
                String line;

                FileInputStream fis = new FileInputStream(f);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);

                ArrayList<String> lines = new ArrayList<>();

                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }

                if(lines.size() == 3) {
                    String [] libline = lines.get(2).split("=");

                    if(libline.length == 2) {
                        ArrayList<String> libs = new ArrayList<>(Arrays.asList(libline[1].split(",")));

                        if(libs.contains("000001") && libs.contains("000002") && libs.contains("000003")) {
                            result = true;

                            return true;
                        } else {
                            return result;
                        }


                    } else {
                        return result;
                    }
                } else {
                    return result;
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
                return result;
            }
        } else {
            return result;
        }
    }

    protected void animationview() {
        Intent intent = new Intent(this, AnimationViewer.class);
        startActivity(intent);
    }

    protected void stageinfoview()
    {
        Intent intent = new Intent(this,StageInfo.class);
        startActivity(intent);
    }

    private class checkUpdates extends AsyncTask<Void,Integer,Void> {
        private String [] lan = {"/en/","/jp/","/kr/","/zh/"};
        private String [] langfile = {"EnemyName.txt","StageName.txt","UnitName.txt"};
        private String source = path+"/lang";
        private String assetlink = "http://battlecatsultimate.cf/api/java/getAssets.php";
        private int size;
        private File output = null;
        private boolean add;
        final private String url = "http://battlecatsultimate.cf/api/resources/lang";
        private URL link;
        private URL asseturl;
        private HttpURLConnection c;

        private JSONObject inp;
        private JSONObject ans;

        @Override
        protected void onPreExecute() {
            animbtn.setVisibility(View.GONE);
            stagebtn.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                inp = new JSONObject();
                inp.put("bcuver",MainBCU.ver);

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

                AlertDialog.Builder donloader = new AlertDialog.Builder(MainActivity.this);
                final Intent intent = new Intent(MainActivity.this, DownloadScreen.class);
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
                        startActivity(intent);
                        finish();
                    }
                });

                donloader.setNegativeButton(R.string.main_file_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                donloader.setCancelable(false);

                String infopath = path + "/files/info/";
                String filename = "info_android.ini";

                File g = new File(infopath);

                if (!g.exists()) {
                    for (int i = 0; i < lib.size(); i++) {
                        fileneed.add(lib.get(i));
                        filenum.add(String.valueOf(i));
                    }
                    donloader.setTitle(R.string.main_down_init);
                    donloader.setMessage(R.string.main_file_cont);
                    AlertDialog downloader = donloader.create();
                    downloader.show();
                } else {
                    File f = new File(infopath, filename);

                    if (f.exists()) {
                        try {
                            String line;

                            FileInputStream fis = new FileInputStream(f);
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader br = new BufferedReader(isr);
                            StringBuilder str = new StringBuilder();

                            while ((line = br.readLine()) != null) {
                                str.append(line).append(System.getProperty("line.separator"));
                            }

                            String[] lit = str.toString().split("\n");

                            if(lit.length == 3) {

                                String[] libnum = lit[2].split("=");

                                if(libnum.length == 2) {

                                    ArrayList<String> ver = new ArrayList<>(Arrays.asList(libnum[1].split(",")));

                                    for (int i = 0; i < lib.size(); i++) {
                                        if (!ver.contains(lib.get(i))) {
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
                                } else {
                                    for (int i = 0; i < lib.size(); i++) {
                                        fileneed.add(lib.get(i));
                                        filenum.add(String.valueOf(i));
                                    }
                                    donloader.setTitle(R.string.main_info_corr);
                                    donloader.setMessage(R.string.main_info_cont);
                                    AlertDialog downloader = donloader.create();
                                    downloader.show();
                                }
                            } else {
                                for (int i = 0; i < lib.size(); i++) {
                                    fileneed.add(lib.get(i));
                                    filenum.add(String.valueOf(i));
                                }
                                donloader.setTitle(R.string.main_info_corr);
                                donloader.setMessage(R.string.main_info_cont);
                                AlertDialog downloader = donloader.create();
                                downloader.show();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        for (int i = 0; i < lib.size(); i++) {
                            fileneed.add(lib.get(i));
                            filenum.add(String.valueOf(i));
                        }
                        donloader.setTitle(R.string.main_down_init);
                        donloader.setMessage(R.string.main_file_cont);
                        AlertDialog downloader = donloader.create();
                        downloader.show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
