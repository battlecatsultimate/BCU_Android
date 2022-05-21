package com.mandarin.bcu.androidutil

import android.content.Context
import common.pack.PackData
import common.pack.UserProfile

object Revalidater {
    fun validate(lang: String, context: Context) {
        Definer.redefine(context, lang)

        if (StaticStore.ludata.isNotEmpty()) {
            StaticStore.ludata.clear()

            val plist = UserProfile.getAllPacks()

            for(m in plist) {
                if(m !is PackData.DefPack && m !is PackData.UserPack) {
                    continue
                }

                for(i in m.units.list.indices) {
                    val unit = m.units.list[i]

                    StaticStore.ludata.add(unit.id)
                }
            }
        }

        if(StaticStore.mapcolcname.isNotEmpty()) {
            StaticStore.mapcolcname.clear()

            for(i in StaticStore.bcMapNames) {
                StaticStore.mapcolcname.add(context.getString(i))
            }

            for(i in UserProfile.getAllPacks()) {
                if(i is PackData.DefPack)
                    continue
                else if(i is PackData.UserPack) {
                    if(i.mc.maps.list.isNotEmpty()) {
                        var k = i.desc.names.toString()

                        if(k.isEmpty()) {
                            k = i.desc.id
                        }

                        StaticStore.mapcolcname.add(k)

                    }
                }
            }
        }
    }
}