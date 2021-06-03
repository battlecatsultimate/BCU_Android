package com.mandarin.bcu.androidutil.supports.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.AssetBrowser
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.AutoMarquee
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.system.files.VFile
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

class AssetListAdapter(private val ac: Activity, private val files: ArrayList<VFile>) : ArrayAdapter<VFile>(ac, R.layout.file_list_layout, files){

    private val extension = arrayOf("png", "csv", "tsv", "json", "imgcut", "mamodel", "maanim", "ini")
    val icon = arrayOf(R.drawable.ic_png, R.drawable.ic_csv, R.drawable.ic_tsv, R.drawable.ic_json, R.drawable.ic_imgcut, R.drawable.ic_mamodel, R.drawable.ic_maanim, R.drawable.ic_ini)

    val size = arrayOf("B", "KB", "MB", "GB")

    val df = DecimalFormat("#.##")

    private class ViewHolder constructor(row: View) {
        val name: AutoMarquee = row.findViewById(R.id.filename)
        val size: AutoMarquee = row.findViewById(R.id.filesize)
        val img: ImageView = row.findViewById(R.id.fileimage)
        val option: FloatingActionButton = row.findViewById(R.id.fileoption)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(convertView == null) {
            val inf = LayoutInflater.from(context)

            row = inf.inflate(R.layout.file_list_layout, parent, false)
            holder = ViewHolder(row)

            row.tag = holder
        } else {
            row = convertView
            holder = row.tag as ViewHolder
        }


        if(files[position].path == AssetBrowser.path) {
            holder.name.text = "..."
            holder.size.visibility = View.GONE
            holder.img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_folderback))
            holder.option.visibility = View.GONE
        } else {
            holder.name.text = files[position].name

            if(isFile(files[position])) {
                holder.option.visibility = View.VISIBLE

                val popup = PopupMenu(context, holder.option)
                val menu = popup.menu

                popup.menuInflater.inflate(R.menu.file_menu, menu)

                popup.setOnMenuItemClickListener { item ->
                    when(item.itemId) {
                        R.id.fileextract -> {
                            AssetBrowser.current = files[position]

                            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                                    .addCategory(Intent.CATEGORY_OPENABLE)
                                    .setType("*/*")

                            val name = files[position].name

                            intent.putExtra(Intent.EXTRA_TITLE, name)

                            ac.startActivityForResult(intent, AssetBrowser.EXTRACT_FILE)

                            return@setOnMenuItemClickListener true
                        }
                        R.id.fileshare -> {
                            val f = File(StaticStore.getExternalTemp(context))

                            if(!f.exists()) {
                                f.mkdirs()
                            }

                            val g = File(f.absolutePath, files[position].name)

                            if(!g.exists()) {
                                g.createNewFile()
                            }

                            val fos = FileOutputStream(g)
                            val ins = files[position].data.stream

                            val b = ByteArray(65536)
                            var len: Int

                            while(ins.read(b).also { len = it } != -1) {
                                fos.write(b, 0, len)
                            }

                            ins.close()
                            fos.close()

                            val uri = FileProvider.getUriForFile(context,"com.mandarin.bcu.provider",g)

                            val intent = Intent()

                            intent.action = Intent.ACTION_SEND
                            intent.type = "*/*"
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                            intent.putExtra(Intent.EXTRA_STREAM, uri)

                            val i = Intent.createChooser(intent, context.getString(R.string.file_share))

                            ac.startActivity(i)

                            return@setOnMenuItemClickListener true
                        }
                    }

                    return@setOnMenuItemClickListener false
                }

                holder.option.setOnClickListener(object : SingleClick() {
                    override fun onSingleClick(v: View?) {
                        popup.show()
                    }

                })

                holder.size.visibility = View.VISIBLE
                holder.size.text = calculateSize(files[position].data.size())

                val index = extension.indexOf(getFileExtension(files[position]))

                if(index == -1)
                    holder.img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_file))
                else
                    holder.img.setImageDrawable(ContextCompat.getDrawable(context, icon[index]))
            } else {
                holder.size.visibility = View.GONE
                holder.img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_folder))
                holder.option.visibility = View.GONE
            }
        }

        return row
    }

    private fun getFileExtension(f: VFile) : String {
        val l = f.name.split(".")

        return l[l.size - 1]
    }

    private fun isFile(f: VFile) : Boolean {
        return f.list() == null
    }

    private fun calculateSize(s: Int) : String {
        var d = s.toDouble()
        var i = 0

        while(d >= 1000) {
            if(i == size.size - 1)
                break

            d /= 1000
            i++
        }

        return df.format(d)+" "+size[i]
    }
}