package com.mandarin.bcu.util

import android.content.Context
import android.util.Log
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.StaticStore.isEnglish
import common.battle.data.MaskAtk
import common.battle.data.MaskEnemy
import common.battle.data.MaskEntity
import common.battle.data.MaskUnit
import common.util.Data
import common.util.lang.Formatter
import common.util.lang.ProcLang
import java.util.*

object Interpret : Data() {
    const val EN = "en"
    const val ZH = "zh"
    const val JA = "ja"
    const val KO = "ko"
    const val RU = "ru"
    const val FR = "fr"

    /**
     * enemy traits
     */
    var TRAIT = Array(0) { "" }

    /**
     * star names
     */
    var STAR = Array(0) { "" }

    /**
     * ability name
     */
    var ABIS = Array(0) { "" }

    var PROC = Array(0) { "" }

    var ATK = ""

    /**
     * Converts Data Proc index to BCU Android Proc Index
     */
    private val P_INDEX = intArrayOf(P_WEAK, P_STOP, P_SLOW, P_KB, P_WARP, P_CURSE, P_IMUATK,
            P_STRONG, P_LETHAL, P_CRIT, P_BREAK, P_SATK, P_MINIWAVE, P_WAVE, P_VOLC, P_IMUWEAK,
            P_IMUSTOP, P_IMUSLOW, P_IMUKB, P_IMUWAVE, P_IMUVOLC, P_IMUWARP, P_IMUCURSE, P_IMUPOIATK,
            P_POIATK, P_BURROW, P_REVIVE, P_SNIPER, P_SEAL, P_TIME, P_SUMMON, P_MOVEWAVE, P_THEME,
            P_POISON, P_BOSS, P_ARMOR, P_SPEED, P_CRITI)

    /**
     * treasure max
     */
    private val TMAX = intArrayOf(30, 30, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 600, 1500, 100,
            100, 100, 30, 30, 30, 30, 30, 10, 300, 300, 600, 600, 600, 20, 20, 20, 20, 20, 20, 20)

    private val PROCIND = arrayOf("WEAK", "STOP", "SLOW", "KB", "WARP", "CURSE", "IMUATK",
            "STRONG", "LETHAL", "CRIT", "BREAK", "SATK", "MINIWAVE", "WAVE", "VOLC", "IMUWEAK",
            "IMUSTOP", "IMUSLOW", "IMUKB", "IMUWAVE", "IMUVOLC", "IMUWARP", "IMUCURSE", "IMUPOIATK",
            "POIATK", "BURROW", "REVIVE", "SNIPER", "SEAL", "TIME", "SUMMON", "MOVEWAVE", "THEME",
            "POISON", "BOSS", "ARMOR", "SPEED", "CRITI")

    private val immune = listOf(P_IMUWEAK, P_IMUSTOP, P_IMUSLOW, P_IMUKB, P_IMUWAVE, P_IMUWARP, P_IMUCURSE)

    fun getTrait(type: Int, star: Int): String {
        val ans = StringBuilder()
        for (i in TRAIT.indices) if (type shr i and 1 > 0) {
            if (i == 6 && star == 1) ans.append(TRAIT[i]).append(" ").append("(").append(STAR[star]).append(")").append(", ") else ans.append(TRAIT[i]).append(", ")
        }
        return ans.toString()
    }

