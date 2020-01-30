package com.mandarin.bcu.androidutil

import android.annotation.SuppressLint
import com.mandarin.bcu.util.Interpret
import common.system.MultiLangCont
import common.util.pack.Pack
import java.util.*

class FilterEntity {
    private var entitynumber: Int
    private var entityname = ""

    constructor(entitynumber: Int) {
        this.entitynumber = entitynumber
        if (StaticStore.lineunitname != null) if (StaticStore.lineunitname.isNotEmpty()) entityname = StaticStore.lineunitname
    }

    constructor(entitynumber: Int, entityname: String) {
        this.entitynumber = entitynumber
        this.entityname = entityname
    }

    @SuppressLint("DefaultLocale")
    fun setFilter(): ArrayList<Int> {
        val b0 = ArrayList<Boolean>()
        val b1 = ArrayList<Boolean>()
        val b2 = ArrayList<Boolean>()
        val b3 = ArrayList<Boolean>()
        val b4 = ArrayList<Boolean>()
        if (StaticStore.rare.isEmpty()) {
            for (i in 0 until entitynumber) b0.add(true)
        }
        if (StaticStore.empty) {
            for (i in 0 until entitynumber) b1.add(true)
        }
        if (StaticStore.attack.isEmpty()) {
            for (i in 0 until entitynumber) b2.add(true)
        }
        if (StaticStore.tg.isEmpty()) {
            for (i in 0 until entitynumber) b3.add(true)
        }
        if (StaticStore.ability.isEmpty()) {
            for (i in 0 until entitynumber) b4.add(true)
        }
        for (u in Pack.def.us.ulist.list) {
            b0.add(StaticStore.rare.contains(u.rarity.toString()))
            val b10 = ArrayList<Boolean>()
            val b20 = ArrayList<Boolean>()
            val b30 = ArrayList<Boolean>()
            val b40 = ArrayList<Boolean>()
            for (f in u.forms) {
                val du = if (StaticStore.talents) f.maxu() else f.du
                val t = du.type
                val a = du.abi
                if (!StaticStore.empty) if (StaticStore.atksimu) b10.add(Interpret.isType(du, 1)) else b10.add(Interpret.isType(du, 0))
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
                val u = StaticStore.units[i]
                var added = false
                for (j in u.forms.indices) {
                    if (added) continue
                    var name = MultiLangCont.FNAME.getCont(u.forms[j])
                    if (name == null) name = number(i)
                    name = number(i) + " - " + name.toLowerCase()
                    if (name.contains(entityname.toLowerCase())) added = true
                }
                if (added) result.add(i)
            } else {
                result.add(i)
            }
        }

        return result
    }

    @SuppressLint("DefaultLocale")
    fun eSetFilter(): ArrayList<Int> {
        val b0 = ArrayList<Boolean>()
        val b1 = ArrayList<Boolean>()
        val b2 = ArrayList<Boolean>()
        val b3 = ArrayList<Boolean>()
        if (StaticStore.empty) {
            for (i in 0 until entitynumber) b0.add(true)
        }
        if (StaticStore.attack.isEmpty()) for (i in 0 until entitynumber) b1.add(true)
        if (StaticStore.tg.isEmpty() && !StaticStore.starred) for (i in 0 until entitynumber) b2.add(true)
        if (StaticStore.ability.isEmpty()) for (i in 0 until entitynumber) b3.add(true)
        for (e in Pack.def.es.list) {
            var b10: Boolean
            var b20: Boolean
            var b30: Boolean
            val de = e.de
            val t = de.type
            val a = de.abi
            if (!StaticStore.empty) if (StaticStore.atksimu) b0.add(Interpret.isType(de, 1)) else b0.add(Interpret.isType(de, 0))
            b10 = !StaticStore.atkorand
            for (k in StaticStore.attack.indices) {
                b10 = if (StaticStore.atkorand) b10 or Interpret.isType(de, StaticStore.attack[k].toInt()) else b10 and Interpret.isType(de, StaticStore.attack[k].toInt())
            }
            if (StaticStore.tg.isEmpty()) b20 = true else {
                b20 = !StaticStore.tgorand
                for (k in StaticStore.tg.indices) {
                    b20 = if (StaticStore.tgorand) if (StaticStore.tg[k] == "") t == 0 else b20 or ((t shr StaticStore.tg[k].toInt() and 1) == 1) else if (StaticStore.tg[k] == "") t == 0 else b20 and ((t shr StaticStore.tg[k].toInt() and 1) == 1)
                }
            }
            val b21 = de.star == 1
            b30 = !StaticStore.aborand
            for (k in StaticStore.ability.indices) {
                val vect = StaticStore.ability[k]
                if (vect[0] == 0) {
                    val bind = a and vect[1] != 0
                    b30 = if (StaticStore.aborand) b30 or bind else b30 and bind
                } else if (vect[0] == 1) {
                    b30 = if (StaticStore.aborand) b30 or (de.getProc(vect[1])[0] != 0) else b30 and (de.getProc(vect[1])[0] != 0)
                }
            }
            b1.add(b10)
            if (StaticStore.starred) b2.add(b20 && b21) else b2.add(b20)
            b3.add(b30)
        }
        val result = ArrayList<Int>()
        for (i in 0 until entitynumber) if (b0[i] && b1[i] && b2[i] && b3[i]) {
            if (entityname.isNotEmpty()) {
                val e = StaticStore.enemies[i]
                var name = MultiLangCont.ENAME.getCont(e)
                if (name == null) name = number(i)
                name = number(i) + " - " + name.toLowerCase()
                if (name.contains(entityname.toLowerCase())) result.add(i)
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