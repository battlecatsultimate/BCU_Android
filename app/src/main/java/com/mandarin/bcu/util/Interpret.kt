package com.mandarin.bcu.util

import android.content.Context
import android.util.Log
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore.isEnglish
import common.battle.BasisSet
import common.battle.Treasure
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
    lateinit var TRAIT: Array<String>

    /**
     * star names
     */
    lateinit var STAR: Array<String>

    /**
     * ability name
     */
    lateinit var ABIS: Array<String>

    lateinit var PROC: Array<String>

    lateinit var TEXT: Array<String>

    /**
     * treasure max
     */
    private val TMAX = intArrayOf(30, 30, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 600, 1500, 100,
            100, 100, 30, 30, 30, 30, 30, 10, 300, 300, 600, 600, 600, 20, 20, 20, 20, 20, 20, 20)

    private val PROCIND = arrayOf("WEAK", "PT", "PT", "PTD", "PTD", "PT", "PT", "STRONG", "PROB", "PM", "PROB", "PM", "WAVE", "VOLC", "IMU", "IMU", "IMU", "IMU", "IMU", "IMU", "IMU", "IMU", "IMU", "PM", "BURROW", "REVIVE", "PT", "PT", "SUMMON", "MOVEWAVE", "THEME", "POISON", "PROB", "ARMOR", "SPEED", "CRITI")

    private val immune = listOf(13, 14, 15, 16, 17, 18, 19, 33, 34)

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
            val f = ProcLang.get().get(PROCIND[i]).format

            var ans = Formatter.format(f, getProcObject(i, mr), c)

            if(!l.contains(ans)) {
                ans = if(isEnglish)
                    "$ans [${getNumberAttack(numberWithExtension(1, lang), lang)}]"
                else
                    "$ans [${TEXT[46].replace("_", 1.toString())}]"
            }

            l.add(ans)
            id.add(i)
        }

        for (k in 0 until du.atkCount) {
            val ma = du.getAtkModel(k)

            for(i in PROCIND.indices) {
                val mf = ProcLang.get().get(PROCIND[i]).format

                var ans = Formatter.format(mf, getProcObject(i, ma), c)

                if(!l.contains(ans)) {
                    if(id.contains(i)) {
                        ans = if(isEnglish)
                            "$ans [${getNumberAttack(numberWithExtension(k + 1, lang), lang)}]"
                        else
                            "$ans [${TEXT[46].replace("_", (k + 1).toString())}]"
                    }

                    l.add(ans)
                    id.add(i)
                }
            }
        }
        return l
    }

    private fun getProcObject(ind: Int, atk: MaskAtk): Any {
        return when (ind) {
            0 -> atk.proc.WEAK
            1 -> atk.proc.STOP
            2 -> atk.proc.SLOW
            3 -> atk.proc.KB
            4 -> atk.proc.WARP
            5 -> atk.proc.CURSE
            6 -> atk.proc.IMUATK
            7 -> atk.proc.STRONG
            8 -> atk.proc.LETHAL
            9 -> atk.proc.CRIT
            10 -> atk.proc.BREAK
            11 -> atk.proc.SATK
            12 -> atk.proc.WAVE
            13 -> atk.proc.VOLC
            14 -> atk.proc.IMUWEAK
            15 -> atk.proc.IMUSTOP
            16 -> atk.proc.IMUSLOW
            17 -> atk.proc.IMUKB
            18 -> atk.proc.IMUWAVE
            19 -> atk.proc.IMUVOLC
            20 -> atk.proc.IMUWARP
            21 -> atk.proc.IMUCURSE
            22 -> atk.proc.IMUPOIATK
            23 -> atk.proc.POIATK
            24 -> atk.proc.BURROW
            25 -> atk.proc.REVIVE
            26 -> atk.proc.SEAL
            27 -> atk.proc.TIME
            28 -> atk.proc.SUMMON
            29 -> atk.proc.MOVEWAVE
            30 -> atk.proc.THEME
            31 -> atk.proc.POISON
            32 -> atk.proc.BOSS
            33 -> atk.proc.ARMOR
            34 -> atk.proc.SPEED
            35 -> atk.proc.CRITI
            else -> {
                Log.e("Interpret", "Invalid index : $ind")
                atk.proc.KB
            }
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
                    if (i == 0) l.add(ABIS[i] + addition[0]) else if (i == 1) l.add(ABIS[i] + addition[1]) else if (i == 2) l.add(ABIS[i] + addition[2]) else if (i == 4) l.add(ABIS[i] + addition[3]) else if (i == 5) l.add(ABIS[i] + addition[4]) else if (i == 14) l.add(ABIS[i] + addition[5]) else if (i == 17) l.add(ABIS[i] + addition[6]) else if (i == 20) l.add(ABIS[i] + addition[7]) else if (i == 21) l.add(ABIS[i] + addition[8]) else l.add(ABIS[i])
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

    fun getValue(ind: Int, t: Treasure): Int {
        if (ind == 0) return t.tech[LV_RES] else if (ind == 1) return t.tech[LV_ACC] else if (ind == 2) return t.trea[T_ATK] else if (ind == 3) return t.trea[T_DEF] else if (ind == 4) return t.trea[T_RES] else if (ind == 5) return t.trea[T_ACC] else if (ind == 6) return t.fruit[T_RED] else if (ind == 7) return t.fruit[T_FLOAT] else if (ind == 8) return t.fruit[T_BLACK] else if (ind == 9) return t.fruit[T_ANGEL] else if (ind == 10) return t.fruit[T_METAL] else if (ind == 11) return t.fruit[T_ZOMBIE] else if (ind == 12) return t.fruit[T_ALIEN] else if (ind == 13) return t.alien else if (ind == 14) return t.star else if (ind == 15) return t.gods[0] else if (ind == 16) return t.gods[1] else if (ind == 17) return t.gods[2] else if (ind == 18) return t.tech[LV_BASE] else if (ind == 19) return t.tech[LV_WORK] else if (ind == 20) return t.tech[LV_WALT] else if (ind == 21) return t.tech[LV_RECH] else if (ind == 22) return t.tech[LV_CATK] else if (ind == 23) return t.tech[LV_CRG] else if (ind == 24) return t.trea[T_WORK] else if (ind == 25) return t.trea[T_WALT] else if (ind == 26) return t.trea[T_RECH] else if (ind == 27) return t.trea[T_CATK] else if (ind == 28) return t.trea[T_BASE] else if (ind == 29) return t.bslv[BASE_H] else if (ind == 30) return t.bslv[BASE_SLOW] else if (ind == 31) return t.bslv[BASE_WALL] else if (ind == 32) return t.bslv[BASE_STOP] else if (ind == 33) return t.bslv[BASE_WATER] else if (ind == 34) return t.bslv[BASE_GROUND] else if (ind == 35) return t.bslv[BASE_BARRIER] else if (ind == 36) return t.bslv[BASE_CURSE]
        return -1
    }

    fun isType(de: MaskUnit, type: Int): Boolean {
        val raw = de.rawAtkData()
        if (type == 0) return !de.isRange else if (type == 1) return de.isRange else if (type == 2) return de.isLD else if (type == 3) return raw.size > 1 else if (type == 4) return de.isOmni else if (type == 5) return de.tba + raw[0][1] < de.itv / 2
        return false
    }

    fun isType(de: MaskEnemy, type: Int): Boolean {
        val raw = de.rawAtkData()
        if (type == 0) return !de.isRange else if (type == 1) return de.isRange else if (type == 2) return de.isLD else if (type == 3) return raw.size > 1 else if (type == 4) return de.isOmni else if (type == 5) return de.tba + raw[0][1] < de.itv / 2
        return false
    }

    fun setValue(ind: Int, v: Int, b: BasisSet) {
        setVal(ind, v, b.t())
        for (bl in b.lb) setVal(ind, v, bl.t())
    }

    private fun setVal(ind: Int, vi: Int, t: Treasure) {
        var v = vi
        if (v < 0) v = 0
        if (v > TMAX[ind]) v = TMAX[ind]
        if (ind == 0) t.tech[LV_RES] = v else if (ind == 1) t.tech[LV_ACC] = v else if (ind == 2) t.trea[T_ATK] = v else if (ind == 3) t.trea[T_DEF] = v else if (ind == 4) t.trea[T_RES] = v else if (ind == 5) t.trea[T_ACC] = v else if (ind == 6) t.fruit[T_RED] = v else if (ind == 7) t.fruit[T_FLOAT] = v else if (ind == 8) t.fruit[T_BLACK] = v else if (ind == 9) t.fruit[T_ANGEL] = v else if (ind == 10) t.fruit[T_METAL] = v else if (ind == 11) t.fruit[T_ZOMBIE] = v else if (ind == 12) t.fruit[T_ALIEN] = v else if (ind == 13) t.alien = v else if (ind == 14) t.star = v else if (ind == 15) t.gods[0] = v else if (ind == 16) t.gods[1] = v else if (ind == 17) t.gods[2] = v else if (ind == 18) t.tech[LV_BASE] = v else if (ind == 19) t.tech[LV_WORK] = v else if (ind == 20) t.tech[LV_WALT] = v else if (ind == 21) t.tech[LV_RECH] = v else if (ind == 22) t.tech[LV_CATK] = v else if (ind == 23) t.tech[LV_CRG] = v else if (ind == 24) t.trea[T_WORK] = v else if (ind == 25) t.trea[T_WALT] = v else if (ind == 26) t.trea[T_RECH] = v else if (ind == 27) t.trea[T_CATK] = v else if (ind == 28) t.trea[T_BASE] = v else if (ind == 29) t.bslv[BASE_H] = v else if (ind == 30) t.bslv[BASE_SLOW] = v else if (ind == 31) t.bslv[BASE_WALL] = v else if (ind == 32) t.bslv[BASE_STOP] = v else if (ind == 33) t.bslv[BASE_WATER] = v else if (ind == 34) t.bslv[BASE_GROUND] = v else if (ind == 35) t.bslv[BASE_BARRIER] = v else t.bslv[BASE_CURSE] = v
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
            else -> TEXT[7].replace("_", "" + n)
        }
    }
}