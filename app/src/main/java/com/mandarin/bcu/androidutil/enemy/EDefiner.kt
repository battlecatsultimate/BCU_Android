package com.mandarin.bcu.androidutil.enemy

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.decode.ZipLib
import com.mandarin.bcu.util.Interpret
import common.CommonStatic
import common.battle.BasisSet
import common.system.MultiLangCont
import common.system.fake.ImageBuilder
import common.system.files.AssetData
import common.util.pack.Pack
import common.util.unit.Combo
import common.util.unit.Enemy
import java.io.File
import java.io.IOException

class EDefiner {
    private val lan = arrayOf("/en/", "/zh/", "/kr/", "/jp/")
    private val files = arrayOf("EnemyName.txt", "EnemyExplanation.txt")
    private val colorid = StaticStore.colorid
    private val starid = StaticStore.starid
    private val starstring = arrayOfNulls<String>(5)
    private val colorstring = arrayOfNulls<String>(colorid.size)
    private val procid = StaticStore.procid
    private val proc = arrayOfNulls<String>(procid.size)
    private val abiid = StaticStore.abiid
    private val abi = arrayOfNulls<String>(abiid.size)
    private val textid = StaticStore.textid
    private val textstring = arrayOfNulls<String>(textid.size)
    fun define(context: Context) {
        try {
            if (StaticStore.enemies == null) {
                try {
                    StaticStore.getEnemyNumber(context)
                    Enemy.readData()
                } catch (e: NullPointerException) {
                    StaticStore.clear()
                    val shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
                    StaticStore.getLang(shared.getInt("Language", 0))
                    ZipLib.init(StaticStore.getExternalPath(context))
                    ZipLib.read(StaticStore.getExternalPath(context))
                    ImageBuilder.builder = BMBuilder()
                    StaticStore.getEnemyNumber(context)
                    DefineItf().init(context)
                    Enemy.readData()
                    StaticStore.root = 1
                }
                StaticStore.enemies = Pack.def.es.list
                if (StaticStore.img15 == null) {
                    StaticStore.readImg(context)
                }
                if (StaticStore.t == null) {
                    Combo.readFile()
                    StaticStore.t = BasisSet.current.t()
                }
                if (StaticStore.icons == null) {
                    val number = StaticStore.anumber
                    StaticStore.icons = arrayOfNulls(number.size)
                    for (i in number.indices) StaticStore.icons[i] = StaticStore.img15[number[i]].bimg() as Bitmap
                    val iconpath = StaticStore.getExternalPath(context)+"org/page/icons/"
                    val files = StaticStore.afiles
                    for (i in files.indices) {
                        if (files[i] == "") continue
                        StaticStore.icons[i] = BitmapFactory.decodeFile(iconpath + files[i])
                    }
                }
                if (StaticStore.picons == null) {
                    val number = StaticStore.pnumber
                    StaticStore.picons = arrayOfNulls(number.size)
                    for (i in number.indices) StaticStore.picons[i] = StaticStore.img15[number[i]].bimg() as Bitmap
                    val iconpath = StaticStore.getExternalPath(context)+"org/page/icons/"
                    val files = StaticStore.pfiles
                    for (i in files.indices) {
                        if (files[i] == "") continue
                        StaticStore.picons[i] = BitmapFactory.decodeFile(iconpath + files[i])
                    }
                }

                for (i in colorid.indices) {
                    colorstring[i] = context.getString(colorid[i])
                }

                starstring[0] = ""

                for (i in starid.indices) starstring[i + 1] = context.getString(starid[i])
                for (i in procid.indices) proc[i] = context.getString(procid[i])
                for (i in abiid.indices) abi[i] = context.getString(abiid[i])
                for (i in textid.indices) textstring[i] = context.getString(textid[i])
                Interpret.TRAIT = colorstring
                Interpret.STAR = starstring
                Interpret.PROC = proc
                Interpret.ABIS = abi
                Interpret.TEXT = textstring
            }
            if (StaticStore.enemeylang == 1) {
                MultiLangCont.ENAME.clear()
                MultiLangCont.EEXP.clear()
                for (l in lan) {
                    for (n in files) {
                        val path = StaticStore.getExternalPath(context)+"lang" + l + n
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
            if (StaticStore.addition == null) {
                val addid = intArrayOf(R.string.unit_info_strong, R.string.unit_info_resis, R.string.unit_info_masdam, R.string.unit_info_exmon, R.string.unit_info_atkbs, R.string.unit_info_wkill, R.string.unit_info_evakill, R.string.unit_info_insres, R.string.unit_info_insmas)
                StaticStore.addition = arrayOfNulls(addid.size)
                for (i in addid.indices) StaticStore.addition[i] = context.getString(addid[i])
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}