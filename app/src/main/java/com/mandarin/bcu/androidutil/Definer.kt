package com.mandarin.bcu.androidutil

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import com.mandarin.bcu.androidutil.io.LangLoader
import com.mandarin.bcu.util.Interpret
import common.CommonStatic
import common.io.assets.AssetLoader
import common.pack.PackData
import common.pack.UserProfile
import common.system.fake.ImageBuilder
import common.system.files.VFile
import common.util.Data
import common.util.lang.ProcLang
import java.io.File
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList

object Definer {
    private val colorid = StaticStore.colorid
    private val starid = StaticStore.starid
    private val procid = StaticStore.procid
    private val abiid = StaticStore.abiid
    private val textid = StaticStore.textid

    fun define(context: Context, prog: Consumer<Double>, text: Consumer<String>) {
        try {
            if (!StaticStore.init) {
                StaticStore.clear()
                val shared2 = context.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
                StaticStore.getLang(shared2.getInt("Language", 0))
                ImageBuilder.builder = BMBuilder()
                DefineItf().init(context)
                AContext.check()
                CommonStatic.ctx.initProfile()

                AssetLoader.load(prog)

                UserProfile.getBCData().load(text, prog)

                StaticStore.init = true
            }

            if(!StaticStore.packRead) {
                text.accept(context.getString(R.string.main_pack))
                UserProfile.loadPacks(prog)

                StaticStore.packRead = true

                val f = File(StaticStore.getExternalPath(context)+"pack/")

                if(f.exists()) {
                    text.accept(context.getString(R.string.main_rev_reformat))

                    StaticStore.deleteFile(f, true)
                }

                val g = File(StaticStore.getExternalRes(context))

                if(g.exists()) {
                    text.accept(context.getString(R.string.main_rev_reformat))

                    StaticStore.deleteFile(g, true)
                }

                handlePacks(context)
            }

            text.accept(context.getString(R.string.load_add))
            prog.accept(-1.0/10000.0)

            if (StaticStore.treasure == null)
                StaticStore.readTreasureIcon()

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
                    val vf = VFile.get(path1+names[i]) ?: return@Array StaticStore.empty(1, 1)

                    val icon = vf.data?.img?.bimg() ?: return@Array StaticStore.empty(1, 1)

                    icon as Bitmap
                }
            }

            if (StaticStore.img15 == null) {
                StaticStore.readImg()
            }

