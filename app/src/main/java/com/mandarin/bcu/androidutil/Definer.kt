package com.mandarin.bcu.androidutil

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.io.LangLoader
import com.mandarin.bcu.util.Interpret
import common.CommonStatic
import common.pack.PackData
import common.pack.UserProfile
import common.system.fake.ImageBuilder
import common.system.files.VFile
import java.io.IOException
import java.util.*

object Definer {
    private val colorid = StaticStore.colorid
    private val starid = StaticStore.starid
    private val procid = StaticStore.procid
    private val abiid = StaticStore.abiid
    private val textid = StaticStore.textid

    fun define(context: Context) {
        try {
            if (!StaticStore.init) {
                StaticStore.clear()
                val shared2 = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
                StaticStore.getLang(shared2.getInt("Language", 0))
                ImageBuilder.builder = BMBuilder()
                DefineItf().init(context)
                AContext.check()
                CommonStatic.ctx.initProfile()
                StaticStore.init = true
            }

            if(Interpret.TRAIT.isEmpty()) {
                val colorString = Array(colorid.size) {
                    context.getString(colorid[it])
                }

                Interpret.TRAIT = colorString
            }

            if(Interpret.STAR.isEmpty()) {
                val startString = Array(5) {
                    if(it == 0)
                        ""
                    else
                        context.getString(starid[it-1])
                }

                Interpret.STAR = startString
            }

            if(Interpret.PROC.isEmpty()) {
                val procString = Array(procid.size) {
                    context.getString(procid[it])
                }

                Interpret.PROC = procString
            }

            if(Interpret.ABIS.isEmpty()) {
                val abiString = Array(abiid.size) {
                    context.getString(abiid[it])
                }

                Interpret.ABIS = abiString
            }

            if(Interpret.TEXT.isEmpty()) {
                val textString = Array(textid.size) {
                    context.getString(textid[it])
                }

                Interpret.TEXT = textString
            }

            if (StaticStore.unitlang == 1) {
                LangLoader.readUnitLang(context)
            }

            if(StaticStore.enemeylang == 1) {
                LangLoader.readEnemyLang(context)
            }

            if(StaticStore.stagelang == 1) {
                LangLoader.readStageLang(context)
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
                StaticStore.readImg()
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

            if (StaticStore.addition.isEmpty()) {
                val addid = intArrayOf(R.string.unit_info_strong, R.string.unit_info_resis, R.string.unit_info_masdam, R.string.unit_info_exmon, R.string.unit_info_atkbs, R.string.unit_info_wkill, R.string.unit_info_evakill, R.string.unit_info_insres, R.string.unit_info_insmas)
                StaticStore.addition = Array(addid.size) { i ->
                    context.getString(addid[i])
                }
            }

            val packs = UserProfile.getAllPacks()

            if(StaticStore.mapcolcname.isEmpty()) {
                if(packs.size != 1 && StaticStore.mapcode.size == StaticStore.BCmaps) {
                    for(p in packs) {
                        if(p is PackData.DefPack)
                            continue
                        else if(p is PackData.UserPack) {
                            if(p.mc.maps.list.isNotEmpty()) {
                                StaticStore.mapcode.add(p.mc.sid)
                            }
                        }
                    }
                }

                for(i in StaticStore.bcMapNames) {
                    StaticStore.mapcolcname.add(context.getString(i))
                }

                for(p in packs) {
                    if(p is PackData.DefPack)
                        continue
                    else if(p is PackData.UserPack) {
                        if(p.mc.maps.list.isNotEmpty()) {
                            val k = StaticStore.getPackName(p.mc.sid)

                            StaticStore.mapcolcname.add(k)
                        }
                    }
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()

            ErrorLogWriter.writeLog(e, StaticStore.upload, context)
        }
    }

    fun redefine(context: Context, lang: String) {
        val shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)

        StaticStore.getLang(shared.getInt("Language", 0))

        val addid = intArrayOf(R.string.unit_info_strong, R.string.unit_info_resis, R.string.unit_info_masdam, R.string.unit_info_exmon, R.string.unit_info_atkbs, R.string.unit_info_wkill, R.string.unit_info_evakill, R.string.unit_info_insres, R.string.unit_info_insmas)
        StaticStore.addition = Array(addid.size) { i ->
            getString(context, addid[i], lang)
        }

        val colorString = Array(colorid.size) {
            context.getString(colorid[it])
        }

        val startString = Array(5) {
            if(it == 0)
                ""
            else
                context.getString(starid[it-1])
        }

        val procString = Array(procid.size) {
            context.getString(procid[it])
        }

        val abiString = Array(abiid.size) {
            context.getString(abiid[it])
        }

        val textString = Array(textid.size) {
            context.getString(textid[it])
        }

        Interpret.TRAIT = colorString
        Interpret.STAR = startString
        Interpret.PROC = procString
        Interpret.ABIS = abiString
        Interpret.TEXT = textString
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun getString(context: Context, id: Int, lang: String): String {
        val locale = Locale(lang)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration).resources.getString(id)
    }
}