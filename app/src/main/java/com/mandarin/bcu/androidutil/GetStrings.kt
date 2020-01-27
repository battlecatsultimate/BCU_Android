package com.mandarin.bcu.androidutil

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.mandarin.bcu.R
import com.mandarin.bcu.util.Interpret
import common.battle.BasisSet
import common.battle.Treasure
import common.battle.data.MaskUnit
import common.system.MultiLangCont
import common.util.stage.Limit
import common.util.stage.SCDef
import common.util.unit.Enemy
import common.util.unit.Form
import java.text.DecimalFormat
import java.util.*

class GetStrings(private val c: Context) {
    private val abilID = arrayOf("1", "2", "3", "8", "10", "11", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "25", "26", "27", "29", "30", "31", "32", "37", "38", "39", "40", "51")
    private val talID = intArrayOf(R.string.sch_abi_we, R.string.sch_abi_fr, R.string.sch_abi_sl, R.string.sch_abi_kb, R.string.sch_abi_str, R.string.sch_abi_su, R.string.sch_abi_cr,
            R.string.sch_abi_zk, R.string.sch_abi_bb, R.string.sch_abi_em, R.string.sch_abi_wv, R.string.talen_we, R.string.talen_fr, R.string.talen_sl, R.string.talen_kb
            , R.string.talen_wv, R.string.unit_info_cost, R.string.unit_info_cd, R.string.unit_info_spd, R.string.sch_abi_ic, R.string.talen_cu,
            R.string.unit_info_atk, R.string.unit_info_hp, R.string.sch_an, R.string.sch_al, R.string.sch_zo, R.string.sch_re, R.string.sch_abi_iv)
    private val talTool = arrayOfNulls<String>(talID.size)
    private val mapcolcid = arrayOf("N", "S", "C", "CH", "E", "T", "V", "R", "M", "A", "B")
    private val mapcodes = listOf("0", "1", "2", "3", "4", "6", "7", "11", "12", "13", "14")
    private val diffid = intArrayOf(R.string.stg_info_easy, R.string.stg_info_norm, R.string.stg_info_hard, R.string.stg_info_vete, R.string.stg_info_expe, R.string.stg_info_insa, R.string.stg_info_dead, R.string.stg_info_merc)
    val talList: Unit
        get() {
            for (i in talTool.indices) talTool[i] = c.getString(talID[i])
        }

    fun getTitle(f: Form?): String {
        if (f == null) return ""
        val result = StringBuilder()
        var name = MultiLangCont.FNAME.getCont(f)
        if (name == null) name = ""
        val rarity: String = when (f.unit.rarity) {
            0 -> c.getString(R.string.sch_rare_ba)
            1 -> c.getString(R.string.sch_rare_ex)
            2 -> c.getString(R.string.sch_rare_ra)
            3 -> c.getString(R.string.sch_rare_sr)
            4 -> c.getString(R.string.sch_rare_ur)
            5 -> c.getString(R.string.sch_rare_lr)
            else -> "Unknown"
        }
        return if (name == "") rarity else result.append(rarity).append(" - ").append(name).toString()
    }

    fun getAtkTime(f: Form?, frse: Int): String {
        if (f == null) return ""
        return if (frse == 0) f.du.itv.toString() + " f" else DecimalFormat("#.##").format(f.du.itv.toDouble() / 30) + " s"
    }

    fun getAtkTime(em: Enemy?, frse: Int): String {
        if (em == null) return ""
        return if (frse == 0) em.de.itv.toString() + " f" else DecimalFormat("#.##").format(em.de.itv.toDouble() / 30) + " s"
    }

    fun getAbilT(f: Form?): String {
        if (f == null) return ""
        val atkdat = f.du.rawAtkData()
        val result = StringBuilder()
        for (i in atkdat.indices) {
            if (i != atkdat.size - 1) {
                if (atkdat[i][2] == 1) result.append(c.getString(R.string.unit_info_true)).append(" / ") else result.append(c.getString(R.string.unit_info_false)).append(" / ")
            } else {
                if (atkdat[i][2] == 1) result.append(c.getString(R.string.unit_info_true)) else result.append(c.getString(R.string.unit_info_false))
            }
        }
        return result.toString()
    }

