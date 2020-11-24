package com.mandarin.bcu.androidutil.filter

import com.mandarin.bcu.androidutil.StatFilterElement
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.util.Interpret
import common.CommonStatic
import common.battle.data.MaskEntity
import common.pack.Identifier
import common.pack.UserProfile
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.unit.AbEnemy
import common.util.unit.Unit
import java.util.*
import kotlin.collections.ArrayList

object FilterEntity {
    fun setUnitFilter(pid: String): ArrayList<Identifier<Unit>> {
        val p = UserProfile.getPack(pid) ?: return ArrayList()

        val b0 = ArrayList<Boolean>()
        val b1 = ArrayList<Boolean>()
        val b2 = ArrayList<Boolean>()
        val b3 = ArrayList<Boolean>()
        val b4 = ArrayList<Boolean>()
        val b5 = ArrayList<Boolean>()

        if (StaticStore.rare.isEmpty()) {
            for (i in p.units.list.indices)
                b0.add(true)
        }
        if (StaticStore.empty) {
            for (i in p.units.list.indices)
                b1.add(true)
        }
        if (StaticStore.attack.isEmpty()) {
            for (i in p.units.list.indices)
                b2.add(true)
        }
        if (StaticStore.tg.isEmpty()) {
            for (i in p.units.list.indices)
                b3.add(true)
        }
        if (StaticStore.ability.isEmpty()) {
            for (i in p.units.list.indices)
                b4.add(true)
        }
        if (StatFilterElement.statFilter.isEmpty()) {
            for(i in p.units.list.indices) {
                b5.add(true)
            }
        }
        for (u in p.units.list) {
            if(StaticStore.rare.isNotEmpty()) b0.add(StaticStore.rare.contains(u.rarity.toString()))

            val b10 = ArrayList<Boolean>()
            val b20 = ArrayList<Boolean>()
            val b30 = ArrayList<Boolean>()
            val b40 = ArrayList<Boolean>()
            val b50 = ArrayList<Boolean>()
            for (f in u.forms) {
                val du = if (StaticStore.talents) f.maxu() else f.du
                val t = du.type
                val a = du.abi
                if (!StaticStore.empty)
                    if (StaticStore.atksimu)
                        b10.add(Interpret.isType(du, 1))
                    else
                        b10.add(Interpret.isType(du, 0))
                var b21 = !StaticStore.atkorand
                for (k in StaticStore.attack.indices) {
                    b21 = if (StaticStore.atkorand) b21 or Interpret.isType(du, StaticStore.attack[k].toInt()) else b21 and Interpret.isType(du, StaticStore.attack[k].toInt())
                }
                var b31 = !StaticStore.tgorand
                for (k in StaticStore.tg.indices) {
                    b31 = if (StaticStore.tgorand) b31 or ((t shr StaticStore.tg[k].toInt() and 1) == 1) else b31 and ((t shr StaticStore.tg[k].toInt() and 1) == 1)
                }
                var b41 = !StaticStore.aborand
                for (k in StaticStore.ability.indices) {
                    val vect = StaticStore.ability[k]
                    if (vect[0] == 0) {
                        val bind = a and vect[1] != 0
                        b41 = if (StaticStore.aborand) b41 or bind else b41 and bind
                    } else if (vect[0] == 1) {
                        b41 = if (StaticStore.aborand) b41 or getChance(vect[1], du) else b41 and getChance(vect[1], du)
                    }
                }
                b20.add(b21)
                b30.add(b31)
                b40.add(b41)
                b50.add(StatFilterElement.performFilter(f, StatFilterElement.orand))
            }
            if (!StaticStore.empty) if (b10.contains(true)) b1.add(true) else b1.add(false)
            if (StaticStore.attack.isNotEmpty()) if (b20.contains(true)) b2.add(true) else b2.add(false)
            if (StaticStore.tg.isNotEmpty()) if (b30.contains(true)) b3.add(true) else b3.add(false)
            if (StaticStore.ability.isNotEmpty()) if (b40.contains(true)) b4.add(true) else b4.add(false)
            if (StatFilterElement.statFilter.isNotEmpty()) if (b50.contains(true)) b5.add(true) else b5.add(false)
        }

        val result = ArrayList<Identifier<Unit>>()

        val lang = Locale.getDefault().language

        for (i in p.units.list.indices) if (b0[i] && b1[i] && b2[i] && b3[i] && b4[i] && b5[i]) {
            val u = p.units.list[i]

            if (StaticStore.entityname.isNotEmpty()) {
                var added = false

                for (j in u.forms.indices) {
                    if (added)
                        continue

                    var name = MultiLangCont.get(u.forms[j]) ?: u.forms[j].name

                    if (name == null)
                        name = ""

                    name = Data.trio(i) + " - " + name.toLowerCase(Locale.ROOT)

                    added = if(CommonStatic.getConfig().lang == 2 || lang == Interpret.KO) {
                        KoreanFilter.filter(name, StaticStore.entityname)
                    } else {
                        name.contains(StaticStore.entityname.toLowerCase(Locale.ROOT))
                    }
                }

                if (added)
                    result.add(u.id)
            } else {
                result.add(u.id)
            }
        }

        return result
    }

