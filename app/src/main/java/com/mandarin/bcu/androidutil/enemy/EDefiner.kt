package com.mandarin.bcu.androidutil.enemy

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.LangLoader
import com.mandarin.bcu.util.Interpret
import common.system.fake.ImageBuilder
import java.io.IOException

object EDefiner {
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
                val shared = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
                StaticStore.getLang(shared.getInt("Language", 0))
                ImageBuilder.builder = BMBuilder()
                DefineItf().init(context)
                StaticStore.init = true

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
                LangLoader.readEnemyLang(context)
            }

            if (StaticStore.addition == null) {
                val addid = intArrayOf(R.string.unit_info_strong, R.string.unit_info_resis, R.string.unit_info_masdam, R.string.unit_info_exmon, R.string.unit_info_atkbs, R.string.unit_info_wkill, R.string.unit_info_evakill, R.string.unit_info_insres, R.string.unit_info_insmas)
                StaticStore.addition = Array(addid.size) {i ->
                    context.getString(addid[i])
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}