    fun getProc(du: MaskEntity, useSecond: Boolean): List<String> {
        val res: MutableList<Int> = ArrayList()
        val lang = Locale.getDefault().language
        for (i in immune.indices.reversed()) {
            res.add(PROC.size - i)
        }
        val l: MutableList<String> = ArrayList()
        val id: MutableList<Int> = ArrayList()
        val mr = du.repAtk
        val c = Formatter.Context(true, useSecond)
        for (i in PROCIND.indices) {
            if (isValidProc(i, mr)) {
                val f = ProcLang.get().get(PROCIND[i]).format

                var ans = if (immune.contains(P_INDEX[i]) && isResist(P_INDEX[i], mr)) {
                    "${StaticStore.pnumber.size - 7 + immune.indexOf(P_INDEX[i])}\\" + Formatter.format(f, getProcObject(i, mr), c)
                } else {
                    "$i\\" + Formatter.format(f, getProcObject(i, mr), c)
                }

                if (!l.contains(ans)) {
                    if (id.contains(i)) {
                        ans = if (isEnglish)
                            "$ans [${getNumberAttack(numberWithExtension(1, lang), lang)}]"
                        else
                            "$ans [${ATK.replace("_", 1.toString())}]"
                    }
                }

                l.add(ans)
                id.add(i)
            }
        }

        for (k in 0 until du.atkCount) {
            val ma = du.getAtkModel(k)

            for (i in PROCIND.indices) {
                if (isValidProc(i, ma)) {
                    val mf = ProcLang.get().get(PROCIND[i]).format

                    var ans = if (immune.contains(P_INDEX[i]) && isResist(P_INDEX[i], ma)) {
                        "${StaticStore.pnumber.size - 7 + immune.indexOf(P_INDEX[i])}\\" + Formatter.format(mf, getProcObject(i, ma), c)
                    } else {
                        "$i\\" + Formatter.format(mf, getProcObject(i, ma), c)
                    }

                    if (!l.contains(ans)) {
                        if (id.contains(i)) {
                            ans = if (isEnglish)
                                "$ans [${getNumberAttack(numberWithExtension(k + 1, lang), lang)}]"
                            else
                                "$ans [${ATK.replace("_", (k + 1).toString())}]"
                        }

                        l.add(ans)
                        id.add(i)
                    }
                }
            }
        }

        return l
    }

    private fun getProcObject(ind: Int, atk: MaskAtk): Any {
        return when (ind) {
            in P_INDEX.indices -> {
                atk.proc.getArr(P_INDEX[ind])
            }
            else -> {
                Log.e("Interpret", "Invalid index : $ind")
                atk.proc.KB
            }
        }
    }

    private fun isValidProc(ind: Int, atk: MaskAtk): Boolean {
        return when (ind) {
            in P_INDEX.indices -> {
                atk.proc.getArr(P_INDEX[ind]).exists()
            }
            else -> {
                Log.e("Interpret", "Invalid index : $ind")
                atk.proc.KB.exists()
            }
        }
    }

    private fun isResist(i: Int, atk: MaskAtk): Boolean {
        return when (i) {
            P_IMUWEAK -> atk.proc.IMUWEAK.mult != 100
            P_IMUSTOP -> atk.proc.IMUSTOP.mult != 100
            P_IMUSLOW -> atk.proc.IMUSLOW.mult != 100
            P_IMUKB -> atk.proc.IMUKB.mult != 100
            P_IMUWAVE -> atk.proc.IMUWAVE.mult != 100
            P_IMUWARP -> atk.proc.IMUWARP.mult != 100
            P_IMUCURSE -> atk.proc.IMUCURSE.mult != 100
            else -> false
        }
    }

    fun getAbiid(me: MaskUnit): List<Int> {
        val l: MutableList<Int> = ArrayList()
        for (i in ABIS.indices) if (me.abi shr i and 1 > 0) l.add(i)
        return l
    }

    fun getAbiid(me: MaskEnemy): List<Int> {
        val l: MutableList<Int> = ArrayList()
        for (i in ABIS.indices) if (me.abi shr i and 1 > 0) l.add(i)
        return l
    }

    fun getAbi(me: MaskUnit, frag: Array<Array<String>>, addition: Array<String>, lang: Int): List<String> {
        val l: MutableList<String> = ArrayList()
        for (i in ABIS.indices) {
            val imu = StringBuilder(frag[lang][0])
            if (me.abi shr i and 1 > 0) {
                if (ABIS[i].startsWith("Imu.")) {
                    imu.append(ABIS[i].substring(4))
                } else {
                    when (i) {
                        0 -> l.add(ABIS[i] + addition[0])
                        1 -> l.add(ABIS[i] + addition[1])
                        2 -> l.add(ABIS[i] + addition[2])
                        4 -> l.add(ABIS[i] + addition[3])
                        5 -> l.add(ABIS[i] + addition[4])
                        14 -> l.add(ABIS[i] + addition[5])
                        17 -> l.add(ABIS[i] + addition[6])
                        20 -> l.add(ABIS[i] + addition[7])
                        21 -> l.add(ABIS[i] + addition[8])
                        else -> l.add(ABIS[i])
                    }
                }
            }
            if (imu.toString().isNotEmpty() && imu.toString() != frag[lang][0]) l.add(imu.toString())
        }
        return l
    }

