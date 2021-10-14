package com.mandarin.bcu.util

import android.content.Context
import android.util.Log
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.StaticStore.isEnglish
import common.battle.data.*
import common.pack.Identifier
import common.util.Data
import common.util.lang.Formatter
import common.util.lang.ProcLang
import common.util.unit.Trait
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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

    var ATK = ""

    /**
     * Converts Data Proc index to BCU Android Proc Index
     */
    private val P_INDEX = intArrayOf(P_WEAK, P_STOP, P_SLOW, P_KB, P_WARP, P_CURSE, P_IMUATK,
            P_STRONG, P_LETHAL, P_CRIT, P_BREAK, P_SHIELDBREAK, P_SATK, P_MINIWAVE, P_WAVE, P_VOLC, P_IMUWEAK,
            P_IMUSTOP, P_IMUSLOW, P_IMUKB, P_IMUWAVE, P_IMUVOLC, P_IMUWARP, P_IMUCURSE, P_IMUPOIATK,
            P_POIATK, P_BARRIER, P_DEMONSHIELD, P_DEATHSURGE, P_BURROW, P_REVIVE, P_SNIPER, P_SEAL, P_TIME, P_SUMMON, P_MOVEWAVE, P_THEME,
            P_POISON, P_BOSS, P_ARMOR, P_SPEED, P_COUNTER, P_DMGCUT, P_DMGCAP, P_CRITI, P_IMUPOI, P_IMUSEAL, P_IMUMOVING, P_IMUSUMMON, P_IMUARMOR, P_IMUSPEED)

    /**
     * treasure max
     */
    private val TMAX = intArrayOf(30, 30, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 600, 1500, 100,
            100, 100, 30, 30, 30, 30, 30, 10, 300, 300, 600, 600, 600, 20, 20, 20, 20, 20, 20, 20)

    private val PROCIND = arrayOf("WEAK", "STOP", "SLOW", "KB", "WARP", "CURSE", "IMUATK",
            "STRONG", "LETHAL", "CRIT", "BREAK", "SHIELDBREAK", "SATK", "MINIWAVE", "WAVE", "VOLC", "IMUWEAK",
            "IMUSTOP", "IMUSLOW", "IMUKB", "IMUWAVE", "IMUVOLC", "IMUWARP", "IMUCURSE", "IMUPOIATK",
            "POIATK", "DEMONSHIELD", "DEATHSURGE", "BURROW", "REVIVE", "SNIPER", "SEAL", "TIME", "SUMMON", "MOVEWAVE", "THEME",
            "POISON", "BOSS", "ARMOR", "SPEED", "CRITI", "IMUPOI", "IMUSEAL", "IMUMOVING", "IMUSUMMON", "IMUARMOR", "IMUSPEED")

    private val immune = listOf(P_IMUWEAK, P_IMUSTOP, P_IMUSLOW, P_IMUKB, P_IMUWAVE, P_IMUWARP, P_IMUCURSE, P_IMUPOIATK, P_IMUVOLC)

    val traitMask = intArrayOf(TRAIT_RED, TRAIT_FLOAT, TRAIT_BLACK, TRAIT_METAL, TRAIT_ANGEL,
        TRAIT_ALIEN, TRAIT_ZOMBIE, TRAIT_DEMON, TRAIT_RELIC, TRAIT_WHITE, TRAIT_EVA, TRAIT_WITCH
    )

    fun getTrait(traits: List<Trait>, star: Int): String {
        val ans = StringBuilder()

        for(trait in traits) {
            if(trait.id.pack == Identifier.DEF) {
                if(trait.id.id == 6 && star == 1) {
                    ans.append(TRAIT[trait.id.id])
                        .append(" (")
                        .append(STAR[star])
                        .append("), ")
                } else {
                    ans.append(TRAIT[trait.id.id])
                        .append(", ")
                }
            } else {
                ans.append(trait.name)
                    .append(", ")
            }
        }

        return ans.toString()
    }

    fun getProc(du: MaskEntity, useSecond: Boolean, isEnemy: Boolean, magnif: DoubleArray): List<String> {
        val lang = Locale.getDefault().language

        val common = if(du is CustomEntity)
            du.common
        else
            true

        val l: MutableList<String> = ArrayList()

        val c = Formatter.Context(isEnemy, useSecond, magnif)

        if(common) {
            val mr = du.repAtk

            for (i in PROCIND.indices) {
                if (isValidProc(i, mr)) {
                    val f = ProcLang.get().get(PROCIND[i]).format

                    val ans = if (immune.contains(P_INDEX[i]) && isResist(P_INDEX[i], mr)) {
                        "${StaticStore.pnumber.size - immune.size + immune.indexOf(P_INDEX[i])}\\" + Formatter.format(f, getProcObject(i, mr), c)
                    } else {
                        "$i\\" + Formatter.format(f, getProcObject(i, mr), c)
                    }

                    l.add(ans)
                }
            }
        } else {
            val atkMap = HashMap<String, ArrayList<Int>>()

            val mr = du.repAtk

            for (i in PROCIND.indices) {
                if (isValidProc(i, mr) && mr.proc.sharable(P_INDEX[i])) {
                    val f = ProcLang.get().get(PROCIND[i]).format

                    val ans = if (immune.contains(P_INDEX[i]) && isResist(P_INDEX[i], mr)) {
                        "${StaticStore.pnumber.size - immune.size + immune.indexOf(P_INDEX[i])}\\" + Formatter.format(f, getProcObject(i, mr), c)
                    } else {
                        "$i\\" + Formatter.format(f, getProcObject(i, mr), c)
                    }

                    l.add(ans)
                }
            }

            for (k in 0 until du.atkCount) {
                val ma = du.getAtkModel(k)

                for (i in PROCIND.indices) {
                    if (isValidProc(i, ma) && !ma.proc.sharable(P_INDEX[i])) {
                        val mf = ProcLang.get().get(PROCIND[i]).format

                        val ans = if (immune.contains(P_INDEX[i]) && isResist(P_INDEX[i], ma)) {
                            "${StaticStore.pnumber.size - immune.size + immune.indexOf(P_INDEX[i])}\\" + Formatter.format(mf, getProcObject(i, ma), c)
                        } else {
                            "$i\\" + Formatter.format(mf, getProcObject(i, ma), c)
                        }

                        val inds = atkMap[ans] ?: ArrayList()

                        inds.add(i)

                        atkMap[ans] = inds
                    }
                }
            }

            for(key in atkMap.keys) {
                val inds = atkMap[key] ?: ArrayList()

                when {
                    inds.isEmpty() -> {
                        l.add(key)
                    }
                    inds.size == du.atkCount -> {
                        l.add(key)
                    }
                    else -> {
                        l.add(getFullExplanationWithAtk(key, inds, lang))
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

    private fun getFullExplanationWithAtk(explanation: String, inds: ArrayList<Int>, lang: String) : String {
        if(isEnglish) {
            val builder = StringBuilder("[")

            for(i in inds.indices) {
                builder.append(numberWithExtension(inds[i], lang))

                if(i < inds.size - 1)
                    builder.append(", ")
            }

            return explanation + " " + builder.append(getNumberAttack(lang)).append("]").toString()
        } else {
            val builder = StringBuilder()

            for(i in inds.indices) {
                builder.append(inds[i])

                if(i < inds.size - 1)
                    builder.append(", ")
            }

            return explanation + " " + ATK.replace("_", builder.toString())
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
            P_IMUPOIATK -> atk.proc.IMUPOIATK.mult != 100
            P_IMUVOLC -> atk.proc.IMUVOLC.mult != 100
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

    fun numberWithExtension(n: Int, lang: String): String {
        val f = n % 10
        return when (lang) {
            EN -> when (f) {
                1 -> n.toString() + if(n != 11) "st" else "th"
                2 -> n.toString() + if(n != 12) "nd" else "th"
                3 -> n.toString() + if(n != 13) "rd" else "th"
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

    private fun getNumberAttack(lang: String): String {
        return when (lang) {
            EN -> "Attack"
            RU -> "Аттака"
            FR -> "Attaque"
            else -> ""
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