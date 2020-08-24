package com.mandarin.bcu.androidutil.unit

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.LangLoader
import com.mandarin.bcu.util.Interpret
import common.system.fake.ImageBuilder
import common.system.files.VFile
import java.io.IOException
import java.util.*

object Definer {
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
            if (!StaticStore.init) {
                StaticStore.clear()
                val shared2 = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
                StaticStore.getLang(shared2.getInt("Language", 0))
                ImageBuilder.builder = BMBuilder()
                DefineItf().init(context)
                StaticStore.init = true

                if (StaticStore.img15 == null) {
                    StaticStore.readImg(context)
                }

                for (i in colorid.indices) {
                    colorstring[i] = context.getString(colorid[i])
                }

                starstring[0] = ""

                for (i in starid.indices)
                    starstring[i + 1] = context.getString(starid[i])

                for (i in procid.indices)
                    proc[i] = context.getString(procid[i])

                for (i in abiid.indices)
                    abi[i] = context.getString(abiid[i])

                for (i in textid.indices)
                    textstring[i] = context.getString(textid[i])

                Interpret.TRAIT = colorstring
                Interpret.STAR = starstring
                Interpret.PROC = proc
                Interpret.ABIS = abi
                Interpret.TEXT = textstring
            }

            if (StaticStore.unitlang == 1) {
                LangLoader.readUnitLang(context)
            }

            if (StaticStore.fruit == null) {
                val path1 = "./org/page/catfruit/"

                val names = arrayOf("gatyaitemD_30_f.png", "gatyaitemD_31_f.png", "gatyaitemD_32_f.png", "gatyaitemD_33_f.png", "gatyaitemD_34_f.png", "gatyaitemD_35_f.png", "gatyaitemD_36_f.png"
                        , "gatyaitemD_37_f.png", "gatyaitemD_38_f.png", "gatyaitemD_39_f.png", "gatyaitemD_40_f.png", "gatyaitemD_41_f.png", "gatyaitemD_42_f.png", "datyaitemD_43_f.png", "xp.png")

                StaticStore.fruit = Array(names.size) {i ->
                    val vf = VFile.get(path1+names[i])

                    if(vf == null)
                        StaticStore.empty(1, 1)

                    val icon = vf.data?.img?.bimg()

                    if(icon == null)
                        StaticStore.empty(1, 1)

                    icon as Bitmap
                }
            }

            if (StaticStore.img15 == null) {
                StaticStore.readImg(context)
            }

            if (StaticStore.icons == null) {
                val number = StaticStore.anumber
                val files = StaticStore.afiles

                val iconpath = StaticStore.getExternalPath(context)+"org/page/icons/"

                StaticStore.icons = Array(number.size) {i ->
                    if(number[i] == 227) {
                        if(files[i].isNotEmpty()) {
                            BitmapFactory.decodeFile(iconpath+files[i])
                        } else {
                            StaticStore.empty(1, 1)
                        }
                    } else {
                        (StaticStore.img15?.get(number[i])?.bimg() ?: StaticStore.empty(1,1)) as Bitmap
                    }
                }
            }

            if (StaticStore.picons == null) {
                val number = StaticStore.pnumber
                val files = StaticStore.pfiles

                val iconpath = StaticStore.getExternalPath(context)+"org/page/icons/"

                StaticStore.picons = Array(number.size) {i ->
                    if(number[i] == 227) {
                        if(files[i].isNotEmpty()) {
                            BitmapFactory.decodeFile(iconpath+files[i])
                        } else {
                            StaticStore.empty(1, 1)
                        }
                    } else {
                        (StaticStore.img15?.get(number[i])?.bimg() ?: StaticStore.empty(1,1)) as Bitmap
                    }
                }
            }

            if (StaticStore.addition == null) {
                val addid = intArrayOf(R.string.unit_info_strong, R.string.unit_info_resis, R.string.unit_info_masdam, R.string.unit_info_exmon, R.string.unit_info_atkbs, R.string.unit_info_wkill, R.string.unit_info_evakill, R.string.unit_info_insres, R.string.unit_info_insmas)
                StaticStore.addition = Array(addid.size) { i ->
                    context.getString(addid[i])
                }
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
        StaticStore.addition = Array(addid.size) { i ->
            getString(context, addid[i], lang)
        }

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