            if (StaticStore.icons == null) {
                val number = StaticStore.anumber
                val files = StaticStore.afiles

                val iconpath = "./org/page/icons/"

                StaticStore.icons = Array(number.size) {i ->
                    if(number[i] == 227) {
                        if(files[i].isNotEmpty()) {
                            VFile.get(iconpath+files[i]).data.img.bimg() as Bitmap
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

                val iconpath = "./org/page/icons/"

                StaticStore.picons = Array(number.size) {i ->
                    if(number[i] == 227) {
                        if(files[i].isNotEmpty()) {
                            VFile.get(iconpath+files[i]).data.img.bimg() as Bitmap
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

            if(StaticStore.medalnumber == 0) {
                val vf = VFile.get("./org/page/medal")

                val lit = vf.list()

                if(lit != null) {
                    println(lit.size)

                    StaticStore.medalnumber = lit.size
                }
            }

            if(StaticStore.medalnumber != 0 && StaticStore.medallang == 1) {
                LangLoader.readMedalLang(context)
            }

            if(SoundHandler.play.isEmpty()) {
                SoundHandler.play = BooleanArray(UserProfile.getBCData().musics.list.size)
            }

            if (StaticStore.eicons == null) {
                StaticStore.eicons = Array(UserProfile.getBCData().enemies.list.size) { i ->
                    val shortPath = "./org/enemy/" + Data.trio(i) + "/enemy_icon_" + Data.trio(i) + ".png"
                    val vf = VFile.get(shortPath)

                    if(vf == null) {
                        StaticStore.empty(context, 18f, 18f)
                    }

                    val icon = vf.data.img.bimg()

                    if(icon == null) {
                        StaticStore.empty(context, 18f, 18f)
                    }

                    StaticStore.getResizeb(icon as Bitmap, context, 36f)
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

        ProcLang.clear()

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

    private fun handlePacks(c: Context) {
        val shared = c.getSharedPreferences(StaticStore.PACK, Context.MODE_PRIVATE)

        val editor = shared.edit()

        val checked = ArrayList<String>()

        for(p in UserProfile.getAllPacks()) {
            if(p is PackData.DefPack)
                continue

            if(p is PackData.UserPack) {
                if(!shared.contains(p.sid)) {
                    //New Pack detected

                    editor.putString(p.sid, "${p.desc.time} - ${p.desc.version}")

                    editor.apply()

                    extractMusic(p, shared)
                } else {
                    val info = shared.getString(p.sid, "")

                    val newInfo = "${p.desc.time} - ${p.desc.version}"

                    if(info != newInfo) {
                        //Update detected

                        editor.putString(p.sid, "${p.desc.time} - ${p.desc.version}")

                        editor.apply()

                        extractMusic(p, shared)
                    }
                }

                checked.add(p.sid)
            }
        }

        //Check if unchecked pack ID in shared preferences

        val notExisting = ArrayList<String>()

        for(any in shared.all) {
            if(any.value !is String)
                continue

            val value = any.key

            if(realNotExisting(notExisting, checked, value)) {
                if(value.contains("-")) {
                    val info = value.split("-")

                    if(info.size != 2) {
                        if(info.size != 2) {
                            Log.w("Definer::extractMusic", "Invalid music file format : $info")

                            continue
                        }

                        notExisting.add(info[0])
                    }
                } else {
                    notExisting.add(value)
                }
            }
        }

        Log.i("Definer::handlePacks", "Not existing pack list : $notExisting")

        //Then perform removing music files

        for(p in notExisting) {
            for(key in shared.all.keys) {
                if(key.startsWith(p)) {
                    if(key.contains("-")) {
                        val f = File(StaticStore.dataPath+"music/", key)

                        if(f.exists()) {
                            f.delete()
                        } else {
                            Log.i("Definer::handlePacks", "??? File is not existing : ${f.absolutePath}")
                        }
                    }

                    editor.remove(key)

                    editor.apply()
                }
            }
        }
    }

    private fun extractMusic(p: PackData.UserPack, shared: SharedPreferences) {
        if(p.musics.list.isEmpty())
            return

        val editor = shared.edit()

        if(CommonStatic.ctx == null || CommonStatic.ctx !is AContext)
            return

        for(m in p.musics.list) {
            val f = (CommonStatic.ctx as AContext).getMusicFile(m)

            if(!f.exists()) {
                val result = StaticStore.extractMusic(m, f)

                editor.putString(result.name, StaticStore.fileToMD5(result))
            } else {
                val md5 = shared.getString(f.name, "")

                val newMD5 = StaticStore.streamToMD5(m.data.stream)

                if(md5 != newMD5) {
                    Log.i("Deinfer::extractMusic","New MD5 detected : ID = ${m.id}, MD5 = $md5, newMD5 = $newMD5")

                    val result = StaticStore.extractMusic(m, f)

                    editor.putString(result.name, newMD5)
                }
            }
        }

        editor.apply()

        //Handle deleted music
        //Get music file list of specified pack first

        val mList = ArrayList<File>()

        val fList = File(StaticStore.dataPath+"music/").listFiles() ?: return

        for(f in fList) {
            if(f.name.startsWith("${p.sid}-"))
                mList.add(f)
        }

        //Then compare if specified music file is existing in pack too

        for(m in mList) {
            val info = m.name.split("-")

            if(info.size != 2) {
                Log.w("Definer::extractMusic", "Invalid music file format : $info")

                continue
            }

            val music = p.musics[CommonStatic.parseIntN(info[1])]

            if(music == null) {
                Log.i("Definer::extractMusic", "Deleted music : ${m.absolutePath}")

                m.delete()

                editor.remove(m.name)
            }
        }

        editor.apply()
    }

    private fun realNotExisting(notExisting: ArrayList<String>, checked: ArrayList<String>, target: String) : Boolean {
        for(p in checked) {
            if(target.startsWith(p))
                return false
        }

        for(u in notExisting) {
            if(target.startsWith(u))
                return false
        }

        return true
    }
}