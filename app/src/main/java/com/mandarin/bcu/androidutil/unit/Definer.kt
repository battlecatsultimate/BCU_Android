package com.mandarin.bcu.androidutil.unit

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.decode.ZipLib
import com.mandarin.bcu.util.Interpret
import common.CommonStatic
import common.battle.BasisSet
import common.battle.data.PCoin
import common.system.MultiLangCont
import common.system.fake.ImageBuilder
import common.system.files.AssetData
import common.util.pack.Pack
import common.util.unit.Combo
import common.util.unit.Unit
import java.io.File
import java.io.IOException
import java.util.*

class Definer {
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
    private val lan = arrayOf("/en/", "/zh/", "/kr/", "/jp/")
    private val files = arrayOf("UnitName.txt", "UnitExplanation.txt", "CatFruitExplanation.txt", "ComboName.txt")
    fun define(context: Context) {
        try {
            if (StaticStore.units == null) {
                try {
                    StaticStore.getUnitnumber()
                    ImageBuilder.builder = BMBuilder()
                    DefineItf().init()
                    Unit.readData()
                    PCoin.read()
                    Combo.readFile()
                } catch (e: Exception) {
                    StaticStore.clear()
                    val shared2 = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
                    StaticStore.getLang(shared2.getInt("Language", 0))
                    ZipLib.init()
                    ZipLib.read()
                    ImageBuilder.builder = BMBuilder()
                    StaticStore.getUnitnumber()
                    DefineItf().init()
                    Unit.readData()
                    PCoin.read()
                    Combo.readFile()
                    StaticStore.root = 1
                    println(StaticStore.unitnumber)
                }
                StaticStore.units = Pack.def.us.ulist.list
                if (StaticStore.img15 == null) {
                    StaticStore.readImg()
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
            if (StaticStore.unitlang == 1) {
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
            if (StaticStore.t == null) {
                Combo.readFile()
                StaticStore.t = BasisSet.current.t()
            }
            if (StaticStore.fruit == null) {
                val path1 = Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU/files/org/page/catfruit/"
                val f = File(path1)
                StaticStore.fruit = arrayOfNulls(f.listFiles().size)
                val names = arrayOf("gatyaitemD_30_f.png", "gatyaitemD_31_f.png", "gatyaitemD_32_f.png", "gatyaitemD_33_f.png", "gatyaitemD_34_f.png", "gatyaitemD_35_f.png", "gatyaitemD_36_f.png"
                        , "gatyaitemD_37_f.png", "gatyaitemD_38_f.png", "gatyaitemD_39_f.png", "gatyaitemD_40_f.png", "gatyaitemD_41_f.png", "gatyaitemD_42_f.png", "xp.png")
                for (i in names.indices) {
                    StaticStore.fruit[i] = BitmapFactory.decodeFile(path1 + names[i])
                }
            }
            if (StaticStore.icons == null) {
                val number = StaticStore.anumber
                StaticStore.icons = arrayOfNulls(number.size)
                for (i in number.indices) StaticStore.icons[i] = StaticStore.img15[number[i]].bimg() as Bitmap
                val iconpath = Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU/files/org/page/icons/"
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
                val iconpath = Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU/files/org/page/icons/"
                val files = StaticStore.pfiles
                for (i in files.indices) {
                    if (files[i] == "") continue
                    StaticStore.picons[i] = BitmapFactory.decodeFile(iconpath + files[i])
                }
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

    fun redefine(context: Context, lang: String) {
        val shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        StaticStore.getLang(shared.getInt("Language", 0))
        for (i in colorid.indices) {
            colorstring[i] = getString(context, colorid[i], lang)
        }
        starstring[0] = ""
        for (i in starid.indices) starstring[i + 1] = getString(context, starid[i], lang)
        for (i in procid.indices) proc[i] = getString(context, procid[i], lang)
        for (i in abiid.indices) abi[i] = getString(context, abiid[i], lang)
        for (i in textid.indices) textstring[i] = getString(context, textid[i], lang)
        val addid = intArrayOf(R.string.unit_info_strong, R.string.unit_info_resis, R.string.unit_info_masdam, R.string.unit_info_exmon, R.string.unit_info_atkbs, R.string.unit_info_wkill, R.string.unit_info_evakill, R.string.unit_info_insres, R.string.unit_info_insmas)
        StaticStore.addition = arrayOfNulls(addid.size)
        for (i in addid.indices) StaticStore.addition[i] = getString(context, addid[i], lang)
        Interpret.TRAIT = colorstring
        Interpret.STAR = starstring
        Interpret.PROC = proc
        Interpret.ABIS = abi
        Interpret.TEXT = textstring
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun getString(context: Context, id: Int, lang: String): String {
        val locale = Locale(lang)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration).resources.getString(id)
    }
}