    private fun getChance(data: Int, du: MaskEntity) : Boolean {
        return when(data) {
            Data.P_WEAK -> du.proc.WEAK.exists()
            Data.P_STOP -> du.proc.STOP.exists()
            Data.P_SLOW -> du.proc.SLOW.exists()
            Data.P_KB -> du.proc.KB.exists()
            Data.P_WARP -> du.proc.WARP.exists()
            Data.P_CURSE -> du.proc.CURSE.exists()
            Data.P_IMUATK -> du.proc.IMUATK.exists()
            Data.P_STRONG -> du.proc.STRONG.exists()
            Data.P_LETHAL -> du.proc.LETHAL.exists()
            Data.P_CRIT -> du.proc.CRIT.exists()
            Data.P_BREAK -> du.proc.BREAK.exists()
            Data.P_SATK -> du.proc.SATK.exists()
            Data.P_WAVE -> du.proc.WAVE.exists()
            Data.P_VOLC -> du.proc.VOLC.exists()
            Data.P_IMUWEAK -> du.proc.IMUWEAK.exists()
            Data.P_IMUSTOP -> du.proc.IMUSTOP.exists()
            Data.P_IMUSLOW -> du.proc.IMUSLOW.exists()
            Data.P_IMUKB -> du.proc.IMUKB.exists()
            Data.P_IMUWAVE -> du.proc.IMUWAVE.exists()
            Data.P_IMUVOLC -> du.proc.IMUVOLC.exists()
            Data.P_IMUWARP -> du.proc.IMUWARP.exists()
            Data.P_IMUCURSE -> du.proc.IMUCURSE.exists()
            Data.P_IMUPOIATK -> du.proc.IMUPOIATK.exists()
            Data.P_POIATK -> du.proc.POIATK.exists()
            Data.P_BURROW -> du.proc.BURROW.exists()
            Data.P_REVIVE -> du.proc.REVIVE.exists()
            Data.P_SEAL -> du.proc.SEAL.exists()
            Data.P_TIME -> du.proc.TIME.exists()
            Data.P_SUMMON -> du.proc.SUMMON.exists()
            Data.P_MOVEWAVE -> du.proc.MOVEWAVE.exists()
            Data.P_THEME -> du.proc.THEME.exists()
            Data.P_POISON -> du.proc.POISON.exists()
            Data.P_BOSS -> du.proc.BOSS.exists()
            Data.P_ARMOR -> du.proc.ARMOR.exists()
            Data.P_SPEED -> du.proc.SPEED.exists()
            Data.P_CRITI -> du.proc.CRITI.exists()
            else -> false
        }
    }

