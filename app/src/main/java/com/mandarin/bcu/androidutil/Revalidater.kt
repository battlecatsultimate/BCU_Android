package com.mandarin.bcu.androidutil

import android.content.Context
import com.mandarin.bcu.androidutil.StaticStore.unitnumber
import com.mandarin.bcu.androidutil.unit.Definer
import common.system.MultiLangCont
import common.util.Data
import common.util.pack.Pack
import common.util.stage.MapColc

object Revalidater {
    fun validate(lang: String, context: Context) {
        Definer().redefine(context, lang)

        if (StaticStore.lunames.isNotEmpty() || StaticStore.ludata.isNotEmpty()) {
            StaticStore.lunames.clear()
            StaticStore.ludata.clear()

            for(m in Pack.map) {
                val p = m.value ?: continue

                val pid = p.id

                for(i in p.us.ulist.list.indices) {
                    val unit = p.us.ulist.list[i]

                    val name = MultiLangCont.FNAME.getCont(unit.forms[0]) ?: unit.forms[0].name ?: ""

                    val id = if(p.id != 0) {
                        StaticStore.getID(p.us.ulist.list[i].id)
                    } else {
                        i
                    }

                    val fullName = if(name != "") {
                        Data.hex(pid)+" - "+number(id)+"/"+name
                    } else {
                        Data.hex(pid)+" - "+number(id)+"/"
                    }

                    StaticStore.lunames.add(fullName)
                    StaticStore.ludata.add("$pid-$i")
                }
            }
        }

        if(StaticStore.mapcolcname.isNotEmpty()) {
            StaticStore.mapcolcname.clear()

            for(i in StaticStore.bcMapNames) {
                StaticStore.mapcolcname.add(context.getString(i))
            }

            for(i in Pack.map) {
                val v= i.value

                if(v.id == 0)
                    continue
                else {
                    val k = Data.hex(i.key)

                    val name = v.name ?: ""

                    if(name == "")
                        StaticStore.mapcolcname.add(k)
                    else
                        StaticStore.mapcolcname.add(k + " - " + v.name)
                }
            }
        }
    }

    private fun number(num: Int): String {
        return when (num) {
            in 0..9 -> {
                "00$num"
            }
            in 10..99 -> {
                "0$num"
            }
            else -> {
                num.toString()
            }
        }
    }


}