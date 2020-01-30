package com.mandarin.bcu.androidutil.music.asynchs

import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
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

        val list: ListView = ac.findViewById(R.id.mulist)

        setDisappear(list)
    }

    override fun doInBackground(vararg params: Void?): Void? {
        val ac = weak.get() ?: return null

        SoundHandler.read()

        if(StaticStore.musicnames.size != Pack.def.ms.size())
            for(i in Pack.def.ms.list.indices) {
                val f = Pack.def.ms[i]

                val uri = Uri.parse(f.absolutePath)
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(ac,uri)

                var time = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toFloat()/1000f

                val min = (time/60f).toInt()

                time -= min.toFloat()*60f

                val sec = round(time).toInt()

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