    fun getAbilT(em: Enemy?): String {
        if (em == null) return ""
        val atks = em.de.rawAtkData()
        val result = StringBuilder()
        for (i in atks.indices) {
            if (i < atks.size - 1) {
                if (atks[i][2] == 1) result.append(c.getString(R.string.unit_info_true)).append(" / ") else result.append(c.getString(R.string.unit_info_false)).append(" / ")
            } else {
                if (atks[i][2] == 1) result.append(c.getString(R.string.unit_info_true)) else result.append(c.getString(R.string.unit_info_false))
            }
        }
        return result.toString()
    }

    fun getPost(f: Form?, frse: Int): String {
        if (f == null) return ""
        return if (frse == 0) f.du.post.toString() + " f" else DecimalFormat("#.##").format(f.du.post.toDouble() / 30) + " s"
    }

    fun getPost(em: Enemy?, frse: Int): String {
        if (em == null) return ""
        return if (frse == 0) em.de.post.toString() + " f" else DecimalFormat("#.##").format(em.de.post.toDouble() / 30) + " s"
    }

    fun getTBA(f: Form?, frse: Int): String {
        if (f == null) return ""
        return if (frse == 0) f.du.tba.toString() + " f" else DecimalFormat("#.##").format(f.du.tba.toDouble() / 30) + " s"
    }

    fun getTBA(em: Enemy?, frse: Int): String {
        if (em == null) return ""
        return if (frse == 0) em.de.tba.toString() + " f" else DecimalFormat("#.##").format(em.de.tba.toDouble() / 30) + " s"
    }

    fun getPre(f: Form?, frse: Int): String {
        if (f == null) return ""
        val atkdat = f.du.rawAtkData()
        return if (frse == 0) {
            if (atkdat.size > 1) {
                val result = StringBuilder()
                for (i in atkdat.indices) {
                    if (i != atkdat.size - 1) result.append(atkdat[i][1]).append(" f / ") else result.append(atkdat[i][1]).append(" f")
                }
                result.toString()
            } else atkdat[0][1].toString() + " f"
        } else {
            if (atkdat.size > 1) {
                val result = StringBuilder()
                for (i in atkdat.indices) {
                    if (i != atkdat.size - 1) result.append(DecimalFormat("#.##").format(atkdat[i][1].toDouble() / 30)).append(" s / ") else result.append(DecimalFormat("#.##").format(atkdat[i][1].toDouble() / 30)).append(" s")
                }
                result.toString()
            } else DecimalFormat("#.##").format(atkdat[0][1].toDouble() / 30) + " s"
        }
    }

    fun getPre(em: Enemy?, frse: Int): String {
        if (em == null) return ""
        val atkdat = em.de.rawAtkData()
        return if (frse == 0) {
            if (atkdat.size > 1) {
                val result = StringBuilder()
                for (i in atkdat.indices) {
                    if (i != atkdat.size - 1) result.append(atkdat[i][1]).append(" f / ") else result.append(atkdat[i][1]).append(" f")
                }
                result.toString()
            } else atkdat[0][1].toString() + " f"
        } else {
            if (atkdat.size > 1) {
                val result = StringBuilder()
                for (i in atkdat.indices) {
                    if (i != atkdat.size - 1) result.append(DecimalFormat("#.##").format(atkdat[i][1].toDouble() / 30)).append(" s / ") else result.append(DecimalFormat("#.##").format(atkdat[i][1].toDouble() / 30)).append(" s")
                }
                result.toString()
            } else DecimalFormat("#.##").format(atkdat[0][1].toDouble() / 30) + " s"
        }
    }

    fun getID(viewHolder: RecyclerView.ViewHolder?, id: String): String {
        return if (viewHolder == null) "" else id + "-" + viewHolder.adapterPosition
    }

    fun getID(form: Int, id: String): String {
        return "$id-$form"
    }

    fun getRange(f: Form?): String {
        if (f == null) return ""
        val tb = f.du.range
        val ma = f.du.repAtk
        val lds = ma.shortPoint
        val ldr = ma.longPoint - ma.shortPoint
        val start = lds.coerceAtMost(lds + ldr)
        val end = lds.coerceAtLeast(lds + ldr)
        return if (lds > 0) "$tb / $start ~ $end" else tb.toString()
    }

    fun getRange(em: Enemy?): String {
        if (em == null) return ""
        val tb = em.de.range
        val ma = em.de.repAtk
        val lds = ma.shortPoint
        val ldr = ma.longPoint - ma.shortPoint
        val start = lds.coerceAtMost(lds + ldr)
        val end = lds.coerceAtLeast(lds + ldr)
        return if (lds > 0) "$tb / $start ~ $end" else tb.toString()
    }

