package com.mandarin.bcu.androidutil.battle.sound

import android.os.Environment
import com.mandarin.bcu.androidutil.battle.asynchs.SoundAsync
import common.CommonStatic
import common.util.pack.Pack
import java.io.File
import java.util.*

object SoundHandler {
    @JvmField
    var MUSIC = SoundPlayer()
    @JvmField
    var SE = ArrayList<ArrayDeque<SoundPlayer>>()
    @JvmField
    var play: BooleanArray? = null
    @JvmField
    var inBattle = false
    @JvmField
    var battleEnd = false
    @JvmField
    var twoMusic = false
    @JvmField
    var haveToChange = false
    @JvmField
    var Changed = false
    @JvmField
    var musicPlay = true
    @JvmField
    var mu1 = 3
    var sePlay = true
    @JvmField
    var se_vol = 1f
    @JvmField
    var mu_vol = 1f
    @JvmField
    var speed = 0
    private const val MAX = 30
    @JvmField
    var available = MAX
    @JvmStatic
    fun read() {
        val path = Environment.getExternalStorageDirectory().absolutePath + "/Android/data/com.mandarin.bcu/music/"
        val mf = File(path)
        if (!mf.exists()) return
        for (i in mf.listFiles().indices) {
            SE.add(ArrayDeque())
        }
        play = BooleanArray(mf.listFiles().size)
        for (f in mf.listFiles()) {
            val name = f.name
            if (name.length != 7) continue
            if (!name.endsWith("ogg")) continue
            val id = CommonStatic.parseIntN(name.substring(0, 3))
            if (id < 0) continue
            Pack.def.ms[id] = f
        }
    }

    @JvmStatic
    fun setSE(ind: Int) {
        if (speed > 1) return
        if (play!![ind]) return
        if (battleEnd) return
        SoundAsync(ind).execute()
        play!![ind] = true
    }

    @JvmStatic
    fun getMP(ind: Int): SoundPlayer {
        if (!SE[ind].isEmpty()) {
            val mp = SE[ind].pollFirst()
            if (mp == null) {
                available--
                return SoundPlayer()
            }
            available--
            return mp
        }
        available--
        return SoundPlayer()
    }

    @JvmStatic
    fun returnBack(mp: SoundPlayer, ind: Int) {
        available++
        SE[ind].add(mp)
    }

    @JvmStatic
    fun releaseAll() {
        for (d in SE) {
            for (mp in d) {
                mp!!.release()
            }
        }
        available = MAX
    }

    @JvmStatic
    fun resetHandler() {
        releaseAll()
        for (i in play!!.indices) play!![i] = false
        inBattle = false
        battleEnd = false
        twoMusic = false
        haveToChange = false
        Changed = false
        mu1 = 3
        speed = 0
    }
}