    fun getAbi(me: MaskEnemy, frag: Array<Array<String>>, addition: Array<String>, lang: Int): List<String> {
        val l: MutableList<String> = ArrayList()
        for (i in ABIS.indices) {
            val imu = StringBuilder(frag[lang][0])
            if (me.abi shr i and 1 > 0) if (ABIS[i].startsWith("Imu.")) imu.append(ABIS[i].substring(4)) else {
                if (i == 0) l.add(ABIS[i] + addition[0]) else if (i == 1) l.add(ABIS[i] + addition[1]) else if (i == 2) l.add(ABIS[i] + addition[2]) else if (i == 4) l.add(ABIS[i] + addition[3]) else if (i == 5) l.add(ABIS[i] + addition[4]) else if (i == 14) l.add(ABIS[i] + addition[5]) else if (i == 17) l.add(ABIS[i] + addition[6]) else if (i == 20) l.add(ABIS[i] + addition[7]) else if (i == 21) l.add(ABIS[i] + addition[8]) else l.add(ABIS[i])
            }
            if (imu.toString().isNotEmpty() && imu.toString() != frag[lang][0]) l.add(imu.toString())
        }
        return l
    }

    fun isType(de: MaskUnit, type: Int): Boolean {
        val raw = de.rawAtkData()
        return when (type) {
            0 -> !de.isRange
            1 -> de.isRange
            2 -> de.isLD
            3 -> raw.size > 1
            4 -> de.isOmni
            5 -> de.tba + raw[0][1] < de.itv / 2
            else -> false
        }
    }

    fun isType(de: MaskEnemy, type: Int): Boolean {
        val raw = de.rawAtkData()

        return when (type) {
            0 -> !de.isRange
            1 -> de.isRange
            2 -> de.isLD
            3 -> raw.size > 1
            4 -> de.isOmni
            5 -> de.tba + raw[0][1] < de.itv / 2
            else -> false
        }

    }

    fun numberWithExtension(n: Int, lang: String?): String {
        val f = n % 10
        return when (lang) {
            EN -> when (f) {
                1 -> n.toString() + "st"
                2 -> n.toString() + "nd"
                3 -> n.toString() + "rd"
                else -> n.toString() + "th"
            }
            RU -> {
                if (f == 3) {
                    n.toString() + "ья"
                } else n.toString() + "ая"
            }
            FR -> {
                if (f == 1) {
                    n.toString() + "ière"
                } else n.toString() + "ième"
            }
            else -> "" + n
        }
    }

    private fun getNumberAttack(pre: String, lang: String): String {
        return when (lang) {
            EN -> "$pre Attack"
            RU -> "$pre Аттака"
            FR -> "$pre Attaque"
            else -> pre
        }
    }

    private fun getOnceTwice(n: Int, lang: String, c: Context): String {
        return when (lang) {
            EN -> when (n) {
                1 -> " Once"
                2 -> " Twice"
                3 -> " $n Times"
                -1 -> " " + c.getString(R.string.infinity) + " Times"
                else -> " $n"
            }
            FR -> if (n == -1) {
                " Temps Infini"
            } else {
                " $n Fois"
            }
            RU -> if (n % 10 == 1) {
                " $n раза"
            } else if (n % 10 == 2 || n % 10 == 3 || n % 10 == 4) {
                if (n / 10 == 1) {
                    " $n раз"
                } else {
                    " $n раза"
                }
            } else if (n == -1) {
                " " + c.getString(R.string.infinity) + " раз"
            } else {
                " $n раза"
            }
            else -> ATK.replace("_", "" + n)
        }
    }
}