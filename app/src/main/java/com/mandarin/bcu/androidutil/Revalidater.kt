package com.mandarin.bcu.androidutil

import android.content.Context
import com.mandarin.bcu.androidutil.unit.Definer
import common.pack.PackData
import common.pack.UserProfile
import common.util.lang.MultiLangCont

object Revalidater {
    fun validate(lang: String, context: Context) {
        Definer().redefine(context, lang)

        if (StaticStore.lunames.isNotEmpty() || StaticStore.ludata.isNotEmpty()) {
            StaticStore.lunames.clear()
            StaticStore.ludata.clear()

            val plist = ArrayList<PackData>()

            plist.add(UserProfile.getBCData())
            plist.addAll(UserProfile.packs())

            for(m in plist) {
                val pid = if(m is PackData.DefPack) {
                    PackData.Identifier.DEF
                } else if(m is PackData.UserPack) {
                    m.desc.name
                } else {
                    continue
                }

                for(i in m.units.list.indices) {
                    val unit = m.units.list[i]

                    val name = MultiLangCont.get(unit.forms[0]) ?: unit.forms[0].name ?: ""

                    val id = if(m !is PackData.DefPack) {
                        StaticStore.getID(m.units.list[i].id.id)
                    } else {
                        i
                    }

                    val fullName = if(name != "") {
                        pid+" - "+number(id)+"/"+name
                    } else {
                        pid+" - "+number(id)+"/"
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

            for(i in StaticStore.getPacks()) {
                if(i is PackData.DefPack)
                    continue
                else if(i is PackData.UserPack) {
                    var k = i.desc.name

                    if(k.isEmpty()) {
                        k = i.desc.id
                    }

                    StaticStore.mapcolcname.add(k)
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