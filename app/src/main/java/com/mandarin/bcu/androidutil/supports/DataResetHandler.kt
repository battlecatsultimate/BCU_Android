package com.mandarin.bcu.androidutil.supports

import android.content.Context
import com.mandarin.bcu.androidutil.StaticStore
import java.io.File

class DataResetHandler(val text: String, val loading: String, private val type: TYPE, vararg assets: String) {
    enum class TYPE {
        LINEUP,
        PACK,
        ASSET,
        MUSIC,
        LANG,
        LOG
    }

    private val asset: String = if(assets.isEmpty()) {
        if (type == TYPE.ASSET)
            throw IllegalStateException("Asset name must be specified if type is ASSET")
        else
            ""
    } else
        assets[0]

    var doPerform = false

    fun performReset(context: Context) : Boolean {
        when(type) {
            TYPE.LINEUP -> {
                val path = File(StaticStore.getExternalUser(context) + "basis.json")

                if(path.exists()) {
                    return path.delete()
                }

                return true
            }
            TYPE.PACK -> {
                val path = File(StaticStore.getExternalPack(context))

                if(path.exists()) {
                    return StaticStore.deleteFile(path, false)
                }

                return true
            }
            TYPE.ASSET -> {
                if(asset.isBlank()) {
                    return true
                }

                val path = File(StaticStore.getExternalAsset(context)+"assets/"+asset)

                if(path.exists())
                    return path.delete()

                return true
            }
            TYPE.LANG -> {
                val path = File(StaticStore.getExternalAsset(context)+"lang/")

                if(path.exists()) {
                    return StaticStore.deleteFile(path, false)
                }

                return true
            }
            TYPE.MUSIC -> {
                val path = File(StaticStore.getExternalAsset(context)+"music/")

                if(path.exists()) {
                    return StaticStore.deleteFile(path, false)
                }

                return true
            }
            TYPE.LOG -> {
                val path = File(StaticStore.getExternalLog(context))

                if(path.exists()) {
                    return StaticStore.deleteFile(path, false)
                }

                return true
            }
            else -> {
                throw IllegalStateException("Invalid enum TYPE passed : $type")
            }
        }
    }
}