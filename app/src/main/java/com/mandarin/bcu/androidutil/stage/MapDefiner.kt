package com.mandarin.bcu.androidutil.stage

import android.content.Context
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.decode.ZipLib
import common.CommonStatic
import common.system.MultiLangCont
import common.system.fake.ImageBuilder
import common.system.files.AssetData
import common.util.Data
import common.util.pack.Background
import common.util.pack.NyCastle
import common.util.pack.Pack
import common.util.stage.CharaGroup
import common.util.stage.Limit
import common.util.stage.MapColc
import java.io.File

class MapDefiner {
    private val file = "StageName.txt"
    private val diff = "Difficulty.txt"
    private val rewa = "RewardName.txt"
    private val lan = arrayOf("/en/", "/zh/", "/kr/", "/jp/")
    fun define(context: Context) {
        try {
            if (StaticStore.map == null) {
                try {
                    if(!StaticStore.mapread) {
                        println("Map read")
                        MapColc.read()
                        StaticStore.mapread = true
                    }

                    if(!StaticStore.chararead) {
                        println("Chara read")
                        CharaGroup.read()
                        StaticStore.chararead = true
                    }

                    if(!StaticStore.limitread) {
                        println("Limit read")
                        Limit.read()
                        StaticStore.limitread = true
                    }

                    if(!StaticStore.nycread) {
                        println("Nyc Read")
                        NyCastle.read()
                        StaticStore.nycread = true
                    }

                    if(!StaticStore.musicread) {
                        println("Music Read")
                        SoundHandler.read(context)
                        StaticStore.musicread = true
                    }

                    if(StaticStore.bgread == 0) {
                        println("BG Read")
                        Background.read()
                        StaticStore.bgread = 1
                    }
                } catch (e: Exception) {
                    ErrorLogWriter.writeLog(e, StaticStore.upload, context)

                    StaticStore.clear()
                    val shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
                    StaticStore.getLang(shared.getInt("Language", 0))
                    ZipLib.init(StaticStore.getExternalPath(context))
                    ZipLib.read(StaticStore.getExternalPath(context))
                    StaticStore.getEnemynumber(context)
                    ImageBuilder.builder = BMBuilder()
                    DefineItf().init(context)
                    StaticStore.root = 1

                    println("Data loss detected")

                    if(!StaticStore.mapread) {
                        println("Map read")
                        MapColc.read()
                        StaticStore.mapread = true
                    }

                    if(!StaticStore.chararead) {
                        println("Chara read")
                        CharaGroup.read()
                        StaticStore.chararead = true
                    }

                    if(!StaticStore.limitread) {
                        println("Limit read")
                        Limit.read()
                        StaticStore.limitread = true
                    }

                    if(!StaticStore.nycread) {
                        println("Nyc Read")
                        NyCastle.read()
                        StaticStore.nycread = true
                    }

                    if(!StaticStore.musicread) {
                        println("Music Read")
                        SoundHandler.read(context)
                        StaticStore.musicread = true
                    }

                    if(StaticStore.bgread == 0) {
                        println("BG Read")
                        Background.read()
                        StaticStore.bgread = 1
                    }
                }
                StaticStore.map = MapColc.MAPS
            }
            if (StaticStore.stagelang == 1) {
                MultiLangCont.SMNAME.clear()
                MultiLangCont.STNAME.clear()
                MultiLangCont.RWNAME.clear()
                for (l in lan) {
                    val path = StaticStore.getExternalPath(context)+"lang" + l + file
                    val f = File(path)
                    if (f.exists()) {
                        val qs = AssetData.getAsset(f).readLine()
                        if (qs != null) {
                            for (s in qs) {
                                val strs = s.trim { it <= ' ' }.split("\t").toTypedArray()
                                if (strs.size == 1) continue
                                val id = strs[0].trim { it <= ' ' }
                                val name = strs[strs.size - 1].trim { it <= ' ' }
                                if (id.isEmpty() || name.isEmpty()) continue
                                val ids = id.split("-").toTypedArray()
                                val id0 = CommonStatic.parseIntN(ids[0].trim { it <= ' ' })
                                val mc = StaticStore.map[id0] ?: continue
                                if (ids.size == 1) {
                                    MultiLangCont.MCNAME.put(l.substring(1, l.length - 1), mc, name)
                                    continue
                                }
                                val id1 = CommonStatic.parseIntN(ids[1].trim { it <= ' ' })
                                if (id1 >= mc.maps.size || id1 < 0) continue
                                val stm = mc.maps[id1] ?: continue
                                if (ids.size == 2) {
                                    MultiLangCont.SMNAME.put(l.substring(1, l.length - 1), stm, name)
                                    continue
                                }
                                val id2 = CommonStatic.parseIntN(ids[2].trim { it <= ' ' })
                                if (id2 >= stm.list.size || id2 < 0) continue
                                val st = stm.list[id2]
                                MultiLangCont.STNAME.put(l.substring(1, l.length - 1), st, name)
                            }
                        }
                    }
                }
                for (l in lan) {
                    val path = StaticStore.getExternalPath(context)+"lang" + l + rewa
                    val f = File(path)
                    if (f.exists()) {
                        val qs = AssetData.getAsset(f).readLine()
                        if (qs != null) {
                            for (s in qs) {
                                val strs = s.trim { it <= ' ' }.split("\t").toTypedArray()
                                if (strs.size <= 1) continue
                                val id = strs[0].trim { it <= ' ' }
                                val name = strs[1].trim { it <= ' ' }
                                MultiLangCont.RWNAME.put(l.substring(1, l.length - 1), id.toInt(), name)
                            }
                        }
                    }
                }
                val path = StaticStore.getExternalPath(context)+"lang/"
                val f = File(path, diff)
                if (f.exists()) {
                    val qs = AssetData.getAsset(f).readLine()
                    if (qs != null) {
                        for (s in qs) {
                            val strs = s.trim { it <= ' ' }.split("\t").toTypedArray()
                            if (strs.size < 2) continue
                            val num = strs[1].trim { it <= ' ' }
                            val numbers = strs[0].trim { it <= ' ' }.split("-").toTypedArray()
                            if (numbers.size < 3) continue
                            val id0 = CommonStatic.parseIntN(numbers[0].trim { it <= ' ' })
                            val id1 = CommonStatic.parseIntN(numbers[1].trim { it <= ' ' })
                            val id2 = CommonStatic.parseIntN(numbers[2].trim { it <= ' ' })
                            val mc = StaticStore.map[id0] ?: continue
                            if (id1 >= mc.maps.size || id1 < 0) continue
                            val stm = mc.maps[id1] ?: continue
                            if (id2 >= stm.list.size || id2 < 0) continue
                            val st = stm.list[id2]
                            st.info.diff = num.toInt()
                        }
                    }
                }
                StaticStore.stagelang = 0
            }

            for(i in Pack.map) {
                if(i.value.id == 0)
                    continue

                val p = i.value ?: continue

                if(p.mc.maps.isNotEmpty()) {
                    StaticStore.mapcode.add(i.key)
                }
            }

            if(StaticStore.mapcolcname.isEmpty()) {
                for(i in StaticStore.bcMapNames) {
                    StaticStore.mapcolcname.add(context.getString(i))
                }

                for(i in Pack.map) {
                    val v= i.value ?: continue

                    if(v.id == 0)
                        continue
                    else {
                        if(v.mc.maps.isNotEmpty()) {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}