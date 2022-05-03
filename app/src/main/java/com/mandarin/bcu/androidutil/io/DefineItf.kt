@file:Suppress("DEPRECATION")

package com.mandarin.bcu.androidutil.io

import android.content.Context
import android.media.MediaMetadataRetriever
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.battle.sound.SoundHandler
import com.mandarin.bcu.androidutil.music.OggDataSource
import common.CommonStatic
import common.CommonStatic.Itf
import common.pack.Identifier
import common.util.stage.Music
import java.io.File

class DefineItf : Itf {
    companion object {
        var dir: String = ""

        fun check(c: Context) {
            if(dir == "") {
                dir = StaticStore.getExternalPath(c)
            }
        }
    }

    override fun save(save: Boolean, exit: Boolean) {}

    override fun getMusicLength(f: Music?): Long {
        f ?: return -1

        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(OggDataSource(f.data))

        return mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: -1
    }

    @Deprecated("Deprecated in Java")
    override fun route(path: String?): File {
        val realPath = path?.replace("./",dir) ?: ""

        return File(realPath)
    }

    override fun setSE(ind: Int) {
        SoundHandler.setSE(ind)
    }

    override fun setBGM(mus: Identifier<Music>) {
        SoundHandler.setBGM(mus)
    }

    fun init(c: Context) {
        dir = StaticStore.getExternalPath(c)

        CommonStatic.def = this
    }
}