    fun getCD(f: Form?, t: Treasure?, frse: Int, talent: Boolean, lvs: IntArray?): String {
        if (f == null || t == null) return ""
        val du: MaskUnit = if (lvs != null && f.pCoin != null) if (talent) f.pCoin.improve(lvs) else f.du else f.du
        return if (frse == 0) t.getFinRes(du.respawn).toString() + " f" else DecimalFormat("#.##").format(t.getFinRes(du.respawn).toDouble() / 30) + " s"
    }

    fun getAtk(f: Form?, t: Treasure?, lev: Int, talent: Boolean, lvs: IntArray?): String {
        if (f == null || t == null) return ""
        val du: MaskUnit = if (lvs != null && f.pCoin != null) if (talent) f.pCoin.improve(lvs) else f.du else f.du
        return if (du.rawAtkData().size > 1) getTotAtk(f, t, lev, talent, lvs) + " " + getAtks(f, t, lev, talent, lvs) else getTotAtk(f, t, lev, talent, lvs)
    }

    fun getAtk(em: Enemy?, multi: Int): String {
        if (em == null) return ""
        return if (em.de.rawAtkData().size > 1) getTotAtk(em, multi) + " " + getAtks(em, multi) else getTotAtk(em, multi)
    }

    fun getSpd(f: Form?, talent: Boolean, lvs: IntArray?): String {
        if (f == null) return ""
        val du: MaskUnit = if (lvs != null && f.pCoin != null) if (talent) f.pCoin.improve(lvs) else f.du else f.du
        return du.speed.toString()
    }

    fun getSpd(em: Enemy?): String {
        return em?.de?.speed?.toString() ?: ""
    }

    fun getBarrier(em: Enemy?): String {
        if (em == null) return ""
        return if (em.de.shield == 0) c.getString(R.string.unit_info_t_none) else em.de.shield.toString()
    }

    fun getHB(f: Form?, talent: Boolean, lvs: IntArray?): String {
        if (f == null) return ""
        val du: MaskUnit = if (lvs != null && f.pCoin != null) if (talent) f.pCoin.improve(lvs) else f.du else f.du
        return du.hb.toString()
    }

    fun getHB(em: Enemy?): String {
        return em?.de?.hb?.toString() ?: ""
    }

    fun getHP(f: Form?, t: Treasure?, lev: Int, talent: Boolean, lvs: IntArray?): String {
        if (f == null || t == null) return ""
        val du: MaskUnit = if (lvs != null && f.pCoin != null) if (talent) f.pCoin.improve(lvs) else f.du else f.du
        return (du.hp * t.defMulti * f.unit.lv.getMult(lev)).toInt().toString()
    }

    fun getHP(em: Enemy?, multi: Int): String {
        if (em == null) return ""
        return (em.de.multi(BasisSet.current) * em.de.hp * multi / 100).toInt().toString()
    }

    fun getTotAtk(f: Form?, t: Treasure?, lev: Int, talent: Boolean, lvs: IntArray?): String {
        if (f == null || t == null) return ""
        val du: MaskUnit = if (lvs != null && f.pCoin != null) if (talent) f.pCoin.improve(lvs) else f.du else f.du
        return (du.allAtk() * t.atkMulti * f.unit.lv.getMult(lev)).toInt().toString()
    }

    private fun getTotAtk(em: Enemy?, multi: Int): String {
        if (em == null) return ""
        return (em.de.multi(BasisSet.current) * em.de.allAtk() * multi / 100).toInt().toString()
    }

    fun getDPS(f: Form?, t: Treasure?, lev: Int, talent: Boolean, lvs: IntArray?): String {
        return if (f == null || t == null) "" else DecimalFormat("#.##").format(getTotAtk(f, t, lev, talent, lvs).toDouble() / (f.du.itv.toDouble() / 30)).toString()
    }

    fun getDPS(em: Enemy?, multi: Int): String {
        return if (em == null) "" else DecimalFormat("#.##").format(getTotAtk(em, multi).toDouble() / (em.de.itv.toDouble() / 30)).toString()
    }

