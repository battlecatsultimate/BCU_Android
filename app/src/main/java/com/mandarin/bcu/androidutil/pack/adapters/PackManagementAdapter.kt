package com.mandarin.bcu.androidutil.pack.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.pack.PackData
import common.pack.Source
import common.pack.UserProfile
import java.io.File
import java.text.DecimalFormat

class PackManagementAdapter(private val ac: Activity, private val pList: ArrayList<PackData.UserPack>) : ArrayAdapter<PackData.UserPack>(ac, R.layout.ability_layout, pList) {
    class ViewHolder(v: View) {
        val id: TextView = v.findViewById(R.id.pmanid)
        val name: TextView = v.findViewById(R.id.pmanname)
        val desc: TextView = v.findViewById(R.id.pmandesc)
        val more: FloatingActionButton = v.findViewById(R.id.pmanmore)
    }

    var dialog = AlertDialog.Builder(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(convertView == null) {
            val inf = LayoutInflater.from(context)

            row = inf.inflate(R.layout.pack_manage_list_layout, parent, false)
            holder = ViewHolder(row)

            row.tag = holder
        } else {
            row = convertView

            holder = row.tag as ViewHolder
        }

        val p = pList[position]

        val title = if(p.desc.author == null || p.desc.author.isBlank()) {
            p.sid
        } else {
            p.sid + " [${p.desc.author}]"
        }

        holder.id.text = title

        holder.name.text = StaticStore.getPackName(p.sid)

        val f = (p.source as Source.ZipSource).packFile

        if(!f.exists()) {
            Log.w("PackManagementAdapter", "File ${f.absolutePath} not existing")

            return row
        }

        val desc = "${f.name} (${byteToMB(f.length())}MB)"

        holder.desc.text = desc

        val popup = PopupMenu(context, holder.more)
        val menu = popup.menu

        popup.menuInflater.inflate(R.menu.pack_list_option_menu, menu)

        popup.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.packremove -> {
                    dialog.setTitle(R.string.pack_manage_remove_sure)
                    dialog.setMessage(R.string.pack_manage_remove_msg)

                    dialog.setPositiveButton(R.string.remove) { _, _ ->
                        deletePack(p, f)

                        pList.removeAt(position)

                        notifyDataSetChanged()

                        StaticStore.showShortMessage(context, R.string.pack_remove_result)

                        ac.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    }

                    dialog.setNegativeButton(R.string.main_file_cancel) {_, _ ->
                        ac.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    }

                    StaticStore.fixOrientation(ac)

                    if (!ac.isDestroyed && !ac.isFinishing) {
                        dialog.show()
                    }
                }
                R.id.packshare -> {
                    if(!f.exists()) {
                        StaticStore.showShortMessage(context, R.string.pack_share_notfound)

                        return@setOnMenuItemClickListener  false
                    }


                    val uri = FileProvider.getUriForFile(context,"com.mandarin.bcu.provider",f)

                    val intent = Intent()

                    intent.action = Intent.ACTION_SEND
                    intent.type = "*/*"
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                    intent.putExtra(Intent.EXTRA_STREAM, uri)

                    val i = Intent.createChooser(intent, context.getString(R.string.pack_manage_share))

                    ac.startActivity(i)
                }
            }

            false
        }

        menu.getItem(1).isEnabled = !cantDelete(p)

        holder.more.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                popup.show()
            }
        })

        return row
    }

    override fun getCount(): Int {
        return pList.size
    }

    private fun byteToMB(bytes: Long) : String {
        val df = DecimalFormat("#.##")

        return df.format(bytes.toDouble()/1000000.0)
    }

    private fun deletePack(p: PackData.UserPack, pack: File) {
        if(pack.exists())
            pack.delete()

        val shared = context.getSharedPreferences(StaticStore.PACK, Context.MODE_PRIVATE)

        val editor = shared.edit()

        val mList = ArrayList<File>()

        val fList = File(StaticStore.dataPath+"music/").listFiles() ?: return

        for(f in fList) {
            if(f.name.startsWith("${p.sid}-"))
                mList.add(f)
        }

        for(m in mList) {
            Log.i("Definer::extractMusic", "Deleted music : ${m.absolutePath}")

            m.delete()

            editor.remove(m.name)
        }

        editor.remove(p.sid)

        editor.apply()

        UserProfile.unloadPack(p)
    }

    private fun cantDelete(p: PackData.UserPack) : Boolean {
        for(pack in UserProfile.getAllPacks()) {
            pack ?: continue

            if(pack is PackData.DefPack || pack.sid == p.sid)
                continue

            if(pack is PackData.UserPack) {
                for(pid in pack.desc.dependency) {
                    if(pid == p.sid)
                        return true
                }
            }
        }

        return false
    }
}