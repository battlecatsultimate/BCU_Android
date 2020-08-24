package com.mandarin.bcu.androidutil.io

import android.content.Context
import com.mandarin.bcu.androidutil.StaticStore
import common.CommonStatic
import common.pack.UserProfile
import common.system.files.VFile
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.stage.MapColc
import java.io.File

object LangLoader {
    fun readUnitLang(c: Context) {
        val lan = arrayOf("/en/", "/zh/", "/kr/", "/jp/")
        val files = arrayOf("UnitName.txt", "UnitExplanation.txt", "CatFruitExplanation.txt", "ComboName.txt")

        MultiLangCont.getStatic().FNAME.clear()
        MultiLangCont.getStatic().FEXP.clear()
        MultiLangCont.getStatic().CFEXP.clear()
        MultiLangCont.getStatic().COMNAME.clear()

        for (l in lan) {
            for (n in files) {
                val path = "./lang$l$n"
                val f = File(path.replace("./", StaticStore.getExternalAsset(c)))
                if (f.exists()) {
                    val qs = VFile.readLine(path)
                    when (n) {
                        "UnitName.txt" -> {
                            val size = qs.size
                            var j = 0
                            while (j < size) {
                                val strs = qs?.poll()?.trim { it <= ' ' }?.split("\t")?.toTypedArray() ?: break

                                val u = UserProfile.getBCData().units[CommonStatic.parseIntN(strs[0])]

                                if (u == null) {
                                    j++
                                    continue
                                }

                                var i = 0

                                while (i < u.forms.size.coerceAtMost(strs.size - 1)) {
                                    MultiLangCont.getStatic().FNAME.put(l.substring(1, l.length - 1), u.forms[i], strs[i + 1].trim { it <= ' ' })
                                    i++
                                }

                                j++
                            }
                        }
                        "UnitExplanation.txt" -> {
                            val size = qs.size
                            var j = 0
                            while (j < size) {
                                val strs = qs?.poll()?.trim { it <= ' ' }?.split("\t")?.toTypedArray() ?: return

                                val u = UserProfile.getBCData().units[CommonStatic.parseIntN(strs[0])]

                                if (u == null) {
                                    j++
                                    continue
                                }

                                var i = 0
                                while (i < u.forms.size.coerceAtMost(strs.size - 1)) {
                                    val lines = strs[i + 1].trim { it <= ' ' }.split("<br>").toTypedArray()
                                    MultiLangCont.getStatic().FEXP.put(l.substring(1, l.length - 1), u.forms[i], lines)
                                    i++
                                }
                                j++
                            }
                        }
                        "CatFruitExplanation.txt" -> for (str in qs) {
                            val strs = str.trim { it <= ' ' }.split("\t").toTypedArray()
                            val u = UserProfile.getBCData().units[CommonStatic.parseIntN(strs[0])] ?: continue
                            if (strs.size == 1) {
                                continue
                            }
                            val lines = strs[1].split("<br>").toTypedArray()
                            MultiLangCont.getStatic().CFEXP.put(l.substring(1, l.length - 1), u.info, lines)
                        }
                        "ComboName.txt" -> for (str in qs) {
                            val strs = str.trim { it <= ' ' }.split("\t").toTypedArray()
                            if (strs.size <= 1) {
                                continue
                            }
                            val id = strs[0].trim { it <= ' ' }.toInt()
                            val name = strs[1].trim { it <= ' ' }
                            MultiLangCont.getStatic().COMNAME.put(l.substring(1, l.length - 1), id, name)
                        }
                    }
                }
            }
        }

        StaticStore.unitlang = 0
    }

    fun readEnemyLang(c: Context) {
        val lan = arrayOf("/en/", "/zh/", "/kr/", "/jp/")
        val files = arrayOf("UnitName.txt", "UnitExplanation.txt", "CatFruitExplanation.txt", "ComboName.txt")

        MultiLangCont.getStatic().ENAME.clear()
        MultiLangCont.getStatic().EEXP.clear()
        for (l in lan) {
            for (n in files) {
                val path = "./lang$l$n"
                val f = File(path.replace("./", StaticStore.getExternalAsset(c)))
                if (f.exists()) {
                    val qs = VFile.readLine(path)

                    when (n) {
                        "EnemyName.txt" -> for (str in qs) {
                            val strs = str.trim { it <= ' ' }.split("\t").toTypedArray()

                            val em = UserProfile.getBCData().enemies[CommonStatic.parseIntN(strs[0])] ?: continue

                            if (strs.size == 1)
                                MultiLangCont.getStatic().ENAME.put(l.substring(1, l.length - 1), em, null)
                            else
                                MultiLangCont.getStatic().ENAME.put(l.substring(1, l.length - 1), em, if (strs[1].trim { it <= ' ' }.startsWith("【")) strs[1].trim { it <= ' ' }.substring(1, strs[1].trim { it <= ' ' }.length - 1) else strs[1].trim { it <= ' ' })
                        }
                        "EnemyExplanation.txt" -> for (str in qs) {
                            val strs = str.trim { it <= ' ' }.split("\t").toTypedArray()
                            val em = UserProfile.getBCData().enemies[CommonStatic.parseIntN(strs[0])]
                                    ?: continue
                            if (strs.size == 1)
                                MultiLangCont.getStatic().EEXP.put(l.substring(1, l.length - 1), em, null)
                            else {
                                val lines = strs[1].trim { it <= ' ' }.split("<br>").toTypedArray()
                                MultiLangCont.getStatic().EEXP.put(l.substring(1, l.length - 1), em, lines)
                            }
                        }
                    }
                }
            }
        }
        StaticStore.enemeylang = 0
    }