    fun getTrait(ef: Form?, talent: Boolean, lvs: IntArray?): String {
        if (ef == null) return ""
        val du: MaskUnit = if (lvs != null && ef.pCoin != null) if (talent) ef.pCoin.improve(lvs) else ef.du else ef.du
        val allcolor = StringBuilder()
        val alltrait = StringBuilder()
        for (i in 0..8) {
            if (i != 0) allcolor.append(Interpret.TRAIT[i]).append(", ")
            alltrait.append(Interpret.TRAIT[i]).append(", ")
        }
        var result: String
        result = Interpret.getTrait(du.type, 0)
        if (result == "") result = c.getString(R.string.unit_info_t_none)
        if (result == allcolor.toString()) result = c.getString(R.string.unit_info_t_allc)
        if (result == alltrait.toString()) result = c.getString(R.string.unit_info_t_allt)
        if (result.endsWith(", ")) result = result.substring(0, result.length - 2)
        return result
    }

    fun getTrait(em: Enemy?): String {
        if (em == null) return ""
        val de = em.de
        val allcolor = StringBuilder()
        val alltrait = StringBuilder()
        for (i in 0..8) {
            if (i != 0) allcolor.append(Interpret.TRAIT[i]).append(", ")
            alltrait.append(Interpret.TRAIT[i]).append(", ")
        }
        var result: String
        val star = de.star
        result = Interpret.getTrait(de.type, star)
        if (result == "") result = c.getString(R.string.unit_info_t_none)
        if (result == allcolor.toString()) result = c.getString(R.string.unit_info_t_allc)
        if (result == alltrait.toString()) result = c.getString(R.string.unit_info_t_allt)
        if (result.endsWith(", ")) result = result.substring(0, result.length - 2)
        return result
    }

    fun getCost(f: Form?, talent: Boolean, lvs: IntArray?): String {
        if (f == null) return ""
        val du: MaskUnit = if (lvs != null && f.pCoin != null) if (talent) f.pCoin.improve(lvs) else f.du else f.du
        return (du.price * 1.5).toInt().toString()
    }

    fun getDrop(em: Enemy?, t: Treasure): String {
        if (em == null) return ""
        return (em.de.drop * t.dropMulti).toInt().toString()
    }

    private fun getAtks(f: Form?, t: Treasure?, lev: Int, talent: Boolean, lvs: IntArray?): String {
        if (f == null || t == null) return ""
        val du: MaskUnit = if (lvs != null && f.pCoin != null) if (talent) f.pCoin.improve(lvs) else f.du else f.du
        val atks = du.rawAtkData()
        val damges = ArrayList<Int>()
        for (atk in atks) {
            damges.add((atk[0] * t.atkMulti * f.unit.lv.getMult(lev)).toInt())
        }
        val result = StringBuilder("(")
        for (i in damges.indices) {
            if (i < damges.size - 1) result.append("").append(damges[i]).append(", ") else result.append("").append(damges[i]).append(")")
        }
        return result.toString()
    }

    private fun getAtks(em: Enemy?, multi: Int): String {
        if (em == null) return ""
        val atks = em.de.rawAtkData()
        val damages = ArrayList<Int>()
        for (atk in atks) {
            damages.add((atk[0] * em.de.multi(BasisSet.current) * multi / 100).toInt())
        }
        val result = StringBuilder("(")
        for (i in damages.indices) {
            if (i < damages.size - 1) result.append("").append(damages[i]).append(", ") else result.append("").append(damages[i]).append(")")
        }
        return result.toString()
    }

    fun getSimu(f: Form?): String {
        if (f == null) return ""
        return if (Interpret.isType(f.du, 1)) c.getString(R.string.sch_atk_ra) else c.getString(R.string.sch_atk_si)
    }

    fun getSimu(em: Enemy?): String {
        if (em == null) return ""
        return if (Interpret.isType(em.de, 1)) c.getString(R.string.sch_atk_ra) else c.getString(R.string.sch_atk_si)
    }

    fun getTalentName(index: Int, f: Form?): String? {
        if (f == null) return ""
        val ans: String?
        val info = f.pCoin.info
        val abil = listOf(*abilID)
        val trait = listOf("37", "38", "39", "40")
        val basic = listOf("25", "26", "27", "31", "32")
        ans = if (trait.contains(info[index][0].toString())) c.getString(R.string.talen_trait) + talTool[abil.indexOf(info[index][0].toString())] else if (basic.contains(info[index][0].toString())) talTool[abil.indexOf(info[index][0].toString())] else c.getString(R.string.talen_abil) + talTool[abil.indexOf(info[index][0].toString())]
        return ans
    }

