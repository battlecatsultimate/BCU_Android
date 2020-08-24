package com.mandarin.bcu.androidutil.stage

import android.content.Context
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.DefineItf
import common.CommonStatic
import common.pack.PackData
import common.pack.UserProfile
import common.system.fake.ImageBuilder
import common.system.files.VFile
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.stage.MapColc
import java.io.File

class MapDefiner {
    private val file = "StageName.txt"
    private val diff = "Difficulty.txt"
    private val rewa = "RewardName.txt"
    private val lan = arrayOf("/en/", "/zh/", "/kr/", "/jp/")

    fun define(context: Context) {
        try {
            if (!StaticStore.init) {
                StaticStore.clear()
                val shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
                StaticStore.getLang(shared.getInt("Language", 0))
                ImageBuilder.builder = BMBuilder()
                DefineItf().init(context)
                CommonStatic.ctx.initProfile()
                StaticStore.init = true
            }

            if (StaticStore.stagelang == 1) {
                MultiLangCont.getStatic().SMNAME.clear()
                MultiLangCont.getStatic().STNAME.clear()
                MultiLangCont.getStatic().RWNAME.clear()

                for (l in lan) {
                    val path = "./lang$l$file"
                    val f = File(path.replace("./", StaticStore.getExternalAsset(context)))
                    if (f.exists()) {
                        val qs = VFile.readLine(path)
                        if (qs != null) {
                            for (s in qs) {
                                val strs = s.trim { it <= ' ' }.split("\t").toTypedArray()

                                if (strs.size == 1)
                                    continue

                                val id = strs[0].trim { it <= ' ' }
                                val name = strs[strs.size - 1].trim { it <= ' ' }

                                if (id.isEmpty() || name.isEmpty())
                                    continue

                                val ids = id.split("-").toTypedArray()

                                val id0 = CommonStatic.parseIntN(ids[0].trim { it <= ' ' })

                                val mc = MapColc.get(Data.hex(id0)) ?: continue

                                if (ids.size == 1) {
                                    MultiLangCont.getStatic().MCNAME.put(l.substring(1, l.length - 1), mc, name)
                                    continue
                                }

                                val id1 = CommonStatic.parseIntN(ids[1].trim { it <= ' ' })

                                if (id1 >= mc.maps.list.size || id1 < 0)
                                    continue

                                val stm = mc.maps.list[id1] ?: continue

                                if (ids.size == 2) {
                                    MultiLangCont.getStatic().SMNAME.put(l.substring(1, l.length - 1), stm, name)
                                    continue
                                }

                                val id2 = CommonStatic.parseIntN(ids[2].trim { it <= ' ' })

                                if (id2 >= stm.list.list.size || id2 < 0)
                                    continue

                                val st = stm.list.list[id2]

                                MultiLangCont.getStatic().STNAME.put(l.substring(1, l.length - 1), st, name)
                            }
                        }
                    }
                }
                for (l in lan) {
                    val path = "./lang$l$rewa"

                    val f = File(path.replace("./", StaticStore.getExternalAsset(context)))

                    if (f.exists()) {
                        val qs = VFile.readLine(path)

                        if (qs != null) {
                            for (s in qs) {
                                val strs = s.trim { it <= ' ' }.split("\t").toTypedArray()

                                if (strs.size <= 1)
                                    continue

                                val id = strs[0].trim { it <= ' ' }
                                val name = strs[1].trim { it <= ' ' }

                                MultiLangCont.getStatic().RWNAME.put(l.substring(1, l.length - 1), id.toInt(), name)
                            }
                        }
                    }
                }

                val path = "./lang/$diff"
                val f = File(path.replace("./", StaticStore.getExternalAsset(context)))
                if (f.exists()) {
                    val qs = VFile.readLine(path)

                    if (qs != null) {
                        for (s in qs) {
                            val strs = s.trim { it <= ' ' }.split("\t").toTypedArray()

                            if (strs.size < 2)
                                continue

                            val num = strs[1].trim { it <= ' ' }

                            val numbers = strs[0].trim { it <= ' ' }.split("-").toTypedArray()

                            if (numbers.size < 3)
                                continue

                            val id0 = CommonStatic.parseIntN(numbers[0].trim { it <= ' ' })
                            val id1 = CommonStatic.parseIntN(numbers[1].trim { it <= ' ' })
                            val id2 = CommonStatic.parseIntN(numbers[2].trim { it <= ' ' })

                            val mc = MapColc.get(Data.hex(id0)) ?: continue

                            if (id1 >= mc.maps.list.size || id1 < 0)
                                continue

                            val stm = mc.maps[id1] ?: continue

                            if (id2 >= stm.list.list.size || id2 < 0)
                                continue

                            val st = stm.list.list[id2]

                            st.info.diff = num.toInt()
                        }
                    }
                }

                StaticStore.stagelang = 0
            }

            for(i in UserProfile.getAllPacks()) {
                if(i is PackData.DefPack)
                    continue
                else if(i is PackData.UserPack) {
                    if(i.mc.maps.list.isNotEmpty()) {
                        StaticStore.mapcode.add(i.desc.id)
                    }
                }
            }

            if(StaticStore.mapcolcname.isEmpty()) {
                for(i in StaticStore.bcMapNames) {
                    StaticStore.mapcolcname.add(context.getString(i))
                }

                for(i in UserProfile.getAllPacks()) {
                    i ?: continue

                    if(i is PackData.DefPack)
                        continue
                    else if(i is PackData.UserPack) {
                        if(i.mc.maps.list.isNotEmpty()) {
                            val k = StaticStore.getPackName(i.desc.id)

                            StaticStore.mapcolcname.add(k)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}