    fun readStageLang(c: Context) {
        val lan = arrayOf("/en/", "/zh/", "/kr/", "/jp/")
        val file = "StageName.txt"
        val diff = "Difficulty.txt"
        val rewa = "RewardName.txt"

        MultiLangCont.getStatic().SMNAME.clear()
        MultiLangCont.getStatic().STNAME.clear()
        MultiLangCont.getStatic().RWNAME.clear()

        for (l in lan) {
            val path = "./lang$l$file"
            val f = File(path.replace("./", StaticStore.getExternalAsset(c)))
            if (f.exists()) {
                val qs = VFile.readLine(path)

                if (qs != null) {
                    for (s in qs) {
                        val strs = s.trim { it <= ' ' }.split("\t").toTypedArray()

                        if (strs.size == 1)
                            continue

                        val id = strs[0].trim { it <= ' ' }
                        val name = strs[strs.size - 1].trim { it <= ' ' }

                        if (id.isEmpty() || name.isEmpty())
                            continue

                        val ids = id.split("-").toTypedArray()

                        val id0 = CommonStatic.parseIntN(ids[0].trim { it <= ' ' })

                        val mc = MapColc.get(Data.hex(id0)) ?: continue

                        if (ids.size == 1) {
                            MultiLangCont.getStatic().MCNAME.put(l.substring(1, l.length - 1), mc, name)
                            continue
                        }

                        val id1 = CommonStatic.parseIntN(ids[1].trim { it <= ' ' })

                        if (id1 >= mc.maps.list.size || id1 < 0)
                            continue

                        val stm = mc.maps.list[id1] ?: continue

                        if (ids.size == 2) {
                            MultiLangCont.getStatic().SMNAME.put(l.substring(1, l.length - 1), stm, name)
                            continue
                        }
                        val id2 = CommonStatic.parseIntN(ids[2].trim { it <= ' ' })
                        if (id2 >= stm.list.list.size || id2 < 0) continue
                        val st = stm.list.list[id2]
                        MultiLangCont.getStatic().STNAME.put(l.substring(1, l.length - 1), st, name)
                    }
                }
            }
        }
        for (l in lan) {
            val path = "./lang$l$rewa"
            val f = File(path.replace("./", StaticStore.getExternalAsset(c)))
            if (f.exists()) {
                val qs = VFile.readLine(path)
                if (qs != null) {
                    for (s in qs) {
                        val strs = s.trim { it <= ' ' }.split("\t").toTypedArray()

                        if (strs.size <= 1)
                            continue

                        val id = strs[0].trim { it <= ' ' }

                        val name = strs[1].trim { it <= ' ' }

                        MultiLangCont.getStatic().RWNAME.put(l.substring(1, l.length - 1), id.toInt(), name)
                    }
                }
            }
        }

        val path = "./lang/$diff"
        val f = File(path.replace("./", StaticStore.getExternalAsset(c)))

        if (f.exists()) {
            val qs = VFile.readLine(path)

            if (qs != null) {
                for (s in qs) {
                    val strs = s.trim { it <= ' ' }.split("\t").toTypedArray()

                    if (strs.size < 2)
                        continue

                    val num = strs[1].trim { it <= ' ' }
                    val numbers = strs[0].trim { it <= ' ' }.split("-").toTypedArray()

                    if (numbers.size < 3)
                        continue

                    val id0 = CommonStatic.parseIntN(numbers[0].trim { it <= ' ' })
                    val id1 = CommonStatic.parseIntN(numbers[1].trim { it <= ' ' })
                    val id2 = CommonStatic.parseIntN(numbers[2].trim { it <= ' ' })

                    val mc = MapColc.get(Data.hex(id0)) ?: continue

                    if (id1 >= mc.maps.list.size || id1 < 0)
                        continue

                    val stm = mc.maps.list[id1] ?: continue

                    if (id2 >= stm.list.list.size || id2 < 0)
                        continue

                    val st = stm.list[id2]

                    st.info.diff = num.toInt()
                }
            }
        }
        StaticStore.stagelang = 0
    }

    fun readALL(c: Context) {
        readUnitLang(c)
        readEnemyLang(c)
        readStageLang(c)
    }
}