    fun number(num: Int): String {
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

    fun getID(mapcode: Int, stid: Int, posit: Int): String {
        val p = mapcodes.indexOf(mapcode.toString())
        return if (p == -1 || p >= mapcolcid.size) {
            "$mapcode-$stid-$posit"
        } else {
            mapcolcid[p] + "-" + stid + "-" + posit
        }
    }

    fun getDifficulty(diff: Int): String {
        return if (diff >= diffid.size || diff < 0) {
            if (diff == -1) c.getString(R.string.unit_info_t_none) else diff.toString()
        } else {
            c.getString(diffid[diff])
        }
    }

    fun getLayer(data: IntArray): String {
        return if (data[SCDef.L0] == data[SCDef.L1]) "" + data[SCDef.L0] else data[SCDef.L0].toString() + " ~ " + data[SCDef.L1]
    }

    fun getRespawn(data: IntArray, frse: Boolean): String {
        return if (data[SCDef.R0] == data[SCDef.R1]) if (frse) data[SCDef.R0].toString() + " f" else DecimalFormat("#.##").format(data[SCDef.R0].toFloat() / 30.toDouble()) + " s" else if (frse) data[SCDef.R0].toString() + " f ~ " + data[SCDef.R1] + " f" else DecimalFormat("#.##").format(data[SCDef.R0].toFloat() / 30.toDouble()) + " s ~ " + DecimalFormat("#.##").format(data[SCDef.R1].toFloat() / 30.toDouble()) + " s"
    }

    fun getBaseHealth(data: IntArray): String {
        return data[SCDef.C0].toString() + " %"
    }

    fun getMultiply(data: IntArray, multi: Int): String {
        return (data[SCDef.M].toFloat() * multi.toFloat() / 100.toFloat()).toInt().toString() + " %"
    }

    fun getNumber(data: IntArray): String {
        return if (data[SCDef.N] == 0) c.getString(R.string.infinity) else data[SCDef.N].toString()
    }

    fun getStart(data: IntArray, frse: Boolean): String {
        return if (frse) data[SCDef.S0].toString() + " f" else DecimalFormat("#.##").format(data[SCDef.S0].toFloat() / 30.toDouble()) + " s"
    }

    fun getLimit(l: Limit?): Array<String> {
        if (l == null) return arrayOf("")
        val limits: MutableList<String> = ArrayList()
        if (l.line != 0) {
            val result = c.getString(R.string.limit_line) + " : " + c.getString(R.string.limit_line2)
            limits.add(result)
        }
        if (l.max != 0) {
            val result = c.getString(R.string.limit_max) + " : " + c.getString(R.string.limit_max2).replace("_", l.max.toString())
            limits.add(result)
        }
        if (l.min != 0) {
            val result = c.getString(R.string.limit_min) + " : " + c.getString(R.string.limit_min2).replace("_", l.min.toString())
            limits.add(result)
        }
        if (l.rare != 0) {
            val rid = intArrayOf(R.string.sch_rare_ba, R.string.sch_rare_ex, R.string.sch_rare_ra, R.string.sch_rare_sr, R.string.sch_rare_ur, R.string.sch_rare_lr)
            val rare = StringBuilder()
            for (i in rid.indices) {
                if (l.rare shr i and 1 == 1) {
                    rare.append(c.getString(rid[i])).append(", ")
                }
            }
            val result = c.getString(R.string.limit_rare) + " : " + rare.toString().substring(0, rare.length - 2)
            limits.add(result)
        }
        if (l.num != 0) {
            val result = c.getString(R.string.limit_deploy) + " : " + l.num
            limits.add(result)
        }
        if (l.group != null) {
            val units = StringBuilder()
            val u: List<common.util.unit.Unit> = ArrayList(l.group.set)
            for (i in u.indices) {
                if (i == l.group.set.size - 1) {
                    var f = MultiLangCont.FNAME.getCont(u[i].forms[0])
                    if (f == null) f = ""
                    units.append(f)
                } else {
                    var f = MultiLangCont.FNAME.getCont(u[i].forms[0])
                    if (f == null) f = ""
                    units.append(f).append(", ")
                }
            }
            val result: String
            result = if (l.group.type == 0) c.getString(R.string.limit_chra) + " : " + c.getString(R.string.limit_chra1).replace("_", units.toString()) else c.getString(R.string.limit_chra) + " : " + c.getString(R.string.limit_chra2).replace("_", units.toString())
            limits.add(result)
        }
        return limits.toTypedArray()
    }

    fun getXP(xp: Int, t: Treasure?, legend: Boolean): String {
        if (t == null) return ""
        return if (legend) "" + (xp * t.xpMult * 9).toInt() else "" + (xp * t.xpMult).toInt()
    }

    init {
        talList
    }
}