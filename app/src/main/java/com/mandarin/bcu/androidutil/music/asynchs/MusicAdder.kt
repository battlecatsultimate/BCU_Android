package com.mandarin.bcu.androidutil.music.asynchs

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import com.mandarin.bcu.MusicPlayer
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.battle.sound.SoundPlayer
import com.mandarin.bcu.androidutil.music.adapters.MusicListAdapter
import common.CommonStatic
import common.util.pack.Pack
import java.io.File
import java.lang.ref.WeakReference
import kotlin.math.round

class MusicAdder(activity: Activity) : AsyncTask<Void, Int, Void>() {
    private val weak = WeakReference<Activity>(activity)

    override fun onPreExecute() {
        val ac = weak.get() ?: return

        val list = ac.findViewById<ListView>(R.id.mulist)

        setDisappear(list)
    }

    override fun doInBackground(vararg params: Void?): Void? {
        weak.get() ?: return null

        if(!StaticStore.musicread) {
            SoundHandler.read()
            StaticStore.musicread = true
        }

        if(StaticStore.musicnames.size != Pack.def.ms.size())
            for(i in Pack.def.ms.list.indices) {
                val f = Pack.def.ms[i]

                val sp = SoundPlayer()
                sp.setDataSource(f.toString())
                sp.prepare()
                StaticStore.durations.add(sp.duration)

                var time = sp.duration.toFloat()/1000f

                sp.release()

                var min = (time/60f).toInt()

                time -= min.toFloat()*60f

                var sec = round(time).toInt()

                if(sec == 60) {
                    min += 1
                    sec = 0
                }

                val mins = min.toString()

                val secs = if(sec < 10) "0$sec"
                    else sec.toString()

                StaticStore.musicnames.add("$mins:$secs")
            }

        publishProgress(0)

        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val ac = weak.get() ?: return

        val loadt: TextView = ac.findViewById(R.id.mulistloadt)
        val list: ListView = ac.findViewById(R.id.mulist)

        loadt.text = ac.getString(R.string.medal_loading_data)

        val loc = ArrayList<Int>()
        val name = arrayOfNulls<String>(Pack.def.ms.size())

        for(i in Pack.def.ms.list.indices) {
            loc.add(getFileLoc(Pack.def.ms.list[i]))
            name[i] = Pack.def.ms.list[i].name
        }

        val adapter = MusicListAdapter(ac,name,loc)
        list.adapter = adapter

        list.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val intent = Intent(ac, MusicPlayer::class.java)
            intent.putExtra("Music",position)
            ac.startActivity(intent)
        }
    }

    override fun onPostExecute(result: Void?) {
        val ac = weak.get() ?: return

        val loadt: TextView = ac.findViewById(R.id.mulistloadt)
        val prog: ProgressBar = ac.findViewById(R.id.mulistprog)
        val list: ListView = ac.findViewById(R.id.mulist)

        setAppear(list)
        setDisappear(loadt, prog)
    }

    private fun setAppear(vararg view: View) {
        for (v in view) {
            v.visibility = View.VISIBLE
        }
    }

    private fun setDisappear(vararg view: View) {
        for (v in view) {
            v.visibility = View.GONE
        }
    }

    private fun getFileLoc(f: File): Int {
        var name = f.name

        if(!name.endsWith(".ogg")) return 0

        name = name.replace(".ogg","")

        return CommonStatic.parseIntN(name)
    }
}