    fun setEnemyFilter(pid: String): ArrayList<Identifier<AbEnemy>> {
        val p = UserProfile.getPack(pid) ?: return ArrayList()

        val b0 = ArrayList<Boolean>()
        val b1 = ArrayList<Boolean>()
        val b2 = ArrayList<Boolean>()
        val b3 = ArrayList<Boolean>()
        val b4 = ArrayList<Boolean>()

        if (StaticStore.empty) {
            for (i in p.enemies.list.indices)
                b0.add(true)
        }

        if (StaticStore.attack.isEmpty())
            for (i in p.enemies.list.indices)
                b1.add(true)

        if (StaticStore.tg.isEmpty() && !StaticStore.starred)
            for (i in p.enemies.list.indices)
                b2.add(true)

        if (StaticStore.ability.isEmpty())
            for (i in p.enemies.list.indices)
                b3.add(true)

        if (StatFilterElement.statFilter.isEmpty())
            for(i in p.enemies.list.indices)
                b4.add(true)

        for (e in p.enemies.list) {
            var b10: Boolean
            var b20: Boolean
            var b30: Boolean

            val de = e.de
            val t = de.type
            val a = de.abi

            if (!StaticStore.empty)
                if (StaticStore.atksimu)
                    b0.add(Interpret.isType(de, 1))
                else
                    b0.add(Interpret.isType(de, 0))

            b10 = !StaticStore.atkorand

            for (k in StaticStore.attack.indices) {
                b10 = if (StaticStore.atkorand)
                    b10 or Interpret.isType(de, StaticStore.attack[k].toInt())
                else
                    b10 and Interpret.isType(de, StaticStore.attack[k].toInt())
            }

            if (StaticStore.tg.isEmpty())
                b20 = true
            else {
                b20 = !StaticStore.tgorand
                for (k in StaticStore.tg.indices) {
                    b20 = if (StaticStore.tgorand)
                        if (StaticStore.tg[k] == "")
                            t == 0
                        else
                            b20 or ((t shr StaticStore.tg[k].toInt() and 1) == 1)
                    else if (StaticStore.tg[k] == "")
                        t == 0
                    else
                        b20 and ((t shr StaticStore.tg[k].toInt() and 1) == 1)
                }
            }

            val b21 = de.star == 1

            b30 = !StaticStore.aborand

            for (k in StaticStore.ability.indices) {
                val vect = StaticStore.ability[k]

                if (vect[0] == 0) {
                    val bind = a and vect[1] != 0
                    b30 = if (StaticStore.aborand)
                        b30 or bind
                    else
                        b30 and bind
                } else if (vect[0] == 1) {
                    b30 = if (StaticStore.aborand)
                        b30 or getChance(vect[1], de)
                    else
                        b30 and getChance(vect[1], de)
                }
            }

            b1.add(b10)

            if (StaticStore.starred)
                b2.add(b20 && b21)
            else
                b2.add(b20)

            b3.add(b30)

            if(StatFilterElement.statFilter.isNotEmpty()) {
                b4.add(StatFilterElement.performFilter(e, StatFilterElement.orand))
            }
        }

        val result = ArrayList<Identifier<AbEnemy>>()

        val lang = Locale.getDefault().language

        for (i in p.enemies.list.indices)
            if (b0[i] && b1[i] && b2[i] && b3[i] && b4[i]) {
                val e = p.enemies.list[i]

                if (StaticStore.entityname.isNotEmpty()) {
                    var name = MultiLangCont.get(e) ?: e.name

                    if (name == null)
                        name = ""

                    name = Data.trio(i) + " - " + name.toLowerCase(Locale.ROOT)

                    val added = if(CommonStatic.getConfig().lang == 2 || lang == Interpret.KO) {
                        KoreanFilter.filter(name, StaticStore.entityname)
                    } else {
                        name.contains(StaticStore.entityname.toLowerCase(Locale.ROOT))
                    }

                    if (added)
                        result.add(e.id)
                } else {
                    result.add(e.id)
                }
            }

        return result
    }

