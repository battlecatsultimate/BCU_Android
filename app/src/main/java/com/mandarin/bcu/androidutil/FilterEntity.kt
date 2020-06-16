package com.mandarin.bcu.androidutil

import com.mandarin.bcu.util.Interpret
import common.system.MultiLangCont
import common.util.Data
import common.util.pack.Pack
import java.util.*
import kotlin.collections.ArrayList

class FilterEntity(private var entitynumber: Int, private var entityname: String, private var pid: Int) {
    companion object {
        fun setLuFilter() : ArrayList<Int> {
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
                val data = info.split("-")

                if(data.size < 2) {
                    b0.add(false)
                    b1.add(false)
                    b2.add(false)
                    b3.add(false)
                    b4.add(false)
                    continue
                }

                val pid = data[0].toInt()

                val p = Pack.map[pid]

                if(p == null) {
                    b0.add(false)
                    b1.add(false)
                    b2.add(false)
                    b3.add(false)
                    b4.add(false)
                    continue
                }

                val id = data[1].toInt()

                if(id >= p.us.ulist.list.size) {
                    b0.add(false)
                    b1.add(false)
                    b2.add(false)
                    b3.add(false)
                    b4.add(false)
                    continue
                }

                val u = p.us.ulist.list[id]

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
                                b41 or (du.getProc(vect[1])[0] > 0)
                            } else {
                                b41 and (du.getProc(vect[1])[0] > 0)
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

            val result = ArrayList<Int>()

            for(i in StaticStore.ludata.indices) {
                if(b0[i] && b1[i] && b2[i] && b3[i] && b4[i]) {
                    if(StaticStore.entityname.isNotEmpty()) {
                        val info = StaticStore.ludata[i].split("-")

                        if(info.size < 2)
                            continue

                        val pid = info[0].toInt()
                        val id = info[1].toInt()

                        val p = Pack.map[pid] ?: continue

                        if(id >= p.us.ulist.list.size)
                            continue

                        val u = p.us.ulist.list[id]

                        var added = false

                        for(j  in u.forms.indices) {
                            if(added)
                                continue

                            var name = MultiLangCont.FNAME.getCont(u.forms[j]) ?: u.forms[j].name

                            if(name == null)
                                name = ""

                            name = Data.trio(j) + "-" + name.toLowerCase(Locale.ROOT)

                            if(name.contains(StaticStore.entityname))
                                added = true
                        }

                        if(added)
                            result.add(i)
                    } else {
                        result.add(i)
                    }
                }
            }

            return result
        }
    }

    fun setFilter(): ArrayList<Int> {
        val p = Pack.map[pid] ?: return ArrayList()

        val b0 = ArrayList<Boolean>()
        val b1 = ArrayList<Boolean>()
        val b2 = ArrayList<Boolean>()
        val b3 = ArrayList<Boolean>()
        val b4 = ArrayList<Boolean>()

        if (StaticStore.rare.isEmpty()) {
            for (i in 0 until entitynumber)
                b0.add(true)
        }
        if (StaticStore.empty) {
            for (i in 0 until entitynumber)
                b1.add(true)
        }
        if (StaticStore.attack.isEmpty()) {
            for (i in 0 until entitynumber)
                b2.add(true)
        }
        if (StaticStore.tg.isEmpty()) {
            for (i in 0 until entitynumber)
                b3.add(true)
        }
        if (StaticStore.ability.isEmpty()) {
            for (i in 0 until entitynumber)
                b4.add(true)
        }
        for (u in p.us.ulist.list) {
            b0.add(StaticStore.rare.contains(u.rarity.toString()))

            val b10 = ArrayList<Boolean>()
            val b20 = ArrayList<Boolean>()
            val b30 = ArrayList<Boolean>()
            val b40 = ArrayList<Boolean>()
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
                        b41 = if (StaticStore.aborand) b41 or (du.getProc(vect[1])[0] > 0) else b41 and (du.getProc(vect[1])[0] > 0)
                    }
                }
                b20.add(b21)
                b30.add(b31)
                b40.add(b41)
            }
            if (!StaticStore.empty) if (b10.contains(true)) b1.add(true) else b1.add(false)
            if (b20.contains(true)) b2.add(true) else b2.add(false)
            if (b30.contains(true)) b3.add(true) else b3.add(false)
            if (b40.contains(true)) b4.add(true) else b4.add(false)
        }
        val result = ArrayList<Int>()
        for (i in 0 until entitynumber) if (b0[i] && b1[i] && b2[i] && b3[i] && b4[i]) {
            if (entityname.isNotEmpty()) {
                val u = p.us.ulist.list[i]

                var added = false

                for (j in u.forms.indices) {
                    if (added)
                        continue

                    var name = MultiLangCont.FNAME.getCont(u.forms[j]) ?: u.forms[j].name

                    if (name == null)
                        name = ""

                    name = number(i) + " - " + name.toLowerCase(Locale.ROOT)

                    if (name.contains(entityname.toLowerCase(Locale.ROOT)))
                        added = true
                }

                if (added)
                    result.add(i)
            } else {
                result.add(i)
            }
        }

        return result
    }

    fun eSetFilter(): ArrayList<Int> {
        val p = Pack.map[pid] ?: return ArrayList()

        val b0 = ArrayList<Boolean>()
        val b1 = ArrayList<Boolean>()
        val b2 = ArrayList<Boolean>()
        val b3 = ArrayList<Boolean>()

        if (StaticStore.empty) {
            for (i in 0 until entitynumber)
                b0.add(true)
        }

        if (StaticStore.attack.isEmpty())
            for (i in 0 until entitynumber)
                b1.add(true)

        if (StaticStore.tg.isEmpty() && !StaticStore.starred)
            for (i in 0 until entitynumber)
                b2.add(true)

        if (StaticStore.ability.isEmpty())
            for (i in 0 until entitynumber)
                b3.add(true)

        for (e in p.es.list) {
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
                        b30 or (de.getProc(vect[1])[0] != 0)
                    else
                        b30 and (de.getProc(vect[1])[0] != 0)
                }
            }

            b1.add(b10)

            if (StaticStore.starred)
                b2.add(b20 && b21)
            else
                b2.add(b20)

            b3.add(b30)
        }

        val result = ArrayList<Int>()

        for (i in 0 until entitynumber)
            if (b0[i] && b1[i] && b2[i] && b3[i]) {
                if (entityname.isNotEmpty()) {
                    val e = p.es.list[i]

                    var name = MultiLangCont.ENAME.getCont(e) ?: e.name

                    if (name == null)
                        name = ""

                    name = number(i) + " - " + name.toLowerCase(Locale.ROOT)

                    if (name.contains(entityname.toLowerCase(Locale.ROOT)))
                        result.add(i)
                } else {
                    result.add(i)
                }
            }

        return result
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
                "" + num
            }
        }
    }
}