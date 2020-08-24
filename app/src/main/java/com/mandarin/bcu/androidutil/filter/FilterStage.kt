package com.mandarin.bcu.androidutil.filter

import android.content.Context
import android.util.SparseArray
import androidx.core.util.isNotEmpty
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import common.pack.Identifier
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.stage.MapColc
import common.util.stage.Stage
import common.util.unit.Enemy
import java.util.*
import kotlin.collections.ArrayList

object FilterStage {
    fun setFilter(name: String, stmname: String, enemies: List<Enemy>, enemorand: Boolean, music: String, bg: String, star: Int, bh: Int, bhop: Int, contin: Int, boss: Int, c: Context) : Map<String, SparseArray<ArrayList<Int>>> {
        val result = HashMap<String, SparseArray<ArrayList<Int>>>()

        for(n in 0 until StaticStore.mapcode.size) {
            val i = StaticStore.mapcode[n]
            val m = MapColc.get(i) ?: continue

            val stresult = SparseArray<ArrayList<Int>>()

            for(j in m.maps.list.indices) {
                val stm = m.maps[j] ?: continue

                val sresult = ArrayList<Int>()

                for(k in 0 until stm.list.list.size) {
                    val s = stm.list.list[k] ?: continue

                    val nam = if(stmname != "") {
                        if(name != "") {
                            val stmnam = (MultiLangCont.get(stm) ?: stm.name ?: "").toLowerCase(Locale.ROOT).contains(stmname.toLowerCase(Locale.ROOT))
                            val stnam = (MultiLangCont.get(s) ?: s.name ?: "").toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))

                            stmnam && stnam
                        } else {
                            (MultiLangCont.get(stm) ?: stm.name ?: "").toLowerCase(Locale.ROOT).contains(stmname.toLowerCase(Locale.ROOT))
                        }
                    } else {
                        (MultiLangCont.get(s) ?: s.name ?: "").toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))
                    }

                    val enem = containEnemy(enemies, s.data.allEnemy, enemorand)

                    var mus = music.isEmpty()

                    if(!mus && Identifier.get(s.mus0) != null) {
                        val m0 = s.mus0.pack + " - " + Data.trio(s.mus0.id)

                        mus = mus || m0 == music
                    }

                    if(!mus && Identifier.get(s.mus1) != null) {
                        val m1 = s.mus1.pack + " - " + Data.trio(s.mus1.id)

                        mus = mus || m1 == music
                    }

                    var backg = bg.isEmpty()

                    if(!backg && Identifier.get(s.bg) != null) {
                        val b = s.bg.pack + " - " + Data.trio(s.bg.id)

                        backg = backg || bg == b
                    }

                    val stars = stm.stars.size > star

                    val baseh = if(bh != -1) {
                        when(bhop) {
                            -1 -> true
                            0 -> s.health < bh
                            1 -> s.health == bh
                            2 -> s.health > bh
                            else -> false
                        }
                    } else {
                        true
                    }

                    val cont = when(contin) {
                        -1 -> true
                        0 -> !s.non_con
                        1 -> s.non_con
                        else -> false
                    }

                    val bos = when(boss) {
                        -1 -> true
                        0 -> hasBoss(s, c)
                        1 -> !hasBoss(s, c)
                        else -> false
                    }

                    if(nam && enem && mus && backg && stars && baseh && cont && bos)
                        sresult.add(k)
                }

                if(sresult.isNotEmpty())
                    stresult.put(j,sresult)
            }

            if(stresult.isNotEmpty())
                result[i] = stresult
        }

        return result
    }

    private fun containEnemy(src: List<Enemy>, target: Set<Enemy>, orand: Boolean) : Boolean {
        if(src.isEmpty()) return true

        if(target.isEmpty()) return false

        val targetid = ArrayList<Enemy>()

        for(ten in target) {
            if (!targetid.contains(ten)) {
                targetid.add(ten)
            }
        }

        //True for Or search

        if(orand) {
            for(i in src) {
                if(targetid.contains(i))
                    return true
            }

            return false
        } else {
            return targetid.containsAll(src)
        }
    }

    private fun hasBoss(st: Stage, c: Context) : Boolean {
        try {
            val def = st.data ?: return false

            for (i in def.datas) {
                if (i.boss == 1)
                    return true
            }
        } catch(e: Exception) {
            ErrorLogWriter.writeLog(e, StaticStore.upload, c)
            return false
        }

        return false
    }
}