    fun setLuFilter() : ArrayList<Identifier<Unit>> {
        val b0 = ArrayList<Boolean>()
        val b1 = ArrayList<Boolean>()
        val b2 = ArrayList<Boolean>()
        val b3 = ArrayList<Boolean>()
        val b4 = ArrayList<Boolean>()

        if(StaticStore.rare.isEmpty()) {
            for(i in 0 until StaticStore.ludata.size)
                b0.add(true)
        }

        if(StaticStore.empty) {
            for(i in 0 until StaticStore.ludata.size)
                b1.add(true)
        }

        if(StaticStore.attack.isEmpty()) {
            for(i in 0 until StaticStore.ludata.size)
                b2.add(true)
        }

        if(StaticStore.tg.isEmpty()) {
            for(i in 0 until StaticStore.ludata.size)
                b3.add(true)
        }

        if(StaticStore.ability.isEmpty()) {
            for(i in 0 until StaticStore.ludata.size)
                b4.add(true)
        }

        for(info in StaticStore.ludata) {
            val u = Identifier.get(info)

            if(u == null) {
                b0.add(false)
                b1.add(false)
                b2.add(false)
                b3.add(false)
                b4.add(false)
                continue
            }

            b0.add(StaticStore.rare.contains(u.rarity.toString()))

            val b10 = ArrayList<Boolean>()
            val b20 = ArrayList<Boolean>()
            val b30 = ArrayList<Boolean>()
            val b40 = ArrayList<Boolean>()

            for(f in u.forms) {
                val du = if(StaticStore.talents)
                    f.maxu()
                else
                    f.du

                val t = du.type
                val a = du.abi

                if(!StaticStore.empty) {
                    if(StaticStore.atksimu) {
                        b10.add(Interpret.isType(du, 1))
                    } else {
                        b10.add(Interpret.isType(du, 0))
                    }
                }

                var b21 = !StaticStore.atkorand

                for(k in StaticStore.attack.indices) {
                    b21 = if(StaticStore.atkorand) {
                        b21 or Interpret.isType(du, StaticStore.attack[k].toInt())
                    } else {
                        b21 and Interpret.isType(du, StaticStore.attack[k].toInt())
                    }
                }

                var b31 = !StaticStore.tgorand

                for(k in StaticStore.tg.indices) {
                    b31 = if(StaticStore.tgorand) {
                        b31 or ((t shr StaticStore.tg[k].toInt() and 1) == 1)
                    } else {
                        b31 and ((t shr StaticStore.tg[k].toInt() and 1) == 1)
                    }
                }

                var b41 = !StaticStore.aborand

                for(k in StaticStore.ability.indices) {
                    val vect = StaticStore.ability[k]

                    if(vect[0] == 0) {
                        val bind = a and vect[1] != 0
                        b41 = if(StaticStore.aborand) {
                            b41 or bind
                        } else {
                            b41 and bind
                        }
                    } else if (vect[0] == 1) {
                        b41 = if(StaticStore.aborand) {
                            b41 or getChance(vect[1], du)
                        } else {
                            b41 and getChance(vect[1], du)
                        }
                    }
                }

                b20.add(b21)
                b30.add(b31)
                b40.add(b41)
            }

            b1.add(!StaticStore.empty && b10.contains(true))
            b2.add(b20.contains(true))
            b3.add(b30.contains(true))
            b4.add(b40.contains(true))
        }

        val result = ArrayList<Identifier<Unit>>()

        val lang = Locale.getDefault().language

        for(i in StaticStore.ludata.indices) {
            if(b0[i] && b1[i] && b2[i] && b3[i] && b4[i]) {
                val u = Identifier.get(StaticStore.ludata[i]) ?: continue

                if(StaticStore.entityname.isNotEmpty()) {
                    var added = false

                    for(j  in u.forms.indices) {
                        if(added)
                            continue

                        var name = MultiLangCont.get(u.forms[j]) ?: u.forms[j].name

                        if(name == null)
                            name = ""

                        name = Data.trio(j) + "-" + name.toLowerCase(Locale.ROOT)

                        added = if(CommonStatic.getConfig().lang == 2 || lang == Interpret.KO) {
                            KoreanFilter.filter(name, StaticStore.entityname)
                        } else {
                            name.contains(StaticStore.entityname.toLowerCase(Locale.ROOT))
                        }
                    }

                    if(added)
                        result.add(u.id)
                } else {
                    result.add(u.id)
                }
            }
        }

        return result
    }
}