package com.mandarin.bcu.androidutil.io

import android.os.Environment
import com.mandarin.bcu.androidutil.StaticStore
import common.CommonStatic
import common.system.MultiLangCont
import common.system.files.AssetData
import common.util.pack.Pack
import java.io.File
import java.util.*

object LangLoader {
    fun readUnitLang() {
        val lan = arrayOf("/en/", "/zh/", "/kr/", "/jp/")
        val files = arrayOf("UnitName.txt", "UnitExplanation.txt", "CatFruitExplanation.txt", "ComboName.txt")

        MultiLangCont.FNAME.clear()
        MultiLangCont.FEXP.clear()
        MultiLangCont.CFEXP.clear()
        MultiLangCont.COMNAME.clear()
        for (l in lan) {
            for (n in files) {
                val path = Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU/lang" + l + n
                val f = File(path)
                if (f.exists()) {
                    val qs = AssetData.getAsset(f).readLine()
                    when (n) {
                        "UnitName.txt" -> {
                            val size = qs.size
                            var j = 0
                            while (j < size) {
                                val strs = Objects.requireNonNull(qs.poll()).trim { it <= ' ' }.split("\t").toTypedArray()
                                val u = Pack.def.us.ulist[CommonStatic.parseIntN(strs[0])]
                                if (u == null) {
                                    j++
                                    continue
                                }
                                var i = 0
                                while (i < u.forms.size.coerceAtMost(strs.size - 1)) {
                                    MultiLangCont.FNAME.put(l.substring(1, l.length - 1), u.forms[i], strs[i + 1].trim { it <= ' ' })
                                    i++
                                }
                                j++
                            }
                        }
                        "UnitExplanation.txt" -> {
                            val size = qs.size
                            var j = 0
                            while (j < size) {
                                val strs = Objects.requireNonNull(qs.poll()).trim { it <= ' ' }.split("\t").toTypedArray()
                                val u = Pack.def.us.ulist[CommonStatic.parseIntN(strs[0])]
                                if (u == null) {
                                    j++
                                    continue
                                }
                                var i = 0
                                while (i < u.forms.size.coerceAtMost(strs.size - 1)) {
                                    val lines = strs[i + 1].trim { it <= ' ' }.split("<br>").toTypedArray()
                                    MultiLangCont.FEXP.put(l.substring(1, l.length - 1), u.forms[i], lines)
                                    i++
                                }
                                j++
                            }
                        }
                        "CatFruitExplanation.txt" -> for (str in qs) {
                            val strs = str.trim { it <= ' ' }.split("\t").toTypedArray()
                            val u = Pack.def.us.ulist[CommonStatic.parseIntN(strs[0])]
                                    ?: continue
                            if (strs.size == 1) {
                                continue
                            }
                            val lines = strs[1].split("<br>").toTypedArray()
                            MultiLangCont.CFEXP.put(l.substring(1, l.length - 1), u.info, lines)
                        }
                        "ComboName.txt" -> for (str in qs) {
                            val strs = str.trim { it <= ' ' }.split("\t").toTypedArray()
                            if (strs.size <= 1) {
                                continue
                            }
                            val id = strs[0].trim { it <= ' ' }.toInt()
                            val name = strs[1].trim { it <= ' ' }
                            MultiLangCont.COMNAME.put(l.substring(1, l.length - 1), id, name)
                        }
                    }
                }
            }
        }
        StaticStore.unitlang = 0
    }

    fun readEnemyLang() {
        val lan = arrayOf("/en/", "/zh/", "/kr/", "/jp/")
        val files = arrayOf("UnitName.txt", "UnitExplanation.txt", "CatFruitExplanation.txt", "ComboName.txt")

        MultiLangCont.ENAME.clear()
        MultiLangCont.EEXP.clear()
        for (l in lan) {
            for (n in files) {
                val path = Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU/lang" + l + n
                val f = File(path)
                if (f.exists()) {
                    val qs = AssetData.getAsset(f).readLine()
                    when (n) {
                        "EnemyName.txt" -> for (str in qs) {
                            val strs = str.trim { it <= ' ' }.split("\t").toTypedArray()
                            val em = Pack.def.es[CommonStatic.parseIntN(strs[0])]
                                    ?: continue
                            if (strs.size == 1) MultiLangCont.ENAME.put(l.substring(1, l.length - 1), em, null) else MultiLangCont.ENAME.put(l.substring(1, l.length - 1), em, if (strs[1].trim { it <= ' ' }.startsWith("【")) strs[1].trim { it <= ' ' }.substring(1, strs[1].trim { it <= ' ' }.length - 1) else strs[1].trim { it <= ' ' })
                        }
                        "EnemyExplanation.txt" -> for (str in qs) {
                            val strs = str.trim { it <= ' ' }.split("\t").toTypedArray()
                            val em = Pack.def.es[CommonStatic.parseIntN(strs[0])]
                                    ?: continue
                            if (strs.size == 1) MultiLangCont.EEXP.put(l.substring(1, l.length - 1), em, null) else {
                                val lines = strs[1].trim { it <= ' ' }.split("<br>").toTypedArray()
                                MultiLangCont.EEXP.put(l.substring(1, l.length - 1), em, lines)
                            }
                        }
                    }
                }
            }
        }
        StaticStore.enemeylang = 0
    }

    fun readStageLang() {
        val lan = arrayOf("/en/", "/zh/", "/kr/", "/jp/")
        val file = "StageName.txt"
        val diff = "Difficulty.txt"
        val rewa = "RewardName.txt"

        MultiLangCont.SMNAME.clear()
        MultiLangCont.STNAME.clear()
        MultiLangCont.RWNAME.clear()
        for (l in lan) {
            val path = Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU/lang" + l + file
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
            val path = Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU/lang" + l + rewa
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
        val path = Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU/lang/"
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

    fun readMapLang() {
        StaticStore.mapnames = arrayOfNulls(StaticStore.map.size)
        for (i in StaticStore.mapnames.indices) {
            val mc = StaticStore.map[StaticStore.MAPCODE[i]] ?: continue
            StaticStore.mapnames[i] = arrayOfNulls(mc.maps.size)
            for (k in mc.maps.indices) {
                StaticStore.mapnames[i][k] = MultiLangCont.SMNAME.getCont(mc.maps[k]) ?: ""
            }
        }
        StaticStore.maplang = 0
    }

    fun readALL() {
        readUnitLang()
        readEnemyLang()
        readStageLang()
        readMapLang()
    }
}